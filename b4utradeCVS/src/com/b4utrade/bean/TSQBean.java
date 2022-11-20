/**
 * TSQBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2006.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


/**
 *   The object for wrapping Time Sales and Quote data
 */
public class TSQBean extends DefaultObject
{
   public final static Short CANCELLED_TRADE = new Short("0");
   public final static Short NON_CANCELLED_TRADE = new Short("1");

   public final static Short TYPE_TRADE = new Short("1");
   public final static Short TYPE_COMPOSITE_QUOTE = new Short("2");
   public final static Short TYPE_REGIONAL_QUOTE = new Short("3");
   public final static Short TYPE_QUOTE = new Short("4");
   public final static Short TYPE_MARKET_MAKER = new Short("5");
   
   public final static Short INCLUDE_IN_VWAP = new Short("1");
   public final static Short EXCLUDE_IN_VWAP = new Short("0");
   
   

   private String ticker;
   private Long msgSequence;
   private Long tradeSequence;
   private Short tradeCancelIndicator;
   private Short messageType;
   private String tradeQuoteCondCode1;
   private String tradeQuoteCondCode2;
   private String tradeQuoteCondCode3;
   private String tradeQuoteCondCode4;
   private Integer tradeQuoteTime;
   private Double tradePrice;
   private Long tradeSize;
   private Double bidPrice;
   private Double askPrice;
   private Long bidSize;
   private Long askSize;
   private String exchangeId;
   private String tradeMarketCenter;
   private String bidMarketCenter;
   private String askMarketCenter;
   private java.sql.Timestamp creationDateTime;
   private java.sql.Timestamp updateDateTime;
   private Double vwap;
   private Double computedVwap;
   private Long totalVolume;
   private Long filteredTotalVolume;
   
   private String marketMakerId;
   private Double underlyingBidPrice;
   private Double underlyingAskPrice;
   private Long underlyingBidSize;
   private Long underlyingAskSize;
   private String underlyingBidExchnage;
   private String underlyingAskExchange;
   
   private byte[] bytes;
   

   private TSQBean child;

   public TSQBean() {}

   public void setTicker(String ticker) {
      this.ticker = ticker;
   }

   public void setMsgSequence(Long msgSequence) {
      this.msgSequence = msgSequence;
   }

   public void setTradeSequence(Long tradeSequence) {
      this.tradeSequence = tradeSequence;
   }

   public void setTradeCancelIndicator(Short tradeCancelIndicator) {
      this.tradeCancelIndicator = tradeCancelIndicator;
   }

   public void setMessageType(Short messageType) {
      this.messageType = messageType;
   }

   public void setTradeQuoteCondCode1(String tradeQuoteCondCode1) {
      this.tradeQuoteCondCode1 = tradeQuoteCondCode1;
   }

   public void setTradeQuoteCondCode2(String tradeQuoteCondCode2) {
      this.tradeQuoteCondCode2 = tradeQuoteCondCode2;
   }

   public void setTradeQuoteCondCode3(String tradeQuoteCondCode3) {
      this.tradeQuoteCondCode3 = tradeQuoteCondCode3;
   }

   public void setTradeQuoteCondCode4(String tradeQuoteCondCode4) {
      this.tradeQuoteCondCode4 = tradeQuoteCondCode4;
   }

   public void setTradeQuoteTime(Integer tradeQuoteTime) {
      this.tradeQuoteTime = tradeQuoteTime;
   }

   public void setTradePrice(Double tradePrice) {
      this.tradePrice = tradePrice;
   }

   public void setTradeSize(Long tradeSize) {
      this.tradeSize = tradeSize;
   }

   public void setBidPrice(Double bidPrice) {
      this.bidPrice = bidPrice;
   }

   public void setAskPrice(Double askPrice) {
      this.askPrice = askPrice;
   }

   public void setBidSize(Long bidSize) {
      this.bidSize = bidSize;
   }

   public void setAskSize(Long askSize) {
      this.askSize = askSize;
   }

   public void setExchangeId(String exchangeId) {
      this.exchangeId = exchangeId;
   }

   public void setTradeMarketCenter(String tradeMarketCenter) {
      this.tradeMarketCenter = tradeMarketCenter;
   }

   public void setBidMarketCenter(String bidMarketCenter) {
      this.bidMarketCenter = bidMarketCenter;
   }

   public void setAskMarketCenter(String askMarketCenter) {
      this.askMarketCenter = askMarketCenter;
   }

   public void setCreationDateTime(java.sql.Timestamp creationDateTime) {
      this.creationDateTime = creationDateTime;
   }

   public void setUpdateDateTime(java.sql.Timestamp updateDateTime) {
      this.updateDateTime = updateDateTime;
   }

