 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * SessionExpiredException.java
  * <P>
  *   The result of the user's session expiring while accessing session 
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
 
  public class SessionExpiredException extends BusinessException
  {
    /**
     * The constructor
     *
     * @param String message  error message
     *
     */
    public SessionExpiredException(String message) 
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
    public SessionExpiredException(String message, Exception e)
    {
        super(message, e);
    }
    
  }
