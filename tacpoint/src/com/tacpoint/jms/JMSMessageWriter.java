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

public class JMSMessageWriter extends Object implements Runnable, TConsumer
{
   private static Log log = LogFactory.getLog(JMSMessageWriter.class);


   private MessageHandler messageHandler = null;
   private MessageReducer messageReducer = null;
   private ReducedMessageInflator messageInflator = null;

   private TMessageQueue queue;

   private JMSMessageWriterListener messageListenerP = null;

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
   public JMSMessageWriter() throws Exception
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
         log.error("JMSMessageWriter - Could not add shutdown hook", e);
      }
   }

   public void run()
   {
      try
      {


         JMSMessageWriterConsumer qcPrimary = null;

         messageListenerP = new JMSMessageWriterListener(aggregationTime);
         messageListenerP.setMessageHandler(messageHandler);
         messageListenerP.setMessageReducer(messageReducer);
         messageListenerP.setMessageInflator(messageInflator);
         messageListenerP.setMessageQueue(queue);
         messageListenerP.setConsumerName(consumerName+"_P");

         qcPrimary = new JMSMessageWriterConsumer(clientID+"_P", primaryIP, topicName, messageListenerP, qos, consumerName+"_P");
         messageListenerP.setPrevMsg(0);

      }
      catch (Exception e)
      {
         log.error("JMSMessageWriter.run() " + e.getMessage());
      }
   }

}

class JMSMessageWriterListener implements MessageListener
{
   private static Log log = LogFactory.getLog(JMSMessageWriterListener.class);

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

   private FileOutputStream fos;

   private long aggregationTime = 0;

   private int msgCount = 0;


   public MessageReducer getMessageReducer() {
      return messageReducer;
   }

   public void setMessageReducer(MessageReducer messageReducer) {
      this.messageReducer = messageReducer;
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

   public JMSMessageWriterListener(long aggregationTime)
   {
      this.aggregationTime = aggregationTime;
   }

   public void onMessage(Message m)
   {
      try
      {

         if (fos == null) {
            fos = new FileOutputStream("US_Equity_Data.out");
         }

         BytesMessage message=(BytesMessage)m;
         currentMsg = message.readInt();

         int bytesRead = 0;
         String topic = message.readUTF();

         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         while ((bytesRead = message.readBytes(bytes)) != -1)
         {
            baos.write(bytes,0,bytesRead);
         }

         fos.write(baos.toByteArray());
         fos.write(10); // newline

      } catch(Exception e) {
         log.error("JMSMessageWriter.onMessage - Error encountered processing message.",e);
      }
   }
}

class JMSMessageWriterConsumer
{
   private static Log log = LogFactory.getLog(JMSMessageWriterConsumer.class);

   TopicSession session = null;
   Topic topic = null;
   TopicSubscriber subscriber = null;
   String client = null;
   String consumerName = null;
   MessageListener messageListener;

   public JMSMessageWriterConsumer(String clientID, String ip, String topicName, MessageListener messageListener,
                        String qos, String consumerName)
   {
      client  = clientID;
      this.consumerName = consumerName;
      this.messageListener = messageListener;

      log.debug("JMSMessageWriter.JMSMessageWriterConsumer -  **** Starting consumer : "+consumerName);
      log.debug("JMSMessageWriter.JMSMessageWriterConsumer -  **** IP : \""+ip+"\"");

      try {

         int index = qos.indexOf("?");

         if (index <= 0) {
            log.debug("JMSMessageWriterConsumer - "+consumerName+" unable to locate replacement character \"?\" for ip address swap.");
            return;
         }

         String qualityOfService = qos.substring(0,index) + ip + qos.substring(index+1);

         log.debug("JMSMessageWriterConsumer - "+consumerName+" qos - "+qualityOfService);

         //IBusJMSContext.getTopicConnection().setClientID(client);

         session = IBusJMSContext.createTopicSession(false,TopicSession.AUTO_ACKNOWLEDGE);
         topic = IBusJMSContext.getTopic(topicName);

         ((IBusTopic)topic).setQOS(qualityOfService);

         log.debug("JMSMessageWriterConsumer -  **** Current thread count : "+Thread.activeCount());

         subscriber = session.createSubscriber(topic);
         subscriber.setMessageListener(messageListener);
         IBusJMSContext.getTopicConnection().start();
      }
      catch (Exception e) {
         log.error("JMSMessageWriterConsumer - "+consumerName+" Error setting up JMS environment.",e);
      }
   }

   public void stopConsumption()
   {
      try {
         log.debug("JMSMessageWriterConsumer.stopConsumption - "+consumerName+" **** Closing consumer : "+client);
         subscriber.close();
      }
      catch (Exception e) {
         log.error("JMSMessageWriterConsumer.stopConsumption - "+consumerName+" Error encountered.",e);
      }
   }
}
