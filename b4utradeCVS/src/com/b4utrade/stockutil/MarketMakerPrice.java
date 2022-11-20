/**
  * MarketMakerPrice.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kimg@tacpoint.com
  * @version 1.0
  * Date created:  5/9/2000
  */
package com.b4utrade.stockutil;



public class MarketMakerPrice extends DefaultStockObject 
{
   String mPrice = StockItems.NOACTIVITY;
   String mSize = StockItems.NOACTIVITY;
   String mMarketMakerID = StockItems.NOACTIVITY;
   double priceAsDouble = 0d;
   int sizeAsInt = 0;
   String upc = StockItems.MM2_UPC_RESET;
   long time = 0l;
   String timeInString = StockItems.NOACTIVITY;

   public void formatTime(java.text.SimpleDateFormat sdf)
   {
	   
	   long hour = time/60;
	   long minute = time % 60;
	   
	   StringBuffer formattedTime = new StringBuffer();
	   	   
	   if (hour < 10) {
		   formattedTime.append("0");
	   }
	   formattedTime.append(String.valueOf(hour));
	   
	   formattedTime.append(":");
	   
	   if (minute < 10) {
		   formattedTime.append("0");
	   }
	   formattedTime.append(String.valueOf(minute));
	   
	   setTimeInString(formattedTime.toString());
	  
   }
   public void setTimeInString(String inTime)
   {
       timeInString = inTime;
   }
   public String getTimeInString()
   {
       return timeInString;
   }
   public void setTime(long time)
   {
      this.time = time;
   }

   public long getTime()
   {
      return time;
   }


   public void setUPC(String upc)
   {
      this.upc = upc;
   }

   public String getUPC()
   {
      return upc;
   }

   public void setPriceAsDouble(double inPrice)
   {
      priceAsDouble = inPrice;
   }

   public double getPriceAsDouble()
   {
      return priceAsDouble;
   }


   public void setPrice(String inPrice)
   {
      mPrice = inPrice;
   }

   public String getPrice()
   {
      return mPrice;
   }

   public void setSize(String inSize)
   {
      mSize = inSize;
   }

   public String getSize()
   {
      return mSize;
   }

   public void setMarketMakerID(String inMarketMakerID)
   {
      mMarketMakerID = inMarketMakerID;
   }

   public String getMarketMakerID()
   {
      return mMarketMakerID;
   }

   public boolean equals(Object o) {
      if (this.mMarketMakerID.trim().equals(((MarketMakerPrice)o).mMarketMakerID.trim()))
         return true;
      return false;
   }
   
   public int getSizeAsInt() {
	   return sizeAsInt;
   }
   public void setSizeAsInt(int sizeAsInt) {
	   this.sizeAsInt = sizeAsInt;
   }


}
