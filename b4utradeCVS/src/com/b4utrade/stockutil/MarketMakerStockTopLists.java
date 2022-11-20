
package com.b4utrade.stockutil;

import com.b4utrade.helper.*;
import java.util.*;
import com.tacpoint.util.*;
import java.math.BigDecimal;


public class MarketMakerStockTopLists extends DefaultStockObject
{
   private String ticker = null;
   private Vector bids = null;
   private Vector asks = null;
   private boolean restricted = false;
   private int maxObjects = 15;
   private transient MarketMakerAskComparator marketMakerAskComparator = new MarketMakerAskComparator();
   private transient MarketMakerBidComparator marketMakerBidComparator = new MarketMakerBidComparator();

   public MarketMakerStockTopLists()
   {
      mStockType = StockTypeConstants.MARKETMAKER_TOP_LISTS;
      maxObjects += 5;

      bids = new Vector(maxObjects+1);
      asks = new Vector(maxObjects+1);

   }

   public MarketMakerStockTopLists(int maxObjects)
   {
      mStockType = StockTypeConstants.MARKETMAKER_TOP_LISTS;
      if (maxObjects > 0)
         this.maxObjects = maxObjects + 5;

      bids = new Vector(maxObjects+1);
      asks = new Vector(maxObjects+1);

   }

   public void setTicker(String ticker)
   {
      this.ticker = ticker;
   }

   public String getTicker()
   {
      return ticker;
   }

   public Vector getTopBidsList()
   {
      return bids;
   }

   public Vector getTopAsksList()
   {
      return asks;
   }

   public void setListsMaxNumber(int inListsMaxNumber)
   {
      maxObjects = inListsMaxNumber;
   }

   public int getListsMaxNumber()
   {
      return maxObjects;
   }

   public void setRestricted(boolean inRestricted)
   {
      restricted = inRestricted;
   }

   public boolean getRestricted()
   {
      return restricted;
   }

   public void updateStock(MarketMakerActivity stock)
   {
      if (stock == null) return;

      if (ticker == null || ticker.length() == 0)
         ticker = stock.getTicker();

      if (stock.getRestricted())
         restricted = true;
      else
         restricted = false;

      updateTopBidList(stock);
      updateTopAskList(stock);

   }

   private void updateTopBidList(MarketMakerActivity mma)
   {
      if (mma == null) return;

      String mmid;
      if ( (mmid = mma.getMarketMakerID()) == null) return;

      if (mma.getBid() == null) return;
      if (mma.getBid().equals(StockItems.NOACTIVITY)) return;

      double bidPrice = getPriceInDecimal(mma.getBid());
      //double askPrice = getPriceInDecimal(mma.getAsk());
/*
      // If only ASK activity, don't do anything
      if (bidPrice < 0.001 && askPrice > 0.001)
         return;

      // If only bid/ask size activity, don't do anything
      if (bidPrice < 0.001 && askPrice < 0.001) {
         if (!("0".equals(mma.getBidSize())))
            return;
         if (!("0".equals(mma.getAskSize())))
            return;
      }
*/
      if (bidPrice < -0.005d) return;

      MarketMakerPrice mmp = new MarketMakerPrice();
      mmp.setPrice(mma.getBid());
      mmp.setSize(mma.getBidSize());
      mmp.setMarketMakerID(mmid);
      mmp.setPriceAsDouble(bidPrice);
      mmp.setUPC(mma.getUPC());
      mmp.setTime(mma.getTime());

      bids.remove(mmp);

      if (bidPrice <= 0.001) {
         return;
      }

      bids.addElement(mmp);

      Collections.sort(bids, marketMakerBidComparator);

      if (bids.size() > maxObjects)
         bids.removeElementAt(maxObjects);

   }

   private void updateTopAskList(MarketMakerActivity mma)
   {
      if (mma == null) return;

      String mmid;
      if ( (mmid = mma.getMarketMakerID()) == null) return;

      if (mma.getAsk() == null) return;
      if (mma.getAsk().equals(StockItems.NOACTIVITY)) return;

      double askPrice = getPriceInDecimal(mma.getAsk());
      //double bidPrice = getPriceInDecimal(mma.getBid());
/*
      // If only BID activity, don't do anything
      if (askPrice < 0.001 && bidPrice > 0.001)
         return;

      // If only bid/ask size activity, don't do anything
      if (askPrice < 0.001  && bidPrice < 0.001) {
         if (!("0".equals(mma.getBidSize())))
            return;
         if (!("0".equals(mma.getAskSize())))
            return;
      }
*/
      if (askPrice < -0.005d) return;

      MarketMakerPrice mmp = new MarketMakerPrice();
      mmp.setPrice(mma.getAsk());
      mmp.setSize(mma.getAskSize());
      mmp.setMarketMakerID(mmid);
      mmp.setPriceAsDouble(askPrice);
      mmp.setUPC(mma.getUPC());
      mmp.setTime(mma.getTime());

      asks.remove(mmp);

      if (askPrice <= 0.001) {
         return;
      }

      asks.addElement(mmp);

      Collections.sort(asks, marketMakerAskComparator);

      if (asks.size() > maxObjects)
         asks.removeElementAt(maxObjects);
   }

   private double getPriceInDecimal(String price) {

      if (price == null || price.length() == 0)
         return 0.0;

      double decimalPrice = 0.0;

      try {

         if (price.indexOf('.') >= 0)
            decimalPrice = Double.parseDouble(price);
         else
            decimalPrice = Fraction.convertToDecimal(price);

         BigDecimal bigDecimal = new BigDecimal(decimalPrice);
         bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
         decimalPrice = bigDecimal.doubleValue();
      }
      catch(Exception e) {
         decimalPrice = -1d;
      }

      return decimalPrice;
   }
}


