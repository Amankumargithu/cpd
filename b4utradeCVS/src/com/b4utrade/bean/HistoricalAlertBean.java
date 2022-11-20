package com.b4utrade.bean;

public class HistoricalAlertBean {
	private int userId;
	private String alertName;
	private String tickerName;
	private String alertValue;
	private String alertComment;
	private String alertType;
	private String newsId;
	private String occuredOn;
	private long alertId;
	private int alertFreq;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getAlertName() {
		return alertName;
	}
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	public String getTickerName() {
		return tickerName;
	}
	public void setTickerName(String tickerName) {
		this.tickerName = tickerName;
	}
	public String getAlertValue() {
		return alertValue;
	}
	public void setAlertValue(String alertValue) {
		this.alertValue = alertValue;
	}
	public String getAlertType() {
		return alertType;
	}
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getOccuredOn() {
		return occuredOn;
	}
	public void setOccuredOn(String occuredOn) {
		this.occuredOn = occuredOn;
	}
	public long getAlertId() {
		return alertId;
	}
	public void setAlertId(long alertId) {
		this.alertId = alertId;
	}
	public String getAlertComment() {
		return alertComment;
	}
	public void setAlertComment(String alertComment) {
		this.alertComment = alertComment;
	}
	public String getNewsId() {
		return newsId;
	}
	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}
	public int getAlertFreq() {
		return alertFreq;
	}
	public void setAlertFreq(int alertFreq) {
		this.alertFreq = alertFreq;
	}
	
	
}
