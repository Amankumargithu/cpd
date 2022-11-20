/**
 * TSQSubscriptionAnalyzer.java
 *
 * @author Copyright (c) 2005 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.b4utrade.subscription;

import com.tacpoint.publisher.TDispatcher;
import com.tacpoint.publisher.TPublisherConfigBean;
import com.tacpoint.publisher.TAnalyzer;

import com.b4utrade.ejb.*;

import java.util.*;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

public class TSQSubscriptionAnalyzer implements Runnable, TAnalyzer
{
   private InitialContext jndiContext = null;
   private Object reference = null;
   private TSQSubscriberHome home = null;
   private TSQSubscriber tsqSubscriber = null;

   private static final String BLANK = "";


   private static Log log = LogFactory.getLog(TSQSubscriptionAnalyzer.class);

   private static ConcurrentHashMap tsqSubs = new ConcurrentHashMap(5000);

   private String subscriberId = null;

   private TPublisherConfigBean configBean;

   public void setConfigBean(TPublisherConfigBean configBean) {
      this.configBean = configBean;
   }

   public void run() {

      try {
         subscriberId = java.net.InetAddress.getLocalHost().getHostName();
      }
      catch (Exception e) {
         subscriberId = String.valueOf(System.currentTimeMillis());
      }

      log.debug("TSQSubscriptionAnalyzer subscriber ID : "+subscriberId);
      
      long curTime = 0;
      long begTime = 0;
       
      boolean isSubListUpToDate = false;

      while (true) {
         try {

            isSubListUpToDate = true;

            curTime = System.currentTimeMillis();

            if ( (curTime - begTime) > 5000) {
               log.debug("TSQSubscriptionAnalyzer - comprehensive tsq subscription list : ");
               begTime = curTime;
               Iterator it =  TDispatcher.MASTER_SUBSCRIPTIONS.keySet().iterator();
               while (it.hasNext()) {
                  String key = (String)it.next();                  
                  log.debug("Subscribed tsq ticker : "+key);
               }
              
               // force a subscription update in case the tsq producer is out of sync!
               isSubListUpToDate = false;
            }

            try {
               Thread.sleep(1000);
            }
            catch (Exception sleepEx) {}

            Iterator it = TDispatcher.MASTER_SUBSCRIPTIONS.keySet().iterator();

            while (it.hasNext()) {
               String key = (String)it.next();

               if (tsqSubs.containsKey(key)) {
                  continue;
               }

               tsqSubs.put(key,BLANK);
               isSubListUpToDate = false;
            }

            it = tsqSubs.keySet().iterator();

            while (it.hasNext()) {
               Object key = it.next();
               if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key))
                  continue;
         
               tsqSubs.remove(key);
               isSubListUpToDate = false;
            }
               
            if (!isSubListUpToDate)
               processSubscriptions();
            
         }
         catch (Exception e) {
            log.error("Exception encountered during TSQSubscriptionAnalyzer run method - "+e.getMessage(),e);
         }
      }
   }

   private void processSubscriptions() {

      try {

         if (tsqSubscriber == null)
            initTSQSubscriber();

         if (tsqSubscriber == null) { 
           log.error("Unable to initialize tsq subscriber handle.");
           return;
         }

         System.out.println("Going to send request for " + tsqSubs);
         tsqSubscriber.subscribe(subscriberId, tsqSubs.keySet().toArray());
 
         log.debug("Finished updating subscription list on TSQ server.");

      }
      catch (Exception e) {
    	  e.printStackTrace();
    	  System.out.println("exception "+e.getMessage());
         log.error("Exception in TSQSubscriptionAnalyzer processSubscriptions method - "+e.getMessage(),e);
         tsqSubscriber = null;
      }
   }

   private void initTSQSubscriber() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");

         if (configBean != null && configBean.getOptionsJndiAddress() != null)
            env.put(Context.PROVIDER_URL, configBean.getOptionsJndiAddress());
         else
            env.put(Context.PROVIDER_URL, "192.168.192.103");

         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/tsqSubscriber");
         tsqSubscriber = (TSQSubscriber) PortableRemoteObject.narrow(reference, TSQSubscriber.class);
        System.out.println("Got TSQSubscriber's instance ");
      }
      catch(Exception e) {
    	  System.out.println("exception "+e.getMessage());
    	  e.printStackTrace();
         log.error("Unable to init TSQSubscriber interface - "+e.getMessage(),e);
         tsqSubscriber = null;
      }
   }
}
