package com.tacpoint.jms;

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import java.io.*;
import java.net.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;

public class TCPMessageConsumer extends Object implements Runnable
{
   private static Log log = LogFactory.getLog(TCPMessageConsumer.class);
   private static final int DEFAULT_THREAD_POOL_SIZE = 10;
   
   private MessageHandler messageHandler = null;


   private String consumerName;

   private String clientID;

   private String topicName;

   private String primaryIP;

   private String secondaryIP;

   private String qos;
   
   private String ipAddress;
   
   private int portNumber;
   

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
   public TCPMessageConsumer() throws Exception
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
         log.error("TCPMessageConsumer - Could not add shutdown hook", e);
      } 
   }

   public void run()
   {
      try
      {

         InputStream is;
         Socket socket;
         long msgCount = 0;
         long curTime = 0;
         long beginTime = 0;

         PooledExecutor pool;
         BoundedBuffer buffer = new BoundedBuffer(20000);
         pool = new PooledExecutor(buffer,1);
         //TopicSession session = IBusJMSContext.createTopicSession(false,TopicSession.AUTO_ACKNOWLEDGE);
         
         socket = new Socket(ipAddress, portNumber);
         is = socket.getInputStream();
         
         byte[] response = new byte[1024];

         int messageTerminatorByte = 63;
         int bytesRead = 0;
         msgCount = 0;
         curTime=0;
         beginTime=0;

         ByteArrayOutputStream baos = new ByteArrayOutputStream(110);

         while ( true ) {

            try {
 
               bytesRead = is.read(response);

               if (bytesRead < 0) {
                  System.out.println("Connection broken, exiting run method.");
                  break;
               }

               for (int i=0; i<bytesRead; i++) {

                  if (response[i] == messageTerminatorByte) {

                     //baos.write(response,i,1);
                     msgCount++;

                     //System.out.println(new String(baos.toByteArray()));

                     MessageHandler mh = (MessageHandler)messageHandler.clone();
                     mh.setMessage(baos.toByteArray());
                     pool.execute(mh);

                     baos.reset();
                     curTime = System.currentTimeMillis();
                     if ( (curTime - beginTime) > 1000) {
                        beginTime = curTime;
                        System.out.println("Messages received : "+msgCount);
                        msgCount = 0;
                     }
                  }
                  else {
                     baos.write(response,i,1);
                  }
               }
            }
            catch (Exception e) {
               e.printStackTrace();
            }
            
         }
      }
      catch (Exception e)
      {
         log.error("TCPMessageConsumer.run() " + e.getMessage());
      }
   }

   // main function
   public static void main(String[] args)
   {
      try
      {
         TCPMessageConsumer jmsMsgConsumer = new TCPMessageConsumer();
         jmsMsgConsumer.run();
      }
      catch(Exception e)
      {
         System.out.println("TCPMessageConsumer.main() Exception " + e.getMessage());
      }
   }

/**
 * @return Returns the ipAddress.
 */
public String getIpAddress() {
	return ipAddress;
}

/**
 * @param ipAddress The ipAddress to set.
 */
public void setIpAddress(String ipAddress) {
	this.ipAddress = ipAddress;
}

/**
 * @return Returns the portNumber.
 */
public int getPortNumber() {
	return portNumber;
}

/**
 * @param portNumber The portNumber to set.
 */
public void setPortNumber(int portNumber) {
	this.portNumber = portNumber;
}
}
