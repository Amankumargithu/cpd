/** Quotes.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 06.09.2000
* Date modified:
*/

package com.b4utrade.stock;

import java.util.Enumeration;
import java.util.LinkedList;

import com.tacpoint.util.Logger;


/** Quotes class
* The Quotes class will have the be managing all of the individual stock
* instances.  This is a singleton class so the servlets can have easy access to
* this class at anytime.
*/
public final class Quotes
{
   private boolean mDebug = true;
   private static Quotes mQuotes = new Quotes();
   private QuoteServerManager mManager = new QuoteServerManager();
   private boolean mLogger = false;
   

   // Constructor
   private Quotes()
   {
      try
      {
         Logger.init();
         mLogger = true;
      }
      catch(Exception e)
      {
      }
   }

   public static Quotes getInstance()
   {
      return mQuotes;
   }
   public LinkedList<byte[]>  getStockQuote(String aTickers)
   {
      return(mManager.getStockQuote(aTickers));
   }

   public byte[] getSingleStockQuote(String aTicker)
   {
      return(mManager.getSingleStockQuote(aTicker));
   }
   
   public void setStockQuote(byte[] aStock, String ticker)
   {
      if (aStock == null)
         return;
      if (ticker != null && ticker.length() > 0)
         mManager.setStockQuote(aStock, ticker);

   }

   public void setStock(String aTicker, byte[] aStock)
   {
      if (aTicker == null || aStock == null)
      {
        return;
      }
      
      mManager.setStock(aTicker, aStock);
   }

   public Enumeration getStockEnumeration()
   {
      return (mManager.getStockEnumeration());
   }

}
