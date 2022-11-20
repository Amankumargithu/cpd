package com.b4utrade.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.paxcel.utils.compression.StreamProvider;

import com.b4utrade.bean.QTMessageBean;
import com.b4utrade.helper.StockActivityBeanConverter;
import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;
import com.tacpoint.util.SystemOutLogger;
public class CompressedLZWEquityStreamer implements IStreamer, Runnable {

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

   public CompressedLZWEquityStreamer() {}

   public void init(MessageQueue quoteQueue, MessageQueue newsQueue, InputStream inputStream,
              NetworkConfiguration networkConfiguration, NetworkStreamer networkStreamer) {
      this.quoteQueue = quoteQueue;
      this.newsQueue = newsQueue;
      this.inputStream = inputStream;
      this.networkConfiguration = networkConfiguration;
      this.networkStreamer = networkStreamer;
      SystemOutLogger.debug("Opt Equity Streamer - init()");

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
		   System.out.println("CompressedLZWEquityStreamer.run() initial call: streamer id =" + streamerId);

           String cookieValue = QTMessageKeys.STREAMER_COOKIE_KEY + "=" +streamerId +";";
           conn.setRequestProperty("Cookie", cookieValue);
	     }
	     else{
		   System.out.println("CompressedLZWEquityStreamer.run() initial call: streamer id is null or empty");

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
         SystemOutLogger.debug("streaming URL "+networkConfiguration.getStreamingURL() + query.toString());

         os = conn.getOutputStream();
         os.write(query.toString().getBytes());

         inputStream = conn.getInputStream();

         bis = StreamProvider.getInputStream(inputStream);

      }
      catch (Exception e) {
         SystemOutLogger.error("Opt Equity Streamer - Unable to connect to URL: "+networkConfiguration.getStreamingURL());
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
      String ticker = "";
      while ( doRun ) {

         try {

            int numBytes = bis.read(response);

            //SystemOutLogger.debug("message-before separating: " + new String(response));

            if (numBytes < 0){
                SystemOutLogger.debug("input stream bytes read " + numBytes);
				return;
		    }

            curTime = System.currentTimeMillis();

            networkStreamer.setLastProcessTime(curTime);

            if ( (curTime - beginTime) > 1000) {
               beginTime = curTime;
               SystemOutLogger.debug("Comp Opt Equity Streamer - Number of messages processed: "+msgCount);
               SystemOutLogger.debug("Comp Opt Equity Streamer - Queue size: "+quoteQueue.getSize());
               msgCount = 0l;
            }
//SystemOutLogger.debug("responce: " + new String(response));
            for (int i=0; i<numBytes; i++) {

               if (response[i] == terminatorByte) {

                  //SystemOutLogger.debug("message-before pass in: " + new String(baos.toByteArray()));
                  Hashtable map = messageInflator.parseKeyValues(baos);

                  String topic = (String)map.get(QTMessageKeys.TICKER);

                  if (topic != null && ( topic.equals(com.b4utrade.util.B4UConstants.DJ_NEWS_TOPIC)
                		  || topic.equals(com.b4utrade.util.B4UConstants.EDGE_NEWS_TOPIC)
                		  || topic.equals(com.b4utrade.util.B4UConstants.BENZINGA_NEWS_TOPIC)
                		  || topic.equals(com.b4utrade.util.B4UConstants.STREET_INSIDER_NEWS_TOPIC)
                		  || topic.equals(com.b4utrade.util.B4UConstants.THE_FLY_ON_THE_WALL_NEWS_TOPIC)
                		  || topic.equals(com.b4utrade.util.B4UConstants.MIDNIGHT_TRADER_NEWS_TOPIC) ))
                		  
                  {
					 //SystemOutLogger.debug("before create news");
					 Vector v = sh.createNewsMsg(map);
					 Enumeration en = v.elements();
					 while(en.hasMoreElements()){
						StockNewsUpdateHelper newsItem = (StockNewsUpdateHelper)en.nextElement();
                        newsQueue.add(newsItem);
                        //SystemOutLogger.debug("news ticker:" + newsItem.getTicker() +" news id: " + newsItem.getLastNewsID());

				     }
             		 //SystemOutLogger.debug("after create news");

                  }
                  else {

                     resultsCache = networkConfiguration.getResultsHash();

                     if (resultsCache == null) {
                        SystemOutLogger.debug("Opt Equity Streamer - unable to obtain network configuration results hash!");
                        baos.reset();
                        continue;
                     }
                     ticker = (String)map.get(QTMessageKeys.TICKER);
                     QTMessageBean qtBean = null;
                     try {

                    	 qtBean = (QTMessageBean)resultsCache.get(map.get(QTMessageKeys.TICKER));
				     }catch(ClassCastException cce){
				       StockActivityHelper sah = (StockActivityHelper)resultsCache.get(map.get(QTMessageKeys.TICKER));
				       sah.init();
				       //qtBean = QTMessageBean.convertSAHToQTM(sah);
				       qtBean = StockActivityBeanConverter.convertSAHToQTM(sah);
				     }
				     catch (NullPointerException ne)
				     {
				    	 if (baos != null && baos.toByteArray() != null)
				    	 {
				    		 System.out.println(new Timestamp(System.currentTimeMillis()) + " BAOS: " + new String(baos.toByteArray()));
				    		 baos.reset();
				    	 }
				    	 else
				    	 {
				    		 System.out.println(new Timestamp(System.currentTimeMillis()) + " BAOS is empty ");
				    	 }
				    	 continue;
				    	 
				     }
				     catch (Exception e)
				     {
				    	 e.printStackTrace();
				    	 
				    	 if (baos != null && baos.toByteArray() != null)
				    	 {
				    		 System.out.println(new Timestamp(System.currentTimeMillis()) + " BAOS: " + new String(baos.toByteArray()));
				    		 baos.reset();
				    	 }
				    	 else
				    	 {
				    		 System.out.println(new Timestamp(System.currentTimeMillis()) + " BAOS is empty ");
				    	 }
				    	 continue;
				    	 
				     }
				     
				     String symbol = (String)map.get(QTMessageKeys.TICKER);
				     if ((qtBean == null) && (symbol.indexOf("TACPOINT") < 0))
                       SystemOutLogger.debug("Un able to find QT Bean for: "  + symbol);

                     qtBean = messageInflator.merge(map, qtBean);
                     if((qtBean.getLastPrice() <0.00001d) && (symbol.indexOf("TACPOINT") < 0))
                       SystemOutLogger.debug("the last price is 0.0, the string from streamer: " + new String(baos.toByteArray()));

                     if (qtBean != null) {
                      resultsCache.put(qtBean.getSystemTicker(),qtBean);
                      
                      //Added so that .TIME will get entitled on UI
                      if(symbol != null && symbol.equals(".TIME"))
                    	  qtBean.setExchangeId("null");
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
            SystemOutLogger.debug("comp Opt Equity Streamer - Exception caught during input stream read." + ticker);
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




