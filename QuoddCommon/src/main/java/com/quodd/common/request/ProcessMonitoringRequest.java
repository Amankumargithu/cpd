package com.quodd.common.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.Constants;
import com.quodd.common.util.QuoddProperty;
import com.quodd.exception.QuoddException;

public class ProcessMonitoringRequest {

	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private static Gson gson = new Gson();
	private static QuoddProperty processMonitorProperties = new QuoddProperty("/monitoring.properties");

	private String serverName;
	private final String processMonitoringUrl;
	private final int maxTries;
	private final int timeOut;
	private final long retrySleepInterval;

	public ProcessMonitoringRequest() throws QuoddException {
		try {
			this.serverName = processMonitorProperties.getStringProperty("SERVER_NAME",
					InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			this.serverName = "UNKNOWN";
			logger.log(Level.WARNING, "SERVER NAME IS SET AS UNKNOWN " + e.getMessage(), e);
		}
		this.maxTries = processMonitorProperties.getIntProperty("MAX_RETRY_COUNT", 3);
		this.processMonitoringUrl = processMonitorProperties.getStringProperty("PROCESS_MONITORING_URL", null);
		if (this.processMonitoringUrl == null) {
			throw new QuoddException("PROCESS_MONITORING_URL cannot be null");
		}
		this.timeOut = processMonitorProperties.getIntProperty("TIME_OUT", 5_000);
		this.retrySleepInterval = processMonitorProperties.getIntProperty("RETRY_SLEEP_INTERVAL", 5_000);
	}

	public void insertProcessDetail(String processName, String processId, String groupId) {
		fireProcessStatus(processName, processId, Constants.PROCESS_STATUS_RUNNING, 0, null, null, groupId);
	}

	public void updateProcessDetail(String processId, int status, int recordCount, String reason, String comment,
			String groupId) {
		fireProcessStatus(null, processId, status, recordCount, reason, comment, groupId);
	}

	private boolean fireProcessStatus(String processName, String processId, int status, int count, String reason,
			String comment, String groupId) {
		boolean result = false;
		try {
			Map<String, Object> bodyMap = new HashMap<>();
			String requestUrl;
			if (status == Constants.PROCESS_STATUS_RUNNING) {
				requestUrl = this.processMonitoringUrl + "/insert";
				bodyMap.put(Constants.PROCESS_NAME, processName);
				bodyMap.put(Constants.COMMENT, comment);
				bodyMap.put(Constants.PROCESS_ID, processId);
				bodyMap.put(Constants.GROUP_ID, groupId);
				bodyMap.put(Constants.RECORD_COUNT, count);
				bodyMap.put(Constants.PROCESS_STATE, status);
				bodyMap.put(Constants.SERVER, this.serverName);
			} else {
				requestUrl = this.processMonitoringUrl + "/update";
				bodyMap.put(Constants.FAILURE_REASON, reason);
				bodyMap.put(Constants.COMMENT, comment);
				bodyMap.put(Constants.PROCESS_ID, processId);
				bodyMap.put(Constants.RECORD_COUNT, count);
				bodyMap.put(Constants.PROCESS_STATE, status);
				bodyMap.put(Constants.GROUP_ID, groupId);
				bodyMap.put(Constants.SERVER, this.serverName);
			}
			int numTries = 0;
			boolean flag = false;
			do {
				flag = sendPostRequest(bodyMap, requestUrl);
				numTries++;
				Thread.sleep(this.retrySleepInterval);
			} while (!flag && numTries < this.maxTries);
			return flag;
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

	private boolean sendPostRequest(Map<String, Object> bodyMap, String requestUrl) {
		String jsonInputString = gson.toJson(bodyMap);
		URL url;
		try {
			url = new URL(requestUrl);
			logger.info("Connecting to url: " + requestUrl + " body: " + jsonInputString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", Constants.REQUEST_TYPE_JSON);
			con.setDoOutput(true);
			con.setConnectTimeout(this.timeOut);
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes(Constants.UTF8);
				os.write(input, 0, input.length);
			}
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.info("Response url: " + requestUrl + " code: " + responseCode);
				return false;
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), Constants.UTF8))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				logger.info(() -> response.toString());
			}
			con.disconnect();
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
		return true;
	}
}
