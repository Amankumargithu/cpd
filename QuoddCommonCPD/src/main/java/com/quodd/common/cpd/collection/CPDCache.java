package com.quodd.common.cpd.collection;

import static com.quodd.common.cpd.CPD.cpdProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.collection.MappedMessageQueue;
import com.quodd.common.logger.CommonLogMessage;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.uc.ExchangeMapPopulator;

import QuoddFeed.util.UltraChan;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class CPDCache {

	protected ConcurrentMap<String, Map<String, Object>> cachedQuoteData = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Map<String, Object>> cachedTradeData = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Map<String, Object>> cachedLegacyData = new ConcurrentHashMap<>();
	protected MappedMessageQueue trademQueue = new MappedMessageQueue();
	protected MappedMessageQueue quotemQueue = new MappedMessageQueue();
	protected MappedMessageQueue legacymQueue = new MappedMessageQueue();
	private Map<String, Map<String, Object>> channelLogMap = new HashMap<>();
	private ConcurrentMap<String, String> pendingTickerMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> imageTickerMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, UltraChan> tickerChannelMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Integer> tickerStreamMap = new ConcurrentHashMap<>();
	private Set<String> metaTickerSet = new HashSet<>();
	private Set<String> rootTickerSet = new HashSet<>();
	protected Map<String, String> exchgMap;
	private final boolean isDelayed;
	private boolean isWebSocketDistribution = true;
	private boolean isLegacyDistribution = true;
	protected Set<Integer> allowedProtocols = new HashSet<>();
	private final Thread marketSnapUpdaterThread;
	private boolean doRunUpdater = true;
	private Logger logger = QuoddLogger.getInstance().getLogger();
	private final long delayedTime;

	public CPDCache() {
		this.isLegacyDistribution = cpdProperties.getBooleanProperty("IS_LEGACY_DISTRIBUTION", false);
		this.isWebSocketDistribution = cpdProperties.getBooleanProperty("IS_WEBSOCKET_DISTRIBUTION", true);
		this.isDelayed = cpdProperties.getBooleanProperty("IS_DELAYED", false);
		this.delayedTime = cpdProperties.getLongProperty("DUMP_DELAYED_TIME", 5_000);
		this.marketSnapUpdaterThread = new Thread(() -> {
			while (this.doRunUpdater) {
				long startTime = System.currentTimeMillis();
				loadMarketDataAsString();
				long endTime = System.currentTimeMillis();
				long diffTime = this.delayedTime - (endTime - startTime);
				if (diffTime < 0) {
					diffTime = 100;
				}
				try {
					this.logger.log(Level.INFO, "Thread Sleeping for ms {0}", diffTime);
					TimeUnit.MILLISECONDS.sleep(diffTime);
				} catch (InterruptedException e) {
					this.logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}, "MarketSnapUpdaterThread");
		this.marketSnapUpdaterThread.start();
	}

	public void stopThread() throws InterruptedException {
		this.doRunUpdater = false;
		this.marketSnapUpdaterThread.join();
	}

	public ConcurrentMap<String, Map<String, Object>> getCachedQuoteData() {
		return this.cachedQuoteData;
	}

	public ConcurrentMap<String, Map<String, Object>> getCachedTradeData() {
		return this.cachedTradeData;
	}

	public Map<String, Map<String, Object>> getChannelLogMap() {
		return this.channelLogMap;
	}

	public ConcurrentMap<String, String> getImageTickerMap() {
		return this.imageTickerMap;
	}

	public ConcurrentMap<String, String> getPendingTickerMap() {
		return this.pendingTickerMap;
	}

	public ConcurrentMap<String, UltraChan> getTickerChannelMap() {
		return this.tickerChannelMap;
	}

	public ConcurrentMap<String, Integer> getTickerStreamMap() {
		return this.tickerStreamMap;
	}

	public void unsubscribeTicker(String ticker) {
		UltraChan channel = this.tickerChannelMap.get(ticker);
		Integer streamId = this.tickerStreamMap.get(ticker);
		if (streamId != null) {
			this.logger.info(() -> CommonLogMessage.unsubscribeSymbol(ticker, streamId.toString()));
			if (channel != null) {
				channel.Unsubscribe(streamId.intValue());
			}
		}
		this.tickerChannelMap.remove(ticker);
		this.tickerStreamMap.remove(ticker);
		this.pendingTickerMap.remove(ticker);
		this.imageTickerMap.remove(ticker);
	}

	public Set<String> getMetaTickerSet() {
		return this.metaTickerSet;
	}

	public Set<String> getRootTickerSet() {
		return this.rootTickerSet;
	}

	public Map<String, String> getExchgMap() {
		return this.exchgMap;
	}

	public Set<Integer> getAllowedProtocols() {
		return this.allowedProtocols;
	}

	public void initExchMap(int port, String ip, String exchnageFilePath) {
		this.exchgMap = new ExchangeMapPopulator(ip, port, exchnageFilePath).getExchangeMap();
	}

	public Map<String, Map<String, Object>> getCachedLegacyData() {
		return this.cachedLegacyData;
	}

	public boolean isDelayed() {
		return this.isDelayed;
	}

	public boolean isWebSocketDistribution() {
		return this.isWebSocketDistribution;
	}

	public boolean isLegacyDistribution() {
		return this.isLegacyDistribution;
	}

	public void setWebSocketDistribution(boolean isWebSocketDistribution) {
		this.isWebSocketDistribution = isWebSocketDistribution;
	}

	public void setLegacyDistribution(boolean isLegacyDistribution) {
		this.isLegacyDistribution = isLegacyDistribution;
	}

	public MappedMessageQueue getTrademQueue() {
		return this.trademQueue;
	}

	public MappedMessageQueue getQuotemQueue() {
		return this.quotemQueue;
	}

	public MappedMessageQueue getLegacymQueue() {
		return this.legacymQueue;
	}

	public final Route getChannelStats = (Request request, Response response) -> this.getChannelLogMap().values();

	public abstract Map<String, Object> getData(String ticker, String dataType);

	public abstract Object getDataByService(String dataType);

	public abstract Object getDataList(Map<String, Set<String>> tickerMap);

	public abstract void loadMarketDataAsString();

}
