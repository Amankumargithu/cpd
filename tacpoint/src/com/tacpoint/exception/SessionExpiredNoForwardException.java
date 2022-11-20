 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * SessionExpiredNoForwardException.java
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
 
  public class SessionExpiredNoForwardException extends BusinessException
  {
    /**
     * The constructor
     *
     * @param String message  error message
     *
     */
    public SessionExpiredNoForwardException(String message) 
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
    public SessionExpiredNoForwardException(String message, Exception e)
    {
        super(message, e);
    }
    
  }
