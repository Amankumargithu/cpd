
package com.b4utrade.web.form;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Environment;


/**
 * Form for the stock up close page.
 *
 * @struts.form name="StockUpCloseForm"
 */
public class StockUpCloseForm extends ActionForm {

   //private boolean isLoggedIn = false;

   private String sessionID;

   private String lookupTicker;
   private String tickerName;
   private String companyName;
   private String exchange;
   private String sector;
   private int industryID;   
   private String industry;

   private int    newsID;
   private String newsHeadline;
   private String newsSource;
   private String newsDate;
   
   private String stockYearHi;
   private String stockYearLo;
   private String stockEPS;
   private String stockPE;
   private String stockAvgDayVol;
   private String stockDiv;
   private String stockYield;
   private String stockMktCap;
   
   private String earningDate;
   private String firstCallEstimate;

   private String latestAnalystRating;
   private String latestAnalystRatingDate;
   private String latestAnalystBroker;
   private String latestAnalystFirm;
   private String latestAnalystRatingTo;
   private String ratingOrCommentFlag;
   private String ratingComment;
   private String nonRatingComment;
   private String upDowngradeFlag;
   private String commentFlag;
   private String brokerageID;
   
   private String nextConfCall;
   private String confCallListenLink;
   private boolean confCallListenFlag;   
   
   private String splitNewsDate;
   private String buybackNewsDate;
   
   private String stockSummaryLink;

