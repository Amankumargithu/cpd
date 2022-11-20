package com.quodd.distributor;

import static com.quodd.controller.NewsEdgeDataController.logger;
import static com.quodd.controller.NewsEdgeDataController.newsProperties;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.jms.BytesMessage;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import com.csvreader.CsvReader;
import com.quodd.db.DatabaseConnectionManager;
import com.quodd.queue.MessageQueue;
import com.quodd.util.Constants;
import com.quodd.util.CounterUtility;
import com.quodd.util.DbCallable;
import com.quodd.util.FutureAnalyser;
import com.quodd.util.NewsBean;
import com.quodd.util.QueryInfoBean;

import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;
import ch.softwired.jms.IBusTopicPublisher;

public class NewsEdgeDataDistributor extends Thread {

	private static final String TABLE_NAME = "NEWSEDGE_INFO";
	private MessageQueue queue;
	private int flag = 0;
	private boolean doRun = true;
	private StringBuilder batchBuffer = new StringBuilder();
	private StringBuilder companyNewsQuery = new StringBuilder();
	private final Set<String> tickerSet = new HashSet<>();
	private final Connection con = DatabaseConnectionManager.getConnection();
	private TopicPublisher newsPublisher;
	private BytesMessage bm;
	private int newsDistributionCount = 0;
	private SimpleDateFormat dt = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss");
	private long storyId;
	private boolean lastRun = false;
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final ConcurrentLinkedQueue<Future<QueryInfoBean>> futureQueue = new ConcurrentLinkedQueue<>();
	private final FutureAnalyser futureAnalyser;

