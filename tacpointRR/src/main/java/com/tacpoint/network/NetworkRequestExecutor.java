/** 
 * NetworkRequestExecutor -  Administers the client's http request. 
 *
 * Copyright: Tacpoint Technologies, Inc. (c), 2000.  All rights reserved.
 * @author Andy Katz
 * @author akatz@tacpoint.com
 * @version 1.0
 * Date created: 02/22/2000
 * Date modified:
 * - 02/22/2000 Initial version
 */
package com.tacpoint.network;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.quodd.equityplus.logging.ConsoleLogger;

/**
 * This class provides network access to a specified url.
 * It reads information from a com.tacpoint.network.NetworkConfiguration object
 * and proceeds to initiate the network request.  Results are stored in
 * the NetworkConfiguration's resultsHash.
 */
public class NetworkRequestExecutor {
	
	private String GZIP = "gzip";
	private int timeout = 0;
	
	public void setTimeOut(int milliseconds)
	{
		timeout = milliseconds;
	}
	
    /**
     * Default constructor.
     */
     public NetworkRequestExecutor() {}
 
    /**
     * Executes the network request on behalf of the client.
     * Results are stored in the resultsHash of the NetworkConfiguration
     * object.
     */
     public void execute(NetworkConfiguration c) throws Exception { 
       try {
          String urlString = c.getURL() + "/" + c.getServlet();
          URL url = new URL(urlString);

          ConsoleLogger.debug(c.getClient()+": Attempting to connect to: "+urlString, ConsoleLogger.CATEGORY_GENERAL);
          URLConnection conn = url.openConnection();
          conn.setDoOutput(true);
          conn.setDoInput(true);
          conn.setAllowUserInteraction(false);
          conn.setUseCaches(false);
          conn.setRequestProperty("Accept-Encoding", GZIP);
          if(timeout > 0)
          {
        	  conn.setConnectTimeout(timeout);
        	  conn.setReadTimeout(timeout);
          }
          // process all keys in the query hash...
          Hashtable queryHash = c.getQueryHash();
          Enumeration eKeys = queryHash.keys();
          StringBuffer query = new StringBuffer();

          while (eKeys.hasMoreElements()) {
             String key = (String)eKeys.nextElement();
             Vector v = (Vector)queryHash.get(key);
             if (v == null) continue;
             query.append(key); 
             query.append("=");
             for (int i = 0; i < v.size(); i++) {
                query.append(URLEncoder.encode((String)v.elementAt(i)));
                if (i+1<v.size())
                   query.append(URLEncoder.encode(","));
             }
             if (eKeys.hasMoreElements())
                query.append("&");
          }
          query.append("&TIMESTAMP="+System.currentTimeMillis());
          ConsoleLogger.debug(c.getClient()+": Query String: "+query.toString(), ConsoleLogger.CATEGORY_GENERAL);
          OutputStream os = conn.getOutputStream();
          os.write(query.toString().getBytes());
          os.close();

          InputStream is = null;
          if ((GZIP).equalsIgnoreCase(conn.getContentEncoding())) {
        	  is = new GZIPInputStream(conn.getInputStream());
          }
          else {
        	  is = conn.getInputStream(); 
          }
          int contentLength = conn.getContentLength();
          ConsoleLogger.debug(c.getClient()+": ContentLength = " + contentLength, ConsoleLogger.CATEGORY_GENERAL);
          byte[] response = new byte[1024];
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          int bytesRead = 0;
          while ( (bytesRead = is.read(response)) != -1) {
             baos.write(response,0,bytesRead);
          }
          is.close();
          ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
          ObjectInputStream ois = new ObjectInputStream(bais);
          Hashtable tempHash = (Hashtable)ois.readObject();

          ois.close();
          if (tempHash!=null) {
             ConsoleLogger.debug(c.getClient()+": Successfully retrieved data.", ConsoleLogger.CATEGORY_GENERAL);
             // swap out the NetworkConfiguration's hash attribute with 
             // newly inflated hash.
             synchronized (c.getResultsHash()) {
                if (c.getPostNetworkRequestHandler() != null) {
                   c.getPostNetworkRequestHandler().execute(tempHash);
                }
                c.setResultsHash(tempHash);
             }
          }
          else {
             ConsoleLogger.debug(c.getClient()+": Unable to retrieve data.", ConsoleLogger.CATEGORY_GENERAL);
          }
       }
       catch (Exception mainEx) { 
          ConsoleLogger.error(c.getClient()+": Exception. " + mainEx.getMessage(), ConsoleLogger.CATEGORY_GENERAL);
          System.out.print(new Timestamp(System.currentTimeMillis()) + "  ");
          mainEx.printStackTrace(); 
       }
    }
}