/**
 * UserPortfolioBean.java
 *
 * @author Copyright (c) 2003 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;
import java.util.Vector;


/**
 * This bean holds the Nomination Tier Data.
 */
public class UserPortfolioBean extends DefaultObject
{

   private int portfolioWatchID;
   private String portfolioName = "";
   private Vector portfolioDetail = new Vector();    // Vector of UserPortfolioDetailBeans

   private String cashToTrack = "";
   
   /**
    *  Default constructor - does nothing.
    */
   public UserPortfolioBean()
   {
      // do nothing
   }


   /**
    * Sets the portfolioWatchID value.
    *
    * @param portfolioWatchID  int
    */
   public void setPortfolioWatchID(int inID) {
            
      this.portfolioWatchID = inID;
            
   }

   /**
    * Gets the portfolioWatchID value.
    *
    * @return portfolioWatchID  int
    */        
   public int getPortfolioWatchID() {
            
       return portfolioWatchID;
            
   }


   /**
    * Sets the portfolioName value.
    *
    * @param portfolioName  String
    */
   public void setPortfolioName(String portfolioName) {
      this.portfolioName = portfolioName;
   }

   /**
    * Gets the portfolioName value.
    *
    * @return portfolioName  String
    */
   public String getPortfolioName() {
      return portfolioName;
   }


   /**
    * Sets the portfolio cash value.
    *
    * @param cashToTrack  String
    */
   public void setCashToTrack(String inCash) {
      this.cashToTrack = inCash;

   }
        
   /**
    * Gets the portfolio cash value.
    *
    * @return cashToTrack String
    */        
   public String getCashToTrack() {
      return cashToTrack;
   }

   /**
    * Sets the portfolioDetail value.
    *
    * @param portfolioDetail  Vector
    */
   public void setPortfolioDetail(Vector portfolioDetail) {
      this.portfolioDetail = portfolioDetail;
   }

   /**
    * Gets the portfolioDetail value.
    *
    * @return portfolioDetail  Vector
    */
   public Vector getPortfolioDetail() {
      return portfolioDetail;
   }


}
