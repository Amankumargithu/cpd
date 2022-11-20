package com.quodd.equity;

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

public class EquityCache extends CPDCache {

	private final ConcurrentMap<String, String> utpTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> ctaaTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> ctabTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> marketTierSet = new ConcurrentHashMap<>();
	private final int utpTradeProtocol;
	private final int utpQuoteProtocol;
	private final int ctaaTradeProtocol;
	private final int ctaaQuoteProtocol;
	private final int ctabTradeProtocol;
	private final int ctabQuoteProtocol;
	private final int utpSnapTradeProtocol;
	private final int utpSnapQuoteProtocol;
	private final int ctaaSnapTradeProtocol;
	private final int ctaaSnapQuoteProtocol;
	private final int ctabSnapTradeProtocol;
	private final int ctabSnapQuoteProtocol;
	private final String ctaaTapeset;
	private final String ctabTapeset;
	private final String utpTapeset;
	private byte[] utpTradeData;
	private byte[] utpQuoteData;
	private byte[] ctaaTradeData;
	private byte[] ctaaQuoteData;
	private byte[] ctabTradeData;
	private byte[] ctabQuoteData;
	private Map<Integer, Integer> snapStreamingProtocolMap = new HashMap<>();
	private final Map<String, Object> defaultMap = new HashMap<>();
	private final Map<String, String> tierMappingMap = new HashMap<>();

