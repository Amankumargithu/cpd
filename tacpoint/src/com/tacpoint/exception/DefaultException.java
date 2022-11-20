 package com.tacpoint.exception;
 
 import java.lang.Exception;
 import java.io.Serializable;
 
 
 /** 
  * DefaultException.java
  * <P>
  *  Default Exception.  All other exceptions inherited from this class.
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

public class DefaultException extends Exception implements Serializable
{
    public DefaultException(String message)
    {
        super(message);
    }
    
    public DefaultException(String message, Exception e)
    {
        super(message + " " + e.getMessage());
    }
    
    
}
