package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.UserLoginStatusBean;
import com.b4utrade.bean.UserStreamerManagementBean;
import com.b4utrade.cache.TickerListManager;
import com.b4utrade.helper.RTDSessionValidator;
import com.google.gson.Gson;

public class RTDSubscribeTickerAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(RTDSubscribeTickerAction.class);
	private static final TickerListManager manager = TickerListManager.getInstance();
	private static final Gson gson = new Gson();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		UserStreamerManagementBean bean = null;
		UserLoginStatusBean ulsb = null;
		int userID = 0;
		try {
			response.setContentType("text/html");
			String action = request.getParameter("ACTION");
			String userIDAsString = request.getHeader("USERID");
			String versionID = request.getHeader("VERSION");
			log.info("Version: " + versionID + " userId: " + userID + " action: " + action);
			Cookie[] cookies = request.getCookies();
			ulsb = RTDSessionValidator.handleCheckUserSession(cookies);
			if (ulsb.getStatus() == 1) {
				userID = Integer.parseInt(userIDAsString);
				if (action.equals("INFO")) {
					bean = manager.getUserStreamingBean(userID);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (bean == null) {
			bean = new UserStreamerManagementBean();
			log.info("UserStreamerManagementBean is empty for userId : " + userID);
		}
		String output = gson.toJson(bean);
		try (PrintWriter sos = response.getWriter();) {
			sos.write(output);
			sos.flush();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
}
