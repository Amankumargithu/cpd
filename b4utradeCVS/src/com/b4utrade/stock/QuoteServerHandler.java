/** QuoteServerHandler.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created:  06.09.2000
*
* - 7/19/00 (JT)
*   switch from ObjectOutputStream to OutputStream, ObjectInputStream to InputStream
*/
package com.b4utrade.stock;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

import com.b4utrade.stockutil.MarketMakerStockTopLists;
import com.b4utrade.stockutil.QuoteRequest;
import com.tacpoint.util.Logger;
import com.tacpoint.util.Utility;

/**
 * QuoteServerHandler class is used to handle the requests .
 */
public class QuoteServerHandler extends Object implements Runnable
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A    M E M B E R S
   ////////////////////////////////////////////////////////////////////////////
   private static final boolean mDebug = true;
   private static final String endString = "||";

   private Socket mClient = null;

   private OutputStream mOutputStream = null;
   private InputStream  mInputStream  = null;
   private DataInputStream mIn;
   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////
   public QuoteServerHandler(Socket aClient) throws Exception
   {
      Logger.init();

      // Delete when testing is done!


      try
      {
         mClient = aClient;
         mInputStream = mClient.getInputStream();
         mOutputStream = mClient.getOutputStream();
         mIn = new DataInputStream(mInputStream);
      }
      catch(IOException ioe)
      {
         Logger.log("QuoteServerHandler.constructor() - get input/output stream");
      }
   }

   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////
   public void run()
   {
      try
      {
         boolean vDoneReading = false;

         while (vDoneReading == false)
         {
            vDoneReading = processRequest();
            Thread.yield();
         }
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerHandler.run().while Exception " + e.getMessage());
      }
      finally
      {
         try
         {
            close();
         }
         catch(Exception e)
         {
            Logger.log("QuoteServerHandler.run() finally Exception " + e.getMessage());
         }
      }
   }

   private void close()
   {
      try
      {
         mOutputStream.close();
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerHandler.mOutputStream.close() Exception " + e.getMessage());
      }
      try
      {
         mInputStream.close();
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerHandler.mInputStream.close() Exception " + e.getMessage());
      }
      try
      {
         mClient.close();
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerHandler.mClient.close() Exception " + e.getMessage());
      }

      reset();
   }

   private void reset()
   {
      try
      {
         mOutputStream = null;
         mInputStream = null;
         mClient = null;
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerHandler.reset() Exception " + e.getMessage());
      }
   }
   private boolean processRequest()
   {
      boolean vDone = false;

      try
      {
         QuoteRequest vRequest = null;
         Quotes vQuotes = null;
         byte vStockBytes[] = null;

         String vStrReceived = readInput();
         if ( (vStrReceived != null) && (vStrReceived.length() > 0) )
         {
            vRequest = (QuoteRequest)Utility.byteArrayToObject(vStrReceived.getBytes());
         }

         if (vRequest != null)
         {
            switch(vRequest.getRequest())
            {
               case QuoteRequest.GET_STOCK_QUOTE:
               {
                  vQuotes = Quotes.getInstance();
                  LinkedList stocks = vQuotes.getStockQuote(vRequest.getTickers());

                  try
                  {
                     if (mOutputStream != null)
                     {
                        mOutputStream.write(Utility.objectToByteArray(stocks));
                     }
                     else
                        System.out.println("outputstream null");
                  }
                  catch(IOException ioe)
                  {
                     Logger.log("QuoteServerHandler.processRequest.mOutputStream.write exception " + ioe.getMessage());
                  }
               }
               break;

               

               case QuoteRequest.GET_MARKET_MAKER_LISTS:
               {
                  //Logger.log("QuoteServerHandler.processRequest() : get MarketMakerlists ");
                  DefaultQuotes vOtherQuotes = MarketMakerQuotes.getInstance();
                  MarketMakerStockTopLists vStockLists = (MarketMakerStockTopLists)vOtherQuotes.getStockQuote(vRequest.getTickers());
                  //Logger.log("QuoteServerHandler.processRequest() : finished get MarketMakerlists ");
                  if ( (vStockLists != null) && (mOutputStream != null) )
                  {
                     //vStockBytes = StockStreamer.changeStockObjectToStream((DefaultStockObject)vStockLists);

                     LinkedList stocks = new LinkedList();
                   //  if (vStockBytes != null)
                   //  {
                       //Logger.log("QuoteServerHandler.processRequest() : able to add");
                       stocks.add(vStockLists);
                   //  }
                     mOutputStream.write(Utility.objectToByteArray(stocks));
                  }
               }
               break;
            } // end switch

            if (mOutputStream != null)
               mOutputStream.flush();
            else
               System.out.println("outputstream null");
         }
      }
      catch(Exception e)
      {
         //close();
         vDone = true;
         Logger.log("QuoteServerHandler.processRequest() " + e.getMessage());
      }

      return (vDone);
   }
   private String readInput()
   {
      String sb = null;

      try
      {
         byte[] response = new byte[mIn.available()];

         mIn.readFully(response);
         if (response != null) {
            sb = new String(response);
         }
      }
      catch(IOException ioe)
      {
         // don't log, this happens very, very often.
      }
      catch(Exception e)
      {
         close();
         Logger.log("QuoteServerHandler.processRequest() " + e.getMessage());
      }

      return(sb);
   }
