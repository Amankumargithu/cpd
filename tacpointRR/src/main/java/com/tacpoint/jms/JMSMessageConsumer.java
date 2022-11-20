package com.tacpoint.jms;

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;

public class JMSMessageConsumer extends Object implements Runnable
{
   private static Log log = LogFactory.getLog(JMSMessageConsumer.class);
   private static final int DEFAULT_THREAD_POOL_SIZE = 10;
   
   private MessageHandler messageHandler = null;

   private JMSMessageListener messageListenerP = null;
   private JMSMessageListener messageListenerS = null;

   private String consumerName;

   private String clientID;

   private String topicName;

   private String primaryIP;

   private String secondaryIP;

   private String qos;

   private boolean discardOldestWhenBlocked = false;

   private int maxThreadCount = DEFAULT_THREAD_POOL_SIZE;
   
   public void setDiscardOldestWhenBlocked(boolean discardOldestWhenBlocked) {
      this.discardOldestWhenBlocked = discardOldestWhenBlocked;
   }

   public boolean getDiscardOldestWhenBlocked() {
      return discardOldestWhenBlocked;
   }

   public void setConsumerName(String consumerName) {
      this.consumerName = consumerName;
   }

   public void setQOS(String qos) {
      this.qos = qos;
   }
   
   public void setMaxThreadCount(int maxThreadCount) {
      this.maxThreadCount = maxThreadCount;
   }

   public void setMessageHandler(MessageHandler messageHandler) {
      this.messageHandler = messageHandler;
   }

   public void setClientID(String clientID) {
      this.clientID = clientID;
   }

   public void setTopicName(String topicName) {
      this.topicName = topicName;
   }

   public void setPrimaryIP(String primaryIP) {
      this.primaryIP = primaryIP;
   }

   public void setSecondaryIP(String secondaryIP) {
      this.secondaryIP = secondaryIP;
   }

   public String  getConsumerName() {
      return consumerName;
   }

   public String  getQOS() {
      return qos;
   }

   public MessageHandler getMessageHandler() {
      return messageHandler;
   }

   public int getMaxThreadCount() {
      return maxThreadCount;
   }

   public String getSecondaryIP() {
      return secondaryIP;
   }

   public String getPrimaryIP() {
      return primaryIP;
   }

   public String getTopicName() {
      return topicName;
   }

   public String getClientID() {
      return clientID;
   }

   // constructor
   public JMSMessageConsumer() throws Exception
   {
      // perform any necessary cleanup prior to shutting down!
      try
      {
         Runtime.getRuntime().addShutdownHook(new Thread()
         {
            public void run()
            {
               System.exit(0);
            }

         });
      } catch (Throwable e)
      {
         log.error("JMSMessageConsumer - Could not add shutdown hook", e);
      } 
   }

   public void run()
   {
      try
      {

         QuoteConsumer qcPrimary = null;
         QuoteConsumer qcSecondary = null;

         messageListenerP = new JMSMessageListener(maxThreadCount);
         messageListenerP.setMessageHandler(messageHandler);
         messageListenerP.setConsumerName(consumerName+"_P");

         /*
         //messageListenerS = new JMSMessageListener(maxThreadCount);
         //messageListenerS.setMessageHandler(messageHandler);
         //messageListenerS.setConsumerName(consumerName+"_S");

         boolean isPrimary = true;
         
         while (true) {

            try {
               Thread.sleep(3001);
            }
            catch (InterruptedException ie) {}

            long currentTime = System.currentTimeMillis();

            if ( (currentTime - messageListenerP.getLastUpdateTime()) > 3000 &&
                 (currentTime - messageListenerS.getLastUpdateTime()) > 3000 ) {


               if (qcPrimary == null) {
                  qcPrimary = new QuoteConsumer(clientID+"_P", primaryIP, topicName, 
                                                messageListenerP, qos, consumerName+"_P");
                  messageListenerP.setPrevMsg(0);
                  messageListenerP.setLastUpdateTime(currentTime);
                  continue;
               }

               if (qcSecondary == null) {
                  qcSecondary = new QuoteConsumer(clientID+"_S", secondaryIP, topicName, 
                                                  messageListenerS, qos, consumerName+"_S");
                  messageListenerS.setPrevMsg(0);
                  messageListenerS.setLastUpdateTime(currentTime);
                  continue;
               }
            }

            else {

               if (qcSecondary != null && qcPrimary != null) {
                  // need to bring down 1 of the consumers...determine which one!
                  if (isPrimary) {
                     qcSecondary.stopConsumption();
                     qcSecondary = null;
                     isPrimary = false;
                  }
                  else {
                     qcPrimary.stopConsumption();
                     qcPrimary = null;
                     isPrimary = true;
                  }
               }
            }
         }
         */

        qcPrimary = new QuoteConsumer(clientID+"_P", primaryIP, topicName,messageListenerP, qos, consumerName+"_P");
        messageListenerP.setPrevMsg(0);

      }
      catch (Exception e)
      {
         log.error("JMSMessageConsumer.run() " + e.getMessage());
      }
   }

   // main function
   public static void main(String[] args)
   {
      try
      {
         JMSMessageConsumer jmsMsgConsumer = new JMSMessageConsumer();
         jmsMsgConsumer.run();
      }
      catch(Exception e)
      {
         System.out.println("JMSMessageConsumer.main() Exception " + e.getMessage());
      }
   }
}

