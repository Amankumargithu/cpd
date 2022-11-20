package com.quodd.bean;

import java.util.Map;

public class TradeUpdateBean extends BaseBean {

	public static final int TRADE_UPDATE_TYPE_CANCEL = 1;
	public static final int TRADE_UPDATE_TYPE_CORRECTION = 2;
	public static final int TRADE_UPDATE_TYPE_PRIOR_DAY_TRADE = 3;
	public static final int TRADE_UPDATE_TYPE_PRIOR_DAY_CANCEL = 4;
	public static final int TRADE_UPDATE_TYPE_PRIOR_DAY_CORRECTION = 5;

	private int tradeUpdateType;
	private long locateCode;
	private int tradeMarketCenterLocate;
	private long originalTradeId;
	private long originalTradePrice;
	private long originalTradeSize;
	private int originalTradeDenominator;
	private int originalPriceFlag;
	private int originalEligibilityFlag;
	private int originalReportFlag;
	private int cancelFlag;

	private long correctedTradeId;
	private long correctedTradePrice;
	private long correctedTradeSize;
	private int correctedTradeDenominator;
	private int correctedPriceFlag;
	private int correctedEligibilityFlag;
	private int correctedReportFlag;
	private int correctionFlag;

	private int priorTradeMonth;
	private int priorTradeDay;
	private int priorTradeYear;
	private long priorTradeTimestamp;

	private int priorCorrectedTradeMonth;
	private int priorCorrectedTradeDay;
	private int priorCorrectedTradeYear;
	private long priorCorrectedTradeTimestamp;
	private Map<Integer, String> appendageMap;
	
	public TradeUpdateBean(int tradeUpdateType) {
		this.tradeUpdateType = tradeUpdateType;
	}

	public int getTradeUpdateType() {
		return this.tradeUpdateType;
	}

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

	public long getOriginalTradeId() {
		return this.originalTradeId;
	}

	public void setOriginalTradeId(long originalTradeId) {
		this.originalTradeId = originalTradeId;
	}

	public long getOriginalTradePrice() {
		return this.originalTradePrice;
	}

	public void setOriginalTradePrice(long originalTradePrice) {
		this.originalTradePrice = originalTradePrice;
	}

	public long getOriginalTradeSize() {
		return this.originalTradeSize;
	}

	public void setOriginalTradeSize(long originalTradeSize) {
		this.originalTradeSize = originalTradeSize;
	}

	public int getOriginalTradeDenominator() {
		return this.originalTradeDenominator;
	}

	public void setOriginalTradeDenominator(int originalTradeDenominator) {
		this.originalTradeDenominator = originalTradeDenominator;
	}

	public int getOriginalPriceFlag() {
		return this.originalPriceFlag;
	}

	public void setOriginalPriceFlag(int originalPriceFlag) {
		this.originalPriceFlag = originalPriceFlag;
	}

	public int getOriginalEligibilityFlag() {
		return this.originalEligibilityFlag;
	}

	public void setOriginalEligibilityFlag(int originalEligibilityFlag) {
		this.originalEligibilityFlag = originalEligibilityFlag;
	}

	public int getOriginalReportFlag() {
		return this.originalReportFlag;
	}

	public void setOriginalReportFlag(int originalReportFlag) {
		this.originalReportFlag = originalReportFlag;
	}

	public int getCancelFlag() {
		return this.cancelFlag;
	}

	public void setCancelFlag(int cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	public long getCorrectedTradeId() {
		return this.correctedTradeId;
	}

	public void setCorrectedTradeId(long correctedTradeId) {
		this.correctedTradeId = correctedTradeId;
	}

	public long getCorrectedTradePrice() {
		return this.correctedTradePrice;
	}

	public void setCorrectedTradePrice(long correctedTradePrice) {
		this.correctedTradePrice = correctedTradePrice;
	}

	public long getCorrectedTradeSize() {
		return this.correctedTradeSize;
	}

	public void setCorrectedTradeSize(long correctedTradeSize) {
		this.correctedTradeSize = correctedTradeSize;
	}

	public int getCorrectedTradeDenominator() {
		return this.correctedTradeDenominator;
	}

	public void setCorrectedTradeDenominator(int correctedTradeDenominator) {
		this.correctedTradeDenominator = correctedTradeDenominator;
	}

	public int getCorrectedPriceFlag() {
		return this.correctedPriceFlag;
	}

	public void setCorrectedPriceFlag(int correctedPriceFlag) {
		this.correctedPriceFlag = correctedPriceFlag;
	}

	public int getCorrectedEligibilityFlag() {
		return this.correctedEligibilityFlag;
	}

	public void setCorrectedEligibilityFlag(int correctedEligibilityFlag) {
		this.correctedEligibilityFlag = correctedEligibilityFlag;
	}

	public int getCorrectedReportFlag() {
		return this.correctedReportFlag;
	}

	public void setCorrectedReportFlag(int correctedReportFlag) {
		this.correctedReportFlag = correctedReportFlag;
	}

	public int getCorrectionFlag() {
		return this.correctionFlag;
	}

	public void setCorrectionFlag(int correctionFlag) {
		this.correctionFlag = correctionFlag;
	}

	public int getPriorTradeMonth() {
		return this.priorTradeMonth;
	}

	public void setPriorTradeMonth(int priorTradeMonth) {
		this.priorTradeMonth = priorTradeMonth;
	}

	public int getPriorTradeDay() {
		return this.priorTradeDay;
	}

	public void setPriorTradeDay(int priorTradeDay) {
		this.priorTradeDay = priorTradeDay;
	}

	public int getPriorTradeYear() {
		return this.priorTradeYear;
	}

	public void setPriorTradeYear(int priorTradeYear) {
		this.priorTradeYear = priorTradeYear;
	}

	public long getPriorTradeTimestamp() {
		return this.priorTradeTimestamp;
	}

	public void setPriorTradeTimestamp(long priorTradeTimestamp) {
		this.priorTradeTimestamp = priorTradeTimestamp;
	}

	public int getPriorCorrectedTradeMonth() {
		return this.priorCorrectedTradeMonth;
	}

	public void setPriorCorrectedTradeMonth(int priorCorrectedTradeMonth) {
		this.priorCorrectedTradeMonth = priorCorrectedTradeMonth;
	}

	public int getPriorCorrectedTradeDay() {
		return this.priorCorrectedTradeDay;
	}

	public void setPriorCorrectedTradeDay(int priorCorrectedTradeDay) {
		this.priorCorrectedTradeDay = priorCorrectedTradeDay;
	}

	public int getPriorCorrectedTradeYear() {
		return this.priorCorrectedTradeYear;
	}

	public void setPriorCorrectedTradeYear(int priorCorrectedTradeYear) {
		this.priorCorrectedTradeYear = priorCorrectedTradeYear;
	}

	public long getPriorCorrectedTradeTimestamp() {
		return this.priorCorrectedTradeTimestamp;
	}

	public void setPriorCorrectedTradeTimestamp(long priorCorrectedTradeTimestamp) {
		this.priorCorrectedTradeTimestamp = priorCorrectedTradeTimestamp;
	}

	public Map<Integer, String> getAppendageMap() {
		return appendageMap;
	}

	public void setAppendageMap(Map<Integer, String> appendageMap) {
		this.appendageMap = appendageMap;
	}
}
