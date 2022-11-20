/**
 * HttpRequestExecutor15 -  Administers the client's http/https request.
 *
 * @author  Copyright: Tacpoint Technologies, Inc.(c), 2001.
 *          All rights reserved.
 *
 * @version %I%, %G%
 */

package com.tacpoint.http;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

import com.tacpoint.exception.BusinessException;

/**
 * This class provides network access to a specified url.
 * It reads information from a HttpConfiguration object
 * and proceeds to initiate the network request.  Results are stored in
 * a byte array that can in turn be requested by the client.
 */
public class HttpRequestExecutor15 {

	
	 private String GZIP = "gzip";
    /**
     * URL encode scheme (default to "UTF-8")
     */
     private String urlenc = "UTF-8";

    /**
     * Used to temporarily store data read from input stream
     */
     private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * Buffer to read data into
     */
     private byte[] buffer = new byte[1024];

    /**
     * The network response (in bytes)
     */
     private byte[] results = null;

    /**
     * The connect time out  (in millis)
     */
     private int connectTimeout = 0;

    /**
     * The read time out  (in millis)
     */
     private int readTimeout = 0;

    /**
     * Default constructor.
     */
     public HttpRequestExecutor15() {}

    /**
     * Returns the results of the network request.
     *
     * @return byte[] the response
     */
     public byte[] getResults() {
        return results;
     }

    /**
     * Set the connect time out
     *
     * @param timeout int
     */
     public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
     }

    /**
     * Set the read timeout
     *
     * @param timeout int
     */
     public void setReadTimeout(int timeout) {
        readTimeout = timeout;
     }

    /**
     * Configures the key/value pairs
     */
     private StringBuffer processQueryString(Hashtable queryHash) {

        if (queryHash==null)
           return null;

        Enumeration eKeys = queryHash.keys();
        StringBuffer query = new StringBuffer();
        while (eKeys.hasMoreElements()) {
           String key = (String)eKeys.nextElement();
           String value = (String)queryHash.get(key);

           if (key == null || value == null) continue;

           query.append(key);
           query.append("=");
           try {
              query.append(URLEncoder.encode(value,urlenc));
              if (eKeys.hasMoreElements())
                 query.append("&");
           }
		      catch(Exception e)
           {}
        }

        if (query.length() < 1)
           return null;

        return query;
     }

    /**
     * Parses out the Cookie name.
     *
     * @return String The value of the cookie name
     */
     public String parseCookieName(String target) {
        return target.substring(0,target.indexOf("="));
     }

    /**
     * Parses out the Cookie value.
     *
     * @return String The cookie value
     */
     public String parseCookieValue(String target) {
        return target.substring(target.indexOf("=")+1,target.indexOf(";"));
     }

    /**
     * Executes an http/https network request on behalf of the client.
     */
     public void execute(HttpConfiguration c) throws BusinessException {

       InputStream is = null;

       try {

          // allow for https communication
          System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

          URL url = new URL(c.getURL());
          URLConnection conn = url.openConnection();

          conn.setConnectTimeout(connectTimeout);
          conn.setReadTimeout(readTimeout);
          conn.setDoInput(true);
          conn.setAllowUserInteraction(false);
          conn.setUseCaches(false);
          conn.setRequestProperty("Accept-Encoding", GZIP);

          Hashtable cookies = c.getCookies();

          if (cookies!=null) {
             StringBuffer csb = new StringBuffer();

             Enumeration e = cookies.keys();

             while (e.hasMoreElements()) {
                String key   = (String)e.nextElement();
                String value = (String)cookies.get(key);
                csb.append(key);
                csb.append("=");
                csb.append(value);
                csb.append(";");
             }

             if (csb.toString() != null && csb.toString().length() > 0)
                conn.setRequestProperty("Cookie", csb.toString());
          }

          Hashtable requestProperties = c.getRequestProperties();

          if (requestProperties != null) {
             Enumeration e = requestProperties.keys();

             while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                String value = (String)requestProperties.get(key);
                conn.setRequestProperty(key,value);
             }
          }


          // get the URL encode scheme
          if (c.getURLEncode() != null && c.getURLEncode().length() > 0)
             urlenc = c.getURLEncode();

          StringBuffer query;

          if ( (query = processQueryString(c.getQueryHash())) == null)
             conn.setDoOutput(false);
          else {
             conn.setDoOutput(true);
             if (c.getEndQueryString() != null) {
		        query.append(c.getEndQueryString());
		     }
             //System.out.println("query string: " +query.toString());

             c.addRequestProperty("Content-Length",String.valueOf(query.toString().length()));
             OutputStream os = conn.getOutputStream();
             os.write(query.toString().getBytes());
             os.close();
          }

          
          if ((GZIP).equalsIgnoreCase(conn.getContentEncoding())) {
        	  is = new GZIPInputStream(conn.getInputStream());
          }
          else {
        	  is = conn.getInputStream(); 
          }
          
          

          int i=0;
          String key, value;

          while (true) {
             key = conn.getHeaderFieldKey(i);
             value = conn.getHeaderField(i);

             if (key == null && value == null)
                break;

             if (key != null && key.equalsIgnoreCase("Set-cookie")) {
                String cookieName  = parseCookieName(value);
                String cookieValue = parseCookieValue(value);
                if (cookies != null)
                   cookies.put(cookieName, cookieValue);
             }
             i++;
          }


          baos.reset();

          int bytesRead = 0;

          while ( (bytesRead = is.read(buffer)) != -1) {
             baos.write(buffer, 0, bytesRead);
          }

          results = baos.toByteArray();

       }
       catch (Exception e) {
          e.printStackTrace();
          throw new BusinessException("HttpRequestExecutor15.execute - "+
                                      "Exception encountered.",e);
       }
       finally {
          try {
			 if (is != null)
             	is.close();
          }
          catch (IOException ioe) {}
       }
    }
}

