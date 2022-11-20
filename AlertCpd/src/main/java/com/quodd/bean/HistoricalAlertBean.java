package com.quodd.bean;

public class HistoricalAlertBean {

	private long userId;
	private String alertName;
	private String ticker;
	private String alertValue;
	private String newsToday;
	private String alertComment;
	private String occuredOn;
	private String alertType;
	private int historicalAlertId;
	private double alertFrequency;
	private String webFlag;
	private String trigValue;
	private String email;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getAlertName() {
		return alertName;
	}
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public String getAlertValue() {
		return alertValue;
	}
	public void setAlertValue(String alertValue) {
		this.alertValue = alertValue;
	}
	public String getNewsToday() {
		return newsToday;
	}
	public void setNewsToday(String newsToday) {
		this.newsToday = newsToday;
	}
	public String getAlertComment() {
		return alertComment;
	}
	public void setAlertComment(String alertComment) {
		this.alertComment = alertComment;
	}
	public String getOccuredOn() {
		return occuredOn;
	}
	public void setOccuredOn(String occuredOn) {
		this.occuredOn = occuredOn;
	}
	public String getAlertType() {
		return alertType;
	}
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	public int getHistoricalAlertId() {
		return historicalAlertId;
	}
	public void setHistoricalAlertId(int historicalAlertId) {
		this.historicalAlertId = historicalAlertId;
	}
	public double getAlertFrequency() {
		return alertFrequency;
	}
	public void setAlertFrequency(double alertFrequency) {
		this.alertFrequency = alertFrequency;
	}
	public String getWebFlag() {
		return webFlag;
	}
	public void setWebFlag(String webFlag) {
		this.webFlag = webFlag;
	}
	public String getTrigValue() {
		return trigValue;
	}
	public void setTrigValue(String trigValue) {
		this.trigValue = trigValue;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String toString() {
		return "HistoricalAlertBean [userId=" + userId + ", alertName=" + alertName + ", ticker=" + ticker
				+ ", alertValue=" + alertValue + ", newsToday=" + newsToday + ", alertComment=" + alertComment
				+ ", occuredOn=" + occuredOn + ", alertType=" + alertType + ", historicalAlertId=" + historicalAlertId
				+ ", alertFrequency=" + alertFrequency + ", webFlag=" + webFlag + ", trigValue=" + trigValue
				+ ", email=" + email + "]";
	}
	
	
	
}