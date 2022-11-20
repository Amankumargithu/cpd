package com.b4utrade.web.action;


import java.util.*;
import java.io.IOException;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tacpoint.util.*;

import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;


/**
 * 
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2003.  All rights reserved.
 * @version 1.0
 */

public class TickerLookupFramesAction extends B4UTradeDefaultAction {

   static Log log = LogFactory.getLog(TickerLookupFramesAction.class);


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
   public ActionForward execute(ActionMapping mapping,
                 ActionForm form,
                 HttpServletRequest request,
                 HttpServletResponse response)
   throws IOException, ServletException {

      ActionForward forward = super.execute(mapping, form, request, response);
      if (forward != null)
         return forward;

      try {
         response.setContentType("text/html");

         String targetWindow = request.getParameter("targetwindow");
         String targetServlet = request.getParameter("targetservlet");
         String returnParam = request.getParameter("returnparam");

         if ((targetWindow == null) && (targetServlet == null)) {
            targetWindow = (String)request.getAttribute("targetwindow");
            targetServlet = (String)request.getAttribute("targetservlet");
            returnParam = (String)request.getAttribute("returnparam");
         }

         request.setAttribute("targetwindow", targetWindow);
         request.setAttribute("targetservlet", targetServlet);
         request.setAttribute("returnparam", returnParam);

      }
      catch(Exception e) {
         log.error("TickerLookupFramesAction.execute: ", e);
         return (mapping.findForward(FAILURE)); 
      }

      return (mapping.findForward("success"));
   }


}
