/** DataFeedServer.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 01.23.2000
* Date modified:
*/

package com.tacpoint.datafeed;

import com.tacpoint.util.*;
import com.tacpoint.configuration.*;
import com.tacpoint.messagequeue.*;
import com.tacpoint.workflow.*;
import com.tacpoint.backoffice.*;

/** DataFeedServer class
* The DataFeedServer class will be main driver for all the data feeds.
* This class will instantiate all other classes.
*/
public class DataFeedServer extends Object
{
   private static final boolean mDebug = true;

   public static void main(String[] args)
   {
      // create a new data feed instance.
      try
      {
         Logger.init();

         // Delete when testing is done!
         Logger.debug("DataFeedServer Main()", mDebug);

         // create thread group to manage all work flow threads
         ThreadGroup vWorkFlowGroup = new ThreadGroup("WorkFlowGroup");
         
         // create message queue
         MessageQueue vRecordMessageQueue = new MessageQueue();
         MessageQueue vParsedMessageQueue = new MessageQueue();

         // start up admin daemon thread to manage the data feed server.
         DataFeedAdminDaemon vAdmin = new DataFeedAdminDaemon(
               vRecordMessageQueue, vParsedMessageQueue);
         Thread vAdminThread = new Thread(vWorkFlowGroup, vAdmin);
         vAdminThread.setDaemon(true);
         vAdminThread.start();
         
         WorkFlowTask vTask = WorkFlowFactory.createTask(WorkFlowTask.RECORD_SPLIT);
         vTask.setMessageQueue(null, vRecordMessageQueue);
         if (!vTask.init())
            throw new BadDataFeedCreation("Unable to initialize task.");
         Thread vDataFeedThread = new Thread(vWorkFlowGroup, vTask);
         
         // 8/16/11 AK moved starting of this thread until after the parser and distributor have been initialized to prevent their queues from building 
         //
         //vDataFeedThread.start();

         // spin off thread to parse message
         // TODO:
         // 1. check message queue, if queue is over certain number of entries
         //    then automatically spin off another thread
         // 2. read config file to get the number of parser to run
         int vParseCount = getParserThreadCount();
         Thread[] vParseThreads = new Thread[vParseCount];
         for (int i = 0; i < vParseCount; i++)
         {
            WorkFlowTask vParseTask = WorkFlowFactory.createTask(WorkFlowTask.PARSE);
            vParseTask.setMessageQueue(vRecordMessageQueue, vParsedMessageQueue);
            if (!vParseTask.init())
            {
               Logger.log("Unable to initialize parser task.");
               continue;
            }
            vParseThreads[i] = new Thread(vWorkFlowGroup, vParseTask);
//          vParseThreads[i].setPriority(7);
            vParseThreads[i].start();
         }
         
         // spin off thread to distribute data
         WorkFlowTask vDistributeTask = WorkFlowFactory.createTask(WorkFlowTask.DISTRIBUTE);
         vDistributeTask.setMessageQueue(vParsedMessageQueue, null);
         vDistributeTask.init();
//         vDistributeTask.run();
         //vDistributeTask.setMessageQueue(vParsedMessageQueue, null);
         Thread vDistributeThread = new Thread(vWorkFlowGroup, vDistributeTask);
         //vDistributeThread.setPriority(8);
         vDistributeThread.start();
         
         try {
        	 Thread.sleep(2000);
         }
         catch (InterruptedException iex) {}
         
         vDataFeedThread.start();

         int vOtherThreadCount = getOtherThreadCount();
         if (vOtherThreadCount > 0)
         {
            Thread[] vOtherThreads = new Thread[vOtherThreadCount];
            for (int i = 0; i < vParseCount; i++)
            {
               WorkFlowTask vOtherTask = WorkFlowFactory.createTask(WorkFlowTask.OTHER_TASK+(i+1));
               vOtherTask.setMessageQueue(vRecordMessageQueue, vParsedMessageQueue);
               if (!vOtherTask.init())
               {
                  Logger.log("Unable to initialize " + WorkFlowTask.OTHER_TASK+i);
                  continue;
               }
               vOtherThreads[i] = new Thread(vWorkFlowGroup, vOtherTask);
               vOtherThreads[i].start();
            }
         }

      }
      catch(BadDataFeedCreation vBadDFException)
      {
         Logger.log(vBadDFException.getMessage());
      }
      catch(NumberFormatException vFormatError)
      {
         Logger.log(vFormatError.getMessage());
      }
      catch(Exception e)
      {
         Logger.log(e.getMessage());
      }
   }
   
   static private int getParserThreadCount() throws Exception
   {
      DataFeedSpecifier vDataFeedJob = DataFeedSpecifier.getDataFeedSpecifier();
      String vParseCountStr = vDataFeedJob.getFieldValue("DATAFEED", "PARSE_COUNT");
      if (vParseCountStr == null || vParseCountStr.length() == 0)
         throw new Exception("Unable to retrieve parse count.");
         
      int vParseCount = Integer.parseInt(vParseCountStr);
      
      return (vParseCount);
   }

   static private int getOtherThreadCount()
   {
      int vThreadCount = 0;
      try
      {
         DataFeedSpecifier vDataFeedJob = DataFeedSpecifier.getDataFeedSpecifier();
         String vOtherThread = vDataFeedJob.getFieldValue("DATAFEED", "OTHER_THREAD_COUNT");
         if (vOtherThread == null || vOtherThread.length() == 0)
            return 0;

         vThreadCount = Integer.parseInt(vOtherThread);
      }
      catch(Exception e) { }

      return (vThreadCount);
   }

}
