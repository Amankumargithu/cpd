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

public class IndicesCache extends CPDCache {

	private final ConcurrentMap<String, String> djTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> ctaTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> gifTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> mdiTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> rsltckTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> spTickerSet = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> tsxTickerSet = new ConcurrentHashMap<>();
	private final int serviceCodeCta;
	private final int serviceCodeMdi;
	private final int serviceCodeDj;
	private final int serviceCodeGif;
	private final int serviceCodeRsltck;
	private final int serviceCodeSp;
	private final int serviceCodeTsx;
	private final int snapServiceCodeCta;
	private final int snapServiceCodeMdi;
	private final int snapServiceCodeDj;
	private final int snapServiceCodeGif;
	private final int snapServiceCodeRsltck;
	private final int snapServiceCodeSp;
	private final int snapServiceCodeTsx;
	private byte[] ctaTradeData;
	private byte[] mdiTradeData;
	private byte[] djTradeData;
	private byte[] gifTradeData;
	private byte[] rsltckTradeData;
	private byte[] spTradeData;
	private byte[] tsxTradeData;
	private final Map<Integer, Integer> snapStreamingProtocolMap = new HashMap<>();
	private final String ctaidxTapeset;
	private final String mdiTapeset;
	private final String djidxTapeset;
	private final String gifTapeset;
	private final String rsltckTapeset;
	private final String spidxTapeset;
	private final String tsxidxTapeset;
	private final Map<String, Object> defaultMap = new HashMap<>();

