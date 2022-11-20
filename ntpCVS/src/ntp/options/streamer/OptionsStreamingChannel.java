package ntp.options.streamer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;

import com.b4utrade.bean.StockOptionBean;

import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.OPBbo;
import QuoddFeed.msg.OPTrade;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsMemoryDB;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import ntp.util.OptionsUtility;

public class OptionsStreamingChannel extends UltraChan {

	private boolean isRunning = false;
	private String name;
	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;

	private OptionsQTMessageQueue cache = OptionsQTMessageQueue.getInstance();
	private MappedMessageQueue queue = cache.getmQueue();
	private ConcurrentMap<String, String> imageTickerMap = cache.getImageTickerMap();
	private ConcurrentMap<String, String> incorrectTickerMap = cache.getIncorrectTickerMap();
	private OptionsUtility utility = OptionsUtility.getInstance();
	// logging variables
	private int subscriptionCount = 0;
	private HashSet<String> imageSet = new HashSet<>();
	private HashSet<String> deadSet = new HashSet<>();
	private int messageCount = 0;
	private long logStartTime;
	private int idx = 0;

	private SimpleDateFormat expirationformatter = new SimpleDateFormat("yyyyMMdd");

	public OptionsStreamingChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
		try {
			boolean doLog = CPDProperty.getInstance().getProperty("LOG_CHANNEL").equalsIgnoreCase("TRUE") ? true
					: false;
			if (doLog)
				startLoggingThread();
		} catch (Exception e) {
			NTPLogger.missingProperty("LOG_CHANNEL");
		}
	}

	public String getChannelName() {
		return this.name;
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public void subscribe(String ticker) {
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
		++subscriptionCount;
	}

	////////////////////////////////////////////////
	//// CALL BACK METHODS
	///////////////////////////////////////////////

	@Override
	public void OnConnect() {
		NTPLogger.onConnectUC(name);
	}

	@Override
	public void OnDisconnect() {
		NTPLogger.onDisconnectUC(name);
	}

	@Override
	public void OnSession(String txt, boolean bUP) {
		NTPLogger.onSessionUC(name, txt, bUP);
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	@Override
	public void OnImage(String StreamName, Image image) {
		try {
		messageCount++;
		String ticker = image.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		ticker = OptionsUtility.getInstance().getEQPlusFormattedTicker(image.tkr());
		imageSet.add(ticker);
		QTCPDMessageBean qtMessageBean = OptionsQTMessageQueue.getInstance().getCachedBean(ticker);
		NTPLogger.image(name, image);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setLastPrice(image._trdPrc);
		qtMessageBean.setOpenInterest(image._openVol);
		qtMessageBean.setLastTradeVolume(image._trdVol);
		qtMessageBean.setChangePrice(image._netChg);
		qtMessageBean.setPercentChange(image._pctChg);
		qtMessageBean.setTickUpDown(image._prcTck);
		qtMessageBean.setExchangeId("" + image.protocol());
		qtMessageBean.setMarketCenter(utility.getEquityPlusExchangeCode(image._trdMktCtr));
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
		qtMessageBean.setTradeTime(image._trdTime);
		qtMessageBean.setDayHigh(image._high);
		qtMessageBean.setDayLow(image._low);
		qtMessageBean.setLastClosedPrice(image._close);
		qtMessageBean.setOpenPrice(image._open);
		qtMessageBean.setRegularOptionVolume(image._acVol);
		qtMessageBean.setVolume(qtMessageBean.getPreOptionVolume() + image._acVol);
		if (!(image._ask == 0 && image._askSize == 0)) {
			qtMessageBean.setAskPrice(image._ask);
			qtMessageBean.setAskSize(image._askSize);
			qtMessageBean.setAskTime(image._askTime);
			qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(image._askMktCtr));
		}
		if (!(image._bid == 0 && image._bidSize == 0)) {
			qtMessageBean.setBidPrice(image._bid);
			qtMessageBean.setBidSize(image._bidSize);
			qtMessageBean.setBidTime(image._bidTime);
			qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(image._bidMktCtr));
		}
		imageTickerMap.put(ticker, ticker);
		// Update Fundamental Cache
		try {
			StockOptionBean bean = new StockOptionBean();
			String expiryDate = "" + image.OptionExpiration();
			Date expiry = expirationformatter.parse(expiryDate);
			Calendar c = Calendar.getInstance();
			c.setTime(expiry);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			bean.setExpirationDate(c);
			String type = image.OptionPutOrCall();
			if (type.equals("CALL"))
				bean.setOptionType(1);
			else
				bean.setOptionType(2);
			try {
				bean.setStrikePrice(image.OptionStrike());
			} catch (Exception e) {
				NTPLogger.warning("Cannot process Option Strike - John Issue");
			}
			bean.setOpenInterest(image.OptionOpenInterest());
			bean.setSecurityDesc(image.Description());
			bean.setTicker(ticker);
			OptionsMemoryDB.getInstance().addStockFundamentalData(ticker, bean);
		} catch (Exception e) {
		}
		qtMessageBean = utility.evaluateGreeks(ticker, qtMessageBean);
		queue.add(ticker, qtMessageBean);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when an OPBbo update is received for an option ticker.
	 */
	@Override
	public void OnUpdate(String StreamName, OPBbo bbo) {
		try {
		messageCount++;
		String ticker = bbo.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "OPBbo.tkr() is null");
			return;
		}
		ticker = OptionsUtility.getInstance().getEQPlusFormattedTicker(ticker);
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "OPBbo No Image " + ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = OptionsQTMessageQueue.getInstance().getCachedBean(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setTicker(ticker);
		if (!bbo.IsCachedAsk()) {
			qtMessageBean.setAskPrice(bbo._ask);
			qtMessageBean.setAskTime(bbo.MsgTime());
			qtMessageBean.setAskSize(bbo._askSize);
			qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(bbo._askMktCtr));
		}
		if (!bbo.IsCachedBid()) {
			qtMessageBean.setBidPrice(bbo._bid);
			qtMessageBean.setBidSize(bbo._bidSize);
			qtMessageBean.setBidTime(bbo.MsgTime());
			qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(bbo._bidMktCtr));
		}
		qtMessageBean.setExchangeId("" + bbo.protocol());
		queue.add(ticker, qtMessageBean);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when an OPTrade update is received for an option ticker.
	 */
	@Override
	public void OnUpdate(String StreamName, OPTrade trade) {
		try {
		messageCount++;
		String ticker = trade.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "OPTrade.tkr() is null");
			return;
		}
		ticker = OptionsUtility.getInstance().getEQPlusFormattedTicker(ticker);
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "OPTrade No Image " + ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = OptionsQTMessageQueue.getInstance().getCachedBean(ticker);
		if (shouldUpdateLast(trade.TradeFlags()))
			qtMessageBean.setLastPrice(trade._trdPrc);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setLastTradeVolume(trade._trdVol);
		qtMessageBean.setVolume(qtMessageBean.getPreOptionVolume() + trade._acVol);
		qtMessageBean.setRegularOptionVolume(trade._acVol);
		qtMessageBean.setOpenPrice(trade._openPrc);
		qtMessageBean.setDayHigh(trade._high);
		qtMessageBean.setDayLow(trade._low);
		qtMessageBean.setChangePrice(trade._netChg);
		qtMessageBean.setPercentChange(trade._pctChg);
		double lastClosedPrice = trade._trdPrc - trade._netChg;
		qtMessageBean.setLastClosedPrice(lastClosedPrice);
		qtMessageBean.setMarketCenter(utility.getEquityPlusExchangeCode(trade._mktCtr));
		qtMessageBean.setExchangeId("" + trade.protocol());
		qtMessageBean.setTradeTime(trade.MsgTimeMs());
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, trade.MsgTimeMs());
		qtMessageBean.setTickUpDown(trade._prcTck);

		// Evaluating Greeks
		qtMessageBean = utility.evaluateGreeks(ticker, qtMessageBean);

		queue.add(ticker, qtMessageBean);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void OnStatus(String StreamName, Status sts) {
		messageCount++;
		char mt = sts.mt();
		if (mt == UltraChan._mtDEAD) {
			String ticker = sts.tkr();
			if (ticker != null) {
				NTPLogger.dead(ticker, name);
				deadSet.add(ticker);
				incorrectTickerMap.put(ticker, ticker);
			}
		} else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	/**
	 * Called when an ticker is added in between the market
	 */
	@Override
	public void OnBlobTable(String qry, BlobTable blobTable) {
		messageCount++;
		try {
			if (blobTable != null) {
				int rowCount = blobTable.nRow();
				NTPLogger.info("NEWISSUE OnBlobTable rowCount is = " + rowCount);
				for (int count = 0; count < rowCount; count++) {
					String ticker = blobTable.GetCell(count, TICKER_POS);
					// check if ticker is equity or not
					String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
					NTPLogger.newIssue(ticker, instrument);

					if (!(ticker.startsWith("O:") /* && instrument.startsWith("OPR") */)) {
						NTPLogger.info("NEWISSUE OnBlobTable not a option symbol instrument = " + instrument);
						return;
					}
					if (!ticker.contains("/")) {
						if (incorrectTickerMap.containsKey(ticker))
							incorrectTickerMap.remove(ticker);
						NTPLogger.newIssue(ticker, instrument);
						String rootSymbol = utility.getRootSymbol(ticker);
						String underlyerSymbol = OptionsQTMessageQueue.getInstance().getUnderlyer(rootSymbol);
						OptionsQTMessageQueue.getInstance().addMarkedSymbol(underlyerSymbol);
					} else {
						NTPLogger.info("NEWISSUE OnBlobTable not a option symbol instrument = " + instrument);
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startLoggingThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					long timeDiff = System.currentTimeMillis() - logStartTime;
					if (timeDiff > 5000) {
						NTPLogger.info("CHANNEL (" + name + ") Ticker Requested: " + subscriptionCount
								+ " Image Received: " + imageSet.size() + " DeadCount: " + deadSet.size()
								+ " Messages Processed: " + messageCount + " in time(ms) " + timeDiff);
						logStartTime = System.currentTimeMillis();
						messageCount = 0;
					}
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
				}
			}
		}, "Opstr_" + name + "_Logger").start();
	}

	private boolean shouldUpdateLast(String tradeFlags) {
		String flags[] = tradeFlags.split(",");
		for (int i = 0; i < flags.length; i++) {
			flags[i] = flags[i].toUpperCase();
			if (flags[i].contains("LAST"))
				return true;
		}
		return false;
	}
}
