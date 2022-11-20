package com.quodd.controller;

import static com.quodd.cpd.AlertCpd.historicalAlertDao;
import static spark.Spark.halt;

import com.quodd.common.util.Constants;
import com.quodd.common.util.Filters;
import com.quodd.common.util.RequestUtil;
import com.quodd.exception.QuoddException;
import com.quodd.util.AlertConstant;

import spark.Route;

public class HistoricalAlertController {

	public static final Route countHistoricalAlertsByUser = (request, response) -> {
		String userid = request.queryParams("user_id");
		if (userid == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(userid))
			throw new QuoddException("user_id cannot be empty");
		long userId = 0;
		try {
			userId = Long.parseLong(userid);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse user_id");
		}
		if (userId == 0)
			throw new QuoddException("user_id cannot be zero");
		return historicalAlertDao.getHistoricalAlertCountById(userId);
	};

	public static final Route deleteHistoricalAlert = (request, response) -> {
		String alertid = request.queryParams("alert_id");
		if (alertid == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(alertid))
			throw new QuoddException("alert_id cannot be empty");
		long alertId = 0;
		try {
			alertId = Long.parseLong(alertid);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse alert_id");
		}
		if (alertId == 0)
			throw new QuoddException("alert_id cannot be zero");
		historicalAlertDao.deleteHistoricalAlert(alertId);
		return RequestUtil.successResponse;
	};

	public static final Route listHistoricalAlert = (request, response) -> {
		String userid = request.queryParams("user_id");
		String startindex = request.queryParams("start_index");
		String endindex = request.queryParams("end_index");
		if (!Filters.checkNullableStrings(userid, startindex, endindex))
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(userid, startindex, endindex))
			throw new QuoddException("parameters cannot be empty");
		long userId = 0;
		try {
			userId = Long.parseLong(userid);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse user_id");
		}
		if (userId == 0)
			throw new QuoddException("user_id cannot be zero");
		int startIndex = 0;
		try {
			startIndex = Integer.parseInt(startindex);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse start_index");
		}
		int endIndex = 0;
		try {
			endIndex = Integer.parseInt(endindex);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse end_index");
		}
		return historicalAlertDao.gethistoricalAlertbyId(userId, startIndex, endIndex);
	};

//	public static final Route listhistoricalAlertbyAlertName = (request, response) -> {
//		String userid = request.queryParams("USERID");
//		String alertName = request.queryParams("ALERTNAME");
//		if (userid == null || alertName == null)
//			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
//		if (!Filters.checkEmptyStrings(userid))
//			throw new QuoddException("User id cannot be empty");
//		if (!Filters.checkEmptyStrings(alertName))
//			throw new QuoddException("Alerts name cannot be empty");
//		int userId = 0;
//		try {
//			userId = Integer.parseInt(userid);
//		} catch (Exception e) {
//			throw new QuoddException("Unable to parse User id");
//		}
//		if (userId == 0)
//			throw new QuoddException("User id cannot be zero.");
//		return historicalAlertDao.gethistoricalAlertbyIdandName(userId, alertName);
//	};
//
//	public static final Route listhistoricalAlertsbyAlertNameandNewsSource = (request, response) -> {
//		String userid = request.queryParams("USERID");
//		String newsSource = request.queryParams("NEWS_SOURCE");
//		String isnews = request.queryParams("IS_NEWS");
//		if (userid == null || newsSource == null || isnews == null)
//			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
//		if (!Filters.checkEmptyStrings(userid, newsSource, isnews))
//			throw new QuoddException("User id cannot be empty");
//		int userId = 0;
//		try {
//			userId = Integer.parseInt(userid);
//		} catch (Exception e) {
//			throw new QuoddException("Unable to parse User id");
//		}
//		if (userId == 0)
//			throw new QuoddException("User id cannot be zero");
//		return historicalAlertDao.getHistoricalAlertNameandNewsSource(userId);
//	};

//	public static final Route addhistoricalAlert = (request, response) -> {
//		try {
//			String body = request.body();
//			if (!Filters.checkNullableStrings(body))
//				throw new QuoddException(AlertConstant.ERROR_REQUEST_BODY_NULL);
//			if (!Filters.checkEmptyStrings(body))
//				throw new QuoddException(AlertConstant.ERROR_REQUEST_BODY_EMPTY);
//			body = URLDecoder.decode(body, Constants.URL_CHARACTER_ENCODING);
//			HistoricalAlertBean user = gson.fromJson(body, HistoricalAlertBean.class);
//			return historicalAlertDao.addHistoricalAlert(user);
//		} catch (Exception e) {
//			System.out.println(e);
//			return "Enable to enter the values. ";
//		}
//	};
}
