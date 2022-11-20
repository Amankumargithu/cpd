package com.b4utrade.dataaccess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsCriteriaBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.common.DefaultObject;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

/**
 * This class is used to select news data.
 */
public class NewsSelector extends DefaultObject {

	static {
		try {
			Logger.init();
			Environment.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static final Gson gson = new GsonBuilder().serializeNulls().create();
	private static final Type gsonMapType = new TypeToken<Map<String, String>>() {
	}.getType();
	private static final Type gsonListType = new TypeToken<List<Map<String, String>>>() {
	}.getType();
	private static final String newsCriteriaUrl = Environment.get("NEWS_CRITERIA_URL");

	public static boolean saveDjNewsCriteria(long userId, NewsCriteriaBean criteriaBean,
			NewsCriteriaDetailBean criteriaDetailBean) {
		try {
			String urlString = newsCriteriaUrl + "/uid/" + userId + "/djnews/add";
			URL obj = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) obj.openConnection();
			urlConnection.setRequestMethod("POST");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			SimpleDateFormat uidf = new SimpleDateFormat("MM/dd/yy");
			String startDate = criteriaBean.getStartDate();
			if (startDate != null && !startDate.isEmpty()) {
				try {
					startDate = sdf.format(uidf.parse(startDate));
				} catch (Exception e) {
					startDate = criteriaBean.getStartDate();
				}
			}
			String endDate = criteriaBean.getEndDate();
			if (endDate != null && !endDate.isEmpty()) {
				try {
					endDate = sdf.format(uidf.parse(endDate));
				} catch (Exception e) {
					endDate = criteriaBean.getEndDate();
				}
			}
			JsonObject data = new JsonObject();
			data.addProperty("tickers", criteriaDetailBean.getTicker());
			data.addProperty("startDate", startDate);
			data.addProperty("endDate", endDate);
			data.addProperty("categorycodelist", criteriaDetailBean.getCategory());
			data.addProperty("operationType", criteriaDetailBean.getOperationType());
			data.addProperty("criterianame", criteriaBean.getCriteriaName());
			urlConnection.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());) {
				wr.writeBytes(data.toString());
				wr.flush();
			}
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("NewsSelector: Unable to get DJ news Criteria for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			Logger.log("Post request" + urlString + " " + responseCode + " params " + data.toString());
			if (responseCode == HttpURLConnection.HTTP_OK) {
				Map<String, String> responseMap = gson.fromJson(jsonString, gsonMapType);
				return responseMap.get("status").equalsIgnoreCase("SUCCESS");
			}
		} catch (Exception e) {
			Logger.log("NewsSelector.saveDjNewsCriteria - exception in api Call " + e.getMessage(), e);
		}
		return false;
	}