	public EquityCache() {
		super();
		this.utpTradeProtocol = cpdProperties.getIntProperty("UTP_TRADE_PROTOCOL", 0);
		this.utpQuoteProtocol = cpdProperties.getIntProperty("UTP_QUOTE_PROTOCOL", 0);
		this.ctaaTradeProtocol = cpdProperties.getIntProperty("CTAA_TRADE_PROTOCOL", 0);
		this.ctaaQuoteProtocol = cpdProperties.getIntProperty("CTAA_QUOTE_PROTOCOL", 0);
		this.ctabTradeProtocol = cpdProperties.getIntProperty("CTAB_TRADE_PROTOCOL", 0);
		this.ctabQuoteProtocol = cpdProperties.getIntProperty("CTAB_QUOTE_PROTOCOL", 0);
		this.utpSnapTradeProtocol = cpdProperties.getIntProperty("UTP_SNAP_TRADE_PROTOCOL", 0);
		this.utpSnapQuoteProtocol = cpdProperties.getIntProperty("UTP_SNAP_QUOTE_PROTOCOL", 0);
		this.ctaaSnapTradeProtocol = cpdProperties.getIntProperty("CTAA_SNAP_TRADE_PROTOCOL", 0);
		this.ctaaSnapQuoteProtocol = cpdProperties.getIntProperty("CTAA_SNAP_QUOTE_PROTOCOL", 0);
		this.ctabSnapTradeProtocol = cpdProperties.getIntProperty("CTAB_SNAP_TRADE_PROTOCOL", 0);
		this.ctabSnapQuoteProtocol = cpdProperties.getIntProperty("CTAB_SNAP_QUOTE_PROTOCOL", 0);
		this.ctaaTapeset = cpdProperties.getStringProperty("CTAA_TAPESET", "ctaa");
		this.ctabTapeset = cpdProperties.getStringProperty("CTAB_TAPESET", "ctab");
		this.utpTapeset = cpdProperties.getStringProperty("UTP_TAPESET", "utp");
		this.snapStreamingProtocolMap.put(this.utpSnapTradeProtocol, this.utpTradeProtocol);
		this.snapStreamingProtocolMap.put(this.utpSnapQuoteProtocol, this.utpQuoteProtocol);
		this.snapStreamingProtocolMap.put(this.ctaaSnapTradeProtocol, this.ctaaTradeProtocol);
		this.snapStreamingProtocolMap.put(this.ctaaSnapQuoteProtocol, this.ctaaQuoteProtocol);
		this.snapStreamingProtocolMap.put(this.ctabSnapTradeProtocol, this.ctabTradeProtocol);
		this.snapStreamingProtocolMap.put(this.ctabSnapQuoteProtocol, this.ctabQuoteProtocol);
		String allowedProtocolStr = cpdProperties.getStringProperty("ALLOWED_PROTOCOLS", "");
		try {
			String[] arr = allowedProtocolStr.split(",");
			for (String s : arr) {
				this.allowedProtocols.add(Integer.parseInt(s));
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "issue in split allowedProtocols", e);
		}
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

	public ConcurrentMap<String, String> getUtpTickerSet() {
		return this.utpTickerSet;
	}

	public ConcurrentMap<String, String> getCtaaTickerSet() {
		return this.ctaaTickerSet;
	}

	public ConcurrentMap<String, String> getCtabTickerSet() {
		return this.ctabTickerSet;
	}

	public int getUtpTradeProtocol() {
		return this.utpTradeProtocol;
	}

	public int getUtpQuoteProtocol() {
		return this.utpQuoteProtocol;
	}

	public int getCtaaTradeProtocol() {
		return this.ctaaTradeProtocol;
	}

	public int getCtaaQuoteProtocol() {
		return this.ctaaQuoteProtocol;
	}

	public int getCtabTradeProtocol() {
		return this.ctabTradeProtocol;
	}

	public int getCtabQuoteProtocol() {
		return this.ctabQuoteProtocol;
	}
	
	public String getTierMapping(String tierId) {
		return this.tierMappingMap.getOrDefault(tierId, tierId);
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
		if ((serviceCode == this.utpTradeProtocol && this.utpTickerSet.get(ticker) != null)
				|| (serviceCode == this.ctaaTradeProtocol && this.ctaaTickerSet.get(ticker) != null)
				|| (serviceCode == this.ctabTradeProtocol && this.ctabTickerSet.get(ticker) != null)) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "trade");
			map.put("data", this.cachedTradeData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
			return resultMap;
		}
		if ((serviceCode == this.utpQuoteProtocol && this.utpTickerSet.get(ticker) != null)
				|| (serviceCode == this.ctaaQuoteProtocol && this.ctaaTickerSet.get(ticker) != null)
				|| (serviceCode == this.ctabQuoteProtocol && this.ctabTickerSet.get(ticker) != null)) {
			resultMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "quote");
			map.put("data", this.cachedQuoteData.getOrDefault(requestTicker, defaultMap));
			resultMap.put("tickerData", map);
			return resultMap;
		}
		return resultMap;
	}

	@Override
	public Object getDataByService(final String dataType) {
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType, e);
			return "";
		}
		if (serviceCode == this.utpTradeProtocol) {
			return this.utpTradeData;
		} else if (serviceCode == this.utpQuoteProtocol) {
			return this.utpQuoteData;
		} else if (serviceCode == this.ctaaTradeProtocol) {
			return this.ctaaTradeData;
		} else if (serviceCode == this.ctaaQuoteProtocol) {
			return this.ctaaQuoteData;
		} else if (serviceCode == this.ctabTradeProtocol) {
			return this.ctabTradeData;
		} else if (serviceCode == this.ctabQuoteProtocol) {
			return this.ctabQuoteData;
		}
		return "";
	}

	@Override
	public void loadMarketDataAsString() {
		Set<Map<String, Object>> utpTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> utpQuoteresultSet = new HashSet<>();
		Set<Map<String, Object>> ctaaTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> ctaaQuoteresultSet = new HashSet<>();
		Set<Map<String, Object>> ctabTraderesultSet = new HashSet<>();
		Set<Map<String, Object>> ctabQuoteresultSet = new HashSet<>();
		try {
			this.utpTickerSet.entrySet().forEach(entry -> {
				String ticker = entry.getKey();
				Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.utpTradeProtocol);
				if (tradeMap != null) {
					utpTraderesultSet.add(tradeMap);
				}
				Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.utpQuoteProtocol);
				if (quoteMap != null) {
					utpQuoteresultSet.add(quoteMap);
				}
			});
			this.utpTradeData = gson.toJson(utpTraderesultSet).getBytes();
			this.utpQuoteData = gson.toJson(utpQuoteresultSet).getBytes();

			this.ctaaTickerSet.entrySet().forEach(entry -> {
				String ticker = entry.getKey();
				Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.ctaaTradeProtocol);
				if (tradeMap != null) {
					ctaaTraderesultSet.add(tradeMap);
				}
				Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.ctaaQuoteProtocol);
				if (quoteMap != null) {
					ctaaQuoteresultSet.add(quoteMap);
				}
			});
			this.ctaaTradeData = gson.toJson(ctaaTraderesultSet).getBytes();
			this.ctaaQuoteData = gson.toJson(ctaaQuoteresultSet).getBytes();

			this.ctabTickerSet.entrySet().forEach(entry -> {
				String ticker = entry.getKey();
				Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.ctabTradeProtocol);
				if (tradeMap != null) {
					ctabTraderesultSet.add(tradeMap);
				}
				Map<String, Object> quoteMap = this.cachedQuoteData.get(ticker + "|" + this.ctabQuoteProtocol);
				if (quoteMap != null) {
					ctabQuoteresultSet.add(quoteMap);
				}
			});
			this.ctabTradeData = gson.toJson(ctabTraderesultSet).getBytes();
			this.ctabQuoteData = gson.toJson(ctabQuoteresultSet).getBytes();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public Object getDataList(final Map<String, Set<String>> tickerMap) {
		ArrayList<Map<String, Object>> snapList = new ArrayList<>();
		if (tickerMap == null) {
			return snapList;
		}
		tickerMap.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Set<String> protocolSet = entry.getValue();
			if (ticker == null || protocolSet == null) {
				return;
			}
			Map<String, Object> snapMap = new HashMap<>();
			for (String protocol : protocolSet) {
				int serviceCode = 0;
				try {
					serviceCode = Integer.parseInt(protocol);
					int streamingProtocol = this.snapStreamingProtocolMap.get(serviceCode);
					if (this.cachedTradeData.get(ticker + "|" + streamingProtocol) != null) {
						snapMap.putAll(this.cachedTradeData.get(ticker + "|" + streamingProtocol));
					}
					if (this.cachedQuoteData.get(ticker + "|" + streamingProtocol) != null) {
						snapMap.putAll(this.cachedQuoteData.get(ticker + "|" + streamingProtocol));
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "exception" + protocol + " " + ticker, e);
				}
			}
			if (snapMap.size() > 0) {
				snapList.add(snapMap);
			}
		});
		return snapList;
	}

	public int getUtpSnapTradeProtocol() {
		return this.utpSnapTradeProtocol;
	}

	public int getUtpSnapQuoteProtocol() {
		return this.utpSnapQuoteProtocol;
	}

	public int getCtaaSnapTradeProtocol() {
		return this.ctaaSnapTradeProtocol;
	}

	public int getCtaaSnapQuoteProtocol() {
		return this.ctaaSnapQuoteProtocol;
	}

	public int getCtabSnapTradeProtocol() {
		return this.ctabSnapTradeProtocol;
	}

	public int getCtabSnapQuoteProtocol() {
		return this.ctabSnapQuoteProtocol;
	}

	public String getCtaaTapeset() {
		return this.ctaaTapeset;
	}

	public String getCtabTapeset() {
		return this.ctabTapeset;
	}

	public String getUtpTapeset() {
		return this.utpTapeset;
	}

	public int getTradeServiceCode(final String ticker) {
		if (getUtpTickerSet().get(ticker) != null) {
			return getUtpTradeProtocol();
		} else if (getCtaaTickerSet().get(ticker) != null) {
			return getCtaaTradeProtocol();
		} else if (getCtabTickerSet().get(ticker) != null) {
			return getCtabTradeProtocol();
		}
		return 0;
	}

	public int getQuoteServiceCode(final String ticker) {
		if (getUtpTickerSet().containsKey(ticker)) {
			return getUtpQuoteProtocol();
		} else if (getCtaaTickerSet().containsKey(ticker)) {
			return getCtaaQuoteProtocol();
		} else if (getCtabTickerSet().containsKey(ticker)) {
			return getCtabQuoteProtocol();
		}
		return 0;
	}

	public ConcurrentMap<String, String> getMarketTierSet() {
		return this.marketTierSet;
	}

}