/** StockActivity.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 02.03.2000
* Date modified:
*/

package com.b4utrade.stockutil;

import com.tacpoint.util.Logger;

/** StockActivity class
* The StockActivity class will have the latest real time data (quotes, volume,
* day hi, low, etc.).  Make this class as lightweight as possible because there
* will be one instance per company.
*/


public class StockActivity extends DefaultStockObject
{

		public String mTicker;
		public String mLastPrice = StockItems.NOACTIVITY;
		public String mOpenPrice = StockItems.NOACTIVITY;
		public String mPreviousPrice = StockItems.NOACTIVITY;
		public String mPercentChange = StockItems.NOACTIVITY;
		public String mChange = StockItems.NOACTIVITY;
		public String mChangePrice = StockItems.NOACTIVITY;
		public String mDayHigh = StockItems.NOACTIVITY;
		public String mDayLow = StockItems.NOACTIVITY;
		public String mBidPrice = StockItems.NOACTIVITY;
		public String mAskPrice = StockItems.NOACTIVITY;
		public String mBidSize = StockItems.NOACTIVITY;
		public String mAskSize = StockItems.NOACTIVITY;
		public String mLastTradeVolume = StockItems.NOACTIVITY;
		public String mVolume = StockItems.NOACTIVITY;
		public boolean mAlert = false;
		public String mExchange = StockItems.NOACTIVITY;
		public String mRegion;
		public boolean mRestricted = false;
		public boolean mUptick = false;
		public boolean mDowntick = false;
		public String mLastTradeDateTime = StockItems.NOACTIVITY;
		public String mLastTradeDateGMT = "";
		public String mLastTradeTimeGMT = "";
		public String mAskExchangeCode = "";
		public String mBidExchangeCode = "";
		public String mRegionalAskExchangeCode = "";
		public String mRegionalBidExchangeCode = "";
		public String mMarketCenter = "";
		public String mVWAP = "";

		public String mRegionalBidPrice = "";
		public String mRegionalAskPrice = "";
		public String mRegionalBidSize = "";
		public String mRegionalAskSize = "";
		public String mQuoteDateTime = "0";
		public String mTradeDateTime = "0";
		public boolean isQuote = false;
		public boolean isRegionalQuote = false;

		public boolean isCancelledTrade = false;
		public String mTradeSequence = "0";

		public String mCorrectedTradePrice = "";
		public String mCorrectedTradeSize = "";
		public String mCancelledTradePrice = "";
		public String mCancelledTradeSize = "";

		public String mQuoteTradeCondCode1 = "";
		public String mQuoteTradeCondCode2 = "";
		public String mQuoteTradeCondCode3 = "";
		public String mQuoteTradeCondCode4 = "";
		public String mExchangeID = "";

		public String marketMakerId = "";
		public boolean isMarketMaker = false;
		public String mMarketMakerBidPrice  = "0";
		public String mMarketMakerAskPrice  = "0";
		public String mMarketMakerBidSize   = "0";
		public String mMarketMakerAskSize   = "0";

		public String mF41Value = "";

                
		public boolean mUpdated = false;
		
