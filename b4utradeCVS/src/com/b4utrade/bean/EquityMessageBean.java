package com.b4utrade.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class EquityMessageBean implements Serializable, Cloneable{

	private static final long serialVersionUID = 1001L;

	private String TICKER;
	//Updated Ticker - Ticker to be updated on UI
	private String UPDATED_TICKER;
	//Mapped Ticker - Ticker from which we will get data from exchange
	private String MAPPED_TICKER;
	private String LAST;
	private String OPEN;
	private String PERCENT_CHANGE;
	private String CHANGE_PRICE;
	private String DAY_HIGH;
	private String DAY_LOW;
	private String VOLUME;
	private String TRADE_SIZE;
	private String BID_SIZE;
	private String ASK_SIZE;
	private String BID;
	private String ASK;
	private String PREVIOUS_PRICE;
	private String SHO;
	private String LAST_TRADE_TIME;
//	private String tickFlag;
//	private String lastClosedPriceRange1;
//	private String lastClosedPriceRange2;
//	private String openPriceRange1;
//	private String openPriceRange2;
//	private String exchangeCode;
	private String ASK_EXCH;
	private String BID_EXCH;
	private String TRADE_EXCH; 
	private String VWAP;
	private String PROTOCOL;
//	private String settlementPrice;
//	private String unabridged;
//	private String limitUpDown; 
	
	private String TRADE_DATE;
	//Type - Equity, Options, Futures, OptionRegional, FutureRegional, OTC, OTC Pink
	private String TYPE;
	//Entitlement - Real, Delayed, LastOnly, NotEntitled 
	private String DATA_QUALITY;
	
	public final static String  TICKER_TYPE_EQUITY = "EQUITY";
	public final static String  TICKER_TYPE_OPTIONS = "OPTIONS";
	public final static String  TICKER_TYPE_FUTURES = "FUTURES";
	public final static String  TICKER_TYPE_OPTIONS_MONTAGE = "OPREG";
	public final static String  TICKER_TYPE_EQUITY_MONTAGE = "EQREG";
	public final static String  TICKER_TYPE_OTC = "OTC";
	public final static String  TICKER_TYPE_OTC_PINK = "PINK";
	
	public EquityMessageBean()
	{
		
	}
	
	public EquityMessageBean(String stockStream, String type, String dataQuality)
	{
		if(stockStream != null)
		{
			this.TYPE = type;
			this.DATA_QUALITY = dataQuality;
			String [] tokens= stockStream.split("\\|\\|");
			if(tokens.length >= 36)
			{
				this.TICKER = tokens[0];
				this.UPDATED_TICKER = tokens[0];
				this.MAPPED_TICKER = tokens[0];
				this.LAST = tokens[2];
				this.OPEN = tokens[3];
				this.PERCENT_CHANGE = tokens[4];
				this.CHANGE_PRICE = tokens[5];
				this.DAY_HIGH = tokens[6];
				this.DAY_LOW = tokens[7];
				this.BID_SIZE = tokens[8];
				this.ASK_SIZE = tokens[9];
				this.VOLUME = tokens[10];
				this.TRADE_SIZE = tokens[11];
				this.BID = tokens[12];
				this.ASK = tokens[13];
				this.PREVIOUS_PRICE = tokens[15];
				this.setSHO(tokens[22]);
//				this.openPriceRange1 = tokens[23];
//				this.openPriceRange2 = tokens[24];
//				this.lastClosedPriceRange1 = tokens[25];
//				this.lastClosedPriceRange2 = tokens[26];
				this.TRADE_DATE = tokens[27];
				this.LAST_TRADE_TIME = tokens[28];
//				this.exchangeCode = tokens[29];
				this.ASK_EXCH = tokens[30];
				this.BID_EXCH = tokens[31];
				this.TRADE_EXCH = tokens[32];
				this.VWAP = tokens[33];
				this.PROTOCOL = tokens[34];
//				this.settlementPrice = tokens[35];
//				if(tokens.length > 36){
//					this.unabridged = tokens[36];
//					this.limitUpDown = tokens[37];
//				}				
			}
			else
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Cannot contruct EquityMessageBean from " + stockStream);
		}
	}

	public String getTICKER() {
		return TICKER;
	}

	public void setTICKER(String tICKER) {
		TICKER = tICKER;
	}

	public String getUPDATED_TICKER() {
		return UPDATED_TICKER;
	}

	public void setUPDATED_TICKER(String uPDATED_TICKER) {
		UPDATED_TICKER = uPDATED_TICKER;
	}

	public String getMAPPED_TICKER() {
		return MAPPED_TICKER;
	}

	public void setMAPPED_TICKER(String mAPPED_TICKER) {
		MAPPED_TICKER = mAPPED_TICKER;
	}

	public String getLAST() {
		return LAST;
	}

	public void setLAST(String lAST) {
		LAST = lAST;
	}

	public String getOPEN() {
		return OPEN;
	}

	public void setOPEN(String oPEN) {
		OPEN = oPEN;
	}

	public String getPERCENT_CHANGE() {
		return PERCENT_CHANGE;
	}

	public void setPERCENT_CHANGE(String pERCENT_CHANGE) {
		PERCENT_CHANGE = pERCENT_CHANGE;
	}

	public String getCHANGE_PRICE() {
		return CHANGE_PRICE;
	}

	public void setCHANGE_PRICE(String cHANGE_PRICE) {
		CHANGE_PRICE = cHANGE_PRICE;
	}

	public String getDAY_HIGH() {
		return DAY_HIGH;
	}

	public void setDAY_HIGH(String dAY_HIGH) {
		DAY_HIGH = dAY_HIGH;
	}

	public String getDAY_LOW() {
		return DAY_LOW;
	}

	public void setDAY_LOW(String dAY_LOW) {
		DAY_LOW = dAY_LOW;
	}

	public String getVOLUME() {
		return VOLUME;
	}

	public void setVOLUME(String vOLUME) {
		VOLUME = vOLUME;
	}

	public String getTRADE_SIZE() {
		return TRADE_SIZE;
	}

	public void setTRADE_SIZE(String tRADE_SIZE) {
		TRADE_SIZE = tRADE_SIZE;
	}

	public String getBID_SIZE() {
		return BID_SIZE;
	}

	public void setBID_SIZE(String bID_SIZE) {
		BID_SIZE = bID_SIZE;
	}

	public String getASK_SIZE() {
		return ASK_SIZE;
	}

	public void setASK_SIZE(String aSK_SIZE) {
		ASK_SIZE = aSK_SIZE;
	}

	public String getBID() {
		return BID;
	}

	public void setBID(String bID) {
		BID = bID;
	}

	public String getASK() {
		return ASK;
	}

	public void setASK(String aSK) {
		ASK = aSK;
	}

	public String getPREVIOUS_PRICE() {
		return PREVIOUS_PRICE;
	}

	public void setPREVIOUS_PRICE(String pREVIOUS_PRICE) {
		PREVIOUS_PRICE = pREVIOUS_PRICE;
	}

	public String getLAST_TRADE_TIME() {
		return LAST_TRADE_TIME;
	}

	public void setLAST_TRADE_TIME(String lAST_TRADE_TIME) {
		LAST_TRADE_TIME = lAST_TRADE_TIME;
	}

	public String getASK_EXCH() {
		return ASK_EXCH;
	}

	public void setASK_EXCH(String aSK_EXCH) {
		ASK_EXCH = aSK_EXCH;
	}

	public String getBID_EXCH() {
		return BID_EXCH;
	}

	public void setBID_EXCH(String bID_EXCH) {
		BID_EXCH = bID_EXCH;
	}

	public String getTRADE_EXCH() {
		return TRADE_EXCH;
	}

	public void setTRADE_EXCH(String tRADE_EXCH) {
		TRADE_EXCH = tRADE_EXCH;
	}

	public String getVWAP() {
		return VWAP;
	}

	public void setVWAP(String vWAP) {
		VWAP = vWAP;
	}

	public String getPROTOCOL() {
		return PROTOCOL;
	}

	public void setPROTOCOL(String pROTOCOL) {
		PROTOCOL = pROTOCOL;
	}

	public String getTRADE_DATE() {
		return TRADE_DATE;
	}

	public void setTRADE_DATE(String tRADE_DATE) {
		TRADE_DATE = tRADE_DATE;
	}

	public String getTYPE() {
		return TYPE;
	}

	public void setTYPE(String tYPE) {
		TYPE = tYPE;
	}

	public String getDATA_QUALITY() {
		return DATA_QUALITY;
	}

	public void setDATA_QUALITY(String dATA_QUALITY) {
		DATA_QUALITY = dATA_QUALITY;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static String getTickerTypeEquity() {
		return TICKER_TYPE_EQUITY;
	}

	public static String getTickerTypeOptions() {
		return TICKER_TYPE_OPTIONS;
	}

	public static String getTickerTypeFutures() {
		return TICKER_TYPE_FUTURES;
	}

	public static String getTickerTypeOptionsMontage() {
		return TICKER_TYPE_OPTIONS_MONTAGE;
	}

	public static String getTickerTypeEquityMontage() {
		return TICKER_TYPE_EQUITY_MONTAGE;
	}

	public static String getTickerTypeOtc() {
		return TICKER_TYPE_OTC;
	}

	public static String getTickerTypeOtcPink() {
		return TICKER_TYPE_OTC_PINK;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getSHO() {
		return SHO;
	}

	public void setSHO(String sHO) {
		SHO = sHO;
	}
}
