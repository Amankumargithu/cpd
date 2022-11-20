package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;
import com.tacpoint.util.Environment;

public class ClearFundamentalCacheAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(ClearFundamentalCacheAction.class);

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
		boolean cacheCleared = false;
		boolean flag = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			response.setContentType("text/html");
			String tickers = (String) request.getParameter("UPCLOSETICKER");
			if (tickers != null) {
				flag = true;
			}

			// adding mongo cache clearing api (new FDC call)

			Environment.init();
			try {
				String updatedURL = Environment.get("MONGO_CLEAR_CACHE_URL");
				if (updatedURL != null) {
					if (flag) {
						if (!tickers.equals("OTCCACHE")) {
							updatedURL = updatedURL + "/reload/ticker";
							sendPostRequest(tickers, updatedURL);
						}
					} else {
						updatedURL = updatedURL + "/clear";
						sendGetRequest(updatedURL);
					}
					log.info(" Executing Fundamental Clear Cache Mongo FDC- " + updatedURL);
				} else {
					log.error("MONGO_CLEAR_CACHE_URL is null");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			String result = "Clear Cache Failed";
			if (cacheCleared == true) {
				result = "Cleared Cache Succesfully";
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArray);
			encoder.writeObject(result);
			encoder.close();
			try (ServletOutputStream sos = response.getOutputStream()) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("ClearFundamentalCacheAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "ClearFundamentalCacheAction.execute() : Unable to clear cache.";
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
		}
		return null;
	}

	private boolean sendPostRequest(String bodyString, String requestUrl) {
		String jsonInputString = bodyString;
		URL url;
		try {
			url = new URL(requestUrl);
			log.info("Connecting to url: " + requestUrl + " body: " + jsonInputString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setConnectTimeout(5_000);
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				log.info("Response url: " + requestUrl + " code: " + responseCode);
				return false;
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				log.info(response.toString());
			}
			con.disconnect();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean sendGetRequest(String requestUrl) {
		URL url;
		try {
			url = new URL(requestUrl);
			log.info("Connecting to url: " + requestUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				log.info("Response url: " + requestUrl + " code: " + responseCode);
				return false;
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				log.info(response.toString());
			}
			con.disconnect();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
}
