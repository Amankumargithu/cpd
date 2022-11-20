package com.quodd.canadian;

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

public class CanadianCache extends CPDCache {

	public static final String EXCHANGE_XTSE = "XTSE";
	public static final String EXCHANGE_XTSX = "XTSX";
	public static final String SUFFIX_UC_XTSE = ".CA/TO";
	public static final String SUFFIX_UC_XTSX = ".CA/TV";
	private ConcurrentMap<String, String> xtseUcTickerSet = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> xtsxUcTickerSet = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> xtseQuoddTickerSet = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> xtsxQuoddTickerSet = new ConcurrentHashMap<>();
	private int xtseTradeProtocol = 0;
	private int xtseQuoteProtocol = 0;
	private int xtsxTradeProtocol = 0;
	private int xtsxQuoteProtocol = 0;
	private String xtseQuoddSuffix = null;
	private String xtsxQuoddSuffix = null;
	private int xtseSnapTradeProtocol = 0;
	private int xtseSnapQuoteProtocol = 0;
	private int xtsxSnapTradeProtocol = 0;
	private int xtsxSnapQuoteProtocol = 0;
	private String xtseTapeset = null;
	private String xtsxTapeset = null;
	private byte[] xtseTradeData;
	private byte[] xtseQuoteData;
	private byte[] xtsxTradeData;
	private byte[] xtsxQuoteData;
	private Map<Integer, Integer> snapStreamingProtocolMap = new HashMap<>();
	private final Map<String, Object> defaultMap = new HashMap<>();

	public CanadianCache() {
		super();
		this.xtseTradeProtocol = cpdProperties.getIntProperty("XTSE_TRADE_PROTOCOL", 0);
		this.xtseQuoteProtocol = cpdProperties.getIntProperty("XTSE_QUOTE_PROTOCOL", 0);
		this.xtsxTradeProtocol = cpdProperties.getIntProperty("XTSX_TRADE_PROTOCOL", 0);
		this.xtsxQuoteProtocol = cpdProperties.getIntProperty("XTSX_QUOTE_PROTOCOL", 0);
		this.xtseSnapTradeProtocol = cpdProperties.getIntProperty("XTSE_SNAP_TRADE_PROTOCOL", 0);
		this.xtseSnapQuoteProtocol = cpdProperties.getIntProperty("XTSE_SNAP_QUOTE_PROTOCOL", 0);
		this.xtsxSnapTradeProtocol = cpdProperties.getIntProperty("XTSX_SNAP_TRADE_PROTOCOL", 0);
		this.xtsxSnapQuoteProtocol = cpdProperties.getIntProperty("XTSX_SNAP_QUOTE_PROTOCOL", 0);
		String allowedProtocolStr = cpdProperties.getStringProperty("ALLOWED_PROTOCOLS", "");
		try {
			String[] arr = allowedProtocolStr.split(",");
			for (String s : arr)
				this.allowedProtocols.add(Integer.parseInt(s));
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.xtseQuoddSuffix = cpdProperties.getStringProperty("SUFFIX_XTSE", ".T");
		this.xtsxQuoddSuffix = cpdProperties.getStringProperty("SUFFIX_XTSX", ".V");
		this.xtsxTapeset = cpdProperties.getStringProperty("XTSX_TAPESET", "xtsx");
		this.xtseTapeset = cpdProperties.getStringProperty("XTSE_TAPESET", "xtse");
		this.snapStreamingProtocolMap.put(this.xtseSnapTradeProtocol, this.xtseTradeProtocol);
		this.snapStreamingProtocolMap.put(this.xtseSnapQuoteProtocol, this.xtseQuoteProtocol);
		this.snapStreamingProtocolMap.put(this.xtsxSnapTradeProtocol, this.xtsxTradeProtocol);
		this.snapStreamingProtocolMap.put(this.xtsxSnapQuoteProtocol, this.xtsxQuoteProtocol);
	}

	public String getXtseQuoddSuffix() {
		return this.xtseQuoddSuffix;
	}

	public String getXtsxQuoddSuffix() {
		return this.xtsxQuoddSuffix;
	}

	public ConcurrentMap<String, String> getXtseUcTickerSet() {
		return this.xtseUcTickerSet;
	}

	public ConcurrentMap<String, String> getXtsxUcTickerSet() {
		return this.xtsxUcTickerSet;
	}

	public ConcurrentMap<String, String> getXtseQuoddTickerSet() {
		return this.xtseQuoddTickerSet;
	}

	public ConcurrentMap<String, String> getXtsxQuoddTickerSet() {
		return this.xtsxQuoddTickerSet;
	}

	public int getXtseTradeProtocol() {
		return this.xtseTradeProtocol;
	}

	public int getXtseQuoteProtocol() {
		return this.xtseQuoteProtocol;
	}

	public int getXtsxTradeProtocol() {
		return this.xtsxTradeProtocol;
	}

	public int getXtsxQuoteProtocol() {
		return this.xtsxQuoteProtocol;
	}

	@Override
	public Map<String, Object> getData(String ticker, String dataType) {
		String requestTicker = ticker + "|" + dataType;
		Map<String, Object> hashMap = new HashMap<>();
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ((serviceCode == this.xtseTradeProtocol && this.xtseQuoddTickerSet.get(ticker) != null)
				|| (serviceCode == this.xtsxTradeProtocol && this.xtsxQuoddTickerSet.get(ticker) != null)) {
			hashMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "trade");
			map.put("data", this.cachedTradeData.getOrDefault(requestTicker, defaultMap));
			hashMap.put("tickerData", map);
			return hashMap;
		}
		if ((serviceCode == this.xtseQuoteProtocol && this.xtseQuoddTickerSet.get(ticker) != null)
				|| (serviceCode == this.xtsxQuoteProtocol && this.xtsxQuoddTickerSet.get(ticker) != null)) {
			hashMap.put("ticker", requestTicker);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("event", "quote");
			map.put("data", this.cachedQuoteData.getOrDefault(requestTicker, defaultMap));
			hashMap.put("tickerData", map);
			return hashMap;
		}
		return hashMap;
	}

