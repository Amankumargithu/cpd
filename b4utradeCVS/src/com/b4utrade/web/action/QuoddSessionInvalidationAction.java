package com.b4utrade.web.action;


import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.helper.ConsoleTimestampLogger;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;


/**
 * The Action class logs off a user.
 *
 * @author Paxcel
 * @version 1.0
 */

public class QuoddSessionInvalidationAction extends B4UTradeDefaultAction {

   static Log log = LogFactory.getLog(QuoddSessionInvalidationAction.class);


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

      try 
      {
    	  ConsoleTimestampLogger.println("Got request: QuoddSessionInvalidationAction: ");
         HttpSession hs = request.getSession(false);
         response.setContentType("text/html");
      	 
         String message = "unable to invalidate";
            if (hs != null)
            {

            	ConsoleTimestampLogger.println("QuoddSessionInvalidationAction: GOT the sessionID " + hs.getId());
	            try 
	            {
	            	hs.invalidate();
	            	message = "successfully invalidated";
				} 
	            catch (Exception e) 
				{
					e.printStackTrace();
				}
            }
            else
            {
            	ConsoleTimestampLogger.println("Hs is null");
            }
 
         ConsoleTimestampLogger.println(message);
         ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
         XMLEncoder encoder = new XMLEncoder(byteArray);
         encoder.writeObject(message);
         encoder.close();

         ServletOutputStream sos = null;

         try
         {
             sos = response.getOutputStream();

              sos.write(byteArray.toByteArray());
              sos.flush();
         } catch (Exception e)
         {
          log.error("QuoddHandleLogoutAction encountered exception. ", e);
         }
         finally
         {
              if (sos != null)
              {
                  try
                  {
                      sos.close();
                  } catch (Exception e)
                  {}
              }
          }         
      }
      catch (Exception e) {
         String msg = "System error occurred while trying to Invalidate.";
         log.error(msg, e);
         request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
         return (null); 
      }

      return (null);
   }


}
