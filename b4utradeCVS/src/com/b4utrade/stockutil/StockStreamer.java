package com.b4utrade.stockutil;

/** StockStreamer.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  3/1/2000
*/

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;

public class StockStreamer
{

    public static int getStockTypeFromStream(String aStream, StringBuffer tickerNameBuffer)
    {
       String tickerName;
       if (aStream == null || aStream.length() == 0)
          return -1;
       int vOldIndex = 0;
       int vIndex = aStream.indexOf(',');
       if (vIndex <= 0)
          return -1;
       int vType = -1;
       try
       {
          String vNumber = aStream.substring(0, vIndex);
          vType = Integer.parseInt(vNumber);
       }
       catch (Exception e)
       {
          return -1;
       }
       vOldIndex = vIndex+1;
       if (vOldIndex >= aStream.length())
          return -1;

       // Get ticker: ticker must be at least 1 char
       vIndex = aStream.indexOf(',', vOldIndex);
       if (vIndex < vOldIndex+1)
          return -1;
       tickerName = aStream.substring(vOldIndex, vIndex);
       tickerNameBuffer = tickerNameBuffer.append(tickerName);
       return vType;
   }

   public static byte[] changeStockObjectToStream(DefaultStockObject aDefaultStock)
   {
	   System.out.println("IN changeStockObjectToStream other one");
      if (aDefaultStock == null)
         return null;

      byte[] vStockStream = null;
      switch (aDefaultStock.mStockType)
      {
      case (StockTypeConstants.STOCK):
         vStockStream = changeStockToStream((StockActivity)aDefaultStock);
         break;

     

      case (StockTypeConstants.NEWS):
//       vStockStream = changeNewsToStream((StockNewsActivity)aDefaultStock);
         break;

      case (StockTypeConstants.OPTION):
         break;

      case StockTypeConstants.MARKETMAKER:
         vStockStream = changeMarketMakerToStream((MarketMakerActivity)aDefaultStock);
         break;

      case StockTypeConstants.MARKETMAKER_TOP_LISTS:
         vStockStream = changeMarketMakerTopListsToStream((MarketMakerStockTopLists)aDefaultStock);
         break;

      default:

      }


      return vStockStream;
   }

   public static DefaultStockObject changeStreamToStockObject(String aStream)
   {
      if (aStream == null || aStream.length() == 0)
         return null;

      int vType = StockTypeConstants.STOCK;
      DefaultStockObject vStock = null;
      switch (vType)
      {
      case (StockTypeConstants.STOCK):
         vStock = changeStreamToStock(aStream);
         break;
      default:
      }
      return vStock;
   }

