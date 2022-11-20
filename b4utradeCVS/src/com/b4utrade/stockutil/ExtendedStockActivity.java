/** ExtendedStockActivity.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author Kim Gentes
* @author kgentes@tacpoint.com
* @version 1.0
* Date created: 5/8/2000
*/

package com.b4utrade.stockutil;

import java.io.*;


/** ExtendedStockActivity class
* The ExtendedStockActivity class will have the latest real time data (quotes, volume,
* day hi, low, etc.).  Make this class as lightweight as possible because there
* will be one instance per company.
*/


public class ExtendedStockActivity extends DefaultStockObject
{

	public String mTicker;
	public String mYearHighPrice = new String();
	public String mYearLowPrice = new String();
/*	public String mEPS = StockItems.NOACTIVITY;
	public String mPE = StockItems.NOACTIVITY;*/
	public String mAvgDailyVolume = new String();
/*	public String mYield = StockItems.NOACTIVITY;
	public String mDividend = StockItems.NOACTIVITY;
	public String mDividendRate = StockItems.NOACTIVITY;
	public String mExDividend = StockItems.NOACTIVITY;*/
	public String mOptionSymbols = new String();
	public String mLEAP = new String();
	public String mWRAP = new String();
/*	public String mInstitutionHeldPercent = StockItems.NOACTIVITY;
	public String mBeta = StockItems.NOACTIVITY;
	public String mLTDebt = StockItems.NOACTIVITY;
	public String mCommonShares = StockItems.NOACTIVITY;
	public String mPreferredShares = StockItems.NOACTIVITY;*/

	public boolean mUpdated = false;

	public ExtendedStockActivity()
	{
		mStockType = StockTypeConstants.EXTSTOCK;
	}


	public void setAll(String aYearHighPrice,
								String aYearLowPrice,
/*								String aEPS,
								String aPE,*/
								String aAvgDailyVolume,
/*								String aYield,
								String aDividend,
								String aDividendRate,
								String aExDividend,*/
								String aOptionSymbols,
/*								String aInstitutionHeldPercent,
								String aBeta,*/
								String aLEAP,
								String aWRAP,
/*								String aLTDebt,
								String aCommonShares,
								String aPreferredShares,*/
								boolean aUpdated)
	{
		mYearHighPrice = aYearHighPrice;
		mYearLowPrice = aYearLowPrice;
//		mEPS = aEPS;
//		mPE = aPE;
		mAvgDailyVolume = aAvgDailyVolume;
/*		mYield = aYield;
		mDividend = aDividend;
		mDividendRate = aDividendRate;
		mExDividend = aExDividend;*/
		mOptionSymbols = aOptionSymbols;
//		mInstitutionHeldPercent = aInstitutionHeldPercent;
//		mBeta = aBeta;
		mLEAP = aLEAP;
		mWRAP = aWRAP;
/*		mLTDebt = aLTDebt;
		mCommonShares = aCommonShares;
		mPreferredShares = aPreferredShares;*/

		mUpdated = aUpdated;

	}

}

