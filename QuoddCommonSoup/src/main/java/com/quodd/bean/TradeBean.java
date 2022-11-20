package com.quodd.bean;

import java.util.Map;

public class TradeBean extends BaseBean {
	private long locateCode;
	private int tradeMarketCenterLocate;
	private long tradeId;
	private long tradePrice;
	private long tradeSize;
	private int tradeDenominator;
	private int priceFlag;
	private int eligibilityFlag;
	private int reportFlag;
	private int changeFlag;
	private Map<Integer, String> appendageMap;

	public long getLocateCode() {
		return this.locateCode;
	}

	public void setLocateCode(long locateCode) {
		this.locateCode = locateCode;
	}

	public int getTradeMarketCenterLocate() {
		return this.tradeMarketCenterLocate;
	}

	public void setTradeMarketCenterLocate(int tradeMarketCenterLocate) {
		this.tradeMarketCenterLocate = tradeMarketCenterLocate;
	}

	public long getTradeId() {
		return this.tradeId;
	}

	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
	}

	public long getTradePrice() {
		return this.tradePrice;
	}

	public void setTradePrice(long tradePrice) {
		this.tradePrice = tradePrice;
	}

	public long getTradeSize() {
		return this.tradeSize;
	}

	public void setTradeSize(long tradeSize) {
		this.tradeSize = tradeSize;
	}

	public int getTradeDenominator() {
		return this.tradeDenominator;
	}

	public void setTradeDenominator(int tradeDenominator) {
		this.tradeDenominator = tradeDenominator;
	}

	public int getPriceFlag() {
		return this.priceFlag;
	}

	public void setPriceFlag(int priceFlag) {
		this.priceFlag = priceFlag;
	}

	public int getEligibilityFlag() {
		return this.eligibilityFlag;
	}

	public void setEligibilityFlag(int eligibilityFlag) {
		this.eligibilityFlag = eligibilityFlag;
	}

	public int getChangeFlag() {
		return this.changeFlag;
	}

	public void setChangeFlag(int changeFlag) {
		this.changeFlag = changeFlag;
	}

	public int getReportFlag() {
		return reportFlag;
	}

	public void setReportFlag(int reportFlag) {
		this.reportFlag = reportFlag;
	}

	public Map<Integer, String> getAppendageMap() {
		return appendageMap;
	}

	public void setAppendageMap(Map<Integer, String> appendageMap) {
		this.appendageMap = appendageMap;
	}

}
