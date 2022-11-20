package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;

public class UserAlertDetailBean extends DefaultObject {
	private static final long serialVersionUID = 1L;
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

	private static final boolean ACTIVITY_UNCHECKED = false;
//	private static final boolean ACTIVITY_CHECKED = true;
	
	public static final int ALERT_DELETE_AFTER_FIRST_ALERT	= 0;
	public static final int ALERT_ONCE_PER_DAY	= 1;
	public static final int ALERT_EVERYTIME	= 2;

	private String alertName;
	private String tickerName;
	private String webFlag;
	private String alertComments;
	private String alarmTime;
	private int alertFrequency = ALERT_DELETE_AFTER_FIRST_ALERT;

	private String lastOverAlertValue;
	private String lastUnderAlertValue;
	private String lastEqualAlertValue;
	private String percentChangeUpAlertValue;
	private String percentChangeDownAlertValue;
	private String bidOverAlertValue;
	private String bidUnderAlertValue;
	private String bidEqualAlertValue;
	private String lastTradeVolumeOverAlertValue;
	private String lastTradeVolumeEqualAlertValue;
	private String lastTradeVolumeEqualOverAlertValue;
	private String askOverAlertValue;
	private String askUnderAlertValue;
	private String askEqualAlertValue;
	private String volumeOverAlertValue;
	private String volumeUnderAlertValue;
	private String volumeEqualAlertValue;

	private boolean lastOverAlertActivity;
	private boolean lastUnderAlertActivity;

	private boolean lastEqualAlertActivity;
	private boolean percentChangeUpAlertActivity;
	private boolean percentChangeDownAlertActivity;
	private boolean bidOverAlertActivity;
	private boolean bidUnderAlertActivity;
	private boolean lastTradeVolumeEqualAlertActivity;
	private boolean lastTradeVolumeOverAlertActivity;
	private boolean lastTradeVolumeEqualOverAlertActivity;

	private boolean bidEqualAlertActivity;
	private boolean askOverAlertActivity;
	private boolean askUnderAlertActivity;

	private boolean askEqualAlertActivity;
	private boolean volumeOverAlertActivity;
	private boolean volumeEqualAlertActivity;
	private boolean volumeUnderAlertActivity;
	private boolean fiftyTwoWeekHighAlertActivity;
	private boolean fiftyTwoWeekLowAlertActivity;
	private boolean companyNewsAlertActivity;
	private boolean earningsReportedAlertActivity;

