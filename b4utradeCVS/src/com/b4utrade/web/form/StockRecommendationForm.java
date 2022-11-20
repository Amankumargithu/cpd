
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
 * @struts.form name="StockRecommendationForm"
 */
public class StockRecommendationForm extends ActionForm {

   //private boolean isLoggedIn = false;

   //private String recordNumber;
   private String consensusRecommendation;
   private String fiscalYearEndMonth;
   private String lastModified;

   private String currentBuy;
   private String oneMonthAgoBuy;
   private String twoMonthsAgoBuy;
   private String oneYearAgoBuy;

   private String currentOutperform;
   private String oneMonthAgoOutperform;
   private String twoMonthsAgoOutperform;
   private String oneYearAgoOutperform;
   
   private String currentHold;
   private String oneMonthAgoHold;
   private String twoMonthsAgoHold;
   private String oneYearAgoHold;
   
   private String currentUnderperform;
   private String oneMonthAgoUnderperform;
   private String twoMonthsAgoUnderperform;
   private String oneYearAgoUnderperform;
   
   private String currentSell;
   private String oneMonthAgoSell;
   private String twoMonthsAgoSell;
   private String oneYearAgoSell;
   
   private String currentNoOpinion;
   private String oneMonthAgoNoOpinion;
   private String twoMonthsAgoNoOpinion;
   private String oneYearAgoNoOpinion;

   private String currentMeanRating;
   private String oneMonthAgoMeanRating;
   private String twoMonthsAgoMeanRating;
   private String oneYearAgoMeanRating;

   //private String coverageBroker;
   private String tickerName;

   /**
    * Standard constructor.
    */
   public StockRecommendationForm() {
      //recordNumber ="";
      consensusRecommendation = "";
      fiscalYearEndMonth = "";
      lastModified = "";
      currentBuy ="";
      oneMonthAgoBuy ="";
      twoMonthsAgoBuy ="";
      oneYearAgoBuy ="";
      currentOutperform ="";
      oneMonthAgoOutperform ="";
      twoMonthsAgoOutperform ="";
      oneYearAgoOutperform ="";
      currentHold ="";
      oneMonthAgoHold ="";
      twoMonthsAgoHold ="";
      oneYearAgoHold ="";
      currentUnderperform ="";
      oneMonthAgoUnderperform ="";
      twoMonthsAgoUnderperform ="";
      oneYearAgoUnderperform ="";
      currentSell ="";
      oneMonthAgoSell ="";
      twoMonthsAgoSell ="";
      oneYearAgoSell ="";
      currentNoOpinion ="";
      oneMonthAgoNoOpinion ="";
      twoMonthsAgoNoOpinion ="";
      oneYearAgoNoOpinion ="";
      currentMeanRating ="";
      oneMonthAgoMeanRating ="";
      twoMonthsAgoMeanRating ="";
      oneYearAgoMeanRating ="";
   }

   /**
    * Gets the current Buy value.
    *
    * @return String  currentBuy
    */
   public String getCurrentBuy() {
      return currentBuy;
   }

   /**
    * Sets the current Buy value.
    *
    * @param currentBuy  String
    */
   public void setCurrentBuy(String currentBuy) {
      this.currentBuy = currentBuy;
   }

   /**
    * Gets the one Month Ago Buy value.
    *
    * @return String  oneMonthAgoBuy
    */
   public String getOneMonthAgoBuy() {
      return oneMonthAgoBuy;
   }

   /**
    * Sets the one Month Ago Buy value.
    *
    * @param oneMonthAgoBuy  String
    */
   public void setOneMonthAgoBuy(String oneMonthAgoBuy) {
      this.oneMonthAgoBuy = oneMonthAgoBuy;
   }

   /**
    * Gets the Two Months Ago Buy value.
    *
    * @return String  twoMonthsAgoBuy
    */
   public String getTwoMonthsAgoBuy() {
      return twoMonthsAgoBuy;
   }

   /**
    * Sets the Two Months Ago Buy value.
    *
    * @param twoMonthsAgoBuy  String
    */
   public void setTwoMonthsAgoBuy(String twoMonthsAgoBuy) {
      this.twoMonthsAgoBuy = twoMonthsAgoBuy;
   }

   /**
    * Gets the One Year Ago Buy value.
    *
    * @return String  oneYearAgoBuy
    */
   public String getOneYearAgoBuy() {
      return oneYearAgoBuy;
   }

