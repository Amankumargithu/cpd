/** QuoteServerManager.java
 * Copyright: Tacpoint Technologies, Inc. (c) 2000.
 * All rights reserved.
 * @author John Tong
 * @author jtong@tacpoint.com
 * @version 1.0
 * Date created: 06.09.2000
 * Date modified:
 */

package com.b4utrade.stock;

import java.util.*;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Logger;
import com.b4utrade.stockutil.*;


/** QuoteServerManager class
* The QuoteServerHandler class will have the be managing all of the individual stock
* instances.  The function is common in many of the specific QuoteServers which are singleton objects.
*/
public final class QuoteServerManager
{

   protected static Hashtable mStocks = new Hashtable();

   // Constructor
   public QuoteServerManager()
   {
   }

   public LinkedList<byte[]>  getStockQuote(String aTickers)
   {
      // use linkedlist instead of vector because vectors can't be
      // serialized and pass over the sockets correctly.
      LinkedList<byte[]> stocks = new LinkedList<byte[]>();
      byte[] stockStream = null;
      StringTokenizer st = new StringTokenizer(aTickers, ",");
      while (st.hasMoreTokens())
      {
         // retrieve data from quote server
         String ticker = st.nextToken();
         if (mStocks.containsKey(ticker))
         {
            stockStream = ((byte[])mStocks.get(ticker));
            stocks.add(stockStream);
         }
      }
      return(stocks);
   }


   public byte[] getSingleStockQuote(String aTicker)
   {
      byte[] stockStream = null;
      if (mStocks.containsKey(aTicker))
      {
         stockStream = (byte[])mStocks.get(aTicker);
      }

      return(stockStream);
   }

   public Object getSingleStockObject(String aTicker)
   {
      Object stockobj = null;
      if (mStocks.containsKey(aTicker))
      {
         stockobj = mStocks.get(aTicker);
      }

      return(stockobj);
   }


   public void setStockQuote(byte[] aStock, String ticker)
   {
      if (aStock == null)
         return;
      if (ticker != null && ticker.length() > 0)
         setStock(ticker, aStock);

   }

   public void setStock(String aTicker, byte[] aStock)
   {

      mStocks.put(aTicker, aStock);
   }

   public void setStockObject(String aTicker, Object aStock)
   {

      mStocks.put(aTicker, aStock);
   }

   public Enumeration getStockEnumeration()
   {
      return mStocks.elements();
   }
   
   public int getSize()
   {
      if (mStocks != null)
      {
         return (mStocks.size());
      }
      
      return 0;
   }
   
}
