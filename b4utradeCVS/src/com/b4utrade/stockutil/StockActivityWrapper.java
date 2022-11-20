package com.b4utrade.stockutil;

public class StockActivityWrapper {
	
	byte[] bytes;
	String ticker;
	String exchange = "";
	String activityDate = "";
	boolean messageProcessed = false;
	
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		if (exchange != null) {
			this.exchange = exchange;
		}			
	}
	public String getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(String activityDate) {
		if (activityDate != null) {
			this.activityDate = activityDate;
		}		
	}
	public boolean isMessageProcessed() {
		return messageProcessed;
	}
	public void setMessageProcessed(boolean messageProcessed) {
		this.messageProcessed = messageProcessed;
	}

	

}
