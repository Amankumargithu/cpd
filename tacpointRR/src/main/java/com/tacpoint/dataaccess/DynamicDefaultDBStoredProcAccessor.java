package com.tacpoint.dataaccess;
 
import com.tacpoint.exception.BusinessException;
 
import java.sql.*;
 
 
/** 
 * DynamicDefaultDBStoredProcAccessor.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Kim Gentes
 * @author kgentes@tacpoint.com
 * @version 1.0
 * Date created: 04/13/2000
 *   This accessor is only applied to the accessor that use Stored Procedure.
 */
  
 
public class DynamicDefaultDBStoredProcAccessor 
								extends DynamicDefaultDBSQLAccessor 
								implements DynamicDBStoredProcAccessor
{

	public ResultSet getResultSet(CallableStatement callableStmt)
	{
		try
		{

			return callableStmt.getResultSet();
		} catch (SQLException se) { return null;}
	}
	 
	public Object clone()
	{
		return( (DynamicDefaultDBStoredProcAccessor)super.clone());

	}

  
}
