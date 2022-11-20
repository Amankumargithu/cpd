package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.QuoddUserOperationStatusBean;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddDeleteUserHistoricalAlertAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadUserHistoricalAlertAction.class);
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
			String alertIdToDelete = request.getParameter("ALERT_ID");
			QuoddUserOperationStatusBean statusbean = new QuoddUserOperationStatusBean();
			Logger.log("Deleting alertIds : " + alertIdToDelete);
			if (alertIdToDelete != null) {
				deleteHistoricalAlert(alertIdToDelete);
				statusbean.setOperationSuccessful();
			} else {
				statusbean.setOperationFailed();
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(statusbean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddDeleteUserHistoricalAlertAction.execute(): exception from servlet output stream.", e);
			}
		} catch (Exception e) {
			String msg = "QuoddDeleteUserHistoricalAlertAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private void deleteHistoricalAlert(String alertId) throws BusinessException {
		try {
			String urlString = alertCpdUrl + "/historical/delete?alert_id=" + alertId;
			Logger.log("QuoddDeleteUserHistoricalAlertAction: requesting " + urlString);
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("QuoddDeleteUserHistoricalAlertAction: Unable to add alert for userId: " + alertId);
			}
		} catch (Exception e) {
			Logger.log("QuoddDeleteUserHistoricalAlertAction.deleteHistoricalAlert -exception in api Call "
					+ e.getMessage(), e);
		}
	}

}
