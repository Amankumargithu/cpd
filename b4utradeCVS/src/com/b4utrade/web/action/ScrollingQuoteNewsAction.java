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

import com.b4utrade.web.form.HomeForm;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;


/**
 * Handles modification of ticker list for the Wall of Stocks.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2003.  All rights reserved.
 * @version 1.0
 */

public class ScrollingQuoteNewsAction extends B4UTradeDefaultAction {

   static Log log = LogFactory.getLog(ScrollingQuoteNewsAction.class);


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

      HomeForm homeForm = (HomeForm)form;

      try {
         response.setContentType("text/html");
         HttpSession hs = request.getSession(false);

         String userID = (String)hs.getValue("USERID");
         if (userID == null) 
           userID = "";
         homeForm.setUserID(userID);

         String stockList = (String)hs.getValue("STOCK_LIST");
         if (stockList == null) {
            stockList = Environment.get("INITIAL_STOCK_LIST");
            hs.putValue("STOCK_LIST", stockList);
         }
         homeForm.setStockList(stockList);      
 
         String sessionID = hs.getId();
         homeForm.setSessionID(sessionID);

         String customNews = (String)hs.getValue("CUSTOM_NEWS");
         if (customNews == null) {
            customNews = new String();
         }

         Vector customNewsVec = new Vector();
         StringTokenizer newsTokenizer = new StringTokenizer(customNews,",");

         while (newsTokenizer.hasMoreTokens()) {
            customNewsVec.addElement(newsTokenizer.nextToken());
         }

         request.setAttribute("CUSTOMNEWSLIST", customNewsVec);
      }
      catch(Exception e) {
         String msg = "Unable to retrieve data.";
         log.error(msg, e);
         request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
         return (mapping.findForward(FAILURE)); 
      }

      return (mapping.findForward("success"));
   }


}
