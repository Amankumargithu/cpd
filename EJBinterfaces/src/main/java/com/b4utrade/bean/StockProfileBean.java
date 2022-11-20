/**
 * StockProfileBO.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Tacpoint Technologies, Inc.
 * @version 1.0
 * Date created:  8/7/2002
 */
package com.b4utrade.bean;


import java.io.Serializable;
import java.util.Calendar;


public class StockProfileBean implements Serializable
{

    private String ticker;
    private String tickerInDB;
    private String underlyingStockTicker;
    private double strikePrice;
    private Calendar  expirationDate;
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
    private Calendar lastSalesDate;
    private int    securityType;
    private String securityDesc;
    private boolean topSecurityFlag;

	// setters

    
	public void setTicker(String inTicker)
	{
		ticker = inTicker;
	}
    
	public void setTickerInDB(String inTicker)
	{
		tickerInDB = inTicker;
	}

	public void setUnderlyingStockTicker(String inTicker)
	{
		underlyingStockTicker = inTicker;
	}

    public void setStrikePrice(double inStrikePrice)
    {
        strikePrice = Double.isNaN(inStrikePrice) ? 0.0 : inStrikePrice;
    }

    public void setExpirationDate(Calendar inExpirationDate)
    {
        expirationDate = inExpirationDate;
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
       this.volatility = Double.isNaN(volatility) ? 0.0 : volatility;
    }
    

    public void setLastClosedPrice(double closePrice)
    {
        lastClosedPrice = Double.isNaN(closePrice) ? 0.0 : closePrice;
    }
    
    public void setLastClosedVolume(long lastVolume)
    {
        lastClosedVolume = lastVolume;
    }
    
    public void setLastClosedOpenInterest(long openInterest)
    {
        lastClosedOpenInterest = openInterest;
    }
    
    public void setLastSalesDate(Calendar lastSalesDate)
    {
        this.lastSalesDate = lastSalesDate;
    }
    
    public void setSecurityType(int securityType)
    {
        this.securityType = securityType;
    }
    
    public void setSecurityDesc(String securityDesc)
    {
        this.securityDesc = securityDesc;
    }
    
    public void setTopSecurityFlag(boolean flg)
    {
        this.topSecurityFlag = flg;
    }


    //getters
        
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


    public double getStrikePrice()
    {
        return strikePrice;
    }

    public Calendar getExpirationDate()
    {
        return expirationDate;
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
    
    public Calendar getLastSalesDate()
    {
        return lastSalesDate;
    }
    
    public int getSecurityType()
    {
        return securityType;
    }
    
    public String getSecurityDesc()
    {
        return(this.securityDesc);
    }
    
    public boolean isTopSecurityFlag()
    {
        return(this.topSecurityFlag);
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("StockProfileBean Information:");
        sb.append('\n');
        sb.append(" ticker="+ticker);
        sb.append(" tickerInDB="+tickerInDB);
        sb.append(" underlyingStockTicker="+underlyingStockTicker);
        sb.append(" expirationDate="+expirationDate.get(Calendar.YEAR)+"-"+expirationDate.get(Calendar.MONTH)+"-"+expirationDate.get(Calendar.DATE));
        sb.append('\n');
        sb.append(" exchange="+exchange);
        sb.append(" optionType="+optionType);
        sb.append(" countryCode="+countryCode);
        sb.append(" contractSize="+contractSize);
        sb.append(" volatility="+volatility);
        sb.append(" leap="+leap);
        sb.append(" securityType="+securityType);        
        sb.append('\n');
        sb.append(" lastclosed open interest="+lastClosedOpenInterest);
        sb.append(" lastclosedPrice="+lastClosedPrice);
        sb.append(" lastClosedVolume="+lastClosedVolume);
        if (lastSalesDate != null)
        {
            sb.append(" lastSalesDate="+lastSalesDate.get(Calendar.YEAR)+"-"+lastSalesDate.get(Calendar.MONTH)+"-"+lastSalesDate.get(Calendar.DATE));
        }
        
        sb.append('\n');
        
        return(sb.toString());
        
    }

}