class JMSMessageListener implements MessageListener
{
   private static Log log = LogFactory.getLog(JMSMessageListener.class);

   private Properties props = new Properties();
   private PooledExecutor pool;
   private BoundedBuffer buffer = new BoundedBuffer(20000);

   private MessageHandler messageHandler = null;

   private long beginTime = 0;
   private long lastUpdateTime = 0;
   private int prevMsg=0;
   private int currentMsg=0;
   private int locallost=0;
   private int localcount=0;
   private String consumerName=null;
   private boolean discardOldestWhenBlocked = false;

   public MessageHandler getMessageHandler() {
      return messageHandler;
   }

   public void setMessageHandler(MessageHandler messageHandler) {
      this.messageHandler = messageHandler;
   }

   public void setDiscardOldestWhenBlocked(boolean discardOldestWhenBlocked) {
      this.discardOldestWhenBlocked = discardOldestWhenBlocked;
   }

   public boolean getDiscardOldestWhenBlocked() {
      return discardOldestWhenBlocked;
   }

   public long getLastUpdateTime() {
      return lastUpdateTime;
   }

   public String getConsumerName() {
      return consumerName;
   }

   public void setConsumerName(String consumerName) {
      this.consumerName = consumerName;
   }

   public void setLastUpdateTime(long lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
   }

   public int getPrevMsg() {
      return prevMsg;
   }

   public void setPrevMsg(int prevMsg) {
      this.prevMsg = prevMsg;
   }

   public JMSMessageListener(int maxThreadCount)
   {
      pool = new PooledExecutor(buffer, maxThreadCount);
      if (discardOldestWhenBlocked)
        pool.discardOldestWhenBlocked();
   }

   public void onMessage(Message m)
   {
      try
      {
         BytesMessage message=(BytesMessage)m;
         currentMsg = message.readInt();

         long currTime = System.currentTimeMillis();

         lastUpdateTime = currTime;

         if (currentMsg < 0) return;

         if (currentMsg != (prevMsg+1)) {
            locallost+=(currentMsg - prevMsg);
         }

         prevMsg = currentMsg;

         localcount++;

         if ( (currTime - beginTime) > 60000) {
            int localsent = localcount+locallost;
            double localconsumed = 1 - ((double)(locallost) / (double)(localsent));
            log.debug(consumerName+" : Msgs sent: "+(localcount+locallost)+" Msgs received: "+ localcount+" consumed: "+localconsumed);
            log.debug(consumerName+" Active thread count : "+Thread.activeCount());
            //Runtime runtime = Runtime.getRuntime();
            //log.debug("Available memory: "+runtime.totalMemory());
            //log.debug("Free memory % : "+(((double)runtime.freeMemory()/(double)runtime.totalMemory())*100));
            //log.debug("JMSMessageListener - "+consumerName+" : thread pool stats: \n"+pool.getStats());
            beginTime = currTime;
            localcount=0;
            locallost=0;
         }

         MessageHandler mh = (MessageHandler)messageHandler.clone();
         mh.setMessage(m);
         pool.execute(mh);

      } catch(Exception e) {
         log.error("JMSMessageConsumer.onMessage - Error encountered processing message.",e);
      }
   }
}

class QuoteConsumer
{
   private static Log log = LogFactory.getLog(QuoteConsumer.class);

   TopicSession session = null;
   Topic topic = null;
   TopicSubscriber subscriber = null;
   String client = null;
   String consumerName = null;
   MessageListener messageListener;

   public QuoteConsumer(String clientID, String ip, String topicName, MessageListener messageListener, 
                        String qos, String consumerName)
   {
      client  = clientID;
      this.consumerName = consumerName;
      this.messageListener = messageListener;

      log.debug("JMSMessageConsumer.QuoteConsumer -  **** Starting consumer : "+consumerName);
      log.debug("JMSMessageConsumer.QuoteConsumer -  **** IP : \""+ip+"\"");

      try {

         int index = qos.indexOf("?");

         if (index <= 0) {
            log.debug("QuoteConsumer - "+consumerName+" unable to locate replacement character \"?\" for ip address swap.");
            return;
         }

         String qualityOfService = qos.substring(0,index) + ip + qos.substring(index+1);

         log.debug("QuoteConsumer - "+consumerName+" qos - "+qualityOfService);

         //IBusJMSContext.getTopicConnection().setClientID(client);

         session = IBusJMSContext.createTopicSession(false,TopicSession.AUTO_ACKNOWLEDGE);
         topic = IBusJMSContext.getTopic(topicName);

         ((IBusTopic)topic).setQOS(qualityOfService);

         log.debug("QuoteConsumer -  **** Current thread count : "+Thread.activeCount());

         subscriber = session.createSubscriber(topic);
         subscriber.setMessageListener(messageListener);
         IBusJMSContext.getTopicConnection().start();
      }
      catch (Exception e) {
         log.error("QuoteConsumer - "+consumerName+" Error setting up JMS environment.",e);
      }
   }

   public void stopConsumption()
   {
      try {
         log.debug("QuoteConsumer.stopConsumption - "+consumerName+" **** Closing consumer : "+client);
         subscriber.close();
      }
      catch (Exception e) {
         log.error("QuoteConsumer.stopConsumption - "+consumerName+" Error encountered.",e);
      }
   }
}

