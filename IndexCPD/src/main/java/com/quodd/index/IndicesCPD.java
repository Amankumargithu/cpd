package com.quodd.index;

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
import com.quodd.index.util.IndicesMetaDataUtility;

public class IndicesCPD extends CPD {
	public static final IndicesCache datacache = new IndicesCache();
	private final IndicesMetaDataUtility metaUtility = new IndicesMetaDataUtility();
	private final Thread metaUtilityThread = new Thread(this.metaUtility);
	private int numberofChannels = 6;
	private int apiPort = 8288;
	private String ip;
	private int port;
	private JVMMemoryMonitor jvmMemoryMonitor;

	public IndicesCPD() {
		this.metaUtilityThread.start();
		if (datacache.isDelayed()) {
			logger.info(() -> "Indices is delayed CPD");
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

		if (datacache.isWebSocketDistribution()) {
			String qos = cpdProperties.getStringProperty("JMS_QT_QOS", null);
			String clientId = cpdProperties.getStringProperty("JMS_CLIENT_ID", null);
			String topicName = cpdProperties.getStringProperty("JMS_QT_TOPIC_NAME", null);
			int conflationTime = cpdProperties.getIntProperty("CONFLATION_TIME", 40);
			int surgeThreshold = cpdProperties.getIntProperty("SURGE_THRESHOLD", 40000);
			JMSPropertyBean jmsProperty = new JMSPropertyBean(qos, clientId, topicName, conflationTime, surgeThreshold);
			boolean writeFile = cpdProperties.getBooleanProperty("WRITE_FILE", false);
			tradeDispacther.startDispatcher("Index", datacache.getTrademQueue(), "trade", writeFile,
					datacache.getCachedTradeData(), jmsProperty);
		}
		if (datacache.isLegacyDistribution()) {
			String lqos = cpdProperties.getStringProperty("LEGACY_JMS_QT_QOS", null);
			String lclientId = cpdProperties.getStringProperty("LEGACY_JMS_CLIENT_ID", null);
			String ltopicName = cpdProperties.getStringProperty("LEGACY_JMS_QT_TOPIC_NAME", null);
			int lconflationTime = cpdProperties.getIntProperty("LEGACY_CONFLATION_TIME", 40);
			int lsurgeThreshold = cpdProperties.getIntProperty("LEGACY_SURGE_THRESHOLD", 40000);
			JMSPropertyBean legacyJmsProperty = new JMSPropertyBean(lqos, lclientId, ltopicName, lconflationTime,
					lsurgeThreshold);
			legacyDispacther.startDispatcher("Index", datacache.getLegacymQueue(), legacyJmsProperty);
		}
		this.numberofChannels = cpdProperties.getIntProperty("NO_OF_CHANNELS", 6);
		this.apiPort = cpdProperties.getIntProperty("API_PORT", 8288);
		boolean runMemoryMonitor = cpdProperties.getBooleanProperty("SHOULD_MONITOR_MEMORY", false);
		if (runMemoryMonitor) {
			logger.info("JVM memory monitor thread started");
			this.jvmMemoryMonitor = new JVMMemoryMonitor(cpdProperties.getLongProperty("TIME_INTERVAL", 15000l));
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
			if (this.jvmMemoryMonitor != null)
				this.jvmMemoryMonitor.stopThread();
			logger.info("Going to reset logger");
			LogManager.getLogManager().reset();
		}, "ShutDownHookThread"));
	}

	private void startProcess() {
		port(this.apiPort);
		path("indcpd", () -> {
			path("/stat", () -> {
				get("", CPDRoutes.getStatsMap, gson::toJson);
				get("/channel", datacache.getChannelStats, gson::toJson);
				get("/system", CPDRoutes.getSystemStats, gson::toJson);
				get("/disk", (request, response) -> StatUtility.getDiskInformation(), gson::toJson);
				get("/ram", (request, response) -> StatUtility.getRamInformation(), gson::toJson);
				get("/count", (request, response) -> StatUtility.getErrorCountFromLog(), gson::toJson);
				get("/filecontent/:fileName", CPDRoutes.getFileContentByName, gson::toJson);
			});
			path("/snap", () -> {
				get("", IndicesDataController.getData, gson::toJson);
				get("/dump/:dataType", IndicesDataController.getDataByServiceId);
				post("/list", IndicesDataController.getDataList, gson::toJson);
			});
			path("/subscribe", () -> get("", IndicesDataController.subscribe, gson::toJson));

			path("/list", () -> {
				get("/meta/:serviceId", IndicesDataController.getTickerServices, gson::toJson);
				get("/image", IndicesDataController.getImageTickers, gson::toJson);
				get("/pending", IndicesDataController.getPendingTickers, gson::toJson);
				get("/root", IndicesDataController.getRootSymbols, gson::toJson);
			});
			path("/unsubscribe", () -> {
				get("", IndicesDataController.unsubscribe, gson::toJson);
			});
			path("/filewriter", () -> {
				get("/start/:dataType", IndicesDataController.startFileWriter, gson::toJson);
				get("/stop/:dataType", IndicesDataController.stopFileWriter, gson::toJson);
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
			IndicesSubsChannel channel = new IndicesSubsChannel(this.ip, this.port, "IdxChan" + i);
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

	public static void main(String[] args) {
		IndicesCPD cpd = new IndicesCPD();
		cpd.startProcess();
	}
}