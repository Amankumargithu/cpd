package com.quodd.populator;

import static com.quodd.controller.DJController.logger;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import com.quodd.bean.NewsBean;
import com.quodd.bean.QueryInfoBean;
import com.quodd.common.logger.CommonLogMessage;
import com.quodd.controller.DJController;
import com.quodd.util.Constants;

import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;
import ch.softwired.jms.IBusTopicPublisher;

public class DJPopulator extends Thread {
	private ConcurrentLinkedQueue<NewsBean> queue = new ConcurrentLinkedQueue<>();
	private TopicPublisher djNewsPublisher;
	private BytesMessage bm;
	private DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private DateTimeFormatter newPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat dt = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss");
	private String tickers = "";
	private String categories = "";
	private boolean doRun = true;
	private int djNewsDistributionCount = 0;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private ConcurrentLinkedQueue<Future<QueryInfoBean>> futureQueue = new ConcurrentLinkedQueue<>();
	private FutureAnalyser futureAnalyser;
	private Connection mysqlCon;

	public DJPopulator(String jmsClientID, String jmsTopic, Connection mysqlCon) throws JMSException {
		this.mysqlCon = mysqlCon;
		IBusJMSContext.getTopicConnection().setClientID(jmsClientID);
		TopicSession session = IBusJMSContext.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
		IBusTopic djNewsTopic = (IBusTopic) IBusJMSContext.getTopic(jmsTopic);
		this.djNewsPublisher = session.createPublisher(djNewsTopic);
		((IBusTopicPublisher) this.djNewsPublisher).setDisableMessageClone(true);
		this.bm = session.createBytesMessage();
		this.futureAnalyser = new FutureAnalyser(this.futureQueue);
		this.futureAnalyser.setName("Dj_Future_Analyser");
		this.futureAnalyser.start();
	}

	public void add(NewsBean bean) {
		this.queue.add(bean);
	}

