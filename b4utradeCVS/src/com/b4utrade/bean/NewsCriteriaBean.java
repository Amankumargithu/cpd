/**
 * NewsCriteriaBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import java.sql.Timestamp;
import java.util.Vector;

import com.tacpoint.common.DefaultObject;


/**
 *   The News criteria bean object holding the criterias to search for news.
 */
public class NewsCriteriaBean extends DefaultObject
{
   private String criteriaName = "";
   private Vector categoryV = new Vector();  // Vector of Strings containing criteria detail bean.
   private String startDate = null;
   private String endDate = null;
   

   /**
    * Default constructor - does nothing.
    */
   public NewsCriteriaBean()
   {
   }


   public String getEndDate()
   {
      return endDate;
   }


   public void setEndDate(String endDate)
   {
      this.endDate = endDate;
   }


   public String getStartDate()
   {
      return startDate;
   }


   public void setStartDate(String startDate)
   {
      this.startDate = startDate;
   }


   /**
    * @return Returns the categoryV.
    */
   public Vector getCategoryV()
   {
      return categoryV;
   }


   /**
    * @param categoryV The categoryV to set.
    */
   public void setCategoryV(Vector categoryV)
   {
      this.categoryV = categoryV;
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


   public void addCategory(Object categoryObj) {
        if (categoryV == null) categoryV = new Vector();
        
        categoryV.addElement(categoryObj);
   }
}