	/**
	 * Default constructor - does nothing.
	 */
	public UserAlertDetailBean() {
		alertName = "";
		webFlag = "";
		tickerName = "";

		lastOverAlertActivity = ACTIVITY_UNCHECKED;
		lastUnderAlertActivity = ACTIVITY_UNCHECKED;
		percentChangeUpAlertActivity = ACTIVITY_UNCHECKED;
		percentChangeDownAlertActivity = ACTIVITY_UNCHECKED;
		bidOverAlertActivity = ACTIVITY_UNCHECKED;
		bidUnderAlertActivity = ACTIVITY_UNCHECKED;
		askOverAlertActivity = ACTIVITY_UNCHECKED;
		askUnderAlertActivity = ACTIVITY_UNCHECKED;
		volumeOverAlertActivity = ACTIVITY_UNCHECKED;
		fiftyTwoWeekHighAlertActivity = ACTIVITY_UNCHECKED;
		fiftyTwoWeekLowAlertActivity = ACTIVITY_UNCHECKED;
		companyNewsAlertActivity = ACTIVITY_UNCHECKED;
		volumeEqualAlertActivity = ACTIVITY_UNCHECKED;
		volumeUnderAlertActivity = ACTIVITY_UNCHECKED;
		earningsReportedAlertActivity = ACTIVITY_UNCHECKED;
		lastTradeVolumeEqualAlertActivity = ACTIVITY_UNCHECKED;
		lastTradeVolumeOverAlertActivity = ACTIVITY_UNCHECKED;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public String getAlertName() {
		return this.alertName;
	}

	public void setLastOverAlertActivity(boolean lastOverAlertActivity) {
		this.lastOverAlertActivity = lastOverAlertActivity;
	}

	public boolean getLastOverAlertActivity() {
		return this.lastOverAlertActivity;
	}

	public boolean isLastOverActivity() {
		return (getLastOverAlertActivity());
	}

	public void setLastOverAlertValue(String alertValue) {
		lastOverAlertValue = alertValue;
	}

	public String getLastOverAlertValue() {
		return (lastOverAlertValue);
	}

	public void setLastUnderAlertActivity(boolean lastUnderAlertActivity) {
		this.lastUnderAlertActivity = lastUnderAlertActivity;
	}

	public boolean getLastUnderAlertActivity() {
		return this.lastUnderAlertActivity;
	}

	public boolean isLastUnderActivity() {
		return (getLastUnderAlertActivity());
	}

	public void setLastUnderAlertValue(String alertValue) {
		lastUnderAlertValue = alertValue;
	}

	public String getLastUnderAlertValue() {
		return (lastUnderAlertValue);
	}

	public void setPercentChangeUpAlertActivity(boolean percentChangeUpAlertActivity) {
		this.percentChangeUpAlertActivity = percentChangeUpAlertActivity;
	}

	public boolean getPercentChangeUpAlertActivity() {
		return (this.percentChangeUpAlertActivity);
	}

	public boolean isPercentChangeUpActivity() {
		return (getPercentChangeUpAlertActivity());
	}

	public void setPercentChangeUpAlertValue(String alertValue) {
		percentChangeUpAlertValue = alertValue;
	}

	public String getPercentChangeUpAlertValue() {
		return (percentChangeUpAlertValue);
	}

	public void setPercentChangeDownAlertActivity(boolean percentChangeDownAlertActivity) {
		this.percentChangeDownAlertActivity = percentChangeDownAlertActivity;
	}

	public boolean getPercentChangeDownAlertActivity() {
		return (this.percentChangeDownAlertActivity);
	}

	public boolean isPercentChangeDownActivity() {
		return (getPercentChangeDownAlertActivity());
	}

	public void setPercentChangeDownAlertValue(String alertValue) {
		percentChangeDownAlertValue = alertValue;
	}

	public String getPercentChangeDownAlertValue() {
		return (percentChangeDownAlertValue);
	}

	public void setBidOverAlertActivity(boolean bidOverAlertActivity) {
		this.bidOverAlertActivity = bidOverAlertActivity;
	}

	public boolean getBidOverAlertActivity() {
		return (this.bidOverAlertActivity);
	}

	public boolean isBidOverActivity() {
		return (getBidOverAlertActivity());
	}

	public void setBidOverAlertValue(String alertValue) {
		bidOverAlertValue = alertValue;
	}

	public String getBidOverAlertValue() {
		return (bidOverAlertValue);
	}

	public void setBidUnderAlertActivity(boolean bidUnderAlertActivity) {
		this.bidUnderAlertActivity = bidUnderAlertActivity;
	}

	public boolean getBidUnderAlertActivity() {
		return (this.bidUnderAlertActivity);
	}

	public boolean isBidUnderActivity() {
		return (getBidUnderAlertActivity());
	}

	public void setBidUnderAlertValue(String alertValue) {
		bidUnderAlertValue = alertValue;
	}

	public String getBidUnderAlertValue() {
		return (bidUnderAlertValue);
	}

	public void setAskOverAlertActivity(boolean askOverAlertActivity) {
		this.askOverAlertActivity = askOverAlertActivity;
	}

	public boolean getAskOverAlertActivity() {
		return (this.askOverAlertActivity);
	}

	public boolean isAskOverActivity() {
		return (getAskOverAlertActivity());
	}

	public void setAskOverAlertValue(String alertValue) {
		askOverAlertValue = alertValue;
	}

	public String getAskOverAlertValue() {
		return (askOverAlertValue);
	}

	public void setAskUnderAlertActivity(boolean askUnderAlertActivity) {
		this.askUnderAlertActivity = askUnderAlertActivity;
	}

	public boolean getAskUnderAlertActivity() {
		return (this.askUnderAlertActivity);
	}

	public boolean isAskUnderActivity() {
		return (getAskUnderAlertActivity());
	}

	public void setAskUnderAlertValue(String alertValue) {
		askUnderAlertValue = alertValue;
	}

	public String getAskUnderAlertValue() {
		return (askUnderAlertValue);
	}

	public void setVolumeOverAlertActivity(boolean volumeAlertActivity) {
		this.volumeOverAlertActivity = volumeAlertActivity;
	}

	public boolean getVolumeOverAlertActivity() {
		return (this.volumeOverAlertActivity);
	}

	public boolean isVolumeOverActivity() {
		return (getVolumeOverAlertActivity());
	}

	public void setVolumeOverAlertValue(String alertValue) {
		volumeOverAlertValue = alertValue;
	}

	public String getVolumeOverAlertValue() {
		return (volumeOverAlertValue);
	}

	public void setFiftyTwoWeekHighAlertActivity(boolean fiftyTwoWeekHighAlertActivity) {
		this.fiftyTwoWeekHighAlertActivity = fiftyTwoWeekHighAlertActivity;
	}

	public boolean getFiftyTwoWeekHighAlertActivity() {
		return (this.fiftyTwoWeekHighAlertActivity);
	}

	public boolean isFiftyTwoWeekHighActivity() {
		return (getFiftyTwoWeekHighAlertActivity());
	}

	public void setFiftyTwoWeekLowAlertActivity(boolean fiftyTwoWeekLowAlertActivity) {
		this.fiftyTwoWeekLowAlertActivity = fiftyTwoWeekLowAlertActivity;
	}

	public boolean getFiftyTwoWeekLowAlertActivity() {
		return (this.fiftyTwoWeekLowAlertActivity);
	}

	public boolean isFiftyTwoWeekLowActivity() {
		return (getFiftyTwoWeekLowAlertActivity());
	}

	public void setCompanyNewsAlertActivity(boolean companyNewsAlertActivity) {
		this.companyNewsAlertActivity = companyNewsAlertActivity;
	}

	public boolean getCompanyNewsAlertActivity() {
		return (this.companyNewsAlertActivity);
	}

	public boolean isCompanyNewsActivity() {
		return (getCompanyNewsAlertActivity());
	}

	public void setEarningsReportedAlertActivity(boolean earningsReportedAlertActivity) {
		this.earningsReportedAlertActivity = earningsReportedAlertActivity;
	}

	public boolean getEarningsReportedAlertActivity() {
		return (this.earningsReportedAlertActivity);
	}

	public boolean isEarningsReportedActivity() {
		return (getEarningsReportedAlertActivity());
	}

	public void setWebFlag(String webFlag) {
		this.webFlag = webFlag;
	}

	public String getWebFlag() {
		return this.webFlag;
	}

	public void setWindowAlert() {
		webFlag = WINDOWALERT;
	}

	public boolean isWindowAlert() {
		return (webFlag.equalsIgnoreCase(WINDOWALERT));
	}

	public void setEmailAlert() {
		webFlag = EMAILALERT;
	}

	public boolean isEmailAlert() {
		return (webFlag.equalsIgnoreCase(EMAILALERT));
	}

	public void setAudibleAlert() {
		webFlag = AUDIBLEALERT;
	}

	public boolean isAudibleAlert() {
		return (webFlag.equalsIgnoreCase(AUDIBLEALERT));
	}

	public void setBothWindowEmailAlert() {
		webFlag = WINDOWEMAILALERT;
	}

	public boolean isBothWindowEmailAlert() {
		return (webFlag.equalsIgnoreCase(WINDOWEMAILALERT));
	}

	public void setBothWindowAudibleAlert() {
		webFlag = WINDOWAUDIBLEALERT;
	}

	public boolean isBothWindowAudibleAlert() {
		return (webFlag.equalsIgnoreCase(WINDOWAUDIBLEALERT));
	}

	public void setBothEmailAudibleAlert() {
		webFlag = EMAILAUDIBLEALERT;
	}

	public boolean isBothEmailAudibleAlert() {
		return (webFlag.equalsIgnoreCase(EMAILAUDIBLEALERT));
	}

	public void setAllAlerts() {
		webFlag = WINDOWEMAILAUDIBLEALERT;
	}

	public boolean isAllAlerts() {
		return (webFlag.equalsIgnoreCase(WINDOWEMAILAUDIBLEALERT));
	}

	public String getTickerName() {
		return tickerName;
	}

	public void setTickerName(String tickerName) {
		this.tickerName = tickerName;
	}

	public String getAlertComments() {
		return alertComments;
	}

	public void setAlertComments(String alertComments) {
		this.alertComments = alertComments;
	}

	public boolean isLastEqualAlertActivity() {
		return lastEqualAlertActivity;
	}

	public void setLastEqualAlertActivity(boolean lastEqualAlertActivity) {
		this.lastEqualAlertActivity = lastEqualAlertActivity;
	}

	public boolean isBidEqualAlertActivity() {
		return bidEqualAlertActivity;
	}

	public void setBidEqualAlertActivity(boolean bidEqualAlertActivity) {
		this.bidEqualAlertActivity = bidEqualAlertActivity;
	}

	public boolean isAskEqualAlertActivity() {
		return askEqualAlertActivity;
	}

	public void setAskEqualAlertActivity(boolean askEqualAlertActivity) {
		this.askEqualAlertActivity = askEqualAlertActivity;
	}

	public String getLastEqualAlertValue() {
		return lastEqualAlertValue;
	}

	public void setLastEqualAlertValue(String lastEqualAlertValue) {
		this.lastEqualAlertValue = lastEqualAlertValue;
	}

	public String getBidEqualAlertValue() {
		return bidEqualAlertValue;
	}

	public void setBidEqualAlertValue(String bidEqualAlertValue) {
		this.bidEqualAlertValue = bidEqualAlertValue;
	}

	public String getAskEqualAlertValue() {
		return askEqualAlertValue;
	}

	public void setAskEqualAlertValue(String askEqualAlertValue) {
		this.askEqualAlertValue = askEqualAlertValue;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	public String getVolumeUnderAlertValue() {
		return volumeUnderAlertValue;
	}

	public void setVolumeUnderAlertValue(String volumeUnderAlertValue) {
		this.volumeUnderAlertValue = volumeUnderAlertValue;
	}

	public String getVolumeEqualAlertValue() {
		return volumeEqualAlertValue;
	}

	public void setVolumeEqualAlertValue(String volumeEqualAlertValue) {
		this.volumeEqualAlertValue = volumeEqualAlertValue;
	}

	public boolean isVolumeEqualAlertActivity() {
		return volumeEqualAlertActivity;
	}

	public void setVolumeEqualAlertActivity(boolean volumeEqualAlertActivity) {
		this.volumeEqualAlertActivity = volumeEqualAlertActivity;
	}

	public boolean isVolumeUnderAlertActivity() {
		return volumeUnderAlertActivity;
	}

	public void setVolumeUnderAlertActivity(boolean volumeUnderAlertActivity) {
		this.volumeUnderAlertActivity = volumeUnderAlertActivity;
	}

	public int getAlertFrequency() {
		return alertFrequency;
	}

	public void setAlertFrequency(int alertFrequency) {
		this.alertFrequency = alertFrequency;
	}
	
	public void setLastTradeVolumeOverAlertActivity(boolean lastTradeVolumeOverActivity) {
		this.lastTradeVolumeOverAlertActivity = lastTradeVolumeOverActivity;
	}

	public boolean getLastTradeVolumeOverAlertActivity() {
		return (this.lastTradeVolumeOverAlertActivity);
	}

	public boolean isLastTradeVolumeOverAlertActivity() {
		return (getLastTradeVolumeOverAlertActivity());
	}

	public void setLastTradeVolumeOverAlertValue(String alertValue) {
		lastTradeVolumeOverAlertValue = alertValue;
	}

	public String getLastTradeVolumeOverAlertValue() {
		return (lastTradeVolumeOverAlertValue);
	}
	
	public void setLastTradeVolumeEqualAlertActivity(boolean lastTradeVolumeEqualActivity) {
		this.lastTradeVolumeEqualAlertActivity = lastTradeVolumeEqualActivity;
	}

	public boolean getLastTradeVolumeEqualAlertActivity() {
		return (this.lastTradeVolumeEqualAlertActivity);
	}

	public boolean isLastTradeVolumeEqualAlertActivity() {
		return (getLastTradeVolumeEqualAlertActivity());
	}

	public void setLastTradeVolumeEqualAlertValue(String alertValue) {
		lastTradeVolumeEqualAlertValue = alertValue;
	}

	public String getLastTradeVolumeEqualAlertValue() {
		return (lastTradeVolumeEqualAlertValue);
	}
	
	public void setLastTradeVolumeEqualOverAlertActivity(boolean lastTradeVolumeEqualOverActivity) {
		this.lastTradeVolumeEqualOverAlertActivity = lastTradeVolumeEqualOverActivity;
	}

	public boolean getLastTradeVolumeEqualOverAlertActivity() {
		return (this.lastTradeVolumeEqualOverAlertActivity);
	}

	public boolean isLastTradeVolumeEqualOverAlertActivity() {
		return (getLastTradeVolumeEqualOverAlertActivity());
	}

	public void setLastTradeVolumeEqualOverAlertValue(String alertValue) {
		lastTradeVolumeEqualOverAlertValue = alertValue;
	}

	public String getLastTradeVolumeEqualOverAlertValue() {
		return (lastTradeVolumeEqualOverAlertValue);
	}
}
