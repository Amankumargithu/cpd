/** QuoteServerProxy.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 06.09.2000
* Date modified:
* - 7/19/00 (JT)
*   switch from ObjectOutputStream to OutputStream, ObjectInputStream to InputStream
*/

package com.b4utrade.stock;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

import com.b4utrade.stockutil.QuoteRequest;
import com.tacpoint.util.Logger;
import com.tacpoint.util.Utility;


/** QuoteServerProxy class
* The QuoteServerProxy class communicate with the QuoteServerImpl to execute the method
*/
public class QuoteServerProxy extends Object
{

   private ByteArrayOutputStream baos = new ByteArrayOutputStream();
   private byte[] bytes = new byte[1024];

   private static final boolean mDebug = true;

   private Socket mClient;

   private InputStream mInputStream;
   private OutputStream mOutputStream;
   private DataInputStream mIn;

   private boolean mConnected;
   private String mIPAddress = "localhost";

   public QuoteServerProxy(String aIPAddress)
   {
      try
      {
         Logger.init();

         Logger.log("QuoteServerProxy constructor " + aIPAddress);
         if (aIPAddress != null || aIPAddress.trim().length() != 0)
            mIPAddress = aIPAddress;
         connect();
      }
      catch (Exception error)
      {
         mClient = null;
         Logger.log(error.getMessage());
      }
   }

   public void connect()
   {
      Logger.log("QuoteServerProxy.connect");
      if ( (mConnected == false) || (mClient == null) )
      {
         try
         {
            mClient = new Socket(mIPAddress, QuoteServerImpl.gPortNumber);

            Logger.log("QuoteServerProxy.connect(): ip: "+ mIPAddress +" portnumber: " + QuoteServerImpl.gPortNumber );
            mOutputStream = mClient.getOutputStream();
            mInputStream  = mClient.getInputStream();
            mIn = new DataInputStream(mInputStream);

            mConnected = true;
            Logger.log("QuoteServerProxy.connect is true");

         }
         catch (Exception error)
         {
            error.printStackTrace();
            resetSocket();
            Logger.log(error.getMessage());
         }
      }
   }

   public boolean connected()
   {
      return mConnected;
   }

   public void closeConnection()
   {
      //Logger.log("QuoteServerProxy.closeConnection");
      try
      {
         if (mInputStream != null)
            mInputStream.close();
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerProxy.closeConnection.closeInputStream(): " + e.getMessage());
      }
      try
      {
         if (mOutputStream != null)
            mOutputStream.close();
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerProxy.closeConnection().closeOutputStream(): " + e.getMessage());
      }
      try
      {
         if (mClient != null)
            mClient.close();
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerProxy.closeConnection().closeClient(): " + e.getMessage());
      }

      resetSocket();
   }

   public LinkedList read()
   {


      LinkedList vStocks = null;

      try
      {
         boolean done = false;
         int counter = 0;
         while (! done)
         {
            //Logger.log("QuoteServerProxy.read(): reading is not done: "+ counter );
            counter++;
            if (mIn.available() > 0)
            {
               //Logger.log("QuoteServerProxy.read(): mIn is avalable ");
               byte[] response = new byte[mIn.available()];

               //Logger.log("QuoteServerProxy.read(): start to read: ");
               mIn.readFully(response);
               //Logger.log("QuoteServerProxy.read():  finished reading : response: " + response);
               if (response != null)
               {
                  vStocks = (LinkedList)Utility.byteArrayToObject(response);

                  done = true;
               }
            }
            else {
            try{
                Thread.sleep(0,5);
            } catch(Exception e){}
            }
            // if sockets screwed up, then get out of loop.
            if ( (mIn == null) || (mOutputStream == null) )
               done = true;
            if (counter > 60)
              throw (new Exception("Server cannot connect: time expired"));
         }
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerProxy.read() exception: " + e.getMessage(), e);

         closeConnection();

         // reconnect
         connect();
      }


      return(vStocks);
   }

   public boolean write(QuoteRequest aRequest)
   {
      //Logger.log("QuoteServerProxy.write()");
      boolean vSuccess = false;

      try
      {
         mOutputStream.write(Utility.objectToByteArray(aRequest));
         mOutputStream.flush();
         vSuccess = true;
      }
      catch(Exception e)
      {
         Logger.log("QuoteServerProxy.write() exception: " + e.getMessage());
         vSuccess = false;
         closeConnection();

         // reconnect
         connect();
      }

      return(vSuccess);
   }

   private boolean socketAlive()
   {
      if ( (mConnected == false)  || (mClient == null) ||
           (mInputStream == null) || (mOutputStream == null) )
      {
         return(false);
      }
      else
      {
         return (true);
      }
   }

   public void resetSocket()
   {
      mConnected = false;
      mClient = null;
      mInputStream = null;
      mOutputStream = null;
   }


}

