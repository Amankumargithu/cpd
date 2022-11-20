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
import com.b4utrade.web.form.ContactUsForm;


/**
 * The Action class expires an user from an existing session.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2003.  All rights reserved.
 * @version 1.0
 */

public class QuoddContactUsAction extends B4UTradeDefaultAction {

   static Log log = LogFactory.getLog(QuoddContactUsAction.class);

   static String CREATE = "create";
   static String DONE = "done";
   static String FAIL = "fail";

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

      ContactUsForm cuf = (ContactUsForm) form;

      String forwardAction = ContactUsForm.NEW;

      if (cuf.getAction().equals(ContactUsForm.NEW)){
         forwardAction =  CREATE;
      }
      else
      if (cuf.getAction().equals(ContactUsForm.SEND)){
         forwardAction =  sendEmail(request, cuf);
      }


      return (mapping.findForward(forwardAction));
   }

   private String sendEmail(HttpServletRequest request, ContactUsForm cuf) {

        StringBuffer sb = new StringBuffer();
        EmailManager em = new EmailManager();


        String smtp = Environment.get("SMTP_SERVER");

        String toAddress = Environment.get("CONTACT_US_FROM_ADDRESS");
        String fromAddress = Environment.get("SERVER_FROM_ADDRESS");

        String subject   = "ContactUs Info";
        String openTags  = "<html><body><font face=\"Arial\" size=\"2\">";
        String beginLine = "<p>";
        String endLine = "</p>";
        String closeTags = "</font></body></html>";
        sb.append(openTags);
        sb.append(beginLine);
        sb.append("First name: " + cuf.getFirstName());
        sb.append(endLine);
        sb.append(beginLine);
        sb.append("Last name: " + cuf.getLastName());
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("Email: " + cuf.getEmail());
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("Company Name: " + cuf.getCompanyName());
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("Job title: " + cuf.getJobTitle());
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("Phone: " + cuf.getPhone());
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("location: " + cuf.getLocation(cuf.getLocationId()));
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("help: " + cuf.getHelp(cuf.getHelpId()));
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("hear us from: " + cuf.getHearUs(cuf.getHearUsId()));
        sb.append(endLine);

        sb.append(beginLine);
        sb.append("Question: " + cuf.getQuestion());
        sb.append(endLine);
        sb.append(closeTags);

        try {
            em.send(toAddress,null,null,
                    fromAddress,smtp,subject,
                    sb.toString(),
                    true,false,null);
        }
        catch (javax.mail.MessagingException me) {

            //throw new BusinessException(me.getMessage());
            return FAIL;
        }
        return DONE;

   }


}