   public void setVwap(Double vwap) {
      this.vwap = vwap;
   }

   public void setChild(TSQBean child) {
      this.child = child;
   }

   public String getTicker() {
      return this.ticker;
   }

   public Long getMsgSequence() {
      return this.msgSequence;
   }

   public Long getTradeSequence() {
      return this.tradeSequence;
   }

   public Short getTradeCancelIndicator() {
      return this.tradeCancelIndicator;
   }

   public Short getMessageType() {
      return this.messageType;
   }

   public String getTradeQuoteCondCode1() {
      return this.tradeQuoteCondCode1;
   }

   public String getTradeQuoteCondCode2() {
      return this.tradeQuoteCondCode2;
   }

   public String getTradeQuoteCondCode3() {
      return this.tradeQuoteCondCode3;
   }

   public String getTradeQuoteCondCode4() {
      return this.tradeQuoteCondCode4;
   }

   public Integer getTradeQuoteTime() {
      return this.tradeQuoteTime;
   }

   public Double getTradePrice() {
      return this.tradePrice;
   }

   public Long getTradeSize() {
      return this.tradeSize;
   }

   public Double getBidPrice() {
      return this.bidPrice;
   }

   public Double getAskPrice() {
      return this.askPrice;
   }

   public Double setAskPrice() {
      return this.askPrice;
   }

   public Long getBidSize() {
      return this.bidSize;
   }

   public Long getAskSize() {
      return this.askSize;
   }

   public String getExchangeId() {
      return this.exchangeId;
   }

   public String getTradeMarketCenter() {
      return this.tradeMarketCenter;
   }

   public String getBidMarketCenter() {
      return this.bidMarketCenter;
   }

   public String getAskMarketCenter() {
      return this.askMarketCenter;
   }

   public java.sql.Timestamp getCreationDateTime() {
      return this.creationDateTime;
   }

   public java.sql.Timestamp getUpdateDateTime() {
      return this.updateDateTime;
   }

   public Double getVwap() {
	  return this.vwap;
   }
   public TSQBean getChild() {
	  return this.child;
   }

/**
 * @return Returns the computedVwap.
 */
public Double getComputedVwap() {
	return computedVwap;
}

/**
 * @param computedVwap The computedVwap to set.
 */
public void setComputedVwap(Double computedVwap) {
	this.computedVwap = computedVwap;
}

/**
 * @return Returns the bytes.
 */
public byte[] getBytes() {
	return bytes;
}

/**
 * @param bytes The bytes to set.
 */
public void setBytes(byte[] bytes) {
	this.bytes = bytes;
}

/**
 * @return Returns the totalVolume.
 */
public Long getTotalVolume() {
	return totalVolume;
}

/**
 * @param totalVolume The totalVolume to set.
 */
public void setTotalVolume(Long totalVolume) {
	this.totalVolume = totalVolume;
}

/**
 * @return Returns the filteredTotalVolume.
 */
public Long getFilteredTotalVolume() {
	return filteredTotalVolume;
}

/**
 * @param filteredTotalVolume The filteredTotalVolume to set.
 */
public void setFilteredTotalVolume(Long filteredTotalVolume) {
	this.filteredTotalVolume = filteredTotalVolume;
}

public String getMarketMakerId() {
	return marketMakerId;
}

public void setMarketMakerId(String marketMakerId) {
	this.marketMakerId = marketMakerId;
}

public Double getUnderlyingBidPrice() {
	return underlyingBidPrice;
}

public void setUnderlyingBidPrice(Double underlyingBidPrice) {
	this.underlyingBidPrice = underlyingBidPrice;
}

public Double getUnderlyingAskPrice() {
	return underlyingAskPrice;
}

public void setUnderlyingAskPrice(Double underlyingAskPrice) {
	this.underlyingAskPrice = underlyingAskPrice;
}

public Long getUnderlyingBidSize() {
	return underlyingBidSize;
}

public void setUnderlyingBidSize(Long underlyingBidSize) {
	this.underlyingBidSize = underlyingBidSize;
}

public Long getUnderlyingAskSize() {
	return underlyingAskSize;
}

public void setUnderlyingAskSize(Long underlyingAskSize) {
	this.underlyingAskSize = underlyingAskSize;
}

public String getUnderlyingBidExchnage() {
	return underlyingBidExchnage;
}

public void setUnderlyingBidExchnage(String underlyingBidExchnage) {
	this.underlyingBidExchnage = underlyingBidExchnage;
}

public String getUnderlyingAskExchange() {
	return underlyingAskExchange;
}

public void setUnderlyingAskExchange(String underlyingAskExchange) {
	this.underlyingAskExchange = underlyingAskExchange;
}
}
