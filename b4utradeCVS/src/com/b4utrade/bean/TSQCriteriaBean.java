/**
 * TimeAndSalesCriteriaBean.java
 *
 * @author Copyright (c) 2006 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */
package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;

public class TSQCriteriaBean extends DefaultObject {

	public final static String TSQ = "T&S&Q";
	public final static String TS = "T&S";
	public final static String TQ = "T&Q";
	
	public final static String TSQ_NASD_BASIC = "T&S&Q_NASD_BASIC";
	public final static String TS_NASD_BASIC = "T&S_NASD_BASIC";
	public final static String TQ_NASD_BASIC = "T&Q_NASD_BASIC";	
	
	public final static String TSV = "T&S&V";
	
	public final static String QUOTE_PRICE_FILTER_TYPE_BID = "BID";
	public final static String QUOTE_PRICE_FILTER_TYPE_ASK = "ASK";
	public final static String QUOTE_PRICE_FILTER_TYPE_BID_AND_ASK = "BID&ASK";
	
	public final static String FROM_MSG_TYPE_TRADE = "1";
	public final static String FROM_MSG_TYPE_REGIONAL_QUOTE = "8";
	public final static String FROM_MSG_TYPE_COMPOSITE_QUOTE = "13";

	private Integer day;

	private String symbol;

	private String display;

	private String exchange;

	private String filterTime;

	private String fromTime;

	private String toTime;

	private String filterSize;

	private String fromSize;

	private String toSize;

	private String filterPrice;

	private String fromPrice;

	private String toPrice;
	
	private String quoteFilterPrice;

	private String quoteFromPrice;

	private String quoteToPrice;
	
	private String quotePriceFilterType;
	
	private boolean bboOnly = false;

	private Integer pageDirection;
	
	private Long fromMessageSequence;
	
	private String fromMessageSequenceTime;
	
	private String fromMessageType;
	
	private String marketMakerId;
	

	public final static Integer NO_PAGING = new Integer(0);

	public final static Integer PAGE_FORWARD = new Integer(1);

	public final static Integer PAGE_BACKWARD = new Integer(-1);

	public TSQCriteriaBean() {
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getFilterPrice() {
		return filterPrice;
	}

	public void setFilterPrice(String filterPrice) {
		this.filterPrice = filterPrice;
	}

	public String getFilterSize() {
		return filterSize;
	}

	public void setFilterSize(String filterSize) {
		this.filterSize = filterSize;
	}

	public String getFilterTime() {
		return filterTime;
	}

	public void setFilterTime(String filterTime) {
		this.filterTime = filterTime;
	}

	public String getFromPrice() {
		return fromPrice;
	}

	public void setFromPrice(String fromPrice) {
		this.fromPrice = fromPrice;
	}

	public String getFromSize() {
		return fromSize;
	}

	public void setFromSize(String fromSize) {
		this.fromSize = fromSize;
	}

	public String getFromTime() {
		return fromTime;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getToPrice() {
		return toPrice;
	}

	public void setToPrice(String toPrice) {
		this.toPrice = toPrice;
	}

	public String getToSize() {
		return toSize;
	}

	public void setToSize(String toSize) {
		this.toSize = toSize;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	/**
	 * @return Returns the pageDirection.
	 */
	public Integer getPageDirection() {
		return pageDirection;
	}

	/**
	 * @param pageDirection
	 *            The pageDirection to set.
	 */
	public void setPageDirection(Integer pageDirection) {
		this.pageDirection = pageDirection;
	}

	/**
	 * @return Returns the fromMessageSequence.
	 */
	public Long getFromMessageSequence() {
		return fromMessageSequence;
	}

	/**
	 * @param fromMessageSequence The fromMessageSequence to set.
	 */
	public void setFromMessageSequence(Long fromMessageSequence) {
		this.fromMessageSequence = fromMessageSequence;
	}

	/**
	 * @return Returns the fromMessageSequenceTime.
	 */
	public String getFromMessageSequenceTime() {
		return fromMessageSequenceTime;
	}

	/**
	 * @param fromMessageSequenceTime The fromMessageSequenceTime to set.
	 */
	public void setFromMessageSequenceTime(String fromMessageSequenceTime) {
		this.fromMessageSequenceTime = fromMessageSequenceTime;
	}

	public String getFromMessageType() {
		return fromMessageType;
	}

	public void setFromMessageType(String fromMessageType) {
		this.fromMessageType = fromMessageType;
	}

	public String getMarketMakerId() {
		return marketMakerId;
	}

	public void setMarketMakerId(String marketMakerId) {
		this.marketMakerId = marketMakerId;
	}

	public boolean isBboOnly() {
		return bboOnly;
	}

	public void setBboOnly(boolean bboOnly) {
		this.bboOnly = bboOnly;
	}

	public String getQuoteFilterPrice() {
		return quoteFilterPrice;
	}

	public void setQuoteFilterPrice(String quoteFilterPrice) {
		this.quoteFilterPrice = quoteFilterPrice;
	}

	public String getQuoteFromPrice() {
		return quoteFromPrice;
	}

	public void setQuoteFromPrice(String quoteFromPrice) {
		this.quoteFromPrice = quoteFromPrice;
	}

	public String getQuoteToPrice() {
		return quoteToPrice;
	}

	public void setQuoteToPrice(String quoteToPrice) {
		this.quoteToPrice = quoteToPrice;
	}

	public String getQuotePriceFilterType() {
		return quotePriceFilterType;
	}

	public void setQuotePriceFilterType(String quotePriceFilterType) {
		this.quotePriceFilterType = quotePriceFilterType;
	}




}
