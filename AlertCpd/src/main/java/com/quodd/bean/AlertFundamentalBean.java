package com.quodd.bean;

public class AlertFundamentalBean {
	private String ticker;
	private String fiftyTwoWeekHigh;
	private String fiftyTwoWeekLow;
	private String averageDailyVolume;

	public AlertFundamentalBean(String ticker, String fiftyTwoWeekHigh, String fiftyTwoWeekLow,
			String averageDailyVolume) {
		this.ticker = ticker;
		this.fiftyTwoWeekHigh = fiftyTwoWeekHigh;
		this.fiftyTwoWeekLow = fiftyTwoWeekLow;
		this.averageDailyVolume = averageDailyVolume;
	}

	public String getTicker() {
		return this.ticker;
	}

	public String getFiftyTwoWeekHigh() {
		return this.fiftyTwoWeekHigh;
	}

	public String getFiftyTwoWeekLow() {
		return this.fiftyTwoWeekLow;
	}

	public String getAverageDailyVolume() {
		return this.averageDailyVolume;
	}

}
