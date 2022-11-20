package com.tacpoint.jms;

import java.util.*;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tacpoint.publisher.TMessage;
import com.tacpoint.publisher.TMessageQueue;
import com.tacpoint.publisher.TDispatcher;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;

public class JMSMessageReader extends Object implements Runnable, TConsumer
{
   private static Log log = LogFactory.getLog(JMSMessageReader.class);

   private MessageHandler messageHandler = null;
   private MessageReducer messageReducer = null;
   private ReducedMessageInflator messageInflator = null;

   private TMessageQueue queue;

   private QueuedJMSMessageReader messageListenerP = null;

   private String consumerName;

   private String clientID;

   private String topicName;

   private String primaryIP;

   private String qos;

   private long aggregationTime = 0;

   public void setAggregationTime(long at) {
      this.aggregationTime = at;
   }

   public void setMessageQueue(TMessageQueue queue) {
      this.queue = queue;
   }

   public void setConsumerName(String consumerName) {
      this.consumerName = consumerName;
   }

   public void setQOS(String qos) {
      this.qos = qos;
   }

   public void setMessageHandler(MessageHandler messageHandler) {
      this.messageHandler = messageHandler;
   }

   public void setMessageReducer(MessageReducer messageReducer) {
      this.messageReducer = messageReducer;
   }

   public void setMessageInflator(ReducedMessageInflator messageInflator) {
      this.messageInflator = messageInflator;
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

   public String  getConsumerName() {
      return consumerName;
   }

   public String  getQOS() {
      return qos;
   }

   public MessageHandler getMessageHandler() {
      return messageHandler;
   }

   public MessageReducer getMessageReducer() {
      return messageReducer;
   }

   public ReducedMessageInflator getMessageInflator() {
      return messageInflator;
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
   public JMSMessageReader() throws Exception
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
         log.error("JMSMessageReader - Could not add shutdown hook", e);
      }
   }

   public void run()
   {
      try
      {
         messageListenerP = new QueuedJMSMessageReader(aggregationTime);
         messageListenerP.setMessageHandler(messageHandler);
         messageListenerP.setMessageReducer(messageReducer);
         messageListenerP.setMessageInflator(messageInflator);
         messageListenerP.setMessageQueue(queue);
         messageListenerP.setConsumerName(consumerName+"_P");
         messageListenerP.setPrevMsg(0);

         log.debug("#####################################################################################");
         log.debug("### JMSMessageReader.run - sleeping for 20 seconds to allow consumers to connect! ###");
         log.debug("#####################################################################################");
         
         try {
            Thread.sleep(30000);
         }
         catch (InterruptedException iex) {}

         log.debug("Master Sub Size : "+TDispatcher.MASTER_SUBSCRIPTIONS.keySet().size());
         Iterator it = TDispatcher.MASTER_SUBSCRIPTIONS.keySet().iterator();
         while (it.hasNext()) {
            log.debug("Master key : "+it.next());
         }

         FileInputStream fis = new FileInputStream("/home/sample_data/US_Equity_Data.out");
         BufferedInputStream bis = new BufferedInputStream(fis,2048);
         
         byte[] bytes = new byte[2048];
         int bytesRead = 0;

         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         int recCount = 0;
         int totRecCount = 0;

         long bpt = 0;
         while (true) {

            while ((bytesRead = bis.read(bytes,0,512)) != -1)
            {

               long cpt = System.currentTimeMillis();
               if ( (cpt - bpt) > 1000) {
                  log.debug("JMSMessageReader.run - msgs processed in cycle : "+recCount);
                  log.debug("JMSMessageReader.run - total msgs processed    : "+totRecCount);
                  recCount = 0;
                  bpt = cpt;
               }

               for (int i=0; i<bytesRead; i++) {

                  if (bytes[i] == 10 /* newline */) {

                     recCount++;
                     totRecCount++;

                     String key = findTopic(baos.toByteArray());

                     if (key == null) {
                     baos.reset();
                        continue;
                     }


                     if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key)) {
                        messageListenerP.processMessage(key,baos.toByteArray());
                     }

                     // step it down ...
                     if (totRecCount % 35 == 0) {
                        try {
                           Thread.sleep(1);
                        }
                        catch (InterruptedException iex) {}
                     }

                     baos.reset();
                  }
                  else {
                     baos.write(bytes,i,1);
                  }
               }
            }

            // reset the file ...
            try {
               bis.close();
            }
            catch (Exception cex) {
               cex.printStackTrace();
            }
                
            try {
               fis.close();
            }
            catch (Exception cex) {
               cex.printStackTrace();
            }

            fis = new FileInputStream("/home/sample_data/US_Equity_Data.out");
            bis = new BufferedInputStream(fis,2048);
         }
      }
      catch (Exception e)
      {
         log.error("JMSMessageReader.run() " + e.getMessage());
      }
   }

   public String findTopic(byte[] message) {
   
      try {
         
         int length = 0;
         for (int i=2; i<message.length; i++) {
            if (message[i] == 44) break;
            length++;
         }
     
         return new String(message,2,length);

      }
      catch (Exception e) {
         log.error("QueuedJMSMessageReader.findTopic - Error encountered processing message.",e);
         return null;
      }
   }
}

