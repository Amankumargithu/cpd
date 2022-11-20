package com.tacpoint.network;

import com.tacpoint.common.*;
import com.tacpoint.exception.*;
import com.tacpoint.util.*;


 /** 
  * DefaultSoapRequestExecutor
  * <P>
  *    The Class is responsible for executing the SOAP Accessor to retrieve data from
  *    a service.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Tacpoint Technologies, Inc.
  * @version 1.0
  * Date created: 2/14/2002
  * Date modified:
  * - 2/14/2002 Initial version
  */
public class DefaultSoapRequestExecutor extends DefaultObject implements ISoapRequestExecutor 
{  
   /**
    * Constructor to allow passing of a java.sql.Connection
    *
    * @param	aConnection	a connection to use for executing statements
    */
   public DefaultSoapRequestExecutor() {
       super();
   }

   /**
    * execute a soap call and delegate all data manipulation to the accessor
    *
    * @param    String soapAccessorName    the access object to delegate data
    *                           preparation and result handling to
    * @exception  ApplicationException    if there is a data-related problem
    */
    public void execute(String soapAccessorName) throws ApplicationException
    {
        
    }
    
        
        
}
