package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Enumeration;

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

import com.b4utrade.bean.UserLoginStatusBean;
import com.b4utrade.cache.RTDUserSessionManager;
import com.b4utrade.helper.SingleLoginHelper;

/**
 * The Action class logs off a user.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2003. All rights
 *         reserved.
 * @version 1.0
 */

public class QuoddHandleLogoutAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddHandleLogoutAction.class);

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

		log.info("QuoddHandleLogoutAction.execute() : Start Logout Process");
		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		UserLoginStatusBean ulsb = new UserLoginStatusBean();
		try {
			HttpSession hs = request.getSession(false);
			log.info(new Timestamp(System.currentTimeMillis()) + " session is : " + hs);
			response.setContentType("text/html");
			userName = (String) request.getParameter(UserLoginStatusBean.USERNAME_KEY);
			log.info("QuoddHandleLogoutAction.execute - User name = " + userName);
			String userIDfromWebStart = (String) request.getParameter(UserLoginStatusBean.USER_ID_KEY);
			log.info("QuoddHandleLogoutAction.execute - user id from web start = " + userIDfromWebStart);
			String userIDfromServerSession = (String) hs.getValue("USERID");
			SingleLoginHelper helper = (SingleLoginHelper) hs.getValue("SLH");
			String userSessionId = null;
			if (helper != null)
				userSessionId = helper.getSessionID();
			log.info("QuoddHandleLogoutAction.execute - user id from server session= " + userIDfromServerSession
					+ " session id is : " + userSessionId);
			if (userSessionId == null) {
				ulsb.setStatusLogoutFailed();
			} else {

				String action = request.getParameter(UserLoginStatusBean.ACTION_KEY);
				if (action.equals(UserLoginStatusBean.ACTION_LOGOUT)) {
					log.info(
							"QuoddHandleLogoutAction.handleExpireSession - Start invalidating User name : " + userName);
					if (hs != null) {
						try {
							Enumeration<String> attributes = hs.getAttributeNames();
							while (attributes.hasMoreElements()) {
								String attribute = attributes.nextElement();
								hs.removeAttribute(attribute);
							}
						} catch (Exception e) {
						}
						hs.invalidate();
					}
					log.info("QuoddHandleLogoutAction.handleExpireSession - Start deleting User from seesion : "
							+ userName);
					// force the delete...
					if ((userIDfromServerSession != null) && (userIDfromServerSession.length() > 0)) {
						SingleLoginHelper sh = new SingleLoginHelper(Integer.parseInt(userIDfromServerSession));
						// SingleLoginHelper sh = (SingleLoginHelper) hs.getAttribute("SLH");
						sh.setSessionID(userSessionId);
						sh.deleteUser();

						ulsb.setStatusLogoutSuccess();
						RTDUserSessionManager.getInstance()
								.deleteUserSession(Integer.parseInt(userIDfromServerSession));
					} else {
						ulsb.setStatusLogoutFailed();
					}
				} else {
					ulsb.setStatusLogoutFailed();
				}
			}
			log.info("QuoddHandleLogoutAction.execute() : End Logout Process");
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(ulsb);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddHandleLogoutAction encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "System error occurred while trying to log off.";
			log.error(msg, e);
		}
		return null;
	}
}
