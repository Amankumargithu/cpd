 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * InvalidAccessException.java
  * <P>
  *   The result of the user's access denial while accessing 
  *   dependant pages.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 2000.  All rights reserved.
  * @author Andy Katz
  * @author akatz@tacpoint.com
  * @version 1.0
  * Date created: 04/18/2000
  * Date modified:
  * - 04/18/2000 Initial version
  */
 
  public class InvalidAccessException extends BusinessException
  {
    /**
     * The constructor
     *
     * @param String message  error message
     *
     */
    public InvalidAccessException(String message) 
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
    public InvalidAccessException(String message, Exception e)
    {
        super(message, e);
    }
    
  }
