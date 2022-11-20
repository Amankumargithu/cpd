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

import com.b4utrade.bo.StockNewsBO;
import com.b4utrade.web.form.ScrollingNewsForm;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;


/**
 * Handles modification of ticker list for the Wall of Stocks.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2003.  All rights reserved.
 * @version 1.0
 */

public class ScrollingNewsDetailHeaderAction extends B4UTradeDefaultAction {

   static Log log = LogFactory.getLog(ScrollingNewsDetailHeaderAction.class);

   private final String TYPE_NEWS = "N";

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

      doCheckUser = false;
      doCheckReferalPartner = false;
      ActionForward forward = super.execute(mapping, form, request, response);
      if (forward != null)
         return forward;
         
      ScrollingNewsForm newsForm = (ScrollingNewsForm)form;         

      try 
      {
         response.setContentType("text/html");
         HttpSession hs = request.getSession(false);

		     String backToId = (String)request.getParameter("BACK_TO_ID");
        log.info("ScrollingNewsDetailHeaderAction: Back To Id = "+backToId);		     
            
         //String popup = (String)request.getParameter("POPUP");
         //if (popup == null) 
         //{
         //   popup = "Y";
		     //}            

         String date = "";
	       String desc = "";
	       String newsTicker = "";
	       if (request.getParameter("NEWS_DATE") != null)
	           date = request.getParameter("NEWS_DATE");
        log.info("ScrollingNewsDetailHeaderAction: date = "+date);	           
	       if (request.getParameter("NEWS_DESC") != null)
	           desc = request.getParameter("NEWS_DESC");
        log.info("ScrollingNewsDetailHeaderAction: desc = "+desc);	           
	       if (request.getParameter("NEWS_TICKER") != null)
	           newsTicker = request.getParameter("NEWS_TICKER");
        log.info("ScrollingNewsDetailHeaderAction: ticker = "+newsTicker);	           
	        
	       String heading = date + ", " + desc;
         if (heading.length() > 95)
            heading = heading.substring(0,91) + "...";	
        log.info("ScrollingNewsDetailHeaderAction: heading = "+heading);                    


	       if (backToId != null && backToId.equals("CUSTOMIZED")) 
	       {
	          ///newsForm.setNewsMoreURL("/b4utrade/jsp/app/ScrollingNewsHeadlineFrameset.jsp?NEWSTICKER="+newsTicker);
	          newsForm.setNewsMoreURLText("More News");
	          //newsForm.setNewsForwardURL("");
	          //newsForm.setNewsForwardURLText("close"); 
	          //newsForm.setNewWindowFlag("Y");
         }
	       else if (backToId != null && backToId.equals("TODAY")) 
	       {
	          //newsForm.setNewsMoreURL("/b4utrade/jsp/app/ScrollingNewsHeadlineFrameset.jsp?NEWSTICKER="+newsTicker);
	          newsForm.setNewsMoreURLText("More News");
	          //newsForm.setNewsForwardURL("");
	          //newsForm.setNewsForwardURLText("close"); 
	          //newsForm.setNewWindowFlag("Y");
         }
         else if (backToId != null && backToId.equals("UPCLOSE")) 
	       {
	          //newsForm.setNewsMoreURL("/b4utrade/jsp/app/ScrollingNewsHeadlineFrameset.jsp?NEWSTICKER="+newsTicker);
	          newsForm.setNewsMoreURLText("More News");
	          //newsForm.setNewsForwardURL("");
	          //newsForm.setNewsForwardURLText("close"); 
	          //newsForm.setNewWindowFlag("");
	       }
	       else if (backToId != null && backToId.equals("IPO")) 
	       {
	          //newsForm.setNewsMoreURL("");
	          newsForm.setNewsMoreURLText("N/A");
	          //newsForm.setNewsForwardURL("");
	          //newsForm.setNewsForwardURLText("close"); 
	          //req.setAttribute("BACK_URL", "/servlet/b4utradepartner/SearchIPOStatusServlet?IPOSEARCHTICKER="+req.getParameter("NEWS_PARAM"));
	          //newsForm.setNewWindowFlag("");	          
	       }
	       else if (backToId != null && backToId.equals("HEADLINES"))
	       {
	          //newsForm.setNewsMoreURL("/b4utrade/jsp/app/ScrollingNewsHeadlineFrameset.jsp?NEWSTICKER="+newsTicker);
	          newsForm.setNewsMoreURLText("More News");
	          //newsForm.setNewsForwardURL("/b4utrade/app/StockUpCloseBody.do");
	          //newsForm.setNewsForwardURLText("Back to Stocks Up Close"); 
	          //newsForm.setNewWindowFlag("");	          
	       }
	       else if (backToId != null && backToId.equals("STREAMING")) 
	       {
	          //newsForm.setNewsMoreURL("/b4utrade/jsp/app/ScrollingNewsHeadlineFrameset.jsp?NEWSTICKER="+newsTicker);
	          newsForm.setNewsMoreURLText("More News");
	          //newsForm.setNewsForwardURL("");
	          //newsForm.setNewsForwardURLText("close"); 
	          //newsForm.setNewWindowFlag("Y");
	       }
         else
         {
	          //newsForm.setNewsMoreURL("");
	          newsForm.setNewsMoreURLText("N/A");          
	          //newsForm.setNewsForwardURL("");
	          //newsForm.setNewsForwardURLText("close"); 
	          //newsForm.setNewWindowFlag("Y");
         }	       
         
log.info("ScrollingNewsDetailHeaderAction: heading = "+heading);
log.info("ScrollingNewsDetailHeaderAction: ticker = "+newsTicker);

         newsForm.setNewsHeader(heading);
         newsForm.setNewsTicker(newsTicker);
         //newsForm.setNewsTarget(targetPage);
         //newsForm.setNewsBackToID(backToFlag);
         //newsForm.setNewsPopupFlag(popup);

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