   /**
    * Sets the One Year Ago Buy value.
    *
    * @param oneYearAgoBuy  String
    */
   public void setOneYearAgoBuy(String oneYearAgoBuy) {
      this.oneYearAgoBuy = oneYearAgoBuy;
   }


   /**
    * Gets the Current Outperform value.
    *
    * @return String  currentOutperform
    */
   public String getCurrentOutperform() {
      return currentOutperform;
   }

   /**
    * Sets the Current Outperform value.
    *
    * @param currentOutperform  String
    */
   public void setCurrentOutperform(String currentOutperform) {
      this.currentOutperform = currentOutperform;
   }

   /**
    * Gets the One Month Ago Outperform value.
    *
    * @return String oneMonthAgoOutperform
    */
   public String getOneMonthAgoOutperform() {
      return oneMonthAgoOutperform;
   }

   /**
    * Sets the One Month Ago Outperform value.
    *
    * @param oneMonthAgoOutperform  String
    */
   public void setOneMonthAgoOutperform(String oneMonthAgoOutperform) {
      this.oneMonthAgoOutperform = oneMonthAgoOutperform;
   }

   /**
    * Gets the Two Months Ago Outperform value.
    *
    * @return String  twoMonthsAgoOutperform
    */
   public String getTwoMonthsAgoOutperform() {
      return twoMonthsAgoOutperform;
   }

   /**
    * Sets the Two Months Ago Outperform value.
    *
    * @param twoMonthsAgoOutperform  String
    */
   public void setTwoMonthsAgoOutperform(String twoMonthsAgoOutperform) {
      this.twoMonthsAgoOutperform = twoMonthsAgoOutperform;
   }

   /**
    * Gets the One Year Ago Outperform value.
    *
    * @return String  oneYearAgoOutperform
    */
   public String getOneYearAgoOutperform() {
      return oneYearAgoOutperform;
   }

   /**
    * Sets the One Year Ago Outperform value.
    *
    * @param oneYearAgoOutperform  String
    */
   public void setOneYearAgoOutperform(String oneYearAgoOutperform) {
      this.oneYearAgoOutperform = oneYearAgoOutperform;
   }

   /**
    * Gets the Current Hold value.
    *
    * @return String  currentHold
    */
   public String getCurrentHold() {
      return currentHold;
   }

   /**
    * Sets the Current Hold value.
    *
    * @param currentHold  String
    */
   public void setCurrentHold(String currentHold) {
      this.currentHold = currentHold;
   }

   /**
    * Gets the One Month Ago Hold value.
    *
    * @return String  oneMonthAgoHold
    */
   public String getOneMonthAgoHold() {
      return oneMonthAgoHold;
   }

   /**
    * Sets the One Month Ago Hold value
    *
    * @param oneMonthAgoHold  String
    */
   public void setOneMonthAgoHold(String oneMonthAgoHold) {
      this.oneMonthAgoHold = oneMonthAgoHold;
   }

   /**
    * Gets the Two Months Ago Hold value.
    *
    * @return String  twoMonthsAgoHold
    */
   public String getTwoMonthsAgoHold() {
      return twoMonthsAgoHold;
   }

   /**
    * Sets the Two Months Ago Hold value
    *
    * @param twoMonthsAgoHold  String
    */
   public void setTwoMonthsAgoHold(String twoMonthsAgoHold) {
      this.twoMonthsAgoHold = twoMonthsAgoHold;
   }

   /**
    * Gets the One Year Ago Hold value.
    *
    * @return String  oneYearAgoHold
    */
   public String getOneYearAgoHold() {
      return oneYearAgoHold;
   }

   /**
    * Sets the One Year Ago Hold value.
    *
    * @param oneYearAgoHold  String
    */
   public void setOneYearAgoHold(String oneYearAgoHold) {
      this.oneYearAgoHold = oneYearAgoHold;
   }

   /**
    * Gets the Current Underperform value.
    *
    * @return String  currentUnderperform
    */
   public String getCurrentUnderperform() {
      return currentUnderperform;
   }

   /**
    * Sets the Current Underperform value.
    *
    * @param currentUnderperform  String
    */
   public void setCurrentUnderperform(String currentUnderperform) {
      this.currentUnderperform = currentUnderperform;
   }

