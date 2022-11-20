
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
 * @struts.form name="stockUpCloseEarningsForm"
 */
public class StockUpCloseEarningsForm extends ActionForm {


   private String tickerName;

   private String consensusRecommendation;
   private String fiscalYearEndMonth;
   private String lastModified;
     
   private String earningDate;
   private String firstCallEstimate;

   /**
    * Standard constructor.
    */
   public StockUpCloseEarningsForm() {
      consensusRecommendation = "";
      fiscalYearEndMonth = "";
      lastModified = "";   	
      earningDate = "";
      firstCallEstimate = "";
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
