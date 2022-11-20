
package com.b4utrade.web.form;

import java.util.*;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import org.apache.struts.util.LabelValueBean;

/**
 * Form containing option search related info
 */
public class OptionSnapsForm extends ActionForm {

   public static final String INIT = "INIT";
   public static final String SEARCH = "SEARCH";

   private String ticker;

   private String action;

   private int fromMonth;
   private int fromYear;

   private int toMonth;
   private int toYear;

   private double fromStrike = 0.0d;
   private double toStrike = 0.0d;

   private int type;

   private ArrayList monthList = new ArrayList();
   private ArrayList yearList = new ArrayList();
   private ArrayList typeList = new ArrayList();

   private ArrayList optionSnaps;

   public OptionSnapsForm() {}

   public String getAction() {
      return action;
   }

   public void setAction(String action) {
      this.action = action;
   }
   
   public String getTicker() {
      return ticker;
   }

   public void setTicker(String ticker) {
      this.ticker = ticker;
   }
   
   public ArrayList getOptionSnaps() {
      return optionSnaps;
   }

   public void setOptionSnaps(ArrayList optionSnaps) {
      this.optionSnaps = optionSnaps;
   }
   
   public ArrayList getTypeList() {
      return typeList;
   }

   public void setTypeList(ArrayList typeList) {
      this.typeList = typeList;
   }
   
   public ArrayList getMonthList() {
      return monthList;
   }

   public void setMonthList(ArrayList monthList) {
      this.monthList = monthList;
   }
   
   public ArrayList getYearList() {
      return yearList;
   }

   public void setYearList(ArrayList yearList) {
      this.yearList = yearList;
   }
   
   public int getFromMonth() {
      return fromMonth;
   }

   public void setFromMonth(int fromMonth) {
      this.fromMonth = fromMonth;
   }

   public int getFromYear() {
      return fromYear;
   }

   public void setFromYear(int fromYear) {
      this.fromYear = fromYear;
   }

   public int getToMonth() {
      return toMonth;
   }

   public void setToMonth(int toMonth) {
      this.toMonth = toMonth;
   }

   public int getToYear() {
      return toYear;
   }

   public void setToYear(int toYear) {
      this.toYear = toYear;
   }

   public double getFromStrike() {
      return fromStrike;
   }

   public void setFromStrike(double fromStrike) {
      this.fromStrike = fromStrike;
   }

   public double getToStrike() {
      return toStrike;
   }

   public void setToStrike(double toStrike) {
      this.toStrike = toStrike;
   }

   public int getType() {
      return type;
   }

   public void setType(int type) {
      this.type = type;
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

       // set from and to date ranges ...

       Calendar c = Calendar.getInstance();

       int year = c.get(Calendar.YEAR);
       
       for (int i=0; i<4; i++) {
          String val = String.valueOf(year+i);
          LabelValueBean lvb = new LabelValueBean(val,val);
          yearList.add(lvb);
       }
          
       LabelValueBean lvb = null;
       lvb = new LabelValueBean("Jan","0");
       monthList.add(lvb);
       lvb = new LabelValueBean("Feb","1");
       monthList.add(lvb);
       lvb = new LabelValueBean("Mar","2");
       monthList.add(lvb);
       lvb = new LabelValueBean("Apr","3");
       monthList.add(lvb);
       lvb = new LabelValueBean("May","4");
       monthList.add(lvb);
       lvb = new LabelValueBean("Jun","5");
       monthList.add(lvb);
       lvb = new LabelValueBean("Jul","6");
       monthList.add(lvb);
       lvb = new LabelValueBean("Aug","7");
       monthList.add(lvb);
       lvb = new LabelValueBean("Sep","8");
       monthList.add(lvb);
       lvb = new LabelValueBean("Oct","9");
       monthList.add(lvb);
       lvb = new LabelValueBean("Nov","10");
       monthList.add(lvb);
       lvb = new LabelValueBean("Dec","11");
       monthList.add(lvb);

       lvb = new LabelValueBean("BOTH","0");
       typeList.add(lvb);
       lvb = new LabelValueBean("CALLS","1");
       typeList.add(lvb);
       lvb = new LabelValueBean("PUTS","2");
       typeList.add(lvb);

   }

}
