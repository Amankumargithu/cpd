package com.tacpoint.network;

/** UDPSocketClient.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
*
* Date created:  05/25/00
* The UDPSocketClient class will listen on the subnet.
*
*/

import com.tacpoint.util.*;
import java.net.*;
import java.util.ArrayList;
import java.io.*;

/**
 * The UDPSocketClient class allows for connection to, writing to,
 * and reading from a socket.
 *
 */
public class UDPSocketClient extends NetworkConnection
{
   private static final boolean mDebug = false;

   /**
    * A Socket instance used to maintaining a reference to the open socket.
    */
   private DatagramSocket mUDPSocket;

   /**
    * A UDP port to listen on.
    */
   private int mUDPPortNum;

   /**
    * An input stream used to read from the socket.
    */
   private DatagramPacket mInputPacket;

   /**
    * UDPSocketClient constructor that just initializes member data.
    */
   public UDPSocketClient(int aPortNum) throws Exception
   {
      try
      {
         Logger.init();
         Logger.debug("UDPSocketClient constructor.", mDebug);
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg = "UDPSocketClient constructor: Unable to init Logger.";
         System.out.println(vMsg);
         throw new Exception(vMsg);
      }

      if (aPortNum <= 0)
      {
         String vMsg = "UDPSocketClient constructor: ";
         vMsg += "parameter [aPortNum] was invalid.";
         throw new Exception(vMsg);
      }

      mUDPPortNum = aPortNum;
      mUDPSocket   = null;
      mInputPacket = null;
   }

   /**
    * Creates a stream socket and connects it to the port number
    * and IP address specified in the constructor.
    *
    * @return True if successfully connected to socket.
    *
    */
   public boolean connect()
   {
      Logger.debug("IN  UDPSocketClient.connect()", mDebug);

      boolean vOK = false;
      try
      {
        // mUDPSocket = new DatagramSocket();
         // create a DatagramSocket to listen on this port and this IP address
         mUDPSocket = new DatagramSocket(mUDPPortNum); /*,
                  InetAddress.getByName("172.16.8.54"));*/

         // the connect method will connect this socket to the specified
         // remote address and port.
         mUDPSocket.connect(InetAddress.getByName("172.16.8.71"), 1034);
         Logger.debug("InetAddress - " + (mUDPSocket.getInetAddress().toString()), mDebug);
         Logger.debug("Port - " + (mUDPSocket.getPort()), mDebug);

         if (mUDPSocket != null)
         {
            vOK = true;
         }
      }
      catch (SocketException se)
      {
         Logger.log("UDPSocketClient.connect() failed: " + se.getMessage());
      }
      catch (SecurityException sece)
      {
         Logger.log("UDPSocketClient.connect() failed: " + sece.getMessage());
      }
      catch (Exception e)
      {
         Logger.log("UDPSocketClient.connect() failed: " + e.getMessage());
      }

      Logger.debug("OUT UDPSocketClient.connect() " + vOK, mDebug);
      return vOK;
   }


   /**
    * Closes the socket and the streams associated with the socket.
    *
    * @return True if successfully disconnected from socket.
    *
    */
   public boolean disconnect()
   {
      Logger.debug("UDPSocketClient.disconnect()", mDebug);

      mUDPSocket.disconnect();
      mUDPSocket.close();
      mUDPSocket   = null;
      mInputPacket = null;

      return true;
   }

   public boolean setTimeout(int aTimeout)
   {
      Logger.debug("UDPSocketClient.setTimeout()", mDebug);
      boolean vOK = false;
      try
      {
         mUDPSocket.setSoTimeout(aTimeout);
         vOK = true;
      }
      catch (SocketException e)
      {
         Logger.log("UDPSocketClient.setTimeout(): " + e.getMessage());
      }
      return vOK;
   }

   /**
    * Write data (a string) to the stream associated with the open socket.
    *
    * @param aData
    *
    * @return True if the data was successfully written to the socket.
    *
    */
   public boolean sendData(String aData)
   {
      Logger.debug("UDPSocketClient.sendData()", mDebug);
      boolean vReturn = false;
      return vReturn;
   }

   public ArrayList receiveParsedMessages(byte msgDelim) {
	   return null;
   }
   
   /**
    * Reads data from the stream associated with the socket.
    *
    * @return A string containing the data read from the socket.
    *
    */
   public String receiveData()
   {
      Logger.debug("IN  UDPSocketClient.receiveData()", mDebug);

      String vMsg;
      byte vBuffer[] = null;

      // 65508 is the maximum size of UDP packets.  If you declare something
      // smaller, then UDP will chop off the rest of the data.  So always get
      // the maximum size and manually trim off the spaces.
      vBuffer = new byte[2048];
      if (vBuffer != null)
      {
         try
         {
            // Read the data from the input stream.
            mInputPacket = new DatagramPacket(vBuffer, vBuffer.length);
            mUDPSocket.receive(mInputPacket);

            InetAddress fromAddress = mInputPacket.getAddress();
            Logger.debug("address - " + fromAddress.toString(), mDebug);
         }
         catch (IOException ioe)
         {
            Logger.log("UDPSocketClient.receiveData() failed: " + ioe.getMessage());
         }
      }

      String vNewString = new String(mInputPacket.getData());
      vNewString.trim();

      int vIndex = -1;
      for (int i=1; i < vNewString.length(); i++)
      {
         char vChar = vNewString.charAt(i);
         if (vChar < 32)
         {
            vIndex = i;
            break;
         }
      }

      if (vIndex > 0)
         vNewString = vNewString.substring(vIndex);
      else
         vNewString = new String();

      Logger.debug("OUT UDPSocketClient.receiveData() - " + vNewString, mDebug);
      return (vNewString);
   }

   /**
    * This method is added temporary.
    * Due to the change made in Parent Class NetworkConnetion,
    * this derive class has to implement this mehtod.
    */
   public byte[] receiveDataAsBytes()
   {
       return null;
   }

   public static void main(String[] args)
   {
      boolean vOK  = false;
      byte[]  vRet = null;

      try
      {
         Logger.init();
         Logger.log("UDPSocketClient.main()");
      }
      catch(Exception e)
      {
         System.out.println("UDPSocketClient.main(): Failed to init Logger");
      }

      try
      {
         NetworkConnection sc = new UDPSocketClient(6010);
         if (!sc.connect())
            return;

         sc.disconnect();
      }
      catch(Exception e)
      {
         System.out.println(e.getMessage());
      }
   }
}
