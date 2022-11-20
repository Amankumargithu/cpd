package ntp.options.streamer;

import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;

import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.OPBbo;
import QuoddFeed.msg.OPTrade;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import ntp.util.OptionsUtility;

public class OptionsPreStreamingChannel extends UltraChan {

	private boolean isRunning = false;
	private String name;
	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;

	private MappedMessageQueue queue = OptionsQTMessageQueue.getInstance().getmQueue();
	private ConcurrentMap<String, String> imageTickerMap = OptionsQTMessageQueue.getInstance().getImageTickerMap();
	private OptionsUtility utility = OptionsUtility.getInstance();

	// logging variables
	private int subscriptionCount = 0;
	private HashSet<String> imageSet = new HashSet<>();
	private HashSet<String> deadSet = new HashSet<>();
	private int messageCount = 0;
	private long logStartTime;
	private int idx = 0;

	public OptionsPreStreamingChannel(String name, String ip, int port) {
		super(ip, port, name, "password", false);
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
		ticker = ticker.replace("P:", "O:");
		ticker = OptionsUtility.getInstance().getEQPlusFormattedTicker(ticker);
		imageSet.add(ticker);
		QTCPDMessageBean qtMessageBean = OptionsQTMessageQueue.getInstance().getCachedBean(ticker);
		NTPLogger.image(name, image);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setExtendedLastPrice(image._trdPrc);
		qtMessageBean.setExtendedLastTradeVolume(image._trdVol);
		qtMessageBean.setExtendedChangePrice(image._netChg);
		qtMessageBean.setExtendedPercentChange(image._pctChg);
		qtMessageBean.setExtendedMarketCenter(utility.getEquityPlusExchangeCode(image._trdMktCtr));
		DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, image._trdTime);
		qtMessageBean.setExtendedTickUpDown(image._prcTck);
		qtMessageBean.setExchangeId("" + image.protocol());
		qtMessageBean.setPreOptionVolume(image._acVol);
		qtMessageBean.setVolume(qtMessageBean.getRegularOptionVolume() + image._acVol);

		if (!(image._ask == 0 && image._askSize == 0)) {
			qtMessageBean.setAskPrice(image._ask);
			qtMessageBean.setAskSize(image._askSize);
			qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(image._askMktCtr));
			qtMessageBean.setAskTime(image._askTime);
		}
		if (!(image._bid == 0 && image._bidSize == 0)) {
			qtMessageBean.setBidPrice(image._bid);
			qtMessageBean.setBidSize(image._bidSize);
			qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(image._bidMktCtr));
			qtMessageBean.setBidTime(image._bidTime);
		}
		imageTickerMap.put(ticker, ticker);
		queue.add(ticker, qtMessageBean);
		}catch (Exception e) {
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
		ticker = ticker.replace("P:", "O:");
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
		}
		catch(Exception e) {
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
		ticker = ticker.replace("P:", "O:");
		ticker = OptionsUtility.getInstance().getEQPlusFormattedTicker(ticker);
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "OPTrade No Image " + ticker);
			return;
		}
		NTPLogger.info("PRE_TRADE " + ticker + " " + trade._trdPrc + " " + trade._trdVol + " " + trade.pDateTimeMs());
		QTCPDMessageBean qtMessageBean = OptionsQTMessageQueue.getInstance().getCachedBean(ticker);
		if (shouldUpdateLast(trade.TradeFlags()))
			qtMessageBean.setExtendedLastPrice(trade._trdPrc);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setVolume(qtMessageBean.getRegularOptionVolume() + trade._acVol);
		qtMessageBean.setPreOptionVolume(trade._acVol);
		qtMessageBean.setExtendedLastTradeVolume(trade._trdVol);
		qtMessageBean.setExtendedChangePrice(trade._netChg);
		qtMessageBean.setExtendedPercentChange(trade._pctChg);
		qtMessageBean.setExtendedMarketCenter(utility.getEquityPlusExchangeCode(trade._mktCtr));
		DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, trade.MsgTimeMs());
		qtMessageBean.setTradeTime(trade.MsgTimeMs());
		qtMessageBean.setExtendedTickUpDown(trade._prcTck);
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
				// incorrectTickerMap.put(ticker, ticker);
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
					if (ticker.startsWith("P:"))
						ticker = ticker.replace("P:", "O:");
					if (!(ticker.startsWith("O:") /* && instrument.startsWith("OPR") */)) {
						NTPLogger.info("NEWISSUE OnBlobTable not a option symbol instrument = " + instrument);
						return;
					}
					if (!ticker.contains("/")) {
						// if(incorrectTickerMap.containsKey(ticker))
						// incorrectTickerMap.remove(ticker);
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
