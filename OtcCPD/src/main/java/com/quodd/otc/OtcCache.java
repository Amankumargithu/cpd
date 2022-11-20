package com.quodd.otc;

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

public class OtcCache extends CPDCache {

	private int otcTradeProtocol = 0;
	private int otcQuoteProtocol = 0;
	private int otcbbTradeProtocol = 0;
	private int otcbbQuoteProtocol = 0;

	private int otcSnapTradeProtocol = 0;
	private int otcSnapQuoteProtocol = 0;
	private int otcbbSnapTradeProtocol = 0;
	private int otcbbSnapQuoteProtocol = 0;

	private ConcurrentMap<String, String> otcSymbolMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> otcbbSymbolMap = new ConcurrentHashMap<>(); // keyset will be otcbb Meta Quodd
	private ConcurrentMap<String, String> otcMetaQuoddTickerSet = new ConcurrentHashMap<>(); // OTC meta Quodd symbols
	private ConcurrentMap<String, String> otcbbMetaQuoddTickerSet = new ConcurrentHashMap<>(); // OTC meta Quodd symbols
	private Set<String> metaUcTickerSet = new HashSet<>(); // meta UC symbols for subscription

	private String otcSymbolSuffix = null;
	private byte[] otcTradeData;
	private byte[] otcQuoteData;
	private byte[] otcbbTradeData;
	private byte[] otcbbQuoteData;
	private Map<Integer, Integer> snapStreamingProtocolMap = new HashMap<>();
	private String otcTapeset = null;
	private String otcbbTapeset = null;
	private final Map<String, Object> defaultMap = new HashMap<>();
	private final Map<String, String> tierMappingMap = new HashMap<>();
	private final ConcurrentMap<String, String> marketTierSet = new ConcurrentHashMap<>();

