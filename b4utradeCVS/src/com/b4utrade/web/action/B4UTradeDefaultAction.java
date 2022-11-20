package com.b4utrade.web.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;
import com.tacpoint.util.Environment;

/**
 * The parent class to all B4UTrade action specific classes.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2003. All rights
 *         reserved.
 * @version 1.0
 */

public class B4UTradeDefaultAction extends Action {

	static Log log = LogFactory.getLog(B4UTradeDefaultAction.class);

	protected static String FAILURE = "failure";
	protected static String LOGIN = "login";

	protected boolean doCheckUser = true;
	protected boolean doCheckReferalPartner = true;
	protected String userName = "";

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it). Return an
	 * <code>ActionForward</code> instance describing where and how control should
	 * be forwarded, or <code>null</code> if the response has already been
	 * completed.
	 *
	 * @param mapping    The ActionMapping used to select this instance
	 * @param actionForm The optional ActionForm bean for this request (if any)
	 * @param request    The HTTP request we are processing
	 * @param response   The HTTP response we are creating
	 *
	 * @exception IOException      if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		try {
			Environment.init();
		} catch (Exception e) {
			String msg = "Unable initialize environment.";
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}

		String sessionName = (String) Environment.get("SESSION_COOKIE");
		if (sessionName == null)
			sessionName = "";

		Cookie[] cookies = request.getCookies();

		try {

			HttpSession hs = request.getSession(true);

			if (doCheckUser) {
				userName = (String) hs.getValue("USERNAME");

				if (request.isRequestedSessionIdFromURL() == true) {
					// expire all cookies and create new one with session id derived from URL...
					for (int i = 0; i < cookies.length; i++) {
						if (sessionName.equals(cookies[i].getName())) {
							cookies[i].setMaxAge(0);
							cookies[i].setPath("/");
							response.addCookie(cookies[i]);
							log.debug("B4UTradeDefaultAction.execute -  Expiring cookie ID: " + cookies[i].getValue());
						}
					}

					Cookie sessionCookie = new Cookie(sessionName, hs.getId());
					sessionCookie.setMaxAge(-1);
					sessionCookie.setPath("/");
					response.addCookie(sessionCookie);
					log.debug("B4UTradeDefaultAction.execute - RequestedSessionIdFromURL User: " + userName
							+ " -  SessionID from URL");
				} else if (request.isRequestedSessionIdFromCookie() == true)
					log.debug("B4UTradeDefaultAction.execute - RequestedSessionIdFromCookie User: " + userName
							+ " -  SessionID from Cookie");
				else
					log.debug("B4UTradeDefaultAction.execute - New session [" + hs.getId() + "] created");

				// log all the cookies currently registered in the request...

				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						log.debug("B4UTradeDefaultAction.execute - Cookie[" + cookies[i].getName() + "] = "
								+ cookies[i].getValue() + " Max age = " + cookies[i].getMaxAge());
					}
				} else {
					log.debug("B4utradeDefaultAction.execute - no cookies in request!");
				}

				log.debug("B4UTradeDefaultAction.execute - User: " + userName + " Session ID = " + hs.getId()
						+ " IP Address = " + request.getRemoteAddr() + " Class = " + this.getClass().getName());

				if (userName == null) {
					log.debug(
							"B4UTradeDefaultAction.execute - Username is null...Session has expired, forwarding to login page");
					request.setAttribute(B4UTradeWebRuleHandler.getReLoginMsg(), "");
					return (mapping.findForward(LOGIN));
				}

			}

			if (doCheckReferalPartner) {
				String referalPartner = (String) hs.getValue("REFERALPARTNER");
				if (referalPartner != null) {

					/*
					 * if ((referalPartner.equalsIgnoreCase("forbes")) ||
					 * (referalPartner.equalsIgnoreCase("brokerageamerica"))) { log.debug("User: " +
					 * userName + " referalPartner: " + referalPartner +
					 * " does not have permission to access this page"); return
					 * (mapping.findForward(HomePageFinder.findHomePage())); }
					 */

				}
			}

		} catch (IllegalStateException ise) {
			log.debug("B4UTradeDefaultAction.execute - Session has expired.", ise);
			return (mapping.findForward(LOGIN));
		}

		return null;

	}

	/**
	 * Checks if the given String is null or blank.
	 *
	 * @return true, if String is null or blank.
	 */
	protected boolean isSpace(String inString) {
		if (inString != null) {
			return (inString.trim().length() == 0);
		} else
			return true;
	}

	/**
	 * Helper method for adding an error to the current errors list. This method
	 * appends the passed error to the current list instead of blowing it all away.
	 * This way error messages can be added from different functions without having
	 * to worry that previous errors will be overwritten.
	 *
	 * @param request  the HTTPServletRequest to add the error to
	 * @param property the property to show the error for (or GLOBAL to show all
	 *                 errors)
	 * @param error    the ActionError to add to the list of errors
	 */
	protected void appendError(HttpServletRequest request, String property, ActionError error) {
		ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY); // Globals in parent class

		if (errors == null) {
			errors = new ActionErrors();
		}

		errors.add(property, error);
		saveErrors(request, errors);
	}

	/**
	 * Helper method for adding a message to the current messages list. This method
	 * appends the passed message to the current list instead of blowing it all
	 * away. This way messages can be added from different functions without having
	 * to worry that previous messages will be overwritten.
	 *
	 * @param request  the HTTPServletRequest to add the error to
	 * @param property the property to show the message for (or GLOBAL to show all
	 *                 messages)
	 * @param msg      the ActionMessage to add to the list of messages
	 */
	protected void appendMessage(HttpServletRequest request, String property, ActionMessage msg) {
		ActionMessages msgs = (ActionMessages) request.getAttribute(Globals.MESSAGE_KEY); // Globals in parent class

		if (msgs == null) {
			msgs = new ActionMessages();
		}

		msgs.add(property, msg);
		saveMessages(request, msgs);
	}

}