	public IndicesCache() {
		super();
		this.serviceCodeCta = cpdProperties.getIntProperty("CTA_SERVICE_PROTOCOL", 0);
		this.serviceCodeDj = cpdProperties.getIntProperty("DJ_SERVICE_PROTOCOL", 0);
		this.serviceCodeGif = cpdProperties.getIntProperty("GIF_SERVICE_PROTOCOL", 0);
		this.serviceCodeMdi = cpdProperties.getIntProperty("MDI_SERVICE_PROTOCOL", 0);
		this.serviceCodeRsltck = cpdProperties.getIntProperty("RSLTCK_SERVICE_PROTOCOL", 0);
		this.serviceCodeSp = cpdProperties.getIntProperty("SP_SERVICE_PROTOCOL", 0);
		this.serviceCodeTsx = cpdProperties.getIntProperty("TSX_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeCta = cpdProperties.getIntProperty("CTA_SNAP_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeDj = cpdProperties.getIntProperty("DJ_SNAP_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeGif = cpdProperties.getIntProperty("GIF_SNAP_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeMdi = cpdProperties.getIntProperty("MDI_SNAP_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeRsltck = cpdProperties.getIntProperty("RSLTCK_SNAP_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeSp = cpdProperties.getIntProperty("SP_SNAP_SERVICE_PROTOCOL", 0);
		this.snapServiceCodeTsx = cpdProperties.getIntProperty("TSX_SNAP_SERVICE_PROTOCOL", 0);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeCta, this.serviceCodeCta);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeDj, this.serviceCodeDj);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeGif, this.serviceCodeGif);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeMdi, this.serviceCodeMdi);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeRsltck, this.serviceCodeRsltck);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeSp, this.serviceCodeSp);
		this.snapStreamingProtocolMap.put(this.snapServiceCodeTsx, this.serviceCodeTsx);
		this.ctaidxTapeset = cpdProperties.getStringProperty("CTAIDX_TAPESET", "ctaidx");
		this.mdiTapeset = cpdProperties.getStringProperty("MDI_TAPESET", "mdi");
		this.djidxTapeset = cpdProperties.getStringProperty("DJIDX_TAPESET", "djidx");
		this.gifTapeset = cpdProperties.getStringProperty("GIF_TAPESET", "gif");
		this.rsltckTapeset = cpdProperties.getStringProperty("RSLTCK_TAPESET", "rsltck");
		this.spidxTapeset = cpdProperties.getStringProperty("SPIDX_TAPESET", "spidx");
		this.tsxidxTapeset = cpdProperties.getStringProperty("TSXIDX_TAPESET", "tsxidx");
	}

	@Override
	public void loadMarketDataAsString() {
		Set<Map<String, Object>> ctaResultSet = new HashSet<>();
		Set<Map<String, Object>> djResultSet = new HashSet<>();
		Set<Map<String, Object>> gifResultSet = new HashSet<>();
		Set<Map<String, Object>> mdiResultSet = new HashSet<>();
		Set<Map<String, Object>> rsltckResultSet = new HashSet<>();
		Set<Map<String, Object>> spResultSet = new HashSet<>();
		Set<Map<String, Object>> tsxResultSet = new HashSet<>();
		this.ctaTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeCta);
			if (tradeMap != null)
				ctaResultSet.add(tradeMap);
		});
		this.djTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeDj);
			if (tradeMap != null)
				djResultSet.add(tradeMap);
		});
		this.gifTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeGif);
			if (tradeMap != null)
				gifResultSet.add(tradeMap);
		});
		this.mdiTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeMdi);
			if (tradeMap != null)
				mdiResultSet.add(tradeMap);
		});
		this.rsltckTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeRsltck);
			if (tradeMap != null)
				rsltckResultSet.add(tradeMap);
		});
		this.spTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeSp);
			if (tradeMap != null)
				spResultSet.add(tradeMap);
		});
		this.tsxTickerSet.entrySet().forEach(entry -> {
			String ticker = entry.getKey();
			Map<String, Object> tradeMap = this.cachedTradeData.get(ticker + "|" + this.serviceCodeTsx);
			if (tradeMap != null)
				tsxResultSet.add(tradeMap);
		});
		try {
			this.ctaTradeData = (gson.toJson(ctaResultSet)).getBytes();
			this.mdiTradeData = (gson.toJson(mdiResultSet)).getBytes();
			this.djTradeData = (gson.toJson(djResultSet)).getBytes();
			this.gifTradeData = (gson.toJson(gifResultSet)).getBytes();
			this.rsltckTradeData = (gson.toJson(rsltckResultSet)).getBytes();
			this.spTradeData = (gson.toJson(spResultSet)).getBytes();
			this.tsxTradeData = (gson.toJson(tsxResultSet)).getBytes();
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
		if(serviceCode == this.serviceCodeCta && this.ctaTickerSet.get(ticker) != null
				|| serviceCode == this.serviceCodeDj && this.djTickerSet.get(ticker) != null
				|| serviceCode == this.serviceCodeGif && this.gifTickerSet.get(ticker) != null
				|| serviceCode == this.serviceCodeMdi && this.mdiTickerSet.get(ticker) != null
				|| serviceCode == this.serviceCodeRsltck && this.rsltckTickerSet.get(ticker) != null
				|| serviceCode == this.serviceCodeSp && this.spTickerSet.get(ticker) != null
				|| serviceCode == this.serviceCodeTsx && this.tsxTickerSet.get(ticker) != null
				)  {
				resultMap.put("ticker", requestTicker);
				Map<String, Object> map = new HashMap<>();
				map.put("event", "index");
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
		if (serviceCode == this.serviceCodeCta) {
			return this.ctaTradeData;
		} else if (serviceCode == this.serviceCodeDj) {
			return this.djTradeData;
		} else if (serviceCode == this.serviceCodeMdi) {
			return this.mdiTradeData;
		} else if (serviceCode == this.serviceCodeGif) {
			return this.gifTradeData;
		} else if (serviceCode == this.serviceCodeRsltck) {
			return this.rsltckTradeData;
		} else if (serviceCode == this.serviceCodeSp) {
			return this.spTradeData;
		} else if (serviceCode == this.serviceCodeTsx) {
			return this.tsxTradeData;
		}
		return "";
	}

	public ConcurrentMap<String, String> getDjTickerSet() {
		return this.djTickerSet;
	}

	public ConcurrentMap<String, String> getCtaTickerSet() {
		return this.ctaTickerSet;
	}

	public ConcurrentMap<String, String> getGifTickerSet() {
		return this.gifTickerSet;
	}

	public ConcurrentMap<String, String> getMdiTickerSet() {
		return this.mdiTickerSet;
	}

	public ConcurrentMap<String, String> getRsltckTickerSet() {
		return this.rsltckTickerSet;
	}

	public ConcurrentMap<String, String> getSpTickerSet() {
		return this.spTickerSet;
	}

	public ConcurrentMap<String, String> getTsxTickerSet() {
		return this.tsxTickerSet;
	}

	public int getServiceCodeCta() {
		return this.serviceCodeCta;
	}

	public int getServiceCodeMdi() {
		return this.serviceCodeMdi;
	}

	public int getServiceCodeDj() {
		return this.serviceCodeDj;
	}

	public int getServiceCodeGif() {
		return this.serviceCodeGif;
	}

	public int getServiceCodeRsltck() {
		return this.serviceCodeRsltck;
	}

	public int getServiceCodeSp() {
		return this.serviceCodeSp;
	}

	public int getServiceCodeTsx() {
		return this.serviceCodeTsx;
	}

	public int getSnapServiceCodeCta() {
		return this.snapServiceCodeCta;
	}

	public int getSnapServiceCodeMdi() {
		return this.snapServiceCodeMdi;
	}

	public int getSnapServiceCodeDj() {
		return this.snapServiceCodeDj;
	}

	public int getSnapServiceCodeGif() {
		return this.snapServiceCodeGif;
	}

	public int getSnapServiceCodeRsltck() {
		return this.snapServiceCodeRsltck;
	}

	public int getSnapServiceCodeSp() {
		return this.snapServiceCodeSp;
	}

	public int getSnapServiceCodeTsx() {
		return this.snapServiceCodeTsx;
	}

	public String getTapeset(int serviceCode) {
		if (serviceCode == this.serviceCodeCta)
			return this.ctaidxTapeset;
		if (serviceCode == this.serviceCodeMdi)
			return this.mdiTapeset;
		if (serviceCode == this.serviceCodeDj)
			return this.djidxTapeset;
		if (serviceCode == this.serviceCodeGif)
			return this.gifTapeset;
		if (serviceCode == this.serviceCodeRsltck)
			return this.rsltckTapeset;
		if (serviceCode == this.serviceCodeSp)
			return this.spidxTapeset;
		if (serviceCode == this.serviceCodeTsx)
			return this.tsxidxTapeset;
		return "";
	}

	public int getServiceCode(String ticker) {
		if (this.ctaTickerSet.get(ticker) != null)
			return this.serviceCodeCta;
		else if (this.djTickerSet.get(ticker) != null)
			return this.serviceCodeDj;
		else if (this.mdiTickerSet.get(ticker) != null)
			return this.serviceCodeMdi;
		else if (this.tsxTickerSet.get(ticker) != null)
			return this.serviceCodeTsx;
		else if (this.gifTickerSet.get(ticker) != null)
			return this.serviceCodeGif;
		else if (this.rsltckTickerSet.get(ticker) != null)
			return this.serviceCodeRsltck;
		else if (this.spTickerSet.get(ticker) != null)
			return this.serviceCodeSp;
		return 0;
	}
}