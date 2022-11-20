package com.tacpoint.dataaccess;

import java.sql.*;
import java.util.Vector;

import com.tacpoint.common.*;
import com.tacpoint.exception.*;
import com.tacpoint.util.Logger;

import com.tacpoint.dataconnection.DBConnectionManager;

 /** 
  * DefaultObject.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  */
public class DynamicJDBCSQLExecutor extends DefaultObject
{  
	/** connection used to execute statements */
	transient Connection mConnection;

	/**
	 * Constructor to allow passing of a java.sql.Connection
	 *
	 * @param	aConnection	a connection to use for executing statements
	 */
	public DynamicJDBCSQLExecutor() {
	}

	/**
	 * Execute a Store Procedure
	 *
	 * @param	 aAccessor    the access object to delegate statement
	 *								  preparation and result handling to
	 * @param	 aWhereBO     the business object containing the keys to
	 *								  go into the &quot;WHERE&quot; clause
	 * @param	 aBusinessObject the business object to be filled up
	 * @param	 aResultVector	 the result vector for more than one object
	 * @exception			BusinessException	if there is a database-related problem
	 */
	public void executeStoreProcedure(DynamicDBStoredProcAccessor aAccessor,
										Object aWhereBO, Object aBusinessObject,
										Object aResultVector) throws BusinessException
	{
		//Logger.log("aAccessor class name"+aAccessor.getClass().getName());
		
		//aAccessor class name=com.b4utrade.dataaccess.DatafeedStoredProcAccessor
		//System.out.println(aAccessor.getStmt(aBusinessObject));
		if (aAccessor == null)
		{
			String vMsg = "DynamicJDBCSQLExecutor.executeStoreProcedure(): ";
			vMsg += "parameter [aAccessor] was null.";
			throw new BusinessException(vMsg);
		}
		if (aBusinessObject == null)
		{
			String vMsg = "DynamicJDBCSQLExecutor.executeStoreProcedure(): ";
			vMsg += "parameter [aBusinessObject] was null.";
			throw new BusinessException(vMsg);
		}

		ResultSet vRS = null;
		CallableStatement vCallableStmt = null;
		try
		{
			// get the named connection from the connection manager
			//Logger.log("Connection"+mConnection);
			mConnection = getConnection(aAccessor);

            while(null == mConnection){
	              mConnection = getConnection(aAccessor);
                                    }
			vCallableStmt = mConnection.prepareCall(
												aAccessor.getStmt(aBusinessObject));
			
			aAccessor.setStmtValues(vCallableStmt, aWhereBO, aBusinessObject);
			vCallableStmt.execute();

			int vUpdateCount = vCallableStmt.getUpdateCount();
			if (vUpdateCount == -1)
			{
				if (aResultVector == null)
				{
					String vMsg = "DynamicJDBCSQLExecutor.executeStoreProcedure(): ";
					vMsg += "parameter [aResultVector] was null.";
					throw new BusinessException(vMsg);
				}

				vRS = aAccessor.getResultSet(vCallableStmt);
				if (vRS != null)
				{
					while(vRS.next())
					{
						Object vSinglerow = aAccessor.inflateBusinessObject(vRS);
						((Vector)aResultVector).addElement(vSinglerow);
					}
				}
			}
			vCallableStmt.close();
			vCallableStmt = null;
		}
		catch (SQLException e)
		{
			String vMsg = "DynamicJDBCSQLExecutor.executeStoreProcedure(): ";
			vMsg += "SQL Exception raised. " + e.getMessage();
			throw new DatabaseException(vMsg, e);
		}
		finally
		{
			if (vCallableStmt != null)
			{
				try
				{
					vCallableStmt.close();
					vCallableStmt = null;
				}
				catch (SQLException e) { }
			}
			if (vRS != null)
			{
				try
				{
					vRS.close();
				}
				catch (SQLException e) { }
			}

			releaseConnection(aAccessor);
		}
	}

	/**
	 * get a connection.  If the accessor has a connection, the process uses it.
	 *
	 * @param	 accessor		  the access object to delegate statement
	 *								  preparation and result handling to
	 * @exception			SQLException	 if there is a database-related problem
	 */
 
	private Connection getConnection(DBSQLAccessor aAccessor) throws SQLException
	{
        //Logger.log("getConnection called ");
		Connection aConnection = aAccessor.getConnection();
		if (aConnection == null)
		{
			DBConnectionManager vDBConnMgr = DBConnectionManager.getInstance();
			aConnection = vDBConnMgr.getConnection("oracle",1000);

		}
		  
		return aConnection;
	}


	/**
	 * Release the Connection.  If the accessor has a connection, don't do anything. 
	 * The connection will be handled by other calling modules.
	 *
	 * @param	 accessor		  the access object to delegate statement
	 *								  preparation and result handling to
	 * @exception			BusinessException	if there is a database-related problem
	 */
 
	private void releaseConnection(DBSQLAccessor aAccessor)
	{
		if (aAccessor != null)
		{
			Connection aConnection = aAccessor.getConnection();
			if (aConnection == null && mConnection != null)
			{
				DBConnectionManager vConnMgr =  DBConnectionManager.getInstance();
				vConnMgr.freeConnection("oracle", mConnection);
			}
		}
	}

}
