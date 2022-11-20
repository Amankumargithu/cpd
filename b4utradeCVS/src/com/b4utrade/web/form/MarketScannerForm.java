
package com.b4utrade.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Form for the market scanner page.
 *
 * @struts.form name="marketScannerForm"
 */
public class MarketScannerForm extends ActionForm {

   public static final String DEFAULT_INDUSTRY_VALUE="INDUSTRY_0";
   public static final String DEFAULT_EXCHANGE_VALUE="EXCHANGE_0";
   public static final String DEFAULT_MKTCAP_VALUE = "MKTCAP_0";
   public static final String DEFAULT_TOPTEN_VALUE = "TOPTEN_1";
   public static final String DEFAULT_PRICE_VALUE = "PRICE_0";


   private String industry;
   private String exchange;
   private String price;
   private String marketCap;
   private String topTen;
   private String tickers;
   private String companies;
   private String windowLocation;
   private String sessionID;
   private String scrollDelay;


   /**
    * Standard constructor.
    */
   public MarketScannerForm() {

   }

   /**
    * Gets the industry value.
    *
    * @return String  industry
    */
   public String getIndustry() {
      return industry;
   }

   /**
    * Sets the industry value.
    *
    * @param industry  String
    */
   public void setIndustry(String industry) {
      this.industry = industry;
   }

   /**
    * Gets the exchange value.
    *
    * @return String  exchange
    */
   public String getExchange() {
      return exchange;
   }

   /**
    * Sets the exchange value.
    *
    * @param exchange  String
    */
   public void setExchange(String exchange) {
      this.exchange = exchange;
   }

   /**
    * Gets the price value.
    *
    * @return String  price
    */
   public String getPrice() {
      return price;
   }

   /**
    * Sets the price value.
    *
    * @param price  String
    */
   public void setPrice(String price) {
      this.price = price;
   }

   /**
    * Gets the marketCap value.
    *
    * @return String  marketCap
    */
   public String getMarketCap() {
      return marketCap;
   }

   /**
    * Sets the marketCap value.
    *
    * @param marketCap  String
    */
   public void setMarketCap(String marketCap) {
      this.marketCap = marketCap;
   }

   /**
    * Gets the topTen value.
    *
    * @return String  topTen
    */
   public String getTopTen() {
      return topTen;
   }

   /**
    * Sets the topTen value.
    *
    * @param topTen  String
    */
   public void setTopTen(String topTen) {
      this.topTen = topTen;
   }

   /**
    * Gets the tickers value.
    *
    * @return String  tickers
    */
   public String getTickers() {
      return tickers;
   }

   /**
    * Sets the tickers value.
    *
    * @param tickers  String
    */
   public void setTickers(String tickers) {
      this.tickers = tickers;
   }

   /**
    * Gets the companies value.
    *
    * @return String  companies
    */
   public String getCompanies() {
      return companies;
   }

   /**
    * Sets the companies value.
    *
    * @param companies  String
    */
   public void setCompanies(String companies) {
      this.companies = companies;
   }

   /**
    * Gets the windowLocation value.
    *
    * @return String  windowLocation
    */
   public String getWindowLocation() {
      return windowLocation;
   }

   /**
    * Sets the windowLocation value.
    *
    * @param windowLocation  String
    */
   public void setWindowLocation(String windowLocation) {
      this.windowLocation = windowLocation;
   }

   /**
    * Gets the sessionID value.
    *
    * @return String  sessionID
    */
   public String getSessionID() {
      return sessionID;
   }

   /**
    * Sets the sessionID value.
    *
    * @param sessionID  String
    */
   public void setSessionID(String sessionID) {
      this.sessionID = sessionID;
   }

   /**
    * Gets the scrollDelay value.
    *
    * @return String  scrollDelay
    */
   public String getScrollDelay() {
      return scrollDelay;
   }

   /**
    * Sets the scrollDelay value.
    *
    * @param scrollDelay  String
    */
   public void setScrollDelay(String scrollDelay) {
      this.scrollDelay = scrollDelay;
   }



   /**
    * Validate the properties that have been set from this HTTP request,
    * and return an <code>ActionErrors</code> object that encapsulates any
    * validation errors that have been found.  If no errors are found, return
    * <code>null</code> or an <code>ActionErrors</code> object with no
    * recorded error messages.
    *
    * @param mapping The mapping used to select this instance
    * @param request The servlet request we are processing
    */
   public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
      ActionErrors errors = new ActionErrors();
      return errors;
   }

   /**
    * Reset all properties to their default values.
    *
    * @param mapping The mapping used to select this instance
    * @param request The servlet request we are processing
    */
   public void reset(ActionMapping mapping, HttpServletRequest request) {

      industry = DEFAULT_INDUSTRY_VALUE;
      exchange = DEFAULT_EXCHANGE_VALUE;
      price = DEFAULT_PRICE_VALUE;
      marketCap = DEFAULT_MKTCAP_VALUE;
      topTen = DEFAULT_TOPTEN_VALUE;
      tickers = "";
      companies = "";
      sessionID = "";
      scrollDelay = "";
      windowLocation = "";

   }

}
