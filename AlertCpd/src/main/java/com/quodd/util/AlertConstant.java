package com.quodd.util;

import java.text.DecimalFormat;

import com.quodd.common.util.Constants;

public final class AlertConstant {

	public static final String WINDOWALERT = "W";
	public static final String EMAILALERT = "E";
	public static final String AUDIBLEALERT = "A";
	public static final String WINDOWEMAILALERT = "WE";
	public static final String WINDOWAUDIBLEALERT = "WA";
	public static final String EMAILAUDIBLEALERT = "EA";
	public static final String WINDOWEMAILAUDIBLEALERT = "WEA";

	public static final String LAST_OVER_ACTIVITY = "LO";
	public static final String LAST_UNDER_ACTIVITY = "LU";
	public static final String LAST_EQUAL_ACTIVITY = "LE";
	public static final String PERCENTCHANGE_UP_ACTIVITY = "CU";
	public static final String PERCENTCHANGE_DOWN_ACTIVITY = "CD";
	public static final String BID_OVER_ACTIVITY = "BO";
	public static final String BID_UNDER_ACTIVITY = "BU";
	public static final String BID_EQUAL_ACTIVITY = "BE";
	public static final String ASK_OVER_ACTIVITY = "AO";
	public static final String ASK_UNDER_ACTIVITY = "AU";
	public static final String ASK_EQUAL_ACTIVITY = "AE";
	public static final String VOLUME_OVER_EQUAL_ACTIVITY = "VO";
	public static final String LAST_TRADE_VOLUME_OVER_ACTIVITY = "LTVO";
	public static final String LAST_TRADE_VOLUME_EQUAL_ACTIVITY = "LTVE";
	public static final String LAST_TRADE_VOLUME_EQUAL_OVER_ACTIVITY = "LTVEO";
	public static final String FIFTYTWOWEEK_HIGH_ACTIVITY = "B1";
	public static final String FIFTYTWOWEEK_LOW_ACTIVITY = "B2";

	public static final String COMPANY_NEWS_ACTIVITY = "C15";
	public static final String EARNINGS_REPORTED_ACTIVITY = "C12";

	public static final int ALERT_DELETE_AFTER_FIRST_ALERT = 0;
	public static final int ALERT_ONCE_PER_DAY = 1;
	public static final int ALERT_EVERYTIME = 2;

	private static final String KEY_ERROR = "error";

	public static final String ERROR_MISSING_PARAMETER_JSON = "{\"" + Constants.KEY_STATUS + "\":\"FAILURE\",\""
			+ KEY_ERROR + "\":\"Parameter Missing.\"}";

	public static final String ERROR_REQUEST_BODY_NULL = "request body cannot be null";
	public static final String ERROR_REQUEST_BODY_EMPTY = "request body cannot be empty";

	public static String formatVolume(String inVolume) {
		DecimalFormat df = new DecimalFormat("#,###,###,##0");
		if (inVolume == null)
			return "";
		try {
			return df.format(Long.parseLong(inVolume.trim()));
		} catch (NumberFormatException ne) {
			return inVolume;
		}
	}

}