/*   private boolean processRequest()
   {
      boolean vDone = false;
      try
      {
         QuoteRequest vRequest = null;
         try
         {
            if (mInputStream != null)
            {
               try
               {
                  int vInByteCount = 0;
                  byte[] vInByteStr = new byte[1024];
                  String vRequestDataString = null;

                  String vBufferString = "";
                  boolean vDoneReading = false;

                  while (! vDoneReading)
                  {
                     try
                     {
                        vInByteCount = mInputStream.read(vInByteStr);
                     }
                     catch(IOException ioe)
                     {
                        Logger.log("QuoteServerHandler.processRequest.read.mInputStream exception");
                        close();
                        vDone = true;
                     }

                     String vReadString = new String(vInByteStr, 0, vInByteCount);
                     vBufferString += vReadString;

                     boolean vFullRecord = false;
                     int vEndIndex = vReadString.lastIndexOf("||");
                     if (vEndIndex == vReadString.length() - 2)
                           vFullRecord = true;

                     StringTokenizer vToken = new StringTokenizer(vBufferString, "||");
                     while (vToken.hasMoreTokens())
                     {
                        vRequestDataString = new String(vToken.nextToken());
                        if (vToken.hasMoreTokens() || vFullRecord)
                        {
                           vRequest = QuoteRequest.changeStreamToRequest(vRequestDataString);
                           vBufferString = "";
                        }
                        else
                        {
                           // save for next read
                           vBufferString = vRequestDataString;
                        }
                     } // end while
                     vDoneReading = true;

                  } // end while done reading
               }
               catch(Exception e)
               {
                  vDone = true;
                  close();
               }
            }
         }
         catch(Exception e2)
         {
            Logger.log("ProcessRequest.mObjectInputStream.readObject exception");
            close();
            vDone = true;
         }

         if ( (vRequest != null) && (mOutputStream != null) )
         {
            vDone = sendRequest(vRequest);
         }
         else
         {
             close();
             vDone = true;
         }
      }
      catch(Exception e)
      {
         close();
         vDone = true;
         Logger.log("QuoteServerHandler.processRequest() " + e.getMessage());
      }

      return (vDone);
   }

   private boolean sendRequest(QuoteRequest aRequest)
   {
      boolean vDone = false;
      if (aRequest == null)
      {
         close();
         return true;
      }

      Quotes vQuotes = null;
      DefaultQuotes vOtherQuotes = null;
      byte vStockBytes[] = null;
      try
      {
         switch(aRequest.getRequest())
         {
         case QuoteRequest.GET_STOCK_QUOTE:
            vQuotes = Quotes.getInstance();
            vStockBytes = vQuotes.getStockQuote(aRequest.getTicker());
            if (vStockBytes != null)
            {
               mOutputStream.write(vStockBytes);
               mOutputStream.write(endString.getBytes());
            }
            break;

         case QuoteRequest.GET_ECNSTOCK_QUOTE:
            vOtherQuotes = ECNQuotes.getInstance();
            ECNStockActivity vECNStockActivity = (ECNStockActivity)vOtherQuotes.getStockQuote(aRequest.getTicker());
            vStockBytes = StockStreamer.changeStockObjectToStream(
                        (DefaultStockObject)vECNStockActivity);

            if (vStockBytes != null)
            {
               mOutputStream.write(vStockBytes);
            }
            break;

         case QuoteRequest.GET_MARKET_MAKER_LISTS:
            vOtherQuotes = MarketMakerQuotes.getInstance();
            MarketMakerStockTopLists vStockLists = (MarketMakerStockTopLists)vOtherQuotes.getStockQuote(aRequest.getTicker());
            vStockBytes = StockStreamer.changeStockObjectToStream(
                        (DefaultStockObject)vStockLists);

            if (vStockBytes != null)
            {
               mOutputStream.write(vStockBytes);
            }
            break;

         default:
            break;
         }
         mOutputStream.flush();
      }
      catch(IOException ioe)
      {
         Logger.log("QuoteServerHandler.sendRequest.writeObject:" + ioe.getMessage());
         close();
         vDone = true;
      }

      return vDone;
   }

   public static void main(String[] args)
   {
      try
      {
//         Socket vClient = new Socket();
//         QuoteServerHandler vQuoteServer = new QuoteServerHandler(vClient);
//         vQuoteServer.run();
      }
      catch(Exception e)
      {
         System.out.println("QuoteServerHandler.main() Exception " + e.getMessage());
      }
   }*/
}
