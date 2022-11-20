package com.b4utrade.bean;

public class ActiveAlertBean {
	private int userId;
	private String tickerName;
	private String webFlag;
	private String alertType;
	private String alertValue;
	private String dateCreated;
	private String alertName;
	private String comments;
	private long newsId;
	private int alertFreq;
	
	
	public long getNewsId() {
		return newsId;
	}
	public void setNewsId(long newsId) {
		this.newsId = newsId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getTickerName() {
		return tickerName;
	}
	public void setTickerName(String tickerName) {
		this.tickerName = tickerName;
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
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public int getAlertFreq() {
		return alertFreq;
	}
	public void setAlertFreq(int alertFreq) {
		this.alertFreq = alertFreq;
	}	
}
