/**
 * TDispatcher.java
 *
 * @author Copyright (c) 2004 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

public final class TDispatcher implements Runnable
{

   private static Log log = LogFactory.getLog(TDispatcher.class);

   private TPublisher publisher;

   private TMessageQueue messageQueue;
   
   private int selectorIndex = 0;

   public static ConcurrentHashMap MASTER_SUBSCRIPTIONS = new ConcurrentHashMap();
   public static ConcurrentHashMap SNAP_QUOTE_CACHE = new ConcurrentHashMap();

   private TSelector[] selectors = null;

   int numSelectors = 1;

   ConcurrentHashMap userSelectorMap = new ConcurrentHashMap();

   Thread dispatcherThread;

   Thread subscriptionAnalyzerThread = null;

   Thread udpRunnerThread = null;
   TProtocolHandler protocolHandler = null;

   public void setMessageQueue(TMessageQueue messageQueue) {
      this.messageQueue = messageQueue;
   }

   public TDispatcher(TPublisher publisher, int numSelectors, Runnable subscriptionAnalyzerRunner, 
                      Runnable udpRunner, String selectorClassName)
   {
      try {
         this.publisher = publisher;

         if (numSelectors > 0) this.numSelectors = numSelectors;

         selectors = new TSelector[numSelectors];

         if (selectorClassName != null && !selectorClassName.equalsIgnoreCase("null")) {
            log.debug("About to create selector instance(s) of type : "+selectorClassName);
            for (int i=0; i<numSelectors; i++) {
               selectors[i] = (TSelector)Class.forName(selectorClassName).newInstance();
               selectors[i].setDispatcher(this);
               selectors[i].setId(String.valueOf(i));
               selectors[i].init();
            }
         }
         else {
            log.debug("About to create selector instance(s) of type : TSelector");
            for (int i=0; i<numSelectors; i++) {
               selectors[i] = new TSelector();
               selectors[i].setDispatcher(this);
               selectors[i].setId(String.valueOf(i));
               selectors[i].init();
            }
         }

         dispatcherThread = new Thread(this);
         dispatcherThread.start();

         if (subscriptionAnalyzerRunner != null) {
            ((TAnalyzer)subscriptionAnalyzerRunner).setConfigBean(publisher.getConfiguration());
            subscriptionAnalyzerThread = new Thread(subscriptionAnalyzerRunner);
            subscriptionAnalyzerThread.start();
         }

         if (udpRunner != null) {
            protocolHandler  = (TProtocolHandler)udpRunner;

            protocolHandler.setConfigBean(publisher.getConfiguration());

            udpRunnerThread = new Thread(udpRunner);
            udpRunnerThread.start();
         }
      }
      catch (Exception e) {
         log.fatal("Unable to initialize TDispatcher instance - "+e.getMessage(),e);
         System.exit(0);
      }
   }

   public synchronized void addToMasterSubscriptionList(Vector interestList) {

      if (interestList == null) return;

      Iterator it = interestList.iterator();
      while (it.hasNext()) {
         String topic = (String)it.next();

         Long count = (Long)MASTER_SUBSCRIPTIONS.get(topic);
         if (count == null) {
            count = new Long(1);
            MASTER_SUBSCRIPTIONS.put(topic,count);
         }
         else {
            MASTER_SUBSCRIPTIONS.put(topic,new Long(count.longValue()+1));
         }
      }
   }

   public synchronized void removeFromMasterSubscriptionList(Vector interestList) {

      if (interestList == null) return;
      Iterator it = interestList.iterator();
      while (it.hasNext()) {
         String topic = (String)it.next();
         Long count = (Long)MASTER_SUBSCRIPTIONS.get(topic);
         if (count == null) {
             continue;
         }
         else if (count.longValue() <= 1) {
            MASTER_SUBSCRIPTIONS.remove(topic);
         }
         else {
            MASTER_SUBSCRIPTIONS.put(topic,new Long(count.longValue()-1));
         }
      }
   }

   public void unregisterFromSelectorMap(String userid) {
      if (userid != null) {
         userSelectorMap.remove(userid);
      }
   }

   public synchronized void register(SocketChannel sc, String userid, Vector interestList)
   {
      if (protocolHandler != null) {
         addToMasterSubscriptionList(interestList);
         cleanup(sc);
         return;
      }
      // see if they're already registered with us ...
      TSelector targetSelector = (TSelector)userSelectorMap.get(userid);
      if (targetSelector != null) {
         log.debug("Found existing selector for user : "+userid);
         targetSelector.register(sc, userid, interestList);
         return;
      }      
      // use a round robin registration algo to choose next selector ...
      targetSelector = selectors[selectorIndex++];
      
      if (selectorIndex >= selectors.length)
    	  selectorIndex = 0;      
 
      int openChannelCount = 0;

      for (int i=0; i<selectors.length; i++) {

         int cCount = selectors[i].getChannelCount();
         log.debug("Selector["+i+"] channel count = "+cCount);
         openChannelCount += cCount;
      }
      log.debug("Registering channel with selector["+targetSelector.getId()+"]");
      log.debug("Approximate socket channel count : "+openChannelCount);

      userSelectorMap.put(userid, targetSelector);
      targetSelector.register(sc, userid, interestList);
   }

   public synchronized void unregister(SocketChannel sc, String userid, Vector interestList)
   {
      if (userid == null) {
         log.error("TDispatcher.unregister - user ID is null.");
         return;
      }
      if (protocolHandler != null) {
         removeFromMasterSubscriptionList(interestList);
         cleanup(sc);
         return;
      }
      // find the selector for which they've been registered!
      TSelector targetSelector = (TSelector)userSelectorMap.get(userid);
      if (targetSelector != null) {
         targetSelector.unregister(sc, userid, interestList);
      }
      else {
         log.error("Unable to find user's registered TSelector object for user ID : "+userid);
      }
   }

   public void run() {

      long curTime = 0;
      long begTime = 0;
      long count=0;

      while (true) {
         try {
            curTime = System.currentTimeMillis();
            if ( (curTime - begTime) > 5000) {
               log.debug("TDispatcher - messages processed from queue : "+count);
               log.debug("TDispatcher - current queue size : "+messageQueue.getSize());
               count=0;
               begTime = curTime;
               
               for (int i=0; i<selectors.length; i++) {
                  log.debug("Selector["+i+"] queue size = "+selectors[i].getQueueSize());
                  if (selectors[i].getQueueSize() > 50000) {
                	  log.debug("WARNING - Selector["+i+"] queue size : "+selectors[i].getQueueSize());
                  }
               }
            }
            if (messageQueue == null) {
               Thread.sleep(1000);
               continue;
            }
            Object[] messages = messageQueue.remove(20000);
            count += messages.length;
            if (messages != null && messages.length > 0) {
               java.util.ArrayList additions = new java.util.ArrayList(messages.length);
               for (int i=0; i<messages.length; i++) {
                  TMessage message = (TMessage)messages[i];
                  additions.add(message);
                  if (protocolHandler != null) protocolHandler.addMessage(message);
               }
               for (int j=0; j<selectors.length; j++) {
                   selectors[j].addAll(additions);
               }
            }
         }
         catch (Exception e) {
            log.error("Exception encountered during TDispatcher run method - "+e.getMessage(),e);
         }
      }
   }

   private void cleanup(SocketChannel target) {

      try {
         target.socket().shutdownInput();
      }
      catch (Exception e) {}
      try {
         target.socket().shutdownOutput();
      }
      catch (Exception e) {}
      try {
         target.close();
      }
      catch (Exception e) {}
   }
}
