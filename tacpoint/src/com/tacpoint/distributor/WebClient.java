/** WebClient.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 02.07.2000
* Date modified:
*/

package com.tacpoint.distributor;

import java.io.*;
import java.net.Socket;
import com.tacpoint.util.*;

/** WebClient class
* The WebClient class represents the socket class and the object input/output streams
*/
public class WebClient extends Object
{
   private static final boolean mDebug = false;

   // Constructor
   public WebClient(String aIPAddress, int aPort)
   {
      try
      {
         Logger.init();
         
         // Delete when testing is done!
         Logger.debug("WebClient Constructor " + aIPAddress + ":" + aPort, mDebug);

         mConnected = false;
         mIPAddress = aIPAddress;
         mPort = aPort;

         connect();
      }
      catch (Exception error)
      {
         mWebClient = null;
         Logger.log(error.getMessage());
      }
   }

   public void connect()
   {
      Logger.debug("IN WebClient connect", mDebug);

      if ( (mConnected == false) || (mWebClient == null) )
      {
         try
         {
            mWebClient = new Socket(mIPAddress, mPort);

            mInputStream = mWebClient.getInputStream();
            mOutputStream = mWebClient.getOutputStream();

/*            mObjectInputStream = new ObjectInputStream(
               new BufferedInputStream(mInputStream));
            mObjectOutputStream = new ObjectOutputStream(
               new BufferedOutputStream(mOutputStream));
*/
            mConnected = true;
         }
         catch (Exception error)
         {
            resetSocket();
//          Logger.log(error.getMessage());
         }
      }
   }

   public void reconnect()
   {
      connect();
   }

   public boolean connected()
   {
      return mConnected;
   }

   // all the gets
   public String getIPAddress()
   {
      return(mIPAddress);
   }

   public InputStream getInputStream()
   {
      if (socketAlive() == false)
      {
         connect();
      }
      if ( (mConnected == false) || (mInputStream == null) )
      {
         resetSocket();
      }
      return(mInputStream);
   }

   public OutputStream getOutputStream()
   {
      if (socketAlive() == false)
         connect();
      
      if ( (mConnected == false) || (mOutputStream == null) )
         resetSocket();

      return(mOutputStream);
   }

   public ObjectInputStream getObjectInputStream()
   {
      if ( (socketAlive() == false) || (mObjectInputStream == null) )
         connect();
     
      if ( (mConnected == false) || (mObjectInputStream == null) )
         resetSocket();
      
      return(mObjectInputStream);
   }

   public ObjectOutputStream getObjectOutputStream()
   {
      if ( (socketAlive() == false) || (mObjectOutputStream == null) )
         connect();
     
      if ( (mConnected == false) || (mObjectOutputStream == null) )
         resetSocket();
      
      return(mObjectOutputStream);
   }

   private boolean socketAlive()
   {
      if ( (mConnected == false)  || (mWebClient == null) ||
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
      mWebClient = null;
      mInputStream = null;
      mOutputStream = null;
      mObjectInputStream = null;
      mObjectOutputStream = null;
   }

   private String mIPAddress;
   private int mPort;
   private InputStream mInputStream;
   private OutputStream mOutputStream;
   private ObjectInputStream mObjectInputStream;
   private ObjectOutputStream mObjectOutputStream;
   private Socket mWebClient;
   private boolean mConnected;
}
