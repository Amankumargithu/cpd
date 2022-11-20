/**
 * DetailedQuoteBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;

/**
 * The News bean object holding news data.
 */
public class DetailedQuoteBean extends DefaultObject {

	private String tickerName;
	private String companyName;
	private String exchange;
	private String sector;
	private int industryID;
	private String industry;

	private String stockYearHi;
	private String stockYearLo;
	private String stockEPS;
	private String stockPE;
	private String stockAvgDayVol;
	private String stockDiv;
	private String stockYield;
	private String stockMktCap;
	private String stockExDivDate;
	private String cusipId;

	private String currency;
	private String stockType;
	private boolean isPinkSheetTicker;

	private String trailingDiv;
	private String divFrequency;

	private String earningReportDate;
	private String isin;
	private String priceBookRatio;
	private String beta;
	private String priceYTDPercent;
	private String YTDvsSPPercent;
	private String av10DVol;
	private String av20DVol;
	private String av30DVol;
	private String av50DVol;
	private String postPanel;
	private String specName;

	public String getPostPanel() {
		return postPanel;
	}

	public void setPostPanel(String postPanel) {
		this.postPanel = postPanel;
	}

	public String getSpecName() {
		return specName;
	}

	public void setSpecName(String specName) {
		this.specName = specName;
	}

	public String getAv10DVol() {
		return av10DVol;
	}

	public void setAv10DVol(String av10dVol) {
		av10DVol = av10dVol;
	}

	public String getAv20DVol() {
		return av20DVol;
	}

	public void setAv20DVol(String av20dVol) {
		av20DVol = av20dVol;
	}

	public String getAv30DVol() {
		return av30DVol;
	}

	public void setAv30DVol(String av30dVol) {
		av30DVol = av30dVol;
	}
	public String getPriceYTDPercent() {
		return priceYTDPercent;
	}

	public void setPriceYTDPercent(String priceYTDPercent) {
		this.priceYTDPercent = priceYTDPercent;
	}

	public String getYTDvsSPPercent() {
		return YTDvsSPPercent;
	}

	public void setYTDvsSPPercent(String yTDvsSPPercent) {
		YTDvsSPPercent = yTDvsSPPercent;
	}

	public String getBeta() {
		return beta;
	}

	public void setBeta(String beta) {
		this.beta = beta;
	}

	/**
	 * Reserved for future use
	 */
	private String reservedVar;

	/**
	 * Standard constructor.
	 */
	public DetailedQuoteBean() {

	}

	/**
	 * Gets the cusip ID.
	 * 
	 * @return String cusipId
	 */
	public String getCusipId() {
		return cusipId;
	}

	/**
	 * Sets the cusipId value.
	 * 
	 * @param cusipId
	 *            String
	 */
	public void setCusipId(String cusipId) {
		this.cusipId = cusipId;
	}

	/**
	 * Gets the ticker value.
	 * 
	 * @return String tickerName
	 */
	public String getTickerName() {
		return tickerName;
	}

	/**
	 * Sets the ticker value.
	 * 
	 * @param tickerName
	 *            String
	 */
	public void setTickerName(String tickerName) {
		this.tickerName = tickerName;
	}

	/**
	 * Gets the company value.
	 * 
	 * @return String companyName
	 */
	public String getCompanyName() {
		return companyName;
	}

	/**
	 * Sets the company value.
	 * 
	 * @param companyName
	 *            String
	 */
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	/**
	 * Gets the exchange value.
	 * 
	 * @return String exchange
	 */
	public String getExchange() {
		return exchange;
	}

	/**
	 * Sets the exchange value.
	 * 
	 * @param exchange
	 *            String
	 */
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	/**
	 * Gets the sector value.
	 * 
	 * @return String sector
	 */
	public String getSector() {
		return sector;
	}

	/**
	 * Sets the sector value.
	 * 
	 * @param sector
	 *            String
	 */
	public void setSector(String sector) {
		this.sector = sector;
	}

	/**
	 * Gets the industry id value.
	 * 
	 * @return int industryID
	 */
	public int getIndustryID() {
		return industryID;
	}

	/**
	 * Sets the industry id value.
	 * 
	 * @param industryID
	 *            int
	 */
	public void setIndustryID(int industryID) {
		this.industryID = industryID;
	}

	/**
	 * Gets the industry value.
	 * 
	 * @return String industry
	 */
	public String getIndustry() {
		return industry;
	}

	/**
	 * Sets the industry value.
	 * 
	 * @param industry
	 *            String
	 */
	public void setIndustry(String industry) {
		this.industry = industry;
	}

	/**
	 * Gets the stockYearHi value.
	 * 
	 * @return String stockYearHi
	 */
	public String getStockYearHi() {
		return stockYearHi;
	}

	/**
	 * Sets the stockYearHi value
	 * 
	 * @param stockYearHi
	 *            String
	 */
	public void setStockYearHi(String stockYearHi) {
		this.stockYearHi = stockYearHi;
	}

	/**
	 * Gets the stockYearLo value.
	 * 
	 * @return String stockYearLo
	 */
	public String getStockYearLo() {
		return stockYearLo;
	}