		public StockActivity()
      {
         mStockType = StockTypeConstants.STOCK;
      }
/*
		 // getters
		 public String getTicker()
		 {
			  return mTicker;
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

		 public boolean getAlert()
		 {
				return mAlert;
		 }

		 public String getExchange()
		 {
				return mExchange;
		 }

		 public String getRegion()
		 {
				return mRegion;
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

		 public boolean getUpdated()
		 {
				return mUpdated;
		 }

		 public String getHaltedTime()
		 {
				return mHaltedTime;
		 }


		 public String getAskExchangeCode()
		 {
				return mAskExchangeCode;
		 }

		 public String getBidExchangeCode()
		 {
				return mBidExchangeCode;
		 }


		 // setters
		 public void setTicker(String inTicker)
		 {
				mTicker = inTicker;
		 }

		 public void setLastPrice(String inLastPrice)
		 {
				mLastPrice = inLastPrice;
		 }

		 public void setPreviousPrice(String aPreviousPrice)
		 {
				mPreviousPrice = aPreviousPrice;
		 }

		 public void setOpenPrice(String inOpenPrice)
		 {
				mOpenPrice = inOpenPrice;
		 }

		 public void setPercentChange(String inPercentChange)
		 {
				mPercentChange = inPercentChange;
		 }

		 public void setVolume(String inTotalVolume)
		 {
				mVolume = inTotalVolume;
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

		 public void setAlert(boolean alert)
		 {
				mAlert = alert;
		 }

		 public void setExchange(String inExchange)
		 {
				mExchange = inExchange;
		 }

		 public void setRegion(String inRegion)
		 {
				mRegion = inRegion;
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

		 public void setHaltedTime(String inHaltedTime)
		 {
				if (inHaltedTime == null || inHaltedTime.length() == 0)
					mHaltedTime = null;
				else
					mHaltedTime = inHaltedTime;
		 }

		 public void setUpdated(boolean inUpdated)
		 {
				mUpdated = inUpdated;
		 }
*/

		public void setAll(String aLastPrice,
								  String aOpenPrice,
								  String aPreviousPrice,
								  String aPercentChange,
								  String aChange,
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
								  String aExchange,
								  String aRegion,
								  boolean aRestricted,
								  boolean aUptick,
								  boolean aDowntick,
								  boolean aUpdated)
	{
		  if (aLastPrice.length() > 0)
				mLastPrice = aLastPrice;
		  if (aOpenPrice.length() > 0)
				mOpenPrice = aOpenPrice;
		  if (aPreviousPrice.length() > 0)
				mPreviousPrice = aPreviousPrice;
		  if (aPercentChange.length() > 0)
				mPercentChange = aPercentChange;
	      if (aChange.length() > 0)
				mChange = aChange;
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

		  mAlert = alert;

		  if (aExchange.length() > 0)
				mExchange = aExchange;
		  if (aRegion.length() > 0)
				mRegion = aRegion;
		  mRestricted = aRestricted;
		  mUptick = aUptick;
		  mDowntick = aDowntick;
		  mUpdated = aUpdated;

	}

   
   public void clone(StockActivity aStock)
   {
      try
      {
         super.clone(aStock);

         mTicker           = aStock.mTicker;
         mLastPrice        = aStock.mLastPrice;
         mOpenPrice        = aStock.mOpenPrice;
         mPreviousPrice    = aStock.mPreviousPrice;
         mPercentChange    = aStock.mPercentChange;
         mChange           = aStock.mChange;
         mChangePrice      = aStock.mChangePrice;
         mDayHigh          = aStock.mDayHigh;
         mDayLow           = aStock.mDayLow;
         mBidPrice         = aStock.mBidPrice;
         mAskPrice         = aStock.mAskPrice;
         mBidSize          = aStock.mBidSize;
         mAskSize          = aStock.mAskSize;
         mLastTradeVolume  = aStock.mLastTradeVolume;
         mVolume           = aStock.mVolume;
         mAlert            = aStock.mAlert;
         mExchange         = aStock.mExchange;
         mRegion           = aStock.mRegion;
         mRestricted       = aStock.mRestricted;
         mUptick           = aStock.mUptick;
         mDowntick         = aStock.mDowntick;
         mUpdated          = aStock.mUpdated;
         mLastTradeDateTime     = aStock.mLastTradeDateTime;
         mLastTradeDateGMT      = aStock.mLastTradeDateGMT;
         mLastTradeTimeGMT      = aStock.mLastTradeTimeGMT;
         mBidExchangeCode  = aStock.mBidExchangeCode;
         mAskExchangeCode  = aStock.mAskExchangeCode;
         mMarketCenter  = aStock.mMarketCenter;
         mVWAP  = aStock.mVWAP;
      }
      catch(Exception e)
      {
         Logger.log("StockActivity.clone exception " + e.getMessage());
      }
	}
}

