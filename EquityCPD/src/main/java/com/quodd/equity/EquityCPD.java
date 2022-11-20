package com.quodd.equity;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.stop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.quodd.common.cpd.CPD;
import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.monitoring.JVMMemoryMonitor;
import com.quodd.common.cpd.monitoring.StatUtility;
import com.quodd.common.cpd.routes.CPDRoutes;
import com.quodd.common.ibus.JMSPropertyBean;
import com.quodd.equity.util.EqMetaDataUtility;

public class EquityCPD extends CPD {
	public static final EquityCache datacache = new EquityCache();
	private final EqMetaDataUtility metaUtility = new EqMetaDataUtility();
	private final Thread metaUtilityThread = new Thread(this.metaUtility);
	private final int numberofChannels;
	private final int apiPort;
	private String ip;
	private int port;
	private JVMMemoryMonitor jvmMemoryMonitor;

	public EquityCPD() {
		super();
		this.metaUtilityThread.start();
		if (datacache.isDelayed()) {
			logger.info(() -> "EQUITY is delayed CPD");
			this.ip = cpdProperties.getStringProperty("DELAYED_IP", null);
			if (this.ip == null) {
				logger.severe(() -> "DELAYED_IP is null. Exiting CPD");
				System.exit(0);
			}
			this.port = cpdProperties.getIntProperty("DELAYED_PORT", 4321);
		} else {
			this.ip = cpdProperties.getStringProperty("SUBSCRIBE_IP", null);
			if (this.ip == null) {
				logger.severe(() -> "SUBSCRIBE_IP is null. Exiting CPD");
				System.exit(0);
			}
			this.port = cpdProperties.getIntProperty("SUBSCRIBE_PORT", 4321);
		}
		if (datacache.isLegacyDistribution()) {
			String lqos = cpdProperties.getStringProperty("LEGACY_JMS_QT_QOS", null);
			String lclientId = cpdProperties.getStringProperty("LEGACY_JMS_CLIENT_ID", null);
			String ltopicName = cpdProperties.getStringProperty("LEGACY_JMS_QT_TOPIC_NAME", null);
			int lconflationTime = cpdProperties.getIntProperty("LEGACY_CONFLATION_TIME", 40);
			final int lsurgeThreshold = cpdProperties.getIntProperty("LEGACY_SURGE_THRESHOLD", 40_000);
			JMSPropertyBean legacyJmsProperty = new JMSPropertyBean(lqos, lclientId, ltopicName, lconflationTime,
					lsurgeThreshold);
			legacyDispacther.startDispatcher("Equity", datacache.getLegacymQueue(), legacyJmsProperty);
		}
		if (datacache.isWebSocketDistribution()) {
			String qos = cpdProperties.getStringProperty("JMS_QT_QOS", null);
			String clientId = cpdProperties.getStringProperty("JMS_CLIENT_ID", null);
			String topicName = cpdProperties.getStringProperty("JMS_QT_TOPIC_NAME", null);
			int conflationTime = cpdProperties.getIntProperty("CONFLATION_TIME", 40);
			final int surgeThreshold = cpdProperties.getIntProperty("SURGE_THRESHOLD", 40_000);
			JMSPropertyBean jmsProperty = new JMSPropertyBean(qos, clientId, topicName, conflationTime, surgeThreshold);
			boolean writeFile = cpdProperties.getBooleanProperty("WRITE_FILE", false);
			tradeDispacther.startDispatcher("EquityTrade", datacache.getTrademQueue(), "trade", writeFile,
					datacache.getCachedTradeData(), jmsProperty);
			quoteDispacther.startDispatcher("EquityQuote", datacache.getQuotemQueue(), "quote", writeFile,
					datacache.getCachedQuoteData(), jmsProperty);
		}
		String exchangeFilePath = cpdProperties.getStringProperty("EXCHANGE_MAPPING_FILE", null);
		datacache.initExchMap(this.port, this.ip, exchangeFilePath);
		this.numberofChannels = cpdProperties.getIntProperty("NO_OF_CHANNELS", 6);
		this.apiPort = cpdProperties.getIntProperty("API_PORT", 8281);
		boolean runMemoryMonitor = cpdProperties.getBooleanProperty("SHOULD_MONITOR_MEMORY", false);
		if (runMemoryMonitor) {
			logger.info(() -> "JVM memory monitor thread started");
			this.jvmMemoryMonitor = new JVMMemoryMonitor(cpdProperties.getLongProperty("TIME_INTERVAL", 15_000l));
			this.jvmMemoryMonitor.startThread();
		}
		stats.startLoggerThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			stop();
			channelManager.stopChannels();
			tradeDispacther.close();
			quoteDispacther.close();
			legacyDispacther.close();
			try {
				datacache.stopThread();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			this.metaUtility.close();
			try {
				this.metaUtilityThread.join();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			if (this.jvmMemoryMonitor != null) {
				this.jvmMemoryMonitor.stopThread();
			}
			logger.info("Going to reset logger");
			LogManager.getLogManager().reset();
		}, "ShutDownHookThread"));
	}

	private void startProcess() {
		port(this.apiPort);
		path("/eqcpd", () -> {
			path("/stat", () -> {
				get("", CPDRoutes.getStatsMap, gson::toJson);
				get("/channel", datacache.getChannelStats, gson::toJson);
				get("/deadsymbols/:channel", CPDRoutes.getDeadSymbolsPerChannel, gson::toJson);
				get("/imagesymbols/:channel", CPDRoutes.getImageSymbolsPerChannel, gson::toJson);
				get("/getDetailedLog", CPDRoutes.getDetailedLog, gson::toJson);
				get("/system", CPDRoutes.getSystemStats, gson::toJson);
				get("/disk", (request, response) -> StatUtility.getDiskInformation(), gson::toJson);
				get("/ram", (request, response) -> StatUtility.getRamInformation(), gson::toJson);
				get("/count", (request, response) -> StatUtility.getErrorCountFromLog(), gson::toJson);
				get("/filecontent/:fileName", CPDRoutes.getFileContentByName, gson::toJson);
			});
			path("/snap", () -> {
				get("", EquityDataController.getData, gson::toJson);
				get("/dump/:dataType", EquityDataController.getDataByServiceId);
				post("/list", EquityDataController.getDataList, gson::toJson);
			});
			path("/subscribe", () -> get("", EquityDataController.subscribe, gson::toJson));
			path("/unsubscribe", () -> get("", EquityDataController.unsubscribe, gson::toJson));
			path("/list", () -> {
				get("/meta/:serviceId", EquityDataController.getTickerServices, gson::toJson);
				get("/image", EquityDataController.getImageTickers, gson::toJson);
				get("/pending", EquityDataController.getPendingTickers, gson::toJson);
				get("/root", EquityDataController.getRootSymbols, gson::toJson);
				get("/tier", EquityDataController.getSymbolTier, gson::toJson);
			});
			path("/filewriter", () -> {
				get("/start/:dataType", EquityDataController.startFileWriter, gson::toJson);
				get("/stop/:dataType", EquityDataController.stopFileWriter, gson::toJson);
			});
		});
		after("*", (request, response) -> {
			String gzip = request.headers("Content-Encoding");
			if (gzip != null && "gzip".equalsIgnoreCase(gzip)) {
				response.header("Content-Encoding", "gzip");
			}
			response.type("application/json");
		});
		ArrayList<CPDChannel> channelList = new ArrayList<>();
		for (int i = 0; i <= this.numberofChannels; i++) {
			EquitySubscriptionChannel channel = new EquitySubscriptionChannel(this.ip, this.port, "EqtyChan" + i);
			channelList.add(channel);
		}
		channelManager.init(channelList);
		Set<String> tickerList = datacache.getMetaTickerSet();
		int tickersPerChannel;
		tickersPerChannel = tickerList.size() / (this.numberofChannels);
		logger.info(() -> "Total number of tickers: " + tickerList.size());
		logger.info(() -> "Tickers per channel: " + tickersPerChannel);
		Set<String> tickerPerChannelList = new HashSet<>();
		int count = 0;
		int channelCount = 1;
		for (String ticker : tickerList) {
			++count;
			tickerPerChannelList.add(ticker);
			if (count == tickersPerChannel && channelCount != this.numberofChannels) {
				channelManager.subscribeTickers(tickerPerChannelList);
				tickerPerChannelList = new HashSet<>();
				count = 0;
				channelCount++;
			}
		}
		if (count > 0) {
			channelManager.subscribeTickers(tickerPerChannelList);
		}
		logger.info("Complete ticker list subscribed on all channels");
	}

	public static void main(final String[] args) {
		final EquityCPD cpd = new EquityCPD();
		cpd.startProcess();
	}
}
