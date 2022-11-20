package com.quodd.common.util;

import java.util.HashMap;
import java.util.Map;

import com.quodd.common.util.Constants;

public final class RequestUtil {

	public static final Map<String, String> successResponse = new HashMap<>();
	public static final Map<String, String> failureResponse = new HashMap<>();

	static {
		successResponse.put(Constants.KEY_STATUS, Constants.STATUS_SUCCESS);
		failureResponse.put(Constants.KEY_STATUS, Constants.STATUS_FAILURE);
	}
}
