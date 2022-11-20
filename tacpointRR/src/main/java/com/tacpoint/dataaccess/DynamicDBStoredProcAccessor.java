 /** 
  * DynamicDBStoredProcAccessor.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kgentes@tacpoint.com
  * @version 1.0
  * Date created: 04/13/2000
  */

package com.tacpoint.dataaccess;

import java.sql.*;

import com.tacpoint.exception.BusinessException;


public interface DynamicDBStoredProcAccessor extends DynamicDBSQLAccessor
{
    /**
     * getResultSet()
     *
     *    Return a result set from the stored Procedure
     *
     *  @param CallableStatement input CallableStatement
     *  @return ResultSet  return result set
     *
     */

	public ResultSet getResultSet(CallableStatement inStmt);


}
