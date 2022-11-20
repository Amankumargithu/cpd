package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.QuoddUserEntitlementBean;
import com.b4utrade.cache.EntitlementManager;
import com.b4utrade.cache.UserIdCache;
import com.google.gson.Gson;

public class QuoddCheckNonProUserAction extends B4UTradeDefaultAction {

	private static final Gson gson = new Gson();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
		response.addHeader("Access-Control-Allow-Headers", "*");
		String userName = request.getParameter("userName");
		HashMap<String, String> resultMap = new HashMap<>();
		resultMap.put("STATUS", "FALSE");
		try {
			int userID = 0;
//			UserBO user = new UserBO();
//			user.setUserName(userName);
//			user.selectUserByName();

			String userIdAsString = UserIdCache.getInstance().getQss4IdUsingUsername(userName);
			if (userIdAsString == null) {
				log.warn("QuoddCheckNonProUserAction : UserId Not in Cache is null for userName = " + userName);
				userID = 0;
			} else {
				log.info("QuoddCheckNonProUserAction : UserId found in Cache is " + userID + " for userName = "
						+ userName);
				userID = Integer.parseInt(userIdAsString);
			}

			if (userID == 0)
				resultMap.put("ERROR", "User does not exists");
			else {
				EntitlementManager manager = EntitlementManager.getInstance();
				QuoddUserEntitlementBean bean = manager.get(userID);
				if (bean == null)
					resultMap.put("ERROR", "Entitlement does not exists");
				else {
					if (manager.isNonProAgreementEntitled(userID))
						resultMap.put("STATUS", "TRUE");
					else
						resultMap.put("ERROR", "User not entitled for Non Pro Entitlements");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("ERROR", "User does not exists");
		}
		try (PrintWriter sos = response.getWriter();) {
			sos.write(gson.toJson(resultMap));
			sos.flush();
		} catch (Exception e) {
			log.error("QuoddCheckNonProUserAction.execute() : servlet output stream encountered exception. ", e);
		}
		return null;
	}
}
