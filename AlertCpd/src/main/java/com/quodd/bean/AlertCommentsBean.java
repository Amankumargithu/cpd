package com.quodd.bean;

public class AlertCommentsBean {

	private long userId;
	private String alertName;
	private String alertComment;
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
	public String getAlertComment() {
		return alertComment;
	}
	public void setAlertComment(String alertComment) {
		this.alertComment = alertComment;
	}
	@Override
	public String toString() {
		return "AlertCommentsBean [userId=" + userId + ", alertName=" + alertName + ", alertComment=" + alertComment
				+ "]";
	}
	
	
}
