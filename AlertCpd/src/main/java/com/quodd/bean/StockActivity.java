package com.quodd.bean;

/**
 * StockActivity class The StockActivity class will have the latest real time
 * data (quotes, volume, day hi, low, etc.). Make this class as lightweight as
 * possible because there will be one instance per company.
 */

public class StockActivity {
	public static final String NOACTIVITY = "N/A";
	public String mTicker;
	public String mLastPrice = NOACTIVITY;
	public String mOpenPrice = NOACTIVITY;
	public String mPreviousPrice = NOACTIVITY;
	public String mPercentChange = NOACTIVITY;
	public String mChange = NOACTIVITY;
	public String mChangePrice = NOACTIVITY;
	public String mDayHigh = NOACTIVITY;
	public String mDayLow = NOACTIVITY;
	public String mBidPrice = NOACTIVITY;
	public String mAskPrice = NOACTIVITY;
	public String mBidSize = NOACTIVITY;
	public String mAskSize = NOACTIVITY;
	public String mLastTradeVolume = NOACTIVITY;
	public String mVolume = NOACTIVITY;
	public boolean mAlert = false;
	public String mExchange = NOACTIVITY;
	public String mRegion;
	public boolean mRestricted = false;
	public boolean mUptick = false;
	public boolean mDowntick = false;
	public String mLastTradeDateTime = NOACTIVITY;
	public String mLastTradeDateGMT = "";
	public String mLastTradeTimeGMT = "";
	public String mAskExchangeCode = "";
	public String mBidExchangeCode = "";
//	public String mRegionalAskExchangeCode = "";
//	public String mRegionalBidExchangeCode = "";
	public String mMarketCenter = "";
	public String mVWAP = "";

//	public String mRegionalBidPrice = "";
//	public String mRegionalAskPrice = "";
//	public String mRegionalBidSize = "";
//	public String mRegionalAskSize = "";
//	public String mQuoteDateTime = "0";
//	public String mTradeDateTime = "0";
//	public boolean isQuote = false;
//	public boolean isRegionalQuote = false;
//
//	public boolean isCancelledTrade = false;
//	public String mTradeSequence = "0";

//	public String mCorrectedTradePrice = "";
//	public String mCorrectedTradeSize = "";
//	public String mCancelledTradePrice = "";
//	public String mCancelledTradeSize = "";

//	public String mQuoteTradeCondCode1 = "";
//	public String mQuoteTradeCondCode2 = "";
//	public String mQuoteTradeCondCode3 = "";
//	public String mQuoteTradeCondCode4 = "";
	public String mExchangeID = "";

//	public String marketMakerId = "";
//	public boolean isMarketMaker = false;
//	public String mMarketMakerBidPrice = "0";
//	public String mMarketMakerAskPrice = "0";
//	public String mMarketMakerBidSize = "0";
//	public String mMarketMakerAskSize = "0";

	public StockActivity() {

	}

}
