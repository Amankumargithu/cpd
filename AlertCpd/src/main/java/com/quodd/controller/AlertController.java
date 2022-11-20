package com.quodd.controller;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.alertCommentDao;
import static com.quodd.cpd.AlertCpd.alertDao;
import static spark.Spark.halt;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.quodd.bean.UserAlertDetailBean;
import com.quodd.common.util.Constants;
import com.quodd.common.util.Filters;
import com.quodd.common.util.RequestUtil;
import com.quodd.exception.QuoddException;
import com.quodd.util.AlertConstant;

import spark.Route;

public class AlertController {

	public static final Route listAlertsByUserId = (request, response) -> {
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
		return alertDao.getUserAlertList(userId);
	};

	public static final Route deleteUserAlertByName = (request, response) -> {
		String userid = request.queryParams("user_id");
		String alertName = request.queryParams("alert_name");
		if (userid == null || alertName == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(userid, alertName))
			throw new QuoddException("user_id and alert_name cannot be empty");
		long userId = 0;
		try {
			userId = Long.parseLong(userid);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse user_id");
		}
		if (userId == 0)
			throw new QuoddException("user_id cannot be zero");
		long count = alertDao.deleteUserAlertByName(userId, alertName);
		alertCommentDao.deleteAlertComment(userId, alertName);
		return RequestUtil.successResponse;
	};

	public static final Route addUserAlert = (request, response) -> {
		String userid = request.queryParams("user_id");
		String body = request.body();
		if (!Filters.checkNullableStrings(body, userid))
			throw new QuoddException(AlertConstant.ERROR_REQUEST_BODY_NULL);
		if (!Filters.checkEmptyStrings(body, userid))
			throw new QuoddException(AlertConstant.ERROR_REQUEST_BODY_EMPTY);
		long userId = 0;
		try {
			userId = Long.parseLong(userid);
		} catch (Exception e) {
			throw new QuoddException("Unable to parse user_id");
		}
		if (userId == 0)
			throw new QuoddException("user_id cannot be zero");
		body = URLDecoder.decode(body, Constants.URL_CHARACTER_ENCODING);
		UserAlertDetailBean detailedBean = new UserAlertDetailBean();
		detailedBean.addAlertFromJson(body);
		String alertName = detailedBean.getAlertName();
		String ticker = detailedBean.getTickerName();
		String webFlag = detailedBean.getWebFlag();
		int frequency = detailedBean.getAlertFrequency();
		String comments = detailedBean.getAlertComments();
		alertDao.deleteUserAlertByName(userId, alertName);
		alertCommentDao.deleteAlertComment(userId, alertName);
		alertCommentDao.addAlertComment(userId, alertName, comments);
		if (detailedBean.isEarningsReportedActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.EARNINGS_REPORTED_ACTIVITY, "",
					alertName, frequency, comments);
		}
		if (detailedBean.isCompanyNewsActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.COMPANY_NEWS_ACTIVITY, "", alertName,
					frequency, comments);
		}
		if (detailedBean.isFiftyTwoWeekHighActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.FIFTYTWOWEEK_HIGH_ACTIVITY, "",
					alertName, frequency, comments);
		}
		if (detailedBean.isFiftyTwoWeekLowActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.FIFTYTWOWEEK_LOW_ACTIVITY, "", alertName,
					frequency, comments);
		}
		if (detailedBean.isLastOverActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.LAST_OVER_ACTIVITY,
					detailedBean.getLastOverAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isLastUnderActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.LAST_UNDER_ACTIVITY,
					detailedBean.getLastUnderAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isLastEqualAlertActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.LAST_EQUAL_ACTIVITY,
					detailedBean.getLastEqualAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isBidOverActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.BID_OVER_ACTIVITY,
					detailedBean.getBidOverAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isBidUnderActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.BID_UNDER_ACTIVITY,
					detailedBean.getBidUnderAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isBidEqualAlertActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.BID_EQUAL_ACTIVITY,
					detailedBean.getBidEqualAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isAskOverActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.ASK_OVER_ACTIVITY,
					detailedBean.getAskOverAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isAskUnderActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.ASK_UNDER_ACTIVITY,
					detailedBean.getAskUnderAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isAskEqualAlertActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.ASK_EQUAL_ACTIVITY,
					detailedBean.getAskEqualAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isPercentChangeUpActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY,
					detailedBean.getPercentChangeUpAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isPercentChangeDownActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY,
					detailedBean.getPercentChangeDownAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isVolumeOverActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY,
					detailedBean.getVolumeOverAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isLastTradeVolumeOverAlertActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.LAST_TRADE_VOLUME_OVER_ACTIVITY,
					detailedBean.getLastTradeVolumeOverAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isLastTradeVolumeEqualOverAlertActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_OVER_ACTIVITY,
					detailedBean.getLastTradeVolumeEqualOverAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.isLastTradeVolumeEqualAlertActivity()) {
			alertDao.addUserAlert(userId, ticker, webFlag, UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_ACTIVITY,
					detailedBean.getLastTradeVolumeEqualAlertValue(), alertName, frequency, comments);
		}
		if (detailedBean.getAlarmTime() != null) {
			alertDao.addUserAlert(userId, ticker, webFlag, "TIME", detailedBean.getAlarmTime(), alertName, frequency,
					comments);
		}
		return RequestUtil.successResponse;
	};

	public static final Route listActiveAlertsByUserId = (request, response) -> {
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
		return alertDao.getActiveAlertsByUserId(userId);
	};

	public static final Route getFiredUserAlerts = (request, response) -> {
		String userid = request.queryParams("user_id");
		if (userid == null)
			halt(Constants.HTTP_RESPONSE_BAD_REQUEST, AlertConstant.ERROR_MISSING_PARAMETER_JSON);
		if (!Filters.checkEmptyStrings(userid))
			throw new QuoddException("user_id cannot be empty");
		List<String> firedList = alertCache.getFiredUserAlerts(userid);
		if (firedList == null)
			firedList = new ArrayList<>();
		return firedList;
	};

}