   /**
    * Gets the One Month Ago Underperform value.
    *
    * @return String  oneMonthAgoUnderperform
    */
   public String getOneMonthAgoUnderperform() {
      return oneMonthAgoUnderperform;
   }

   /**
    * Sets the One Month Ago Underperform value.
    *
    * @param oneMonthAgoUnderperform  String
    */
   public void setOneMonthAgoUnderperform(String oneMonthAgoUnderperform) {
      this.oneMonthAgoUnderperform = oneMonthAgoUnderperform;
   }


   /**
    * Gets the Two Months Ago Underperform value.
    *
    * @return String  twoMonthsAgoUnderperform
    */
   public String getTwoMonthsAgoUnderperform() {
      return twoMonthsAgoUnderperform;
   }

   /**
    * Sets the Two Months Ago Underperform value.
    *
    * @param twoMonthsAgoUnderperform  String
    */
   public void setTwoMonthsAgoUnderperform(String twoMonthsAgoUnderperform) {
      this.twoMonthsAgoUnderperform = twoMonthsAgoUnderperform;
   }

   /**
    * Gets the One Year Ago Underperform value
    *
    * @return String  oneYearAgoUnderperform
    */
   public String getOneYearAgoUnderperform() {
      return oneYearAgoUnderperform;
   }

   /**
    * Sets the One Year Ago Underperform value.
    *
    * @param oneYearAgoUnderperform  String
    */
   public void setOneYearAgoUnderperform(String oneYearAgoUnderperform) {
      this.oneYearAgoUnderperform = oneYearAgoUnderperform;
   }

   /**
    * Gets the Current Sell value.
    *
    * @return String  currentSell
    */
   public String getCurrentSell() {
      return currentSell;
   }

   /**
    * Sets the Current Sell value.
    *
    * @return String  currentSell
    */
   public void setCurrentSell(String currentSell) {
      this.currentSell = currentSell;
   }


   /**
    * Gets the One Month Ago Sell value.
    *
    * @return String  oneMonthAgoSell
    */
   public String getOneMonthAgoSell() {
      return oneMonthAgoSell;
   }

   /**
    * Sets the One Month Ago Sell value.
    *
    * @param oneMonthAgoSell String
    */
   public void setOneMonthAgoSell(String oneMonthAgoSell) {
      this.oneMonthAgoSell = oneMonthAgoSell;
   }

   /**
    * Gets the Two Months Ago Sell value.
    *
    * @return String  twoMonthsAgoSell
    */
   public String getTwoMonthsAgoSell() {
      return twoMonthsAgoSell;
   }

   /**
    * Sets the Two Months Ago Sell value.
    *
    * @param twoMonthsAgoSell String
    */
   public void setTwoMonthsAgoSell(String twoMonthsAgoSell) {
      this.twoMonthsAgoSell = twoMonthsAgoSell;
   }

   /**
    * Gets the One Year Ago Sell value.
    *
    * @return String  oneYearAgoSell
    */
   public String getOneYearAgoSell() {
      return oneYearAgoSell;
   }

   /**
    * Sets the One Year Ago Sell value.
    *
    * @param oneYearAgoSell String
    */
   public void setOneYearAgoSell(String oneYearAgoSell) {
      this.oneYearAgoSell = oneYearAgoSell;
   }

   /*****************************
    *         No Opinion        *
    *****************************/
   /**
    * Gets the Current No Opinion value.
    *
    * @return String  currentNoOpinion
    */
   public String getCurrentNoOpinion() {
      return currentNoOpinion;
   }

   /**
    * Sets the Current No Opinion value.
    *
    * @param currentNoOpinion String
    */
   public void setCurrentNoOpinion(String currentNoOpinion) {
      this.currentNoOpinion = currentNoOpinion;
   }

   /**
    * Gets the One Month Ago No Opinion value.
    *
    * @return String  oneMonthAgoNoOpinion
    */
   public String getOneMonthAgoNoOpinion() {
      return oneMonthAgoNoOpinion;
   }

   /**
    * Sets the One Month Ago No Opinion value.
    *
    * @param oneMonthAgoNoOpinion String
    */
   public void setOneMonthAgoNoOpinion(String oneMonthAgoNoOpinion) {
      this.oneMonthAgoNoOpinion = oneMonthAgoNoOpinion;
   }

