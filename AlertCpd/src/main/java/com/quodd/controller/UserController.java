package com.quodd.controller;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.logger;
import static spark.Spark.halt;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import com.quodd.common.util.Constants;
import com.quodd.common.util.Filters;
import com.quodd.exception.QuoddException;
import com.quodd.util.AlertConstant;

import spark.Route;

public class UserController {
	public static final Route getFiredUserAlerts = (request, response) -> {
		String result = "";
		String body = request.body();
		if (!Filters.checkNullableStrings(body))
			throw new QuoddException(AlertConstant.ERROR_REQUEST_BODY_NULL);
		if (!Filters.checkEmptyStrings(body))
			throw new QuoddException(AlertConstant.ERROR_REQUEST_BODY_EMPTY);
		try {
			long userid = -1;
			String[] tempPairs = body.split("&");
			for (String pair : tempPairs) {
				String[] temp = pair.split("=");
				if ("AUID".equals(temp[0])) {
					try {
						userid = Long.parseLong(temp[1]);
					} catch (NumberFormatException e) {
						logger.log(Level.WARNING, e.getMessage(), e);
					}
					break;
				}
			}
			Hashtable<String, String> hash = new Hashtable<>();
			StringBuilder templist = new StringBuilder();
			if (userid > 0) {
				List<String> firedList = alertCache.getFiredUserAlerts(Long.toString(userid));
				if (firedList != null) {
					firedList.forEach(s -> templist.append("|").append(s));
				}
				firedList = alertCache.getUserSystemAlerts(Long.toString(userid));
				if (firedList != null) {
					firedList.forEach(s -> templist.append("|").append(s));
				}
			}
			String alerts = "";
			if (templist.length() > 0)
				alerts = templist.toString().substring(1);
			hash.put("ALERTS", alerts);

			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);) {
				oos.writeObject(hash);
				oos.flush();
				result = new String(baos.toByteArray(), "UTF-8");
			}
//			response.body(result);
			response.type("application/text");
			response.header("Content-Length", Integer.toString(result.length()));
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	};

	public static final Route getFiredUserAlertsJson = (request, response) -> {
		String alerts = "";
		String userid = request.queryParams("user_id");
		if (userid == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(userid))
			throw new QuoddException("user_id cannot be empty");
		StringBuilder templist = new StringBuilder();
		List<String> firedList = alertCache.getFiredUserAlerts(userid);
		if (firedList != null) {
			firedList.forEach(s -> templist.append("|").append(s));
		}
		firedList = alertCache.getUserSystemAlerts(userid);
		if (firedList != null) {
			firedList.forEach(s -> templist.append("|").append(s));
		}
		if (templist.length() > 0)
			alerts = templist.toString().substring(1);
		return alerts;
	};
}
