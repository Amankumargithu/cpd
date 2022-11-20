package com.b4utrade.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import net.paxcel.utils.arith.*;

import java.util.StringTokenizer;

import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.bean.QTMessageBean;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;
import net.paxcel.utils.arith.*;

public class CompressedArithEquityStreamer implements IStreamer, Runnable {

   private MessageQueue quoteQueue;
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
   private Hashtable resultsCache = null;
   private String streamerId = "";

   public CompressedArithEquityStreamer() {}

   public void init(MessageQueue quoteQueue, MessageQueue newsQueue, InputStream inputStream,
              NetworkConfiguration networkConfiguration, NetworkStreamer networkStreamer) {
      this.quoteQueue = quoteQueue;
      this.newsQueue = newsQueue;
      this.inputStream = inputStream;
      this.networkConfiguration = networkConfiguration;
      this.networkStreamer = networkStreamer;
      System.out.println("Opt Equity Streamer - init()");

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
      InputStream bis;
      try {

         URL url = new URL(networkConfiguration.getStreamingURL());

         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setAllowUserInteraction(false);
         conn.setUseCaches(false);

         String authString = "Authorization";
	     String authValue = "Basic "+ com.tacpoint.publisher.Base64.encode("streamer:str3amer!".getBytes());
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
               query.append(URLEncoder.encode((String)v.elementAt(i)));
               if (i+1<v.size())
                  query.append(URLEncoder.encode(","));
            }
         }
         query.append("&SUBEND=true");
         System.out.println("streaming URL "+networkConfiguration.getStreamingURL() + query.toString());

         os = conn.getOutputStream();
         os.write(query.toString().getBytes());

         inputStream = conn.getInputStream();
         bis = new ArithCodeInputStream(inputStream);

      }
      catch (Exception e) {
         System.out.println("Opt Equity Streamer - Unable to connect to URL: "+networkConfiguration.getStreamingURL());
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

      QTReducedMessageInflator messageInflator = new QTReducedMessageInflator();
      StreamerHelper sh = new StreamerHelper();

      while ( doRun ) {

         try {

            int numBytes = bis.read(response);

            System.out.println("message-before separating: " + new String(response));

            if (numBytes < 0){
                System.out.println("input stream bytes read " + numBytes);
				return;
		    }

            curTime = System.currentTimeMillis();

            networkStreamer.setLastProcessTime(curTime);

            if ( (curTime - beginTime) > 1000) {
               beginTime = curTime;
               System.out.println("Comp Opt Equity Streamer - Number of messages processed: "+msgCount);
               System.out.println("Comp Opt Equity Streamer - Queue size: "+quoteQueue.getSize());
               msgCount = 0l;
            }
System.out.println("responce: " + new String(response));
            for (int i=0; i<numBytes; i++) {

               if (response[i] == terminatorByte) {

                  System.out.println("message-before pass in: " + new String(baos.toByteArray()));
                  Hashtable map = messageInflator.parseKeyValues(baos);

                  String topic = (String)map.get(QTMessageKeys.TICKER);

                  if (topic != null && ( topic.equals(com.b4utrade.util.B4UConstants.DJ_NEWS_TOPIC) 
                		  || topic.equals(com.b4utrade.util.B4UConstants.EDGE_NEWS_TOPIC)) )
                		  {
					 //System.out.println("before create news");
					 Vector v = sh.createNewsMsg(map);
					 Enumeration en = v.elements();
					 while(en.hasMoreElements()){
						StockNewsUpdateHelper newsItem = (StockNewsUpdateHelper)en.nextElement();
                        newsQueue.add(newsItem);
                        //System.out.println("news ticker:" + newsItem.getTicker() +" news id: " + newsItem.getLastNewsID());

				     }
             		 //System.out.println("after create news");

                  }
                  else {

                     resultsCache = networkConfiguration.getResultsHash();

                     if (resultsCache == null) {
                        System.out.println("Opt Equity Streamer - unable to obtain network configuration results hash!");
                        baos.reset();
                        continue;
                     }

                     QTMessageBean qtBean = (QTMessageBean)resultsCache.get(map.get(QTMessageKeys.TICKER));

                     qtBean = messageInflator.merge(map, qtBean);
                     //QTMessageBean newBean = new QTMessageBean();

                     if (qtBean != null) {
                        resultsCache.put(qtBean.getSystemTicker(),qtBean);
                        quoteQueue.add(qtBean);
                        msgCount++;
                     }
                  }

                  baos.reset();

               }
               else {
                  baos.write(response,i,1);
               }
            }
         }
         catch (Exception ioe) {
            System.out.println("Opt Equity Streamer - Exception caught during input stream read.");
            ioe.printStackTrace();
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




