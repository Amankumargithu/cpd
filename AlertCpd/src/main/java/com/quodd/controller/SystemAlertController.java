package com.quodd.controller;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.systemAlertDao;
import static spark.Spark.halt;

import java.text.SimpleDateFormat;

import com.quodd.common.util.Constants;
import com.quodd.common.util.Filters;
import com.quodd.common.util.RequestUtil;
import com.quodd.exception.QuoddException;
import com.quodd.util.AlertConstant;

import spark.Route;

public class SystemAlertController {

	public static final Route deleteSystemAlert = (request, response) -> {
		String systemAlertIdStr = request.queryParams("system_alert_id");
		if (systemAlertIdStr == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(systemAlertIdStr))
			throw new QuoddException("system_alert_id cannot be empty");
		long systemAlertId = 0;
		try {
			systemAlertId = Long.parseLong(systemAlertIdStr);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse system_alert_id");
		}
		if (systemAlertId == 0)
			throw new QuoddException("system_alert_id cannot be zero");
		systemAlertDao.deleteSystemAlert(systemAlertId);
		return RequestUtil.successResponse;
	};

	public static final Route getActiveSystemAlert = (request, response) -> systemAlertDao.selectActiveSystemAlert();

	public static final Route addSystemAlert = (request, response) -> {
		String alertText = request.queryParams("alert_text");
		String effectiveDateStr = request.queryParams("effective_date");
		String expiryDateStr = request.queryParams("expiry_date");
		if (!Filters.checkNullableStrings(alertText, expiryDateStr, effectiveDateStr))
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(alertText, expiryDateStr, effectiveDateStr))
			throw new QuoddException("parameters cannot be empty");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		long effectiveDate = 0;
		try {
			effectiveDate = sdf.parse(effectiveDateStr).getTime();
		} catch (Exception e) {
			throw new QuoddException("Unable to parse effective_date");
		}
		long expiryDate = 0;
		try {
			expiryDate = sdf.parse(expiryDateStr).getTime();
		} catch (Exception e) {
			throw new QuoddException("Unable to parse expiry_date");
		}
		if (effectiveDate == 0 || expiryDate == 0)
			throw new QuoddException("dates cannot be zero");
		systemAlertDao.addSystemAlert(alertText, effectiveDate, expiryDate);
		return RequestUtil.successResponse;
	};

	public static final Route updateSystemAlert = (request, response) -> {
		String systemAlertIdStr = request.queryParams("system_alert_id");
		String alertText = request.queryParams("alert_text");
		String effectiveDateStr = request.queryParams("effective_date");
		String expiryDateStr = request.queryParams("expiry_date");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		if (!Filters.checkNullableStrings(alertText, expiryDateStr, effectiveDateStr, systemAlertIdStr))
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(alertText, expiryDateStr, effectiveDateStr, systemAlertIdStr))
			throw new QuoddException("parameters cannot be empty");
		long effectiveDate = 0;
		try {
			effectiveDate = sdf.parse(effectiveDateStr).getTime();
		} catch (Exception e) {
			throw new QuoddException("Unable to parse effective_date");
		}
		long expiryDate = 0;
		try {
			expiryDate = sdf.parse(expiryDateStr).getTime();
		} catch (Exception e) {
			throw new QuoddException("Unable to parse expiry_date");
		}
		long systemAlertId = 0;
		try {
			systemAlertId = Long.parseLong(systemAlertIdStr);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse system_alert_id");
		}
		if (effectiveDate == 0 || expiryDate == 0 || systemAlertId == 0)
			throw new QuoddException("dates cannot be zero");
		systemAlertDao.updateSystemAlert(alertText, effectiveDate, expiryDate, systemAlertId);
		return RequestUtil.successResponse;
	};

	public static final Route getFiredUserAlerts = (request, response) -> {
		String userid = request.queryParams("user_id");
		if (userid == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(userid))
			throw new QuoddException("user_id cannot be empty");
		return alertCache.getUserSystemAlerts(userid);
	};
}
