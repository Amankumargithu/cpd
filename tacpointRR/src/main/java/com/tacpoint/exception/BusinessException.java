 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * BusinessException.java
  * <P>
  *   The exception is related to retrieve data for Business Object.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The generic exception for the application.
  */
 
  public class BusinessException extends DefaultException
  {
    /**
     * The constructor
     *
     * @param String message  error message
     *
     */
    public BusinessException(String message) 
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
    public BusinessException(String message, Exception e)
    {
        super(message, e);
    }
    
  }