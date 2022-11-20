/**
 * NewsCriteriaDetailBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import java.util.Vector;
import java.util.List;

import com.tacpoint.common.DefaultObject;


/**
 *   The News criteria detail bean object holding the criterias to search for news.
 */
public class NewsCriteriaDetailBean extends DefaultObject
{

   private String criteriaName = "";
   private int    criteriaListId = 1;
   private String ticker = "";
   private String category = "";
   private String operationType = "";
   private java.sql.Timestamp startDate = null;
   private java.sql.Timestamp endDate = null;
   private List tickers;
   private List categories;

   public static final String OPERATION_AND = "AND";
   public static final String OPERATION_OR  = "OR";




   /**
    * Default constructor - does nothing.
    */
   public NewsCriteriaDetailBean()
   {
   }

   public List getCategories() {
	   return categories;
   }

   public List getTickers() {
	   return tickers;
   }

   public void setTickers(List tickers) {
	   this.tickers = tickers;
   }

   public void setCategories(List categories) {
	   this.categories = categories;
   }

   /**
    * @return Returns the category.
    */
   public String getCategory()
   {
      return category;
   }


   /**
    * @param category The category to set.
    */
   public void setCategory(String category)
   {
      this.category = category;
   }


   /**
    * @return Returns the endDate.
    */
   public java.sql.Timestamp getEndDate()
   {
      return endDate;
   }


   /**
    * @param endDate The endDate to set.
    */
   public void setEndDate(java.sql.Timestamp endDate)
   {
      this.endDate = endDate;
   }


   /**
    * @return Returns the startDate.
    */
   public java.sql.Timestamp getStartDate()
   {
      return startDate;
   }


   /**
    * @param startDate The startDate to set.
    */
   public void setStartDate(java.sql.Timestamp startDate)
   {
      this.startDate = startDate;
   }


   /**
    * @return Returns the ticker.
    */
   public String getTicker()
   {
      return ticker;
   }


   /**
    * @param ticker The ticker to set.
    */
   public void setTicker(String ticker)
   {
      this.ticker = ticker;
   }



   /**
    * @return Returns the criteria name.
    */
   public String getCriteriaName()
   {
      return criteriaName;
   }


   /**
    * @param criteriaName The criteriaName to set.
    */
   public void setCriteriaName(String criteriaName)
   {
      this.criteriaName = criteriaName;
   }


   /**
    * @return Returns the Operation Type.
    */
   public String getOperationType()
   {
      return operationType;
   }


   /**
    * @param operationType The operationType to set.
    */
   public void setOperationType(String operationType)
   {
      this.operationType = operationType;
   }

   public void setCriteriaListId(int listId) {
       this.criteriaListId = listId;
   }

   public int getCriteriaListId() {
        return(this.criteriaListId);
   }


}
