 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * UserNameException.java
  * <P>
  *   The exception is related to unknown user name
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The exception caused by unknown user name.
  */
 
  public class UserNameException extends BusinessException
  {
    /**
     * The constructor
     *
     * @param String message  error message
     *
     */
    public UserNameException(String message) 
    {
        super(message);
    }
    

   /**
    *
    *  The Constructor
    *
    *  @param String message the error message
    *  @param Exception e
    *
    */
    public UserNameException(String message, Exception e)
    {
        super(message, e);
    }
    
  }