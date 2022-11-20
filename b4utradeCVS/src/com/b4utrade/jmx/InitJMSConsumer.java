package com.b4utrade.jmx;

import com.b4utrade.stock.*;
import com.tacpoint.util.*;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;

public class InitJMSConsumer implements InitJMSConsumerMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=InitJMSConsumer";
   private static Logger log = Logger.getLogger(InitJMSConsumer.class);

   private Thread t;

   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {

      try {

         Environment.init();

         log.info("InitJMSConsumer - about to instantiate MM2 JMSMessageConsumer!");

         com.tacpoint.jms.JMSMessageConsumer mc = new com.tacpoint.jms.JMSMessageConsumer();

         mc.setMessageHandler(new com.b4utrade.stock.MM2MessageHandler());
         mc.setClientID(Environment.get("JMS_MM2_CLIENT_ID"));
         mc.setPrimaryIP(Environment.get("JMS_MM2_PRIMARY_IP"));
         mc.setSecondaryIP(Environment.get("JMS_MM2_SECONDARY_IP"));
         mc.setTopicName(Environment.get("JMS_MM2_TOPIC_NAME"));
         mc.setQOS(Environment.get("JMS_MM2_QOS"));
         mc.setConsumerName("MM2 Data");

         t = new Thread(mc);
         t.setDaemon(true);
         t.start();

         log.info("InitJMSConsumer - successfully instantiated MM2 JMSMessageConsumer!");

      }
      catch (Exception e) {
         e.printStackTrace();
      }

      return new ObjectName(OBJECT_NAME);
   }
   
   public void postRegister(Boolean registrationDone)
   {
      // empty
   }
   
   public void preDeregister() throws Exception
   {
      // empty
   }
   
   public void postDeregister()
   {
      // empty
   }
   
}