   /**
    * Standard constructor.
    */
   public StockUpCloseForm() {

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
    * Gets the lookup ticker value.
    *
    * @return String  lookupTicker
    */
   public String getLookupTicker() {
      return lookupTicker;
   }

   /**
    * Sets the lookup ticker.
    *
    * @param lookupTicker  String
    */
   public void setLookupTicker(String lookupTicker) {
      this.lookupTicker = lookupTicker;
   }

   /**
    * Gets the ticker value.
    *
    * @return String  tickerName
    */
   public String getTickerName() {
      return tickerName;
   }

   /**
    * Sets the ticker value.
    *
    * @param tickerName  String
    */
   public void setTickerName(String tickerName) {
      this.tickerName = tickerName;
   }

   /**
    * Gets the company value.
    *
    * @return String  companyName
    */
   public String getCompanyName() {
      return companyName;
   }

   /**
    * Sets the company value.
    *
    * @param companyName  String
    */
   public void setCompanyName(String companyName) {
      this.companyName = companyName;
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
    * Gets the sector value.
    *
    * @return String  sector
    */
   public String getSector() {
      return sector;
   }

   /**
    * Sets the sector value.
    *
    * @param sector  String
    */
   public void setSector(String sector) {
      this.sector = sector;
   }

   /**
    * Gets the industry id value.
    *
    * @return int  industryID
    */
   public int getIndustryID() {
      return industryID;
   }

   /**
    * Sets the industry id value.
    *
    * @param industryID  int
    */
   public void setIndustryID(int industryID) {
      this.industryID = industryID;
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
    * Gets the newsID value.
    *
    * @return int newsID
    */
   public int getNewsID() {
      return newsID;
   }

   /**
    * Sets the newsID value.
    *
    * @param newsID  int
    */
   public void setNewsID(int newsID) {
      this.newsID = newsID;
   }

   /**
    * Gets the newsHeadline value.
    *
    * @return String  newsHeadline
    */
   public String getNewsHeadline() {
      return newsHeadline;
   }

   /**
    * Sets the newsHeadline value.
    *
    * @param newsHeadline  String
    */
   public void setNewsHeadline(String newsHeadline) {
      this.newsHeadline = newsHeadline;
   }

   /**
    * Gets the newsSource value.
    *
    * @return String  newsSource
    */
   public String getNewsSource() {
      return newsSource;
   }

   /**
    * Sets the newsSource value.
    *
    * @param newsSource  String
    */
   public void setNewsSource(String newsSource) {
      this.newsSource = newsSource;
   }

   /**
    * Gets the newsDate value.
    *
    * @return String  newsDate
    */
   public String getNewsDate() {
      return newsDate;
   }

   /**
    * Sets the newsDate value.
    *
    * @param newsDate  String
    */
   public void setNewsDate(String newsDate) {
      this.newsDate = newsDate;
   }

   /**
    * Gets the stockYearHi value.
    *
    * @return String  stockYearHi
    */
   public String getStockYearHi() {
      return stockYearHi;
   }

   /**
    * Sets the stockYearHi value
    *
    * @param stockYearHi  String
    */
   public void setStockYearHi(String stockYearHi) {
      this.stockYearHi = stockYearHi;
   }

   /**
    * Gets the stockYearLo value.
    *
    * @return String  stockYearLo
    */
   public String getStockYearLo() {
      return stockYearLo;
   }

   /**
    * Sets the stockYearLo value
    *
    * @param stockYearLo  String
    */
   public void setStockYearLo(String stockYearLo) {
      this.stockYearLo = stockYearLo;
   }

   /**
    * Gets the stockEPS value.
    *
    * @return String  stockEPS
    */
   public String getStockEPS() {
      return stockEPS;
   }

   /**
    * Sets the stockEPS value.
    *
    * @param stockEPS  String
    */
   public void setStockEPS(String stockEPS) {
      this.stockEPS = stockEPS;
   }

   /**
    * Gets the stockPE value.
    *
    * @return String  stockPE
    */
   public String getStockPE() {
      return stockPE;
   }

   /**
    * Sets the stockPE value.
    *
    * @param stockPE  String
    */
   public void setStockPE(String stockPE) {
      this.stockPE = stockPE;
   }

   /**
    * Gets the stockAvgDayVol value.
    *
    * @return String  stockAvgDayVol
    */
   public String getStockAvgDayVol() {
      return stockAvgDayVol;
   }

   /**
    * Sets the stockAvgDayVol value.
    *
    * @param stockAvgDayVol  String
    */
   public void setStockAvgDayVol(String stockAvgDayVol) {
      this.stockAvgDayVol = stockAvgDayVol;
   }


   /**
    * Gets the stockDiv value.
    *
    * @return String  stockDiv
    */
   public String getStockDiv() {
      return stockDiv;
   }

   /**
    * Sets the stockDiv value.
    *
    * @param stockDiv  String
    */
   public void setStockDiv(String stockDiv) {
      this.stockDiv = stockDiv;
   }

   /**
    * Gets the stockYield value
    *
    * @return String  stockYield
    */
   public String getStockYield() {
      return stockYield;
   }

   /**
    * Sets the stockYield value.
    *
    * @param stockYield  String
    */
   public void setStockYield(String stockYield) {
      this.stockYield = stockYield;
   }

   /**
    * Gets the stockMktCap value.
    *
    * @return String  stockMktCap
    */
   public String getStockMktCap() {
      return stockMktCap;
   }

   /**
    * Sets the stockMktCap value.
    *
    * @return String  stockMktCap
    */
   public void setStockMktCap(String stockMktCap) {
      this.stockMktCap = stockMktCap;
   }


   /**
    * Gets the latest analyst rating value.
    *
    * @return String  latestAnalystRating
    */
   public String getLatestAnalystRating() {
      return latestAnalystRating;
   }

   /**
    * Sets the latest analyst rating value.
    *
    * @param latestAnalystRating String
    */
   public void setLatestAnalystRating(String latestAnalystRating) {
      this.latestAnalystRating = latestAnalystRating;
   }

   /**
    * Gets the latest analyst rating date value.
    *
    * @return String  latestAnalystRatingDate
    */
   public String getLatestAnalystRatingDate() {
      return latestAnalystRatingDate;
   }

   /**
    * Sets the latest analyst rating date value.
    *
    * @param latestAnalystRatingDate String
    */
   public void setLatestAnalystRatingDate(String latestAnalystRatingDate) {
      this.latestAnalystRatingDate = latestAnalystRatingDate;
   }

   /**
    * Gets the latest analyst broker value.
    *
    * @return String  latestAnalystBroker
    */
   public String getLatestAnalystBroker() {
      return latestAnalystBroker;
   }

   /**
    * Sets the latest analyst broker value.
    *
    * @param latestAnalystBroker String
    */
   public void setLatestAnalystBroker(String latestAnalystBroker) {
      this.latestAnalystBroker = latestAnalystBroker;
   }

   /**
    * Gets the latest analyst firm value.
    *
    * @return String  latestAnalystFirm
    */
   public String getLatestAnalystFirm() {
      return latestAnalystFirm;
   }

   /**
    * Sets the latest analyst firm value.
    *
    * @param latestAnalystFirm String
    */
   public void setLatestAnalystFirm(String latestAnalystFirm) {
      this.latestAnalystFirm = latestAnalystFirm;
   }

   /**
    * Gets the latest analyst rating to value.
    *
    * @return String  latestAnalystRatingTo
    */
   public String getLatestAnalystRatingTo() {
      return latestAnalystRatingTo;
   }

   /**
    * Sets the latest analyst rating to value.
    *
    * @param latestAnalystRatingTo String
    */
   public void setLatestAnalystRatingTo(String latestAnalystRatingTo) {
      this.latestAnalystRatingTo = latestAnalystRatingTo;
   }

   /**
    * Gets the rating Or comment flag value.
    *
    * @return String  ratingOrCommentFlag
    */
   public String getRatingOrCommentFlag() {
      return ratingOrCommentFlag;
   }

   /**
    * Sets the rating Or comment flag value.
    *
    * @param ratingOrCommentFlag String
    */
   public void setRatingOrCommentFlag(String ratingOrCommentFlag) {
      this.ratingOrCommentFlag = ratingOrCommentFlag;
   }

   /**
    * Gets the rating comment value.
    *
    * @return String  ratingComment
    */
   public String getRatingComment() {
      return ratingComment;
   }

   /**
    * Sets the rating comment value.
    *
    * @param ratingComment String
    */
   public void setRatingComment(String ratingComment) {
      this.ratingComment = ratingComment;
   }

   /**
    * Gets the non rating comment value.
    *
    * @return String  nonRatingComment
    */
   public String getNonRatingComment() {
      return nonRatingComment;
   }

   /**
    * Sets the non rating comment value.
    *
    * @param nonRatingComment String
    */
   public void setNonRatingComment(String nonRatingComment) {
      this.nonRatingComment = nonRatingComment;
   }

   /**
    * Gets the upgrade downgrade Flag value.
    *
    * @return String  upDowngradeFlag
    */
   public String getUpDowngradeFlag() {
      return upDowngradeFlag;
   }

   /**
    * Sets the upgrade downgrade flag value.
    *
    * @param upDowngradeFlag  String
    */
   public void setUpDowngradeFlag(String upDowngradeFlag) {
      this.upDowngradeFlag = upDowngradeFlag;
   }

   /**
    * Gets the comment Flag value.
    *
    * @return String  commentFlag
    */
   public String getCommentFlag() {
      return commentFlag;
   }

   /**
    * Sets the comment Flag value.
    *
    * @param commentFlag  String
    */
   public void setCommentFlag(String commentFlag) {
      this.commentFlag = commentFlag;
   }

   /**
    * Gets the brokerage ID.
    *
    * @return String  brokerageID
    */
   public String getBrokerageID() {
      return brokerageID;
   }

   /**
    * Sets the brokerage ID.
    *
    * @param brokerageID  String
    */
   public void setBrokerageID(String brokerageID) {
      this.brokerageID = brokerageID;
   }

   /**
    * Gets the next conf call.
    *
    * @return String  nextConfCall
    */
   public String getNextConfCall() {
      return nextConfCall;
   }

   /**
    * Sets the next conf call.
    *
    * @param nextConfCall  String
    */
   public void setNextConfCall(String nextConfCall) {
      this.nextConfCall = nextConfCall;
   }


   /**
    * Gets the conf call listen link value.
    *
    * @return String  confCallListenLink
    */
   public String getConfCallListenLink() {
      return confCallListenLink;
   }

   /**
    * Sets the conf call listen link value.
    *
    * @param confCallListenLink  String
    */
   public void setConfCallListenLink(String confCallListenLink) {
      this.confCallListenLink = confCallListenLink;
   }

   /**
    * Gets the conf call listen flag value.
    *
    * @return boolean  confCallListenFlag
    */
   public boolean getConfCallListenFlag() {
      return confCallListenFlag;
   }

   /**
    * Sets the conf call listen flag value.
    *
    * @param confCallListenFlag  boolean
    */
   public void setConfCallListenFlag(boolean confCallListenFlag) {
      this.confCallListenFlag = confCallListenFlag;
   }

   /**
    * Gets the earning date value.
    *
    * @return String  earningDate
    */
   public String getEarningDate() {
      return earningDate;
   }

   /**
    * Sets the earning date value.
    *
    * @param earningDate  String
    */
   public void setEarningDate(String earningDate) {
      this.earningDate = earningDate;
   }

   /**
    * Gets the first call estimate value.
    *
    * @return String  firstCallEstimate
    */
   public String getFirstCallEstimate() {
      return firstCallEstimate;
   }

   /**
    * Sets the first call estimate value.
    *
    * @param firstCallEstimate  String
    */
   public void setFirstCallEstimate(String firstCallEstimate) {
      this.firstCallEstimate = firstCallEstimate;
   }

   /**
    * Gets the split news date value.
    *
    * @return String  splitNewsDate
    */
   public String getSplitNewsDate() {
      return splitNewsDate;
   }

   /**
    * Sets the split news date value.
    *
    * @param splitNewsDate  String
    */
   public void setSplitNewsDate(String splitNewsDate) {
      this.splitNewsDate = splitNewsDate;
   }

   /**
    * Gets the buyback news date value.
    *
    * @return String  buybackNewsDate
    */
   public String getBuybackNewsDate() {
      return buybackNewsDate;
   }

   /**
    * Sets the buyback news date value.
    *
    * @param buybackNewsDate  String
    */
   public void setBuybackNewsDate(String buybackNewsDate) {
      this.buybackNewsDate = buybackNewsDate;
   }

   /**
    * Gets the stock summary link.
    *
    * @return String  stockSummaryLink
    */
   public String getStockSummaryLink() {
      return stockSummaryLink;
   }

   /**
    * Sets the stock summary link.
    *
    * @param stockSummaryLink  String
    */
   public void setStockSummaryLink(String stockSummaryLink) {
      this.stockSummaryLink = stockSummaryLink;
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


   }


}
