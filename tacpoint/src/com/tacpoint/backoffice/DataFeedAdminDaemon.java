/** DataFeedAdminDaemon.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created:  01/31/2000
*
*/
package com.tacpoint.backoffice;

import java.io.*;
import java.net.*;
import java.util.*;
import com.tacpoint.util.*;
import com.tacpoint.configuration.*;
import com.tacpoint.messagequeue.*;
import com.tacpoint.comstock.*;
/**
 * DataFeedAdminDaemon class is used to control the data feed server.
 */
public class DataFeedAdminDaemon extends Object implements Runnable
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A    M E M B E R S
   ////////////////////////////////////////////////////////////////////////////
   Socket mClient = null;
   ServerSocket mServer = null;

   InputStream  mInputStream  = null;
   OutputStream mOutputStream = null;

   private MessageQueue mQueue1;
   private MessageQueue mQueue2;


   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////
   public DataFeedAdminDaemon(MessageQueue aQueue1, MessageQueue aQueue2) throws Exception
   {
      Logger.init();
      Environment.init();

      // Delete when testing is done!
      Logger.log("DataFeedAdminDaemon Constructor");

      mQueue1 = aQueue1;
      mQueue2 = aQueue2;

      if (Boolean.valueOf(Environment.get("RUN_COMSTOCK_STATS")).booleanValue()) {

        String statsClass = Environment.get("COMSTOCK_PRODUCER_STATS_CLASS");
        if (statsClass != null)
        {
            Logger.log("DataFeedAdminDaemon Constructor: trying to create the thread");
            ComstockStatsHelper csh = (ComstockStatsHelper)Class.forName(statsClass).newInstance();
            csh.init(this);
            //statsThread = new Thread(csh);
            //statsThread.start();
            Logger.log("DataFeedAdminDaemon Constructor: the thread has been created");
        }
      }

   }

   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////
   public MessageQueue getQueue1()
   {
       return mQueue1;
   }
   public MessageQueue getQueue2()
   {
       return mQueue2;
   }
   public void run()
   {
      try
      {
         boolean vDone = false;

         final String vWelcome = new String("\r\nDataFeed Server Backoffice\r\n");
         final String vBye = new String("\r\nBye!\r\n");
         final String vHelp = new String("\r\nQUITE_APP\r\nCLOSE\r\nREAD_CONFIG\r\nPARSER_COUNT\r\nQUEUE_1_COUNT\r\nQUEUE_2_COUNT\r\n");

         NetworkUtility vUtil = NetworkUtility.getInstance();
         mServer = new ServerSocket(vUtil.getPortNumber("ADMIN", "PORT"));

         while(! vDone)
         {
            mClient = mServer.accept();

            mInputStream  = mClient.getInputStream();
            mOutputStream = mClient.getOutputStream();

            mOutputStream.write(vWelcome.getBytes());
            mOutputStream.write(vHelp.getBytes());
            mOutputStream.flush();

            boolean vDoneReading = false;
            String vCommand = null;
            byte[] vByteRead = new byte[50];
            int vNumBytesRead = 0;

            while (! vDoneReading)
            {
               try
               {
                  vNumBytesRead = mInputStream.read(vByteRead);
               }
               catch(IOException ioe)
               {
                  System.out.println("exception in reading inputstream");
               }

               vCommand = new String(vByteRead, 0 /*offset*/, vNumBytesRead);
               StringTokenizer vReadToken = new StringTokenizer(vCommand, "\r\n");
               vCommand = vReadToken.nextToken();

               if (vCommand.equalsIgnoreCase(DataFeedAdminCommand.PARSER_COUNT))
               {
                  //vOutputStream.writeUTF("\r\nPARSER THREAD COUNT\r\n");
                  //vOutputStream.flush();
               }
               else if (vCommand.equalsIgnoreCase(DataFeedAdminCommand.QUEUE_1_COUNT))
               {
                  int vCount = mQueue1.getSize();
                  String vCountStr = new String("\r\nQueue 1 count = " + vCount + "\r\n");
                  mOutputStream.write(vCountStr.getBytes());
                  mOutputStream.flush();
               }
               else if (vCommand.equalsIgnoreCase(DataFeedAdminCommand.QUEUE_2_COUNT))
               {
                  int vCount = mQueue2.getSize();
                  String vCountStr = new String("\r\nQueue 2 count = " + vCount + "\r\n");
                  mOutputStream.write(vCountStr.getBytes());
                  mOutputStream.flush();
               }
               else if (vCommand.equalsIgnoreCase(DataFeedAdminCommand.READ_CONFIG))
               {
                  DataFeedAdminCommand.gReadConfig = true;
               }
               else if (vCommand.equalsIgnoreCase(DataFeedAdminCommand.CLOSE))
               {
                  mOutputStream.write(vBye.getBytes());
                  mOutputStream.flush();

                  closeClient();
                  // get out of loop
                  vDoneReading = true;
               }
               else if (vCommand.equalsIgnoreCase(DataFeedAdminCommand.QUIT_APP))
               {
                  DataFeedAdminCommand.gQuit = true;
                  ThreadGroup vGroup = Thread.currentThread().getThreadGroup();
                  int vActiveCount = vGroup.activeCount();
                  System.out.println("Count = " + vActiveCount);
                  while (vActiveCount > 1)
                  {
                     vActiveCount = vGroup.activeCount();
                  }
                  mOutputStream.write(vBye.getBytes());
                  mOutputStream.flush();

                  // get out of loop
                  vDoneReading = true;
                  vDone = true;
               }
            }
         }
      }
      catch(Exception e)
      {
         System.out.println("Exception" + e.getMessage());
      }
      finally
      {
         try
         {
            closeClient();
            mServer.close();
         }
         catch(Exception e)
         {
         }
      }
   }

   private void closeClient()
   {
      try
      {
         mOutputStream.close();
      }
      catch(IOException ioe)
      {
      }
      try
      {
         mInputStream.close();
      }
      catch(IOException ioe)
      {
      }
      try
      {
         mClient.close();
      }
      catch(IOException ioe)
      {
      }
   }
}
