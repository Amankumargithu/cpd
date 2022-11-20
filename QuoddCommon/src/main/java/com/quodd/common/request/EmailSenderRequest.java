package com.quodd.common.request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.Constants;
import com.quodd.common.util.QuoddProperty;
import com.quodd.exception.QuoddException;

public class EmailSenderRequest {

	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private static final QuoddProperty emailProperties = new QuoddProperty("/email.properties");
	private static final Gson gson = new Gson();

	public final String emailUrl;
	private final int timeOut;
	public List<String> listTo = new ArrayList<>();
	public List<String> listCc = new ArrayList<>();

	public EmailSenderRequest() throws QuoddException {
		this.emailUrl = emailProperties.getStringProperty("EMAIL_URL", null);
		String emailTo = emailProperties.getStringProperty("EMAIL_TO", null);
		String emailCc = emailProperties.getStringProperty("EMAIL_CC", null);
		this.timeOut = emailProperties.getIntProperty("TIME_OUT", 5_000);
		if (emailTo != null)
			this.listTo = Arrays.asList(emailTo.split(","));
		if (emailCc != null) {
			this.listCc = Arrays.asList(emailCc.split(","));
		}
		if (this.emailUrl == null) {
			throw new QuoddException("EMAIL_URL cannot be null");
		}
		if (emailTo == null) {
			throw new QuoddException("EMAIL_TO cannot be null");
		}
	}

	public boolean sendEmail(String body, String subject) {
		boolean result = false;
		try {
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("body", body);
			bodyMap.put("to", this.listTo);
			bodyMap.put("subject", subject);
			bodyMap.put("cc", this.listCc);
			String jsonInputString = gson.toJson(bodyMap);
			logger.info("Connecting to url: " + this.emailUrl + " body: " + jsonInputString);
			URL url = new URL(this.emailUrl);
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
			if (con.getResponseCode() != 200) {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getErrorStream(), Constants.UTF8))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					logger.warning("Sub: " + subject + " Body: " + body + " responseCode: " + con.getResponseCode()
							+ " response: " + response.toString());
					result = false;
				}
			} else {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), Constants.UTF8))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					logger.info("Email Sub: " + subject + " Body: " + body + " response: " + response.toString());
				}
				result = true;
			}
			con.disconnect();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}
}
