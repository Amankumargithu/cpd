package ntp.bean;

import java.sql.Timestamp;

public class TSQBean
{
	public final static Short TYPE_TRADE = new Short("1");
	public final static Short TYPE_COMPOSITE_QUOTE = new Short("2");
	public final static Short TYPE_REGIONAL_QUOTE = new Short("3");

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
	private Timestamp creationDateTime;
	private Timestamp updateDateTime;
	private Double vwap;

	private byte[] bytes;
	private String TUPLE_SEP     = "|";
	private String FIELD_SEP     = ":";

	private String TICKER                  = "T";
	private String MESSAGE_SEQUENCE        = "MS";
	private String TRADE_SEQUENCE          = "TS";
	private String MESSAGE_TYPE            = "MT";
	private String TRADE_CANCEL_IND        = "X";
	private String TRADE_QUOTE_COND_CODE_1 = "C1";
	private String TRADE_QUOTE_COND_CODE_2 = "C2";
	private String TRADE_QUOTE_COND_CODE_3 = "C3";
	private String TRADE_QUOTE_COND_CODE_4 = "C4";
	private String TRADE_QUOTE_TIME        = "TQ";
	private String TRADE_PRICE             = "TP";
	private String TRADE_SIZE              = "TZ";
	private String BID_PRICE               = "BP";
	private String BID_SIZE                = "BZ";
	private String ASK_PRICE               = "AP";
	private String ASK_SIZE                = "AZ";
	private String EXCHANGE_ID             = "EI";
	private String TRADE_MARKET_CENTER     = "TC";
	private String BID_MARKET_CENTER       = "BC";
	private String ASK_MARKET_CENTER       = "AC";
	private String VWAP                    = "V";
	private String CREATION_DATE           = "CD";

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

	 public byte[] deflate()
	 {
		 try {
			 StringBuffer sb = new StringBuffer();
			 sb.append(TICKER);
			 sb.append(FIELD_SEP);
			 sb.append(getTicker());
			 if (getMsgSequence() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(MESSAGE_SEQUENCE);
				 sb.append(FIELD_SEP);
				 sb.append(getMsgSequence().toString());
			 }
			 if (getMessageType() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(MESSAGE_TYPE);
				 sb.append(FIELD_SEP);
				 sb.append(getMessageType().toString());
			 }
			 if (getTradeCancelIndicator() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_CANCEL_IND);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeCancelIndicator().toString());
			 }
			 if (getTradeSequence() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_SEQUENCE);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeSequence().toString());
			 }
			 if (getTradeQuoteCondCode1() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_QUOTE_COND_CODE_1);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeQuoteCondCode1());
			 }
			 if (getTradeQuoteCondCode2() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_QUOTE_COND_CODE_2);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeQuoteCondCode2());
			 }
			 if (getTradeQuoteCondCode3() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_QUOTE_COND_CODE_3);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeQuoteCondCode3());
			 }
			 if (getTradeQuoteCondCode4() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_QUOTE_COND_CODE_4);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeQuoteCondCode4());
			 }
			 if (getTradeQuoteTime() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_QUOTE_TIME);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeQuoteTime().toString());
			 }
			 if (getTradePrice() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_PRICE);
				 sb.append(FIELD_SEP);
				 sb.append(getTradePrice().toString());
			 }
			 if (getTradeSize() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_SIZE);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeSize().toString());
			 }
			 if (getBidPrice() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(BID_PRICE);
				 sb.append(FIELD_SEP);
				 sb.append(getBidPrice().toString());
			 }
			 if (getBidSize() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(BID_SIZE);
				 sb.append(FIELD_SEP);
				 sb.append(getBidSize().toString());
			 }
			 if (getAskPrice() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(ASK_PRICE);
				 sb.append(FIELD_SEP);
				 sb.append(getAskPrice().toString());
			 }
			 if (getAskSize() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(ASK_SIZE);
				 sb.append(FIELD_SEP);
				 sb.append(getAskSize().toString());
			 }
			 if (getExchangeId() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(EXCHANGE_ID);
				 sb.append(FIELD_SEP);
				 sb.append(getExchangeId());
			 }
			 if (getTradeMarketCenter() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(TRADE_MARKET_CENTER);
				 sb.append(FIELD_SEP);
				 sb.append(getTradeMarketCenter());
			 }
			 if (getAskMarketCenter() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(ASK_MARKET_CENTER);
				 sb.append(FIELD_SEP);
				 sb.append(getAskMarketCenter());
			 }
			 if (getBidMarketCenter() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(BID_MARKET_CENTER);
				 sb.append(FIELD_SEP);
				 sb.append(getBidMarketCenter());
			 }
			 if (getVwap() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(VWAP);
				 sb.append(FIELD_SEP);
				 sb.append(getVwap().toString());
			 }
			 if (getCreationDateTime() != null) {
				 sb.append(TUPLE_SEP);
				 sb.append(CREATION_DATE);
				 sb.append(FIELD_SEP);
				 sb.append(getCreationDateTime().toString());
			 }
			 return sb.toString().getBytes();
		 }
		 catch (Exception e) {
			 e.printStackTrace();
			 return null;
		 }
	 }
}