   /**
    * Gets the Two Months Ago No Opinion value.
    *
    * @return String  twoMonthsAgoNoOpinion
    */
   public String getTwoMonthsAgoNoOpinion() {
      return twoMonthsAgoNoOpinion;
   }

   /**
    * Sets the Two Months Ago No Opinion value.
    *
    * @param twoMonthsAgoNoOpinion String
    */
   public void setTwoMonthsAgoNoOpinion(String twoMonthsAgoNoOpinion) {
      this.twoMonthsAgoNoOpinion = twoMonthsAgoNoOpinion;
   }

   /**
    * Gets the One Year Ago No Opinion value.
    *
    * @return String  oneYearAgoNoOpinion
    */
   public String getOneYearAgoNoOpinion() {
      return oneYearAgoNoOpinion;
   }

   /**
    * Sets the One Year Ago No Opinion value.
    *
    * @param oneYearAgoNoOpinion String
    */
   public void setOneYearAgoNoOpinion(String oneYearAgoNoOpinion) {
      this.oneYearAgoNoOpinion = oneYearAgoNoOpinion;
   }

   /*****************************
    *        Mean Rating        *
    *****************************/
   /**
    * Gets the Current Mean Rating value.
    *
    * @return String  currentMeanRating
    */
   public String getCurrentMeanRating() {
      return currentMeanRating;
   }

   /**
    * Sets the Current Mean Rating value.
    *
    * @param currentMeanRating String
    */
   public void setCurrentMeanRating(String currentMeanRating) {
      this.currentMeanRating = currentMeanRating;
   }

   /**
    * Gets the One Month Ago Mean Rating value.
    *
    * @return String  oneMonthAgoMeanRating
    */
   public String getOneMonthAgoMeanRating() {
      return oneMonthAgoMeanRating;
   }

   /**
    * Sets the One Month Ago Mean Rating value.
    *
    * @param oneMonthAgoMeanRating String
    */
   public void setOneMonthAgoMeanRating(String oneMonthAgoMeanRating) {
      this.oneMonthAgoMeanRating = oneMonthAgoMeanRating;
   }

   /**
    * Gets the Two Months Ago Mean Rating value.
    *
    * @return String  twoMonthsAgoMeanRating
    */
   public String getTwoMonthsAgoMeanRating() {
      return twoMonthsAgoMeanRating;
   }

   /**
    * Sets the Two Months Ago Mean Rating value.
    *
    * @param twoMonthsAgoMeanRating String
    */
   public void setTwoMonthsAgoMeanRating(String twoMonthsAgoMeanRating) {
      this.twoMonthsAgoMeanRating = twoMonthsAgoMeanRating;
   }

   /**
    * Gets the One Year Ago Mean Rating value.
    *
    * @return String  oneYearAgoMeanRating
    */
   public String getOneYearAgoMeanRating() {
      return oneYearAgoMeanRating;
   }

   /**
    * Sets the One Year Ago Mean Rating value.
    *
    * @param oneYearAgoMeanRating String
    */
   public void setOneYearAgoMeanRating(String oneYearAgoMeanRating) {
      this.oneYearAgoMeanRating = oneYearAgoMeanRating;
   }


   /**
    * Gets the Consensus Recommendation.
    *
    * @return String  consensusRecommendation
    */
   public String getConsensusRecommendation() {
      return consensusRecommendation;
   }

   /**
    * Sets the Consensus Recommendation.
    *
    * @param consensusRecommendation String
    */
   public void setConsensusRecommendation(String consensusRecommendation) {
      this.consensusRecommendation = consensusRecommendation;
   }

   /**
    * Gets the Fiscal Year End Month.
    *
    * @return String  fiscalYearEndMonth
    */
   public String getFiscalYearEndMonth() {
      return fiscalYearEndMonth;
   }

   /**
    * Sets the Fiscal Year End Month.
    *
    * @param fiscalYearEndMonth String
    */
   public void setFiscalYearEndMonth(String fiscalYearEndMonth) {
      this.fiscalYearEndMonth = fiscalYearEndMonth;
   }

   /**
    * Gets the Last Modified.
    *
    * @return String  lastModified
    */
   public String getLastModified() {
      return lastModified;
   }

   /**
    * Sets the Last Modified.
    *
    * @param lastModified String
    */
   public void setLastModified(String lastModified) {
      this.lastModified = lastModified;
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
