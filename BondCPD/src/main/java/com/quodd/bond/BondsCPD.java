package com.quodd.bond;

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

import com.quodd.bond.util.BondsDataFileWriter;
import com.quodd.bond.util.BondsMetaDataUtility;
import com.quodd.common.cpd.CPD;
import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.monitoring.JVMMemoryMonitor;
import com.quodd.common.cpd.monitoring.StatUtility;
import com.quodd.common.cpd.routes.CPDRoutes;
import com.quodd.common.ibus.JMSPropertyBean;

public class BondsCPD extends CPD {

	public static final BondsCache datacache = new BondsCache();
	private final BondsMetaDataUtility metaUtility = new BondsMetaDataUtility();
	private final Thread metaUtilityThread = new Thread(this.metaUtility);
	private final int numberofChannels;
	private final int apiPort;
	private String ip;
	private int port;
	private BondsDataFileWriter bondThread = null;
	private JVMMemoryMonitor jvmMemoryMonitor;

	public BondsCPD() {
		super();
		this.metaUtilityThread.start();
		this.bondThread = new BondsDataFileWriter();
		this.bondThread.setName("BondWriterThread");
		this.bondThread.start();
		this.ip = cpdProperties.getStringProperty("SUBSCRIBE_IP", null);
		if (this.ip == null) {
			logger.info("Exiting CPD");
			System.exit(0);
		}
		this.port = cpdProperties.getIntProperty("SUBSCRIBE_PORT", 4321);
		if (datacache.isLegacyDistribution()) {
			String lqos = cpdProperties.getStringProperty("LEGACY_JMS_QT_QOS", null);
			String lclientId = cpdProperties.getStringProperty("LEGACY_JMS_CLIENT_ID", null);
			String ltopicName = cpdProperties.getStringProperty("LEGACY_JMS_QT_TOPIC_NAME", null);
			int lconflationTime = cpdProperties.getIntProperty("LEGACY_CONFLATION_TIME", 40);
			int lsurgeThreshold = cpdProperties.getIntProperty("LEGACY_SURGE_THRESHOLD", 40000);
			JMSPropertyBean legacyJmsProperty = new JMSPropertyBean(lqos, lclientId, ltopicName, lconflationTime,
					lsurgeThreshold);
			legacyDispacther.startDispatcher("Bonds", datacache.getLegacymQueue(), legacyJmsProperty);
		}
		if (datacache.isWebSocketDistribution()) {
			String qos = cpdProperties.getStringProperty("JMS_QT_QOS", null);
			String clientId = cpdProperties.getStringProperty("JMS_CLIENT_ID", null);
			String topicName = cpdProperties.getStringProperty("JMS_QT_TOPIC_NAME", null);
			int conflationTime = cpdProperties.getIntProperty("CONFLATION_TIME", 40);
			int surgeThreshold = cpdProperties.getIntProperty("SURGE_THRESHOLD", 40000);
			JMSPropertyBean jmsProperty = new JMSPropertyBean(qos, clientId, topicName, conflationTime, surgeThreshold);
			boolean writeFile = cpdProperties.getBooleanProperty("WRITE_FILE", false);
			tradeDispacther.startDispatcher("BondsTrade", datacache.getTrademQueue(), "trade", writeFile,
					datacache.getCachedTradeData(), jmsProperty);
			quoteDispacther.startDispatcher("BondsQuote", datacache.getQuotemQueue(), "quote", writeFile,
					datacache.getCachedQuoteData(), jmsProperty);
		}
		this.numberofChannels = cpdProperties.getIntProperty("NO_OF_CHANNELS", 6);
		this.apiPort = cpdProperties.getIntProperty("API_PORT", 8285);
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
			this.bondThread.setDoRun(false);
			try {
				this.bondThread.join();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			logger.info("Going to reset logger");
			LogManager.getLogManager().reset();
		}, "ShutDownHookThread"));
	}

	private void startProcess() {
		port(this.apiPort);
		path("bondcpd", () -> {
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
				get("", BondsDataController.getData, gson::toJson);
				get("/dump/:dataType", BondsDataController.getDataByServiceId);
				post("/list", BondsDataController.getDataList, gson::toJson);
			});
			path("/subscribe", () -> get("", BondsDataController.subscribe, gson::toJson));
			path("/unsubscribe", () -> get("", BondsDataController.unsubscribe, gson::toJson));
			path("/list", () -> {
				get("/meta/:serviceId", BondsDataController.getTickerServices, gson::toJson);
				get("/image", BondsDataController.getImageTickers, gson::toJson);
				get("/pending", BondsDataController.getPendingTickers, gson::toJson);
				get("/root", BondsDataController.getRootSymbols, gson::toJson);
			});
			path("/filewriter", () -> {
				get("/start/:dataType", BondsDataController.startFileWriter, gson::toJson);
				get("/stop/:dataType", BondsDataController.stopFileWriter, gson::toJson);
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
			BondsSubsChannel channel = new BondsSubsChannel(this.ip, this.port, "BondChan" + i);
			channelList.add(channel);
		}
		channelManager.init(channelList);
		Set<String> tickerSet = datacache.getMetaTickerSet();
		int tickersPerChannel;
		tickersPerChannel = tickerSet.size() / (this.numberofChannels);
		logger.info(() -> "Total number of tickers: " + tickerSet.size());
		logger.info(() -> "Tickers per channel: " + tickersPerChannel);
		Set<String> tickerSetPerChannel = new HashSet<>();
		int count = 0;
		int channelCount = 1;
		for (String ticker : tickerSet) {
			++count;
			tickerSetPerChannel.add(ticker);
			if (count == tickersPerChannel && channelCount != this.numberofChannels) {
				channelManager.subscribeTickers(tickerSetPerChannel);
				tickerSetPerChannel = new HashSet<>();
				count = 0;
				channelCount++;
			}
		}
		if (count > 0) {
			channelManager.subscribeTickers(tickerSetPerChannel);
		}
		logger.info("Complete ticker list subscribed on all channels");
	}

	public static void main(String[] args) {
		BondsCPD cpd = new BondsCPD();
		cpd.startProcess();
	}
}