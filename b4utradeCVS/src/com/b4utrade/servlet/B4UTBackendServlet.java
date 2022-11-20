
package com.b4utrade.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.tacpoint.servlet.DefaultServlet;
import com.tacpoint.util.*;
import com.tacpoint.exception.*;
import com.b4utrade.bo.*;


public class B4UTBackendServlet extends B4UTServlet
{


    public void init(ServletConfig config)
    	throws ServletException
    {
       super.init(config);
     
    }

    public void forwardToLoginPage(HttpServletRequest req, 
                                   HttpServletResponse res) throws Exception
    {
       boolean doLog=Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
       Logger.debug("B4UTBackendServlet:The current session has expired.",doLog);
       String msg = "Your session has expired.  Please re-login.";
       req.setAttribute("LOGIN_MESSAGE",msg);
       String url = "/b4utrade/backend/jsp/Backend_Login.jsp";
       RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
       rd.forward(req,res);    
    }

    
    protected String checkUser(HttpSession hs)
    {
        return((String)hs.getValue("BACKEND_USERNAME"));
    }
        

    public String getServletInfo()
    {
    	return "B4UTBackendServlet: Copyright(c) 2000, "+
               "Tacpoint Technologies Inc.";
    }

    public void destroy() {
       super.destroy();
    }
}

