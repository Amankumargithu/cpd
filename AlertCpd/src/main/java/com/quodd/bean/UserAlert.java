package com.quodd.bean;

/**
 * UserAlert The UserAlert class is used for company and stocks alerts that have
 * no price or percent value associated with them. Alerts are stored in a
 * Hashtable according to the ticker and activity, the alert is for, so there is
 * no need to store that information here. As well the same email is sent to
 * each user that wants a particular alert for a particular ticker so we do not
 * need a SendMail object for each user that wants the same alert. We can create
 * one and use it for all alerts of the same type.
 */
public class UserAlert {
	private boolean byWindow;
	private boolean byEmail;
	private boolean byAudio;
	private String alertName;
	private String comments;
	private long userID;
	private String email;
	private String value;
	private String ticker;
	private int alertFreq;
	private String alertType;
	private String webFlag;
	private String triggeredValue;

	public UserAlert() {
		byWindow = false;
		byEmail = false;
		byAudio = false;
	}

	@Override
	public String toString() {
		return "Ticker: " + ticker + " Type: " + alertType + " name: " + alertName + " value: " + value + " webFlag: "
				+ webFlag + " freq: " + alertFreq + " triggeredVal: " + triggeredValue;
	}

	public boolean isByWindow() {
		return byWindow;
	}

	public void setByWindow(boolean byWindow) {
		this.byWindow = byWindow;
	}

	public boolean isByEmail() {
		return byEmail;
	}

	public void setByEmail(boolean byEmail) {
		this.byEmail = byEmail;
	}

	public boolean isByAudio() {
		return byAudio;
	}

	public void setByAudio(boolean byAudio) {
		this.byAudio = byAudio;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public long  getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public int getAlertFreq() {
		return alertFreq;
	}

	public void setAlertFreq(int alertFreq) {
		this.alertFreq = alertFreq;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getWebFlag() {
		return webFlag;
	}

	public void setWebFlag(String webFlag) {
		this.webFlag = webFlag;
		if (webFlag.equalsIgnoreCase(UserAlertDetailBean.WINDOWEMAILAUDIBLEALERT)) {
			byWindow = true;
			byEmail = true;
			byAudio = true;
		} else if (webFlag.equalsIgnoreCase(UserAlertDetailBean.WINDOWEMAILALERT)) {
			byWindow = true;
			byEmail = true;
		} else if (webFlag.equalsIgnoreCase(UserAlertDetailBean.WINDOWAUDIBLEALERT)) {
			byWindow = true;
			byAudio = true;
		} else if (webFlag.equalsIgnoreCase(UserAlertDetailBean.EMAILAUDIBLEALERT)) {
			byEmail = true;
			byEmail = true;
		} else if (webFlag.equalsIgnoreCase(UserAlertDetailBean.WINDOWALERT)) {
			byWindow = true;
		} else if (webFlag.equalsIgnoreCase(UserAlertDetailBean.EMAILALERT)) {
			byEmail = true;
		} else if (webFlag.equalsIgnoreCase(UserAlertDetailBean.AUDIBLEALERT)) {
			byAudio = true;
		} else
			this.webFlag = "";
	}

	public String getTriggeredValue() {
		return triggeredValue;
	}

	public void setTriggeredValue(String triggeredValue) {
		this.triggeredValue = triggeredValue;
	}
}
