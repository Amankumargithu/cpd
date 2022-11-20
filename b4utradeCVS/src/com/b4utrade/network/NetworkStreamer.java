package com.b4utrade.network;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

import com.tacpoint.http.*;
import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;
import com.tacpoint.util.SystemOutLogger;

public class NetworkStreamer implements Runnable {

   MessageQueue queue;
   MessageQueue newsQueue;
   NetworkConfiguration networkConfiguration;
   HttpPollingHandler httpPolling;
   IStreamer streamer;
   IStreamer defaultStreamer;
   String streamerId= "";
   String userName = "";
   String password = "";
   String url ="";
   String userId="";
   
 //temp
   String streamerType = "N/A";

   long lastProcessTime;

   boolean doRun = true;
   boolean httpPollingIsRunning = false;
   boolean isStreamerConsuming = true;

   int streamerCreationInterval = 30;
   int lastProcessFailedOverTime = 15000;

   public void setLastProcessFailedOverTime(int lastProcessFailedOverTime) {
      this.lastProcessFailedOverTime = lastProcessFailedOverTime;
   }

   public long getLastProcessFailedOverTime() {
      return lastProcessFailedOverTime;
   }

   public void setStreamerId(String streamerId) {
      this.streamerId = streamerId;
   }

   public String getStreamerId() {
      return streamerId;
   }
   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getUserName() {
      return userName;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getUrl() {
      return url;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getPassword() {
      return password;
   }
   
   public void setStremerType(String streamerType) {
	      this.streamerType = streamerType;
	   }

   public String getStreamerType() {
	      return streamerType;
}


   public void setLastProcessTime(long lastProcessTime) {
      this.lastProcessTime = lastProcessTime;
   }

   public long getLastProcessTime() {
      return lastProcessTime;
   }
   public void setStreamerCreationInterval(int streamerCreationInterval) {
      if (streamerCreationInterval > 0)
         this.streamerCreationInterval = streamerCreationInterval;
   }

   public int getStreamerCreationInterval() {
      return this.streamerCreationInterval;
   }

   public void setIsStreamerConsuming(boolean isStreamerConsuming) {
      this.isStreamerConsuming = isStreamerConsuming;
   }

   //set the default streamer, streamer used when cannot get from streamer server
   public void setStreamer(IStreamer streamer) {
      this.defaultStreamer = streamer;
   }
   // retrieve the streamer used in the network.
   public IStreamer getStreamer() {
      return streamer;
   }

   public void setDoRun(boolean doRun) {
      this.doRun = doRun;
   }

   public void setHttpPolling(HttpPollingHandler httpPolling) {
      this.httpPolling = httpPolling;
   }

   public void setNetworkConfiguration(NetworkConfiguration networkConfiguration) {
      this.networkConfiguration = networkConfiguration;
   }

   public void setMessageQueue(MessageQueue queue) {
      this.queue = queue;
   }

   public void setNewsMessageQueue(MessageQueue newsQueue) {
      this.newsQueue = newsQueue;
   }

   public void run() {

      Thread streamerThread = null;
      InputStream inputStream = null;

      try {

         try{
            streamer = retrieveStreamer();
         }catch(Exception e){
             System.out.println("Can not make initial call to streamer...");

	     }
		 if (streamer == null)
		    streamer = defaultStreamer;

         if (streamer == null) {
            SystemOutLogger.error("Unable to use null streamer.");
         }
         else {
        	 System.out.println(new Timestamp(System.currentTimeMillis()) + " Streamer Type: " + streamerType + ", User Id: " + userId + ", Starting streamer thread ...");
            streamer.init(queue, newsQueue, inputStream, networkConfiguration, this);
            streamer.setDoRun(true);
            streamer.setIsBlocking(true);
            streamer.setStreamerId(streamerId);

            streamerThread = new Thread((Runnable)streamer);
            streamerThread.start();
         }

         int sleepCyclesBetweenStreamerCreation = 0;
         lastProcessTime = System.currentTimeMillis();
         while (doRun) {

            try {
               Thread.sleep(1000);
               sleepCyclesBetweenStreamerCreation++;
            }
            catch (InterruptedException ie) {
               if (doRun == false) {
            	   System.out.println(new Timestamp(System.currentTimeMillis()) + " Network Streamer thread has been interrupted ... ");
                  break;
               }
            }

            long now = System.currentTimeMillis();

            if ( (now - lastProcessTime) > (long)lastProcessFailedOverTime ) {

               if (sleepCyclesBetweenStreamerCreation > streamerCreationInterval ) {

            	   System.out.println(new Timestamp(System.currentTimeMillis()) + " Streamer Type: " + streamerType + ", User Id: " + userId +  ", Shutting down existing Streamer thread ...");

                  try {
                     streamerThread.interrupt();
                  }
                  catch (Exception iex) {}

                  streamer.shutDownIO();
                  try{
		            streamer = retrieveStreamer();
			      }catch(Exception e){
                    System.out.println("Can not make initial call to streamer...");

			      }

		          if (streamer == null)
		             streamer = defaultStreamer;


		          System.out.println("Attempting to instantiate new Streamer thread ...");
                  streamer.init(queue, newsQueue, inputStream, networkConfiguration, this);


                  isStreamerConsuming = true;
                  streamer.setDoRun(true);
                  streamer.setIsBlocking(true);
                  streamer.setStreamerId(streamerId);

                  streamerThread = new Thread((Runnable)streamer);
                  streamerThread.start();
                  sleepCyclesBetweenStreamerCreation = 0;
               }

               if (httpPolling != null && !httpPollingIsRunning ) {
            	   System.out.println("Starting http polling mechanism ...");
                  httpPolling.execute(queue, newsQueue, networkConfiguration);
                  httpPollingIsRunning = true;
               }

            }
            else {
               if (httpPolling != null && httpPollingIsRunning ) {
                  httpPollingIsRunning = false;
                  httpPolling.stop();
               }
            }
         }

         if (streamer != null) {
        	 System.out.println(new Timestamp(System.currentTimeMillis()) + " shuting down streamer in Network Streamer");

            streamer.setDoRun(false);
            try {
                streamerThread.interrupt();
            }
            catch (Exception iex) {}

            streamer.shutDownIO();
            streamerThread = null;

         }

         if (httpPolling != null )
            httpPolling.stop();
      }
      catch (Exception e) {}
   }

   private IStreamer retrieveStreamer() {

      HttpConfiguration httpconfig = new HttpConfiguration();
 //use for java 1.2
      //     HttpRequestExecutor12 reqExecutor = new HttpRequestExecutor12();
//use for java 1.5 
      HttpRequestExecutor15 reqExecutor = new HttpRequestExecutor15();
      reqExecutor.setConnectTimeout(8000);
      reqExecutor.setReadTimeout(8000);
 //-->     
      httpconfig.setURL(url);
      httpconfig.addQueryKeyValue("ACTION", "CHECK");

	  String authValueBeforeEncode = userName +":" +password;
	  String authValue = "Basic "+new sun.misc.BASE64Encoder().encode(authValueBeforeEncode.getBytes());
      httpconfig.addRequestProperty("Authorization",authValue);
      httpconfig.setEndQueryString("&SUBEND=true");
      IStreamer streamer = null;
      Hashtable table = null;
      try
      {
           reqExecutor.execute(httpconfig);
           table = parseKeyValues(reqExecutor.getResults());

      } catch (Exception e)
      {
           e.printStackTrace();
      }

      String streamerClass = null;
      if (table.containsKey(QTMessageKeys.STREAMER_CLASS)){
         streamerClass = (String)table.get(QTMessageKeys.STREAMER_CLASS);
      }
      try{
         if ((streamerClass != null) && (!streamerClass.equals("")))
         {
           streamer = (IStreamer)Class.forName(streamerClass).newInstance();
         }
      }
      catch (Exception e) {
         e.printStackTrace();
//         System.out.println("QuoddStreamerInitializationController.getStreamer - Unable to instantiate IStreamer["+streamerClass+"]");
      }
      if (streamer != null){
		  if(table.containsKey(QTMessageKeys.UDP_BIND_ADDRESS)){
			StringTokenizer ast = new StringTokenizer((String)table.get(QTMessageKeys.UDP_BIND_ADDRESS),",");
			if (ast.hasMoreTokens())
              streamer.setUdpBindAddress(ast.nextToken());
	      }
		  if(table.containsKey(QTMessageKeys.UDP_PORT)){
			int port = 0;
			try {
			   StringTokenizer pst = new StringTokenizer((String)table.get(QTMessageKeys.UDP_PORT),",");
			   if (pst.hasMoreTokens())
		         port = Integer.valueOf(pst.nextToken()).intValue();

		    }catch(NumberFormatException nfe) {

		    }
            streamer.setUdpPort(port);
	      }
      }
      if (table.containsKey(QTMessageKeys.STREAMER_ID)){
         streamerId = (String)table.get(QTMessageKeys.STREAMER_ID);
      }
      System.out.println("networkStreamer: streamer class =" + streamerClass);
      System.out.println("networkStreamer: streamer id =" + streamerId);
      streamer.setUserId(userId);
      return streamer;
   }

   private Hashtable parseKeyValues(byte[] bytes) {
      Hashtable map = new Hashtable();
//System.out.println("networkStreamer: response string =" + new String(bytes));

      StringTokenizer st = new StringTokenizer(new String(bytes),QTMessageKeys.TUPLE_SEP);
      while (st.hasMoreTokens()) {
         String tuple = st.nextToken();
//System.out.println("networkStreamer: tuple =" + tuple);

         int index = tuple.indexOf(QTMessageKeys.FIELD_SEP);
//System.out.println("networkStreamer: key =" + tuple.substring(0,index) + " value: " + tuple.substring(index+1));
         map.put(tuple.substring(0,index),tuple.substring(index+1));
      }
      return map;
   }

}
