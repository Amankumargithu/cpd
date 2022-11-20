package com.quodd.common.cpd.routes;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.stats;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.monitoring.StatUtility;

import spark.Request;
import spark.Response;
import spark.Route;

public class CPDRoutes {

	private CPDRoutes() {
		throw new UnsupportedOperationException();
	}

	public static final Route getStatsMap = (Request request, Response response) -> {
		Map<String, Long> result = new HashMap<>();
		result.put("tradeDropCount", stats.getDroppedTradeMessages());
		result.put("quoteDropCount", stats.getDroppedQuoteMessages());
		result.put("ucProcessedTrade", stats.getProcessedUCTradeMessages());
		result.put("ucProcessedQuote", stats.getProcessedUCQuoteMessages());
		result.put("startTime", stats.getStartTime());
		result.put("currentTime", System.currentTimeMillis());
		return result;
	};

	public static final Route getSystemStats = (Request request, Response response) -> {
		Map<String, Object> allProperties = new LinkedHashMap<>();
		allProperties.put("server", InetAddress.getLocalHost().getHostName());
		allProperties.put("username", System.getProperty("user.name"));
		allProperties.put("user.dir", System.getProperty("user.dir"));
		allProperties.put("user.home", System.getProperty("user.home"));
		allProperties.put("crontab", StatUtility.getCrontab());
		allProperties.put("properties", StatUtility.getProperties("cpd.properties","log.properties","fileWriter.properties"));
		allProperties.put("helperFiles", StatUtility.getHelperFileDetails());
		return allProperties;
	};

	public static final Route getDeadSymbolsPerChannel = (Request request, Response response) -> {
		CPDChannel channel = channelManager.getChannelByName(request.params("channel"));
		if (channel == null)
			return new HashSet<>();
		return channel.getDeadSet();
	};

	public static final Route getImageSymbolsPerChannel = (Request request, Response response) -> {
		CPDChannel channel = channelManager.getChannelByName(request.params("channel"));
		if (channel == null)
			return new HashSet<>();
		return channel.getImageSet();
	};

	public static final Route getDetailedLog = (Request request, Response response) -> StatUtility.getDetailedLog(
			request.queryParams("key"), request.queryParams("position"), request.queryParams("count"),
			request.queryParams("case"));

	public static final Route getFileContentByName = (Request request, Response response) -> {
		String directory = cpdProperties.getStringProperty("META_FILE_DIR", null);
		if (directory != null) {
			File dir = new File(directory);
			File file = new File(dir + "/" + request.params("fileName"));
			if (file.exists()) {
				Path path = Paths.get(file.getAbsolutePath());
				return new String(Files.readAllBytes(path));
			}
		}
		return "";
	};
}
