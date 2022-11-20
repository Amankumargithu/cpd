package com.quodd.bean;

public class AlertBean {

	private long userId;
	private String ticker;
	private String webFlag;
	private String alertType;
	private String alertValue;
	private String dateCreated;
	private String alertName;
	private double alertFrequency;
	private String comment;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getWebFlag() {
		return webFlag;
	}

	public void setWebFlag(String webFlag) {
		this.webFlag = webFlag;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getAlertValue() {
		return alertValue;
	}

	public void setAlertValue(String alertValue) {
		this.alertValue = alertValue;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public double getAlertFrequency() {
		return alertFrequency;
	}

	public void setAlertFrequency(double alertFrequency) {
		this.alertFrequency = alertFrequency;
	}

	@Override
	public String toString() {
		return "UserBean [userId=" + userId + ", ticker=" + ticker + ", webFlag=" + webFlag
				+ ", alertType=" + alertType + ", alertValue=" + alertValue + ", dateCreated=" + dateCreated
				+ ", alertName=" + alertName + ", alertFrequency=" + alertFrequency + "]";
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
