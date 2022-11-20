 /**
  * StockNewsActivity.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kimg@tacpoint.com
  * @version 1.0
  * Date created:  5/25/2000
  */
package com.b4utrade.stockutil;


public class StockNewsActivity extends DefaultStockObject
{
	public String mTicker;
	public String mDowJones = StockItems.NOACTIVITY;
	public int mDowJonesID = 0;
	public String mReuters = StockItems.NOACTIVITY;
	public int mReutersID = 0;
	public String mWhisper = StockItems.NOACTIVITY;
	public int mWhisperID = 0;
	public String mEdgar = StockItems.NOACTIVITY;
	public int mEdgarID = 0;
	public String mLastNews = StockItems.NOACTIVITY;
	public String mLastNewsSource = StockItems.NOACTIVITY;
	public int mLastNewsID = 0;
	public int mLastChatRumorID = 0;
	public String mLastChatRumor;

	public boolean mUpdated = false;

	public StockNewsActivity()
	{
		mStockType = StockTypeConstants.NEWS;
	}

	public void setAll(String aDowJones,
								int aDowJonesID,
								String aReuters,
								int aReutersID,
								String aWhisper,
								int aWhisperID,
								String aEdgar,
								int aEdgarID,
								String aLastNews,
								String aLastNewsSource,
								int aLastNewsID,
								int aLastChatRumorID,
								String aLastChatRumor,
								boolean aUpdated)
	{
		mDowJones = aDowJones;
		mDowJonesID = aDowJonesID;
		mReuters = aReuters;
		mReutersID = aReutersID;
		mWhisper = aWhisper;
		mWhisperID = aWhisperID;
		mEdgar = aEdgar;
		mEdgarID = aEdgarID;
		mLastNews = aLastNews;
		mLastNewsSource = aLastNewsSource;
		mLastNewsID = aLastNewsID;
		mLastChatRumorID = aLastChatRumorID;
		mLastChatRumor = aLastChatRumor;
		mUpdated = aUpdated;

	}

}
