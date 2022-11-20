package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;

public class QuoddMobileColumnLayoutAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddMobileColumnLayoutAction.class);
	private static final Gson gson = new Gson();

	private static final String SAVE_ACTION = "SAVE";
	private static final String GET_ACTION = "GET";
	private static final String DELETE_ACTION = "DELETE";
	private static final String ENV_ANDROID = "ANDROID";
	private static final String ENV_IOS = "iOS";

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			String responseObj = "failure";
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
			response.addHeader("Access-Control-Allow-Headers", "*");
			String layout = null;
			String lambdaApi = null;
			try {
				layout = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			} catch (Exception e) {
				log.error(
						"QuoddMobileColumnLayoutAction.execute(): Error while reading the input Stream (mobile column layout)",
						e);
			}
			String action = request.getParameter("ACTION");
			String env = request.getParameter("env");
			String userId = request.getParameter("userid");
			String wallId = request.getParameter("wallid");
			if (env == null || env.length() < 1 || userId == null || userId.length() < 1) {
				throw new Exception(
						"QuoddMobileColumnLayoutAction.execute(): One or two request param are null , env : " + env
								+ " UserId : " + userId);
			}
			if (!GET_ACTION.equalsIgnoreCase(action) && (wallId == null || wallId.length() < 1)) {
				throw new Exception(
						"QuoddMobileColumnLayoutAction.execute(): wallId needed in param is null , wallId : " + wallId
								+ " UserId : " + userId);
			}
			try {
				lambdaApi = loadlambdaApi(action, env);
			} catch (Exception e) {
				log.error("QuoddMobileColumnLayoutAction.execute(): Error while loading lambda api from env , userid : "
						+ userId, e);
			}
			try {
				if (SAVE_ACTION.equalsIgnoreCase(action)) {
					if (layout == null || layout.isEmpty()) {
						log.warn("QuoddMobileColumnLayoutAction.execute : column layout is null or empty in request");
						return null;
					}
					boolean status = saveLayoutToS3(lambdaApi, userId, layout, action, wallId);
					if (status)
						responseObj = "success";
					else
						responseObj = "failure";
				} else if (DELETE_ACTION.equalsIgnoreCase(action)) {
					// saving layout to s3
					boolean status = saveLayoutToS3(lambdaApi, userId, layout, action, wallId);
					if (status)
						responseObj = "success";
					else
						responseObj = "failure";
				} else if (GET_ACTION.equalsIgnoreCase(action)) {
					responseObj = fetchLayouFromS3(lambdaApi, userId);
					if (responseObj == null || responseObj.isEmpty() || responseObj.equalsIgnoreCase("{}")) {
						log.warn(
								"QuoddMobileColumnLayoutAction.execute : layout fetch from s3 is null going to fetch layout from oracle db of user : "
										+ userId);
					}
				}
			} catch (Exception e) {
				String msg = "QuoddMobileColumnLayoutAction. mobileColumnLayoutAction() : Unable to perform mobile column layout actions for user : "
						+ userId + " Error : " + e.getMessage();
				responseObj = "failure";
				log.error(msg, e);
			}
			try (ServletOutputStream sos = response.getOutputStream()) {
				sos.write(responseObj.getBytes());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddMobileColumnLayoutAction.execute(): exception while sending response.", e);
			}
		} catch (Exception e) {
			log.error("QuoddMobileColumnLayoutAction.execute ,Error while processing request.", e);
			return null;
		}
		return (null);
	}

	private boolean saveLayoutToS3(String lambdaApi, String userId, String layout, String action, String wallid) {
		if (lambdaApi == null) {
			log.info("QuoddMobileColumnLayoutAction.saveLayoutToS3() : lambdaApi is null or empty");
			return false;
		}
		String basicAuth = Environment.get("BASIC_AUTH");
		if (basicAuth == null) {
			log.info("QuoddMobileColumnLayoutAction.saveLayoutToS3() : Basic Auth fetched from env is null");
			return false;
		}
		try {
			String resultMap = null;
			URL url = new URL(lambdaApi);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip");
			urlConnection.addRequestProperty("Authorization", basicAuth);
			urlConnection.addRequestProperty("userid", userId);
			urlConnection.addRequestProperty("action", action);
			urlConnection.addRequestProperty("wallid", wallid);
			try (OutputStream os = new DataOutputStream(urlConnection.getOutputStream());) {
				os.write(layout.getBytes());
				os.flush();
			} catch (Exception e) {
				log.error(
						"QuoddMobileColumnLayoutAction.saveLayoutToS3(): Error while writing column layout to output stream",
						e);
				return false;
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
				log.error("QuoddMobileColumnLayoutAction.saveLayoutToS3(): Error while reading input / error stream",
						e);
				return false;
			}
			urlConnection.disconnect();
			resultMap = sb.toString();
			if (resultMap.toLowerCase().contains("success")) {
				log.info(
						"QuoddMobileColumnLayoutAction.saveLayoutToS3(): Successfully Uploaded column Layout to S3 of user : "
								+ userId);
				return true;
			} else {
				log.info(
						"QuoddMobileColumnLayoutAction.saveLayoutToS3(): Error while uploading column Layout to S3 of user : "
								+ userId + " Error : " + resultMap);
				return false;
			}
		} catch (Exception e) {
			log.error("QuoddMobileColumnLayoutAction.execute(): Error while saving column layout to S3 for user : "
					+ userId, e);
			return false;
		}
	}

	private String fetchLayouFromS3(String lambdaDownloadApi, String userId) {
		String s3Layout = null;
		String basicAuth = Environment.get("BASIC_AUTH");
		if (basicAuth == null) {
			log.error("QuoddMobileColumnLayoutAction.fetchLayouFromS3() : Basic Auth fetched from env is null");
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
				log.error("QuoddMobileColumnLayoutAction.fetchLayouFromS3(): Error while reading input / error stream",
						e);
			}
			urlConnection.disconnect();
			String resultObj = sb.toString();
			if (resultObj.toLowerCase().contains("success"))
				log.info(
						"QuoddMobileColumnLayoutAction.fetchLayouFromS3(): Successfully Downloaded column Layout from S3 of user : "
								+ userId);
			else
				log.error("Error while Downloading Layout from S3 ,Response From Lambda : " + resultObj);
			HashMap<String, String> resultMap = gson.fromJson(resultObj, new TypeToken<HashMap<String, String>>() {
			}.getType());
			s3Layout = resultMap.get("userLayout");
		} catch (Exception e) {
			log.error(
					"QuoddMobileColumnLayoutAction.fetchLayouFromS3(): Error while Downloading column layout to S3 for user : "
							+ userId,
					e);
		}
		return s3Layout;
	}

	private String loadlambdaApi(String action, String env) {
		String api = null;
		try {
			if (SAVE_ACTION.equalsIgnoreCase(action) || DELETE_ACTION.equalsIgnoreCase(action)) {
				if (ENV_ANDROID.equalsIgnoreCase(env)) {
					api = Environment.get("UPLOAD_ANDROID_MOBILE_COLUMN_LAYOUT_URL");
				} else if (ENV_IOS.equalsIgnoreCase(env)) {
					api = Environment.get("UPLOAD_IOS_MOBILE_COLUMN_LAYOUT_URL");
				}
			} else if (GET_ACTION.equalsIgnoreCase(action)) {
				if (ENV_ANDROID.equalsIgnoreCase(env)) {
					api = Environment.get("DOWNLOAD_ANDROID_MOBILE_COLUMN_LAYOUT_URL");
				} else if (ENV_IOS.equalsIgnoreCase(env)) {
					api = Environment.get("DOWNLOAD_IOS_MOBILE_COLUMN_LAYOUT_URL");
				}
			}
		} catch (Exception e) {
			log.error("QuoddMobileColumnLayoutAction.loadlambdaApi() : Error while loading lambda api");
			return api;
		}
		return api;
	}
}
