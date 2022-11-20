/**
 * StockOptionBO.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Tacpoint Technologies, Inc.
 * @version 1.0
 * Date created:  8/7/2002
 */
package com.b4utrade.bean;


import java.io.Serializable;
import java.util.Calendar;


public class StockOptionBean implements Serializable
{

    private long   optionID;
    private String ticker;
    private String tickerInDB;
    private String underlyingStockTicker;
    private double lastPrice;
    private double bidPrice;
    private double askPrice;
    private long   volume;
    private double strikePrice;
    private Calendar  expirationDate;
    private long   openInterest;
    private String exchange;
    private int    optionType;
    private String countryCode;
    private int    contractSize;
    private double volatility;
    private boolean isLeap;
    private int    leap;
    private double lastClosedPrice;
    private long   lastClosedVolume;
    private long   lastClosedOpenInterest;
    private long   bidSize;
    private long   askSize;

    private double intrinsicValue;
    private double timeValue;
    private double theoreticalValue;
    private double difference;
    private double delta;
    private double gamma;
    private double theta;
    
    private double highIn52Week;
    private double lowIn52Week;
    private String securityDesc;
    private double openPrice;
    private double openPriceRange2;
    private double lastClosedPriceRange1;
    private double lastClosedPriceRange2;
    private Calendar lastClosedDate;
    private Calendar lastPriceDate;
	// setters

    public void setIntrinsicValue(double intrinsicValue) {
       this.intrinsicValue = intrinsicValue;
    }

    public void setTimeValue(double timeValue) {
       this.timeValue = timeValue;
    }

    public void setTheoreticalValue(double theoreticalValue) {
       this.theoreticalValue = theoreticalValue;
    }

    public void setDifference(double difference) {
       this.difference = difference;
    }

    public void setDelta(double delta) {
       this.delta = delta;
    }

    public void setGamma(double gamma) {
       this.gamma = gamma;
    }

    public void setTheta(double theta) {
       this.theta = theta;
    }


    public void setLastClosedPrice(double closePrice)
    {
        lastClosedPrice = closePrice;
    }

    public void setLastClosedVolume(long lastVolume)
    {
        lastClosedVolume = lastVolume;
    }

    public void setLastClosedOpenInterest(long openInterest)
    {
        lastClosedOpenInterest = openInterest;
    }


    public void setOptionID(long optionID)
    {
        this.optionID = optionID;
    }

	public void setTicker(String inTicker)
	{
		ticker = inTicker;
	}

	public void setTickerInDB(String inTickerInDB)
	{
		tickerInDB = inTickerInDB;
	}

	public void setUnderlyingStockTicker(String inTicker)
	{
		underlyingStockTicker = inTicker;
	}

    public void setLastPrice(double inLastPrice)
    {
        lastPrice = inLastPrice;
    }

    public void setBidPrice(double inBidPrice)
    {
         bidPrice = inBidPrice;
    }

    public void setAskPrice(double inAskPrice)
    {
        askPrice = inAskPrice;
    }

    public void setVolume(long inVolume)
    {
        volume = inVolume;
    }


    public void setStrikePrice(double inStrikePrice)
    {
        strikePrice = inStrikePrice;
    }

    public void setExpirationDate(Calendar inExpirationDate)
    {
        expirationDate = inExpirationDate;
    }

    public void setOpenInterest(long inOpenInterest)
    {
        openInterest = inOpenInterest;
    }

    public void setExchange(String inExchange)
    {
        exchange = inExchange;
    }

    public void setOptionType(int inOptionType)
    {
        optionType = inOptionType;
    }

    public void setIsLeap(boolean inIsLeap)
    {
        isLeap = inIsLeap;
    }

    public void setLeap(int leap)
    {
        this.leap = leap;
    }

    public void setCountryCode(String country)
    {
       countryCode = country;
    }

    public void setContractSize(int contractSize)
    {
       this.contractSize = contractSize;
    }

    public void setVolatility(double volatility)
    {
       this.volatility = volatility;
    }

    public void setBidSize(long inbidsize)
    {
        bidSize = inbidsize;
    }

