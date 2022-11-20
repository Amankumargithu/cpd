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

import com.b4utrade.bean.MM2MessageBean;
import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;
import net.paxcel.utils.compression.*;


public class CompressedLZWMarketMakerStreamer implements IStreamer, Runnable {

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
   private final String MM2Appender = ".MM2";
   private final String paramSeperator = "?";
   private String streamerId = "";

   public CompressedLZWMarketMakerStreamer() {}

   public void init(MessageQueue queue, MessageQueue newsQueue, InputStream inputStream,
                    NetworkConfiguration networkConfiguration, NetworkStreamer networkStreamer) {
      this.queue = queue;
      this.newsQueue = newsQueue;
      this.inputStream = inputStream;
      this.networkConfiguration = networkConfiguration;
      this.networkStreamer = networkStreamer;
   }

   public void setDoRun(boolean doRun) {
      this.doRun = doRun;
   }

   public boolean getIsBlocking() {
      return isBlocking;
   }
   public void setUdpBindAddress(String bindingAddress){
   }
   public void setUdpPort(int port){}

   public void setIsBlocking(boolean isBlocking) {
      this.isBlocking = isBlocking;
   }
   public void setUserId(String userId) {
      this.userId = userId;
   }
   public void setStreamerId(String streamerId) {
      this.streamerId = streamerId;
   }
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

      InputStream is = null;

      networkStreamer.setIsStreamerConsuming(false);

      try {

         URL url = new URL(networkConfiguration.getStreamingURL());

         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setAllowUserInteraction(false);
         conn.setUseCaches(false);

         String authString = "Authorization";
	     String authValue = "Basic "+com.tacpoint.publisher.Base64.encode("streamer:str3amer!".getBytes());
         //conn.addRequestProperty(authString,authValue);
         conn.setDefaultRequestProperty(authString,authValue);
         if ((streamerId != null) && !(streamerId.trim().equals("")) ){
           String cookieValue = QTMessageKeys.STREAMER_COOKIE_KEY + "=" +streamerId +";";
           conn.setRequestProperty("Cookie", cookieValue);
	     }

         Hashtable queryHash = networkConfiguration.getQueryHash();
         Enumeration eKeys = queryHash.keys();
         StringBuffer query = new StringBuffer();
	     query.append(paramSeperator);
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
               String ticker = (String)v.elementAt(i) + MM2Appender;
               query.append(URLEncoder.encode(ticker));
               if (i+1<v.size())
                  query.append(URLEncoder.encode(","));
            }
         }
         query.append("&SUBEND=true");

         os = conn.getOutputStream();
         os.write(query.toString().getBytes());

         inputStream = conn.getInputStream();
         is = (InputStream)new LZWInputStream(inputStream, false);

      }
      catch (Exception e) {
         System.out.println("Compressed Market Maker Streamer - Unable to connect to URL: "+networkConfiguration.getStreamingURL());
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

            bytesRead = is.read(response);

            if (bytesRead > 1024) bytesRead = 1024;

            if (bytesRead < 1) {
               try {
                  Thread.sleep(50);
               }
               catch (Exception ex) {}
               continue;
            }

            curTime = System.currentTimeMillis();

            networkStreamer.setLastProcessTime(curTime);

            if ( (curTime - beginTime) > 1000) {
               beginTime = curTime;
               //System.out.println("Compressed MarketMaker Streamer - Number of messages processed: "+msgCount);
               //System.out.println("Compressed MarketMaker Streamer - Queue size: "+queue.getSize());
               msgCount = 0l;
            }

            for (int i=0; i<bytesRead; i++) {
               if (response[i] == terminatorByte) {

                  MM2MessageBean mm2mb = MM2MessageBean.convertMM2MessageBean(baos.toByteArray());
                  if (mm2mb.isParseOk()) {
                    mm2mb.init();
                    queue.add(mm2mb);
                  }
                  msgCount++;
                  baos.reset();
               }
               else {
                  baos.write(response,i,1);
               }
            }
         }
         catch (Exception ioe) {
            System.out.println("Compressed MarketMaker Streamer - Exception caught during input stream read.");
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




