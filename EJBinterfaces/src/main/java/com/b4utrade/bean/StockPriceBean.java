/**
 * StockPriceBO.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Tacpoint Technologies, Inc.
 * @version 1.0
 * Date created:  8/7/2002
 */
package com.b4utrade.bean;


import java.io.Serializable;
import java.util.Calendar;


public class StockPriceBean implements Serializable
{

    private String ticker;
    private double lastPrice;
    private double bidPrice;
    private double askPrice;
    private long   volume;
    private long   openInterest;
    private double lastClosedPrice;
    private long   lastClosedVolume;
    private long   lastClosedOpenInterest;
    private long   bidSize;
    private long   askSize;
    private double changePrice;
    private double percentChange;
    private double dayHigh;
    private double dayLow;
    private double openPrice;
    private Calendar lastClosedDate;
    private Calendar lastPriceDate;    
    private double openPriceRange2=0.0;
    private double lastClosedPriceRange1=0.0;
    private double lastClosedPriceRange2=0.0;

	// setters
  

    public void setLastClosedPrice(double closePrice)
    {
        lastClosedPrice = Double.isNaN(closePrice) ? 0.0 : closePrice;
    }
    
    public void setLastClosedPriceRange2(double closePrice)
    {
        lastClosedPriceRange2 = Double.isNaN(closePrice) ? 0.0 : closePrice;
        
    }
    
    public void setLastClosedPriceRange1(double closePrice)
    {
        lastClosedPriceRange1 = Double.isNaN(closePrice) ? 0.0 : closePrice;
        
    }

    
    public void setLastClosedVolume(long lastVolume)
    {
        lastClosedVolume = lastVolume;
    }
    
    public void setLastClosedOpenInterest(long openInterest)
    {
        lastClosedOpenInterest = openInterest;
    }
        
    
	public void setTicker(String inTicker)
	{
		ticker = inTicker;
	}

    public void setLastPrice(double inLastPrice)
    {
        lastPrice = Double.isNaN(inLastPrice) ? 0.0 : inLastPrice;
    }

    public void setBidPrice(double inBidPrice)
    {
         bidPrice = Double.isNaN(inBidPrice) ? 0.0 : inBidPrice;
    }

    public void setAskPrice(double inAskPrice)
    {
        askPrice = Double.isNaN(inAskPrice) ? 0.0 : inAskPrice;
    }

    public void setVolume(long inVolume)
    {
        volume = inVolume;
    }


    public void setOpenInterest(long inOpenInterest)
    {
        openInterest = inOpenInterest;
    }
    
    public void setBidSize(long inbidsize)
    {
        bidSize = inbidsize;
    }
    
    public void setAskSize(long inasksize)
    {
        askSize = inasksize;
    }

    public void setChangePrice(double changePrice)
    {
        this.changePrice = Double.isNaN(changePrice) ? 0.0 : changePrice;
    }
    
    public void setPercentChange(double percentChange)
    {
        this.percentChange = Double.isNaN(percentChange) ? 0.0 : percentChange;
    }
    
    public void setOpenPrice(double openPrice)
    {
        this.openPrice = Double.isNaN(openPrice) ? 0.0 : openPrice;
    }
    
    public void setOpenPriceRange2(double openPrice)
    {
        this.openPriceRange2 = Double.isNaN(openPrice) ? 0.0 : openPrice;
        
    }

    public void setDayHigh(double dayHigh)
    {
        this.dayHigh = Double.isNaN(dayHigh) ? 0.0 : dayHigh;
    }
    
    public void setDayLow(double dayLow)
    {
        this.dayLow = Double.isNaN(dayLow) ? 0.0 : dayLow;
    }
    
    public void setLastClosedDate(Calendar lastClosedDate)
    {
        this.lastClosedDate = lastClosedDate;
    }
    
    public void setLastPriceDate(Calendar lastClosedDate)
    {
        this.lastPriceDate = lastClosedDate;
    }
    
    
    //getters


    public double getLastClosedPriceRange1()
    {
        return lastClosedPriceRange1;
    }

    public double getLastClosedPriceRange2()
    {
        return lastClosedPriceRange2;
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


    public long getOpenInterest()
    {
        return openInterest;
    }
    
    public long getBidSize()
    {
        return bidSize;
    }
    
    
    public long getAskSize()
    {
        return askSize;
    }
    
    public double getChangePrice()
    {
        return changePrice;
    }
    
    public double getPercentChange()
    {
        return percentChange;
    }
    
    public double getOpenPrice()
    {
        return openPrice;
    }
    
    public double getOpenPriceRange2()
    {
        return openPriceRange2;
    }
        
    public double getDayHigh()
    {
        return dayHigh;
    }
    
    public double getDayLow()
    {
        return dayLow;
    }
    
    public Calendar getLastClosedDate()
    {
        return lastClosedDate;
    }

    
    public Calendar getLastPriceDate()
    {
        return lastPriceDate;
    }
    
        
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("StockPriceBean Information:");
        sb.append('\n');
        sb.append(" ticker="+ticker);
        sb.append(" lastPrice="+lastPrice);
        sb.append(" bidPrice="+bidPrice);
        sb.append(" askPrice="+askPrice);
        sb.append(" volume="+volume);
        sb.append(" openInterest="+openInterest);
        sb.append('\n');
        sb.append(" lastclosed open interest="+lastClosedOpenInterest);
        sb.append(" lastclosedPrice="+lastClosedPrice);
        sb.append(" lastClosedVolume="+lastClosedVolume);
        sb.append('\n');
        sb.append(" changePrice="+changePrice);
        sb.append(" percentChange="+percentChange);
        sb.append(" openPrice = "+openPrice);
        sb.append('\n');
        
        return(sb.toString());
        
    }

}
