package com.b4utrade.helper;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Environment;

public class CompanyReorgnizationHelper {
	static Log log = LogFactory.getLog(CompanyReorgnizationHelper.class);
	private static volatile CompanyReorgnizationHelper instance = new CompanyReorgnizationHelper();
	private LinkedHashMap<String, String> reorganisedTickerMap = new LinkedHashMap<>();

	private CompanyReorgnizationHelper() {
		log.info("CompanyReorgnisationHelper's instance created");
	}

	public static CompanyReorgnizationHelper getInstance() {
		return instance;
	}

	public LinkedHashMap<String, String> getCompanyReorganiztionDataMap() throws BusinessException {
		if (reorganisedTickerMap.isEmpty()) {
			String result = null;
			try {
				String reorganizationUrl = Environment.get("TICKER_REORGANIZATION_URL");
				URL url = new URL(reorganizationUrl);
				log.info("Requesting " + url);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				int responseCode = urlConnection.getResponseCode();
				if (responseCode != 200) {
					log.info(responseCode + " - " + url);
				}
				try (InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());) {
					int numCharsRead;
					char[] charArray = new char[1024];
					StringBuilder sb = new StringBuilder();
					while ((numCharsRead = isr.read(charArray)) > 0) {
						sb.append(charArray, 0, numCharsRead);
					}
					result = sb.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Gson gson = new Gson();
			Map<String, String> map = gson.fromJson(result, Map.class);
			reorganisedTickerMap.putAll(map);
			log.info("Picked results from DB");
		} else {
			log.info("Picked results from ticker reorganisation cache");
		}
		return reorganisedTickerMap;
	}
}
