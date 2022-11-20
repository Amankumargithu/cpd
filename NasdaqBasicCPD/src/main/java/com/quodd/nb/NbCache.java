package com.quodd.nb;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.gson;
import static com.quodd.common.cpd.CPD.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.quodd.common.cpd.collection.CPDCache;

public class NbCache extends CPDCache {

	private String symbolSuffix = null;
	private int tradeProtocol = 0;
	private int quoteProtocol = 0;
	private int snapTradeProtocol = 0;
	private int snapQuoteProtocol = 0;
	private String snapTradeProtocolStr = "";
	private String snapQuoteProtocolStr = "";
	private Set<String> nbMetaUcTickerSet = new HashSet<>(); // meta nb UC symbls
	private Set<String> nbMetaQuoddTickerSet = new HashSet<>(); // meta Quodd symbols
	private byte[] nbTradeData;
	private byte[] nbQuoteData;
	private String tapeset;
	private final Map<String, Object> defaultMap = new HashMap<>();

	public NbCache() {
		super();
		this.symbolSuffix = cpdProperties.getStringProperty("SYMBOL_SUFFIX", ".NB");
		this.tradeProtocol = cpdProperties.getIntProperty("TRADE_PROTOCOL", 0);
		this.quoteProtocol = cpdProperties.getIntProperty("QUOTE_PROTOCOL", 0);
		this.snapTradeProtocol = cpdProperties.getIntProperty("SNAP_TRADE_PROTOCOL", 0);
		this.snapQuoteProtocol = cpdProperties.getIntProperty("SNAP_QUOTE_PROTOCOL", 0);
		this.snapQuoteProtocolStr = Integer.toString(this.snapQuoteProtocol);
		this.snapTradeProtocolStr = Integer.toString(this.snapTradeProtocol);
		this.tapeset = cpdProperties.getStringProperty("TAPESET", "nb");
		String allowedProtocolStr = cpdProperties.getStringProperty("ALLOWED_PROTOCOLS", "");
		try {
			String[] arr = allowedProtocolStr.split(",");
			for (String s : arr)
				this.allowedProtocols.add(Integer.parseInt(s));
		} catch (Exception e) {
			logger.log(Level.WARNING, "issue in split allowedProtocols", e);
		}
	}

	public String getSymbolSuffix() {
		return this.symbolSuffix;
	}

	public int getTradeProtocol() {
		return this.tradeProtocol;
	}

	public int getQuoteProtocol() {
		return this.quoteProtocol;
	}

	public int getSnapTradeProtocol() {
		return this.snapTradeProtocol;
	}

	public int getSnapQuoteProtocol() {
		return this.snapQuoteProtocol;
	}

	@Override
	public Map<String, Object> getData(String ticker, String dataType) {
		String requestTicker = ticker + "|" + dataType;
		Map<String, Object> resultMap = new HashMap<>();
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType, e);
			return resultMap;
		}
		if (serviceCode == getTradeProtocol() && this.nbMetaQuoddTickerSet.contains(ticker)) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "trade");
			map.put("data", this.cachedTradeData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
			return resultMap;
		}
		if (serviceCode == getQuoteProtocol() && this.nbMetaQuoddTickerSet.contains(ticker)) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "quote");
			map.put("data", this.cachedQuoteData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
			return resultMap;
		}
		return resultMap;
	}

	public Set<String> getNbMetaUcTickerSet() {
		return this.nbMetaUcTickerSet;
	}

	public Set<String> getNbMetaQuoddTickerSet() {
		return this.nbMetaQuoddTickerSet;
	}

	@Override
	public Object getDataList(Map<String, Set<String>> tickerMap) {
		ArrayList<Map<String, Object>> snapList = new ArrayList<>();
		if (tickerMap == null)
			return snapList;
		tickerMap.entrySet().forEach(entry -> {
			Map<String, Object> snapMap = new HashMap<>();
			String ticker = entry.getKey();
			Set<String> protocolSet = entry.getValue();
			if (ticker != null && protocolSet != null && !protocolSet.isEmpty()) {
				for (String protocol : protocolSet) {
					try {
						Map<String, Object> dataMap = null;
						if (protocol.equalsIgnoreCase(this.snapTradeProtocolStr)) {
							dataMap = this.cachedTradeData.get(ticker + "|" + this.tradeProtocol);
						} else if (protocol.equalsIgnoreCase(this.snapQuoteProtocolStr)) {
							dataMap = this.cachedQuoteData.get(ticker + "|" + this.quoteProtocol);
						}
						if (dataMap != null)
							snapMap.putAll(dataMap);
					} catch (Exception e) {
						logger.log(Level.WARNING, "exception " + protocol + " " + ticker, e);
					}
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
		if (serviceCode == this.tradeProtocol) {
			return this.nbTradeData;
		} else if (serviceCode == this.quoteProtocol) {
			return this.nbQuoteData;
		}
		return "";
	}

	@Override
	public void loadMarketDataAsString() {
		try {
			this.nbTradeData = (gson.toJson(this.cachedTradeData.values())).getBytes();
			this.nbQuoteData = (gson.toJson(this.cachedQuoteData.values())).getBytes();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public String getTapeset() {
		return tapeset;
	}
}