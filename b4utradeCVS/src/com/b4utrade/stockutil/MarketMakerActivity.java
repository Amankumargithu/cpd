 /**
  * MarketMakerActivity.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kimg@tacpoint.com
  * @version 1.0
  * Date created:  5/9/2000
  */
package com.b4utrade.stockutil;

import java.io.*;


public class MarketMakerActivity extends DefaultStockObject
{
   public String mTicker;
   public String mBid = StockItems.NOACTIVITY;
   public String mBidSize = StockItems.NOACTIVITY;
   public String mAsk = StockItems.NOACTIVITY;
   public String mAskSize = StockItems.NOACTIVITY;
   public String mMarketMakerID = StockItems.NOACTIVITY;
   public String upc = StockItems.MM2_UPC_RESET;
   
   // new Level II feed attrs
   public String orderId = StockItems.NOACTIVITY;
   public String nextOrderId = StockItems.NOACTIVITY;
   public String prevOrderId = StockItems.NOACTIVITY;
   public boolean deleteIndicator = false;
   public boolean topOfTheBidBookIndicator = false;
   public boolean topOfTheAskBookIndicator = false;
   public String f74MessageType = StockItems.NOACTIVITY;
   public String g43MessageType = StockItems.NOACTIVITY;
   public boolean u13Indicator = false;

   public long time = 0l;
   public boolean mRestricted = false;
   public boolean mUptick = false;
   public boolean mDowntick = false;

   public boolean mUpdated = false;

   public MarketMakerActivity()
   {
      mStockType = StockTypeConstants.MARKETMAKER;
   }

   public void setTime(long time)
   {
      this.time = time;
   }

   public long getTime()
   {
      return time;
   }

   public void setUPC(String upc)
   {
      this.upc = upc;
   }

   public String getUPC()
   {
      return upc;
   } 
   public void setTicker(String inTicker)
   {
      mTicker = inTicker;
   }

   public String getTicker()
   {
      return mTicker;
   }

   public void setBid(String inBid)
   {
      mBid = inBid;
   }

   public String getBid()
   {
      return mBid;
   }

   public void setBidSize(String inBidSize)
   {
      mBidSize = inBidSize;
   }

   public String getBidSize()
   {
      return mBidSize;
   }

   public void setAsk(String inAsk)
   {
      mAsk = inAsk;
   }

   public String getAsk()
   {
      return mAsk;
   }

   public void setAskSize(String inAskSize)
   {
      mAskSize = inAskSize;
   }

   public String getAskSize()
   {
      return mAskSize;
   }

   public void setMarketMakerID(String inMarketMakerID)
   {
      mMarketMakerID = inMarketMakerID;
   }

   public String getMarketMakerID()
   {
      return mMarketMakerID;
   }

   public boolean getRestricted()
   {
      return mRestricted;
   }

   public boolean getUptick()
   {
      return mUptick;
   }

   public boolean getDowntick()
   {
      return mDowntick;
   }

   public void setRestricted(boolean inRestricted)
   {
      mRestricted = inRestricted;
   }

   public void setUptick(boolean inUptick)
   {
      mUptick = inUptick;
   }

   public void setDowntick(boolean inDowntick)
   {
      mDowntick = inDowntick;
   }


   public void setAll(String aBid,
                      String aBidSize,
                      String aAsk,
                      String aAskSize,
                      String aMarketMakerID,
                      boolean aUpdated)
   {
      mBid = aBid;
      mBidSize = aBidSize;
      mAsk = aAsk;
      mAskSize = aAskSize;
      mMarketMakerID = aMarketMakerID;
      mUpdated = aUpdated;

   }

}
