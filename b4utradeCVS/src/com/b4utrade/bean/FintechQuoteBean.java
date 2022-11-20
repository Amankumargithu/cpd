package com.b4utrade.bean;

public class FintechQuoteBean {
	String symbol;
	FintechBean values;
	String exchange;
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public FintechBean getValues() {
		return values;
	}
	public void setValues(FintechBean values) {
		this.values = values;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
}
