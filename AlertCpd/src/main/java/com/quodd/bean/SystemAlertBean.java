package com.quodd.bean;

public class SystemAlertBean {

	private Long systemAlertId;
	private String alertText;
	private long effectiveDate;
	private long expiryDate;

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

	public long getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(long effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public long getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(long expiryDate) {
		this.expiryDate = expiryDate;
	}

}
