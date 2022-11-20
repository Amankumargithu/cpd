
package com.b4utrade.web.form;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Form for the login page.
 *
 * @struts.form name="loginForm"
 */
public class LoginForm extends ActionForm {

   private String userName;
   private String password;
   private String connectionSpeed;

   private boolean doRememberUser = false;


   /**
    * Standard constructor.
    */
   public LoginForm() {

   }

   /**
    * Gets the userName value.
    *
    * @return String  userName
    */
   public String getUserName() {
      return userName;
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
    * Gets the password value.
    *
    * @return String  password
    */
   public String getPassword() {
      return password;
   }

   /**
    * Sets the password value.
    *
    * @param password  String
    */
   public void setPassword(String password) {
      this.password = password;
   }

   /**
    * Gets the connectionSpeed value.
    *
    * @return String  connectionSpeed
    */
   public String getConnectionSpeed() {
      return connectionSpeed;
   }

   /**
    * Sets the connectionSpeed value.
    *
    * @param connectionSpeed  String
    */
   public void setConnectionSpeed(String connectionSpeed) {
      this.connectionSpeed = connectionSpeed;
   }

   /**
    * Gets the doRememberUser status
    *
    * @return the doRememberUser flag
    */
   public boolean getDoRememberUser() {
      return doRememberUser;
   }

   /**
    * Sets the doRememberUser flag
    *
    * @param doRememberUser  boolean
    */
   public void setDoRememberUser(boolean doRememberUser) {
      this.doRememberUser = doRememberUser;
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

      doRememberUser = false;
      userName = "";
      password = "";
      connectionSpeed = "";

   }

}
