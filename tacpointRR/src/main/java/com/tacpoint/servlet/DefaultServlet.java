/**
 * Copyright 1999 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  andy katz
 * @author akatz@tacpoint.com
 * @version 1.0
 * Date created: 12/28/1999
 * Date modified:
 * - 12/28/1999 Initial version
 *
 * This class contains the source for DefaultServlet.  It should be used
 * as a subclass for all application level servlets.  
 *
 */

package com.tacpoint.servlet;

import com.tacpoint.util.*;
import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class DefaultServlet extends HttpServlet
{  

   /**
    * Initialize the <code>Environment</code> instance.
    * Initialize the <code>Logger</code> instance.
    *
    * @param	config a ServletConfig objecft
    */
    public void init(ServletConfig config) throws ServletException
    {
       super.init(config);
       try {
          Logger.init();
          Environment.init();
       }
       catch (Exception e) {}
    }

   /**
    * Default method implementation of HttpServlet
    *
    * @param	req HttpServletRequest
    * @param	res HttpServletResponse
    */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
    	throws IOException
    {

        doPost(req,res);
    }

   /**
    * Default method implementation of HttpServlet
    *
    * @param	req HttpServletRequest
    * @param	res HttpServletResponse
    */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    	throws IOException
    {
    
        try {
           handleRequest(req, res);
        }
        catch (Exception ex) {
           // forward to error page...
           Logger.log("DefaultServlet: Excepetion encountered. ",ex);
        }

    }
   
   /**
    * Used to save the session across multiple servers if necessary.
    */
    protected void saveSession(HttpServletRequest req) 
    {
    }

   /**
    * Clients of this class should override this application level method.
    */
    public void handleRequest(HttpServletRequest req, 
                              HttpServletResponse res) throws Exception
    {
       return; 
    }

   /**
    * Provide class name and author to the client
    */
    public String getServletInfo()
    {
    	return "DefaultServlet - Copyright(c) 1999, Tacpoint Technologies Inc.";
    }

   /**
    * Perform any clean-up.
    */
    public void destroy() {
       super.destroy();
    }

}

