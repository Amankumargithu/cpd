package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Environment;

/*
 *  Save Mobile layouts to S3  
 */
public class QuoddSaveMobileLayoutAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddSaveMobileLayoutAction.class);

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

			String lambdaDownloadApi = null;
			String layout = null;
			try {
				layout = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			} catch (Exception e) {
				log.error("QuoddSaveMobileLayoutAction.execute(): Error while reading the input Stream (mobile layout)",
						e);
			}
			if (layout == null || layout.isEmpty()) {
				log.info("QuoddSaveMobileLayoutAction.execute(): layout is null");
				return null;
			}
			String env = request.getParameter("env");
			String userId = request.getParameter("userid");
			if (env == null || env.length() < 1 || userId == null || userId.length() < 1) {
				throw new Exception("QuoddSaveMobileLayoutAction.execute(): One or two request param are null , env : "
						+ env + " UserId : " + userId);
			}
			try {
				if (env.equalsIgnoreCase("ANDROID")) {
					lambdaDownloadApi = Environment.get("UPLOAD_ANDROID_MOBILE_LAYOUT_URL");
				} else if (env.equalsIgnoreCase("iOS")) {
					lambdaDownloadApi = Environment.get("UPLOAD_IOS_MOBILE_LAYOUT_URL");
				}
			} catch (Exception e) {
				log.error("QuoddSaveMobileLayoutAction.execute(): Error while fetching environment variables using env",
						e);
			}
			// saving layout to s3
			String resultMap = saveLayoutToS3(lambdaDownloadApi, userId, layout);
			try (ServletOutputStream sos = response.getOutputStream()) {
				sos.write((resultMap == null) ? "".getBytes() : resultMap.getBytes());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddSaveMobileLayoutAction.execute(): exception while sending response.", e);
			}
		} catch (Exception e) {
			String msg = "QuoddSaveMobileLayoutAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private String saveLayoutToS3(String lambdaDownloadApi, String userId, String layout) {
		String resultMap = null;
		String basicAuth = Environment.get("BASIC_AUTH");
		if (basicAuth == null) {
			log.info("QuoddSaveMobileLayoutAction.saveLayoutToS3() : Basic Auth fetched from env is null");
			return resultMap;
		}
		try {
			URL url = new URL(lambdaDownloadApi);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip");
			urlConnection.addRequestProperty("Authorization", basicAuth);
			urlConnection.addRequestProperty("userid", userId);
			try (OutputStream os = new DataOutputStream(urlConnection.getOutputStream());) {
				os.write(layout.getBytes());
				os.flush();
			} catch (Exception e) {
				log.error("QuoddSaveMobileLayoutAction.saveLayoutToS3(): Error while writing layout to output stream",
						e);
			}
			StringBuffer sb = new StringBuffer();
			try (BufferedReader br1 = new BufferedReader(
					new InputStreamReader(urlConnection.getResponseCode() == 200 ? urlConnection.getInputStream()
							: urlConnection.getErrorStream()));) {
				String line;
				while ((line = br1.readLine()) != null) {
					sb.append(line);
				}
			} catch (Exception e) {
				log.error("QuoddSaveMobileLayoutAction.saveLayoutToS3(): Error while reading input / error stream", e);
			}
			urlConnection.disconnect();
			resultMap = sb.toString();
			if (resultMap.toLowerCase().contains("success"))
				log.info("QuoddSaveMobileLayoutAction.execute(): Successfully Uploaded Layout to S3 of user : "
						+ userId);
			else
				log.info("QuoddSaveMobileLayoutAction.execute(): Error while uploading Layout to S3 of user : " + userId
						+ " Error : " + resultMap);
		} catch (Exception e) {
			log.error("QuoddSaveMobileLayoutAction.execute(): Error while saving layout to S3 for user : " + userId, e);
		}
		return resultMap;
	}
}
