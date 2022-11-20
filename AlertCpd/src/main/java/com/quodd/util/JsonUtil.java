package com.quodd.util;

import java.util.HashMap;

import com.google.gson.Gson;

public class JsonUtil {
	private static Gson gson = new Gson();
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAILURE = "FAILURE";
	public static final int HTTP_RESPONSE_BAD_REQUEST = 400;

	public static String statusToJson(String status, String errorMsg) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", status);
		map.put("error", errorMsg);
		return gson.toJson(map);
	}

	public static String statusToJson(String status, String errorMsg, Object... data) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", status);
		map.put("error", errorMsg);
		int size = data.length;
		HashMap<Object, Object> result = new HashMap<>();
		for (int i = 0; i < size; i += 2)
			result.put(data[i], data[i + 1]);
		map.put("data", result);
		return gson.toJson(map);
	}
}
