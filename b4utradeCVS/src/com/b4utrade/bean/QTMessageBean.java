package com.b4utrade.bean;

import java.util.Calendar;
import java.util.StringTokenizer;

import com.b4utrade.helper.StockActivityHelper;
import com.tacpoint.common.DefaultObject;

/**
 * Bean class used to model both trade and quote messages.
 * 
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002. All rights
 *         reserved.
 * @version 1.0
 */

public class QTMessageBean extends DefaultObject {

	private transient static Calendar currentTradeDate = null;

	private String ticker;

	private String systemTicker; // same as ticker except for option; option
									// will add suffix ".OP" to ticker.

	private double lastPrice = 0.0;

	private double openPrice = 0.0;

	private double percentChange = 0.0;

	private double changePrice = 0.0;

	private double dayHigh = 0.0;

	private double dayLow = 0.0;

	private long volume;

	private long lastTradeVolume;

	private long bidSize;

	private long askSize;

	private double bidPrice = 0.0;

	private double askPrice = 0.0;

	private String bidSize_str;

	private String askSize_str;

	private String bidPrice_str;

	private String askPrice_str;

	private int messageType;

	private double lastClosedPrice = 0.0;

	private Calendar lastTradeDate;

	private boolean tickFlag;

	private boolean compositeOpen;

	private boolean compositeHigh;

	private boolean compositeLow;

	private boolean updateOhl;

	private boolean updateLast;

	private int lastTradeYear;

	private int lastTradeMonth;

	private int lastTradeDay;

	private int lastTradeHour;

	private int lastTradeMinute;

	private int lastTradeSecond;

	private int lastTradeMillisecond;

	private String lastTradeYear_str;

	private String lastTradeMonth_str;

	private String lastTradeDay_str;

	private String lastTradeHour_str;

	private String lastTradeMinute_str;

	private String lastTradeSecond_str;

	private String lastTradeMillisecond_str;

	private int securityType;

	private double lastClosedPriceRange1 = 0.0;

	private double lastClosedPriceRange2 = 0.0;

	private double openPriceRange1 = 0.0;

	private double openPriceRange2 = 0.0;

	private String exchangeCode;
 
	private String mAskExchangeCode;

	private String mBidExchangeCode;

	private String mMarketCenter;
 
	private String mVWAP;

	private String tradeDate;

	private String lastTradeTime;
	
	private String exchangeId;
	
	private double settlementPrice = 0.0;

	private boolean unabridged = false;

	public final static int MSG_TYPE_AGGREGATE = 0;

	public final static int MSG_TYPE_QUOTE = 1;

	public final static int MSG_TYPE_TRADE = 2;

	public final static int SECURITY_TYPE_EQUITY = 0;

	public final static int SECURITY_TYPE_FUTURE = 2;

	public final static int SECURITY_TYPE_EQUITY_OPTION = 4;

	public final static int SECURITY_TYPE_FUTURE_OPTION = 5;

	byte[] bytedata = null;

	String outputString = null;
	
	private String limitUpDown;
	
	private String refVar1;
	
	private String refVar2;

	private double extendedLastPrice = 0.0;
	private double extendedPercentChange = 0.0;
	private double extendedChangePrice = 0.0;
	private long extendedLastTradeVolume = 0;
	private String extendedLastTradeTime;
	private String extendedTradeDate;
	private String extendedMarketCenter;
	private boolean extendedUpDownTick;
	private long volumePlus;
	/**
	 * Constructor set processing flag to false.
	 */
	public QTMessageBean() {
		messageType = QTMessageBean.MSG_TYPE_AGGREGATE;
		compositeOpen = false;
		compositeHigh = false;
		compositeLow = false;
		updateOhl = false;
		updateLast = false;
	}

	public byte[] getByteData() {
		return this.bytedata;
	}

