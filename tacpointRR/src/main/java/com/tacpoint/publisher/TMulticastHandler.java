/**
 * TMulticastHandler.java
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

import java.io.*;
import java.net.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TMulticastHandler implements Runnable, TProtocolHandler
{
   private static Log log = LogFactory.getLog(TMulticastHandler.class);

   private TMessageQueue messageList = new TMessageQueue();

   private TPublisherConfigBean configBean;

   private int port = 5557;
   private int ttl = 32;
   private String bindAddress;

   private long millisDelay = 0;
   private long nanosDelay = 0;

   public void addMessage(TMessage amessage) {
      messageList.add(amessage);
   }

   public void setConfigBean(TPublisherConfigBean configBean) {
      this.configBean = configBean;
   }

   public TMulticastHandler() {}

   public void run() {

      InetAddress address = null;

      try {

         port = Integer.parseInt(configBean.getUdpPort());
         ttl = Integer.parseInt(configBean.getUdpTtl());
         address = InetAddress.getByName(configBean.getUdpBindAddress());

      }
      catch (Exception configEx) {
         log.fatal("***** TMulticastHandler - unable to configure multicast port/address/ttl data - "+configEx.getMessage(),configEx);
          return;
      }

      log.debug("TMulticastHandler - port : "+port);
      log.debug("TMulticastHandler - Bind Address : "+bindAddress);
      log.debug("TMulticastHandler - TTL : "+ttl);

      try {

         MulticastSocket sock=new MulticastSocket(port);
         sock.setTimeToLive(ttl);


         if (bindAddress == null)
            address = InetAddress.getByName("224.0.0.150");
         else
            address = InetAddress.getByName(bindAddress);

         //sock.setInterface(address);

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
               log.debug("Muticast Msg broadcast count : "+localMsgCount);
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

                  packet=new DatagramPacket(buffer, buffer.length, address, port);
                  sock.send(packet);
                  localMsgCount++;
               }
            }
            catch (Exception iex) {
               log.error("Exception encountered while attempting to broadcast message data - "+iex.getMessage(),iex);
            }
         }
      }
      catch(Exception e) {
         log.error("TMulticastHandler.run - exiting while loop - " + e.getMessage(),e);
      }
   }
}

class MCastAddress {

   private int port;
   private int ttl;
   private String address;
   private InetAddress inetAddress;

   public void setPort(int port) {
      this.port = port;
   }

   public void setTtl(int ttl) {
	  this.ttl = ttl;
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

   public int getTtl() {
	   return ttl;
   }

}
