package com.b4utrade.helper;

import com.tacpoint.common.DefaultObject;
import java.util.*;
import java.text.*;
import java.io.*;






/**
* StockActivityHelper.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
* @author BO Generator
* @author mlau@tacpoint.com
* @version 1.0
* Date created: 06/03/2000
* Date modified:
* - 06/03/2000 Initial version
*/



public class StockActivityHelper extends DefaultObject
{
    private String   mTicker;
    private String   mLastPrice;
    private String   mOpenPrice;
    private String   mPreviousPrice;
    private String   mPercentChange;
    private String   mChangePrice;
    private String   mDayHigh;
    private String   mDayLow;
    private String   mBidPrice;
    private String   mAskPrice;
    private String   mBidSize;
    private String   mAskSize;
    private String   mLastTradeVolume;
    private String   mVolume;
    private boolean  mAlert;
    private int      mNewsID;
    private String   mNewsIDString;
    private int      mChatRumorID;
    private String   mChatRumorIDString;
    private boolean  mUpTick;
    private boolean  mDownTick;
    private String   mRegion;
    private boolean  mRestricted;
    private String   mExchange;
    private String   mNewsTime;
    private boolean  mUpdate;
    private byte[]   mStockStream;
    private boolean  mIsOption;
    private boolean  mIsParseOk;

    private String mAskExchangeCode;
    private String mBidExchangeCode;
    private String mMarketCenter;
    private String mVWAP;

    private String  mLastClosePriceRange1="";
    private String  mLastClosePriceRange2="";
    private String  mOpenPriceRange1 = "";
    private String  mOpenPriceRange2 = ""; 

    private String mLastTradeDateTime;
    private String mLastTradeDateGMT;
    private String mLastTradeTimeGMT;
    
    private String mComstockExchangeId;
    private String mSettlementPrice = "0.0";
    
    private String limitUpDown;
	
	private String refVar1;
	
	private String refVar2;
	
    public String getLimitUpDown() {
		return limitUpDown;
	}
	public void setLimitUpDown(String limitUpDown) {
		this.limitUpDown = limitUpDown;
	}
	public String getRefVar1() {
		return refVar1;
	}
	public void setRefVar1(String refVar1) {
		this.refVar1 = refVar1;
	}
	public String getRefVar2() {
		return refVar2;
	}
	public void setRefVar2(String refVar2) {
		this.refVar2 = refVar2;
	}
	public String getSettlementPrice() {
		return mSettlementPrice;
	}
	public void setSettlementPrice(String settlementPrice) {
		mSettlementPrice = settlementPrice;
	}
	
	public StockActivityHelper()
    {
    }
    public void init()
    {
        if (mStockStream != null)
        {
           if(mIsOption)
             changeQTStreamToStockActivityHelper(this, new String(mStockStream));
           else
             changeStreamToStockActivityHelper(this, new String(mStockStream));
        }
    }

    //setters

    public void setTicker(String inTicker)
    {
        mTicker = inTicker;
    }

    public void setLastTradeDateTime(String inLastTradeDateTime)
    {
        mLastTradeDateTime = inLastTradeDateTime;
    }

    public void setLastTradeDateGMT (String inLastTradeDateGMT )
    {
        mLastTradeDateGMT  = inLastTradeDateGMT ;
    }

    public void setLastTradeTimeGMT(String inLastTradeTimeGMT)
    {
        mLastTradeTimeGMT = inLastTradeTimeGMT;
    }

    public void setAskExchangeCode(String askExchangeCode)
    {
        mAskExchangeCode = askExchangeCode;
    }

    public void setBidExchangeCode(String bidExchangeCode)
    {
        mBidExchangeCode = bidExchangeCode;
    }

    public void setMarketCenter(String marketCenter)
    {
        mMarketCenter = marketCenter;
    }

    public void setVWAP(String vwap)
    {
        mVWAP = vwap;
    }

    public void setOpenPriceRange1(String openPriceRange1)
    {
        mOpenPriceRange1 = openPriceRange1;
    }

    public void setOpenPriceRange2(String openPriceRange2)
    {
        mOpenPriceRange2 = openPriceRange2;
    }

    public void setLastClosePriceRange1(String lastClosePriceRange1)
    {
        mLastClosePriceRange1 = lastClosePriceRange1;
    }

    public void setLastClosePriceRange2(String lastClosePriceRange2)
    {
        mLastClosePriceRange2 = lastClosePriceRange2;
    }


