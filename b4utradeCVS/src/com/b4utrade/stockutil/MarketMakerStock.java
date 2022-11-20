/** Stock.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  4/27/2000
*
*/

package com.b4utrade.stockutil;

import java.util.Hashtable;
import java.util.Vector;
import java.text.*;



/**
 * Holds stock values and does all calculations necessary to update values.
 */
public class MarketMakerStock
{
   private String mTicker = null;
   private int mDecimalPlace = -1;
   private boolean mRestricted = false;
   private boolean topOfTheBookIndicator = false;
   private boolean isRegionalQuote = false;
   
   
   //private MarketMakerActivity mma = new MarketMakerActivity();
   
   private java.util.Hashtable marketMakerMap = new java.util.Hashtable();
   
   public MarketMakerActivity getMarketMaker(String mmId) {
	   
	   MarketMakerActivity marketMakerActivity = (MarketMakerActivity)marketMakerMap.get(mmId);
	   if (marketMakerActivity == null) {
		   marketMakerActivity = new MarketMakerActivity();
		   marketMakerMap.put(mmId,marketMakerActivity);
	   }
	   
	   return marketMakerActivity;
	   
   }

/*
   // Constructor
   public MarketMakerStock(MarketMakerActivity aStock)
   {
      mStock = aStock;
   }

   public MarketMakerActivity getStock()
   {
      return mStock;
   }

   public void clearStockValues()
   {
      mStock.setAll(StockItems.NOACTIVITY, StockItems.NOACTIVITY, StockItems.NOACTIVITY, StockItems.NOACTIVITY, StockItems.NOACTIVITY, false);
   }
*/

   public void setDecimalPlace(String inDecimalPlace)
   {
      try
      {
         mDecimalPlace = Integer.parseInt(inDecimalPlace);
      }
      catch(Exception e)
      {
      }
   }

   public void setDecimalPlace(int inDecimalPlace)
   {
      mDecimalPlace = inDecimalPlace;
   }

   public int getDecimalPlace()
   {
      return mDecimalPlace;
   }

   public void setRestricted(boolean inRestricted)
   {
      mRestricted = inRestricted;
   }

   public boolean getRestricted()
   {
      return mRestricted;
   }


   public void setTicker(String inTicker)
   {
      mTicker = inTicker;
   }

   public String getTicker()
   {
      return mTicker;
   }

/**
 * @return Returns the topOfTheBookIndicator.
 */
public boolean isTopOfTheBookIndicator() {
	return topOfTheBookIndicator;
}

/**
 * @param topOfTheBookIndicator The topOfTheBookIndicator to set.
 */
public void setTopOfTheBookIndicator(boolean topOfTheBookIndicator) {
	this.topOfTheBookIndicator = topOfTheBookIndicator;
}

/**
 * @return Returns the isRegionalQuote.
 */
public boolean isRegionalQuote() {
	return isRegionalQuote;
}

/**
 * @param isRegionalQuote The isRegionalQuote to set.
 */
public void setRegionalQuote(boolean isRegionalQuote) {
	this.isRegionalQuote = isRegionalQuote;
}

}
