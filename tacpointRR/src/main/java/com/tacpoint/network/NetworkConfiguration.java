/**
 * NetworkConfiguration - A class to be used by the NetworkCommunicator class
 * It will contain relevant information about the network connection and
 * the query string to be passed to the server.
 */

package com.tacpoint.network;

import java.util.*;
import java.io.*;

public class NetworkConfiguration implements Serializable
{
   /**
    * Used to perform any modifications to the results hash
    * upon return from the servlet.
    */
   private IPostNetworkRequestHandler postNetworkRequestHandler;

   /**
    * Internal hashtable used to store results from a network call.
    */
   private Hashtable resultsHash;

   /**
    * String object holding the session ccokie.
    */
   private String cookie;

   private byte terminator;

   /**
    * String object holding the client id
    */
   private String client;

   /**
    * boolean representing the debug mode of the NetworkConfiguration object
    */
   private boolean debug;

   /**
    * String object holding the target servlet name.
    * E.g., MyServlet
    */
   private String servlet;

   /**
    * String containing the destination url.
    * E.g., http://www.domain.com:81/servlet
    */
   private String url;
   private String streamingURL;


   /**
    * An Exception reference used to hold any Exception encountered
    * by the helper classes.
    */
   private Exception ex;

   /**
    * Represents the amount of time (in milliseconds) between
    * network requests. This is only a suggestion amount, i.e., it
    * could take longer to process if network traffic is heavy.
    */
   private long delay;

   /**
    * Represents the time at which this configuration was last executed.
    */
   private long lastExecutionTime;

   /**
    * Hashtable used to store key/value pairs used in the query string.
    * The key should always be a <code>String</code> object,
    * e.g., "PERSON" and the value must always be a <code>Vector</code>
    * of size > 0.  The Vector object is used to provide the ability to
    * concatenate more than 1 value (comma delimited) for a specified key.
    */
   private Hashtable queryHash;

   /**
    * Default constructor.
    */
   public NetworkConfiguration() {
      queryHash = new Hashtable();
      resultsHash = new Hashtable();
      servlet = "";
      url = "";
      streamingURL="";
      cookie = "";
      ex = null;
      lastExecutionTime = 0;
      debug=false;
      client = "N/A";
      terminator=com.tacpoint.publisher.TConstants.TERMINATOR_BYTE;
   }

   /**
    * Returns the queryHash object
    *
    * @return Hashtable The queryHash.
    */
   public Hashtable getQueryHash() {
      return queryHash;
   }

   /**
    * Returns the resultHash object
    *
    * @return Hashtable The resultsHash.
    */
   public Hashtable getResultsHash() {
      return resultsHash;
   }

   /**
    * Returns a single object from the resultsHash
    *
    * @return object The value based upon the corresponding key.
    */
   public Object getSingleResultsHashObject(String key) {
      if (key!=null)
         return resultsHash.get(key);
      return null;
   }

   /**
    * Sets the resultHash object
    *
    * @param resultsHash the new hashtable
    */
   public void setResultsHash(Hashtable resultsHash) {
      this.resultsHash = resultsHash;
   }

   /**
    * Returns the exception
    *
    * @return exception
    */
   public Exception getException() {
      return ex;
   }

   /**
    * Sets the exception
    *
    * @param ex the new exception
    */
   public void setException(Exception ex) {
      this.ex = ex;
   }

   /**
    * Returns the cookie string
    *
    * @return String The cookie.
    */
   public String getCookie() {
      return cookie;
   }

   public byte getTerminator() {
      return terminator;
   }

   /**
    * Sets the cookie string
    *
    * @param cookie the new cookie
    */
   public void setCookie(String cookie) {
      this.cookie = cookie;
   }

   public void setTerminator(byte terminator) {
      this.terminator = terminator;
   }

   /**
    * Returns the client string
    *
    * @return String The client.
    */
   public String getClient() {
      return client;
   }

   /**
    * Sets the client string
    *
    * @param client the new client
    */
   public void setClient(String client) {
      this.client = client;
   }

   /**
    * Returns the PostNetworkRequestHandler
    *
    * @return IPostNetworkRequestHandler postNetworkRequestHandler
    */
   public IPostNetworkRequestHandler getPostNetworkRequestHandler() {
      return postNetworkRequestHandler;
   }

