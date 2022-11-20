 package com.tacpoint.exception;
 
 import java.lang.Exception;
 
 /** 
  * DataaccessException.java
  * <P>
  *   The exception is related to Database Access Error.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The generic exception for the database access.
  */
 
  public class DatabaseException extends BusinessException
  {
    public DatabaseException(String message) 
    {
        super(message);
    }
    
    public DatabaseException(String message, Exception e)
    {
        super(message, e);
    }
    

    
  }