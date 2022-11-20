package com.b4utrade.bo;

import java.io.Serializable;

public class MarketScannerBO implements Serializable{

  /**
   *  represents the company id
   */
  private int ID;

  /**
   *  represents the exchange
   */
  private int exchange;

  /**
   *  represents the market cap
   */
  private int marketCapID ;

  /**
   *  represents the industry code
   */
  private int industryCode;

  /**
   *  represents the price
   */
  private double price;

  /**
   *  represents news available for today
   */
  private boolean newsToday;

  /**
   *  represents the ticker for the company
   */
  private String ticker;

  /**
   *  represents the ticker's company name
   */
  private String companyName;

  /**
   *  represents the current volume for the day
   */
  private double volume;

  /**
   *  represents the $ change for the day
   */
  private double dollarChange;

  /**
   *  represents the % change for the day
   */
  private double percentChange;

  /**
   *  a 52 week hi has been reached
   */
  private boolean  fiftyTwoWeekHi;

  /**
   *  a 52 week low has been reached
   */
  private boolean  fiftyTwoWeekLow;

  private String sector;
  
  private String sharesOutStd;
  
  private String fiftyTwoWeekHighValue;
  
  private String fiftyTwoWeekLowValue;
  
  /**
   *  Constructor
   */
  public MarketScannerBO() {
     fiftyTwoWeekHi = false;
     fiftyTwoWeekLow = false;
     newsToday = false;
  }

  /**
   *  Sets the company ID
   *
   *  @param ID int
   */
  public void setID(int ID) {
     this.ID = ID;
  }

  /**
   *  Sets the exchange
   *
   *  @param exchange int
   */
  public void setExchange(int exchange) {
     this.exchange = exchange;
  }

  /**
   *  Sets the market cap
   *
   *  @param market cap int
   */
  public void setMarketCapID(int marketCapID) {
     this.marketCapID = marketCapID;
  }

  /**
   *  Sets the industry code
   *
   *  @param industry code int
   */
  public void setIndustryCode(int industryCode) {
     this.industryCode = industryCode;
  }

  /**
   *  Sets the price
   *
   *  @param price double
   */
  public void setPrice(double price) {
     this.price = price;
  }

  /**
   *  Sets the news today flag
   *
   *  @param newsToday boolean
   */
  public void setNewsToday(boolean newsToday) {
     this.newsToday = newsToday;
  }

  /**
   *  Sets the company ticker
   *
   *  @param ticker String
   */
  public void setTicker(String ticker) {
     this.ticker = ticker;
  }

  /**
   *  Sets the company name
   *
   *  @param name String
   */
  public void setCompanyName(String companyName) {
     this.companyName = companyName;
  }

  /**
   *  Sets the volume
   *
   *  @param volume double
   */
  public void setVolume(double volume) {
     this.volume = volume;
  }

  
  
  public void setSector(String sector){
	  this.sector = sector;
  }
  /**
   *  Sets the $ change
   *
   *  @param dollarChange double
   */
  public void setDollarChange(double dollarChange) {
     this.dollarChange = dollarChange;
  }

  /**
   *  Sets the % change
   *
   *  @param percentChange double
   */
  public void setPercentChange(double percentChange) {
     this.percentChange = percentChange;
  }

  /**
   *  Sets the 52 week hi flag
   *
   *  @param fiftyTwoWeekHi boolean
   */
  public void setFiftyTwoWeekHi(boolean fiftyTwoWeekHi) {
     this.fiftyTwoWeekHi = fiftyTwoWeekHi;
  }

  /**
   *  Sets the 52 week low flag
   *
   *  @param fiftyTwoWeekLow boolean
   */
  public void setFiftyTwoWeekLow(boolean fiftyTwoWeekLow) {
     this.fiftyTwoWeekLow = fiftyTwoWeekLow;
  }

  /**
   *  Gets the company ID
   *
   *  @return int ID
   */
  public int getID() {
     return ID;
  }
  
  public String getSector(){
	  return sector;
  }

