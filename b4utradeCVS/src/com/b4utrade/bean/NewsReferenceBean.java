/**
 * NewsReferenceBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


public class NewsReferenceBean extends DefaultObject
{
   private int    companyID = 0;
   private String ticker = null;
   private String categoryCode = null;
   private long    newsId = 0;
   private long accessionNumber = 0;
   private long lastUpdateTime = 0;

   public void setCompanyID(int inCompanyID)
   {
      companyID = inCompanyID;
   }

   public void setTicker(String inTicker)
   {
      ticker = inTicker;
   }

   public void setNewsId(long inNewsId)
   {
        newsId = inNewsId;
   }

   public void setAccessionNumber(long accessionNumber)
   {
        this.accessionNumber = accessionNumber;
   }

   public void setLastUpdateTime(long lastUpdateTime)
   {
        this.lastUpdateTime = lastUpdateTime;
   }

   public void setCategoryCode(String inCategoryCode)
   {
        categoryCode = inCategoryCode;
   }

   public int getCompanyID()
   {
      return companyID;
   }

   public String getTicker()
   {
      return ticker;
   }

   public String getCategoryCode()
   {
        return(categoryCode);
   }

   public long getNewsId()
   {
        return(newsId);
   }

   public long getAccessionNumber()
   {
        return(accessionNumber);
   }

   public long getLastUpdateTime()
   {
        return(lastUpdateTime);
   }


}
