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

public class PinkSheetTickerDataHelper {
	private static Log log = LogFactory.getLog(PinkSheetTickerDataHelper.class);
	private static volatile PinkSheetTickerDataHelper instance = new PinkSheetTickerDataHelper();
	private static final Gson gson = new Gson();
	private static final Type type = new TypeToken<ArrayList<String>>() {
	}.getType();
	private String tickerManagerDomain;

	private PinkSheetTickerDataHelper() {
		log.info("PinkSheetTickerDataHelper's instance created");
		try {
			tickerManagerDomain = Environment.get("TICKER_MANAGER_DOMAIN");
			if (tickerManagerDomain == null) {
				log.error("PinkSheetTickerDataHelper.PinkSheetTickerDataHelper() : TICKER_MANAGER_DOMAIN is null.");
			}
		} catch (Exception e) {
			log.error("PinkSheetTickerDataHelper.PinkSheetTickerDataHelper() ", e);
		}
	}

	public static PinkSheetTickerDataHelper getInstance() {
		return instance;
	}

	public List<String> getPinkSheetTickerList() {
		ArrayList<String> resultList = new ArrayList<>();
		String urlStr = tickerManagerDomain + "/uniquepink/list";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				log.error("PinkSheetTickerDataHelper.getPinkSheetTickerList() : Unable to fetch list");
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				String responseStr = br.readLine();
				resultList = gson.fromJson(responseStr, type);
			}
			urlConnection.disconnect();
		} catch (Exception e) {
			log.error("PinkSheetTickerDataHelper.getPinkSheetTickerList() " + urlStr, e);
		}
		return resultList;
	}
}
