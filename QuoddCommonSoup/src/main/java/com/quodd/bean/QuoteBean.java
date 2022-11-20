package com.quodd.bean;

public class QuoteBean extends BaseBean {
	public static final char QUOTE_TYPE_COMPLETE = 'C';
	public static final char QUOTE_TYPE_BID = 'B';
	public static final char QUOTE_TYPE_ASK = 'A';
	private long locateCode;
	private int marketCenterLocate;
	private int marketMakerLocate;
	private long bidPrice;
	private int bidSize;
	private int bidDenominator;
	private long askPrice;
	private int askSize;
	private int askDenominator;
	private int condition;
	private int quoteFlag;
	private int bidFlag;
	private int askFlag;
//	private Map appendMap;
	private char quoteType = QUOTE_TYPE_COMPLETE;

	public long getLocateCode() {
		return this.locateCode;
	}

	public void setLocateCode(long locateCode) {
		this.locateCode = locateCode;
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

	public int getMarketCenterLocate() {
		return this.marketCenterLocate;
	}

	public void setMarketCenterLocate(int marketCenterLocate) {
		this.marketCenterLocate = marketCenterLocate;
	}

	public int getQuoteFlag() {
		return this.quoteFlag;
	}

	public void setQuoteFlag(int quoteFlag) {
		this.quoteFlag = quoteFlag;
	}

	public int getBidFlag() {
		return this.bidFlag;
	}

	public void setBidFlag(int bidFlag) {
		this.bidFlag = bidFlag;
	}

	public int getAskFlag() {
		return this.askFlag;
	}

	public void setAskFlag(int askFlag) {
		this.askFlag = askFlag;
	}

	public char getQuoteType() {
		return this.quoteType;
	}

	public void setQuoteType(char quoteType) {
		this.quoteType = quoteType;
	}

	public int getMarketMakerLocate() {
		return marketMakerLocate;
	}

	public void setMarketMakerLocate(int marketMakerLocate) {
		this.marketMakerLocate = marketMakerLocate;
	}

}
