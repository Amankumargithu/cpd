package com.b4utrade.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;
import com.tacpoint.util.SystemOutLogger;

public class EquityStreamer implements IStreamer, Runnable {

   private MessageQueue queue;
   private MessageQueue newsQueue;
   private NetworkStreamer networkStreamer;
   private NetworkConfiguration networkConfiguration;
   private InputStream inputStream;
   private OutputStream os = null;
   boolean doRun = true;
   boolean isBlocking = true;
   int     readAttemptsBeforeTerminating = 5;
   String  userId = null;
   private final String prefix = "TOPICS";
   private final String paramSeperator = "?";
   private String streamerId = "";

   public EquityStreamer() {}

   public void init(MessageQueue queue, MessageQueue newsQueue, InputStream inputStream,
              NetworkConfiguration networkConfiguration, NetworkStreamer networkStreamer) {
      this.queue = queue;
      this.newsQueue = newsQueue;
      this.inputStream = inputStream;
      this.networkConfiguration = networkConfiguration;
      this.networkStreamer = networkStreamer;
      SystemOutLogger.debug("Equity Streamer - init()");

   }

   public boolean getIsBlocking() {
	  return isBlocking;
   }

   public void setIsBlocking(boolean isBlocking) {
	  this.isBlocking = isBlocking;
   }

   public void setDoRun(boolean doRun) {
      this.doRun = doRun;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }
   public void setStreamerId(String streamerId) {
      this.streamerId = streamerId;
   }
   public void setUdpBindAddress(String bindingAddress){
   }
   public void setUdpPort(int port){}

   public void setReadAttemptsBeforeTerminating(int readAttemptsBeforeTerminating) {
      if (readAttemptsBeforeTerminating > 0)
         this.readAttemptsBeforeTerminating = readAttemptsBeforeTerminating;
   }
   public void shutDownIO() {
         try {
	       if (inputStream != null)
	         inputStream.close();
	       if (os != null)
	         os.close();
         }
         catch (Exception ioCloseException) {}

   }

   public void run() {

      networkStreamer.setIsStreamerConsuming(false);

      try {

         URL url = new URL(networkConfiguration.getStreamingURL());

         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setAllowUserInteraction(false);
         conn.setUseCaches(false);
         Hashtable queryHash = networkConfiguration.getQueryHash();
         Enumeration eKeys = queryHash.keys();
         StringBuffer query = new StringBuffer();
//	     query.append(paramSeperator);
         if (userId != null) {
	        query.append("USERID=");
	        query.append(userId);
	        query.append("&");

	     }
         query.append(prefix);
         query.append("=");
         while (eKeys.hasMoreElements()) {
            String key = (String)eKeys.nextElement();
            Vector v = (Vector)queryHash.get(key);
            if (v == null) continue;
            for (int i = 0; i < v.size(); i++) {
               query.append(URLEncoder.encode((String)v.elementAt(i)));
               if (i+1<v.size())
                  query.append(URLEncoder.encode(","));
            }
         }
         query.append("&SUBEND=true");
         SystemOutLogger.debug("streaming URL "+networkConfiguration.getStreamingURL() + query.toString());

         os = conn.getOutputStream();
         os.write(query.toString().getBytes());

         inputStream = conn.getInputStream();

      }
      catch (Exception e) {
         SystemOutLogger.error("Equity Streamer - Unable to connect to URL: "+networkConfiguration.getStreamingURL());
         e.printStackTrace();
         return;
      }
      finally {
		 isBlocking = false;
         try {
            if (os != null)
               os.flush();
         }
         catch (Exception osFlushException) {}
         try {
            if (os != null)
               os.close();
         }
         catch (Exception osCloseException) {}
      }

      networkStreamer.setIsStreamerConsuming(true);

      byte[] response = new byte[1024];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      long beginTime = System.currentTimeMillis();
      long curTime = beginTime;

      long msgCount = 0;

      byte terminatorByte = networkConfiguration.getTerminator();

      int bytesRead = 0;
      int failedReadAttempts = 0;

      while ( doRun ) {

         try {

            bytesRead = inputStream.available();

            if (bytesRead > 1024) bytesRead = 1024;

            if (bytesRead < 1) {
               try {
                  Thread.sleep(50);
               }
               catch (Exception ex) {}
               continue;
            }

            int numBytes = inputStream.read(response,0,bytesRead);

            if (numBytes < 0) return;

            curTime = System.currentTimeMillis();

            networkStreamer.setLastProcessTime(curTime);

            if ( (curTime - beginTime) > 1000) {
               beginTime = curTime;
               SystemOutLogger.debug("Equity Streamer - Number of messages processed: "+msgCount);
               SystemOutLogger.debug("Equity Streamer - Queue size: "+queue.getSize());
               msgCount = 0l;
            }

            for (int i=0; i<bytesRead; i++) {
               if (response[i] == terminatorByte) {
                  StockActivityHelper sah = new StockActivityHelper();
                  sah.setStockStream(baos.toByteArray());
                  sah.init();
                  if (!sah.isParseOk()) {
                     sah.setIsOption(true);
                     sah.init();
                  }
                  queue.add(sah);
                  msgCount++;
                  baos.reset();
               }
               else {
                  baos.write(response,i,1);
               }
            }
         }
         catch (Exception ioe) {
            SystemOutLogger.error("Equity Streamer - Exception caught during input stream read.");
            //ioe.printStackTrace();
            return;
         }
      }

      networkStreamer.setIsStreamerConsuming(false);

      try {
         if (inputStream != null)
            inputStream.close();
      }
      catch(IOException ioe) {}

   }
}




