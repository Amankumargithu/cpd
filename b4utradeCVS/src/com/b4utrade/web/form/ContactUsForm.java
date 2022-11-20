
package com.b4utrade.web.form;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.util.LabelValueBean;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Form for the login page.
 *
 * @struts.form name="loginForm"
 */
public class ContactUsForm extends ActionForm {


   public final static String NEW      = "NEW";
   public final static String SEND     = "SEND";
   public final static String NA     = "N/A";

   private LabelValueBean[] locationSelect;
   private LabelValueBean[] helpSelect;
   private LabelValueBean[] hearUsSelect;

   private Hashtable locationTable;
   private Hashtable helpTable;
   private Hashtable hearUsTable;

   private String action = NEW;

   private String firstName;
   private String lastName;
   private String email;
   private String companyName;
   private String jobTitle;
   private String phone;
   private int locationId;
   private int helpId;
   private int hearUsId;
   private String question;


   /**
    * Standard constructor.
    */
   public ContactUsForm() {
       locationTable = new Hashtable();
       helpTable = new Hashtable();
       hearUsTable = new Hashtable();

       locationTable.put("0","North America" );
       locationTable.put("1","Europe" );
       locationTable.put("2","Other" );

       helpTable.put("0","Please select" );
       helpTable.put("1","Technical support" );
       helpTable.put("2","Sales inquiry" );
       helpTable.put("3","General information" );
       helpTable.put("4","Online demo" );
       helpTable.put("5","Other" );

       hearUsTable.put("0","Please select" );
       hearUsTable.put("1","Search Engine" );
       hearUsTable.put("2","Press Article" );
       hearUsTable.put("3","Word of Mouth" );
       hearUsTable.put("4","Another Website" );
       hearUsTable.put("5","Other" );

       locationSelect = new LabelValueBean[3];
       locationSelect[0] = new LabelValueBean("North America","0");
       locationSelect[1] = new LabelValueBean("Europe","1");
       locationSelect[2] = new LabelValueBean("Other","2");

       helpSelect = new LabelValueBean[6];
       helpSelect[0] = new LabelValueBean("Please select","0");
       helpSelect[1] = new LabelValueBean("Technical support","1");
       helpSelect[2] = new LabelValueBean("Sales inquiry","2");
       helpSelect[3] = new LabelValueBean("General information","3");
       helpSelect[4] = new LabelValueBean("Online demo","4");
       helpSelect[5] = new LabelValueBean("Other","5");

       hearUsSelect = new LabelValueBean[6];
       hearUsSelect[0] = new LabelValueBean("Please select","0");
       hearUsSelect[1] = new LabelValueBean("Search Engine","1");
       hearUsSelect[2] = new LabelValueBean("Press Article","2");
       hearUsSelect[3] = new LabelValueBean("Word of Mouth","3");
       hearUsSelect[4] = new LabelValueBean("Another Website","4");
       hearUsSelect[5] = new LabelValueBean("Other","5");
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
    * Sets the action value.
    *
    * @param firstName  String
    */
   public void setAction(String action) {
      this.action = action;
   }

   /**
    * Gets the action value.
    *
    * @return String  action
    */
   public String getAction() {
      return action;
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
    * Gets the lastName value.
    *
    * @return String  lastName
    */
   public String getLastName() {
      return lastName;
   }

   /**
    * Sets the lastName value.
    *
    * @param lastName  String
    */
   public void setlastName(String lastName) {
      this.lastName = lastName;
   }

   /**
    * Gets the email value.
    *
    * @return String  email
    */
   public String getEmail() {
      return email;
   }

   /**
    * Sets the email value.
    *
    * @param email  String
    */
   public void setEmail(String email) {
      this.email = email;
   }

   /**
    * Gets the companyName value.
    *
    * @return String  companyName
    */
   public String getCompanyName() {
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
    * Gets the jobTitle value.
    *
    * @return String  jobTitle
    */
   public String getJobTitle() {
      return jobTitle;
   }

   /**
    * Sets the jobTitle value.
    *
    * @param jobTitle  String
    */
   public void setJobTitle(String jobTitle) {
      this.jobTitle = jobTitle;
   }

   /**
    * Gets the phone value.
    *
    * @return String  phone
    */
   public String getPhone() {
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
    * Gets the question value.
    *
    * @return String  question
    */
   public String getQuestion() {
      return question;
   }

   /**
    * Sets the question value.
    *
    * @param question  String
    */
   public void setQuestion(String question) {
      this.question = question;
   }

   /**
    * Gets the locationId value.
    *
    * @return int  locationId
    */
   public int getLocationId() {
      return locationId;
   }

   /**
    * Sets the locationId value.
    *
    * @param locationId int
    */
   public void setPhone(int locationId) {
      this.locationId = locationId;
   }

   /**
    * Gets the helpId value.
    *
    * @return int  helpId
    */
   public int getHelpId() {
      return helpId;
   }

   /**
    * Sets the helpId value.
    *
    * @param helpId int
    */
   public void setHelpId(int helpId) {
      this.helpId = helpId;
   }

   /**
    * Gets the hearUsId value.
    *
    * @return int  hearUsId
    */
   public int getHearUsId() {
      return hearUsId;
   }

   /**
    * Sets the hearUsId value.
    *
    * @param hearUsId int
    */
   public void setHearUsId(int hearUsId) {
      this.hearUsId = hearUsId;
   }

   /**
    * Gets the locationSelect value.
    *
    * @return LabelValueBean[]  locationSelect
    */
   public LabelValueBean[] getLocationSelect() {
      return locationSelect;
   }

   /**
    * Sets the locationSelect value.
    *
    * @param locationSelect LabelValueBean[]
    */
   public void setLocationSelect(LabelValueBean[] locationSelect) {
      this.locationSelect = locationSelect;
   }

   /**
    * Gets the helpSelect value.
    *
    * @return LabelValueBean[]  helpSelect
    */
   public LabelValueBean[] getHelpSelect() {
      return helpSelect;
   }

   /**
    * Sets the helpSelect value.
    *
    * @param helpSelect LabelValueBean[]
    */
   public void setHelpSelect(LabelValueBean[] helpSelect) {
      this.helpSelect = helpSelect;
   }

   /**
    * Gets the hearUsSelect value.
    *
    * @return LabelValueBean[]  hearUsSelect
    */
   public LabelValueBean[] gethearUsSelect() {
      return hearUsSelect;
   }

   /**
    * Sets the locationTable value.
    *
    * @param hearUsSelect LabelValueBean[]
    */
   public void setHearUsSelect(LabelValueBean[] hearUsSelect) {
      this.hearUsSelect = hearUsSelect;
   }

   /**
    * Gets the locationTable value.
    *
    * @return Hashtable  locationTable
    */
   public Hashtable getLocationTable() {
      return locationTable;
   }

   /**
    * Sets the locationTable value.
    *
    * @param helpTable Hashtable
    */
   public void setLocationTable(Hashtable locationTable) {
      this.locationTable = locationTable;
   }

   /**
    * Gets the location value.
    *
    * @return String  location
    */
   public String getLocation(int index) {
      if ((locationTable != null) && (index < locationTable.size())){
        return (String)locationTable.get(String.valueOf(index));
      }
      else
        return NA;
   }

   /**
    * Gets the helpTable value.
    *
    * @return Hashtable  helpTable
    */
   public Hashtable getHelpTable() {
      return helpTable;
   }

   /**
    * Sets the helpTable value.
    *
    * @param helpTable Hashtable
    */
   public void setHelpTable(Hashtable helpTable) {
      this.helpTable = helpTable;
   }

   /**
    * Gets the help value.
    *
    * @return String  help
    */
   public String getHelp(int index) {
      if ((helpTable != null) && (index < helpTable.size())){
        return (String)helpTable.get(String.valueOf(index));
      }
      else
        return NA;
   }

   /**
    * Gets the hearUsTable value.
    *
    * @return Hashtable  hearUsTable
    */
   public Hashtable getHearUsTable() {
      return hearUsTable;
   }

   /**
    * Sets the hearUsTable value.
    *
    * @param hearUsTable Hashtable
    */
   public void setHearUsId(Hashtable hearUsTable) {
      this.hearUsTable = hearUsTable;
   }

   /**
    * Gets the HearUs value.
    *
    * @return String  HearUs
    */
   public String getHearUs(int index) {
      if ((hearUsTable != null) && (index < hearUsTable.size())){
        return (String)hearUsTable.get(String.valueOf(index));
      }
      else
        return NA;
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
