/** 
 * ServletMessenger - This class provides an interface to execute servlets
 *                    on any server.
 *
 * Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 * @version 1.0
 * Date created: 02/13/2001
 * Date modified:
 * - 02/13/2001 Initial version
 *
 */

package com.tacpoint.network;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import com.tacpoint.exception.*;
import com.tacpoint.util.*;

/**
 * This class allows a client to invoke a servlet for a given ip address
 * and port number.  The optional session id can be passed along allowing
 * access to the user's stateful session data.  There also exists an optional
 * search results string parameter that can be utilized to search for a 
 * pre-determined result from the output of the servlet.
 */
public class ServletMessenger {

    /**
     * The IP address of the server
     */
     String ip; 

    /**
     * The port number that the server is listening on
     */
     int    port;   

    /**
     * The servlet name the client wants to execute
     */
     String servlet; 

    /**
     * The user's session id %lt;optional%gt; you would like to access
     */
     String sessionID;

    /**
     * Vector object holding the Cookie(s)
     */
     Vector cookies;

    /**
     * The results string %lt;optional%gt; to search for within the server's 
     * output. 
     */
     String searchResultsString;

    /**
     * Constructor
     *
     * @param ip                  - IP address of the server
     * @param port                - Port number of the server
     * @param servlet             - Servlet name 
     * @param sessionID           - %lt;optional%gt; Session ID for a given user
     * @param searchResultsString - %lt;optional%gt; Search string to look for 
     */
     public ServletMessenger(String ip, int port, String servlet,
                             String sessionID, String searchResultsString) {
        this.ip = ip;
        this.port = port;
        this.servlet = servlet;
        this.sessionID = sessionID;
        this.searchResultsString = searchResultsString;

        this.cookies = new Vector();

     }

    /**
     * Set the IP address
     *
     * @param ip - IP address of the server
     */
     public void setIP(String ip) {
        this.ip = ip;
     }

    /**
     * Returns the IP address
     *
     * @return String The IP address.
     */
     public String getIP() {
        return this.ip;
     }

    /**
     * Set the Port number
     *
     * @param port - port number
     */
     public void setPort(int port) {
        this.port = port;
     }

    /**
     * Returns the Port number
     *
     * @return int The port number
     */
     public int getPort() {
        return this.port;
     }

    /**
     * Set the servlet name
     *
     * @param servlet - servlet name
     */
     public void setServlet(String servlet) {
        this.servlet = servlet;
     }

    /**
     * Returns the Servlet name 
     *
     * @return String The servlet name
     */
     public String getServlet() {
        return this.servlet;
     }

    /**
     * Set the Session id 
     *
     * @param sessionID - user's session ID
     */
     public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
     }

    /**
     * Returns the Session ID 
     *
     * @return String The session ID
     */
     public String getSessionID() {
        return this.sessionID;
     }

    /**
     * Adds a new Cookie to the cookie vector.
     *
     * @param cookie the new cookie value
     */
     public void addCookie(Cookie cookie) {
        if (cookie!=null)
          cookies.addElement(cookie);
     } 

    /**
     * Deletes the Cookie from the cookie vector.
     *
     * @param cookie the cookie to be removed
     */
     public void removeCookie(Cookie cookie) {
        if (cookie!=null)
           cookies.remove(cookie);
     }

    /**
     * Returns the cookies object
     *
     * @return Vector The cookies Vector.
     */
     public Vector getCookies() {
        return cookies;
     }

    /**
     * Set the Search Results String 
     *
     * @param searchResultsString -  The search results string to search for
     */
     public void setSearchResultsString(String searchResultsString) {
        this.searchResultsString = searchResultsString;
     }

    /**
     * Returns the Search Results String
     *
     * @return String The search results string 
     */
     public String getSearchResultsString() {
        return this.searchResultsString;
     }

    /**
     * Returns true if the client's search results string
     * is found in the servlet's output stream, else returns
     * false 
     *
     * @return boolean Success or failure
     */
     public boolean execute() {

        boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        String session = "";
        boolean ok = true;

        URL urlText;

        Logger.debug("Executing ServletMessenger.execute() - ", doLog);

        if (sessionID!=null)
           session = "/" + sessionID;

        try
        {
/*
           sock = new Socket(ip,port);
           os = sock.getOutputStream();
           String req = "GET " + 
                     servlet + 
                     session +
                    " HTTP/1.0\r\n" + 
                    "User-Agent: com.tacpoint.network.ServletMessenger\r\n\r\n";
           os.write(req.getBytes());
           os.flush();

           is = sock.getInputStream();
           InputStreamReader isr = new InputStreamReader(is);
           BufferedReader in = new BufferedReader(isr);
            

            Logger.debug("ServletMessenger.execute() - new URL", doLog);
            urlText = new URL(req);
            
            Logger.debug("ServletMessenger.execute() - open Stream", doLog);

            is = urlText.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);

*/

            String req = "http://" + ip + ":" + port + servlet;
            
            URL url = new URL(req);
            URLConnection conn = url.openConnection();
            
            if (cookies!=null) {
                
                StringBuffer csb = new StringBuffer();
                for (int i=0; i<cookies.size(); i++) {
                
                    Cookie cookie = (Cookie)cookies.elementAt(i);
                    csb.append(cookie.getName());
                    csb.append("=");
                    csb.append(cookie.getValue());
                    csb.append(";");
                
                }
             
                conn.setRequestProperty("Cookie", csb.toString());
            }

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader in = new BufferedReader(isr);

            Logger.debug("ServletMessenger.execute() - look for searchResultString", doLog);

           if (searchResultsString!=null) {
              while (true) {
                 String next = in.readLine();
                 if (next==null) {
                    ok = false;
                    break;
                 }
                 if (next.endsWith(searchResultsString)) {
                    ok = true;
                    break;
                 } 
              }
           }
        }
        catch (Exception e) {
           Logger.log("ServletMessenger.execute() - Error encountered.",e);
           ok = false;
        }
        finally {

           Logger.debug("ServletMessenger.execute() - finally", doLog);
            
           try {
              is.close();
              os.close();
              sock.close();
           }
           catch (Exception e) {}
        }

        Logger.debug("ServletMessenger.execute() - return ok=" + ok, doLog);
        return ok;
     }
}
