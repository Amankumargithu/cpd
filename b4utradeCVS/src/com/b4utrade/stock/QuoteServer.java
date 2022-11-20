/* QuoteServer.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 02.03.2000
* Date modified:
*     06.09.2000 - Change class to be a proxy class
* - 7/19/00 (JT)
*   switch from ObjectOutputStream to OutputStream, ObjectInputStream to InputStream
*/

package com.b4utrade.stock;

import java.util.*;
import com.tacpoint.util.*;
import com.b4utrade.stockutil.*;


/** QuoteServer class
* The QuoteServer class will communicate with the real quote server to get the
* quote information.  This is a singleton class so the servlets can have easy access to
* this class at anytime.
*/
public final class QuoteServer
{
   private boolean mDebug = true;
   private static QuoteServer mQuoteServer = new QuoteServer();

   private QuoteServerProxy[] mQuoteServerProxy;

   // Constructor
   private QuoteServer()
   {
      try
      {
         Logger.init();
         Environment.init();

         mLogger = true;

         //
         // build connections...
         //
         String quoteServers = Environment.get("QUOTE_SERVERS");
         StringTokenizer st = new StringTokenizer(quoteServers,",");
         mQuoteServerProxy = new QuoteServerProxy[st.countTokens()];

         int count = 0;

         while (st.hasMoreTokens()) {
            String proxy = st.nextToken();
            mQuoteServerProxy[count] = new QuoteServerProxy(proxy);
            Logger.log("QuoteServer - new proxy: "+proxy);
            count++;
         }

         if (mQuoteServerProxy == null ) {
            mQuoteServerProxy = new QuoteServerProxy[1];
            mQuoteServerProxy[0] = new QuoteServerProxy("localhost");
         }

         for (int i=0; i<mQuoteServerProxy.length; i++ )
            Logger.log("QuoteServerProxy["+i+"] = "+mQuoteServerProxy[i]);

      }
      catch(Exception e)
      {
		  e.printStackTrace();
      }
   }

   public static QuoteServer getInstance()
   {
      return mQuoteServer;
   }

   public synchronized LinkedList getStockQuote(String aTickers)
   {
      LinkedList vStocks = null;

      if (mQuoteServerProxy != null)
      {
         //
         // attempt to loop through array of proxies until 1 can satisfy our request...
         //
         int proxyIndex = 0;
         int proxyArraySize = mQuoteServerProxy.length;
         boolean success = false;

         while ( (!success) && (proxyIndex < proxyArraySize) ) {

            try {

               //Logger.log("QuoteServer - attempting to get quote from server: "+proxyIndex);
               QuoteRequest vRequest = new QuoteRequest();
               vRequest.setTickers(aTickers);
               vRequest.setRequest(QuoteRequest.GET_STOCK_QUOTE);
               //Logger.log("QuoteRequest send: "+proxyIndex+ " - "+ aTickers);
               if (mQuoteServerProxy[proxyIndex].write(vRequest) == true)
               {
                  vStocks = mQuoteServerProxy[proxyIndex].read();

                  //Logger.log("QuoteServer - received quote from server: "+proxyIndex);
                  /*Logger.log("Vector size from server: "+proxyIndex+ " - "+vStocks.size() + " tickers: " + aTickers);
                  ListIterator li = vStocks.listIterator(0);
                  int j = 0;
                  while (li.hasNext()) {
                    byte[] bytes= (byte[])li.next();
                    Logger.log("QuoteServer - getStockQuote: element: " + j + "string :" + new String(bytes));
                    j++;
			      }*/


                  if (vStocks != null)
                  {
                    for (int i = 0; i < vStocks.size(); i++)
                    {
                       byte[] stockStream = (byte[])vStocks.get(i);
                       if (stockStream.length > 0)
                       {
                             success = true;
                             //Logger.log("Quote found in hash...index = "+proxyIndex);

                       }
                    }
			      }
			      else
			        success = true;
               }
            }
            catch(Exception e)
            {
               e.printStackTrace();
               Logger.log("QuoteServer.getStock() " + e.getMessage(),e);
            }
            finally {
               proxyIndex++;
            }

         } // end while

      } // end if

      return vStocks;

   }

/*   public synchronized byte[] getStockQuote(String aTicker)
   {
      String stockString = null;

      if (mQuoteServerProxy != null)
      {
         //
         // attempt to loop through array of proxies until 1 can satisfy our request...
         //

         int proxyIndex = 0;
         int proxyArraySize = mQuoteServerProxy.length;
         boolean success = false;

         QuoteRequest vRequest = new QuoteRequest(aTicker, QuoteRequest.GET_STOCK_QUOTE);

         while ( (!success) && (proxyIndex < proxyArraySize) ) {

            try {

               //Logger.log("QuoteServer - attempting to get quote from server: "+proxyIndex);

               if (mQuoteServerProxy[proxyIndex].write(vRequest) == true)
               {
                  Vector vStocks = mQuoteServerProxy[proxyIndex].read();

                  //Logger.log("QuoteServer - received quote from server: "+proxyIndex);
                  //Logger.log("Vector size from server: "+proxyIndex+ " - "+vStocks.size());

                  for (int i = 0; i < vStocks.size(); i++)
                  {
                     stockString = (String)vStocks.get(i);
                  }
               }
            }
            catch(Exception e)
            {
               Logger.log("QuoteServer.getStock() " + e.getMessage(),e);
            }
            finally {
               proxyIndex++;
            }

         } // end while

      } // end if

      if (stockString == null)
         stockString = new String();

      return stockString.getBytes();

   }*/

