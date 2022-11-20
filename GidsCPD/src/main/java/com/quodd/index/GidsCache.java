package com.quodd.index;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.gson;
import static com.quodd.common.cpd.CPD.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.quodd.common.cpd.collection.CPDCache;

public class GidsCache extends CPDCache {

	private final ConcurrentMap<String, String> gidsTickerMap = new ConcurrentHashMap<>();
	private final int serviceCodeGids;
	private final int snapServiceCodeGids;
	private byte[] gidsTradeData;
	private final Map<Integer, Integer> snapStreamingProtocolMap = new HashMap<>();
	private final String tapeset;
	private final Map<String, Object> defaultMap = new HashMap<>();

	public GidsCache() {
		super();
		this.serviceCodeGids = cpdProperties.getIntProperty("GIDS_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeGids = cpdProperties.getIntProperty("GIDS_SNAP_SERVICE_PROTOCOL", 0);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeGids, this.serviceCodeGids);
		this.tapeset = cpdProperties.getStringProperty("GIDS_TAPESET", "gids");
	}

	@Override
	public void loadMarketDataAsString() {
		Set<Map<String, Object>> gidsResultSet = new HashSet<>();
		this.gidsTickerMap.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeGids);
			if (tradeMap != null)
				gidsResultSet.add(tradeMap);
		});
		try {
			this.gidsTradeData = (gson.toJson(gidsResultSet)).getBytes();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Object> getData(String ticker, String dataType) {
		Map<String, Object> resultMap = new HashMap<>();
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType, e);
			return resultMap;
		}
		String requestTicker = ticker + "|" + dataType;
		try {
			if (serviceCode == this.serviceCodeGids) {
				resultMap.put("ticker", requestTicker);
				Map<String, Object> map = new HashMap<>();
				map.put("event", "index");
				map.put("data", this.cachedTradeData.getOrDefault(requestTicker,defaultMap));
				resultMap.put("tickerData", map);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return resultMap;
	}

	@Override
	public Object getDataList(final Map<String, Set<String>> tickerMap) {
		ArrayList<Map<String, Object>> snapList = new ArrayList<>();
		if (tickerMap == null)
			return snapList;
		tickerMap.entrySet().forEach(entry -> {
			Map<String, Object> snapMap = new HashMap<>();
			String ticker = entry.getKey();
			Set<String> protocolSet = entry.getValue();
			if (ticker == null || protocolSet == null)
				return;
			for (String protocol : protocolSet) {
				int serviceCode = 0;
				try {
					serviceCode = Integer.parseInt(protocol);
					int streamingCode = this.snapStreamingProtocolMap.get(serviceCode);
					if (this.cachedTradeData.get(ticker + "|" + streamingCode) != null)
						snapMap.putAll(this.cachedTradeData.get(ticker + "|" + streamingCode));
				} catch (Exception e) {
					logger.log(Level.WARNING, "exception " + protocol + " " + ticker, e);
				}
			}
			if (snapMap.size() > 0)
				snapList.add(snapMap);
		});
		return snapList;
	}

	@Override
	public Object getDataByService(String dataType) {
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType, e);
			return "";
		}
		if (serviceCode == this.serviceCodeGids) {
			return this.gidsTradeData;
		}
		return "";
	}

	public ConcurrentMap<String, String> getGidsTickerMap() {
		return this.gidsTickerMap;
	}

	public int getServiceCodeGids() {
		return this.serviceCodeGids;
	}

	public int getSnapServiceCodeGids() {
		return this.snapServiceCodeGids;
	}

	public String getTapeset() {
		return this.tapeset;
	}
}