	@Override
	public Object getDataByService(String dataType) {
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType);
			return "";
		}
		if (serviceCode == this.xtseTradeProtocol) {
			return this.xtseTradeData;
		} else if (serviceCode == this.xtseQuoteProtocol) {
			return this.xtseQuoteData;
		} else if (serviceCode == this.xtsxTradeProtocol) {
			return this.xtsxTradeData;
		} else if (serviceCode == this.xtsxQuoteProtocol) {
			return this.xtsxQuoteData;
		}
		return "";
	}

	public String canadianToQuoddSymbology(String ticker) {
		if (ticker.endsWith(SUFFIX_UC_XTSE))
			ticker = ticker.replace(SUFFIX_UC_XTSE, this.xtseQuoddSuffix);
		if (ticker.endsWith(SUFFIX_UC_XTSX))
			ticker = ticker.replace(SUFFIX_UC_XTSX, this.xtsxQuoddSuffix);
		return ticker;
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

	@Override
	public void loadMarketDataAsString() {
		Set<Map<String, Object>> xtseTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> xtseQuoteresultSet = new HashSet<>();
		Set<Map<String, Object>> xtsxTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> xtsxQuoteresultSet = new HashSet<>();

		this.xtseQuoddTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.xtseTradeProtocol);
			if (tradeMap != null)
				xtseTraderesultSet.add(tradeMap);
			Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.xtseQuoteProtocol);
			if (quoteMap != null)
				xtseQuoteresultSet.add(quoteMap);
		});

		this.xtsxQuoddTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.xtsxTradeProtocol);
			if (tradeMap != null)
				xtsxTraderesultSet.add(tradeMap);
			Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.xtsxQuoteProtocol);
			if (quoteMap != null)
				xtsxQuoteresultSet.add(quoteMap);
		});
		try {
			this.xtseTradeData = (gson.toJson(xtseTraderesultSet)).getBytes();
			this.xtseQuoteData = (gson.toJson(xtseQuoteresultSet)).getBytes();
			this.xtsxTradeData = (gson.toJson(xtsxTraderesultSet)).getBytes();
			this.xtsxQuoteData = (gson.toJson(xtsxQuoteresultSet)).getBytes();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public int getXtseSnapTradeProtocol() {
		return this.xtseSnapTradeProtocol;
	}

	public int getXtseSnapQuoteProtocol() {
		return this.xtseSnapQuoteProtocol;
	}

	public int getXtsxSnapTradeProtocol() {
		return this.xtsxSnapTradeProtocol;
	}

	public int getXtsxSnapQuoteProtocol() {
		return this.xtsxSnapQuoteProtocol;
	}

	public String getXtseTapeset() {
		return xtseTapeset;
	}

	public String getXtsxTapeset() {
		return xtsxTapeset;
	}
}