    public void setAskSize(long inasksize)
    {
        askSize = inasksize;
    }
    

    public void setHighIn52Week(double priceValue)
    {
        this.highIn52Week = priceValue;
    }
    
    public void setLowIn52Week(double priceValue)
    {
        this.lowIn52Week = priceValue;
    }
    
    public void setSecurityDesc(String securityDesc)
    {
        this.securityDesc = securityDesc;
    }
    
    public void setOpenPrice(double priceValue)
    {
        this.openPrice = priceValue;
    }
    
    public void setOpenPriceRange2(double priceValue)
    {
        this.openPriceRange2 = priceValue;
    }
    
    public void setLastClosedPriceRange1(double priceValue)
    {
        this.lastClosedPriceRange1 = priceValue;
    }
   
    public void setLastClosedPriceRange2(double priceValue)
    {
        this.lastClosedPriceRange2 = priceValue;
    }
     
    public void setLastClosedDate(Calendar dateValue)
    {
        this.lastClosedDate = dateValue;
    }
    
    public void setLastPriceDate(Calendar dateValue)
    {
        this.lastPriceDate = dateValue;
    }


    //getters

    public double getIntrinsicValue() {
       return  intrinsicValue;
    }

    public double getTimeValue() {
       return timeValue;
    }

    public double getTheoreticalValue() {
       return theoreticalValue;
    }

    public double getDifference() {
       return difference;
    }

    public double getDelta() {
       return delta;
    }

    public double getGamma() {
       return gamma;
    }

    public double getTheta() {
       return theta;
    }




    public double getLastClosedPrice()
    {
        return lastClosedPrice;
    }

    public long getLastClosedVolume()
    {
        return lastClosedVolume;
    }

    public long getLastClosedOpenInterest()
    {
        return lastClosedOpenInterest;
    }

    public long getBidSize()
    {
        return bidSize;
    }

    public long getAskSize()
    {
        return askSize;
    }

    public long getOptionID()
    {
        return optionID;
    }

	public String getTicker()
	{
		return ticker;
	}

	public String getTickerInDB()
	{
		return tickerInDB;
	}

	public String getUnderlyingStockTicker()
	{
		return underlyingStockTicker;
	}

    public double getLastPrice()
    {
        return lastPrice;
    }

    public double getBidPrice()
    {
         return bidPrice;
    }

    public double getAskPrice()
    {
        return askPrice;
    }

    public long getVolume()
    {
        return volume;
    }


    public double getStrikePrice()
    {
        return strikePrice;
    }

    public Calendar getExpirationDate()
    {
        return expirationDate;
    }

    public long getOpenInterest()
    {
        return openInterest;
    }

    public String getExchange()
    {
        return exchange;
    }

    public int getOptionType()
    {
        return optionType;
    }

    public boolean isLeap()
    {
        return isLeap;
    }


    public int getLeap()
    {
        return leap;
    }

    public String getCountryCode()
    {
        return countryCode;
    }

    public int getContractSize()
    {
        return contractSize;
    }

    public double getVolatility()
    {
        return volatility;
    }


    public double getHighIn52Week()
    {
        return(this.highIn52Week);
    }
    
    public double getLowIn52Week()
    {
        return(this.lowIn52Week);
    }
    
    public String getSecurityDesc()
    {
        return(this.securityDesc);
    }
    
    public double getOpenPrice()
    {
        return(this.openPrice);
    }
    
    public double getOpenPriceRange2()
    {
        return(this.openPriceRange2);
    }
    
    public double getLastClosedPriceRange1()
    {
        return(this.lastClosedPriceRange1);
    }
    
    public double getLastClosedPriceRange2()
    {
        return(this.lastClosedPriceRange2);
    }
    
    public Calendar getLastClosedDate()
    {
        return(this.lastClosedDate);
    }
    