	private String createMysqlBatch(NewsBean newsBean) {
		StringBuilder batchBuffer = new StringBuilder();
		try {
			this.tickers = "";
			this.categories = "";
			String headline = newsBean.getHeadline();
			List<List<String>> categoriesList = newsBean.getCategories();
			if (!categoriesList.isEmpty()) {
				List<String> tickerList = categoriesList.get(0);
				tickerList.forEach(ticker -> this.tickers += "," + ticker);
				if (this.tickers.equals("")) {
					this.tickers = null;
				} else if (this.tickers.length() > 1)
					this.tickers = this.tickers.substring(1);
				if (this.tickers != null) {
					if (this.tickers.contains("'"))
						this.tickers = this.tickers.replaceAll("'", "''");
					if (this.tickers.contains(";"))
						this.tickers = this.tickers.replaceAll(";", "'';");
					if (this.tickers.contains("|"))
						this.tickers = this.tickers.replaceAll("\\|", "");
					if (this.tickers.contains("?"))
						this.tickers = this.tickers.replaceAll("\\?", "");
				}
				for (int i = 1; i < categoriesList.size(); i++) {
					if (i == 3) {
						continue;
					}
					if (i > 7) {
						break;
					}
					List<String> categoryList = categoriesList.get(i);
					categoryList.forEach(category -> this.categories += "," + category);
				}
				if (newsBean.getTempPerm().equals("H "))
					this.categories += ",N/HOT";
				if (!this.categories.isEmpty())
					this.categories = this.categories.substring(1);
			}
			long currentTime = System.currentTimeMillis();
			Date now = new Date();
			String currentDate = this.dt.format(now);
			if (newsBean.getMsgType().equals("N")) {
				batchBuffer.append("INSERT INTO " + Constants.tableName + " VALUES ('" + newsBean.getNewsId() + "', '"
						+ newsBean.getPublisher() + "', '" + headline + "', '" + newsBean.getNewsLink() + "', '"
						+ newsBean.getSeq() + "', '" + currentDate + "', '" + formatDate(newsBean) + "','" + currentDate
						+ "', '" + getCurrentDateTime() + "', '");
				if (newsBean.getTempPerm().equals("T"))
					batchBuffer.append("2', '");
				else
					batchBuffer.append("400','");
				batchBuffer.append(newsBean.getAccessionNumber() + "',");
				if (this.tickers == null)
					batchBuffer.append((String) null);
				else
					batchBuffer.append("'" + this.tickers + "'");
				batchBuffer.append(",");
				if (this.categories == null)
					batchBuffer.append((String) null);
				else
					batchBuffer.append("'" + this.categories + "'");
				batchBuffer.append(", '" + currentTime + "')");
			} else if (newsBean.getMsgType().equals("C")) {
				String actionCode = newsBean.getActionCode();
				if (actionCode.equals("XA")) {
					batchBuffer.append(
							"DELETE FROM " + Constants.tableName + " WHERE NEWS_ID = '" + newsBean.getNewsId() + "'");
				} else {
					batchBuffer.append("UPDATE " + Constants.tableName + " SET ");
					if (actionCode.contains("H") || actionCode.contains("N")) {
						batchBuffer.append(" NEWS_DESC = '" + headline + "',");
					}
					if (actionCode.contains("I")) {
						if (this.tickers != null && !this.tickers.equals(""))
							batchBuffer.append(" TICKERS = '" + this.tickers + "',");
						if (this.categories != null && !this.categories.equals(""))
							batchBuffer.append(" CATEGORIES = '" + this.categories + "',");
					}
					batchBuffer.append(" DB_DATE = '" + currentDate + "', LAST_UPDATE_TIME = '" + currentTime
							+ "' where NEWS_ID = '" + newsBean.getNewsId() + "'");
				}
			} else if (newsBean.getMsgType().equals("E")) {
				batchBuffer.append("DELETE FROM " + Constants.tableName + " WHERE ");
				Iterator<String> itr = newsBean.getExpireList().iterator();
				while (itr.hasNext()) {
					Object id = itr.next();
					batchBuffer.append(" NEWS_ID =  '" + id + "' ");
					if (itr.hasNext())
						batchBuffer.append(" OR ");
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING,
					this.getName() + " createMysqlBatch() " + batchBuffer.toString() + " " + e.getMessage(), e);
		}
		return batchBuffer.toString();
	}

	private void distributeToMysqlDatabase(String query) {
		Future<QueryInfoBean> future = this.executorService.submit(new DbCallable(this.mysqlCon, query));
		this.futureQueue.add(future);
	}

	@Override
	public void run() {
		try {
			logger.info(CommonLogMessage.startThread(this.getName()));
			while (this.doRun) {
				try {
					NewsBean newsBean = this.queue.poll();
					if (newsBean != null) {
						if (newsBean.getPublisher() != null) {
							String batchQuery = createMysqlBatch(newsBean);
							logger.info(this.getName() + " MysqlQuery " + batchQuery);
							distributeToMysqlDatabase(batchQuery);
							if (newsBean.getMsgType().equals("N"))
								distributeToWeb(newsBean);
						} else {
							DJController.dropCount++;
							logger.warning("DROP Publisher is null " + newsBean.toString());
						}
					} else
						TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
			while (!this.queue.isEmpty()) {
				NewsBean newsBean = this.queue.poll();
				if (newsBean != null) {
					if (newsBean.getPublisher() != null) {
						String batchQuery = createMysqlBatch(newsBean);
						logger.info(this.getName() + " Query " + batchQuery);
						distributeToMysqlDatabase(batchQuery);
						if (newsBean.getMsgType().equals("N"))
							distributeToWeb(newsBean);
					} else {
						DJController.dropCount++;
						logger.warning("DROP Publisher is null " + newsBean.toString());
					}
				}
			}
		} catch (Exception | Error e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void finish() {
		this.doRun = false;
		this.futureAnalyser.finish();
	}

	private void distributeToWeb(NewsBean newsBean) {
		try {
			this.bm.clearBody();
			this.djNewsDistributionCount++;
			this.bm.writeInt(this.djNewsDistributionCount);
			this.bm.writeUTF(Constants.DJ_NEWS_TOPIC);
			String newsID = newsBean.getNewsId();
			if (newsID == null)
				newsID = "-1";
			// tickers
			if (this.tickers == null)
				this.tickers = "";
			String tickers = this.tickers;
			Date now = new Date();
			String date = this.dt.format(now);
			// categories
			if (this.categories == null)
				this.categories = "";
			String categories = this.categories;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

			baos.write(Constants.TICKER.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(Constants.DJ_NEWS_TOPIC.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());

			baos.write(Constants.NEWS_ID.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(newsID.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());

			baos.write(Constants.NEWS_HEADLINE.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			baos.write(newsBean.getHeadline().getBytes());
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
			// baos.write(getCurrentDateTime().getBytes());
			baos.write(date.getBytes());
			baos.write(Constants.TUPLE_SEP.getBytes());

			baos.write(Constants.NEWS_SOURCE.getBytes());
			baos.write(Constants.FIELD_SEP.getBytes());
			// sometimes publisher is null when categories size is less than 4
			if (newsBean.getPublisher() != null)
				baos.write(newsBean.getPublisher().getBytes());
			else
				baos.write(new String().getBytes());
			this.bm.writeBytes(baos.toByteArray());
			this.djNewsPublisher.publish(this.bm);
			DJController.publishCount++;
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage() + " " + newsBean.toString(), e);
		}
	}

	private String formatDate(NewsBean newsBean) {
		if (newsBean != null && newsBean.getDisplayDate() != null && newsBean.getDisplayTime() != null) {
			String date = newsBean.getDisplayDate().trim() + newsBean.getDisplayTime().trim();
			try {
				LocalDateTime datetime = LocalDateTime.parse(date, this.oldPattern);
				return datetime.format(this.newPattern);
			} catch (DateTimeParseException e) {
				return getCurrentDateTime();
			}
		} else {
			return getCurrentDateTime();
		}
	}

	private String getCurrentDateTime() {
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(this.newPattern);
	}

}