package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.gson.Gson;
import com.tacpoint.util.Environment;

/*
 * Loads mobile layout from S3 
 */
public class QuoddLoadMobileLayoutAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadMobileLayoutAction.class);
	private static final Gson gson = new Gson();

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
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
			response.addHeader("Access-Control-Allow-Headers", "*");
			String env = request.getParameter("env");
			String userId = request.getParameter("userid");
			if (env == null || env.length() < 1 || userId == null || userId.length() < 1) {
				throw new Exception("QuoddLoadMobileLayoutAction.execute(): One or two request param are null , env : "
						+ env + " UserId : " + userId);
			}
			String lambdaDownloadApi = null;
			try {
				if (env.equalsIgnoreCase("ANDROID")) {
					lambdaDownloadApi = Environment.get("DOWNLOAD_ANDROID_MOBILE_LAYOUT_URL");
				} else if (env.equalsIgnoreCase("iOS")) {
					lambdaDownloadApi = Environment.get("DOWNLOAD_IOS_MOBILE_LAYOUT_URL");
				}
			} catch (Exception e) {
				log.error("QuoddLoadMobileLayoutAction.execute(): Error while fetching environment variables using env",
						e);
			}
			// fetching layout from lambda
			String layout = fetchLayouFromS3(lambdaDownloadApi, userId);
			if (layout == null || layout.isEmpty()) {
				log.info("QuoddLoadMobileLayoutAction.execute(): layout from s3 is null for user : " + userId);
			}
			try (ServletOutputStream sos = response.getOutputStream()) {
				sos.write((layout == null) ? "".getBytes() : layout.getBytes());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLoadMobileLayoutAction.execute(): exception while sending response.", e);
			}
		} catch (Exception e) {
			String msg = "QuoddLoadMobileLayoutAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private String fetchLayouFromS3(String lambdaDownloadApi, String userId) {
		String s3Layout = null;
		String basicAuth = Environment.get("BASIC_AUTH");
		if (basicAuth == null) {
			log.error("QuoddLoadMobileLayoutAction.fetchLayouFromS3() : Basic Auth fetched from env is null");
			return s3Layout;
		}
		try {
			URL url = new URL(lambdaDownloadApi);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip");
			urlConnection.addRequestProperty("Authorization", basicAuth);
			urlConnection.addRequestProperty("userid", userId);
			StringBuffer sb = new StringBuffer();
			try (BufferedReader br1 = new BufferedReader(
					new InputStreamReader(urlConnection.getResponseCode() == 200 ? urlConnection.getInputStream()
							: urlConnection.getErrorStream()));) {
				String line;
				while ((line = br1.readLine()) != null) {
					sb.append(line);
				}
			} catch (Exception e) {
				log.error("QuoddLoadMobileLayoutAction.fetchLayouFromS3(): Error while reading input / error stream",
						e);
			}
			urlConnection.disconnect();
			String resultObj = sb.toString();
			if (resultObj.toLowerCase().contains("success")) {
				log.info(
						"QuoddLoadMobileLayoutAction.fetchLayouFromS3(): Successfully Downloaded Layout from S3 of user : "
								+ userId);
				HashMap<String, String> resultMap = gson.fromJson(resultObj, HashMap.class);
				s3Layout = resultMap.get("userLayout");
			} else {
				log.error("Error while Downloading Layout from S3 ,Response From Lambda : " + resultObj);
			}
		} catch (Exception e) {
			log.error(
					"QuoddLoadMobileLayoutAction.execute(): Error while Downloading layout to S3 for user : " + userId,
					e);
		}
		return s3Layout;
	}
}
