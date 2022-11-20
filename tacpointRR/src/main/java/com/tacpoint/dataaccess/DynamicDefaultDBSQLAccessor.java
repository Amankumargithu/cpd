package com.tacpoint.dataaccess;
 
import com.tacpoint.exception.BusinessException;
 
import java.sql.*;
 
 
/** 
 * DynamicDefaultDBSQLAccessor.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Kim Gentes
 * @author kgentes@tacpoint.com
 * @version 1.0
 * Date created: 04/13/2000
 */
  
 
public class DynamicDefaultDBSQLAccessor extends DefaultDBSQLAccessor implements DynamicDBSQLAccessor
{

   /**
    * This method gets the sql statement for building a prepared statement
    *
    * @param  Object businessObject: the business object.
    * @return String  the sql statement
    * @exception BusinessException
    */

	public String getStmt(Object businessObject) throws BusinessException
	{
		return null;
	}

	public Object clone()
	{
		return( (DynamicDefaultDBSQLAccessor)super.clone());
	}


}
