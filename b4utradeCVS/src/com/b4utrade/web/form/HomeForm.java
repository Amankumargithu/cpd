
package com.b4utrade.web.form;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Environment;


/**
 * Form for the home page.
 *
 * @struts.form name="HomeForm"
 */
public class HomeForm extends ActionForm {

   private boolean isLoggedIn = false;

   private String sessionID;
   private String userID;
   private String firstName;
   private String todayDate;
   private String stockList;
   private String scrollDelay;
   private String newsLinks;
   private String referalPartner;

   private String subscriptionURL;
   private String stockSplitsAndBuybacksURL;
   private String streamingWallOfStocksURL;
   private String streamingPortfolioTrackerURL;
   private String optionsSuperStreamerURL;
   private String stocksUpCloseURL;
   private String marketScannerURL;
   private String chartStreamURL;
   private String iposURL;


   /**
    * Standard constructor.
    */
   public HomeForm() {

   }

   /**
    * Gets the isLoggedIn status
    *
    * @return the isLoggedIn flag
    */
   public boolean getIsLoggedIn() {
      return isLoggedIn;
   }

   /**
    * Sets the isLoggedIn flag
    *
    * @param isLoggedIn  boolean
    */
   public void setIsLoggedIn(boolean isLoggedIn) {
      this.isLoggedIn = isLoggedIn;
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
    * Gets the userID value.
    *
    * @return String  userID
    */
   public String getUserID() {
      return userID;
   }

   /**
    * Sets the userID value.
    *
    * @param userID  String
    */
   public void setUserID(String userID) {
      this.userID = userID;
   }

   /**
    * Gets the firstName value.
    *
    * @return String  firstName
    */
   public String getFirstName() {
      return firstName;
   }

   /**
    * Sets the firstName value.
    *
    * @param firstName  String
    */
   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   /**
    * Gets the todayDate value.
    *
    * @return String  todayDate
    */
   public String getTodayDate() {
      return todayDate;
   }

   /**
    * Sets the todayDate value.
    *
    * @param todayDate  String
    */
   public void setTodayDate(String todayDate) {
      this.todayDate = todayDate;
   }

   /**
    * Gets the stockList value.
    *
    * @return String  stockList
    */
   public String getStockList() {
      return stockList;
   }

   /**
    * Sets the stockList value
    *
    * @param stockList  String
    */
   public void setStockList(String stockList) {
      this.stockList = stockList;
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
    * Gets the newsLinks value.
    *
    * @return String  newsLinks
    */
   public String getNewsLinks() {
      return newsLinks;
   }

   /**
    * Sets the newsLinks value.
    *
    * @param newsLinks  String
    */
   public void setNewsLinks(String newsLinks) {
      this.newsLinks = newsLinks;
   }

   /**
    * Gets the referalPartner value.
    *
    * @return String  referalPartner
    */
   public String getReferalPartner() {
      return referalPartner;
   }

   /**
    * Sets the referalPartner value.
    *
    * @param referalPartner  String
    */
   public void setReferalPartner(String referalPartner) {
      this.referalPartner = referalPartner;
   }


   /**
    * Gets the subscriptionURL value.
    *
    * @return String  subscriptionURL
    */
   public String getSubscriptionURL() {
      return subscriptionURL;
   }

   /**
    * Sets the subscriptionURL value.
    *
    * @param subscriptionURL  String
    */
   public void setSubscriptionURL(String subscriptionURL) {
      this.subscriptionURL = subscriptionURL;
   }

   /**
    * Gets the stockSplitsAndBuybacksURL value
    *
    * @return String  stockSplitsAndBuybacksURL
    */
   public String getStockSplitsAndBuybacksURL() {
      return stockSplitsAndBuybacksURL;
   }

   /**
    * Sets the stockSplitsAndBuybacksURL value.
    *
    * @param stockSplitsAndBuybacksURL  String
    */
   public void setStockSplitsAndBuybacksURL(String stockSplitsAndBuybacksURL) {
      this.stockSplitsAndBuybacksURL = stockSplitsAndBuybacksURL;
   }

   /**
    * Gets the streamingWallOfStocksURL value.
    *
    * @return String  streamingWallOfStocksURL
    */
   public String getStreamingWallOfStocksURL() {
      return streamingWallOfStocksURL;
   }

   /**
    * Sets the streamingWallOfStocksURL value.
    *
    * @return String  streamingWallOfStocksURL
    */
   public void setStreamingWallOfStocksURL(String streamingWallOfStocksURL) {
      this.streamingWallOfStocksURL = streamingWallOfStocksURL;
   }

   /**
    * Sets the streamingPortfolioTrackerURL value.
    *
    * @return String  streamingPortfolioTrackerURL
    */
   public void setStreamingPortfolioTrackerURL(String streamingPortfolioTrackerURL) {
      this.streamingPortfolioTrackerURL = streamingPortfolioTrackerURL;
   }

   /**
    * Gets the streamingPortfolioTrackerURL value.
    *
    * @return String  streamingPortfolioTrackerURL
    */
   public String getStreamingPortfolioTrackerURL() {
      return streamingPortfolioTrackerURL;
   }

   /**
    * Sets the optionsSuperStreamerURL value.
    *
    * @return String  optionsSuperStreamerURL
    */
   public void setOptionsSuperStreamerURL(String optionsSuperStreamerURL) {
      this.optionsSuperStreamerURL = optionsSuperStreamerURL;
   }

   /**
    * Gets the optionsSuperStreamerURL value.
    *
    * @return String  optionsSuperStreamerURL
    */
   public String getOptionsSuperStreamerURL() {
      return optionsSuperStreamerURL;
   }

   /**
    * Sets the stocksUpCloseURL value.
    *
    * @return String  stocksUpCloseURL
    */
   public void setStocksUpCloseURL(String stocksUpCloseURL) {
      this.stocksUpCloseURL = stocksUpCloseURL;
   }

   /**
    * Gets the stocksUpCloseURL value.
    *
    * @return String  stocksUpCloseURL
    */
   public String getStocksUpCloseURL() {
      return stocksUpCloseURL;
   }

   /**
    * Sets the marketScannerURL value.
    *
    * @return String  marketScannerURL
    */
   public void setMarketScannerURL(String marketScannerURL) {
      this.marketScannerURL = marketScannerURL;
   }

   /**
    * Gets the marketScannerURL value.
    *
    * @return String  marketScannerURL
    */
   public String getMarketScannerURL() {
      return marketScannerURL;
   }

   /**
    * Sets the chartStreamURL value.
    *
    * @return String  chartStreamURL
    */
   public void setChartStreamURL(String chartStreamURL) {
      this.chartStreamURL = chartStreamURL;
   }

   /**
    * Gets the chartStreamURL value.
    *
    * @return String  chartStreamURL
    */
   public String getChartStreamURL() {
      return chartStreamURL;
   }

   /**
    * Sets the iposURL value.
    *
    * @return String  iposURL
    */
   public void setIposURL(String iposURL) {
      this.iposURL = iposURL;
   }

   /**
    * Gets the iposURL value.
    *
    * @return String  iposURL
    */
   public String getIposURL() {
      return iposURL;
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

      isLoggedIn = false;

      subscriptionURL = "javascript:openWindow('"+Environment.get("B4UTRADE_SECURE_URL") + "/servlet/subscription/B4utradeSubscriptionServlet')";
      //subscriptionURL = "javascript:alert('Coming soon!')";
      stockSplitsAndBuybacksURL = subscriptionURL;
      streamingWallOfStocksURL = subscriptionURL;
      streamingPortfolioTrackerURL = subscriptionURL;
      optionsSuperStreamerURL = subscriptionURL;
      stocksUpCloseURL = subscriptionURL;
      marketScannerURL = subscriptionURL;
      chartStreamURL = subscriptionURL;
      iposURL = subscriptionURL;
   }


}
