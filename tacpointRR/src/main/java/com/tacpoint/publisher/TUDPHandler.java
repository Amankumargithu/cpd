/**
 * TUDPHandler.java
 *
 * @author Copyright (c) 2005 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import java.io.*;
import java.net.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TUDPHandler implements Runnable, TProtocolHandler
{
   private static Log log = LogFactory.getLog(TUDPHandler.class);
   
   private TMessageQueue messageList = new TMessageQueue();

   private TPublisherConfigBean configBean;

   private int port = 5557;
   private String bindAddress;

   private long millisDelay = 0;
   private long nanosDelay = 0;

   public void addMessage(TMessage amessage) {
      messageList.add(amessage);
   }

   public void setConfigBean(TPublisherConfigBean configBean) {
      this.configBean = configBean;
   }

   public TUDPHandler() {}

   private List configureAddressInfo(TPublisherConfigBean configBean) throws Exception {
      ArrayList list = new ArrayList();

      String ports = configBean.getUdpPort();
      String addresses = configBean.getUdpBindAddress();

      StringTokenizer portTokens = new StringTokenizer(ports,",");
      StringTokenizer addressTokens = new StringTokenizer(addresses,",");

      // making assumption that the same number of tokens are present for both ports and addresses!

      while (portTokens.hasMoreTokens()) {

         int port = Integer.parseInt(portTokens.nextToken());
         String address = addressTokens.nextToken();
         InetAddress inetAddress = InetAddress.getByName(address);
         UdpAddress udpAddress = new UdpAddress();
         udpAddress.setPort(port);
         udpAddress.setAddress(address);
         udpAddress.setInetAddress(inetAddress);

         log.info("TUDPHandler - Added UDP port : "+port+" UDP address :"+address);

         list.add(udpAddress);

      }

      return list;

   }

   public void run() {

      List udpAddresses = null;

      try {
         udpAddresses = configureAddressInfo(configBean);
      }
      catch (Exception e) {
         log.fatal("****** Unable to parse out UDP address info - "+e.getMessage(),e);
         return;
      }
         

      try
      {

         DatagramSocket sock = new DatagramSocket();
         sock.setBroadcast(true);

         long curTime = 0;
         long begTime = 0;

         DatagramPacket packet = null;
         byte[] buffer = null;

         long msgCount = 0;
         long localMsgCount = 0;

         ByteArrayOutputStream baos = new ByteArrayOutputStream(128);

         // first build the packet number *seed* which will be used
         // by the clients to determine if the message should be discarded
         // or not.

         long seedTime = System.currentTimeMillis();
         
         while (true) {

            curTime = System.currentTimeMillis();
            if ( (curTime-begTime) > 5000) {
               log.debug("UDP Msg broadcast count : "+localMsgCount);
               localMsgCount = 0;
               begTime = curTime;
            }

            try {

               Object[] messages = messageList.removeAll();

               for (int i=0; i<messages.length; i++) {

                  baos.reset();

                  buffer = String.valueOf(seedTime++).getBytes();
                  baos.write(buffer, 0, buffer.length);

                  TMessage message = (TMessage)messages[i];

                  buffer = message.getBaos().toByteArray();

                  baos.write(buffer, 0, buffer.length);

                  buffer = baos.toByteArray();

                  Iterator it = udpAddresses.iterator();

                  while (it.hasNext()) {
                     UdpAddress udpAddress = (UdpAddress)it.next();
                     packet = new DatagramPacket(buffer, buffer.length, udpAddress.getInetAddress(), udpAddress.getPort());
                     sock.send(packet);
                  }

                  localMsgCount++;
                  
               }
            }
            catch (Exception iex) {
               log.error("Exception encountered while attempting to broadcast message data - "+iex.getMessage(),iex);
            }
         }
      }
      catch(Exception e) {
         log.error("TUDPHandler.run - exiting while loop - " + e.getMessage(),e);
      }
   }
}

class UdpAddress {

   private int port;
   private String address;
   private InetAddress inetAddress;

   public void setPort(int port) {
      this.port = port;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public void setInetAddress(InetAddress inetAddress) {
      this.inetAddress = inetAddress;
   }

   public int getPort() {
      return port;
   }

   public String getAddress() {
      return address;
   }

   public InetAddress getInetAddress() {
      return inetAddress;
   }

}
