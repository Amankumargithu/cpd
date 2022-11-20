package com.quodd.util;

public class NewsBean {
	private String newsID;
	private String multicastTicker;
	private String newsDesc;
	private String newsLink;
	private String newsDate;
	private String dbDate;
	private String duration;
	private String source;
	private String tickers;
	private String categories;
	private long lastUpdateTime;
	private String publishReason;
	private String publishDate;
	private String dbDateTime;
	
	public String getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}
	public String getMulticastTicker() {
		return multicastTicker;
	}
	public void setMulticastTicker(String multicastTicker) {
		this.multicastTicker = multicastTicker;
	}
	public String getPublishReason() {
		return publishReason;
	}
	public void setPublishReason(String publishReason) {
		this.publishReason = publishReason;
	}
	public String getNewsID() {
		return newsID;
	}
	public void setNewsID(String newsID) {
		this.newsID = newsID;
	}
	public String getNewsDesc() {
		return newsDesc;
	}
	public void setNewsDesc(String newsDesc) {
		this.newsDesc = newsDesc;
	}
	public String getNewsLink() {
		return newsLink;
	}
	public void setNewsLink(String newsLink) {
		this.newsLink = newsLink;
	}
	public String getNewsDate() {
		return newsDate;
	}
	public void setNewsDate(String newsDate) {
		this.newsDate = newsDate;
	}
	public String getDbDate() {
		return dbDate;
	}
	public void setDbDate(String dbDate) {
		this.dbDate = dbDate;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTickers() {
		return tickers;
	}
	public void setTickers(String tickers) {
		this.tickers = tickers;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public String getDbDateTime() {
		return dbDateTime;
	}
	public void setDbDateTime(String dbDateTime) {
		this.dbDateTime = dbDateTime;
	}
}