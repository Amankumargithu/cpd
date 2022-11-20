/** QuoteServerImpl.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created:  06.09.2000
*
*/
package com.b4utrade.stock;

import java.io.*;
import java.net.*;
import java.util.*;
import com.tacpoint.util.*;
import com.tacpoint.configuration.*;


/**
 * QuoteServerImpl class is used to get stock from Quotes object.
 */
public class QuoteServerImpl extends Object implements Runnable
{
   private static final boolean mDebug = true;

   public static final int gPortNumber = 8415;

   private Socket mClient = null;
   private ServerSocket mServer = null;

   private QuoteServerHandler mCurrentHandler;

   public QuoteServerImpl() throws Exception
   {
      Logger.init();

      // Delete when testing is done!
   }

   public void run()
   {
      try
      {
         boolean vDone = false;

         mServer = new ServerSocket(gPortNumber);

         while(vDone == false)
         {
            mClient = mServer.accept();

            QuoteServerHandler vHandler = new QuoteServerHandler(mClient);
            Thread vHandlerThread = new Thread(vHandler);
            vHandlerThread.start();
//            vHandlerThread.run();
         }
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerImpl.run().while Exception " + e.getMessage());
      }
      finally
      {
         try
         {
            mServer.close();
         }
         catch(Exception e)
         {
            Logger.log("QuoteServerImpl.run() finally Exception " + e.getMessage());
         }
      }
   }

   public static void main(String[] args)
   {
      try
      {
         QuoteServerImpl vQuoteServer = new QuoteServerImpl();
         vQuoteServer.run();
      }
      catch(Exception e)
      {
         System.out.println("QuoteServerImpl.main() Exception " + e.getMessage());
      }
   }
}
