/**
 * QuoddUserOperationStatusBean.java
 *
 * @author Copyright (c) 2003 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


/**
 * This bean holds the user operation status information.
 */
public class QuoddUserOperationStatusBean extends DefaultObject
{

   private static final int STATUS_OPERATION_FAILED = 0;
   private static final int STATUS_OPERATION_SUCCESS = 1;

   private int status=0;
   private String message;


   /**
    *  Default constructor - does nothing.
    */
   public QuoddUserOperationStatusBean()
   {
      // do nothing
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

   public void setOperationSuccessful()
   {
       status = STATUS_OPERATION_SUCCESS;
   }

   public void setOperationFailed()
   {
      status = STATUS_OPERATION_FAILED;
   }

   public boolean isOperationSuccessful()
   {
       return(status == STATUS_OPERATION_SUCCESS);
   }

   public boolean isOperationFailed()
   {
       return(status == STATUS_OPERATION_FAILED);
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



}
