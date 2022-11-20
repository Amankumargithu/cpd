package com.quodd.mf;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.gson;
import static com.quodd.common.cpd.CPD.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.quodd.common.cpd.collection.CPDCache;

public class MfCache extends CPDCache {

	private int serviceCode = 0;
	private int snapServiceCode = 0;
	private byte[] mfServiceData;
	private final Map<String, Object> defaultMap = new ConcurrentHashMap<>();

	public MfCache() {
		this.serviceCode = cpdProperties.getIntProperty("SERVICE_PROTOCOL", 0);
		this.snapServiceCode = cpdProperties.getIntProperty("SNAP_SERVICE_PROTOCOL", 0);
	}

	@Override
	public Map<String, Object> getData(final String ticker, final String dataType) {
		Map<String, Object> resultMap = new HashMap<>();
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType, e);
			return resultMap;
		}
		String requestTicker = ticker + "|" + dataType;
		if (serviceCode == this.serviceCode && getMetaTickerSet().contains(ticker)) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "nav");
			map.put("data", this.cachedTradeData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
		}
		return resultMap;
	}

	@Override
	public Object getDataList(Map<String, Set<String>> tickerMap) {
		ArrayList<Map<String, Object>> snapList = new ArrayList<>();
		if (tickerMap == null)
			return snapList;
		logger.info("tickerMap : " + tickerMap);
		tickerMap.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Set<String> protocolSet = entry.getValue();
			if (ticker == null || protocolSet == null) {
				return;
			}
			logger.info("ticker : " + ticker);
			Map<String, Object> snapMap = new HashMap<>();
			for (String protocol : protocolSet) {
				try {
					logger.info("protocol: " + protocol);
					String requString = ticker + "|" + this.serviceCode;
					logger.info(requString);
					if (this.cachedTradeData.get(requString) != null)
						snapMap.putAll(this.cachedTradeData.get(requString));
				} catch (Exception e) {
					logger.log(Level.WARNING, "exception " + protocol + " " + ticker, e);
				}
			}
			if (snapMap.size() > 0) {
				snapList.add(snapMap);
			}
		});
		return snapList;
	}

	@Override
	public Object getDataByService(String dataType) {
		return this.mfServiceData;
	}

	@Override
	public void loadMarketDataAsString() {
		this.mfServiceData = (gson.toJson(this.cachedTradeData.values())).getBytes();
	}

	public int getServiceCode() {
		return this.serviceCode;
	}

	public int getSnapServiceCode() {
		return this.snapServiceCode;
	}
}