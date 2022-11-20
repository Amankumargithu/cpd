 package com.tacpoint.dataaccess;

 import com.tacpoint.common.DefaultObject;
 import com.tacpoint.exception.BusinessException;

 import java.sql.Connection;


 /**
  * DefaultBusinessObject.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The base business object.
  */

public class DynamicDefaultBO extends DefaultBusinessObject
{
	// the connection object that uses for sql execution.
	private transient Connection connection = null;

	/**
	 * This method sets the connection
	 *
	 * @param	aConnection	a connection for the accessor
	 */

	public void setConnection(Connection inConnection)
	{
		connection = inConnection;
	}

	/**
	 * This method gets the connection
	 *
	 * @return	aConnection	a connection for the accessor
	 */

	public Connection getConnection()
	{
		return connection;
	}

	/**
	 * runStoreProcedure
	 *
	 *	 Runs a store procedure.
	 * @param int accessorKey The key to retrieve an Accessor
	 * @param Object whereBO The object to populate conditions
	 * @exception BusinessException
	 */

	protected void runStoreProcedure(int aAccessorKey, Object aWhereBO,
										Object aBusinessObject, Object aResultVector)
										throws BusinessException
	{
		DynamicDBStoredProcAccessor aAccessor = 
							(DynamicDBStoredProcAccessor)getAccessor(aAccessorKey);
		aAccessor.setConnection(getConnection());
		DynamicJDBCSQLExecutor vExecutor = new DynamicJDBCSQLExecutor();
		vExecutor.executeStoreProcedure(aAccessor, aWhereBO,
													aBusinessObject, aResultVector);
	}

	/**
	 * getAccessor
	 *
	 *	 Get an accessor from accessor Factory.
	 * @param int inKey The key to retrieve an Accessor
	 * @return DBSQLAccessor The Accessor object
	 * @exception BusinessException
	 */

	private DBSQLAccessor getAccessor(int inKey) throws BusinessException
	{
		AccessorFactory af = AccessorBroker.getAccessorFactory(this.getClass());
		if (af != null)
		{
			DBSQLAccessor accessor = af.getAccessor(inKey);
			return accessor;
		}
		else
		{
			throw new BusinessException("Accessor is not found for the key: " + inKey);
		}
	}

}
