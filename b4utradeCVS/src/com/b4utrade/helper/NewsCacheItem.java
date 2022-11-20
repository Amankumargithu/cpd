/***************************************************************************
  * NewsCacheItem.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kevin Chan
  * @version 1.0
  * Date created:  8/16/2001
  * Purpose:  Class file use for news which contain newsID
  *           and time.
 **************************************************************************/
package com.b4utrade.helper;
import com.tacpoint.common.DefaultObject;



public class NewsCacheItem  extends DefaultObject
{
   private String newsID = "";
   private String newsTime = "";
   private String newsDESC = "";
   private String newsSource = "";
   private String dowJones = "";

   public String getDowJones() {
	return dowJones;
}

public void setDowJones(String inDowJones) {
	dowJones = inDowJones;
}

public void setNewsID(String inNewsID)
   {
      newsID = inNewsID;
   }

   public String getNewsID()
   {
      return newsID;
   }

   public void setNewsSource(String inNewsSource)
   {
      newsSource = inNewsSource;
   }

   public String getNewsSource()
   {
      return newsSource;
   }

   public void setNewsDESC(String inNewsDESC)
   {
      newsDESC = inNewsDESC;
   }

   public String getNewsDESC()
   {
      return newsDESC;
   }

   public void setTime(String inTime)
   {
      newsTime = inTime;
   }

   public String getDateTime()
   {
      return newsTime;
   }

   public String getTime()
   {
      if (newsTime != null)
        if (newsTime.equals(""))
          return newsTime;
        else
          if (newsTime.length() >= 15)
            return newsTime.substring(8,13);
      return "";
   }

}