	public OtcCache() {
		super();
		this.otcTradeProtocol = cpdProperties.getIntProperty("OTC_TRADE_PROTOCOL", 0);
		this.otcQuoteProtocol = cpdProperties.getIntProperty("OTC_QUOTE_PROTOCOL", 0);
		this.otcbbTradeProtocol = cpdProperties.getIntProperty("OTCBB_TRADE_PROTOCOL", 0);
		this.otcbbQuoteProtocol = cpdProperties.getIntProperty("OTCBB_QUOTE_PROTOCOL", 0);
		this.otcSnapTradeProtocol = cpdProperties.getIntProperty("OTC_SNAP_TRADE_PROTOCOL", 0);
		this.otcSnapQuoteProtocol = cpdProperties.getIntProperty("OTC_SNAP_QUOTE_PROTOCOL", 0);
		this.otcbbSnapTradeProtocol = cpdProperties.getIntProperty("OTCBB_SNAP_TRADE_PROTOCOL", 0);
		this.otcbbSnapQuoteProtocol = cpdProperties.getIntProperty("OTCBB_SNAP_QUOTE_PROTOCOL", 0);
		this.otcSymbolSuffix = cpdProperties.getStringProperty("OTC_SYMBOL_SUFFIX", ".PK");
		String allowedProtocolStr = cpdProperties.getStringProperty("ALLOWED_PROTOCOLS", "");
		try {
			String[] arr = allowedProtocolStr.split(",");
			for (String s : arr)
				this.allowedProtocols.add(Integer.parseInt(s));
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.otcTapeset = cpdProperties.getStringProperty("OTC_TAPESET", "otc");
		this.otcbbTapeset = cpdProperties.getStringProperty("OTCBB_TAPESET", "otcbb");
		this.snapStreamingProtocolMap.put(this.otcSnapTradeProtocol, this.otcTradeProtocol);
		this.snapStreamingProtocolMap.put(this.otcSnapQuoteProtocol, this.otcQuoteProtocol);
		this.snapStreamingProtocolMap.put(this.otcbbSnapTradeProtocol, this.otcbbTradeProtocol);
		this.snapStreamingProtocolMap.put(this.otcbbSnapQuoteProtocol, this.otcbbQuoteProtocol);

		this.tierMappingMap.put("1", "NASDAQ Global Select");
		this.tierMappingMap.put("2", "NASDAQ Global");
		this.tierMappingMap.put("3", "NASDAQ Capital");
		this.tierMappingMap.put("4", "NYSE");
		this.tierMappingMap.put("5", "NYSE Mkts");
		this.tierMappingMap.put("6", "NYSE Arca");
		this.tierMappingMap.put("7", "BATS");
		this.tierMappingMap.put("8", "OTC Markets No Tier");
		this.tierMappingMap.put("9", "OTCQX US Premier");
		this.tierMappingMap.put("10", "OTCQX US");
		this.tierMappingMap.put("11", "OTCQX International Premier");
		this.tierMappingMap.put("12", "OTCQX International");
		this.tierMappingMap.put("13", "OTCQB");
		this.tierMappingMap.put("14", "OTCBB");
		this.tierMappingMap.put("15", "OTC Pink Current");
		this.tierMappingMap.put("16", "OTC Pink Limited");
		this.tierMappingMap.put("17", "OTC Pink No Information");
		this.tierMappingMap.put("18", "OTC Grey Market");
		this.tierMappingMap.put("19", "OTC Yellow");
		this.tierMappingMap.put("20", "OTC Bonds");
		this.tierMappingMap.put("21", "Funds News Media List");
		this.tierMappingMap.put("22", "Funds Supplemental List");
		this.tierMappingMap.put("23", "IEX");
	}

	public String getOtcSymbolSuffix() {
		return this.otcSymbolSuffix;
	}

	public int getOtcTradeProtocol() {
		return this.otcTradeProtocol;
	}

	public int getOtcQuoteProtocol() {
		return this.otcQuoteProtocol;
	}

	public ConcurrentMap<String, String> getOtcSymbolMap() {
		return this.otcSymbolMap;
	}

	public ConcurrentMap<String, String> getOtcbbSymbolMap() {
		return this.otcbbSymbolMap;
	}

	public ConcurrentMap<String, String> getOtcMetaQuoddTickerSet() {
		return this.otcMetaQuoddTickerSet;
	}

	public Set<String> getMetaUcTickerSet() {
		return this.metaUcTickerSet;
	}

	public int getOtcbbTradeProtocol() {
		return this.otcbbTradeProtocol;
	}

	public int getOtcbbQuoteProtocol() {
		return this.otcbbQuoteProtocol;
	}

	public int getOtcSnapTradeProtocol() {
		return this.otcSnapTradeProtocol;
	}

	public int getOtcSnapQuoteProtocol() {
		return this.otcSnapQuoteProtocol;
	}

	public int getOtcbbSnapTradeProtocol() {
		return this.otcbbSnapTradeProtocol;
	}

	public int getOtcbbSnapQuoteProtocol() {
		return this.otcbbSnapQuoteProtocol;
	}

	public ConcurrentMap<String, String> getOtcbbMetaQuoddTickerSet() {
		return this.otcbbMetaQuoddTickerSet;
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
		if (serviceCode == this.otcTradeProtocol && this.otcMetaQuoddTickerSet.get(ticker) != null
				|| serviceCode == this.otcbbTradeProtocol && this.otcbbMetaQuoddTickerSet.get(ticker) != null) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "trade");
			map.put("data", this.cachedTradeData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
		} else if (serviceCode == this.otcQuoteProtocol && this.otcMetaQuoddTickerSet.get(ticker) != null
				|| serviceCode == this.otcbbQuoteProtocol && this.otcbbMetaQuoddTickerSet.get(ticker) != null) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "quote");
			map.put("data", this.cachedQuoteData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
		}
		return resultMap;
	}

	@Override
	public void loadMarketDataAsString() {
		Set<Map<String, Object>> otcTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> otcQuoteresultSet = new HashSet<>();
		Set<Map<String, Object>> otcbbTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> otcbbQuoteresultSet = new HashSet<>();
		this.otcMetaQuoddTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.otcTradeProtocol);
			if (tradeMap != null)
				otcTraderesultSet.add(tradeMap);
			Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.otcQuoteProtocol);
			if (quoteMap != null)
				otcQuoteresultSet.add(quoteMap);
		});

		this.otcbbMetaQuoddTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.otcbbTradeProtocol);
			if (tradeMap != null)
				otcbbTraderesultSet.add(tradeMap);
			Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.otcbbQuoteProtocol);
			if (quoteMap != null)
				otcbbQuoteresultSet.add(quoteMap);
		});
		try {
			this.otcTradeData = (gson.toJson(otcTraderesultSet)).getBytes();
			this.otcQuoteData = (gson.toJson(otcQuoteresultSet)).getBytes();
			this.otcbbTradeData = (gson.toJson(otcbbTraderesultSet)).getBytes();
			this.otcbbQuoteData = (gson.toJson(otcbbQuoteresultSet)).getBytes();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
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
		if (serviceCode == this.otcTradeProtocol)
			return this.otcTradeData;
		if (serviceCode == this.otcQuoteProtocol)
			return this.otcQuoteData;
		if (serviceCode == this.otcbbTradeProtocol)
			return this.otcbbTradeData;
		if (serviceCode == this.otcbbQuoteProtocol)
			return this.otcbbQuoteData;
		return "";
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
			if (ticker != null && protocolSet != null) {
				for (String protocol : protocolSet) {
					int serviceCode = 0;
					try {
						serviceCode = Integer.parseInt(protocol);
						int streamingCode = this.snapStreamingProtocolMap.get(serviceCode);
						if (this.cachedTradeData.get(ticker + "|" + streamingCode) != null)
							snapMap.putAll(this.cachedTradeData.get(ticker + "|" + streamingCode));
						if (this.cachedQuoteData.get(ticker + "|" + streamingCode) != null)
							snapMap.putAll(this.cachedQuoteData.get(ticker + "|" + streamingCode));
					} catch (Exception e) {
						logger.log(Level.WARNING, "exception" + protocol + " " + ticker);
					}
				}
			}
			if (snapMap.size() > 0)
				snapList.add(snapMap);
		});
		return snapList;
	}

	public String getOtcTapeset() {
		return otcTapeset;
	}

	public String getOtcbbTapeset() {
		return otcbbTapeset;
	}

	public ConcurrentMap<String, String> getMarketTierSet() {
		return marketTierSet;
	}

	public String getTierMapping(String tierId) {
		return this.tierMappingMap.getOrDefault(tierId, tierId);
	}

}