    public void setLastPrice(String inLastPrice)
    {
        mLastPrice = inLastPrice;
    }

    public void setOpenPrice(String inOpenPrice)
    {
        mOpenPrice = inOpenPrice;
    }

    public void setPreviousPrice(String inPreviousPrice)
    {
        mPreviousPrice = inPreviousPrice;
    }

    public void setPercentChange(String inPercentChange)
    {
        mPercentChange = inPercentChange;
    }

    public void setChangePrice(String inChangePrice)
    {
        mChangePrice = inChangePrice;
    }

    public void setDayHigh(String inDayHigh)
    {
        mDayHigh = inDayHigh;
    }

    public void setDayLow(String inDayLow)
    {
        mDayLow = inDayLow;
    }

    public void setBidPrice(String inBidPrice)
    {
        mBidPrice = inBidPrice;
    }

    public void setAskPrice(String inAskPrice)
    {
        mAskPrice = inAskPrice;
    }

    public void setBidSize(String inBidSize)
    {
        mBidSize = inBidSize;
    }

    public void setAskSize(String inAskSize)
    {
        mAskSize = inAskSize;
    }

    public void setLastTradeVolume(String inLastTradeVolume)
    {
        mLastTradeVolume = inLastTradeVolume;
    }

    public void setVolume(String inVolume)
    {
        mVolume = inVolume;
    }

    public void setAlert(boolean inAlert)
    {
        mAlert = inAlert;
    }

    public void setNewsID(int inNewsID)
    {
        mNewsID = inNewsID;
                if (mNewsID>0)
                   mNewsIDString = ""+mNewsID;
    }

    public void setChatRumorID(int inChatRumorID)
    {
        mChatRumorID = inChatRumorID;
                if (mChatRumorID>0)
           mChatRumorIDString = ""+inChatRumorID;
    }

    public void setUpdate(boolean inUpdate)
    {
        mUpdate = inUpdate;
    }
    public void setUpTick(boolean inUpTick)
    {
        mUpTick = inUpTick;
    }


    public void setDownTick(boolean inDownTick)
    {
        mDownTick = inDownTick;
    }

    public void setRegion(String inRegion)
    {
        mRegion = inRegion;
    }

    public void setRestricted(boolean inRestricted)
    {
        mRestricted = inRestricted;
    }

    public void setExchange(String inExchange)
    {
        mExchange = inExchange;
    }
    public void setNewsTime(String inNewsTime)
    {
        mNewsTime = inNewsTime;
    }

    public void setStockStream (byte[] inStream)
    {
        mStockStream = inStream;
    }
    public void setIsOption(boolean isOption)
    {
        mIsOption = isOption;
    }
    public void setIsParseOk(boolean isParseOk)
    {
        mIsParseOk = isParseOk;
    }
    //getters

    public String getTicker()
    {
        return mTicker;
    }

    public String getLastTradeDateTime()
    {
        return mLastTradeDateTime;
    }
    public String getLastTradeDateGMT()
    {
        return mLastTradeDateGMT;
    }
    public String getLastTradeTimeGMT()
    {
        return mLastTradeTimeGMT;
    }

    public String getAskExchangeCode()
    {
        return mAskExchangeCode;
    }

    public String getBidExchangeCode()
    {
        return mBidExchangeCode;
    }

    public String getMarketCenter()
    {
        return mMarketCenter;
    }

    public String getVWAP()
    {
        return mVWAP;
    }

    public String getOpenPriceRange1()
    {
        return mOpenPriceRange1;
    }

    public String getOpenPriceRange2()
    {
        return mOpenPriceRange2;
    }

    public String getLastClosePriceRange1()
    {
        return mLastClosePriceRange1;
    }

    public String getLastClosePriceRange2()
    {
        return mLastClosePriceRange2;
    }

    public String getLastPrice()
    {
        return mLastPrice;
    }

    public String getOpenPrice()
    {
        return mOpenPrice;
    }

    public String getPreviousPrice()
    {
        return mPreviousPrice;
    }

    public String getPercentChange()
    {
        return mPercentChange;
    }

    public String getChangePrice()
    {
        return mChangePrice;
    }

    public String getDayHigh()
    {
        return mDayHigh;
    }

    public String getDayLow()
    {
        return mDayLow;
    }

    public String getBidPrice()
    {
        return mBidPrice;
    }

    public String getAskPrice()
    {
        return mAskPrice;
    }

    public String getBidSize()
    {
        return mBidSize;
    }

