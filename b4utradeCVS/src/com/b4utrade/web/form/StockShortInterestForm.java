
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
 * @struts.form name="StockShortInterestForm"
 */
public class StockShortInterestForm extends ActionForm {

   //private boolean isLoggedIn = false;

   private String ticker;
   private String currentShare;
   private String previousShare;


   /**
    * Standard constructor.
    */
   public StockShortInterestForm() {
      ticker ="N/A";
      currentShare ="";
      previousShare ="";
   }

   /**
    * Gets the ticker.
    *
    * @return String  ticker
    */
   public String getTicker() {
      return ticker;
   }

   /**
    * Sets the ticker.
    *
    * @param ticker  String
    */
   public void setTicker(String ticker) {
      this.ticker = ticker;
   }

   /**
    * Gets the current share.
    *
    * @return String  currentShare
    */
   public String getCurrentShare() {
      return currentShare;
   }

   /**
    * Sets the current share.
    *
    * @param currentStrongBuy  String
    */
   public void setCurrentShare(String currentShare) {
      this.currentShare = currentShare;
   }

   /**
    * Gets the previous share.
    *
    * @return String  previousShare
    */
   public String getPreviousShare() {
      return previousShare;
   }

   /**
    * Sets the previous share.
    *
    * @param previousShare  String
    */
   public void setPreviousShare(String previousShare) {
      this.previousShare = previousShare;
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
