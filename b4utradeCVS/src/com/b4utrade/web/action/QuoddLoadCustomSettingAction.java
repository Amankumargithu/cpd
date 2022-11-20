package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

import com.google.gson.Gson;
import com.tacpoint.util.Environment;

public class QuoddLoadCustomSettingAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadCustomSettingAction.class);
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
			response.setContentType("text/html");
			request.getSession(false);
			Cookie[] cookies = request.getCookies();
			String userIdStr = "0";
			for (Cookie cook : cookies) {
				if ("USER_ID".equals(cook.getName()))
					userIdStr = cook.getValue();
			}
			int userId = 0;
			try {
				userId = Integer.parseInt(userIdStr);
			} catch (Exception e) {
				userId = 0;
			}
			byte csBytes[];
			String s3Layout = ungzipPayload(fetchLayoutFromS3(userIdStr));
			if (s3Layout == null || s3Layout.isEmpty()) {
				log.info("QuoddLoadCustomSettingAction.execute() : layout retrieved from s3 is null for user id : "
						+ userId);
				csBytes = "".getBytes();
			} else {
				log.info(
						"QuoddLoadCustomSettingAction.execute() : fetched layout from s3 successfully for oracle user id : "
								+ userId);
				csBytes = s3Layout.getBytes();
			}
			String encoding = request.getHeader("Accept-Encoding");
			// can the client handle gzip compression
			if (encoding != null && encoding.indexOf("gzip") >= 0) {
				log.info("QuoddLoadCustomSettingAction.execute() : Compressing layout XML of user : " + userId);
				response.setHeader("Content-Encoding", "gzip");
				try (OutputStream os = response.getOutputStream(); GZIPOutputStream gz = new GZIPOutputStream(os);) {
					gz.write(csBytes);
					gz.finish();
				} catch (Exception ex) {
					log.error("QuoddLoadCustomSettingAction.execute() : Unable to write layout to output of user : "
							+ userId, ex);
				}
			} else {
				log.info("QuoddLoadCustomSettingAction.execute() : No compression being used for layout XML user : "
						+ userId);
				try (ServletOutputStream sos = response.getOutputStream();) {
					sos.write(csBytes);
					sos.flush();
				} catch (Exception e) {
					log.error(
							"QuoddLoadCustomSettingAction.execute() : encountered exception from servlet output stream.",
							e);
				}
			}
		} catch (Exception e) {
			String msg = "QuoddLoadCustomSettingAction.execute() : Unable to retrieve custom setting data.";
			log.error(msg, e);
		}
		return null;
	}

	private String ungzipPayload(String body) {
		log.info("QuoddLoadCustomSettingAction.ungzipPayload() : going to unzip layout");
		String result = null;
		if (body != null) {
			String data = body.substring(1, body.length() - 2);
			String[] dataArr = data.split(",");
			byte[] byteArr = new byte[dataArr.length];
			int index = 0;
			for (String value : dataArr) {
				byteArr[index] = Byte.valueOf(value);
				index++;
			}
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ByteArrayInputStream bais = new ByteArrayInputStream(byteArr);
					GZIPInputStream gis = new GZIPInputStream(bais)) {
				byte[] buffer = new byte[2048];
				int len = 0;
				while ((len = gis.read(buffer)) >= 0) {
					baos.write(buffer, 0, len);
				}
				result = baos.toString();
			} catch (Exception e) {
				log.error("QuoddLoadCustomSettingAction.ungzipPayload() : Exception While ungzipping layout", e);
			}
		}
		log.info("QuoddLoadCustomSettingAction.ungzipPayload() : layout unzipping done size : "
				+ ((result == null) ? 0 : result.getBytes().length / 1024) + "KB");
		return result;
	}

	private String fetchLayoutFromS3(String userId) {
		log.info("QuoddLoadCustomSettingAction.fetchLayoutFromS3() : going to fetch layout from s3 for user id : "
				+ userId);
		String layout = null;
		String response = null;
		if (userId != null) {
			try {
				URL url = new URL(Environment.get("DOWNLOAD_EQPLUS_LAYOUT_URL"));
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setRequestProperty("Content-Type", "application/json");
				urlConnection.setRequestProperty("Accept-Encoding", "gzip");
				urlConnection.addRequestProperty("Authorization", Environment.get("BASIC_AUTH"));
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
					log.error(
							"QuoddLoadCustomSettingAction.fetchLayoutFromS3(): Error while reading input / error stream",
							e);
				}
				response = sb.toString();
				urlConnection.disconnect();
				if (response.toLowerCase().contains("success"))
					log.info(
							"QuoddLoadCustomSettingAction.fetchLayoutFromS3() : Successfully Fetched Layout from S3 of user : "
									+ userId);
				else
					log.error("Error while Downloading Layout from S3 ,Response From Lambda : " + response);
				HashMap<String, String> map = gson.fromJson(response, HashMap.class);
				layout = map.get("userLayout");
			} catch (Exception e) {
				log.error(
						"QuoddLoadCustomSettingAction.fetchLayoutFromS3(): Error while downloading eqplus layout from S3 for user: "
								+ userId,
						e);
			}
		}
		log.info("QuoddLoadCustomSettingAction.fetchLayoutFromS3() : zipped layout fetched size : "
				+ ((layout == null) ? 0 : layout.getBytes().length / 1024) + "KB of user id : " + userId);
		return layout;
	}
}