    public String getFormattedBidPrice()
    {
      if (mUpTick)
      {
          return("+ " + mBidPrice);
      }
      else if (mDownTick)
      {
          return("- " + mBidPrice);
      }
      else if (!("N/A".equals(mBidPrice)))
      {
          return("  " + mBidPrice);
      }
      else
      {
          return mBidPrice;
      }
    }


    public String getAskSize()
    {
        return mAskSize;
    }

    public String getLastTradeVolume()
    {
        return mLastTradeVolume;
    }

    public String getVolume()
    {
        return mVolume;
    }

    public String getFormattedVolume()
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(4);

        int vol = 0;
        double d = 0;

        try {
           vol = Integer.parseInt(mVolume);
        }
        catch (Exception e) {
           return "N/A";
        }
        d = (double)vol/(double)1000000;

        return nf.format(d);
    }

    public boolean getAlert()
    {
        return mAlert;
    }

    public int getNewsID()
    {
        return mNewsID;
    }

    public String getNewsIDAsString()
    {
        return mNewsIDString;
    }

    public int getChatRumorID()
    {
        return mChatRumorID;
    }

    public String getChatRumorIDAsString()
    {
        return mChatRumorIDString;
    }

    public boolean getUpTick()
    {
        return mUpTick;
    }
    public boolean getUpdate()
    {
        return mUpdate;
    }
    public boolean getDownTick()
    {
        return mDownTick;
    }

    public String getRegion()
    {
        return mRegion;
    }

    public boolean getRestricted()
    {
        return mRestricted;
    }

    public String getExchange()
    {
        return mExchange;
    }
    public String getNewsTime()
    {
        return mNewsTime;
    }

    public byte[] getStockStream()
    {
        return mStockStream;
    }

    public boolean getIsOption()
    {
        return mIsOption;
    }
    public boolean isParseOk()
    {
        return mIsParseOk;
    }


    public void setAll(String aLastPrice,
                       String aOpenPrice,
                       String aPreviousPrice,
                       String aPercentChange,
                       String aChangePrice,
                       String aDayHigh,
                       String aDayLow,
                       String aBidPrice,
                       String aAskPrice,
                       String aBidSize,
                       String aAskSize,
                       String aLastTradeVolume,
                       String aVolume,
                       boolean alert,
                       int    aNewsID,
                       int    aChatRumorID,
                       boolean upTick,
                       boolean downTick,
                       boolean aRestricted,
                       String  aRegion,
                       String  aExchange)
    {
        if (aLastPrice.length() > 0)
            mLastPrice = aLastPrice;
        if (aOpenPrice.length() > 0)
            mOpenPrice = aOpenPrice;
        if (aPreviousPrice.length() > 0)
            mPreviousPrice = aPreviousPrice;
        if (aPercentChange.length() > 0)
            mPercentChange = aPercentChange;
        if (aChangePrice.length() > 0)
            mChangePrice = aChangePrice;
        if (aDayHigh.length() > 0)
            mDayHigh = aDayHigh;
        if (aDayLow.length() > 0)
            mDayLow = aDayLow;
        if (aBidPrice.length() > 0)
            mBidPrice = aBidPrice;
        if (aAskPrice.length() > 0)
            mAskPrice = aAskPrice;
        if (aBidSize.length() > 0)
            mBidSize = aBidSize;
        if (aAskSize.length() > 0)
            mAskSize = aAskSize;
        if (aLastTradeVolume.length() > 0)
            mLastTradeVolume = aLastTradeVolume;
        if (aVolume.length() > 0)
            mVolume = aVolume;

            //mNewsID = aNewsID;
            //mChatRumorID = aChatRumorID;
                setNewsID(aNewsID);
                setChatRumorID(aChatRumorID);

            mUpTick = upTick;
            mDownTick = downTick;

            mAlert = alert;

            mRestricted = aRestricted;
            mRegion = aRegion;
            mExchange = aExchange;
    }
    public void setAllWithTime(String aLastPrice,
                       String aOpenPrice,
                       String aPreviousPrice,
                       String aPercentChange,
                       String aChangePrice,
                       String aDayHigh,
                       String aDayLow,
                       String aBidPrice,
                       String aAskPrice,
                       String aBidSize,
                       String aAskSize,
                       String aLastTradeVolume,
                       String aVolume,
                       boolean alert,
                       int    aNewsID,
                       int    aChatRumorID,
                       boolean upTick,
                       boolean downTick,
                       boolean aRestricted,
                       String  aRegion,
                       String  aExchange,
                       String  aNewsTime)
    {
        if (aLastPrice.length() > 0)
            mLastPrice = aLastPrice;
        if (aNewsTime.length() >0)
            mNewsTime = aNewsTime;
        if (aOpenPrice.length() > 0)
            mOpenPrice = aOpenPrice;
        if (aPreviousPrice.length() > 0)
            mPreviousPrice = aPreviousPrice;
        if (aPercentChange.length() > 0)
            mPercentChange = aPercentChange;
        if (aChangePrice.length() > 0)
            mChangePrice = aChangePrice;
        if (aDayHigh.length() > 0)
            mDayHigh = aDayHigh;
        if (aDayLow.length() > 0)
            mDayLow = aDayLow;
        if (aBidPrice.length() > 0)
            mBidPrice = aBidPrice;
        if (aAskPrice.length() > 0)
            mAskPrice = aAskPrice;
        if (aBidSize.length() > 0)
            mBidSize = aBidSize;
        if (aAskSize.length() > 0)
            mAskSize = aAskSize;
        if (aLastTradeVolume.length() > 0)
            mLastTradeVolume = aLastTradeVolume;
        if (aVolume.length() > 0)
            mVolume = aVolume;

            //mNewsID = aNewsID;
            //mChatRumorID = aChatRumorID;
                setNewsID(aNewsID);
                setChatRumorID(aChatRumorID);

            mUpTick = upTick;
            mDownTick = downTick;

            mAlert = alert;

            mRestricted = aRestricted;
            mRegion = aRegion;
            mExchange = aExchange;
    }


    public static void changeStreamToStockActivityHelper(StockActivityHelper sah, String aStream)
    {
       if (aStream == null || aStream.length() == 0)
         return;


          // Get stock type
          int vOldIndex = 0;
          int vIndex = aStream.indexOf(',');
          if (vIndex <= 0)
             return;
          // Don't bother to convert stock type string to an integer
          // since we should only be here if this is a stock stream.
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          // Get ticker: ticker must be at least 1 char
          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex+1)
             return;
          sah.setTicker(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          String vValue = null;
          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          sah.setAlert(false);
          if (vIndex > vOldIndex)
          {
             vValue = aStream.substring(vOldIndex, vIndex);
             if (vValue.compareTo("T") == 0)
                sah.setAlert(true);
          }
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setAskPrice(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setAskSize(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setBidPrice(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setBidSize(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setChangePrice(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setDayHigh(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setDayLow(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          sah.setDownTick(false);
          if (vIndex > vOldIndex)
          {
             vValue = aStream.substring(vOldIndex, vIndex);
             if (vValue.compareTo("T") == 0)
                sah.setDownTick(true);
          }
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setExchange(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setLastPrice(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setLastTradeVolume(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setOpenPrice(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setPercentChange(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setPreviousPrice(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          if (vIndex > vOldIndex)
             sah.setRegion(aStream.substring(vOldIndex, vIndex));
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          sah.setRestricted(false);
          if (vIndex > vOldIndex)
          {
             vValue = aStream.substring(vOldIndex, vIndex);
             if (vValue.compareTo("T") == 0)
                sah.setRestricted(true);
          }
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          sah.setUpdate(false);
          if (vIndex > vOldIndex)
          {
             vValue = aStream.substring(vOldIndex, vIndex);
             if (vValue.compareTo("T") == 0)
                sah.setUpdate(true);
          }

          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length())
             return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex)
             return;
          sah.setUpTick(false);
          if (vIndex > vOldIndex)
          {
             vValue = aStream.substring(vOldIndex, vIndex);
             if (vValue.compareTo("T") == 0)
                sah.setUpTick(true);
          }

          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return ;
          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return ;
          sah.setVolume(aStream.substring(vOldIndex,vIndex));

          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return ;
          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return ;
          sah.setAskExchangeCode(aStream.substring(vOldIndex,vIndex));


          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return;

          sah.setBidExchangeCode(aStream.substring(vOldIndex,vIndex));

          // trading market center
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return;

          sah.setMarketCenter(aStream.substring(vOldIndex,vIndex));

          // VWAP market center
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return;

          sah.setVWAP(aStream.substring(vOldIndex,vIndex));

          // trade date/time attrs ...
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return;

          sah.setLastTradeDateTime(aStream.substring(vOldIndex, vIndex));

          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return;

          sah.setLastTradeDateGMT(aStream.substring(vOldIndex, vIndex));

          
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          vIndex = aStream.indexOf(',', vOldIndex);
          if (vIndex < vOldIndex) return;

          sah.setLastTradeTimeGMT(aStream.substring(vOldIndex, vIndex));
          
          vOldIndex = vIndex+1;
          if (vOldIndex >= aStream.length()) return;

          sah.setComstockExchangeId(aStream.substring(vOldIndex));

          sah.setIsParseOk(true);

          return;
       }

  public static void changeQTStreamToStockActivityHelper(StockActivityHelper sah, String beanstr)
  {
    try
    {

        StringTokenizer st = new StringTokenizer(beanstr, "||");
        //skip ticker
        st.nextToken();
        sah.setTicker((st.nextToken()).trim());
        sah.setLastPrice(parseFourDecimal(st.nextToken()));
        sah.setOpenPrice(parseFourDecimal(st.nextToken()));
        sah.setPercentChange((st.nextToken()).trim());
        sah.setChangePrice(parseFourDecimal(st.nextToken()));
        sah.setDayHigh(parseFourDecimal(st.nextToken()));
        sah.setDayLow(parseFourDecimal(st.nextToken()));
        sah.setBidSize((st.nextToken()).trim());

        sah.setAskSize((st.nextToken()).trim());

        sah.setVolume((st.nextToken()).trim());
        sah.setLastTradeVolume((st.nextToken()).trim());

        sah.setBidPrice(parseFourDecimal(st.nextToken()));

        sah.setAskPrice(parseFourDecimal(st.nextToken()));

        // skip messageType
        st.nextToken();
        
        //lastClosedPrice
        sah.setPreviousPrice(st.nextToken());

        //skip lastTradeYear
        st.nextToken();

        //skip lastTradeMonth
        st.nextToken();

        //skip lastTradeDay
        st.nextToken();

        //skip lastTradeHour
        st.nextToken();

        //skip lastTradeMinute
        st.nextToken();

        //skip lastTradeSecond
        st.nextToken();

        sah.setUpTick(false);
        sah.setDownTick(false);
        String flag = st.nextToken().trim();
        if (flag.equals("T"))
        {
        	sah.setRestricted(true);
//            sah.setUpTick(true);
        }
        else
        {
        	sah.setRestricted(false);
//            sah.setDownTick(true);
        }

        // open price range 1
        sah.setOpenPriceRange1(parseFourDecimal(st.nextToken()));

        // open price range 2
        sah.setOpenPriceRange2(parseFourDecimal(st.nextToken()));

        // last close price range 1 
        sah.setLastClosePriceRange1(parseFourDecimal(st.nextToken()));

        // last close price range 2
        sah.setLastClosePriceRange2(parseFourDecimal(st.nextToken()));
        
        sah.setLastTradeDateGMT(st.nextToken());
        sah.setLastTradeTimeGMT(st.nextToken());
        
        sah.setExchange(st.nextToken());
        sah.setAskExchangeCode(st.nextToken());
        sah.setBidExchangeCode(st.nextToken());
        sah.setMarketCenter(st.nextToken());
        sah.setVWAP(st.nextToken());
        
        sah.setComstockExchangeId(st.nextToken());
        sah.setSettlementPrice(st.nextToken());
        //used for Halt flag
        String flg = st.nextToken().trim();
        if (flg.equals("T"))
        {
        	sah.setIsParseOk(true);
//            sah.setUpTick(true);
        }
        else
        {
        	sah.setIsParseOk(false);
//            sah.setDownTick(true);
        }
        
        if (st.hasMoreTokens())
        {
        	String limitUpDown = st.nextToken();
            sah.setLimitUpDown(limitUpDown);
        }

    } catch (Exception e)
    {
        //Exception happend may due to not enough tokens or invalid data.
        //when one of the above situation happend, return a null object.
    }

    return;

  }
    public static String parseFourDecimal(String inString)
    {
        DecimalFormat df = new DecimalFormat("######0.0000");

        if (inString == null)
        return "";

        try
        {
            return df.format(Double.valueOf(inString.trim()).doubleValue());

        } catch (NumberFormatException ne)
        {
           return inString;
        }
    }
	public String getComstockExchangeId() {
		return mComstockExchangeId;
	}
	public void setComstockExchangeId(String comstockExchangeId) {
		mComstockExchangeId = comstockExchangeId;
	}
}
