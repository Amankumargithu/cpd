 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * NoDataFoundException.java
  * <P>
  *   This exception is thrown when no record is found in the database.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   
  */
 
  public class NoDataFoundException extends BusinessException
  {
    /**
     * The constructor
     *
     * @param String message  error message
     *
     */
    public NoDataFoundException(String message) 
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
    public NoDataFoundException(String message, Exception e)
    {
        super(message, e);
    }
    
  }