    public Calendar getLastPriceDate()
    {
        return(this.lastPriceDate);
    }
    

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("StockOptionBean Information:");
        sb.append('\n');
        sb.append(" optionID="+optionID);
        sb.append(" ticker="+ticker);
        sb.append(" tickerInDB="+tickerInDB);
        sb.append(" underlyingStockTicker="+underlyingStockTicker);
        sb.append(" lastPrice="+lastPrice);
        sb.append(" bidPrice="+bidPrice);
        sb.append('\n');
        sb.append(" askPrice="+askPrice);
        sb.append(" volume="+volume);
        sb.append(" strikePrice="+strikePrice);
        sb.append(" expirationDate="+getExpirationDateAsString());
        sb.append(" openInterest="+openInterest);
        sb.append('\n');
        sb.append(" exchange="+exchange);
        sb.append(" optionType="+optionType);
        sb.append(" countryCode="+countryCode);
        sb.append(" contractSize="+contractSize);
        sb.append(" volatility="+volatility);
        sb.append(" leap="+leap);
        sb.append('\n');
        sb.append(" lastclosed open interest="+lastClosedOpenInterest);
        sb.append(" lastclosedPrice="+lastClosedPrice);
        sb.append(" lastClosedVolume="+lastClosedVolume);
        sb.append(" Security Desc="+securityDesc);
        sb.append(" lastClosedPriceRange1="+lastClosedPriceRange1);
        sb.append(" lastClosedPriceRange2="+lastClosedPriceRange2);
        sb.append(" openPriceRange2="+openPriceRange2);
        sb.append(" lastClosedDate="+getLastClosedDateAsString());
        sb.append(" lastPriceDate="+getLastPriceDateAsString());
        sb.append('\n');

        return(sb.toString());

    }


    public void populate(StockProfileBean spbean)
    {
        if (spbean == null)
        {
            return;
        }

        ticker = spbean.getTicker();
        tickerInDB = spbean.getTickerInDB();
        underlyingStockTicker = spbean.getUnderlyingStockTicker();
        strikePrice = spbean.getStrikePrice();
        expirationDate = spbean.getExpirationDate();
        exchange = spbean.getExchange();
        optionType = spbean.getOptionType();
        countryCode = spbean.getCountryCode();
        contractSize = spbean.getContractSize();
        lastClosedPrice = spbean.getLastClosedPrice();
        lastClosedVolume = spbean.getLastClosedVolume();
        lastClosedOpenInterest = spbean.getLastClosedOpenInterest();
        volatility = spbean.getVolatility();
        securityDesc = spbean.getSecurityDesc();
        
        
     
    }

    public void populate(StockPriceBean spbean)
    {
        if (spbean == null)
        {
            return;
        }

        lastPrice = spbean.getLastPrice();
        bidPrice = spbean.getBidPrice();
        askPrice = spbean.getAskPrice();
        volume = spbean.getVolume();
        openInterest = spbean.getOpenInterest();
        bidSize = spbean.getBidSize();
        askSize = spbean.getAskSize();
        lastClosedPrice = spbean.getLastClosedPrice();
        lastClosedDate = spbean.getLastClosedDate();
        lastPriceDate = spbean.getLastPriceDate();
        openPriceRange2 = spbean.getOpenPriceRange2();
        lastClosedPriceRange1 = spbean.getLastClosedPriceRange1();
        lastClosedPriceRange2 = spbean.getLastClosedPriceRange2();


    }

    public String getExpirationDateAsString()
    {
        return(getCalendarAsString(expirationDate));
        
    }

    public String getLastClosedDateAsString()
    {
        return(getCalendarAsString(lastClosedDate));
    }

    public String getLastPriceDateAsString()
    {
        return(getCalendarAsString(lastPriceDate));
    }
    
    
    private String getCalendarAsString(Calendar inputCal)
    {
        if (inputCal == null)
        {
            return null;
        }

        StringBuffer sb = new StringBuffer();

        sb.append(inputCal.get(Calendar.YEAR));
        sb.append("-");
        sb.append((inputCal.get(Calendar.MONTH))+1);  //add 1 to the display month since calendar start with 0
        sb.append("-");
        sb.append(inputCal.get(Calendar.DAY_OF_MONTH));


        return(sb.toString());
    }
}
