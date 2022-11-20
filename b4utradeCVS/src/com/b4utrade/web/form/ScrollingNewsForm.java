
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
 * @struts.form name="ScrollingNewsForm"
 */
public class ScrollingNewsForm extends ActionForm {

   private String newsParam;
   private String newsTicker;
   private String newsURL;
   private String newsDate;
   private String newsDesc;
   private String newsTarget;
   private String newsBackToID;
   private String newsPopupFlag;
   
   private String newsHeader;
   private String newsForwardURL;
   private String newsForwardURLText;
   private String newsMoreURL;
   private String newsMoreURLText;
   private String newWindowFlag;

   /**
    * Standard constructor.
    */
   public ScrollingNewsForm() {

   }

   /**
    * Gets the newsParam value.
    *
    * @return String  newsParam
    */
   public String getNewsParam() {
      if (newsParam == null)
         return "";
      else 
         return newsParam;
   }

   /**
    * Sets the newsParam value.
    *
    * @param newsParam  String
    */
   public void setNewsParam(String newsParam) {
      this.newsParam = newsParam;
   }

   /**
    * Gets the newsTicker value.
    *
    * @return String  newsTicker
    */
   public String getNewsTicker() {
      if (newsTicker == null)
         return "";
      else 
         return newsTicker;    
   }

   /**
    * Sets the newsTicker value.
    *
    * @param newsTicker  String
    */
   public void setNewsTicker(String newsTicker) {
      this.newsTicker = newsTicker;
   }

   /**
    * Gets the newsURL value.
    *
    * @return String  newsURL
    */
   public String getNewsURL() {
      if (newsURL == null)
         return "";
      else 
         return newsURL;    
   }

   /**
    * Sets the newsURL value.
    *
    * @param newsURL  String
    */
   public void setNewsURL(String newsURL) {
      this.newsURL = newsURL;
   }

   /**
    * Gets the newsDate value.
    *
    * @return String  newsDate
    */
   public String getNewsDate() {
      if (newsDate == null)
         return "";
      else 
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
    * Gets the newsDesc value.
    *
    * @return String  newsDesc
    */
   public String getNewsDesc() {
      if (newsDesc == null)
         return "";
      else 
         return newsDesc;    
   }

   /**
    * Sets the newsDesc value.
    *
    * @param newsDesc  String
    */
   public void setNewsDesc(String newsDesc) {
      if (!(newsDesc == null)) 
         this.newsDesc = newsDesc.trim();
      else
         this.newsDesc = newsDesc;
   }

   /**
    * Gets the newsTarget value.
    *
    * @return string  newsTarget
    */
   public String getNewsTarget() {
      if (newsTarget == null)
         return "";
      else 
         return newsTarget;    
   }

   /**
    * Sets the newsTarget value.
    *
    * @param newsTarget  String
    */
   public void setNewsTarget(String newsTarget) {
      this.newsTarget = newsTarget;
   }

   /**
    * Gets the newsBackToID value.
    *
    * @return String  newsBackToID
    */
   public String getNewsBackToID() {
      if (newsBackToID == null)
         return "";
      else 
         return newsBackToID;    
   }

   /**
    * Sets the newsBackToID value.
    *
    * @param newsBackToID  String
    */
   public void setNewsBackToID(String newsBackToID) {
      this.newsBackToID = newsBackToID;
   }

   /**
    * Gets the newsPopupFlag value.
    *
    * @return String  newsPopupFlag
    */
   public String getNewsPopupFlag() {
      if (newsPopupFlag == null)
         return "";
      else 
         return newsPopupFlag;    
   }

   /**
    * Sets the newsPopupFlag value.
    *
    * @param newsPopupFlag  String
    */
   public void setNewsPopupFlag(String newsPopupFlag) {
      this.newsPopupFlag = newsPopupFlag;
   }

   /**
    * Gets the newsHeader value.
    *
    * @return String  newsHeader
    */
   public String getNewsHeader() {
      if (newsHeader== null)
         return "";
      else 
         return newsHeader;    
   }

   /**
    * Sets the newsHeader value.
    *
    * @param newsHeader  String
    */
   public void setNewsHeader(String newsHeader) {
      this.newsHeader = newsHeader;
   }         

   /**
    * Gets the newsForwardURL value.
    *
    * @return String  newsForwardURL
    */
   public String getNewsForwardURL() {
      if (newsForwardURL == null)
         return "";
      else 
         return newsForwardURL;    
   }

   /**
    * Sets the newsForwardURL value.
    *
    * @param newsForwardURL  String
    */
   public void setNewsForwardURL(String newsForwardURL) {
      this.newsForwardURL = newsForwardURL;
   }
   
   /**
    * Gets the newsForwardURLText value.
    *
    * @return String  newsForwardURLText
    */
   public String getNewsForwardURLText() {
      if (newsForwardURLText == null)
         return "";
      else 
         return newsForwardURLText;    
   }

   /**
    * Sets the newsForwardURLText value.
    *
    * @param newsForwardURLText  String
    */
   public void setNewsForwardURLText(String newsForwardURLText) {
      this.newsForwardURLText = newsForwardURLText;
   }
   
   /**
    * Gets the newsMoreURL value.
    *
    * @return String  newsMoreURL
    */
   public String getNewsMoreURL() {
      if (newsMoreURL == null)
         return "";
      else 
         return newsMoreURL;    
   }

   /**
    * Sets the newsMoreURL value.
    *
    * @param newsMoreURL  String
    */
   public void setNewsMoreURL(String newsMoreURL) {
      this.newsMoreURL = newsMoreURL;
   }
   
   /**
    * Gets the newsMoreURLText value.
    *
    * @return String  newsMoreURLText
    */
   public String getNewsMoreURLText() {
      if (newsMoreURLText == null)
         return "";
      else 
         return newsMoreURLText;    
   }

   /**
    * Sets the newsMoreURLText value.
    *
    * @param newsMoreURLText  String
    */
   public void setNewsMoreURLText(String newsMoreURLText) {
      this.newsMoreURLText = newsMoreURLText;
   }         
   
   /**
    * Gets the newWindowFlag value.
    *
    * @return String  newWindowFlag
    */
   public String getNewWindowFlag() {
      if (newWindowFlag == null)
         return "";
      else 
         return newWindowFlag;    
   }

   /**
    * Sets the newWindowFlag value.
    *
    * @param newWindowFlag  String
    */
   public void setNewWindowFlag(String newWindowFlag) {
      this.newWindowFlag = newWindowFlag;
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
