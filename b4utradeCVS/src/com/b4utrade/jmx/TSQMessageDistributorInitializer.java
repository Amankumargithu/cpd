package com.b4utrade.jmx;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

import com.tacpoint.datafeed.DataFeedServer;
import com.tacpoint.util.Environment;

public class TSQMessageDistributorInitializer implements TSQMessageDistributorInitializerMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=TSQMessageDistributorInitializer";
   private static Logger log = Logger.getLogger(TSQMessageDistributorInitializer.class);

   private DataFeedServer instance;

   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {

      try {

         Environment.init();

         log.info("TSQMessageDistributorInitializer - about to instantiate DataFeedServer instance!");

         instance = new DataFeedServer();
         instance.main(null);

         log.info("TSQMessageDistributorInitializer - successfully instantiated DataFeedServer instance!");
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