	public static Vector<NewsCriteriaBean> getDJNewsCriteria(long userId) {
		Vector<NewsCriteriaBean> vector = new Vector<>();
		try {
			String urlString = newsCriteriaUrl + "/uid/" + userId + "/djnews/list";
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("NewsSelector: Unable to get DJ news Criteria for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			List<Map<String, String>> dataList = gson.fromJson(jsonString, gsonListType);
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			SimpleDateFormat uidf = new SimpleDateFormat("MM/dd/yy");
			for (Map<String, String> dataMap : dataList) {
				NewsCriteriaBean bean = new NewsCriteriaBean();
				NewsCriteriaDetailBean ncdbean = new NewsCriteriaDetailBean();
				bean.addCategory(ncdbean);
				ncdbean.setTicker(dataMap.get("tickers"));
				ncdbean.setOperationType(dataMap.get("operationType"));
				ncdbean.setCategory(dataMap.get("categoryCodeList"));
				bean.setCriteriaName(dataMap.get("criteriaName"));
				try {
					ncdbean.setCriteriaListId(1);
					String tempString = dataMap.get("endDate");
					if (tempString != null && !tempString.isEmpty())
						bean.setEndDate(uidf.format(sdf.parse(tempString)));
					tempString = dataMap.get("startDate");
					if (tempString != null && !tempString.isEmpty())
						bean.setStartDate(uidf.format(sdf.parse(tempString)));
				} catch (Exception e) {
					Logger.log("NewsSelector.getDJNewsCriteria: error in json parsing for jsonstring: " + jsonString
							+ " Exception: " + e.getMessage(), e);
				}
				vector.add(bean);
			}
		} catch (Exception e) {
			Logger.log("NewsSelector.getDJNewsCriteria -exception in api Call " + e.getMessage(), e);
		}
		return vector;
	}

	public static boolean deleteDJNewsCriteria(long userId, NewsCriteriaBean criteriaBean) {
		try {
			String urlString = newsCriteriaUrl + "/uid/" + userId + "/djnews/delete/cname/"
					+ criteriaBean.getCriteriaName();
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("NewsSelector : Unable to delete DJ news Criteria " + criteriaBean.getCriteriaName()
						+ " for user_id: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			Map<String, String> responseMap = gson.fromJson(jsonString, gsonMapType);
			return responseMap.get("status").equalsIgnoreCase("SUCCESS");
		} catch (Exception e) {
			Logger.log("NewsSelector.deleteDJNewsCriteria - exception in api Call " + e.getMessage(), e);
		}
		return false;
	}

	public static boolean saveEdgeNewsCriteria(long userId, EdgeNewsCriteriaBean criteriaBean) {
		try {
			String urlString = newsCriteriaUrl + "/uid/" + userId + "/edgenews/add";
			URL obj = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) obj.openConnection();
			urlConnection.setRequestMethod("POST");
			JsonObject data = new JsonObject();
			data.addProperty("tickers", criteriaBean.getTickers());
			data.addProperty("startDate", criteriaBean.getStartDate());
			data.addProperty("endDate", criteriaBean.getEndDate());
			data.addProperty("categorycodelist", criteriaBean.getCategories());
			data.addProperty("operationType", criteriaBean.getOperationType() + "");
			data.addProperty("criterianame", criteriaBean.getQueryName());
			data.addProperty("Source", criteriaBean.getSources());
			urlConnection.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());) {
				wr.writeBytes(data.toString());
				wr.flush();
			}
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("NewsSelector: Unable to save Edgenews Criteria for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			Logger.log("Post request" + urlString + " " + responseCode + " params " + data.toString());
			if (responseCode == HttpURLConnection.HTTP_OK) {
				Map<String, String> responseMap = gson.fromJson(jsonString, gsonMapType);
				return responseMap.get("status").equalsIgnoreCase("SUCCESS");
			}
		} catch (Exception e) {
			Logger.log("NewsSelector.saveEdgeNewsCriteria - exception in api Call " + e.getMessage(), e);
		}
		return false;
	}

	public static Vector<EdgeNewsCriteriaBean> getEdgeNewsCriteria(long userId) {
		Vector<EdgeNewsCriteriaBean> vector = new Vector<>();
		try {
			String urlString = newsCriteriaUrl + "/uid/" + userId + "/edgenews/list";
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("NewsSelector: Unable to get EdgeNews Criteria for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			List<Map<String, String>> dataList = gson.fromJson(jsonString, gsonListType);
			for (Map<String, String> dataMap : dataList) {
				EdgeNewsCriteriaBean bean = new EdgeNewsCriteriaBean();
				bean.setTickers(dataMap.get("tickers"));
				bean.setEndDate(dataMap.get("endDate"));
				bean.setStartDate(dataMap.get("startDate"));
				bean.setCategories(dataMap.get("categoryCodeList"));
				bean.setSources(dataMap.get("source"));
				bean.setQueryName(dataMap.get("criteriaName"));
				try {
					bean.setOperationType(Integer.parseInt(dataMap.get("operationType")));
				} catch (Exception e) {
				}
				vector.add(bean);
			}
		} catch (Exception e) {
			Logger.log("NewsSelector.getEdgeNewsCriteria -exception in api Call " + e.getMessage(), e);
		}
		return vector;
	}

	public static boolean deleteEdgeNewsCriteria(long userId, EdgeNewsCriteriaBean criteriaBean) {
		try {
			String urlString = newsCriteriaUrl + "/uid/" + userId + "/edgenews/delete/cname/"
					+ criteriaBean.getQueryName();
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("NewsSelector :  Unable to delete Edge news Criteria " + criteriaBean.getQueryName()
						+ " for user_id: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			Map<String, String> responseMap = gson.fromJson(jsonString, gsonMapType);
			return responseMap.get("status").equalsIgnoreCase("SUCCESS");
		} catch (Exception e) {
			Logger.log("NewsSelector.addEdgeNewsCriteriaByUser - db exception occurred. ", e);
		}
		return false;
	}

}
