package com.quodd.nb;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.stop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;

import com.quodd.common.cpd.CPD;
import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.channel.CPDChannelManager;
import com.quodd.common.cpd.monitoring.JVMMemoryMonitor;
import com.quodd.common.cpd.monitoring.StatUtility;
import com.quodd.common.cpd.routes.CPDRoutes;
import com.quodd.common.ibus.JMSPropertyBean;
import com.quodd.nb.util.NbMetaDataUtility;

public class NasdaqBasicCpd extends CPD {
	public static final CPDChannelManager volPlusChannelManager = new CPDChannelManager();
	public static final NbCache datacache = new NbCache();
	private final NbMetaDataUtility metaUtility = new NbMetaDataUtility();
	private final Thread metaUtilityThread = new Thread(this.metaUtility);
	private int numberofChannels = 6;
	private int apiPort = 8283;
	private String ip;
	private int port;
	private JVMMemoryMonitor jvmMemoryMonitor;

	public NasdaqBasicCpd() {
		this.metaUtilityThread.start();
		if (datacache.isDelayed()) {
			logger.info(() -> "NBCPD is delayed CPD");
			this.ip = cpdProperties.getStringProperty("DELAYED_IP", null);
			if (this.ip == null) {
				logger.info(() -> "DELAYED_IP is null. Exiting CPD");
				System.exit(0);
			}
			this.port = cpdProperties.getIntProperty("DELAYED_PORT", 4321);
		} else {
			this.ip = cpdProperties.getStringProperty("SUBSCRIBE_IP", null);
			if (this.ip == null) {
				logger.info(() -> "SUBSCRIBE_IP is null. Exiting CPD");
				System.exit(0);
			}
			this.port = cpdProperties.getIntProperty("SUBSCRIBE_PORT", 4321);
		}
		if (datacache.isLegacyDistribution()) {
			String lqos = cpdProperties.getStringProperty("LEGACY_JMS_QT_QOS", null);
			String lclientId = cpdProperties.getStringProperty("LEGACY_JMS_CLIENT_ID", null);
			String ltopicName = cpdProperties.getStringProperty("LEGACY_JMS_QT_TOPIC_NAME", null);
			int lconflationTime = cpdProperties.getIntProperty("LEGACY_CONFLATION_TIME", 40);
			int lsurgeThreshold = cpdProperties.getIntProperty("LEGACY_SURGE_THRESHOLD", 40000);
			JMSPropertyBean legacyJmsProperty = new JMSPropertyBean(lqos, lclientId, ltopicName, lconflationTime,
					lsurgeThreshold);
			legacyDispacther.startDispatcher("NBEquity", datacache.getLegacymQueue(), legacyJmsProperty);
		}
		if (datacache.isWebSocketDistribution()) {
			String qos = cpdProperties.getStringProperty("JMS_QT_QOS", null);
			String clientId = cpdProperties.getStringProperty("JMS_CLIENT_ID", null);
			String topicName = cpdProperties.getStringProperty("JMS_QT_TOPIC_NAME", null);
			int conflationTime = cpdProperties.getIntProperty("CONFLATION_TIME", 40);
			int surgeThreshold = cpdProperties.getIntProperty("SURGE_THRESHOLD", 40000);
			JMSPropertyBean jmsProperty = new JMSPropertyBean(qos, clientId, topicName, conflationTime, surgeThreshold);
			boolean writeFile = cpdProperties.getBooleanProperty("WRITE_FILE", false);
			tradeDispacther.startDispatcher("NBEquity_Trade", datacache.getTrademQueue(), "trade", writeFile,
					datacache.getCachedTradeData(), jmsProperty);
			quoteDispacther.startDispatcher("NBEquity_Quote", datacache.getQuotemQueue(), "quote", writeFile,
					datacache.getCachedQuoteData(), jmsProperty);

		}
		this.numberofChannels = cpdProperties.getIntProperty("NO_OF_CHANNELS", 6);
		this.apiPort = cpdProperties.getIntProperty("API_PORT", 8283);
		boolean runMemoryMonitor = cpdProperties.getBooleanProperty("SHOULD_MONITOR_MEMORY", false);
		if (runMemoryMonitor) {
			logger.info(() -> "JVM memory monitor thread started");
			this.jvmMemoryMonitor = new JVMMemoryMonitor(cpdProperties.getLongProperty("TIME_INTERVAL", 15000l));
			this.jvmMemoryMonitor.startThread();
		}
		stats.startLoggerThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			stop();
			channelManager.stopChannels();
			volPlusChannelManager.stopChannels();
			tradeDispacther.close();
			quoteDispacther.close();
			legacyDispacther.close();
			try {
				datacache.stopThread();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			this.metaUtility.close();
			try {
				this.metaUtilityThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (this.jvmMemoryMonitor != null)
				this.jvmMemoryMonitor.stopThread();
			logger.info("Going to reset logger");
			LogManager.getLogManager().reset();
		}, "ShutDownHookThread"));
	}

	private void startProcess() {
		port(this.apiPort);
		path("nbcpd", () -> {
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
				get("", NbDataController.getData, gson::toJson);
				get("/dump/:dataType", NbDataController.getDataByServiceId);
				post("/list", NbDataController.getDataList, gson::toJson);
			});
			path("/subscribe", () -> get("", NbDataController.subscribe, gson::toJson));
			path("/unsubscribe", () -> get("", NbDataController.unsubscribe, gson::toJson));
			path("/list", () -> {
				get("/meta/:serviceId", NbDataController.getTickerServices, gson::toJson);
				get("/image", NbDataController.getImageTickers, gson::toJson);
				get("/pending", NbDataController.getPendingTickers, gson::toJson);
				get("/root", NbDataController.getRootSymbols, gson::toJson);
				get("/subscribed", NbDataController.getNasdaqBasicSymbols, gson::toJson);
			});
			path("/filewriter", () -> {
				get("/start/:dataType", NbDataController.startFileWriter, gson::toJson);
				get("/stop/:dataType", NbDataController.stopFileWriter, gson::toJson);
			});
		});
		after("*", (request, response) -> {
			String gzip = request.headers("Content-Encoding");
			if (gzip != null && "gzip".equalsIgnoreCase(gzip))
				response.header("Content-Encoding", "gzip");
			response.type("application/json");
		});
		ArrayList<CPDChannel> channelList = new ArrayList<>();
		for (int i = 0; i <= this.numberofChannels; i++) {
			NbSubscriptionChannel channel = new NbSubscriptionChannel(this.ip, this.port, "NbChan" + i);
			channelList.add(channel);
		}
		channelManager.init(channelList);
		Set<String> nbSymbolSet = datacache.getNbMetaUcTickerSet();
		int tickersPerChannel;
		tickersPerChannel = nbSymbolSet.size() / (this.numberofChannels);
		logger.info(() -> "Total number of tickers for nb channel: " + nbSymbolSet.size());
		logger.info(() -> "Tickers per nb channel: " + tickersPerChannel);
		Set<String> tickerPerChannelList = new HashSet<>();
		int count = 0;
		int channelCount = 1;
		for (String ticker : nbSymbolSet) {
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
		String volPlusIp = cpdProperties.getStringProperty("VOL_PLUS_UC_IP", null);
		if (volPlusIp == null) {
			logger.info("Clsoing volume plus channels as VOL_PLUS_UC_IP is null.");
		} else {
			int volPLusPort = cpdProperties.getIntProperty("VOL_PLUS_PORT", 4321);
			ArrayList<CPDChannel> volPlusChannelList = new ArrayList<>();
			for (int i = 0; i <= this.numberofChannels; i++) {
				VolumePlusEquityChannel channel = new VolumePlusEquityChannel(volPlusIp, volPLusPort, "VolPlusNB_" + i);
				volPlusChannelList.add(channel);
			}
			volPlusChannelManager.init(volPlusChannelList);
			Set<String> rootSymbolSet = datacache.getMetaTickerSet();
			int volumePlusTickersPerChannel;
			volumePlusTickersPerChannel = rootSymbolSet.size() / (this.numberofChannels);
			logger.info(() -> "Total number of tickers for volplus : " + rootSymbolSet.size());
			logger.info(() -> "Tickers per volume plus channel: " + volumePlusTickersPerChannel);
			Set<String> volumeTickerPerChannelList = new HashSet<>();
			count = 0;
			channelCount = 1;
			for (String ticker : rootSymbolSet) {
				++count;
				volumeTickerPerChannelList.add(ticker);
				if (count == volumePlusTickersPerChannel && channelCount != this.numberofChannels) {
					volPlusChannelManager.subscribeTickers(volumeTickerPerChannelList);
					volumeTickerPerChannelList = new HashSet<>();
					count = 0;
					channelCount++;
				}
			}
			if (count > 0) {
				volPlusChannelManager.subscribeTickers(volumeTickerPerChannelList);
			}
		}
		logger.info("Complete ticker list subscribed on all channels");
	}

	public static void main(String[] args) {
		NasdaqBasicCpd cpd = new NasdaqBasicCpd();
		cpd.startProcess();
	}
}
