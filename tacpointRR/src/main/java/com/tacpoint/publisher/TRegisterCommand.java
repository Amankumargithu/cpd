/**
  * TRegisterCommand.java
  *
  * @author Copyright (c) 2003 by Tacpoint Technologie, Inc.
  *    All rights reserved.
  * @version 1.0
  * Created on Sep 18, 2003
  */
package com.tacpoint.publisher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;
import java.beans.XMLEncoder;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public final class TRegisterCommand implements Runnable
{
   private static Log log = LogFactory.getLog(TRegisterCommand.class);


   private TPublisher publisher;
   private SelectionKey selectionKey;
   private SocketChannel registerChannel;
   private TPublisherConfigBean configBean;
   private static final int DEFAULT_BUFFER_SIZE = 1024;
   private static final String TOPIC_END = "&SUBEND=true";
   private static final String CONTENT_LENGTH_ZERO = "Content-Length: 0";
   private static final String BLANK = "";
   private static final char[] HTTP_LINE_FEEDS = {13,10,13,10};
   private static final String HTTP_LINE_FEEDS_STRING = new String(HTTP_LINE_FEEDS);

   private static final String USERID = "USERID";

   private static final String TOPICS = "TOPICS";
   private static final String OLD_TOPICS = "OLD_TOPICS";
   private static final String NEW_TOPICS = "NEW_TOPICS";

   private static final String ACTION = "ACTION";
   private static final String ADD = "ADD";
   private static final String REMOVE = "REMOVE";
   private static final String CHECK = "CHECK";
   private static final String MODIFY = "MODIFY";

   private String heartbeatTopic;

   //private static final String HTTP_RESP_HEADER="HTTP/1.1 200 OK\r\nContent-Type: text/html;charset=ISO-8859-1\r\nTransfer-Encoding: chunked\r\n\r\n";

   private static final String HTTP_RESP_HEADER="HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nConnection: Keep-Alive\r\n\r\n";

   //private static final String HTTP_RESP_HEADER="HTTP/1.0 200 OK\r\nContent-Type: application/octet-stream\r\nConnection: Close\r\n\r\n";

   //private static final String HTTP_RESP_HEADER="HTTP/1.0 200 OK\r\nContent-Type: text/html;charset=ISO-8859-1\r\nConnection: Close\r\n\r\n";

   private static final String FAILED_AUTH_HTTP_RESP_HEADER="HTTP/1.1 401 \r\nWWW-Authenticate: Basic realm=\"StreamerRealm\"\r\nContent-Type: text/html;charset=ISO-8859-1\r\nConnection: Close\r\n\r\n";

   private static final String AUTHORIZATION = "authorization: basic ";
   private static final String COLON = ":";

   protected TRegisterCommand()
   {
   }

   public TRegisterCommand(TPublisher pub, SelectionKey sk, SocketChannel sc)
   {
      publisher = pub;
      selectionKey = sk;
      registerChannel = sc;
      configBean = publisher.getConfiguration();
      heartbeatTopic = configBean.getHeartbeatTopic();
   }

   public void run()
   {
      try
      {
    	 
         Socket socket = registerChannel.socket();

         socket.setSoTimeout(2000);
         //socket.setSendBufferSize(2*2048);

         String ipAddress = socket.getInetAddress().getHostAddress();

         //socket.setSendBufferSize(2*2048);

         InputStream is = socket.getInputStream();
         //BufferedInputStream is = new BufferedInputStream(socket.getInputStream(),2048);

         byte[] response = new byte[2048];

         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         //log.debug("About to parse input stream ... ");

         int bytesRead = 0;
         int attempts = 0;

         boolean doProcess = false;

         while ( (bytesRead = is.read(response)) > 0) {
         //while ( is.available() > 0) {

            //if (attempts++ > 3) break;

        	//bytesRead = is.read(response);
        	
            baos.write(response,0,bytesRead);

            String partial = new String(baos.toByteArray());

            //log.debug("Contents of request - "+partial);

            // search for the end of query delim ...
            if (partial.indexOf(CONTENT_LENGTH_ZERO) > 0)
               break;


            // search for the end of query delim ...
            if (partial.indexOf(TOPIC_END) > 0) {
               doProcess = true;
               break;
            }

            // this line is needed for people holding onto old b4utrade.jar files
            // so as not to hang this method!
            if (partial.indexOf("||") > 0) {
               doProcess = true;
               break;
            }

         }
         
         //log.debug("Complete socket contents : "+baos.toString());
         

         //log.debug("Should process ? "+doProcess);

         if (doProcess == false) {
            cleanup(registerChannel);
            return;
         }

         String httpRequest = new String(baos.toByteArray());

         if (publisher.requiresAuthentication()) {
            if (!authenticate(httpRequest)) {
               log.debug("Failed authorization.");
               processAuthError(registerChannel);
               return;
            }
            log.debug("Successfully authenticated.");
         }

         HashMap queryMap = parseQuery(httpRequest);

         String action = (String)queryMap.get(ACTION);

         String userid = null;


         userid = (String)queryMap.get(USERID);
         if (userid == null) {
            userid = String.valueOf(publisher.getSocketKey());
         }

         if (action == null) action = BLANK;

         if (action.equals(MODIFY)) {

            // new topic additions ...
            String newTopicQuery = (String)queryMap.get(NEW_TOPICS);
            String newTopicString = java.net.URLDecoder.decode(newTopicQuery,"UTF-8");
            Vector newInterests = parseTopics(newTopicString);
            if (newInterests.size() > 0) {
               handleUpdate(ipAddress, userid, registerChannel, newInterests,
                            false, /* don't write response */ false /* don't close channel yet */);
            }

            // old topic removals ...
            String oldTopicQuery = (String)queryMap.get(OLD_TOPICS);
            String oldTopicString = java.net.URLDecoder.decode(oldTopicQuery,"UTF-8");
            Vector oldInterests = parseTopics(oldTopicString);
            if (oldInterests.size() > 0) {
               handleDelete(ipAddress, userid, registerChannel, oldInterests,
                            false, /* write response */ false /* don't close channel yet */);
            }

            try {
               registerChannel.write(ByteBuffer.wrap(new String(HTTP_RESP_HEADER+"OK").getBytes()));
            }
            catch (Exception rex) {}

            cleanup(registerChannel);

         }
         else if (action.equals(CHECK)) {
            handleCheckMethod();
         }
         else {

            String query = (String)queryMap.get(TOPICS);
            String topicString = java.net.URLDecoder.decode(query,"UTF-8");
            Vector interests = parseTopics(topicString);

            if (action.equals(ADD)) {
               handleUpdate(ipAddress, userid, registerChannel, interests, true, /* write response */ true /* close channel */);
            }
            else if (action.equals(REMOVE)) {
               handleDelete(ipAddress, userid, registerChannel, interests, true, /* write response */ true /* close channel */);
            }
            else
               handleInit(ipAddress, userid, interests);
         }
      }
      catch (java.net.SocketTimeoutException ste ) {
         log.error("Socket timeout occurred during read method ... shutting down socket!");
         cleanup(registerChannel);
      }
      catch (IOException ie)
      {
    	  log.error("Exception encountered while processing HTTP request : "+ie.getMessage());
      }
      catch(Exception e)
      {
         log.error("Exception encountered while processing HTTP request : "+e.getMessage(),e);
         cleanup(registerChannel);
      }
   }

   private void handleInit(String ipAddress, String userid, Vector interests) throws Exception {

      log.debug("handle init being called for user ID "+userid+" IP address "+ipAddress);

      SocketChannel existingChannel = publisher.channelLookup(userid);

      /*
      if (existingChannel != null) {
         log.error("Performing cleanup on existing channel for user ID - "+userid);
         cleanup(existingChannel);
      }
      */

      interests.add(heartbeatTopic);
      registerChannel.configureBlocking(false);
      registerChannel.write(ByteBuffer.wrap(HTTP_RESP_HEADER.getBytes()));
      publisher.registerChannel(userid, registerChannel);
      publisher.subscribe(interests, registerChannel, userid);

   }

   private void handleUpdate(String ipAddress, String userid, SocketChannel socketChannel, Vector interests,
                             boolean writeResponse, boolean doCleanup) throws Exception {

      log.debug("handle update being called for user ID "+userid+" IP address "+ipAddress);

      // first find the existing socket channel!
      SocketChannel existingChannel = publisher.channelLookup(userid);

      if (existingChannel != null) {
         publisher.subscribe(interests, existingChannel, userid);

         if (writeResponse)
            registerChannel.write(ByteBuffer.wrap(new String(HTTP_RESP_HEADER+"OK").getBytes()));

         if (doCleanup)
            cleanup(registerChannel);
      }
   }

   private void handleDelete(String ipAddress, String userid, SocketChannel socketChannel, Vector interests,
                             boolean writeResponse, boolean doCleanup) throws Exception {

      log.debug("handle delete being called for user ID "+userid+" IP address "+ipAddress);

      // first find the existing socket channel!
      SocketChannel existingChannel = publisher.channelLookup(userid);
      if (existingChannel != null) {
         publisher.unsubscribe(interests, existingChannel, userid);
      }

      if (writeResponse)
         registerChannel.write(ByteBuffer.wrap(new String(HTTP_RESP_HEADER+"OK").getBytes()));
      if (doCleanup)
         cleanup(registerChannel);

   }

   private void handleCheckMethod() throws Exception {
      log.debug("handle check subscribe method  ");
      String streamerString = "com.b4utrade.network.OptimizedEquityStreamer";
      //add logic to determind the right streamer to use


      // kluge until we can figure out *where* the request is coming from!

      String port = configBean.getUdpPort();

      if (configBean.getUdpPort() != null && configBean.getUdpPort().indexOf(",") >= 0)
         port = configBean.getUdpPort().substring(0,configBean.getUdpPort().indexOf(","));

      log.debug("Streamer class = "+configBean.getStreamerClassName());
      log.debug("Bind Address   = "+configBean.getUdpBindAddress());
      log.debug("port number    = "+port);

      StringBuffer sb = new StringBuffer();
      sb.append("SC:");
      sb.append(configBean.getStreamerClassName());

      if((configBean.getUdpBindAddress() != null)&&!(configBean.getUdpBindAddress().trim().equals("")))
      {
		sb.append("|");
		sb.append("UBA:");
        sb.append(configBean.getUdpBindAddress());

      }
      if((port != null)&&!(port.trim().equals("")))
      {
		sb.append("|");
		sb.append("UP:");
        sb.append(port);

      }
      if((configBean.getStreamerId() != null)&&!(configBean.getStreamerId().trim().equals("")))
      {
		sb.append("|");
		sb.append("SI:");
        sb.append(configBean.getStreamerId());

      }


//      HashMap initMap = new HashMap();
//      initMap.put("STREAMER_CLASS", configBean.getStreamerClassName());
//      initMap.put("UDP_BIND_ADDRESS", configBean.getUdpBindAddress());
//      initMap.put("STREMER_ID", configBean.getStreamerId();

//      if (port != null)
//         initMap.put("UDP_PORT", new Integer(port));

//      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//      XMLEncoder encoder = new XMLEncoder(byteArray);
//      encoder.writeObject(initMap);
//      encoder.close();


      registerChannel.write(ByteBuffer.wrap(new String(HTTP_RESP_HEADER).getBytes()));
      registerChannel.write(ByteBuffer.wrap(sb.toString().getBytes()));

      cleanup(registerChannel);

   }

   private boolean authenticate(String httpRequest) {

      // find Authorization line if present ...
      log.debug("Complete authorization string : "+httpRequest);

      int beginIndex = httpRequest.toLowerCase().indexOf(AUTHORIZATION);

      if (beginIndex < 0) return false;

      String authorization = httpRequest.substring(beginIndex+AUTHORIZATION.length());

      log.debug("Authorization string : "+authorization);

      // strip carriage return and newline (if any)

      int crIndex = authorization.indexOf('\r');
      if (crIndex < 0) crIndex = 0;

      int nlIndex = authorization.indexOf('\n');
      if (nlIndex < 0) nlIndex = 0;

      // use smallest index!
      if (crIndex < nlIndex)
         authorization = authorization.substring(0,crIndex);
      else if (crIndex > nlIndex)
         authorization = authorization.substring(0,nlIndex);
      else if (crIndex == nlIndex)
         authorization = authorization.substring(0,crIndex);

      log.debug("New authorization string : "+authorization);

      String unencoded = new String(Base64.decode(authorization.getBytes()));

      log.debug("unencoded authorization string : "+unencoded);

      int colonIndex = unencoded.indexOf(COLON);
      if (colonIndex < 0)
         return false;

      String username = unencoded.substring(0, colonIndex).trim();
      String password = unencoded.substring(colonIndex + 1).trim();

      log.debug("Auth user : "+username);
      log.debug("Auth pass : "+password);

      if (publisher.getAuthUserName().equals(username) && publisher.getAuthPassword().equals(password))
         return true;

      return false;

   }

   private HashMap parseQuery(String queryString) {

      HashMap results = new HashMap();

      if (queryString == null) return results;

      String keyValues = BLANK;

      int begIndex = queryString.indexOf("?");

      if (begIndex > 0)
         keyValues = queryString.substring(begIndex+1);
      else {
         // assume an HTTP post ...
         begIndex = queryString.indexOf(HTTP_LINE_FEEDS_STRING);
         keyValues = queryString.substring(begIndex+HTTP_LINE_FEEDS_STRING.length());
      }

      //log.debug("Query String : "+queryString);

      StringTokenizer st = new StringTokenizer(keyValues,"&");

      boolean doAppend = false;

      String token = null;

      while (st.hasMoreElements()) {

		 token = st.nextToken();

         //log.debug("Current token : "+token);

         int index = token.indexOf("=");

         if (index < 0) {
			 log.error("Unable to parse out equals sign from token.");
			 continue;
		 }

         String key = token.substring(0,index);
         String value = token.substring(index+1);
         //log.debug("Key = "+key+" Value = "+value);
         results.put(key,value);
      }

      return results;
   }
   private Vector parseTopics(String t)
   {
      try
      {
         StringTokenizer st = new StringTokenizer(t, ",");

         HashMap map = new HashMap();

         // parse for topics and remove dups!
         while (st.hasMoreElements())
         {
            map.put(st.nextToken(),"");
         }

         Vector topics = new Vector(map.keySet());

         return topics;
      }
      catch(Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   private void processAuthError(SocketChannel sc) throws Exception {
      sc.write(ByteBuffer.wrap(FAILED_AUTH_HTTP_RESP_HEADER.getBytes()));
      cleanup(sc);
   }

   private void cleanup(SocketChannel target) {

	   
	  try {
		  target.close();
	  }
	  catch (Exception e) {}
	  
	  /*
      try {
         target.socket().shutdownInput();
      }
      catch (Exception e) {}
      try {
         target.socket().shutdownOutput();
      }
      catch (Exception e) {}
      try {
         target.close();
      }
      catch (Exception e) {}
      */
   }

}
