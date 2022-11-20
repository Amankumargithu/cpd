package com.quodd.common.api.stat;

public class ApiStatBean {
	private String sessionId;
	private String requestIp;
	private long userId;
	private String companyCode;
	private boolean isAuthenticated;
	private long requestTimeStamp;
	private long serviceId;
	private String identifier;
	private String uniqueIdentifier;
	private String reasonForFailure;
	private String marketTier;

	public ApiStatBean(String sessionId, String requestIp, long userId, String companyCode, boolean isAuth,
			long requestTime, long service, String identifier, String uniqueIdentifier, String reasonForFailure,
			String marketTier) {
		this.sessionId = sessionId;
		this.requestIp = requestIp;
		this.userId = userId;
		this.companyCode = companyCode;
		this.isAuthenticated = isAuth;
		this.requestTimeStamp = requestTime;
		this.serviceId = service;
		this.identifier = identifier;
		this.uniqueIdentifier = uniqueIdentifier;
		this.reasonForFailure = reasonForFailure;
		this.marketTier = marketTier;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public String getRequestIp() {
		return this.requestIp;
	}

	public long getUserId() {
		return this.userId;
	}

	public String getCompanyCode() {
		return this.companyCode;
	}

	public boolean isAuthenticated() {
		return this.isAuthenticated;
	}

	public long getRequestTimeStamp() {
		return this.requestTimeStamp;
	}

	public long getServiceId() {
		return this.serviceId;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getUniqueIdentifier() {
		return this.uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public String getReasonForFailure() {
		return this.reasonForFailure;
	}

	public String getMarketTier() {
		return marketTier;
	}

	public void setMarketTier(String marketTier) {
		this.marketTier = marketTier;
	}
}
