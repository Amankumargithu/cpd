 /**
  * StockOptionActivity.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kimg@tacpoint.com
  * @version 1.0
  * Date created:  4/27/2000
  */
 package com.b4utrade.stockutil;


 public class StockOptionActivity extends DefaultStockObject
 {

       public String mTicker;
       public String mParentTicker = new String();
       public String mLastPrice = StockItems.NOACTIVITY;
       public String mPreviousPrice = StockItems.NOACTIVITY;
       public String mChangePrice = StockItems.NOACTIVITY;
       public String mBidPrice = StockItems.NOACTIVITY;
       public String mAskPrice = StockItems.NOACTIVITY;
       public String mVolume = StockItems.NOACTIVITY;
       public String mStrikePrice = StockItems.NOACTIVITY;
       public String mExpirationDate = StockItems.NOACTIVITY;
       public String mOpenInterest = StockItems.NOACTIVITY;
       public String mExchange = StockItems.NOACTIVITY;
       public String mOptionType = new String();
       public boolean mIsLeap = false;

		 public boolean mUpdated = false;

       public StockOptionActivity()
       {
      		mStockType = StockTypeConstants.OPTION;
       }

      // setters
/*
       public void setTicker(String inTicker)
       {
            mTicker = inTicker;
       }

		 public void setParentTicker(String inParentTicker)
		 {
				mParentTicker = inParentTicker;
		 }

       public void setLastPrice(String inLastPrice)
       {
            mLastPrice = inLastPrice;
       }

		 public void setPreviousPrice(String inPreviousPrice)
		 {
				mPreviousPrice = inPreviousPrice;
		 }

       public void setChangePrice(String inChangePrice)
       {
            mChangePrice = inChangePrice;
       }

       public void setBidPrice(String inBidPrice)
       {
            mBidPrice = inBidPrice;
       }

       public void setAskPrice(String inAskPrice)
       {
            mAskPrice = inAskPrice;
       }

       public void setVolume(String inVolume)
       {
            mVolume = inVolume;
       }


       public void setStrikePrice(String inStrikePrice)
       {
            mStrikePrice = inStrikePrice;
       }

       public void setExpirationDate(String inExpirationDate)
       {
            mExpirationDate = inExpirationDate;
       }

       public void setOpenInterest(String inOpenInterest)
       {
            mOpenInterest = inOpenInterest;
       }

       public void setExchange(String inExchange)
       {
            mExchange = inExchange;
       }

       public void setOptionType(String inOptionType)
       {
            mOptionType = inOptionType;
       }

		 public void setUpdated(boolean inUpdated)
		 {
				mUpdated = inUpdated;
		 }


       // getters

       public String getTicker()
       {
				return mTicker;
       }

		 public String getParentTicker()
		 {
				return mParentTicker;
		 }

       public String getLastPrice()
       {
            return mLastPrice;
       }

		 public String getPreviousPrice()
		 {
				return mPreviousPrice;
		 }

       public String getChangePrice()
       {
            return mChangePrice;
       }

       public String getBidPrice()
       {
            return mBidPrice;
       }

       public String getAskPrice()
       {
            return mAskPrice;
       }


       public String getVolume()
       {
            return mVolume;
       }

       public String getStrikePrice()
       {
            return mStrikePrice;
       }

       public String getExpirationDate()
       {
            return mExpirationDate;
       }

       public String getOpenInterest()
       {
            return mOpenInterest;
       }

       public String getExchange()
       {
            return mExchange;
       }

       public String getOptionType()
       {
            return mOptionType;
       }

		 public boolean getUpdated()
		 {
				return mUpdated;
		 }
*/
		public void setAll(String aParentTicker,
								  String aLastPrice,
								  String aPreviousPrice,
								  String aChangePrice,
								  String aBidPrice,
								  String aAskPrice,
								  String aVolume,
								  String aStrikePrice,
								  String aExpirationDate,
								  String aOpenInterest,
								  String aExchange,
								  String aOptionType,
								  boolean aIsLeap,
								  boolean aUpdated)
	{
		  if (aLastPrice.length() > 0)
				mLastPrice = aLastPrice;
		  if (aPreviousPrice.length() > 0)
				mPreviousPrice = aPreviousPrice;
		  if (aChangePrice.length() > 0)
				mChangePrice = aChangePrice;
		  if (aBidPrice.length() > 0)
				mBidPrice = aBidPrice;
		  if (aAskPrice.length() > 0)
				mAskPrice = aAskPrice;
		  if (aVolume.length() > 0)
				mVolume = aVolume;
		  if (aStrikePrice.length() > 0)
				mStrikePrice = aStrikePrice;
		  if (aExpirationDate.length() > 0)
				mExpirationDate = aExpirationDate;
		  if (aOpenInterest.length() > 0)
				mOpenInterest = aOpenInterest;
		  if (aExchange.length() > 0)
				mExchange = aExchange;
		  if (aOptionType.length() > 0)
				mOptionType = aOptionType;

		  mIsLeap = aIsLeap;
		  mUpdated = aUpdated;

	}

}