   public void disconnect()
   {
      if (mQuoteServerProxy == null)
         return;

      for (int i=0; i<mQuoteServerProxy.length; i++)
         mQuoteServerProxy[i].closeConnection();

   }

   private boolean mLogger = false;


   // main function
   public static void main(String[] args)
   {
 /*     try
      {
         QuoteServer vQuote = null;

         for (int i = 0; i < 5; i++)
         {
            vQuote = QuoteServer.getInstance();
            StockDetail vStockDetail = vQuote.getStock("CSCO");
            StockActivity vStockQuote = vStockDetail.getStock();
            if (vStockQuote != null)
            {
               System.out.println("Ticker - " + vStockQuote.mTicker);
               System.out.println("Alert - " + vStockQuote.mAlert);
               System.out.println("Ask Price - " + vStockQuote.mAskPrice);
               System.out.println("Ask Size - " + vStockQuote.mAskSize);
               System.out.println("Bid Price - " + vStockQuote.mBidPrice);
               System.out.println("Bid Size - " + vStockQuote.mBidSize);
               System.out.println("Change Price - " + vStockQuote.mChangePrice);
               System.out.println("Day High - " + vStockQuote.mDayHigh);
               System.out.println("Day Low - " + vStockQuote.mDayLow);
               System.out.println("Downtick - " + vStockQuote.mDowntick);
               System.out.println("Exchange - " + vStockQuote.mExchange);
               System.out.println("Last Price - " + vStockQuote.mLastPrice);
               System.out.println("Last Trade Volume - " + vStockQuote.mLastTradeVolume);
               System.out.println("Open Price - " + vStockQuote.mOpenPrice);
               System.out.println("Percent Change - " + vStockQuote.mPercentChange);
               System.out.println("Previous Price - " + vStockQuote.mPreviousPrice);
               System.out.println("Region - " + vStockQuote.mRegion);
               System.out.println("Restricted - " + vStockQuote.mRestricted);
               System.out.println("Updated - " + vStockQuote.mUpdated);
               System.out.println("Uptick - " + vStockQuote.mUptick);
               System.out.println("Volume - " + vStockQuote.mVolume);
               System.out.println("_________________________________");
            }
            Thread.sleep(500);
            vStockQuote = vQuote.getStockQuote("ORCL");
            //vStockDetail = vQuote.getStock("ORCL");
            //vStockQuote = vStockDetail.getStock();
            if (vStockQuote != null)
            {
               System.out.println("Ticker - " + vStockQuote.mTicker);
               System.out.println("Alert - " + vStockQuote.mAlert);
               System.out.println("Ask Price - " + vStockQuote.mAskPrice);
               System.out.println("Ask Size - " + vStockQuote.mAskSize);
               System.out.println("Bid Price - " + vStockQuote.mBidPrice);
               System.out.println("Bid Size - " + vStockQuote.mBidSize);
               System.out.println("Change Price - " + vStockQuote.mChangePrice);
               System.out.println("Day High - " + vStockQuote.mDayHigh);
               System.out.println("Day Low - " + vStockQuote.mDayLow);
               System.out.println("Downtick - " + vStockQuote.mDowntick);
               System.out.println("Exchange - " + vStockQuote.mExchange);
               System.out.println("Last Price - " + vStockQuote.mLastPrice);
               System.out.println("Last Trade Volume - " + vStockQuote.mLastTradeVolume);
               System.out.println("Open Price - " + vStockQuote.mOpenPrice);
               System.out.println("Percent Change - " + vStockQuote.mPercentChange);
               System.out.println("Previous Price - " + vStockQuote.mPreviousPrice);
               System.out.println("Region - " + vStockQuote.mRegion);
               System.out.println("Restricted - " + vStockQuote.mRestricted);
               System.out.println("Updated - " + vStockQuote.mUpdated);
               System.out.println("Uptick - " + vStockQuote.mUptick);
               System.out.println("Volume - " + vStockQuote.mVolume);
               System.out.println("_________________________________");
            }
            Thread.sleep(500);
            QuoteServer vQuote2 = QuoteServer.getInstance();
            vStockQuote = vQuote2.getStockQuote("SUNW");
            //vStockDetail = vQuote.getStock("SUNW");
            //vStockQuote = vStockDetail.getStock();
            if (vStockQuote != null)
            {
               System.out.println("Ticker - " + vStockQuote.mTicker);
               System.out.println("Alert - " + vStockQuote.mAlert);
               System.out.println("Ask Price - " + vStockQuote.mAskPrice);
               System.out.println("Ask Size - " + vStockQuote.mAskSize);
               System.out.println("Bid Price - " + vStockQuote.mBidPrice);
               System.out.println("Bid Size - " + vStockQuote.mBidSize);
               System.out.println("Change Price - " + vStockQuote.mChangePrice);
               System.out.println("Day High - " + vStockQuote.mDayHigh);
               System.out.println("Day Low - " + vStockQuote.mDayLow);
               System.out.println("Downtick - " + vStockQuote.mDowntick);
               System.out.println("Exchange - " + vStockQuote.mExchange);
               System.out.println("Last Price - " + vStockQuote.mLastPrice);
               System.out.println("Last Trade Volume - " + vStockQuote.mLastTradeVolume);
               System.out.println("Open Price - " + vStockQuote.mOpenPrice);
               System.out.println("Percent Change - " + vStockQuote.mPercentChange);
               System.out.println("Previous Price - " + vStockQuote.mPreviousPrice);
               System.out.println("Region - " + vStockQuote.mRegion);
               System.out.println("Restricted - " + vStockQuote.mRestricted);
               System.out.println("Updated - " + vStockQuote.mUpdated);
               System.out.println("Uptick - " + vStockQuote.mUptick);
               System.out.println("Volume - " + vStockQuote.mVolume);
               System.out.println("_________________________________");
            }
            Thread.sleep(500);
            vStockQuote = vQuote.getStockQuote("EXTR");
            //vStockDetail = vQuote.getStock("EXTR");
            //vStockQuote = vStockDetail.getStock();
            if (vStockQuote != null)
            {
               System.out.println("Ticker - " + vStockQuote.mTicker);
               System.out.println("Alert - " + vStockQuote.mAlert);
               System.out.println("Ask Price - " + vStockQuote.mAskPrice);
               System.out.println("Ask Size - " + vStockQuote.mAskSize);
               System.out.println("Bid Price - " + vStockQuote.mBidPrice);
               System.out.println("Bid Size - " + vStockQuote.mBidSize);
               System.out.println("Change Price - " + vStockQuote.mChangePrice);
               System.out.println("Day High - " + vStockQuote.mDayHigh);
               System.out.println("Day Low - " + vStockQuote.mDayLow);
               System.out.println("Downtick - " + vStockQuote.mDowntick);
               System.out.println("Exchange - " + vStockQuote.mExchange);
               System.out.println("Last Price - " + vStockQuote.mLastPrice);
               System.out.println("Last Trade Volume - " + vStockQuote.mLastTradeVolume);
               System.out.println("Open Price - " + vStockQuote.mOpenPrice);
               System.out.println("Percent Change - " + vStockQuote.mPercentChange);
               System.out.println("Previous Price - " + vStockQuote.mPreviousPrice);
               System.out.println("Region - " + vStockQuote.mRegion);
               System.out.println("Restricted - " + vStockQuote.mRestricted);
               System.out.println("Updated - " + vStockQuote.mUpdated);
               System.out.println("Uptick - " + vStockQuote.mUptick);
               System.out.println("Volume - " + vStockQuote.mVolume);
               System.out.println("_________________________________");
            }
         }
         //Thread.sleep(500000);
         if (vQuote != null)
            vQuote.disconnect();
      }
      catch(Exception e)
      {
         System.out.println("ComstockServer.main() Exception " + e.getMessage());
      }
      */
   }
}
