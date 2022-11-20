/**
 * HttpConfiguration - A class to be used by the HttpRequestExecutor class
 * It will contain relevant information about the network connection and
 * the query string to be passed to the server.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 */

package com.tacpoint.http;

import java.util.Hashtable;

public class HttpConfiguration
{

   private String endQueryString;

   /**
    * Hashtable containing key (String) / value (String) objects.
    */
   private Hashtable cookies;

   /**
    * Hashtable containing key Request Properties
    */
   private Hashtable requestProperties;

   /**
    * String containing the destination url.
    * E.g., http://www.domain.com:81/servlet/MyServlet
    */
   private String url;

   /**
    * String containing the url encode scheme.
    * E.g., UTF-8, ISO-8859-1
    */
   private String urlenc;

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
   public HttpConfiguration() {
      queryHash = new Hashtable();
      cookies = new Hashtable();
      requestProperties = new Hashtable();
      url = "";
      urlenc = "";
   }


   public void addRequestProperty(String key, String value) {
      if (key != null && value != null)
         requestProperties.put(key,value);
   }

   public Hashtable getRequestProperties() {
      return requestProperties;
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
    * Returns the cookies object
    *
    * @return Hashtable The cookies hash.
    */
   public Hashtable getCookies() {
      return cookies;
   }

   /**
    * Adds a new cookie to the cookie hashtable.
    *
    * @param key String
    * @param value String
    */
   public void addCookie(String key, String value) {
      if (key != null && value != null)
         cookies.put(key,value);
   }

   /**
    * Delete a cookie from the hashtable
    *
    * @param key the cookie to be removed
    */
   public void removeCookie(String key) {
      if (key!=null)
         cookies.remove(key);
   }

   /**
    * Returns the end Query String
    *
    * @return String The endQueryString.
    */
   public String getEndQueryString() {
      return endQueryString;
   }

   /**
    * Returns the url string
    *
    * @return String The url.
    */
   public String getURL() {
      return url;
   }

   /**
    * Returns the urlenc string
    *
    * @return String The urlenc.
    */
   public String getURLEncode() {
      return urlenc;
   }

   /**
    * Sets the end query string
    *
    * @param endString the new endQueryString
    */
   public void setEndQueryString(String endString) {
      this.endQueryString = endString;
   }

   /**
    * Sets the url string
    *
    * @param url the new url
    */
   public void setURL(String url) {
      this.url = url;
   }

   /**
    * Sets the urlenc string
    *
    * @param urlenc the new urlenc
    */
   public void setURLEncode(String urlenc) {
      this.urlenc = urlenc;
   }

   /**
    * Adds a new key/value pair to the queryHash.
    *
    * @param key a <code>String</code> containing a unique name.
    * @param value a <code>String</code> containing a value.
    */
   public void addQueryKeyValue(String key, String value) {
      if (key == null || value == null)
         return;
      queryHash.put(key,value);
   }

   /**
    * Removes an existing key from the queryHash.
    *
    * @param key a <code>String</code> containing a unique name.
    */
   public synchronized void removeQueryKey(String key) {
      if (key == null)
         return;
      queryHash.remove(key);
   }
}
