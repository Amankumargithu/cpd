/**
 * UserLoginStatusBean.java
 *
 * @author Copyright (c) 2003 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


/**
 * This bean holds the user login status information.
 */
public class UserLoginStatusBean extends DefaultObject
{

   private static final int STATUS_LOGIN_FAIL = 0;
   private static final int STATUS_LOGIN_SUCCESS = 1;
   private static final int STATUS_LOGIN_DUPLICATE = 2;
   private static final int STATUS_EXPIRE_OTHER_FAIL = 3;
   private static final int STATUS_EXPIRE_OTHER_SUCCESS = 4;
   private static final int STATUS_LOGOUT_FAIL = 5;
   private static final int STATUS_LOGOUT_SUCCESS = 6;
   private static final int STATUS_SESSION_CHECK_FAIL = 7;
   private static final String LOGIN_FAIL_MESSAGE = "Invalid username or password";
   private static final String LOGIN_DUPLICATE_MESSAGE = "User already logged in";
   private static final String EXPIRE_OTHER_MESSAGE = "Unable to expire other user";
   private static final String LOGOUT_FAIL_MESSAGE = "Unable to log out";
   private static final String SESSION_CHECK_FAIL_MESSAGE = "Unable to check user session";
   public static final String USERNAME_KEY = "USER_NAME";
   public static final String PASSWORD_KEY = "PASSWORD";
   public static final String USER_ID_KEY = "USER_ID";
   public static final String ACTION_KEY = "ACTION";
   public static final String ACTION_LOGIN = "LOGIN";
   public static final String ACTION_EXPIRE_SESSION = "EXPIRE_OTHER";
   public static final String ACTION_LOGOUT = "LOGOUT";
   public static final String ACTION_CHECK_SESSION = "SESSION_EXIST";
   private int userID;
   private String username;
   private String password;
   private int status=0;
   private String message;
   private String referralPartner;
   
   //AMAN
   private String firstName;
   private String lastName;
   private String emailID;
   


   /**
    *  Default constructor - does nothing.
    */
   public UserLoginStatusBean()
   {
      // do nothing
   }


   /**
    * Sets the userID value.
    *
    * @param userID  int
    */
   public void setUserID(int inID) {

      this.userID = inID;

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
    * Sets the status value.
    *
    * @param status  int
    */
   public void setStatus(int status) {

      this.status = status;

   }

   /**
    * Gets the status value.
    *
    * @return status  int
    */
   public int getStatus() {

       return status;

   }


   /**
    * Sets the username value.
    *
    * @param username  String
    */
   public void setUsername(String username) {
      this.username = username;
   }

   /**
    * Gets the username value.
    *
    * @return username  String
    */
   public String getUsername() {
      return username;
   }

   /**
    * Sets the setReferralPartner value.
    *
    * @param referralPartner  String
    */
   public void setReferralPartner(String referralPartner) {
      this.referralPartner = referralPartner;
   }

   /**
    * Gets the referralPartner value.
    *
    * @return referralPartner  String
    */
   public String getReferalParrtner() {
      return referralPartner;
   }



   /**
    * Sets the password.
    *
    * @param password  String
    */
   public void setPassword(String password) {
      this.password = password;

   }

   /**
    * Gets the password
    *
    * @return password String
    */
   public String getPassword() {
      return password;
   }

   /**
    * Sets the message .
    *
    * @param message  Vector
    */
   public void setMessage(String message) {
      this.message = message;
   }

   /**
    * Gets the message.
    *
    * @return message String
    */
   public String getMessage() {
      return message;
   }

   /**
    * set the login failed
    *
    */

   public void setStatusLoginFailed() {
	   this.message = UserLoginStatusBean.LOGIN_FAIL_MESSAGE;
       this.status = UserLoginStatusBean.STATUS_LOGIN_FAIL;
   }

   public boolean isLoginFailed()
   {
       return(this.status == UserLoginStatusBean.STATUS_LOGIN_FAIL);
   }


   /**
    * set the login success
    *
    */

   public void setStatusLoginSuccess() {
       this.status = UserLoginStatusBean.STATUS_LOGIN_SUCCESS;
   }

   public boolean isLoginSuccessful()
   {
       return(this.status == UserLoginStatusBean.STATUS_LOGIN_SUCCESS);
   }



   /**
    * set the login duplicated
    *
    */

   public void setStatusLoginDuplicate() {
	   this.message = UserLoginStatusBean.LOGIN_DUPLICATE_MESSAGE;
       this.status = UserLoginStatusBean.STATUS_LOGIN_DUPLICATE;
   }

   public boolean isLoginDuplicated()
   {
       return(this.status == UserLoginStatusBean.STATUS_LOGIN_DUPLICATE);
   }


   /**
    * set the expired other fail
    *
    */

   public void setStatusExpireOtherFail() {
	   this.message = UserLoginStatusBean.EXPIRE_OTHER_MESSAGE;
       this.status = UserLoginStatusBean.STATUS_EXPIRE_OTHER_FAIL;
   }

   public boolean isExpireOtherFailed()
   {
       return(this.status == UserLoginStatusBean.STATUS_EXPIRE_OTHER_FAIL);
   }



   /**
    * set the expired other success
    *
    */

   public void setStatusExpireOtherSuccess() {
       this.status = UserLoginStatusBean.STATUS_EXPIRE_OTHER_SUCCESS;
   }

   public boolean isExpireOtherSuccessful()
   {
       return(this.status == UserLoginStatusBean.STATUS_EXPIRE_OTHER_SUCCESS);
   }


   /**
    * set the logout failed
    *
    */

   public void setStatusLogoutFailed() {
	   this.message = UserLoginStatusBean.LOGOUT_FAIL_MESSAGE;
       this.status = UserLoginStatusBean.STATUS_LOGOUT_FAIL;
   }

   public boolean isLogoutFailed()
   {
       return(this.status == UserLoginStatusBean.STATUS_LOGOUT_FAIL);
   }


   /**
    * set the logout success
    *
    */

   public void setStatusLogoutSuccess() {
       this.status = UserLoginStatusBean.STATUS_LOGOUT_SUCCESS;
   }

   public boolean isLogoutSuccessful()
   {
       return(this.status == UserLoginStatusBean.STATUS_LOGOUT_SUCCESS);
   }


   /**
    * set the user session check
    *
    */

   public void setStatusSessionCheckFailed() {
       this.message = UserLoginStatusBean.SESSION_CHECK_FAIL_MESSAGE;
       this.status = UserLoginStatusBean.STATUS_SESSION_CHECK_FAIL;
   }

   public boolean isSessionCheckFailed()
   {
       return(this.status == UserLoginStatusBean.STATUS_SESSION_CHECK_FAIL);
   }


	public String getFirstName() {
		return firstName;
	}
	
	
	public void setFirstName(String firstName) { 		
		this.firstName = firstName;
	}
	
	
	public String getLastName() {
		return lastName;
	}
	
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	
	public String getEmailID() {
		return emailID;
	}
	
	
	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}

   
}