   public static byte[] changeStockToBytes(StockActivity aStock)
   {
      if (aStock == null)
         return null;
      if (aStock.mTicker == null || aStock.mTicker.length() == 0)
         return null;

      StringBuffer vStockStream = new StringBuffer(130);
      vStockStream.append(aStock.mStockType);
      vStockStream.append(',');
      vStockStream.append(aStock.mTicker);
      vStockStream.append(',');
      if (aStock.mAlert)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      vStockStream.append(aStock.mAskPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mAskSize);
      vStockStream.append(',');
      vStockStream.append(aStock.mBidPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mBidSize);
      vStockStream.append(',');
      vStockStream.append(aStock.mChangePrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mDayHigh);
      vStockStream.append(',');
      vStockStream.append(aStock.mDayLow);
      vStockStream.append(',');
      if (aStock.mDowntick)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      vStockStream.append(aStock.mExchange);
      vStockStream.append(',');
      vStockStream.append(aStock.mLastPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeVolume);
      vStockStream.append(',');
      vStockStream.append(aStock.mOpenPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mPercentChange);
      vStockStream.append(',');
      vStockStream.append(aStock.mPreviousPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mRegion);
      vStockStream.append(',');
      if (aStock.mRestricted)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      if (aStock.mUpdated)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      if (aStock.mUptick)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      vStockStream.append(aStock.mVolume);

      vStockStream.append(',');
      vStockStream.append(aStock.mAskExchangeCode);

      vStockStream.append(',');
      vStockStream.append(aStock.mBidExchangeCode);

      vStockStream.append(',');
      vStockStream.append(aStock.mMarketCenter);

      vStockStream.append(',');
      vStockStream.append(aStock.mVWAP);

      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeDateTime);

      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeDateGMT);

      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeTimeGMT);
      

      return vStockStream.toString().getBytes();
   }

   private static byte[] changeStockToStream(StockActivity aStock)
   {
      if (aStock == null)
         return null;
      if (aStock.mTicker == null || aStock.mTicker.length() == 0)
         return null;

      StringBuffer vStockStream = new StringBuffer();
      vStockStream.append(aStock.mStockType);
      vStockStream.append(',');
      vStockStream.append(aStock.mTicker);
      vStockStream.append(',');
      if (aStock.mAlert)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      vStockStream.append(aStock.mAskPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mAskSize);
      vStockStream.append(',');
      vStockStream.append(aStock.mBidPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mBidSize);
      vStockStream.append(',');
      vStockStream.append(aStock.mChangePrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mDayHigh);
      vStockStream.append(',');
      vStockStream.append(aStock.mDayLow);
      vStockStream.append(',');
      if (aStock.mDowntick)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      vStockStream.append(aStock.mExchange);
      vStockStream.append(',');
      vStockStream.append(aStock.mLastPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeVolume);
      vStockStream.append(',');
      vStockStream.append(aStock.mOpenPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mPercentChange);
      vStockStream.append(',');
      vStockStream.append(aStock.mPreviousPrice);
      vStockStream.append(',');
      vStockStream.append(aStock.mRegion);
      vStockStream.append(',');
      if (aStock.mRestricted)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      if (aStock.mUpdated)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      if (aStock.mUptick)
         vStockStream.append('T');
      else
         vStockStream.append('F');

      vStockStream.append(',');
      vStockStream.append(aStock.mVolume);

      vStockStream.append(',');
      vStockStream.append(aStock.mAskExchangeCode);

      vStockStream.append(',');
      vStockStream.append(aStock.mBidExchangeCode);

      vStockStream.append(',');
      vStockStream.append(aStock.mMarketCenter);

      vStockStream.append(',');
      vStockStream.append(aStock.mVWAP);

      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeDateTime);

      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeDateGMT);

      vStockStream.append(',');
      vStockStream.append(aStock.mLastTradeTimeGMT);
      
      vStockStream.append(',');
      vStockStream.append(aStock.mExchangeID);
      
      return vStockStream.toString().getBytes();
   }
   
   
   

   public static String parseFourDecimal(String inString)
   {
       DecimalFormat df = new DecimalFormat("#,###,##0.0000");

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
   
   
 //===== changes for ntp equity CPD
   
   private static StockActivity changeStreamToStock(String aStream)
   {
	   StockActivity vStock = new StockActivity();
	   try
	   {    	
		   StringTokenizer st = new StringTokenizer(aStream, "||");   
		   // Don't bother to convert stock type string to an integer
		   // since we should only be here if this is a stock stream.
		   vStock.mStockType = StockTypeConstants.STOCK;        
		   //skip ticker
		   st.nextToken();
		   vStock.mTicker = (st.nextToken()).trim();
		   vStock.mLastPrice = (st.nextToken());
		   vStock.mOpenPrice = (st.nextToken());
		   vStock.mPercentChange = (st.nextToken()).trim();
		   vStock.mChangePrice = (st.nextToken());
		   vStock.mDayHigh =  (st.nextToken());
		   vStock.mDayLow = (st.nextToken());
		   vStock.mBidSize = (st.nextToken()).trim();
		   vStock.mAskSize = (st.nextToken()).trim();
		   vStock.mVolume = (st.nextToken()).trim();
		   vStock.mLastTradeVolume = (st.nextToken()).trim();
		   vStock.mBidPrice = (st.nextToken());
		   vStock.mAskPrice = (st.nextToken());
		   // skip messageType
		   st.nextToken();
		   vStock.mPreviousPrice = st.nextToken();
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
		   vStock.mUptick = false;
		   vStock.mDowntick = false;
		   String flag = st.nextToken().trim();
		   if (flag.equals("T"))
		   {
			   vStock.mUptick=true;
		   }
		   else
		   {
			   vStock.mDowntick=true;
		   }
		   // open price range 1
		   st.nextToken();
		   // open price range 2
		   st.nextToken();
		   // last close price range 1
		   st.nextToken();
		   // last close price range 2
		   st.nextToken();
		   vStock.mLastTradeDateGMT = st.nextToken();
		   vStock.mLastTradeTimeGMT = st.nextToken();
		   vStock.mExchange = st.nextToken();
		   vStock.mAskExchangeCode = st.nextToken();
		   vStock.mBidExchangeCode = st.nextToken();
		   vStock.mMarketCenter = st.nextToken();
		   vStock.mVWAP = st.nextToken();
		   vStock.mExchangeID = st.nextToken(); 
	   } 
	   catch (Exception e)
	   {
		   System.out.println("Tokenizer Exception");
		   e.printStackTrace();
	   }
	   return vStock;
}

   

   private static byte[] changeMarketMakerToStream(MarketMakerActivity aStock)
   {
      if (aStock == null)
         return null;
      if (aStock.mTicker == null || aStock.mTicker.length() == 0)
         return null;

      StringBuffer vStockStream = new StringBuffer();
      vStockStream.append(aStock.mStockType);
      vStockStream.append(',');
      vStockStream.append(aStock.mTicker);
      vStockStream.append(',');
      vStockStream.append(aStock.mBid);
      vStockStream.append(',');
      vStockStream.append(aStock.mBidSize);
      vStockStream.append(',');
      vStockStream.append(aStock.mAsk);
      vStockStream.append(',');
      vStockStream.append(aStock.mAskSize);
      vStockStream.append(',');
      vStockStream.append(aStock.mMarketMakerID);
      vStockStream.append(',');
      vStockStream.append(aStock.upc);
      vStockStream.append(',');
      vStockStream.append(String.valueOf(aStock.time));
      vStockStream.append(',');
      if (aStock.mUpdated)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      // Append "||" (end of record indicator).
      vStockStream.append(',');
      if (aStock.mRestricted)
         vStockStream.append('T');
      else
         vStockStream.append('F');
      vStockStream.append(',');
      
      // add 5 new market maker fields 8/29/07
      vStockStream.append(aStock.orderId);
      vStockStream.append(',');
      vStockStream.append(aStock.prevOrderId);
      vStockStream.append(',');
      vStockStream.append(aStock.nextOrderId);
      vStockStream.append(',');
      if (aStock.deleteIndicator) 
    	  vStockStream.append('T');
      else
    	  vStockStream.append('F');
      vStockStream.append(',');
      if (aStock.topOfTheBidBookIndicator) 
    	  vStockStream.append('T');
      else
    	  vStockStream.append('F');
      
      vStockStream.append(',');
      if (aStock.topOfTheAskBookIndicator) 
    	  vStockStream.append('T');
      else
    	  vStockStream.append('F');
      
      vStockStream.append(',');     
      vStockStream.append("||");

      return vStockStream.toString().getBytes();
   }

   private static byte[] changeMarketMakerTopListsToStream(MarketMakerStockTopLists aStockLists)
   {
      if (aStockLists == null)
         return null;
      String vTicker = aStockLists.getTicker();
      if (vTicker == null || vTicker.length() == 0)
         return null;

      StringBuffer vStockStream = new StringBuffer();
      vStockStream.append(aStockLists.mStockType);
      vStockStream.append(',');
      vStockStream.append(aStockLists.getListsMaxNumber());
      vStockStream.append(',');
      vStockStream.append(vTicker);
      vStockStream.append(',');

      // get top bids list
      Vector vBids = aStockLists.getTopBidsList();
      for (int i=0; i < vBids.size(); i++)
      {
         MarketMakerPrice mma = (MarketMakerPrice)vBids.elementAt(i);
         String vMMStream = changeMMPriceToStream(mma);
         if (vMMStream != null)
         {
            vStockStream.append(vMMStream);
            vStockStream.append('<');
         }
      }

      vStockStream.append('>');

      // get top ask list
      Vector vAsks = aStockLists.getTopAsksList();
      for (int j=0; j < vAsks.size(); j++)
      {
         MarketMakerPrice mma = (MarketMakerPrice)vAsks.elementAt(j);
         String vMMStream = changeMMPriceToStream(mma);
         if (vMMStream != null)
         {
            vStockStream.append(vMMStream);
            vStockStream.append('<');
         }

      }

      // Append "||" (end of record indicator).
      vStockStream.append("||");

      return vStockStream.toString().getBytes();
   }

   private static String changeMMPriceToStream(MarketMakerPrice aStock)
   {
      if (aStock == null)
         return null;

      StringBuffer vStockStream = new StringBuffer();
      vStockStream.append(aStock.getPrice());
      vStockStream.append(',');
      vStockStream.append(aStock.getSize());
      vStockStream.append(',');
      vStockStream.append(aStock.getMarketMakerID());

      return vStockStream.toString();
   }
}