   /**
    * Returns the debug value
    *
    * @return boolean debug
    */
   public boolean getDebug() {
      return debug;
   }

   /**
    * Sets the debug boolean
    *
    * @param debug boolean
    */
   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   /**
    * Sets the Post Network Request Handler
    *
    * @param iPostNetworkRequestHandler IPostNetworkRequestHandler
    */
   public void setPostNetworkRequestHandler(IPostNetworkRequestHandler postNetworkRequestHandler) {
      this.postNetworkRequestHandler = postNetworkRequestHandler;
   }

   /**
    * Returns the servlet string
    *
    * @return String The servlet.
    */
   public String getServlet() {
      return servlet;
   }

   /**
    * Sets the servlet string
    *
    * @param servlet the new servlet
    */
   public void setServlet(String servlet) {
      this.servlet = servlet;
   }

   /**
    * Returns the url string
    *
    * @return String The url.
    */
   public String getURL() {
      return url;
   }

   public String getStreamingURL() {
      return streamingURL;
   }

   /**
    * Sets the url string
    *
    * @param url the new url
    */
   public void setURL(String url) {
      this.url = url;
   }

   public void setStreamingURL(String streamingURL) {
      this.streamingURL = streamingURL;
   }
   /**
    * Returns the last execution time
    *
    * @return long lastExecutionTime
    */
   public long getLastExecutionTime() {
      return lastExecutionTime;
   }

   /**
    * Sets the last execution time attribute
    *
    * @param lastExecutionTime a long representing the time (in milliseconds)
    *        since this object was last executed.
    */
   public void setLastExecutionTime(long lastExecutionTime) {
      this.lastExecutionTime = lastExecutionTime;
   }

   /**
    * Returns the delay time
    *
    * @return long delay.
    */
   public long getDelay() {
      return delay;
   }

   /**
    * Sets the delay attribute
    *
    * @param delay a long representing the time (in milliseconds) between
    *              requests.
    */
   public void setDelay(long delay) {
      if (delay >= 0)
         this.delay = delay;
   }

   /**
    * Combines the two NetworkConfiguration objects.
    *
    * @param target the item to be merged.
    */
   public void merge(NetworkConfiguration nc) {
      if (nc==null)
         return;

      // verify equality...
      if (!this.url.equals(nc.url))
         return;
      if (!this.servlet.equals(nc.servlet))
         return;
      if (!this.cookie.equals(nc.cookie))
         return;

      if (this.delay > nc.delay)
         this.delay = nc.delay;

      //System.out.println("Merging NetworkConfiguration objects.");
      Enumeration e = nc.queryHash.keys();
      while (e.hasMoreElements()) {
         String key = (String)e.nextElement();
         Vector v1 = (Vector)this.queryHash.get(key);
         Vector v2 = (Vector)nc.queryHash.get(key);

         if (v1==null) {
            this.queryHash.put(key,v2);
            continue;
         }

         if (v1.equals(v2))
            continue;

         for (int i=0; i<v2.size(); i++) {
            //System.out.println("adding elements to vec.");
            v1.addElement(v2.elementAt(i));
         }

         //System.out.println("Vec size = " + v1.size());
      }
   }

   /**
    * Adds a new String to the Vector associated with the following
    * key to the queryHash.
    *
    * @param key a <code>String</code> containing a unique name.
    * @param value a <code>String</code> containing a value.
    */
   public void addQueryKeyValue(String key, String value) {
      Vector v = (Vector)queryHash.get(key);
      if (v == null) {
         v = new Vector();
      }
      v.addElement(value);
      queryHash.put(key,v);
   }

   /**
    * Removes an existing String from the Vector associated with the following
    * key to the queryHash.
    *
    * @param key a <code>String</code> containing a unique name.
    * @param value a <code>String</code> containing a value.
    */
   public synchronized void removeQueryKeyValue(String key, String value) {

      Vector v = (Vector)queryHash.get(key);
      if (v == null) {
         return;
      }

      //synchronized(v) {
         v.removeElement(value);
         if (v.size() < 1)
            queryHash.remove(key);
      //}

      //if (v.size() > 0)
      //   queryHash.put(key,v);
   }

}