  /**
   *  Gets the exchange
   *
   *  @return int exchange
   */
  public int getExchange() {
     return exchange;
  }

  /**
   *  Gets the market cap
   *
   *  @return int marketCapID
   */
  public int getMarketCapID() {
     return marketCapID;
  }

  /**
   *  Gets the industry code
   *
   *  @return int industryCode
   */
  public int getIndustryCode() {
     return industryCode;
  }

  /**
   *  Gets the price
   *
   *  @return double price
   */
  public double getPrice() {
     return price;
  }

  /**
   *  Gets the news today flag
   *
   *  @return boolean newsToday
   */
  public boolean getNewsToday() {
     return newsToday;
  }

  /**
   *  Gets the company ticker
   *
   *  @return String ticker
   */
  public String getTicker() {
     return ticker;
  }

  /**
   *  Gets the company name
   *
   *  @return String name
   */
  public String getCompanyName() {
     return companyName;
  }

  /**
   *  Gets the volume
   *
   *  @return double volume
   */
  public double getVolume() {
     return volume;
  }

  /**
   *  Gets the $ change
   *
   *  @return double dollarChange
   */
  public double getDollarChange() {
     return dollarChange;
  }

  /**
   *  Gets the % Change
   *
   *  @return double percentChange
   */
  public double getPercentChange() {
     return percentChange;
  }

  /**
   *  Gets the 52 week hi flag
   *
   *  @return boolean fiftyTwoWeekHi
   */
  public boolean getFiftyTwoWeekHi() {
     return fiftyTwoWeekHi;
  }

  /**
   *  Gets the 52 week low flag
   *
   *  @return boolean fiftyTwoWeekLow
   */
  public boolean getFiftyTwoWeekLow() {
     return fiftyTwoWeekLow;
  }

public String getSharesOutStd() {
	return sharesOutStd;
}

public void setSharesOutStd(String sharesOutStd) {
	this.sharesOutStd = sharesOutStd;
}

public String getFiftyTwoWeekHighValue() {
	return fiftyTwoWeekHighValue;
}

public void setFiftyTwoWeekHighValue(String fiftyTwoWeekHighValue) {
	this.fiftyTwoWeekHighValue = fiftyTwoWeekHighValue;
}

public String getFiftyTwoWeekLowValue() {
	return fiftyTwoWeekLowValue;
}

public void setFiftyTwoWeekLowValue(String fiftyTwoWeekLowValue) {
	this.fiftyTwoWeekLowValue = fiftyTwoWeekLowValue;
}

@Override
 public String toString(){
	 StringBuilder builder = new StringBuilder();
	 builder.append("Ticker is :");
	 builder.append(ticker);
	 builder.append("   exchange is : ");
	 builder.append(exchange);
	 builder.append("   marketCapID is : ");
	 builder.append(marketCapID);
	 builder.append("   industryCode is : ");
	 builder.append(industryCode);
	 builder.append("   price is : ");
	 builder.append(price);
	 builder.append("   newsToday is : ");
	 builder.append(newsToday);
	 builder.append("   companyName is : ");
	 builder.append(companyName);
	 builder.append("   volume is : ");
	 builder.append(volume);
	 builder.append("   dollarChange is : ");
	 builder.append(dollarChange);
	 builder.append("   percentChange is : ");
	 builder.append(percentChange);
	 builder.append("   fiftyTwoWeekHi is : ");
	 builder.append(fiftyTwoWeekHi);
	 builder.append("   fiftyTwoWeekLow is : ");
	 builder.append(fiftyTwoWeekLow);
	 builder.append("   sector is : ");
	 builder.append(sector);
	 builder.append("   sharesOutStd is : ");
	 builder.append(sharesOutStd);
	 builder.append("   fiftyTwoWeekHighValue is : ");
	 builder.append(fiftyTwoWeekHighValue);
	 builder.append("   fiftyTwoWeekLowValue is : ");
	 builder.append(fiftyTwoWeekLowValue);
	 return builder.toString();
 }

}
