package com.quodd.bean;

import java.util.Map;

public class InstrumentStatusBean extends BaseBean {
	private long locateCode;
	private int marketCenterLocate;
	private int statusType;
	private int statusCode;
	private int reasonCode;
	private int statusFlag;
	private String reasonDetail;
	private Map<Integer, String> appendageMap;

	public long getLocateCode() {
		return this.locateCode;
	}

	public void setLocateCode(long locateCode) {
		this.locateCode = locateCode;
	}

	public int getMarketCenterLocate() {
		return this.marketCenterLocate;
	}

	public void setMarketCenterLocate(int marketCenterLocate) {
		this.marketCenterLocate = marketCenterLocate;
	}

	public int getStatusType() {
		return this.statusType;
	}

	public void setStatusType(int statusType) {
		this.statusType = statusType;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getReasonCode() {
		return this.reasonCode;
	}

	public void setReasonCode(int reasonCode) {
		this.reasonCode = reasonCode;
	}

	public int getStatusFlag() {
		return this.statusFlag;
	}

	public void setStatusFlag(int statusFlag) {
		this.statusFlag = statusFlag;
	}

	public String getReasonDetail() {
		return this.reasonDetail;
	}

	public void setReasonDetail(String reasonDetail) {
		this.reasonDetail = reasonDetail;
	}

	public Map<Integer, String> getAppendageMap() {
		return this.appendageMap;
	}

	public void setAppendageMap(Map<Integer, String> appendageMap) {
		this.appendageMap = appendageMap;
	}
}
