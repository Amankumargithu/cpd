/**
  * TMonitorLogger.java
  *
  * @author Copyright (c) 2004 by Tacpoint Technologie, Inc.
  *    All rights reserved.
  * @version 1.0
  */

package com.tacpoint.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class TMonitorLogger implements Runnable
{
   private static Log log = LogFactory.getLog(TMonitorLogger.class);
   private TPublisher publisher;
   private int sleepTime;
   
   public TMonitorLogger(TPublisher p)
   {
      publisher = p;
      TPublisherConfigBean bean = publisher.getConfiguration();
      sleepTime = bean.getHeartbeatIntervalInSeconds() * 1000 * 3600;
   }
   
   public void run()
   {
      try
      {
         while (true)
         {
            Thread.sleep(sleepTime);
            if (publisher != null)
            {
               log.info(publisher.getMonitorStats());
            }
         }
      }
      catch (Exception e)
      {
         log.error("THeartbeat.run() exception " + e.getMessage());
      }
   }
}