	public NewsEdgeDataDistributor() {
		futureAnalyser = new FutureAnalyser(futureQueue);
		futureAnalyser.setName("Future Analyser");
		futureAnalyser.start();
		createTickerSet();
		String query = "SELECT MAX(STORY_ID) FROM NEWSEDGE_INFO";
		logger.info("QUERY " + query);
		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
			if (rs.next()) {
				storyId = rs.getLong(1);
			}
			logger.info("Starting story ID from: " + storyId);
		} catch (Exception e) {
			logger.log(Level.WARNING, " Unbale to load data from Database: " + e.getMessage(), e);
			System.exit(0);
		}
	}

	private void createTickerSet() {
		String mongoTickerFilePath = newsProperties.getStringProperty("MONGO_TICKER_FILE_PATH",
				"/home/process/properties/MongoTicker.csv");
		CsvReader reader = null;
		try {
			reader = new CsvReader(mongoTickerFilePath);
			reader.readHeaders();
			while (reader.readRecord()) {
				String ticker = reader.get("TICKER");
				if (ticker != null && !ticker.trim().isEmpty())
					tickerSet.add(reader.get("TICKER"));
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null)
				reader.close();
		}
		String oracleTickerFilePath = newsProperties.getStringProperty("ORACLE_TICKER_FILE_PATH",
				"/home/process/properties/OracleTicker.csv");
		try {
			reader = new CsvReader(oracleTickerFilePath);
			reader.readHeaders();
			while (reader.readRecord()) {
				String ticker = reader.get("TICKER");
				if (ticker != null && !ticker.trim().isEmpty())
					tickerSet.add(reader.get("TICKER"));
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null)
				reader.close();
		}
		logger.info("total tickers read from files:" + tickerSet.size());
	}

	public void add(NewsBean bean) {
		queue.add(bean);
	}

	@Override
	public void run() {
		try {
			logger.info("Start thread " + this.getName());
			String jmsClientID = newsProperties.getStringProperty("JMS_CLIENT_ID", "newsedgenews");
			String jmsTopic = newsProperties.getStringProperty("TOPIC_NAME", "/news/newsedge");
			IBusJMSContext.getTopicConnection().setClientID(jmsClientID);
			TopicSession session = IBusJMSContext.createTopicSession(false, TopicSession.DUPS_OK_ACKNOWLEDGE);
			IBusTopic newsTopic = (IBusTopic) IBusJMSContext.getTopic(jmsTopic);
			newsPublisher = session.createPublisher(newsTopic);
			((IBusTopicPublisher) newsPublisher).setDisableMessageClone(true);
			bm = session.createBytesMessage();
			while (doRun || lastRun) {
				if (!doRun) {
					lastRun = false;
					logger.info("Thread in last run state" + this.getName());
				}
				try {
					Object[] items = queue.removeAll();
					if (items == null)
						continue;
					long uniqueStoryId = 0;
					for (int i = 0; i < items.length; i++) {
						NewsBean newsBean = (NewsBean) items[i];
						if (newsBean.getPublishReason().contains("ORIGINAL")) {
							uniqueStoryId = ++storyId;
							createBatch(newsBean, uniqueStoryId);
							distributeToDatabase(batchBuffer.toString(), companyNewsQuery.toString());
							String publishDate = newsBean.getPublishDate();
							String receivedDate = newsBean.getNewsDate();
							if (publishDate != null && receivedDate != null) {
								publishDate = publishDate.substring(0, 11);
								receivedDate = receivedDate.substring(0, 11);
								if (publishDate.equalsIgnoreCase(receivedDate)) {
									distributeToWeb(newsBean, uniqueStoryId);
								}
							}
						} else {
							logger.info(" PUBLISH REASON : " + newsBean.getPublishReason());
							int rowsUpdated = updateRecord(newsBean);
							if (rowsUpdated <= 0) {
								uniqueStoryId = ++storyId;
								logger.warning("Unable to update news record: " + newsBean.getNewsID());
								createBatch(newsBean, uniqueStoryId);
								distributeToDatabase(batchBuffer.toString(), companyNewsQuery.toString());
							}
						}
						batchBuffer = new StringBuilder();
						companyNewsQuery = new StringBuilder();
						CounterUtility.incrementDistributorCounter();
					}
				} catch (Exception e) {
					batchBuffer = new StringBuilder();
					companyNewsQuery = new StringBuilder();
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, " NewsEdgeDistributor.run() Exception " + e.getMessage(), e);
		}
	}

	private int updateRecord(NewsBean newsBean) {
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE NEWSEDGE_INFO SET TICKERS='");
		builder.append(newsBean.getTickers());
		builder.append("', CATEGORIES='");
		builder.append(newsBean.getCategories());
		builder.append("', NEWS_DESC='");
		builder.append(newsBean.getNewsDesc());
		builder.append("', NEWS_DATE='");
		builder.append(newsBean.getNewsDate());
		builder.append("', DB_DATE='");
		builder.append(newsBean.getDbDate());
		builder.append("', DB_DATE_TIME='");
		builder.append(newsBean.getDbDateTime());
		builder.append("', PUBLISH_DATE='");
		builder.append(newsBean.getPublishDate());
		builder.append("', NEWS_LINK='");
		builder.append(newsBean.getNewsLink());
		builder.append("', SOURCE='");
		builder.append(newsBean.getSource());
		builder.append("', LAST_UPDATE_TIME='");
		builder.append(newsBean.getLastUpdateTime());
		builder.append("' WHERE NEWS_ID='");
		builder.append(newsBean.getNewsID());
		builder.append("'");
		logger.info("QUERY " + builder.toString());
		try (Statement stmt = con.createStatement();) {
			int updateFlag = stmt.executeUpdate(builder.toString());
			logger.info(" Database Rows updated : " + updateFlag);
			return updateFlag;
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			return 0;
		}
	}

	private void distributeToDatabase(String query, String companyNewsQuery) {
		logger.info("QUERY " + query);
		Future<QueryInfoBean> future = executorService.submit(new DbCallable(con, query));
		futureQueue.add(future);
		if (flag != 0) {
			logger.info("QUERY " + companyNewsQuery);
			Future<QueryInfoBean> future1 = executorService.submit(new DbCallable(con, companyNewsQuery));
			futureQueue.add(future1);
		}
	}

	private void distributeToWeb(NewsBean newsBean, long uniqueStoryId) {
		try {
			logger.info("IBUS published : " + 1);
			bm.clearBody();
			newsDistributionCount++;
			bm.writeInt(newsDistributionCount);
			String multicastTicker = newsBean.getMulticastTicker();
			bm.writeUTF(multicastTicker);
			String source = newsBean.getSource();
			String tickers = newsBean.getTickers();
			String[] tickerArray = tickers.split(",");
			StringBuilder temp = new StringBuilder();
			for (int i = 0; i < tickerArray.length; i++) {
				if (tickerSet.contains(tickerArray[i])) {
					temp.append(",");
					temp.append(tickerArray[i]);
				}
			}
			if (temp.length() > 0)
				tickers = temp.substring(1);
			else
				tickers = temp.toString();
			String categories = newsBean.getCategories();
			String headline = newsBean.getNewsDesc();
			Date now = new Date();
			String date = dt.format(now);
			String newsID = uniqueStoryId + "";
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			baos.write(Constants.TICKER.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(multicastTicker.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_ID.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(newsID.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_HEADLINE.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(headline.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_TICKERS.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(tickers.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_CATEGORIES.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(categories.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_DATE.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(date.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_SOURCE.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(source.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());
			baos.write(Constants.NEWS_VENDOR.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write("N".getBytes());
			logger.info("IBUS MESSAGE " + new String(baos.toByteArray()));
			bm.writeBytes(baos.toByteArray());
			newsPublisher.publish(bm);
		} catch (Exception e) {
			logger.log(Level.WARNING, " NewsEdgeDistributor.distributeToWeb Exception " + e.getMessage(), e);
		}
	}

	private void createBatch(NewsBean newsBean, long uniqueStoryId) {
		flag = 0;
		try {
			String desc = newsBean.getNewsDesc();
			String tickers = newsBean.getTickers();
			String[] tickerArray = tickers.split(",");
			StringBuilder temp = new StringBuilder();
			for (int i = 0; i < tickerArray.length; i++) {
				if (tickerSet.contains(tickerArray[i])) {
					temp.append(",");
					temp.append(tickerArray[i]);
				}
			}
			if (temp.length() > 0)
				tickers = temp.substring(1);
			else
				tickers = temp.toString();
			String categories = newsBean.getCategories();
			batchBuffer.append("INSERT INTO " + TABLE_NAME
					+ "(STORY_ID,NEWS_ID,NEWS_DESC,NEWS_LINK,NEWS_DATE,DB_DATE,DB_DATE_TIME,PUBLISH_DATE,DURATION,SOURCE,TICKERS,CATEGORIES,LAST_UPDATE_TIME)"
					+ " VALUES (" + uniqueStoryId + ",'" + newsBean.getNewsID() + "', '" + desc + "', '"
					+ newsBean.getNewsLink() + "', '" + newsBean.getNewsDate() + "', '" + newsBean.getDbDate() + "', '"
					+ newsBean.getDbDateTime() + "', '" + newsBean.getPublishDate() + "', '" + newsBean.getDuration()
					+ "','" + newsBean.getSource() + "','" + tickers + "','" + categories + "',"
					+ newsBean.getLastUpdateTime() + ")");
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void setInputQueue(MessageQueue messageQueue) {
		this.queue = messageQueue;
	}

	public void stopThread() {
		doRun = false;
		lastRun = true;
		logger.info("Stop thread " + this.getName());
	}

}