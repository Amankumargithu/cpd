package com.quodd.otc;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.gson;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.quoteDispacther;
import static com.quodd.common.cpd.CPD.tradeDispacther;
import static com.quodd.otc.OtcCPD.datacache;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.gson.reflect.TypeToken;

import spark.Request;
import spark.Response;
import spark.Route;

public class OtcDataController {

	private static final Type dataListType = new TypeToken<Map<String, Set<String>>>() {
	}.getType();

	public static final Route getData = (Request request, Response response) -> {
		String ticker = request.queryParams("ticker");
		String dataType = request.queryParams("dataType");
		return datacache.getData(ticker, dataType);
	};

	public static final Route getDataByServiceId = (Request request, Response response) -> {
		String dataType = request.params("dataType");
		return datacache.getDataByService(dataType);
	};

	public static final Route getDataList = (Request request, Response response) -> {
		try {
			String body = request.body();
			body = URLDecoder.decode(body, "UTF-8");
			Map<String, Set<String>> map = gson.fromJson(body, dataListType);
			return datacache.getDataList(map);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return null;
	};

	public static final Route subscribe = (Request request, Response response) -> {
		String ticker = request.queryParams("ticker");
		if (datacache.getImageTickerMap().containsKey(ticker)) {
			return "Symbol Already subscribed " + ticker;
		}
		HashSet<String> tickerList = new HashSet<>();
		tickerList.add(ticker);
		channelManager.subscribeTickers(tickerList);
		return "Symbol subscribed " + ticker;
	};

	public static final Route unsubscribe = (Request request, Response response) -> {
		String ticker = request.queryParams("ticker");
		datacache.unsubscribeTicker(ticker);
		return "Symbol unsubscribed " + ticker;
	};

	public static final Route getRootSymbols = (Request request, Response response) -> datacache.getOtcSymbolMap()
			.keySet();

	public static final Route getUcSymbols = (Request request, Response response) -> datacache.getMetaUcTickerSet();

	public static final Route getDuallyQuoted = (Request request, Response response) -> datacache.getOtcbbSymbolMap()
			.keySet();
	
	public static final Route getSymbolTier = (Request request, Response response) -> datacache.getMarketTierSet();

	public static final Route getUniqueOtcSymbol = (Request request, Response response) -> {
		HashSet<String> uniqueSet = new HashSet<>();
		uniqueSet.addAll(datacache.getOtcSymbolMap().keySet());
		uniqueSet.removeAll(datacache.getOtcbbSymbolMap().keySet());
		return uniqueSet;
	};

	public static final Route getTickerServices = (Request request, Response response) -> {
		String serviceId = request.params("serviceId");
		int id = 0;
		try {
			id = Integer.parseInt(serviceId);
		} catch (Exception e) {
			return new HashSet<>();
		}
		if (datacache.getOtcTradeProtocol() == id || datacache.getOtcQuoteProtocol() == id
				|| datacache.getOtcSnapTradeProtocol() == id || datacache.getOtcSnapQuoteProtocol() == id) {
			return datacache.getOtcMetaQuoddTickerSet().keySet();
		} else if (datacache.getOtcbbTradeProtocol() == id || datacache.getOtcbbQuoteProtocol() == id
				|| datacache.getOtcbbSnapTradeProtocol() == id || datacache.getOtcbbSnapQuoteProtocol() == id) {
			return datacache.getOtcbbMetaQuoddTickerSet().keySet();
		}
		return new HashSet<>();
	};

	public static final Route getImageTickers = (Request request, Response response) -> datacache.getImageTickerMap()
			.keySet();

	public static final Route getPendingTickers = (Request request, Response response) -> datacache
			.getPendingTickerMap().keySet();

	public static final Route startFileWriter = (Request request, Response response) -> {
		String dataType = request.params("dataType");
		if ("trade".equals(dataType)) {
			tradeDispacther.startFileThread();
		} else if ("quote".equals(dataType)) {
			quoteDispacther.startFileThread();
		} else {
			return "Unable to process. Wrong command";
		}
		return "File Write Thread Started";
	};

	public static final Route stopFileWriter = (Request request, Response response) -> {
		String dataType = request.params("dataType");
		if ("trade".equals(dataType)) {
			tradeDispacther.stopFileThread();
		} else if ("quote".equals(dataType)) {
			quoteDispacther.stopFileThread();
		} else {
			return "Unable to process. Wrong command";
		}
		return "File Write Thread Stopped";
	};

	private OtcDataController() {
		throw new UnsupportedOperationException();
	}
}
