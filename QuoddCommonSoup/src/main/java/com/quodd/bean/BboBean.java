package com.quodd.bean;

import java.util.Map;

public class BboBean extends BaseBean {
	public static final char BBO_TYPE_COMPLETE = 'C';
	public static final char BBO_TYPE_BID = 'B';
	public static final char BBO_TYPE_ASK = 'A';
	private long locateCode;
	private int bidMarketCenterLocate;
	private long bidPrice;
	private int bidSize;
	private int bidDenominator;
	private int askMarketCenterLocate;
	private long askPrice;
	private int askSize;
	private int askDenominator;
	private int condition;
	private int flag;
	private Map<Integer, String> appendageMap;
	private char bboType = BBO_TYPE_COMPLETE;

	public long getLocateCode() {
		return this.locateCode;
	}

	public void setLocateCode(long locateCode) {
		this.locateCode = locateCode;
	}

	public int getBidMarketCenterLocate() {
		return this.bidMarketCenterLocate;
	}

	public void setBidMarketCenterLocate(int bidMarketCenterLocate) {
		this.bidMarketCenterLocate = bidMarketCenterLocate;
	}

	public long getBidPrice() {
		return this.bidPrice;
	}

	public void setBidPrice(long bidPrice) {
		this.bidPrice = bidPrice;
	}

	public int getBidSize() {
		return this.bidSize;
	}

	public void setBidSize(int bidSize) {
		this.bidSize = bidSize;
	}

	public long getAskPrice() {
		return this.askPrice;
	}

	public void setAskPrice(long askPrice) {
		this.askPrice = askPrice;
	}

	public int getAskSize() {
		return this.askSize;
	}

	public void setAskSize(int askSize) {
		this.askSize = askSize;
	}

	public int getCondition() {
		return this.condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public int getFlag() {
		return this.flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getAskMarketCenterLocate() {
		return this.askMarketCenterLocate;
	}

	public void setAskMarketCenterLocate(int askMarketCenterLocate) {
		this.askMarketCenterLocate = askMarketCenterLocate;
	}

	public int getBidDenominator() {
		return this.bidDenominator;
	}

	public void setBidDenominator(int bidDenominator) {
		this.bidDenominator = bidDenominator;
	}

	public int getAskDenominator() {
		return this.askDenominator;
	}

	public void setAskDenominator(int askDenominator) {
		this.askDenominator = askDenominator;
	}

	public char getBboType() {
		return this.bboType;
	}

	public void setBboType(char bboType) {
		this.bboType = bboType;
	}

	public Map<Integer, String> getAppendageMap() {
		return appendageMap;
	}

	public void setAppendageMap(Map<Integer, String> appendageMap) {
		this.appendageMap = appendageMap;
	}
}
