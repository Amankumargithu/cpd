package com.quodd.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.quodd.common.logger.QuoddLogger;

public class StatExecutor implements Runnable {
	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private static final Gson gson = new Gson();

	private final String serverName;
	private final String clientName;
	private final String filename;
	private final String sftpHostName;
	private final String statUrl;
	private final String error;

	public StatExecutor(String serverName, String clientName, String fileName, String hostName, String statUrl,
			String msg) {
		this.serverName = serverName;
		this.clientName = clientName;
		this.filename = fileName;
		this.sftpHostName = hostName;
		this.statUrl = statUrl;
		this.error = msg;
	}

	@Override
	public void run() {
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("server_name", this.serverName);
		requestMap.put("client_name", this.clientName);
		requestMap.put("file_name", this.filename);
		requestMap.put("host_name", this.sftpHostName);
		requestMap.put("error_message", this.error);
		URL url;
		try {
			url = new URL(this.statUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());) {
				wr.writeBytes(gson.toJson(requestMap));
				wr.flush();
			}
			int responseCode = con.getResponseCode();
			logger.info(() -> "ResponseCode: " + this.statUrl + " " + responseCode);
			con.disconnect();
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

	}

}
