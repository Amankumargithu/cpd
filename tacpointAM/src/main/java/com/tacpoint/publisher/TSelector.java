/**
 * TSelector.java
 *
 * @author Copyright (c) 2004 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TSelector implements Runnable
{

   protected ByteArrayOutputStream baos = new ByteArrayOutputStream(4096*2);

   protected int channelCount = 0;

   protected long curTime = 0;
   protected long begTime = 0;

   protected long atByteCount = 0;
   protected long acByteCount = 0;
   protected double mbCount = 0;

   protected static Log log = LogFactory.getLog(TSelector.class);
   protected TDispatcher dispatcher;

   protected TMessageQueue messageList = new TMessageQueue();

   protected long messagesSentCount = 0;

   protected ArrayList registrations = new ArrayList(2000);
   protected ArrayList deregistrations = new ArrayList(2000);

   protected long MAX_QUEUE_SIZE = 10000;
   protected long MAX_CHANNEL_IDLE_TIME = 60000;
   protected int MAX_MSGS_PER_POLLING_CYCLE = 100;

   protected TMessage[] messages = new TMessage[MAX_MSGS_PER_POLLING_CYCLE];
   protected ByteBuffer[] byteBuffers = new ByteBuffer[MAX_MSGS_PER_POLLING_CYCLE];
   protected Iterator readySet = null;
   protected Set readyKeys = null;


   protected Selector selector;
   protected Thread selectorThread;

   protected String id;

   public int getQueueSize() {
      return messageList.getSize();
   }

   public int getChannelCount() {
      return registrations.size()+channelCount;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public void addMessage(TMessage amessage) {
      messageList.add(amessage);
   }
   
   public void addAll(java.util.Collection additions) {
	   messageList.addAll(additions);
   }

   public void setDispatcher(TDispatcher dispatcher) {
      this.dispatcher = dispatcher;
   }

   public void runDumpStack() {
      System.out.println(new java.util.Date()+" Dumping thread stack for TSelector ["+id+"] ...");
      selectorThread.dumpStack();
      System.out.println(new java.util.Date()+" Completed thread stack for TSelector ["+id+"] ...");
   }

   public void init() {
      try
      {
         selector = Selector.open();
         selectorThread = new Thread(this, "TSelector-"+id);
         selectorThread.start();

      }
      catch(Exception e)
      {
         log.error("TSelector.init() " + e.getMessage(),e);
      }
   }

   public TSelector() {}

   protected void processExpiredChannels(Set keyset) {
      long pecBegTime = System.currentTimeMillis();
      channelCount = keyset.size();
      log.debug("Selector keyset size : "+channelCount);
      Iterator it = keyset.iterator();
      SelectionKey key = null;
      long now = System.currentTimeMillis();
      while (it.hasNext()) {
         try {
            key = (SelectionKey)it.next();
            SocketChannel channel = (SocketChannel)key.channel();
            TAttachKey attachKey = (TAttachKey)key.attachment();
            if ( (now - attachKey.lastWriteTime) > MAX_CHANNEL_IDLE_TIME) {
               try {
                  if (!attachKey.cancelProcessed) {
                     log.debug("TSelector ["+id+"] - Removing *stale* channel from selector.");
                     processCancelKey(channel, key);
                  }
                  else {
                     log.debug("TSelector ["+id+"] - cancel has already been processed for User ID : "+attachKey.userId);
                  }
               }
               catch (Exception ex) {
                  log.error("Exception encountered while trying to remove *stale* channel - "+ex.getMessage(),ex);
               }
            }
         }
         catch (Exception wex) {
            log.error("Exception encountered while trying to process *stale* channel - "+wex.getMessage(),wex);
         }
      }
   }

   public void register(SocketChannel sc, String userid, Vector interestList)
   {
      TSelectorRegistration tsr = new TSelectorRegistration();
      tsr.setSocketChannel(sc);
      tsr.setUserId(userid);
      tsr.setInterestList(interestList);
      synchronized (registrations) {
         registrations.add(tsr);
      }
   }

   protected void processRegistrationEvents() {
      Object[] tsrArray = null;
      synchronized(registrations) {
         tsrArray = registrations.toArray();
         registrations.clear();
      }
      if (tsrArray == null) return;
      try {

         if (tsrArray.length > 0)
            log.debug("About to process "+tsrArray.length+ " TSR objects.");
         for (int i=0; i<tsrArray.length; i++) {
            TSelectorRegistration tsr = (TSelectorRegistration)tsrArray[i];
            if (tsr == null) {
                log.error("Process registration events - TSR is null.");
                continue;
            }
            String userid = tsr.getUserId();
            if (userid == null ) {
                // should NEVER happen, but left log to help track down if it does.
                log.error("Process registration events - User id is null.");
            }
            SocketChannel channel = tsr.getSocketChannel();
            if (channel == null ) {
                // should NEVER happen, but left log to help track down if it does.
                log.error("Process registration events - socket channel is null.");
            }
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
               key = channel.keyFor(selector);
               log.debug("Process registration socket channel was previously registered - user ID : "+userid);
            }
            else {
               try {
                  log.debug("Process registration socket channel was not previously registered - user ID : "+userid);
                  key = channel.register(selector, SelectionKey.OP_WRITE);
               }
               catch (Exception cce) {
                  try {
                     processClosedChannel(channel, userid, tsr.getInterestList());
                  }
                  catch (Exception any) {}
                  continue;
               }
            }
            TAttachKey tak = (TAttachKey)key.attachment();
            if (tak == null) {
               tak = new TAttachKey();
               tak.userId = userid;
            }
            HashMap userInterests = tak.topics;
            Vector newInterests = new Vector();
            if (tsr.getInterestList() != null) {
               Iterator it = tsr.getInterestList().iterator();
               while (it.hasNext()) {
                  Object topic = it.next();
                  if (userInterests.containsKey(topic))
                     continue;

                  userInterests.put(topic,null);
                  newInterests.addElement(topic);
               }
            }
            if (newInterests.size() > 0) {
               dispatcher.addToMasterSubscriptionList(newInterests);
            }
            key.attach(tak);
         }
      }
      catch(Exception e)
      {
         log.error("TSelector.processRegistrationEvents() " + e.getMessage(),e);
      }
   }

   public void unregister(SocketChannel sc, String userid, Vector interestList)
   {
      TSelectorRegistration tsr = new TSelectorRegistration();
      tsr.setSocketChannel(sc);
      tsr.setUserId(userid);
      tsr.setInterestList(interestList);
      synchronized (deregistrations) {
         deregistrations.add(tsr);
      }
   }

   protected void processDeRegistrationEvents() {
      Object[] tsrArray = null;
      synchronized(deregistrations) {
         tsrArray = deregistrations.toArray();
         deregistrations.clear();
      }
      if (tsrArray == null) return;
      try
      {
         for (int i=0; i<tsrArray.length; i++) {
            TSelectorRegistration tsr = (TSelectorRegistration)tsrArray[i];
            SocketChannel sc = tsr.getSocketChannel();
            String userid = tsr.getUserId();
            log.debug("Process de-registration event for user ID : "+userid);
            // find existing selection key!
            SelectionKey key = sc.keyFor(selector);
            if (key == null) {
               // can't find the key, nothing else we can do ...
               log.error("Process de-reg - unable to find selection key for user ID : "+userid);
               continue;
            }
            TAttachKey tak = (TAttachKey)key.attachment();
            if (tak == null) {
               // if attachment is null, can't proceed any further ...
               log.error("Process de-reg - unable to find attachment for user ID : "+userid);
               continue;
            }
            HashMap userInterests = tak.topics;
            Vector removals = new Vector();
            Iterator it = tsr.getInterestList().iterator();
            while (it.hasNext()) {
               Object topic = it.next();
               userInterests.remove(topic);
               removals.addElement(topic);
            }
            dispatcher.removeFromMasterSubscriptionList(removals);

            if (userInterests.size() == 0) {
               log.error("User interest size is 0 ... cancelling channel for user ID : "+userid);
               processCancelKey(sc,key);
            }
         }
      }
      catch(Exception e)
      {
         log.error("TSelector.processDeRegistrationEvents() " + e.getMessage(),e);
      }
   }

   protected void processClosedChannel(SocketChannel sc, String userId, Vector userInterests) {
      try {
         dispatcher.removeFromMasterSubscriptionList(userInterests);
      }
      catch (Exception npe) {
         log.debug("TSelector ["+id+"] exc occured during dispatcher.removeFromMasterSubList - "+npe.getMessage(),npe);
      }
      try {
         SocketChannel regSocketChannel = TPublisher.channelLookup(userId);
         if (regSocketChannel != null) {
            if (regSocketChannel.equals(sc)) {
               TPublisher.unregisterChannel(userId);
               dispatcher.unregisterFromSelectorMap(userId);
            }
         }
       }
      catch (Exception npe) {
         log.debug("TSelector ["+id+"] Process closed channel exc occured during unregistering "+
                   "of selector & channel maps - "+npe.getMessage(),npe);
      }
   }

   protected void cancel(SocketChannel sc, TAttachKey tak)
   {
      // used to prevent multiple cancellations on the same channel
      if (tak != null) tak.cancelProcessed = true;
      try {
         dispatcher.removeFromMasterSubscriptionList(new Vector(tak.topics.keySet()));
      }
      catch (Exception npe) {
         log.debug("TSelector ["+id+"] exc occured during dispatcher.removeFromMasterSubList - "+npe.getMessage(),npe);
      }
      log.info("Cancelling subscription socket for user ID : "+tak.userId);
      try {
         SocketChannel regSocketChannel = TPublisher.channelLookup(tak.userId);
         if (regSocketChannel != null) {
            if (regSocketChannel.equals(sc)) {
               TPublisher.unregisterChannel(tak.userId);
               dispatcher.unregisterFromSelectorMap(tak.userId);
            }
         }
       }
      catch (Exception npe) {
         log.debug("TSelector ["+id+"] exc occured during unregistering of selector & channel maps - "+npe.getMessage(),npe);
      }
      if (sc == null) return;
      try {
          sc.close();
      }
      catch (Exception cex) {}     
   }

   public void run() {
      int queueReads = 1;
      int messagesRead = 0;
      try
      {
         while (true) {
            curTime = System.currentTimeMillis();
            if ( (curTime-begTime) > 5000) {
               mbCount = (double)atByteCount / (double)1048576;
               log.debug("TSelector ["+id+"] - attempted number of MB pushed "+mbCount);
               mbCount = (double)acByteCount / (double)1048576;
               log.debug("TSelector ["+id+"] - actual number of MB pushed "+mbCount);

               if (queueReads != 0)
                  log.debug("TSelector ["+id+"] - avg nbr msgs drained from queue per read : "+(messagesRead / queueReads));
               log.debug("TSelector ["+id+"] - number of queue reads : "+(queueReads));

               begTime = curTime;
               atByteCount=0;
               acByteCount=0;
               messagesRead = 0;
               queueReads = 0;
               processExpiredChannels(selector.keys());
            }
            processRegistrationEvents();
            processDeRegistrationEvents();
            Object[] readyMessages = messageList.removeAll();
            long rcBegTime = System.currentTimeMillis();
            messagesRead += readyMessages.length;
            queueReads++;
            if (readyMessages.length > MAX_QUEUE_SIZE) {
               // flush this queue as it's already stale!
               log.debug("TSelector["+id+"] - Message queue is too large - size = "+readyMessages.length);
               log.debug("TSelector["+id+"] - Message queue being flushed in order to catch up.");
               System.out.println("TSelector["+id+"] - Message queue being flushed in order to catch up.");
               continue;
            }
            // are any channels ready to consume data?
            if ( selector.select(100) == 0 ) {
               log.debug("TSelector["+id+"] - no channels selected for consumption - discarding existing messages!");
               continue;
            }
            // Get set of ready objects
            readyKeys = selector.selectedKeys();
            int messageIndex = 0;
            int msgTotal = 0;
            for (int i=0; i<readyMessages.length; i++) {
               TMessage tmsg = (TMessage)readyMessages[i];
               messages[messageIndex] = tmsg;
               msgTotal++;
               messageIndex++;
               // check to see if we're at the maximum number of messages per polling cycle
               if (messageIndex == MAX_MSGS_PER_POLLING_CYCLE) {
                  feedChannels(messageIndex);
                  messageIndex = 0;
               }
            }
            // process any remaining messages
            if (messageIndex > 0)
               feedChannels(messageIndex);
            // clean up selection keys before next select invokation!
            try {
               readySet = readyKeys.iterator();
               while (readySet.hasNext()) {
                  readySet.next();
                  readySet.remove();
               }
            }
            catch (Exception removeEx) {
               log.error("TSelector.run - Exception encountered while removing selection keys - "+removeEx.getMessage(),removeEx);
            }
         } // while (true)
      }
      catch(Exception e) {
         log.error("TSelector.run - exiting while loop - " + e.getMessage());
      }
   }

   protected void feedChannels(int maxMessageIndex) {

      SocketChannel writeChannel = null;
      SelectionKey writeKey = null;
      TAttachKey attachKey = null;
      TMessage message = null;
      ByteBuffer buffer = null;

      String userid = null;

      // Walk through set
      readySet = readyKeys.iterator();

      long beginTime = System.currentTimeMillis();
      int chCount = 0;

      while (readySet.hasNext())
      {
         try
         {
            // Get key from set
            writeKey = (SelectionKey)readySet.next();
            attachKey = (TAttachKey)writeKey.attachment();
            userid = attachKey.userId;

            if (writeKey.isWritable() && writeKey.isValid())
            {
               chCount++;
               // Get channel
               writeChannel = (SocketChannel)writeKey.channel();
               if ( writeChannel != null )
               {
                  HashMap interestHash = attachKey.topics;
                  if (interestHash == null) {
                     log.error("TSelector.run - Interest hash is null for user : "+userid);
                     processCancelKey(writeChannel, writeKey);
                     continue;
                  }

                  int curMsgCount = 0;
                  long now = System.currentTimeMillis();
                  baos.reset();
                  for (int i=0; i<maxMessageIndex; i++) {
                     message = messages[i];
                     if (interestHash.containsKey(message.getKey())) {
                        baos.write(message.getBaos().toByteArray());
                     }
                  }
                  if (baos.size() > 0) {
                     int blength = baos.toByteArray().length;
                     buffer = ByteBuffer.wrap(baos.toByteArray());
                     int curBytesWritten = writeChannel.write(buffer);
                     atByteCount += blength;
                     if (curBytesWritten > 0) {
                        attachKey.lastWriteTime = now;
                     }
                     else {
                        log.error("TSelector.run - no bytes written to channel for user : "+userid);
                     }
                     acByteCount += curBytesWritten;
                     ++curMsgCount;
                     ++messagesSentCount;

                     if (buffer.hasRemaining()) {
                        acByteCount += writeChannel.write(buffer);
                     }
                  }
               }
            }
            else {
               log.info("Write Key is invalid or not writeable for User ID : "+userid);
               readySet.remove();
               processCancelKey(writeChannel, writeKey);
            }
         }
         catch (java.util.ConcurrentModificationException cme) {
            log.debug("TSelector ["+id+"] Concurrent mod ex.",cme);
         }
         catch(Exception e) {
            try {
               log.info("Processing runtime exception for User ID : "+userid+" - "+e.getMessage(),e);
               readySet.remove();
               processCancelKey(writeChannel, writeKey);
            }
            catch (Exception pckEx) {}
         }
      } // while hasNext
      long endTime = System.currentTimeMillis();
   }

   protected void processCancelKey(SocketChannel writeChannel, SelectionKey writeKey) {
      try {
         cancel(writeChannel,(TAttachKey)writeKey.attachment());
         writeKey.cancel();
      }
      catch (Exception wkEx) {
         log.error("Exception encountered while attempting to cancel write key - "+wkEx.getMessage(),wkEx);
      }
   }
}
