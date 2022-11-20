package com.b4utrade.jmx;

import com.b4utrade.stock.*;
import com.tacpoint.util.*;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;

public class InitTCPMM2Consumer implements InitTCPMM2ConsumerMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=InitTCPMM2Consumer";
   private static Logger log = Logger.getLogger(InitTCPMM2Consumer.class);

   private Thread t;

   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {

      try {

         Environment.init();

         log.info("InitTCPMM2Consumer - about to instantiate MM2 TCPMessageConsumer!");

         com.tacpoint.jms.TCPMessageConsumer mc = new com.tacpoint.jms.TCPMessageConsumer();

         mc.setMessageHandler(new com.b4utrade.stock.MM2MessageHandler());
         
         mc.setIpAddress(Environment.get("MM2_SERVER_IP_ADDRESS"));
         mc.setPortNumber(Integer.parseInt(Environment.get("MM2_SERVER_PORT_NUMBER")));
         
         
         /*
         mc.setClientID(Environment.get("JMS_MM2_CLIENT_ID"));
         mc.setPrimaryIP(Environment.get("JMS_MM2_PRIMARY_IP"));
         mc.setSecondaryIP(Environment.get("JMS_MM2_SECONDARY_IP"));
         mc.setTopicName(Environment.get("JMS_MM2_TOPIC_NAME"));
         mc.setQOS(Environment.get("JMS_MM2_QOS"));
         mc.setConsumerName("MM2 Data");
         */

         t = new Thread(mc);
         t.setDaemon(true);
         t.start();

         log.info("InitTCPMM2Consumer - successfully instantiated MM2 TCPMessageConsumer!");

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
