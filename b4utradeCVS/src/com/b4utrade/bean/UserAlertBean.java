/**
 * UserAlertBean.java
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
public class UserAlertBean extends DefaultObject
{
   private static final int SAVE = 0;
   private static final int DELETE = 1;
   
   private static final boolean UNSUCCESSFUL = false;
   private static final boolean SUCCESSFUL = true;   
	
   private int userID;
   private String userName;
   private String userPassword; 
   private Vector alertDetail = new Vector();    // Vector of UserAlertDetailBeans
   private int actionType = -1;
   private boolean loadingStatusFlag = UNSUCCESSFUL;   
   
   
   /**
    *  Default constructor
    */
   public UserAlertBean()
   {
      actionType = -1;
      loadingStatusFlag = UNSUCCESSFUL;
   }




   /**
    * Sets the userID value.
    *
    * @param userID  int
    */
   public void setUserID(int userID) {
      this.userID = userID;
   }

   /**
    * Gets the userID value.
    *
    * @return userID  int
    */
   public int getUserID() {
      return userID;
   }

   /**
    * Sets the userName value.
    *
    * @param userName  String
    */
   public void setUserName(String userName) {
      this.userName = userName;
   }

   /**
    * Gets the userName value.
    *
    * @return userName  String
    */
   public String getUserName() {
      return userName;
   }

   /**
    * Sets the userPassword value.
    *
    * @param userPassword  String
    */
   public void setUserPassword(String userPassword) {
      this.userPassword = userPassword;
   }

   /**
    * Gets the userPassword value.
    *
    * @return userPassword  String
    */
   public String getUserPassword() {
      return userPassword;
   }



   /**
    * Sets the alertDetail value.
    *
    * @param alertDetail Vector
    */
   public void setAlertDetail(Vector alertDetail) {
      this.alertDetail = alertDetail;
   }

   /**
    * Gets the alertDetail value.
    *
    * @return alertDetail  Vector
    */
   public Vector getAlertDetail() {
      return alertDetail;
   }


   /**
    * set the action type to indicate if portfolio data is saved or deleted.
    *
    */

   public void setActionType(int actionType)
   {
       this.actionType = actionType;
   }

   public int getActionType()
   {
       return(this.actionType);
   }

   public void setSaveActionType()          { setActionType(SAVE); }
   public void setDeleteActionType()        { setActionType(DELETE); }
   
   public boolean isSaveActionType()        { return(SAVE == getActionType()); }
   public boolean isDeleteActionType()      { return(DELETE == getActionType()); }


   /**
    * set the loading Status flag to indicate if the data loading is successfully.
    *
    */

   public void setLoadingStatusFlag(boolean loadingStatusFlag)
   {
       this.loadingStatusFlag = loadingStatusFlag;
   }

   public boolean getLoadingStatusFlag()
   {
       return(this.loadingStatusFlag);
   }

   public void setLoadingSuccessfulFlag()
   {
       setLoadingStatusFlag(SUCCESSFUL);
   }

   public void setLoadingFailedFlag()
   {
       setLoadingStatusFlag(UNSUCCESSFUL);
   }

   public boolean isLoadingSuccessful()
   {
       return(getLoadingStatusFlag());
   }


}
