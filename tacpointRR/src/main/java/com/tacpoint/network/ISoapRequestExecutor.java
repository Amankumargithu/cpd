package com.tacpoint.network;

import java.sql.*;
import java.util.Vector;

import com.tacpoint.common.*;
import com.tacpoint.exception.*;
import com.tacpoint.util.*;

import java.net.*;

 /** 
  * ISoapRequestExecutor
  * <P>
  *    The Class is the interface which responsible for executing the SOAP Accessor to retrieve
  *    data from a service.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Tacpoint Technologies, Inc.
  * @version 1.0
  * Date created: 2/14/2002
  * Date modified:
  * - 2/14/2002 Initial version
  */
public interface ISoapRequestExecutor
{  

   /**
    * Select from a database using an access object to fill an object
    *
    * @param    String soapAccessorName   the access name to delegate detail
    *                           preparation and result handling to
    * @exception  ApplicationException    if there is a data-related problem
    */
    public void execute(String soapAccessorName) throws ApplicationException;
        
}
