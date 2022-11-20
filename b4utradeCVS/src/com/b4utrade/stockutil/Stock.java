
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

import java.util.HashMap;
import java.text.*;
import com.tacpoint.util.Fraction;


/**
 * Holds stock values and does all calculations necessary to update values.
 */
public class Stock
{
   private StockActivity mStock = null;

   private long mLongVolume = 0;
   private Fraction mPrevPriceFraction = null;
   private double mPrevPriceDecimal = 0.0;
   private int mDecimalPlace = -1;

   // Constructor
   public Stock(StockActivity aStock)
   {
      mStock = aStock;
   }

   public StockActivity getStock()
   {
      return mStock;
   }

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
/*      if (mDecimalPlace < 0 && mPrevPriceDecimal > 0)
      {
         // Correct last price
         mPrevPriceFraction = null;
         double vDivisor = getDivisor(inDecimalPlace);
         if (vDivisor > 0)
            mPrevPriceDecimal = mPrevPriceDecimal/vDivisor;
      }*/
      mDecimalPlace = inDecimalPlace;
   }

   public int getDecimalPlace()
   {
      return mDecimalPlace;
   }

   public void setPreviousPrice(String aPreviousPrice)
   {
      if (aPreviousPrice == null || aPreviousPrice.length() == 0)
      {
         mStock.mPreviousPrice = StockItems.NOACTIVITY;
         mPrevPriceFraction = null;
         mPrevPriceDecimal = 0.0;
         return;
      }
      try
      {
         if (mDecimalPlace >= 0 || aPreviousPrice.indexOf('.') >= 0)
         {
            // These are decimal prices
            Double vDouble = new Double(aPreviousPrice);
            mPrevPriceDecimal = vDouble.doubleValue();
            mPrevPriceFraction = null;
            mStock.mPreviousPrice = aPreviousPrice;
         }
         else
         {
            // These are fractional pricess
            mPrevPriceFraction = new Fraction(aPreviousPrice);
            mPrevPriceDecimal = mPrevPriceFraction.getDecimal();
            mStock.mPreviousPrice = aPreviousPrice;
         }
      }
      catch (Exception e)
      {
         mStock.mPreviousPrice = StockItems.NOACTIVITY;
         mPrevPriceFraction = null;
         mPrevPriceDecimal = 0.0;
      }
   }

   public void calculateChange(String aPrice)
   {
      if (aPrice == null || aPrice.length() == 0)
         return;

      String vPreviousPrice = mStock.mPreviousPrice;
      if (vPreviousPrice.equalsIgnoreCase(StockItems.NOACTIVITY))
      {
         mStock.mLastPrice = aPrice;
         mStock.mPercentChange = StockItems.NOACTIVITY;
         mStock.mChangePrice = StockItems.NOACTIVITY;
         return;
      }

      if (mDecimalPlace >= 0 || aPrice.indexOf('.') >= 0)
         calculateDecimalChange(aPrice);
      else
         calculateFractionChange(aPrice);

   }

   private void calculateFractionChange(String aPrice)
   {
      try
      {
         mStock.mLastPrice = aPrice;
         if (mPrevPriceFraction == null)
         {
            mPrevPriceFraction = new Fraction(mStock.mPreviousPrice);
            mPrevPriceDecimal = mPrevPriceFraction.getDecimal();
         }
         // ChangePrice = aPrice - previous day
         Fraction vNewPrice = new Fraction(aPrice);
         Fraction vChange = vNewPrice.subtract(mPrevPriceFraction);
//         Fraction vChange = mPrevPriceFraction.subtract(vNewPrice);
         mStock.mChangePrice = vChange.toString();

         // PercentChange =  (change / prev day's close) * 100
         double vPercent = 0.0;
         if (mPrevPriceDecimal != 0)
            vPercent = (vChange.getDecimal() / mPrevPriceDecimal) * 100.0;
         // Format to 2 decimal places
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMinimumFractionDigits(0);
         nf.setMaximumFractionDigits(2);
         mStock.mPercentChange = nf.format(vPercent);
      }
      catch (Exception e)
      {
         mStock.mLastPrice = aPrice;
         mStock.mPercentChange = StockItems.NOACTIVITY;
         mStock.mChangePrice = StockItems.NOACTIVITY;
      }

   }

   private void calculateDecimalChange(String aPrice)
   {
      Double vDouble = null;

      try
      {
         mStock.mLastPrice = aPrice;
         if (mPrevPriceDecimal <= 0.0)
         {
            vDouble = new Double(mStock.mPreviousPrice);
            mPrevPriceDecimal = vDouble.doubleValue();
            mPrevPriceFraction = null;
         }

         //NumberFormat nf = NumberFormat.getInstance();
         java.text.DecimalFormat nf = new DecimalFormat("############.##");
         nf.setMinimumFractionDigits(2);
         nf.setMaximumFractionDigits(2);
         nf.setMinimumIntegerDigits(1);
         
        
         

         // ChangePrice = aPrice - previous day
         vDouble = new Double(aPrice);
         double vPrice = vDouble.doubleValue();
         double vChange = vPrice - mPrevPriceDecimal;
         mStock.mChangePrice = nf.format(vChange);
         

         // PercentChange =  (change / prev day's close) * 100
         double vPercent = 0.0;
         if (mPrevPriceDecimal != 0)
            vPercent = (vChange / mPrevPriceDecimal) * 100.0;
         // Format to 2 decimal places
         nf.setMaximumFractionDigits(2);
         mStock.mPercentChange = nf.format(vPercent);
      }
      catch (Exception e)
      {
         mStock.mLastPrice = aPrice;
         mStock.mPercentChange = StockItems.NOACTIVITY;
         mStock.mChangePrice = StockItems.NOACTIVITY;
      }

   }

   public void calculatePercentChange(String aChange)
   {
      if (aChange == null || aChange.length() == 0)
         return;

      String vPreviousPrice = mStock.mPreviousPrice;
      if (vPreviousPrice.equalsIgnoreCase(StockItems.NOACTIVITY))
      {
         mStock.mPercentChange = StockItems.NOACTIVITY;
         return;
      }

      if (mDecimalPlace >= 0 || aChange.indexOf('.') >= 0)
         calculateDecimalPercentChange(aChange);
      else
         calculateFractionPercentChange(aChange);

   }

   private void calculateFractionPercentChange(String aChange)
   {
      try
      {
         if (mPrevPriceFraction == null)
         {
            mPrevPriceFraction = new Fraction(mStock.mPreviousPrice);
            mPrevPriceDecimal = mPrevPriceFraction.getDecimal();
         }
         // ChangePrice = aPrice - previous day
         Fraction vChange = new Fraction(aChange);

         // PercentChange =  (change / prev day's close) * 100
         double vPercent = 0.0;
         if (mPrevPriceDecimal != 0)
            vPercent = (vChange.getDecimal() / mPrevPriceDecimal) * 100.0;
         // Format to 2 decimal places
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMinimumFractionDigits(0);
         nf.setMaximumFractionDigits(2);
         mStock.mPercentChange = nf.format(vPercent);
      }
      catch (Exception e)
      {
         mStock.mPercentChange = StockItems.NOACTIVITY;
      }
   }

   private void calculateDecimalPercentChange(String aChange)
   {
      Double vDouble = null;

      try
      {
         if (mPrevPriceDecimal <= 0.0)
         {
            vDouble = new Double(mStock.mPreviousPrice);
            mPrevPriceDecimal = vDouble.doubleValue();
         }

         vDouble = new Double(aChange);
         double vChange = vDouble.doubleValue();

         // PercentChange =  (change / prev day's close) * 100
         double vPercent = 0.0;
         if (mPrevPriceDecimal != 0)
            vPercent = (vChange / mPrevPriceDecimal) * 100.0;
         // Format to 2 decimal places
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMinimumFractionDigits(2);
         nf.setMaximumFractionDigits(2);
         nf.setMinimumIntegerDigits(1);

         mStock.mPercentChange = nf.format(vPercent);

         if (mStock.mPercentChange.indexOf(",") >= 0)
            mStock.mPercentChange = StockItems.NOACTIVITY;
      }
      catch (Exception e)
      {
         mStock.mPercentChange = StockItems.NOACTIVITY;
      }

   }

   public void calculateVolume(String aTradeVolume)
   {
      if (aTradeVolume == null || aTradeVolume.length() == 0)
         return;

      try
      {
         long vVolume = Long.parseLong(aTradeVolume);
         mLongVolume += vVolume;
         mStock.mLastTradeVolume  = aTradeVolume;
         mStock.mVolume = String.valueOf(mLongVolume);
      }
      catch (Exception e)
      {
      }
   }

   public void setVolume(String aTotalVolume)
   {
      if (aTotalVolume == null || aTotalVolume.length() == 0)
      {
         mStock.mVolume = StockItems.NOACTIVITY;
         mLongVolume = 0;
         return;
      }

      try
      {
         long vVolume = Long.parseLong(aTotalVolume);
         mLongVolume = vVolume;
         mStock.mVolume = aTotalVolume;
      }
      catch (Exception e)
      {
      }
   }

   public void processStartOfDay()
   {
      // Set volume
      mStock.mVolume = "0";
      mLongVolume = 0;

      // Set net change
      mStock.mChangePrice = "0";
      mStock.mPercentChange = "0";

      mStock.mDayHigh = StockItems.NOACTIVITY;
      mStock.mDayLow = StockItems.NOACTIVITY;
      mStock.mOpenPrice = StockItems.NOACTIVITY;

      // Set previous price
      mStock.mPreviousPrice = mStock.mLastPrice;
      try
      {
         if (!(mStock.mLastPrice.equalsIgnoreCase(StockItems.NOACTIVITY)))
         {
            if (mDecimalPlace >= 0 || mStock.mLastPrice.indexOf('.') >= 0)
            {
               Double vDouble = new Double(mStock.mPreviousPrice);
               mPrevPriceDecimal = vDouble.doubleValue();
            }
            else
            {
               mPrevPriceFraction = new Fraction(mStock.mLastPrice);
               mPrevPriceDecimal = mPrevPriceFraction.getDecimal();
            }
         }
         else
         {
             mPrevPriceFraction = null;
             mPrevPriceDecimal = 0.0;
         }
      }
      catch (Exception e)
      {
          mPrevPriceFraction = null;
          mPrevPriceDecimal = 0.0;
      }

   }

   private double getDivisor(int aDecimalPlace)
   {
      if (aDecimalPlace <= 0)
         return 0;
      int vDivisor = 1;
      for (int i=0; i < aDecimalPlace; i++)
      {
         vDivisor *= 10;
      }
      return (double)vDivisor;
   }

}

