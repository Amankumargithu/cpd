package com.quodd.bean;

import java.util.Map;

public class ValueUpdateBean extends BaseBean {
	private long locateCode;
	private int marketCenterLocate;
	private long updateFlag;
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

	public long getUpdateFlag() {
		return this.updateFlag;
	}

	public void setUpdateFlag(long updateFlag) {
		this.updateFlag = updateFlag;
	}

	public Map<Integer, String> getAppendageMap() {
		return this.appendageMap;
	}

	public void setAppendageMap(Map<Integer, String> appendageMap) {
		this.appendageMap = appendageMap;
	}

}
