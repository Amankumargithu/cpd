package com.quodd.bean;

public class AlertActivityWrapper {

	private String ticker;
	private String activity;
	private String value;

	public AlertActivityWrapper(String ticker, String activity, String value) {
		this.ticker = ticker;
		this.activity = activity;
		this.value = value;
	}

	public String getTicker() {
		return this.ticker;
	}

	public String getActivity() {
		return this.activity;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "Ticker: " + this.ticker + " Type: " + this.activity + " value: " + this.value;
	}
}
