package com.b4utrade.web.action;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.QuoddUserOperationStatusBean;
import com.b4utrade.bean.UserAlertBean;
import com.b4utrade.bean.UserAlertDetailBean;
import com.google.gson.Gson;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddManageUserAlertAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddManageUserAlertAction.class);
	private static final String alertCpdUrl = Environment.get("ALERT_CPD_URL");
	private static Gson gson = new Gson();

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
			HttpSession hs = request.getSession(false);
			QuoddUserOperationStatusBean statusbean = new QuoddUserOperationStatusBean();
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
			String dataInputObj = request.getParameter("USER_ALERT");
			Object resultObject = null;
			UserAlertBean alertBean = null;
			try (ByteArrayInputStream bais = new ByteArrayInputStream(dataInputObj.getBytes());
					XMLDecoder decoder = new XMLDecoder(bais);) {
				resultObject = decoder.readObject();
				alertBean = (UserAlertBean) resultObject;
			} catch (Exception e) {
				e.printStackTrace();
				log.error("QuoddManageUserAlertAction.execute: unable to manage alert : ");
				statusbean.setOperationFailed();
			}
			if (alertBean != null) {
				if (alertBean.isSaveActionType()) {
					log.info("QuoddManageUserAlertAction.execute: Save Alert action type");
					saveAlert(userId, alertBean);
					statusbean.setOperationSuccessful();
				} else if (alertBean.isDeleteActionType()) {
					log.info("QuoddManageUserAlertAction.execute: Delete Alert action type");
					Vector alertDetailVec = alertBean.getAlertDetail();
					if (alertDetailVec != null && !alertDetailVec.isEmpty()) {
						UserAlertDetailBean aUserAlertDetailBean = (UserAlertDetailBean) alertDetailVec.elementAt(0);
						deleteAlert(userId, aUserAlertDetailBean.getAlertName());
					}
					statusbean.setOperationSuccessful();
				} else {
					log.error("QuoddManageUserAlertAction.execute: Alert action type is not available");
					statusbean.setOperationFailed();
				}
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(statusbean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddManageUserAlertAction.execute() : servlet output stream encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "Unable to retrieve data.";
			log.error("QuoddManageUserAlertAction.execute(): " + msg, e);
		}
		return null;
	}

	private void deleteAlert(long userId, String alertName) throws BusinessException {
		try {
			String urlString = alertCpdUrl + "/alerts/delete?user_id=" + userId + "&alert_name="
					+ URLEncoder.encode(alertName, "UTF-8");
			Logger.log("QuoddManageUserAlertAction: requesting " + urlString);
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("QuoddManageUserAlertAction: Unable to add alert for userId: " + userId);
			}
		} catch (Exception e) {
			Logger.log("QuoddManageUserAlertAction.deleteAlert -exception in api Call " + e.getMessage(), e);
		}
	}

	private void saveAlert(long userId, UserAlertBean aUserAlertBean) throws BusinessException {
		try {
			String urlString = alertCpdUrl + "/alerts/add?user_id=" + userId;
			Logger.log("QuoddManageUserAlertAction: requesting " + urlString);
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			if (!aUserAlertBean.getAlertDetail().isEmpty()) {
				try (OutputStream os = urlConnection.getOutputStream();) {
					os.write(gson.toJson(aUserAlertBean.getAlertDetail().firstElement()).getBytes());
					os.flush();
				} catch (Exception e) {
					Logger.log("Exception Occured while sending post data", e);
				}
				int responseCode = urlConnection.getResponseCode();
				if (responseCode != 200) {
					Logger.log("QuoddManageUserAlertAction: Unable to add alert for userId: " + userId
							+ " Response Code: " + responseCode + " : Vector Size in Bean :  "
							+ aUserAlertBean.getAlertDetail().size());
				}
			} else
				Logger.log("QuoddManageUserAlertAction : Vector is empty in bean .saveAlert()");
		} catch (Exception e) {
			Logger.log("QuoddManageUserAlertAction.saveAlert -exception in api Call " + e.getMessage(), e);
		}
	}

}