	/**
	 * Sets the stockYearLo value
	 * 
	 * @param stockYearLo
	 *            String
	 */
	public void setStockYearLo(String stockYearLo) {
		this.stockYearLo = stockYearLo;
	}

	/**
	 * Gets the stockEPS value.
	 * 
	 * @return String stockEPS
	 */
	public String getStockEPS() {
		return stockEPS;
	}

	/**
	 * Sets the stockEPS value.
	 * 
	 * @param stockEPS
	 *            String
	 */
	public void setStockEPS(String stockEPS) {
		this.stockEPS = stockEPS;
	}

	/**
	 * Gets the stockPE value.
	 * 
	 * @return String stockPE
	 */
	public String getStockPE() {
		return stockPE;
	}

	/**
	 * Sets the stockPE value.
	 * 
	 * @param stockPE
	 *            String
	 */
	public void setStockPE(String stockPE) {
		this.stockPE = stockPE;
	}

	/**
	 * Gets the stockAvgDayVol value.
	 * 
	 * @return String stockAvgDayVol
	 */
	public String getStockAvgDayVol() {
		return stockAvgDayVol;
	}

	/**
	 * Sets the stockAvgDayVol value.
	 * 
	 * @param stockAvgDayVol
	 *            String
	 */
	public void setStockAvgDayVol(String stockAvgDayVol) {
		this.stockAvgDayVol = stockAvgDayVol;
	}

	/**
	 * Gets the stockDiv value.
	 * 
	 * @return String stockDiv
	 */
	public String getStockDiv() {
		return stockDiv;
	}

	/**
	 * Sets the stockDiv value.
	 * 
	 * @param stockDiv
	 *            String
	 */
	public void setStockDiv(String stockDiv) {
		this.stockDiv = stockDiv;
	}

	/**
	 * Gets the stockYield value
	 * 
	 * @return String stockYield
	 */
	public String getStockYield() {
		return stockYield;
	}

	/**
	 * Sets the stockYield value.
	 * 
	 * @param stockYield
	 *            String
	 */
	public void setStockYield(String stockYield) {
		this.stockYield = stockYield;
	}

	/**
	 * Gets the stockMktCap value.
	 * 
	 * @return String stockMktCap
	 */
	public String getStockMktCap() {
		return stockMktCap;
	}

	/**
	 * Sets the stockMktCap value.
	 * 
	 * @return String stockMktCap
	 */
	public void setStockMktCap(String stockMktCap) {
		this.stockMktCap = stockMktCap;
	}

	/**
	 * Gets the stockExDivDate value.
	 * 
	 * @return String stockExDivDate
	 */
	public String getStockExDivDate() {
		return stockExDivDate;
	}

	/**
	 * Sets the stockExDivDate value.
	 * 
	 * @param stockExDivDate
	 *            String
	 */
	public void setStockExDivDate(String stockExDivDate) {
		this.stockExDivDate = stockExDivDate;
	}

	/**
	 * Gets the currency value.
	 * 
	 * @return String currency
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * Sets the currency value.
	 * 
	 * @param currency
	 *            String
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * Gets the stockType value.
	 * 
	 * @return String stockType
	 */
	public String getType() {
		return stockType;
	}

	/**
	 * Sets the stockType value.
	 * 
	 * @param stockType
	 *            String
	 */
	public void setType(String stockType) {
		this.stockType = stockType;
	}

	/**
	 * Gets the boolean for pinksheet symbol
	 * 
	 * @return isPinkSheetTicker boolean
	 */
	public boolean isPinkSheetTicker() {
		return isPinkSheetTicker;
	}

	/**
	 * Sets the isPinkSheetTicker value.
	 * 
	 * @param isPinkSheetTicker
	 *            boolean
	 */
	public void setPinkSheetTicker(boolean isPinkSheetTicker) {
		this.isPinkSheetTicker = isPinkSheetTicker;
	}

	/**
	 * Gets the reservedVar value. This variable is reserved for future
	 * reference
	 * 
	 * @return String reservedVar
	 */
	public String getReservedVar() {
		return reservedVar;
	}

	/**
	 * Sets the reservedVar value. This variable is reserved for future
	 * reference
	 * 
	 * @param reservedVar
	 *            String
	 */
	public void setReservedVar(String reservedVar) {
		this.reservedVar = reservedVar;
	}

	public String getTrailingDiv() {
		return trailingDiv;
	}

	public void setTrailingDiv(String trailingDiv) {
		this.trailingDiv = trailingDiv;
	}

	public String getDivFrequency() {
		return divFrequency;
	}

	public void setDivFrequency(String divFrequency) {
		this.divFrequency = divFrequency;
	}

	public String getEarningReportDate() {
		return earningReportDate;
	}

	public void setEarningReportDate(String earningReportDate) {
		this.earningReportDate = earningReportDate;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public String getPriceBookRatio() {
		return priceBookRatio;
	}

	public void setPriceBookRatio(String priceBookRatio) {
		this.priceBookRatio = priceBookRatio;
	}

	public String getAv50DVol() {
		return av50DVol;
	}

	public void setAv50DVol(String av50dVol) {
		av50DVol = av50dVol;
	}

}
