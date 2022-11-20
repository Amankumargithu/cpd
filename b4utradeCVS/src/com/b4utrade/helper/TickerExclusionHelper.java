package com.b4utrade.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;

public class TickerExclusionHelper {

	private static final Gson gson = new Gson();
	private static final Type type = new TypeToken<ArrayList<String>>() {
	}.getType();
	private static final Log log = LogFactory.getLog(TickerExclusionHelper.class);
	private String tickerManagerDomain;

	public TickerExclusionHelper() {
		try {
			tickerManagerDomain = Environment.get("TICKER_MANAGER_DOMAIN");
			if (tickerManagerDomain == null) {
				log.error("TickerExclusionHelper.TickerExclusionHelper() : TICKER_MANAGER_DOMAIN is null.");
			}
		} catch (Exception e) {
			log.error("TickerExclusionHelper.TickerExclusionHelper() ", e);
		}
	}

	public List<String> getTickerExclusionList() {
		List<String> resultList = new ArrayList<>();
		String urlStr = tickerManagerDomain + "/tickerexclusion/list";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				log.error("TickerExclusionHelper.getTickerExclusionList() : Unable to fetch list");
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				String responseStr = br.readLine();
				resultList = gson.fromJson(responseStr, type);
			}
			urlConnection.disconnect();
		} catch (Exception e) {
			log.error("TickerExclusionHelper.getTickerExclusionList() " + urlStr, e);
		}
		return resultList;
	}
}