	public String getOutputString() {
		return outputString;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public void setSystemTicker(String ticker) {
		this.systemTicker = ticker;
	}

	public void setUnabridged(boolean unabridged) {
		this.unabridged = unabridged;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}

	public void setPercentChange(double percentChange) {
		this.percentChange = percentChange;
	}

	public void setChangePrice(double changePrice) {
		this.changePrice = changePrice;
	}

	public void setDayHigh(double dayHigh) {
		this.dayHigh = dayHigh;
	}

	public void setDayLow(double dayLow) {
		this.dayLow = dayLow;
	}

	public void setBidSize(long bidSize) {
		this.bidSize = bidSize;
		bidSize_str = "" + bidSize;
	}

	public void setAskSize(long askSize) {
		this.askSize = askSize;
		askSize_str = "" + askSize;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public void setLastTradeVolume(long lastTradeVolume) {
		this.lastTradeVolume = lastTradeVolume;
	}

	public void setBidPrice(double bidPrice) {
		this.bidPrice = bidPrice;
		bidPrice_str = "" + bidPrice;
	}

	public void setAskPrice(double askPrice) {
		this.askPrice = askPrice;
		askPrice_str = "" + askPrice;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public void setLastClosedPrice(double lastClosedPrice) {
		this.lastClosedPrice = lastClosedPrice;
	}

	public void setLastTradeDate(Calendar lastTradeDate) {
		this.lastTradeDate = lastTradeDate;
		if (lastTradeDate != null) {
			setLastTradeDate(lastTradeDate.get(Calendar.YEAR), lastTradeDate
					.get(Calendar.MONTH), lastTradeDate
					.get(Calendar.DAY_OF_MONTH), lastTradeDate
					.get(Calendar.HOUR_OF_DAY), lastTradeDate
					.get(Calendar.MINUTE), lastTradeDate.get(Calendar.SECOND),
					lastTradeDate.get(Calendar.MILLISECOND));
		}

	}

	public void setTickFlag(boolean tickFlag) {
		this.tickFlag = tickFlag;
	}

	public void setCompositeOpen(boolean compositeOpen) {
		this.compositeOpen = compositeOpen;
	}

	public void setCompositeHigh(boolean compositeHigh) {
		this.compositeHigh = compositeHigh;
	}

	public void setCompositeLow(boolean compositeLow) {
		this.compositeLow = compositeLow;
	}

	public void setUpdateOhl(boolean updateOhl) {
		this.updateOhl = updateOhl;
	}

	public void setUpdateLast(boolean updateLast) {
		this.updateLast = updateLast;
	}

	public void setSecurityType(int securityType) {
		this.securityType = securityType;
	}

	public void setSecurityTypeEquity() {
		this.securityType = SECURITY_TYPE_EQUITY;
	}

	public void setSecurityTypeFuture() {
		this.securityType = SECURITY_TYPE_FUTURE;
	}

	public void setSecurityTypeEquityOption() {
		this.securityType = SECURITY_TYPE_EQUITY_OPTION;
	}

	public void setSecurityTypeFutureOption() {
		this.securityType = SECURITY_TYPE_FUTURE_OPTION;
	}

	public void setLastClosedPriceRange2(double priceRange) {
		this.lastClosedPriceRange2 = priceRange;
	}

	public void setLastClosedPriceRange1(double priceRange) {
		this.lastClosedPriceRange1 = priceRange;
	}

	public void setOpenPriceRange1(double priceRange) {
		this.openPriceRange1 = priceRange;
	}

	public void setOpenPriceRange2(double priceRange) {
		this.openPriceRange2 = priceRange;
	}

	public void setExchangeCode(String exchangeCode) {
		this.exchangeCode = exchangeCode;
	}

	public void setAskExchangeCode(String askExchangeCode) {
		mAskExchangeCode = askExchangeCode;
	}

	public void setBidExchangeCode(String bidExchangeCode) {
		mBidExchangeCode = bidExchangeCode;
	}

	public void setMarketCenter(String marketCenter) {
		mMarketCenter = marketCenter;
	}

	public void setVWAP(String vwap) {
		mVWAP = vwap;
	}
	
	public void setLimitUpDown(String limitUpDown) {
		this.limitUpDown = limitUpDown;
	}
	
	public void setRefVar1(String refVar1) {
		this.refVar1 = refVar1;
	}
	
	public void setRefVar2(String refVar2) {
		this.refVar2 = refVar2;
	}

	public String getRefVar1() {
		return refVar1;
	}

	public String getRefVar2() {
		return refVar2;
	}
	
	public String getLimitUpDown() {
		return limitUpDown;
	}

	public String getTicker() {
		return ticker;
	}

	public String getSystemTicker() {
		return systemTicker;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public double getPercentChange() {
		return percentChange;
	}

	public double getChangePrice() {
		return changePrice;
	}

	public double getDayHigh() {
		return dayHigh;
	}

	public double getDayLow() {
		return dayLow;
	}

	public long getBidSize() {
		return bidSize;
	}

	public long getAskSize() {
		return askSize;
	}

	public long getVolume() {
		return volume;
	}

	public long getLastTradeVolume() {
		return lastTradeVolume;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public double getAskPrice() {
		return askPrice;
	}

	public int getMessageType() {
		return messageType;
	}

	public double getLastClosedPrice() {
		return lastClosedPrice;
	}

	public Calendar getLastTradeDate() {
		this.lastTradeDate = Calendar.getInstance();
		this.lastTradeDate.set(lastTradeYear, lastTradeMonth, lastTradeDay,
				lastTradeHour, lastTradeMinute, lastTradeSecond);
		return this.lastTradeDate;
	}

	public boolean getTickFlag() {
		return this.tickFlag;
	}

	public int getSecurityType() {
		return (this.securityType);
	}

	public double getLastClosedPriceRange1() {
		return (this.lastClosedPriceRange1);
	}

	public double getLastClosedPriceRange2() {
		return (this.lastClosedPriceRange2);
	}

	public double getOpenPriceRange1() {
		return (this.openPriceRange1);
	}

	public double getOpenPriceRange2() {
		return (this.openPriceRange2);
	}

	public String getExchangeCode() {
		return (this.exchangeCode);
	}

	public String getAskExchangeCode() {
		return mAskExchangeCode;
	}

	public String getBidExchangeCode() {
		return mBidExchangeCode;
	}

	public String getMarketCenter() {
		return mMarketCenter;
	}

	public String getVWAP() {
		return mVWAP;
	}

	public boolean isSecurityTypeEquity() {
		return (this.securityType == SECURITY_TYPE_EQUITY);
	}

	public boolean isSecurityTypeFuture() {
		return (this.securityType == SECURITY_TYPE_FUTURE);
	}

	public boolean isSecurityTypeEquityOption() {
		return (this.securityType == SECURITY_TYPE_EQUITY_OPTION);
	}

	public boolean isSecurityTypeFutureOption() {
		return (this.securityType == SECURITY_TYPE_FUTURE_OPTION);
	}

	public boolean isCompositeOpen() {
		return this.compositeOpen;
	}

	public boolean isCompositeHigh() {
		return this.compositeHigh;
	}

	public boolean isCompositeLow() {
		return this.compositeLow;
	}

	public boolean isUpdateOhl() {
		return this.updateOhl;
	}

	public boolean isUpdateLast() {
		return this.updateLast;
	}

	public boolean isUnabridged() {
		return this.unabridged;
	}

	public void createByteData() {
		bytedata = this.toByteArray();
	}

	public void setLastTradeDate(int year, int month, int day, int hour,
			int minute, int second, int millis) {
		lastTradeYear = year;
		lastTradeMonth = month;
		lastTradeDay = day;
		lastTradeHour = hour;
		lastTradeMinute = minute;
		lastTradeSecond = second;
		lastTradeMillisecond = millis;

		lastTradeYear_str = "" + lastTradeYear;
		lastTradeMonth_str = "" + lastTradeMonth;
		lastTradeDay_str = "" + lastTradeDay;
		lastTradeHour_str = "" + lastTradeHour;
		lastTradeMinute_str = "" + lastTradeMinute;
		lastTradeSecond_str = "" + lastTradeSecond;
		lastTradeMillisecond_str = "" + lastTradeMillisecond;

	}

	/*
	 * createSystemTicker
	 * 
	 * if the system ticker is null, use this method to create the system ticker
	 * based on the ticker. The system ticker is the ticker will be used in the
	 * internal system (with .OP suffix).
	 * 
	 */
	public void createSystemTicker() {

		if (isSecurityTypeFuture()) {
			createFutureSystemTicker();
		} else {
			createEquityOptionSystemTicker();
		}
	}

	private void createEquityOptionSystemTicker() {

		if (ticker == null)
			return;
		if (ticker.length() < 5) {
			systemTicker = ticker + ".OP";
		} else {
			// this ticker will be used in b4utrade system
			StringBuffer sb = new StringBuffer();
			sb.append((ticker.substring(0, 3)).trim());
			sb.append((ticker.substring(3)).trim());
			sb.append(".OP");
			systemTicker = sb.toString();
		}

	}

	private void createFutureSystemTicker() {
		if (ticker == null)
			return;
		systemTicker = ticker + "." + exchangeCode + ".FT";
	}

	/**
	 * merge
	 * 
	 * This method merge two QTMessageBeans and this method is used by the input
	 * data. Therefore, only update the string values. If the input is Quote
	 * message, Quote related data are updated, and if the input is Trade
	 * message, Trade related data are updated.
	 * 
	 * @param QTMessageBean
	 *            qtbean input message
	 */

	public void merge(QTMessageBean qtbean) {

		if (qtbean == null) {
			return;
		}

		if (qtbean.messageType == QTMessageBean.MSG_TYPE_QUOTE) {
			this.bidSize_str = qtbean.bidSize_str;
			this.askSize_str = qtbean.askSize_str;
			this.bidPrice_str = qtbean.bidPrice_str;
			this.askPrice_str = qtbean.askPrice_str;
		} else {
			mergeTradeMessage(qtbean);
		}

	}

	/**
	 * mergeTradeMessage
	 * 
	 * This method focuses on the business rules for merging an input trade
	 * message.
	 * 
	 * @param QTMessageBean
	 *            qtbean input trade message
	 */
	private void mergeTradeMessage(QTMessageBean qtbean) {
		if (qtbean == null) {
			return;
		}

		if (qtbean.isUpdateLast()) {

			if (isSecurityTypeFuture()) {
				this.lastTradeVolume = qtbean.lastTradeVolume;

				if (qtbean.lastPrice > 0.0) {
					this.lastPrice = qtbean.lastPrice;
					this.changePrice = lastPrice - lastClosedPrice;

					if (this.dayLow > qtbean.lastPrice || this.dayLow == 0.0) {
						this.dayLow = qtbean.lastPrice;
					}
				}

			} else if (qtbean.lastTradeVolume > 0.0) {
				this.lastTradeVolume = qtbean.lastTradeVolume;

				if (qtbean.lastPrice > 0.0) {
					this.lastPrice = qtbean.lastPrice;
					this.changePrice = lastPrice - lastClosedPrice;

					if (this.dayLow > qtbean.lastPrice || this.dayLow == 0.0) {
						this.dayLow = qtbean.lastPrice;
					}
				}
			}

			if (qtbean.volume > 0.0) {
				this.volume = qtbean.volume;
			}

			if (this.lastClosedPrice > 0.0) {
				this.percentChange = this.changePrice / this.lastClosedPrice;
				this.percentChange = Double.isNaN(this.percentChange) ? 0.0
						: this.percentChange;
			}

			if (this.dayHigh < qtbean.lastPrice || this.dayHigh == 0.0) {
				this.dayHigh = qtbean.lastPrice;
			}

			if (qtbean.isUpdateOhl()) {
				if (qtbean.isCompositeOpen()) {
					this.openPrice = qtbean.lastPrice;
				}

				if (qtbean.isCompositeLow()) {
					this.dayLow = qtbean.lastPrice;
				}

				if (qtbean.isCompositeHigh()) {
					this.dayHigh = qtbean.lastPrice;
				}
			}
			
			this.lastTradeTime = qtbean.lastTradeTime;
			this.tradeDate = qtbean.tradeDate;

		}

		this.openPriceRange1 = (qtbean.openPriceRange1 > 0.0 ? qtbean.openPriceRange1
				: 0.0);
		this.openPriceRange2 = (qtbean.openPriceRange2 > 0.0 ? qtbean.openPriceRange2
				: 0.0);
		this.lastClosedPriceRange1 = (qtbean.lastClosedPriceRange1 > 0.0 ? qtbean.lastClosedPriceRange1
				: 0.0);
		this.lastClosedPriceRange2 = (qtbean.lastClosedPriceRange2 > 0.0 ? qtbean.lastClosedPriceRange2
				: 0.0);

	}

	/**
	 * populate Inflate the current QTMessageBean with Stock Profile Data.
	 * 
	 * @param StockProfileBean
	 *            profilebean the stock profile information
	 */
	public void populate(StockProfileBean profilebean) {
		if (profilebean == null) {
			return;
		}

		systemTicker = profilebean.getTicker();
		ticker = profilebean.getTickerInDB();
		lastClosedPrice = profilebean.getLastClosedPrice();
		lastTradeVolume = profilebean.getLastClosedVolume();

	}

	/**
	 * populate Inflate the current QTMessageBean with Stock Price data.
	 * 
	 * @param StockPriceBean
	 *            pricebean the price data
	 */
	public void populate(StockPriceBean pricebean) {
		if (pricebean == null) {
			setToDefaultValue();
			return;
		}

		lastClosedPrice = pricebean.getLastClosedPrice();
		lastPrice = pricebean.getLastPrice();
		openPrice = pricebean.getOpenPrice();
		percentChange = pricebean.getPercentChange();
		changePrice = pricebean.getChangePrice();
		dayHigh = pricebean.getDayHigh();
		dayLow = pricebean.getDayLow();
		bidSize = pricebean.getBidSize();
		askSize = pricebean.getAskSize();
		bidSize_str = "" + pricebean.getBidSize();
		askSize_str = "" + pricebean.getAskSize();
		volume = pricebean.getVolume();
		bidPrice = pricebean.getBidPrice();
		askPrice = pricebean.getAskPrice();
		bidPrice_str = "" + pricebean.getBidPrice();
		askPrice_str = "" + pricebean.getAskPrice();
		lastTradeDate = pricebean.getLastClosedDate();

		setLastTradeDate(0, 0, 0, 0, 0, 0, 0);

		if (lastTradeDate != null) {
			setLastTradeDate(lastTradeDate.get(Calendar.YEAR), lastTradeDate
					.get(Calendar.MONTH), lastTradeDate
					.get(Calendar.DAY_OF_MONTH), lastTradeDate
					.get(Calendar.HOUR_OF_DAY), lastTradeDate
					.get(Calendar.MINUTE), lastTradeDate.get(Calendar.SECOND),
					lastTradeDate.get(Calendar.MILLISECOND));
		}

		this.openPriceRange1 = pricebean.getOpenPrice();
		this.openPriceRange2 = pricebean.getOpenPriceRange2();
		this.lastClosedPriceRange1 = pricebean.getLastClosedPriceRange1();
		this.lastClosedPriceRange2 = pricebean.getLastClosedPriceRange2();

	}

	/**
	 * setToDefaultValue Set certain attributes to the default values.
	 */
	private void setToDefaultValue() {

		lastPrice = 0.0;
		openPrice = 0.0;
		percentChange = 0.0;
		changePrice = 0.0;
		dayHigh = 0.0;
		dayLow = 0.0;
		bidSize = 0;
		askSize = 0;
		bidSize_str = "0";
		askSize_str = "0";
		volume = 0;
		bidPrice = 0.0;
		askPrice = 0.0;
		bidPrice_str = "0.0";
		askPrice_str = "0.0";

		lastTradeDate = Calendar.getInstance(); // default to current date

		setLastTradeDate(0, 0, 0, 0, 0, 0, 0);

		if (lastTradeDate != null) {
			setLastTradeDate(lastTradeDate.get(Calendar.YEAR), lastTradeDate
					.get(Calendar.MONTH), lastTradeDate
					.get(Calendar.DAY_OF_MONTH), lastTradeDate
					.get(Calendar.HOUR_OF_DAY), lastTradeDate
					.get(Calendar.MINUTE), lastTradeDate.get(Calendar.SECOND),
					lastTradeDate.get(Calendar.MILLISECOND));

		}

		this.openPriceRange1 = 0.0;
		this.openPriceRange2 = 0.0;
		this.lastClosedPriceRange1 = 0.0;
		this.lastClosedPriceRange2 = 0.0;

	}

	/**
	 * toString To display the content of the bean as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ticker=");
		sb.append(ticker);
		sb.append(" systemTicker=");
		sb.append(systemTicker);
		sb.append(" lastPrice=");
		sb.append(lastPrice);
		sb.append(" openPrice=");
		sb.append(openPrice);
		sb.append(" PercentChange=");
		sb.append(percentChange);
		sb.append("\n");
		sb.append("changePrice=");
		sb.append(changePrice);
		sb.append(" dayHigh=");
		sb.append(dayHigh);
		sb.append(" dayLow=");
		sb.append(dayLow);
		sb.append(" bidSize=");
		sb.append(bidSize_str);
		sb.append("\n");
		sb.append("askSize=");
		sb.append(askSize_str);
		sb.append(" volume=");
		sb.append(volume);
		sb.append(" lastTradeVolume=");
		sb.append(lastTradeVolume);
		sb.append(" bidPrice=");
		sb.append(bidPrice_str);
		sb.append(" askPrice=");
		sb.append(askPrice_str);
		sb.append("\n");
		sb.append("messageType=");
		sb.append(messageType);
		sb.append(" lastClosedPrice=");
		sb.append(lastClosedPrice);
		sb.append(" lastTradeYear_str = " + lastTradeYear_str);
		sb.append(" lastTradeMonth_str = " + lastTradeMonth_str);
		sb.append(" lastTradeDay_str = " + lastTradeDay_str);
		sb.append(" lastTradeHour_str = " + lastTradeHour_str);
		sb.append(" lastTradeMinute_str = " + lastTradeMinute_str);
		sb.append(" lastTradeSecond_str = " + lastTradeSecond_str);
		sb.append(" openPriceRange1 = " + openPriceRange1);
		sb.append(" openPriceRange2 = " + openPriceRange2);
		sb.append(" lastClosedPriceRange1 = " + lastClosedPriceRange1);
		sb.append(" lastClosedPriceRange2 = " + lastClosedPriceRange2);
		sb.append(" tradeDate = "+tradeDate);
		sb.append(" lastTradeTime = "+lastTradeTime);
		sb.append(" exchangeCode = "+exchangeCode);
		sb.append(" askExchangeCode = "+mAskExchangeCode);
		sb.append(" bidExchangeCode = "+mBidExchangeCode);
		sb.append(" marketCenter = "+mMarketCenter);
		sb.append(" vwap = "+mVWAP);
		sb.append(" exchangeId = "+exchangeId);
		sb.append(" settlementPrice = "+settlementPrice);

		return (sb.toString());
	}

	/**
	 * toByteArray create the byte array based on the QTMessageBean's content
	 * 
	 * @return byte[]
	 */
	public byte[] toByteArray() {

		createOutputString();

		return (outputString.getBytes());

	}

	/**
	 * createOutputString Create the output String that will be serialized and
	 * send to other part of the system. This is used by the message producer to
	 * create the output string.
	 */
	public void createOutputString() {

		StringBuffer sb = new StringBuffer();
		sb.append(ticker);
		sb.append("||");
		sb.append(systemTicker);
		sb.append("||");
		sb.append(lastPrice);
		sb.append("||");
		sb.append(openPrice);
		sb.append("||");
		sb.append(percentChange);
		sb.append("||");
		sb.append(changePrice);
		sb.append("||");
		sb.append(dayHigh);
		sb.append("||");
		sb.append(dayLow);
		sb.append("||");
		sb.append((bidSize_str != null ? bidSize_str : "0"));
		sb.append("||");
		sb.append((askSize_str != null ? askSize_str : "0"));
		sb.append("||");
		sb.append(volume);
		sb.append("||");
		sb.append(lastTradeVolume);
		sb.append("||");
		sb.append((bidPrice_str != null ? bidPrice_str : "0.0"));
		sb.append("||");
		sb.append((askPrice_str != null ? askPrice_str : "0.0"));
		sb.append("||");
		sb.append(messageType);
		sb.append("||");
		sb.append(lastClosedPrice);
		sb.append("||");
		sb.append((lastTradeYear_str != null ? lastTradeYear_str : "0"));
		sb.append("||");
		sb.append((lastTradeMonth_str != null ? lastTradeMonth_str : "0"));
		sb.append("||");
		sb.append((lastTradeDay_str != null ? lastTradeDay_str : "0"));
		sb.append("||");
		sb.append((lastTradeHour_str != null ? lastTradeHour_str : "0"));
		sb.append("||");
		sb.append((lastTradeMinute_str != null ? lastTradeMinute_str : "0"));
		sb.append("||");
		sb.append((lastTradeSecond_str != null ? lastTradeSecond_str : "0"));
		sb.append("||");

		if (tickFlag) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}

		sb.append("||");
		sb.append(openPriceRange1);

		sb.append("||");
		sb.append(openPriceRange2);

		sb.append("||");
		sb.append(lastClosedPriceRange1);

		sb.append("||");
		sb.append(lastClosedPriceRange2);

		sb.append("||");
		sb.append(tradeDate);

		sb.append("||");
		sb.append(lastTradeTime);
        
        sb.append("||");
        sb.append(exchangeCode);
        
        sb.append("||");
        sb.append(mAskExchangeCode);
        
        sb.append("||");
        sb.append(mBidExchangeCode);
        
        sb.append("||");
        sb.append(mMarketCenter);
        
        sb.append("||");
        sb.append(mVWAP);

		sb.append("||");
		sb.append(exchangeId);

		sb.append("||");
		sb.append(settlementPrice);
		
		//for Halted status
		sb.append("||");

		if (unabridged) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		
		// For Limit up down
		
		sb.append("||");
		sb.append(limitUpDown);
		outputString = (sb.toString());
	}

	/**
	 * convertSAHToQTM This method is used by consumer to convert the byte array
	 * to the QTMessageBean.
	 * 
	 * @param byte[]
	 *            bytes the byte array of a QTMessageBean
	 * @return QTMessageBean resurrected QTMessageBean
	 */
	public static QTMessageBean convertSAHToQTM(StockActivityHelper sah) {
		if (sah == null)
			return null;
		QTMessageBean qtmb = new QTMessageBean();
		qtmb.setSystemTicker(sah.getTicker());
		try {
			qtmb.setLastPrice(Double.parseDouble(sah.getLastPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setOpenPrice(Double.parseDouble(sah.getOpenPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setLastClosedPrice(Double.parseDouble(sah.getPreviousPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setBidSize(Long.parseLong(sah.getBidSize()));
		} catch (Exception e) {
		}
		try {
			qtmb.setAskSize(Long.parseLong(sah.getAskSize()));
		} catch (Exception e) {
		}
		try {
			qtmb.setBidPrice(Double.parseDouble(sah.getBidPrice()));
		} catch (Exception e) {
		}
		try {
			qtmb.setAskPrice(Double.parseDouble(sah.getAskPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setChangePrice(Double.parseDouble(sah.getChangePrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setPercentChange(Double.parseDouble(sah.getPercentChange()));
		} catch (Exception e) {
		}

		try {
			qtmb.setDayLow(Double.parseDouble(sah.getDayLow()));
		} catch (Exception e) {
		}

		try {
			qtmb.setDayHigh(Double.parseDouble(sah.getDayHigh()));
		} catch (Exception e) {
		}

		try {
			qtmb.setVolume(Long.parseLong(sah.getVolume()));
		} catch (Exception e) {
		}
		try {
			qtmb.setLastTradeVolume(Long.parseLong(sah.getLastTradeVolume()));
		} catch (Exception e) {
		}

		if (sah.getExchange() != null && !sah.getExchange().equals(""))
			qtmb.setExchangeCode(sah.getExchange());		
		
		if (sah.getAskExchangeCode() != null && !sah.getAskExchangeCode().equals(""))
			qtmb.setAskExchangeCode(sah.getAskExchangeCode());
		
		if (sah.getBidExchangeCode() != null && !sah.getBidExchangeCode().equals(""))
			qtmb.setBidExchangeCode(sah.getBidExchangeCode());
		
		if (sah.getMarketCenter() != null && !sah.getMarketCenter().equals(""))
			qtmb.setMarketCenter(sah.getMarketCenter());
		
		if (sah.getVWAP() != null && !sah.getVWAP().equals(""))
			qtmb.setVWAP(sah.getVWAP());

		qtmb.setTradeDate(sah.getLastTradeDateGMT());
		qtmb.setLastTradeTime(sah.getLastTradeTimeGMT());
		
		if (sah.getComstockExchangeId() != null && !sah.getComstockExchangeId().equals(""))
			qtmb.setExchangeId(sah.getComstockExchangeId());
		
		try {
			qtmb.setSettlementPrice(Double.parseDouble(sah.getSettlementPrice()));	
		}
		catch (Exception e) {}
		

		return (qtmb);
	}

	/**
	 * getQTMessageBean This method is used by consumer to convert the byte
	 * array to the QTMessageBean.
	 * 
	 * @param byte[]
	 *            bytes the byte array of a QTMessageBean
	 * @return QTMessageBean resurrected QTMessageBean
	 */
	public static QTMessageBean getQTMessageBean(byte[] bytes) {
		try {
			QTMessageBean bean = new QTMessageBean();
			String beanstr = new String(bytes);

			StringTokenizer st = new StringTokenizer(beanstr, "||");
			bean.ticker = (st.nextToken()).trim();
			bean.systemTicker = (st.nextToken()).trim();
			bean.lastPrice = Double.parseDouble((st.nextToken()).trim());
			bean.openPrice = Double.parseDouble((st.nextToken()).trim());
			bean.percentChange = Double.parseDouble((st.nextToken()).trim());
			bean.changePrice = Double.parseDouble((st.nextToken()).trim());
			bean.dayHigh = Double.parseDouble((st.nextToken()).trim());
			bean.dayLow = Double.parseDouble((st.nextToken()).trim());
			bean.bidSize_str = (st.nextToken()).trim();
			bean.bidSize = Long.parseLong(bean.bidSize_str);

			bean.askSize_str = (st.nextToken()).trim();
			bean.askSize = Long.parseLong(bean.askSize_str);

			bean.volume = Long.parseLong((st.nextToken()).trim());
			bean.lastTradeVolume = Long.parseLong((st.nextToken()).trim());

			bean.bidPrice_str = (st.nextToken()).trim();
			bean.bidPrice = Double.parseDouble(bean.bidPrice_str);

			bean.askPrice_str = (st.nextToken()).trim();
			bean.askPrice = Double.parseDouble(bean.askPrice_str);

			bean.messageType = Integer.parseInt((st.nextToken()).trim());
			bean.lastClosedPrice = Double.parseDouble((st.nextToken()).trim());

			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			/*bean.lastTradeYear_str = (st.nextToken()).trim();
			bean.lastTradeYear = Integer.parseInt(bean.lastTradeYear_str);

			bean.lastTradeMonth_str = (st.nextToken()).trim();
			bean.lastTradeMonth = Integer.parseInt(bean.lastTradeMonth_str);

			bean.lastTradeDay_str = (st.nextToken()).trim();
			bean.lastTradeDay = Integer.parseInt(bean.lastTradeDay_str);

			bean.lastTradeHour_str = (st.nextToken()).trim();
			bean.lastTradeHour = Integer.parseInt(bean.lastTradeHour_str);

			bean.lastTradeMinute_str = (st.nextToken()).trim();
			bean.lastTradeMinute = Integer.parseInt(bean.lastTradeMinute_str);

			bean.lastTradeSecond_str = (st.nextToken()).trim();
			bean.lastTradeSecond = Integer.parseInt(bean.lastTradeSecond_str);*/

			bean.tickFlag = false;
			String flag = st.nextToken().trim();
			if (flag.equals("T")) {
				bean.tickFlag = true;
			}

			bean.openPriceRange1 = Double.parseDouble((st.nextToken()).trim());
			bean.openPriceRange2 = Double.parseDouble((st.nextToken()).trim());
			bean.lastClosedPriceRange1 = Double.parseDouble((st.nextToken())
					.trim());
			bean.lastClosedPriceRange2 = Double.parseDouble((st.nextToken())
					.trim());

			bean.tradeDate = st.nextToken().trim();
			bean.lastTradeTime = st.nextToken().trim();
			
			bean.exchangeCode = st.nextToken().trim();
			bean.mAskExchangeCode = st.nextToken().trim();
			bean.mBidExchangeCode = st.nextToken().trim();
			bean.mMarketCenter = st.nextToken().trim();
			bean.mVWAP = st.nextToken().trim();			
			
			String exId = st.nextToken();
			if (exId != null)
				bean.exchangeId = exId.trim();
			
			bean.setSettlementPrice(Double.parseDouble(st.nextToken()));
			bean.unabridged = false;
			String halted = st.nextToken().trim();
			if (halted.equalsIgnoreCase("T")) {
				bean.unabridged = true;
			}
			
			bean.limitUpDown = st.nextToken();
			
			return bean;

		} catch (Exception e) {
			// Exception happend may due to not enough tokens or invalid data.
			// when one of the above situation happend, return a null object.
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * getQTMessageBean
	 * 
	 * This method is used by the message producer to convert the string to the
	 * QTMessageBean object. The message producer creates a message string as
	 * input to the system.
	 * 
	 * @param String
	 *            beandata a string of quote or trade message
	 * @return QTMessageBean
	 */

	public static QTMessageBean getQTMessageBean(String beandata) {
		if (beandata == null)
			return null;

		try {

			int startpos = 0;
			int endpos = 0;
			String datastr = null;
			QTMessageBean bean = new QTMessageBean();
			// StringTokenizer st = new StringTokenizer(beandata, "||");
			endpos = beandata.indexOf("||");
			bean.messageType = Integer.parseInt(beandata.substring(startpos,
					endpos));
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			datastr = (beandata.substring(startpos, endpos)).trim(); // symbol

			if ("".equals(datastr)) {
				return null;
			}

			bean.ticker = datastr;

			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeYear_str = beandata.substring(startpos, endpos); // year
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeMonth_str = beandata.substring(startpos, endpos); // month
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeDay_str = beandata.substring(startpos, endpos); // day
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeHour_str = beandata.substring(startpos, endpos); // hour
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeMinute_str = beandata.substring(startpos, endpos); // minute
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeSecond_str = beandata.substring(startpos, endpos); // second
			startpos = endpos + 2;
			endpos = beandata.indexOf("||", startpos);
			bean.lastTradeMillisecond_str = beandata
					.substring(startpos, endpos); // milliseconds

			if (bean.messageType == QTMessageBean.MSG_TYPE_QUOTE) {
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos);
				bean.bidSize_str = beandata.substring(startpos, endpos); // bidSizeArray

				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos);
				bean.bidPrice_str = beandata.substring(startpos, endpos); // bidPriceArray

				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // askSizeArray
				bean.askSize_str = beandata.substring(startpos, endpos); // askSizeArray

				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // askPriceArray
				bean.askPrice_str = beandata.substring(startpos, endpos); // askPriceArray

				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // bboBidSizeArray
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // bboBidPriceArray
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // bboAskSizeArray
				startpos = endpos + 2; // bboAskPriceArray

			} else if (bean.messageType == QTMessageBean.MSG_TYPE_TRADE) {
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos);

				bean.volume = QTMessageBean.parseLong(beandata.substring(
						startpos, endpos)); // tradeVolumeArray
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos);

				bean.lastTradeVolume = QTMessageBean.parseLong(beandata
						.substring(startpos, endpos)); // tradeSizeArray
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos);

				bean.lastPrice = QTMessageBean.parseDouble(beandata.substring(
						startpos, endpos)); // tradePriceArray
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // askPriceArray

				int bitflag = QTMessageBean.parseInt(beandata.substring(
						startpos, endpos)); // compositeOpenArray
				if (bitflag == 1) {
					bean.compositeOpen = true;
				}
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // askPriceArray

				bitflag = QTMessageBean.parseInt(beandata.substring(startpos,
						endpos)); // compositeLowArray
				if (bitflag == 1) {
					bean.compositeLow = true;
				}
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // askPriceArray

				bitflag = QTMessageBean.parseInt(beandata.substring(startpos,
						endpos)); // compositeHighArray
				if (bitflag == 1) {
					bean.compositeHigh = true;
				}
				startpos = endpos + 2;
				endpos = beandata.indexOf("||", startpos); // askPriceArray

				bitflag = QTMessageBean.parseInt(beandata.substring(startpos,
						endpos)); // updateOhlArray
				if (bitflag == 1) {
					bean.updateOhl = true;
				}
				startpos = endpos + 2;
				bitflag = QTMessageBean.parseInt(beandata.substring(startpos)); // updateLastArray
				if (bitflag == 1) {
					bean.updateLast = true;
				}
			}

			return bean;
		} catch (Exception e) {
			// exception occurs due to missing tokens or invalid data.
			// return null
		}

		return null;

	}

	private static double parseDouble(String datastr) {
		double output = 0.0;
		try {
			output = Double.parseDouble(datastr);
		} catch (Exception e) {
			output = 0.0;
		}

		return output;
	}

	private static long parseLong(String datastr) {
		long output = 0l;
		try {
			output = Long.parseLong(datastr);
		} catch (Exception e) {
			output = 0l;
		}

		return output;
	}

	private static int parseInt(String datastr) {
		int output = 0;
		try {
			output = Integer.parseInt(datastr);
		} catch (Exception e) {
			output = 0;
		}

		return output;
	}

	/**
	 * @return Returns the lastTradeTime.
	 */
	public String getLastTradeTime() {
		return lastTradeTime;
	}

	/**
	 * @param lastTradeTime
	 *            The lastTradeTime to set.
	 */
	public void setLastTradeTime(String lastTradeTime) {
		this.lastTradeTime = lastTradeTime;
	}

	/**
	 * @return Returns the tradeDate.
	 */
	public String getTradeDate() {
		return tradeDate;
	}

	/**
	 * @param tradeDate
	 *            The tradeDate to set.
	 */
	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public double getSettlementPrice() {
		return settlementPrice;
	}

	public void setSettlementPrice(double settlementPrice) {
		this.settlementPrice = settlementPrice;
	}

	public double getExtendedLastPrice() {
		return extendedLastPrice;
	}

	public void setExtendedLastPrice(double extendedLastPrice) {
		this.extendedLastPrice = extendedLastPrice;
	}

	public double getExtendedPercentChange() {
		return extendedPercentChange;
	}

	public void setExtendedPercentChange(double extendedPercentChange) {
		this.extendedPercentChange = extendedPercentChange;
	}

	public double getExtendedChangePrice() {
		return extendedChangePrice;
	}

	public void setExtendedChangePrice(double extendedChangePrice) {
		this.extendedChangePrice = extendedChangePrice;
	}

	public long getExtendedLastTradeVolume() {
		return extendedLastTradeVolume;
	}

	public void setExtendedLastTradeVolume(long extendedLastTradeVolume) {
		this.extendedLastTradeVolume = extendedLastTradeVolume;
	}

	public String getExtendedLastTradeTime() {
		return extendedLastTradeTime;
	}

	public void setExtendedLastTradeTime(String extendedLastTradeTime) {
		this.extendedLastTradeTime = extendedLastTradeTime;
	}

	public String getExtendedTradeDate() {
		return extendedTradeDate;
	}

	public void setExtendedTradeDate(String extendedTradeDate) {
		this.extendedTradeDate = extendedTradeDate;
	}

	public String getExtendedMarketCenter() {
		return extendedMarketCenter;
	}

	public void setExtendedMarketCenter(String extendedMarketCenter) {
		this.extendedMarketCenter = extendedMarketCenter;
	}

	public boolean isExtendedUpDownTick() {
		return extendedUpDownTick;
	}

	public void setExtendedUpDownTick(boolean extendedUpDownTick) {
		this.extendedUpDownTick = extendedUpDownTick;
	}

	public long getVolumePlus() {
		return volumePlus;
	}

	public void setVolumePlus(long volumePlus) {
		this.volumePlus = volumePlus;
	}

}
