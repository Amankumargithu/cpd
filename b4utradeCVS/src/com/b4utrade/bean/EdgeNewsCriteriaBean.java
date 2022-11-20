package com.b4utrade.bean;

import java.io.Serializable;


public class EdgeNewsCriteriaBean implements Serializable{

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
	public String getSources() {
		return sources;
	}
	public void setSources(String sources) {
		this.sources = sources;
	}
	public int getOperationType() {
		return operationType;
	}
	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}
	public int getOperationType1() {
		return operationType1;
	}
	public void setOperationType1(int operationType1) {
		this.operationType1 = operationType1;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	private String tickers = "";
	private String categories = "";
	private String sources = "";
	private int operationType = 0;
	private int operationType1 = 0;
	private String startDate = null;
	private String endDate = null;
	private String queryName = "";
	
	public static final int OPERATION_AND = 1;
	public static final int OPERATION_OR  = 0;

}
