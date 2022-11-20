package com.b4utrade.web.action;

import java.io.IOException;

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

/**
 * The class manages session check for RTD users. Provide URL info along with
 * session Check
 */

public class RTDResubscribeTickerAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(RTDResubscribeTickerAction.class);
	private static final TickerListManager manager = TickerListManager.getInstance();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		UserLoginStatusBean ulsb = null;
		String streamType = request.getParameter("TYPE");
		String userId = request.getHeader("USERID");
		String versionID = request.getHeader("VERSION");
		log.info("Version: " + versionID + "  UserId: " + userId + " streamerType: " + streamType);
		if (userId == null || streamType == null)
			return null;
		Cookie[] cookies = request.getCookies();
		ulsb = RTDSessionValidator.handleCheckUserSession(cookies);
		if (ulsb.getStatus() == 1) {
			try {
				UserStreamerManagementBean bean = manager.getUserStreamingBean(Integer.parseInt(userId));
				if (bean == null)
					return null;
				bean.reSubscribe(streamType.toUpperCase().trim());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			response.setStatus(401);
		}
		return null;
	}
}