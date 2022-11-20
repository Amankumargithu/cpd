package com.b4utrade.bean;

import java.util.Calendar;

public class OptionsFundamentalBean {

	public final int OPTION_CALL = 1;
	public final int OPTION_PUT = 2;
	
	String ticker;
	String description;
	long openInterest;
	double strikePrice;
	int optionType;
	Calendar expirationDate;	// Need to change to Date
	public int getOPTION_CALL() {
		return OPTION_CALL;
	}
	public int getOPTION_PUT() {
		return OPTION_PUT;
	}
	public String getTicker() {
		return ticker;
	}
	public String getDescription() {
		return description;
	}
	public long getOpenInterest() {
		return openInterest;
	}
	public double getStrikePrice() {
		return strikePrice;
	}
	public int getOptionType() {
		return optionType;
	}
	public Calendar getExpirationDate() {
		return expirationDate;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setOpenInterest(long openInterest) {
		this.openInterest = openInterest;
	}
	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}
	public void setOptionType(int optionType) {
		this.optionType = optionType;
	}
	public void setExpirationDate(Calendar expirationDate) {
		this.expirationDate = expirationDate;
	}
}
