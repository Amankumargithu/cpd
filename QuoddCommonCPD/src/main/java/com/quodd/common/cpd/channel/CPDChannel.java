package com.quodd.common.cpd.channel;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.quodd.common.cpd.collection.CPDCache;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public abstract class CPDChannel extends UltraChan {

	protected static final int TICKER_POS = 0;
	protected static final int INSTRUMENT_POS = 2;
	private boolean isRunning = false;
	private String ip;
	protected String name = "";
	private int idx = 0;
	protected int messageCount = 0;
	private int subscriptionCount = 0;
	private long logStartTime;
	private int totalMessageCount = 0;

	private CPDCache cpdCache;
	protected Set<String> imageSet = new HashSet<>();
	private Set<String> deadSet = new HashSet<>();

	private Thread loggingThread = null;
	private boolean isLogging = true;

	public CPDChannel(String ip, int port, String name, CPDCache cpdCache) {
		super(ip, port, name, "password", false);
		this.ip = ip;
		this.name = name;
		this.cpdCache = cpdCache;
	}

	public String getName() {
		return this.name;
	}

	public void connectChannel() {
		if (!this.isRunning) {
			Start();
			this.isRunning = true;
			logger.info(() -> CommonLogMessage.connectChannel(this.name, this.ip));
		}
	}

	public void subscribeTicker(String ticker) {
		int streamID = Subscribe(ticker, ++this.idx);
		this.cpdCache.getPendingTickerMap().put(ticker, ticker);
		this.cpdCache.getTickerChannelMap().put(ticker, this);
		this.cpdCache.getTickerStreamMap().put(ticker, Integer.valueOf(streamID));
		++this.subscriptionCount;
		logger.info(() -> CommonLogMessage.subscribe(ticker, this.name, streamID));
	}

	public void subscribeTickers(Set<String> tickerList) {
		int threshold = cpdProperties.getIntProperty("THRESHOLD", 200);
		int sleepTime = cpdProperties.getIntProperty("SLEEP_TIME", 500);
		new Thread(() -> {
			int count = 0;
			for (String ticker : tickerList) {
				count++;
				subscribeTicker(ticker);
				if (count % threshold == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (Exception e) {
						logger.log(Level.WARNING, " threshold thread sleep exception " + e.getMessage(), e);
					}
				}
			}
			logger.info("Completed Subscriptions for " + this.name);
		}, "SUBSCRIPTION_" + this.name).start();
	}

	////////////////////////////////////////////////
	//// CALL BACK METHODS
	///////////////////////////////////////////////

	@Override
	public void OnConnect() {
		logger.info(() -> CommonLogMessage.onConnectUC(this.name));
	}

	@Override
	public void OnDisconnect() {
		logger.info(() -> CommonLogMessage.onDisconnectUC(this.name));
	}

	@Override
	public void OnSession(String txt, boolean bUP) {
		logger.info(() -> CommonLogMessage.onSessionUC(this.name, txt, bUP));
	}

	@Override
	public void OnStatus(String streamName, Status sts) {
		this.messageCount++;
		char mt = sts.mt();
		if (mt == UltraChan._mtDEAD) {
			String ticker = sts.tkr();
			if (ticker != null) {
				logger.info(() -> CommonLogMessage.dead(ticker, this.name));
				this.deadSet.add(ticker);
				this.cpdCache.unsubscribeTicker(ticker);
			}
		} else {
			logger.info(() -> CommonLogMessage.unknown(sts.tkr(), this.name, mt));
		}
	}

	@Override
	public void OnResubscribe(String tkr, int oldStreamID, int newStreamID) {
		logger.info(() -> CommonLogMessage.onResubscribeUC(tkr, oldStreamID, newStreamID));
		this.cpdCache.getTickerStreamMap().put(tkr, newStreamID);
	}

	public void startLoggingThread() {
		this.loggingThread = new Thread(() -> {
			while (this.isLogging) {
				long timeDiff = System.currentTimeMillis() - this.logStartTime;
				if (timeDiff > 5000) {
					logger.info(() -> "CHANNEL (" + this.name + ") Ticker Requested: " + this.subscriptionCount
							+ " Image Received: " + this.imageSet.size() + " DeadCount: " + this.deadSet.size()
							+ " Messages Processed: " + this.messageCount + " in time(ms) " + timeDiff);
					this.logStartTime = System.currentTimeMillis();
					this.totalMessageCount += this.messageCount;
					Map<String, Object> valueMap = new HashMap<>();
					valueMap.put("SubscriptionCount", this.subscriptionCount);
					valueMap.put("ImagesReceived", this.imageSet.size());
					valueMap.put("DeadCount", this.deadSet.size());
					valueMap.put("TotalMessagesProcessed", this.totalMessageCount);
					valueMap.put("MessagesProcessed", this.messageCount);
					valueMap.put("Time", timeDiff);
					valueMap.put("name", this.name);
					this.cpdCache.getChannelLogMap().put(this.name, valueMap);
					this.messageCount = 0;
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					logger.log(Level.WARNING, "error in sleep thread", e);
				}
			}
		}, "CpdCh_" + this.name + "_Logger");
		this.loggingThread.start();
	}

	protected void stopChannel() throws InterruptedException {
		this.isLogging = false;
		this.loggingThread.join();
		for(String tkr : this.imageSet) {
			this.cpdCache.unsubscribeTicker(tkr);
		}
		this.Stop();
	}

	public Set<String> getImageSet() {
		return this.imageSet;
	}

	public Set<String> getDeadSet() {
		return this.deadSet;
	}

	public String getEquityPlusExchangeCode(String nasdaqExchangeCode) {
		if (nasdaqExchangeCode.startsWith("??") || nasdaqExchangeCode.length() < 1)
			nasdaqExchangeCode = " ";
		nasdaqExchangeCode = nasdaqExchangeCode.toUpperCase();
		if (this.cpdCache.getExchgMap().containsKey(nasdaqExchangeCode))
			return this.cpdCache.getExchgMap().get(nasdaqExchangeCode).toLowerCase();
		return nasdaqExchangeCode;
	}
}
