package com.b4utrade.bean;

import java.sql.Timestamp;
import java.util.Hashtable;

public class SystemAlertBean {

	private Long systemAlertId;
	private String alertText;
	private Timestamp effectiveDate;
	private Timestamp expiryDate;

	private Hashtable<String, String> userAlerts;

	public Long getSystemAlertId() {
		return systemAlertId;
	}

	public void setSystemAlertId(Long systemAlertId) {
		this.systemAlertId = systemAlertId;
	}

	public String getAlertText() {
		return alertText;
	}

	public void setAlertText(String alertText) {
		this.alertText = alertText;
	}

	public Timestamp getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Timestamp effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Timestamp getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Timestamp expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Hashtable<String, String> getUserAlerts() {
		return userAlerts;
	}

	public void setUserAlerts(Hashtable<String, String> userAlerts) {
		this.userAlerts = userAlerts;
	}
}
