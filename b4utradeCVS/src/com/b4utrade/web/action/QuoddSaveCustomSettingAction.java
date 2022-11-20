package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
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

import com.b4utrade.bean.QuoddUserOperationStatusBean;
import com.tacpoint.util.Environment;

public class QuoddSaveCustomSettingAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddSaveCustomSettingAction.class);

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
			QuoddUserOperationStatusBean statusbean = new QuoddUserOperationStatusBean();
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
			if (userId <= 0) {
				log.info("QuoddSaveCustomSettingAction.execute() : user name or password from session is null for user="
						+ userId);
				statusbean.setOperationFailed();
			} else {
				InputStream inputStream = request.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(inputStream);
				int bytesRead = 0;
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((bytesRead = bis.read(buffer)) != -1) {
					baos.write(buffer, 0, bytesRead);
				}
				String dataInputObj = null;
				String input = new String(baos.toByteArray());
				if (response != null) {
					String[] params = input.split("&");
					for (int i = 0; i < params.length; i++) {
						String parameter = params[i];
						String[] pair = parameter.split("=");
						if (pair.length == 2) {
							if (pair[0].equals("USER_CUSTOM_SETTING"))
								dataInputObj = pair[1];
						}
					}
				}
				inputStream.close();
				bis.close();
				baos.close();
				if (dataInputObj == null) {
					log.error(
							"QuoddSaveCustomSettingAction.execute: value from parameter - USER_CUSTOM_SETTING - is empty or null.");
					return null;
				}
				dataInputObj = URLDecoder.decode(dataInputObj, "UTF-8");
				if (isSpace(dataInputObj)) {
					log.error(
							"QuoddSaveCustomSettingAction.execute: value from parameter - USER_CUSTOM_SETTING - is empty or space.");
					return null;
				}
				String s3Uploadstatus = saveLayoutToS3(dataInputObj, userIdStr);
				if (s3Uploadstatus != null && s3Uploadstatus.toLowerCase().contains("success")) {
					log.info("QuoddSaveCustomSettingAction.execute(): Layout Saved Successfully to S3 of user : "
							+ userIdStr);
					statusbean.setOperationSuccessful();
				} else {
					log.info("QuoddSaveCustomSettingAction.execute(): Layout not saved Successfully to S3 of user : "
							+ userIdStr + " Response from lambda : " + s3Uploadstatus);
					statusbean.setOperationFailed();
				}
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(statusbean);
			}
			byte[] resObject = byteArray.toString().getBytes();
			byteArray.close();
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(resObject);
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddSaveCustomSettingAction.execute() : encountered exception from servlet output stream.",
						e);
			}
		} catch (Exception e) {
			String msg = "QuoddSaveCustomSettingAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
			return null;
		}
		return null;
	}

	private String saveLayoutToS3(String layout, String userId) {
		log.info("QuoddSaveCustomSettingAction.saveLayoutToS3() saving layout to s3 of user : " + userId);
		String response = null;
		if (userId != null) {
			try {
				URL url = new URL(Environment.get("UPLOAD_EQPLUS_LAYOUT_URL"));
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "application/json");
				urlConnection.setRequestProperty("Accept-Encoding", "gzip");
				urlConnection.addRequestProperty("Authorization", Environment.get("BASIC_AUTH"));
				urlConnection.addRequestProperty("userid", userId);
				try (OutputStream os = new DataOutputStream(urlConnection.getOutputStream());) {
					os.write(gzipPayload(layout).getBytes());
					os.flush();
				} catch (Exception e) {
					log.error(
							"QuoddSaveCustomSettingAction.saveLayoutToS3(): Error while writing layout to output stream",
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
					log.error("QuoddSaveCustomSettingAction.saveLayoutToS3(): Error while reading input stream", e);
				}
				response = sb.toString();
				urlConnection.disconnect();
				if (response.toLowerCase().contains("success"))
					log.info(
							"QuoddSaveCustomSettingAction.saveLayoutToS3() : Successfully saved Layout to S3 of user : "
									+ userId);
				else
					throw new Exception("Response from Lambda : " + response);
			} catch (Exception e) {
				log.error("QuoddSaveCustomSettingAction.saveLayoutToS3(): Error while saving layout to S3 for user : "
						+ userId, e);
			}
		}
		return response;
	}

	private String gzipPayload(String body) {
		log.info("QuoddSaveCustomSettingAction.gzipPayload() : layout size : " + body.getBytes().length / 1024 + "KB");
		String result = null;
		byte[] data = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			GZIPOutputStream gzip = new GZIPOutputStream(baos);
			gzip.write(body.getBytes());
			gzip.close();
			data = baos.toByteArray();
		} catch (Exception e) {
			log.error("QuoddSaveCustomSettingAction.gzipPayload() : Exception While Gzipping layout ", e);
		}
		if (data == null || data.length == 0)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (Byte b : data) {
			sb.append(b + ",");
		}
		sb.append("]");
		result = sb.toString();
		log.info("QuoddSaveCustomSettingAction.gzipPayload() : zipped layout size : " + result.getBytes().length / 1024
				+ "KB");
		return result;
	}
}
