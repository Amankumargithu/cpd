/**
 * NewsBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


/**
 *   The News bean object holding news data.
 */
public class NewsBean extends DefaultObject
{

   private long newsID;
   private int categoryID;
   private String categoryType = "";	//This is used in few places to specify database used Edge/DOWJONES
   private String categoryName = "";
   private String headline = "";
   private String newsLink = "";
   private String newsStory = "";
   private String seqID = "";
   private java.sql.Timestamp newsDate = null;
   private String formattedNewsDate="";
   private String newsSource = "";
   private String tickers;
   private String categories;
   private long lastUpdateTime;



   /**
    * Default constructor - does nothing.
    */
   public NewsBean()
   {
   }

   /**
    * Sets the newsID value.
    *
    * @param newsID  long
    */
   public void setNewsID(long newsID) {
      this.newsID = newsID;
   }

   public void setLastUpdateTime(long lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
   }

   /**
    * Gets the newsID value.
    *
    * @return newsID  long
    */
   public long getNewsID() {
      return newsID;
   }

   public long getLastUpdateTime() {
      return lastUpdateTime;
   }

   /**
    * Sets the categoryID value.
    *
    * @param categoryID  int
    */
   public void setCategoryID(int categoryID) {
      this.categoryID = categoryID;
   }

   /**
    * Gets the categoryID value.
    *
    * @return categoryID  int
    */
   public int getCategoryID() {
      return categoryID;
   }

   /**
    * Sets the categories
    *
    * @param categories  String
    */
   public void setCategories(String categories) {
      this.categories = categories;
   }

   /**
    * Gets the categories String.
    *
    * @return categories  String
    */
   public String getCategories() {
      return categories;
   }


   /**
    * Sets the tickers
    *
    * @param tickers  String
    */
   public void setTickers(String tickers) {
      this.tickers = tickers;
   }

   /**
    * Gets the tickers string.
    *
    * @return tickers  String
    */
   public String getTickers() {
      return tickers;
   }


   /**
    * Sets the categoryType value.
    *
    * @param categoryType  String
    */
   public void setCategoryType(String categoryType) {
      this.categoryType = categoryType;
   }

   /**
    * Gets the categoryType value.
    *
    * @return categoryType  String
    */
   public String getCategoryType() {
      return categoryType;
   }

   /**
    * Sets the categoryName value.
    *
    * @param categoryName  String
    */
   public void setCategoryName(String categoryName) {
      this.categoryName = categoryName;
   }

   /**
    * Gets the categoryName value.
    *
    * @return categoryName  String
    */
   public String getCategoryName() {
      return categoryName;
   }

   /**
    * Sets the headline value.
    *
    * @param headline  String
    */
   public void setHeadline(String headline) {
      this.headline = headline;
   }

   /**
    * Gets the headline value.
    *
    * @return headline  String
    */
   public String getHeadline() {
      return headline;
   }

   /**
    * Sets the newsLink value.
    *
    * @param newsLink  String
    */
   public void setNewsLink(String newsLink) {
      this.newsLink = newsLink;
   }

   /**
    * Gets the newsLink value.
    *
    * @return newsLink  String
    */
   public String getNewsLink() {
      return newsLink;
   }

   /**
    * Sets the newsStory value.
    *
    * @param newsStory  String
    */
   public void setNewsStory(String newsStory) {
      this.newsStory = newsStory;
   }

   /**
    * Gets the newsStory value.
    *
    * @return newsStory  String
    */
   public String getNewsStory() {
      return newsStory;
   }

   /**
    * Sets the seqID value.
    *
    * @param seqID  String
    */
   public void setSeqID(String seqID) {
      this.seqID = seqID;
   }

   /**
    * Gets the seqID value.
    *
    * @return seqID  String
    */
   public String getSeqID() {
      return seqID;
   }

   /**
    * Sets the newsDate value.
    *
    * @param newsDate  java.sql.Timestamp
    */
   public void setNewsDate(java.sql.Timestamp newsDate) {
      this.newsDate = newsDate;
   }

   /**
    * Gets the newsDate value.
    *
    * @param newsDate  java.sql.Timestamp
    */
   public java.sql.Timestamp getNewsDate() {
      return newsDate;
   }

   /**
    * Sets the newsSource value.
    *
    * @param newsSource  String
    */
   public void setNewsSource(String newsSource) {
      this.newsSource = newsSource;
   }

   /**
    * Gets the newsSource value.
    *
    * @return newsSource  String
    */
   public String getNewsSource() {
      return newsSource;
   }

   public void setFormattedNewsDate(String indate) {
        this.formattedNewsDate = indate;
   }

   public String getFormattedNewsDate() {
        return(this.formattedNewsDate);
   }


}