class QueuedJMSMessageReader implements MessageListener
{
   private static Log log = LogFactory.getLog(QueuedJMSMessageReader.class);

   private Properties props = new Properties();

   private MessageHandler  messageHandler = null;
   private MessageReducer  messageReducer = null;
   private ReducedMessageInflator messageInflator = null;

   byte[] bytes = new byte[1024];

   private long beginTime = 0;
   private long lastUpdateTime = 0;
   private long lastResetTime = 0;
   private int prevMsg=0;
   private int currentMsg=0;
   private int locallost=0;
   private int localcount=0;

   private long nonOptMsgByteCount;
   private long optMsgByteCount;
   private long binMsgByteCount;

   private String consumerName=null;
   private TMessageQueue messageQueue;
   private String terminator = "?";

   private long aggregationTime = 0;

   private int msgCount = 0;

   private QueueReaderReleaser queueReleaser;
   private Thread queueReleaserThread;

   public MessageReducer getMessageReducer() {
      return messageReducer;
   }

   public void setMessageReducer(MessageReducer messageReducer) {
      this.messageReducer = messageReducer;
      if (queueReleaser != null)
         queueReleaser.setMessageReducer(messageReducer);
   }

   public void setMessageInflator(ReducedMessageInflator messageInflator) {
      this.messageInflator = messageInflator;
   }

   public MessageHandler getMessageHandler() {
      return messageHandler;
   }

   public void setMessageHandler(MessageHandler messageHandler) {
      this.messageHandler = messageHandler;
   }

   public void setMessageQueue(TMessageQueue mq) {
      this.messageQueue = mq;
      if (queueReleaser != null)
         queueReleaser.setMessageQueue(mq);
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

   public QueuedJMSMessageReader(long aggregationTime)
   {
      this.aggregationTime = aggregationTime;

      queueReleaser = new QueueReaderReleaser();
      queueReleaser.setAggregationTime(aggregationTime);

      queueReleaserThread = new Thread(queueReleaser);
      queueReleaserThread.start();
   }

   public void onMessage(Message m)
   {
   }


   public void processMessage(String topic, byte[] message) {
      try {

         if (messageReducer != null) {

            ByteArrayOutputStream compactedBaos = messageReducer.reduceMessage(topic,message);

            if (compactedBaos == null) {
               log.error("Unable to compact message from raw MSG : "+new String(message));
               return;
            }

            nonOptMsgByteCount += message.length;
            optMsgByteCount += compactedBaos.toByteArray().length;

            TMessage msg = new TMessage();
            msg.setMap(messageReducer.parseKeyValues(compactedBaos));

            compactedBaos.write(terminator.getBytes());
            msg.setKey(topic);
            msg.setBaos(compactedBaos);

            //System.out.println("ADDING MSG : "+new String(compactedBaos.toByteArray()));

            messageQueue.add(msg);

            messageReducer.resetAttribute("O" /* open price */,"N/A");
            messageReducer.resetAttribute("lp" /* last closed price */,"N/A");
            messageReducer.resetAttribute("sp" /* settlement price */,"N/A");

         }

      } catch(Exception e) {
         log.error("JMSMessageReader.onMessage - Error encountered processing message.",e);
      }
   }
}

class QueueReaderReleaser implements Runnable {

