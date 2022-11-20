
package com.b4utrade.web.form;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Environment;


/**
 * Form for the Company summary pages.
 *
 * @struts.form name="CompanySummaryForm"
 */
public class CompanySummaryForm extends ActionForm {

   private static final String NA_VALUE = "N/A";

   private String ticker = NA_VALUE;
   private String address = NA_VALUE;
   private String city = "";
   private String state = "";
   private String postalCode = "";
   private String country = "";
   private String phone = NA_VALUE;
   private String officers = NA_VALUE;
   private String industry = NA_VALUE;
   private String sector = NA_VALUE;
   private String companyName = NA_VALUE;
   private String numEmployees = NA_VALUE;
   private String marketCap = NA_VALUE;
   private String updateDate = NA_VALUE;
   private String summary = "";


   /**
    * Standard constructor.
    */
   public CompanySummaryForm() {

   }

   /**
    * Gets the ticker value.
    *
    * @return String  ticker
    */
   public String getTicker() {
      if (ticker == null)
         return "";
      else 
         return ticker;
   }

   /**
    * Sets the ticker value.
    *
    * @param ticker  String
    */
   public void setTicker(String ticker) {
      this.ticker = ticker;
   }

   /**
    * Gets the address value.
    *
    * @return String  address
    */
   public String getAddress() {
      if (address == null)
         return "";
      else 
         return address;
   }

   /**
    * Sets the address value.
    *
    * @param address  String
    */
   public void setAddress(String address) {
      this.address = address;
   }

   /**
    * Gets the city value.
    *
    * @return String  city
    */
   public String getCity() {
      if (city == null)
         return "";
      else 
         return city;    
   }

   /**
    * Sets the city value.
    *
    * @param city  String
    */
   public void setCity(String city) {
      this.city = city;
   }

   /**
    * Gets the state value.
    *
    * @return String  state
    */
   public String getState() {
      if (state == null)
         return "";
      else 
         return state;    
   }

   /**
    * Sets the state value.
    *
    * @param state  String
    */
   public void setState(String state) {
      this.state = state;
   }

   /**
    * Gets the postalCode  value.
    *
    * @return String  postalCode 
    */
   public String getPostalCode() {
      if (postalCode == null)
         return "";
      else 
         return postalCode ;    
   }

   /**
    * Sets the postalCode  value.
    *
    * @param postalCode   String
    */
   public void setPostalCode(String postalCode ) {
      this.postalCode  = postalCode ;
   }         

   /**
    * Gets the country value.
    *
    * @return String  country
    */
   public String getCountry() {
      if (country == null)
         return "";
      else 
         return country;    
   }

   /**
    * Sets the country value.
    *
    * @param country  String
    */
   public void setCountry(String country) {
      this.country = country;
   }

   /**
    * Gets the phone value.
    *
    * @return String  phone
    */
   public String getPhone() {
      if (phone == null)
         return "";
      else 
         return phone;    
   }

   /**
    * Sets the phone value.
    *
    * @param phone  String
    */
   public void setPhone(String phone) {
      this.phone = phone;
   }

   /**
    * Gets the officers value.
    *
    * @return string  officers
    */
   public String getOfficers() {
      if (officers == null)
         return "";
      else 
         return officers;    
   }

   /**
    * Sets the officers value.
    *
    * @param officers  String
    */
   public void setOfficers(String officers) {
      this.officers = officers;
   }

   /**
    * Gets the industry value.
    *
    * @return String  industry
    */
   public String getIndustry() {
      if (industry == null)
         return "";
      else 
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
    * Gets the sector value.
    *
    * @return String  sector
    */
   public String getSector() {
      if (sector == null)
         return "";
      else 
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
    * Gets the companyName value.
    *
    * @return String  companyName
    */
   public String getCompanyName() {
      if (companyName== null)
         return "";
      else 
         return companyName;    
   }

   /**
    * Sets the companyName value.
    *
    * @param companyName  String
    */
   public void setCompanyName(String companyName) {
      this.companyName = companyName;
   }         

   /**
    * Gets the numEmployees value.
    *
    * @return String  numEmployees
    */
   public String getNumEmployees() {
      if (numEmployees == null)
         return "";
      else 
         return numEmployees;    
   }

   /**
    * Sets the numEmployees value.
    *
    * @param numEmployees  String
    */
   public void setNumEmployees(String numEmployees) {
      this.numEmployees = numEmployees;
   }
   
   /**
    * Gets the marketCap value.
    *
    * @return String  marketCap
    */
   public String getMarketCap() {
      if (marketCap == null)
         return "";
      else 
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
    * Gets the updateDate value.
    *
    * @return String  updateDate
    */
   public String getUpdateDate() {
      if (updateDate == null)
         return "";
      else 
         return updateDate;    
   }

   /**
    * Sets the updateDate value.
    *
    * @param updateDate  String
    */
   public void setUpdateDate(String updateDate) {
      this.updateDate = updateDate;
   }
   
   /**
    * Gets the summary value.
    *
    * @return String  summary
    */
   public String getSummary() {
      if (summary == null)
         return "";
      else 
         return summary;    
   }

   /**
    * Sets the summary value.
    *
    * @param summary  String
    */
   public void setSummary(String summary) {
      this.summary = summary;
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
