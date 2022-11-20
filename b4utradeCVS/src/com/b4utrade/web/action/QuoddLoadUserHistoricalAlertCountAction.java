package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddLoadUserHistoricalAlertCountAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadUserHistoricalAlertCountAction.class);
	private static final String alertCpdUrl = Environment.get("ALERT_CPD_URL");

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

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			response.setContentType("text/xml");
			request.getSession(false);
			Cookie[] cookies = request.getCookies();
			String userIdStr = "0";
			for (Cookie cook : cookies) {
				if ("USER_ID".equals(cook.getName()))
					userIdStr = cook.getValue();
			}
			long userId = 0;
			try {
				userId = Long.parseLong(userIdStr);
			} catch (Exception e) {
				userId = 0;
			}
			Vector historicalAlertCount = getHistoricalAlertCount(userId);
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(historicalAlertCount);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLoadUserHistoricalAlertCountAction.execute(): exception from servlet output stream.",
						e);
			}
		} catch (Exception e) {
			String msg = "QuoddLoadUserHistoricalAlertCountAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	public static Vector<Integer> getHistoricalAlertCount(long userId) {
		Vector<Integer> detailBeanVector = new Vector<>();
		try {
			String urlString = alertCpdUrl + "/historical/count?user_id=" + userId;
			Logger.log("QuoddLoadUserHistoricalAlertCountAction: requesting " + urlString);
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("QuoddLoadUserHistoricalAlertCountAction: Unable to get alert list for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			int count = Integer.parseInt(jsonString);
			detailBeanVector.add(count);
		} catch (Exception e) {
			Logger.log("QuoddLoadUserHistoricalAlertCountAction -exception in api Call " + e.getMessage(), e);
		}
		return detailBeanVector;
	}
}
