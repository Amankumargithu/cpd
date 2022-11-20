package com.quodd.bond;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.gson;
import static com.quodd.common.cpd.CPD.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.quodd.common.cpd.collection.CPDCache;
import com.quodd.common.cpd.collection.MappedMessageQueue;

public class BondsCache extends CPDCache {

	private int tradeProtocol = 0;
	private int quoteProtocol = 0;
	private int snapTradeProtocol = 0;
	private int snapQuoteProtocol = 0;
	private MappedMessageQueue bondWriterQueue = new MappedMessageQueue();
	private byte[] bondTradeData;
	private byte[] bondQuoteData;
	private final Map<String, Object> defaultMap = new HashMap<>();

	public BondsCache() {
		super();
		this.tradeProtocol = cpdProperties.getIntProperty("TRADE_PROTOCOL", 0);
		this.quoteProtocol = cpdProperties.getIntProperty("QUOTE_PROTOCOL", 0);
		this.snapTradeProtocol = cpdProperties.getIntProperty("SNAP_TRADE_PROTOCOL", 0);
		this.snapQuoteProtocol = cpdProperties.getIntProperty("SNAP_QUOTE_PROTOCOL", 0);
	}

	@Override
	public Map<String, Object> getData(String ticker, String dataType) {
		String requestTicker = ticker + "|" + dataType;
		Map<String, Object> hashMap = new HashMap<>();
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType);
		}
		if (serviceCode == this.tradeProtocol && getMetaTickerSet().contains(ticker)) {
			hashMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "trade");
			map.put("data", this.cachedTradeData.getOrDefault(requestTicker, defaultMap));
			hashMap.put("tickerData", map);
			return hashMap;
		}
		if (serviceCode == this.quoteProtocol && getMetaTickerSet().contains(ticker)) {
			hashMap.put("ticker", requestTicker);
			Map<String, Object> map = new HashMap<>();
			map.put("event", "quote");
			map.put("data", this.cachedQuoteData.getOrDefault(requestTicker, defaultMap));
			hashMap.put("tickerData", map);
			return hashMap;
		}
		return hashMap;
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
					try {
						if (protocol.equalsIgnoreCase(getSnapTradeProtocol() + "")) {
							if (this.cachedTradeData.get(ticker + "|" + getTradeProtocol()) != null)
								snapMap.putAll(this.cachedTradeData.get(ticker + "|" + getTradeProtocol()));
						} else if (protocol.equalsIgnoreCase(getSnapQuoteProtocol() + "")) {
							if (this.cachedQuoteData.get(ticker + "|" + getQuoteProtocol()) != null)
								snapMap.putAll(this.cachedQuoteData.get(ticker + "|" + getQuoteProtocol()));
						}
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
	public Object getDataByService(String dataType) {
		int serviceCode = 0;
		try {
			serviceCode = Integer.parseInt(dataType);
		} catch (Exception e) {
			logger.log(Level.WARNING, "dataType parameter is not integer " + dataType);
			return "";
		}
		if (serviceCode == this.tradeProtocol)
			return this.bondTradeData;
		else if (serviceCode == this.quoteProtocol)
			return this.bondQuoteData;
		return "";
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

	public MappedMessageQueue getBondWriterQueue() {
		return this.bondWriterQueue;
	}

	public void setBondWriterQueue(MappedMessageQueue bondWriterQueue) {
		this.bondWriterQueue = bondWriterQueue;
	}

	@Override
	public void loadMarketDataAsString() {
		try {
			this.bondTradeData = (gson.toJson(this.cachedTradeData.values())).getBytes();
			this.bondQuoteData = (gson.toJson(this.cachedQuoteData.values())).getBytes();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}