   long aggregationTime = 0;
   TMessageQueue messageQueue;
   MessageReducer messageReducer;
   LinkedHashMap map = new LinkedHashMap();
   Object lock = new Object();
   TMessage prevMsg = null;

   int msgCount = 0;


   int dups = 0;
   int additions = 0;

   private static Log log = LogFactory.getLog(QueueReaderReleaser.class);

   public void setMessageQueue(TMessageQueue messageQueue) {
      this.messageQueue = messageQueue;
   }

   public void setMessageReducer(MessageReducer messageReducer) {
      this.messageReducer = messageReducer;
   }

   public void add(TMessage msg) {
      synchronized(lock) {

         additions++;


         // **AK** 
         /*
         String ticker = (String)msg.getKey();
         if (ticker != null) {
            if (ticker.equals("EGN") || 
                ticker.equals("END") || 
                ticker.equals("EP")  || 
                ticker.equals("EPD") || 
                ticker.equals("EPE") ) {

               System.out.println(new java.util.Date()+" QueueReaderReleaser.add msg stream - "+new String(msg.getBaos().toByteArray()));
            } 
         }
         */

         if (messageReducer != null) {
            prevMsg = (TMessage)map.get(msg.getKey());
            if (prevMsg != null) {
               dups++;
               messageReducer.conflateReducedMessages(prevMsg.getBaos(), prevMsg.getMap(), msg.getMap());

               // **AK** 
               /*
               if (ticker != null) {
                  if (ticker.equals("EGN") || 
                      ticker.equals("END") || 
                      ticker.equals("EP")  || 
                      ticker.equals("EPD") || 
                      ticker.equals("EPE") ) {

                     System.out.println(new java.util.Date()+" QueueReaderReleaser.add post conflate - "+
                                        new String(prevMsg.getBaos().toByteArray()));
                  } 
               }
               */

            }
            else {
               map.put(msg.getKey(),msg);
            }
         }
         else {
            if (map.containsKey(msg.getKey()))
               dups++;
            map.put(msg.getKey(),msg);
         }
      }
   }

   public void setAggregationTime(long at) {
      this.aggregationTime = at;
   }

   public int getMsgCount() {
      return msgCount;
   }

   public void resetMsgCount() {
      msgCount = 0;
   }

   public void run() {

      if (aggregationTime <= 0) {
         log.error("Invalid aggregation time valiu ["+aggregationTime+"] - must be greater than zero.");
         return;
      }

      log.debug("Queue Releaser aggregation time : "+aggregationTime);

      Iterator it = null;

      long curTime = 0;
      long beginTime = 0;

      while (true) {

         try {
            Thread.sleep(aggregationTime);


            if (map.size() > 0  && messageQueue != null) {
               synchronized(lock) {
                  it = map.values().iterator();
                  while (it.hasNext()) {

                     TMessage item = (TMessage)it.next();
                     messageQueue.add(item);

                     //messageQueue.add(it.next());
                     msgCount++;

                     // **AK** 
                     /*
                     String ticker = (String)item.getKey();

                     if (ticker != null) {
                        if (ticker.equals("EGN") || 
                            ticker.equals("END") || 
                            ticker.equals("EP")  || 
                            ticker.equals("EPD") || 
                            ticker.equals("EPE") ) {

                           System.out.println(new java.util.Date()+" QueueReaderReleaser.run - "+
                                              new String(item.getBaos().toByteArray()));
       
                        } 
                     }
                     */
                  }
                  map.clear();
               }
            }

            curTime = System.currentTimeMillis();
            if ( (curTime - beginTime) > 5000) {
               beginTime = curTime;
               log.debug("Queue Releaser - number of records processed    : "+additions);
               log.debug("Queue Releaser - number of duplicates discarded : "+dups);
               log.debug("Queue Releaser - % decrease "+((double)dups/(double)additions));
               dups = 0;
               additions = 0;
            }
         }
         catch (Exception ex) {
            log.error("Unable to disperse messages from mapped queue - "+ex.getMessage(),ex);
         }
      }
   }
}




