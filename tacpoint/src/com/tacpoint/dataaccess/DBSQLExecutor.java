package com.tacpoint.dataaccess;

import com.tacpoint.common.DefaultObject;
import com.tacpoint.exception.BusinessException;

/** 
  * DBSQLExecutor.java
  * <P>
  *   The DBSQLAccessorExecutor is responsible for fulfilling the dataaccess request
  *   from the Business Object.  It completes the request by delegating to JDBCSQLExecutor.
  * <P>  
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  */

public class DBSQLExecutor extends DefaultObject
{
    
   /**
    * Used to execute a Select SQL statement that only retrieve one object from database.
    * The business Object that is requested the service is inflated.
    *
    * @param	accessor DBSQLAccessor  The accessor that the executor uses to fulfill the dataaccess request.
    * @param    whereBO  Object         The Business Object that contains the values of parameters for the dataaccess.
    * @param	businessObject Object   The Business Object that initiates the request and to be inflated.
    */
    
    public static void executeSingleSelect(DBSQLAccessor accessor,
                                           Object whereBO,
                                           Object businessObject) throws BusinessException
    {
        
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeSingleSelect(accessor, whereBO, businessObject);
    }
    
   /**
    * Used to execute a Select SQL statement that only retrieve more than one object from database.
    * The inflated objects are stored in the resultVector.
    *
    * @param	accessor DBSQLAccessor  The accessor that the executor uses to fulfill the dataaccess request.
    * @param    whereBO  Object         The Business Object that contains the values of parameters for the dataaccess.
    * @param    Object   businessObject The business Object.  Sometimes for capturing criteria
    * @param    maxFetches              The maximum number of records to retrieve.  The default is Integer.MaxValues
    * @param	businessObject Object   The Business Object that initiates the request and to be inflated.
    */
    
    public static void executeCursorSelect(DBSQLAccessor accessor,
                                           Object whereBO,
                                           Object businessObject,
                                           int maxFetches,
                                           Object resultVector) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeCursorSelect(accessor, whereBO, businessObject, maxFetches, resultVector);
    }
    
   /**
    * Used to execute a Select SQL statement that only retrieve more than one object from database.
    * The inflated objects are stored in the resultVector.
    *
    * @param	DBSQLAccessor accessor  The accessor that the executor uses to fulfill the dataaccess request.
    * @param    Object   whereBO        The Business Object that contains the values of parameters for the dataaccess.
    * @param    Object   businessObject The business Object.  Sometimes for capturing criteria
    * @param    int      beginRow       The first record of a range to retrieve.  The default is 0
    * @param    int      endRow         The last record of a range to retrieve.  The default is Integer.MaxValues
    * @param	Object   resultsVector  The object to capture result.
    */
    
    public static void executeCursorSelect(DBSQLAccessor accessor,
                                           Object whereBO,
                                           Object businessObject,
                                           int beginRow,
                                           int endRow,
                                           Object resultVector) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeCursorSelect(accessor, whereBO, businessObject, beginRow, endRow, resultVector);
    }

   /**
    * Used to execute a Update SQL statement for database.
    *
    * @param	accessor DBSQLAccessor  The accessor that the executor uses to fulfill the dataaccess request.
    * @param    whereBO  Object         The Business Object that contains the values of parameters for the dataaccess.
    * @param	businessObject Object   The Business Object that initiates the request and to be inflated.
    */
   
    public static void executeUpdate(DBSQLAccessor accessor,
                                     Object whereBO,
                                     Object businessObject) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeUpdate(accessor, whereBO, businessObject);
    }
    

    /**
     *
     *   Execute the delete SQL statement
     *
     * @param DBSQLAccessor accessor  The Accessor to be used
     * @param Object whereBO The Criteria Object to be used for populating conditions.
     * @exception BusinessException
     */
    public static void executeDelete(DBSQLAccessor accessor,
                                     Object whereBO) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeDelete(accessor, whereBO);
    }
    
    /**
     *
     *    Execute the single insert SQL statement
     *
     * @param DBSQLAccessor accessor The accessor to be used
     * @param Object whereBO The criteria Object to be used for populating conditions
     * @param Object businessObject The Business Object to be inserted to Database
     * @exception BusinessException 
     */
    public static void executeInsert(DBSQLAccessor accessor,
                                     Object whereBO,
                                     Object businessObject) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeInsert(accessor, whereBO, businessObject);
    }


    /**
     *
     *    Execute the multiple inserts SQL statement
     *
     * @param DBSQLAccessor accessor The accessor to be used
     * @param Object whereBO The criteria Object to be used for populating conditions
     * @param Object businessObject The Business Object to be inserted to Database
     * @param Object resultVector  The vector that contains all Business Objects to be inserted
     * @exception BusinessException 
     */
    public static void executeInsert(DBSQLAccessor accessor,
                                     Object whereBO,
                                     Object businessObject,
                                     Object resultVector) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeInsert(accessor, whereBO, businessObject, resultVector);
    }


    /**
     *
     *    Execute Store Procedure for single select, update, delete, and insert.
     *
     * @param DBSQLAccessor accessor The Accessor to be used
     * @param Object whereBO The criteria Object to be used for populating conditions
     * @param Object businessObject the business Object
     * @exception BusinessException
     */
    public static void executeStoreProcedure(DBStoredProcAccessor accessor,
                                             Object whereBO,
                                             Object businessObject) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeStoreProcedure(accessor, whereBO, businessObject);
    }
    
    
    /**
     *
     *    Execute Store Procedure for multiple insert.
     *
     * @param DBSQLAccessor accessor The Accessor to be used
     * @param Object whereBO The criteria Object to be used for populating conditions
     * @param Object businessObject the business Object
     * @param Object resultVector the result of the execution
     * @exception BusinessException
     */
    public static void executeStoreProcedure(DBStoredProcAccessor accessor,
                                             Object whereBO,
                                             Object businessObject,
                                             Object resultVector) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeStoreProcedure(accessor, whereBO, businessObject, resultVector);
    }
 
   
    /**
     *
     *    Execute Store Procedure for multiple select.
     *
     * @param DBSQLAccessor accessor The Accessor to be used
     * @param Object whereBO The criteria Object to be used for populating conditions
     * @param Object businessObject the business Object
     * @param int    maxFetches  the maximum return row of records
     * @param Object resultVector the result of the execution
     * @exception BusinessException
     */
    public static void executeStoreProcedure(DBStoredProcAccessor accessor,
                                             Object whereBO,
                                             Object businessObject,
                                             int    maxFetches,
                                             Object resultVector) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeStoreProcedure(accessor, whereBO, businessObject, maxFetches, resultVector);
    }

    /**
     *
     *    Execute Store Procedure for multiple select.
     *
     * @param DBSQLAccessor accessor The Accessor to be used
     * @param Object whereBO The criteria Object to be used for populating conditions
     * @param Object businessObject the business Object
     * @param int beginRow       The first record of a range to retrieve.  The default is 0
     * @param int endRow         The last record of a range to retrieve.  The default is Integer.MaxValues
     * @param Object resultVector the result of the execution
     * @exception BusinessException
     */
    public static void executeStoreProcedure(DBStoredProcAccessor accessor,
                                             Object whereBO,
                                             Object businessObject,
                                             int    beginRow,
                                             int    endRow,
                                             Object resultVector) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        executor.executeStoreProcedure(accessor, whereBO, businessObject, beginRow, endRow, resultVector);
    }

    public static long executeResultRowCount(DBSQLAccessor accessor,
                                     Object whereBO,
                                     Object businessObject) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        return executor.executeResultRowCount(accessor, whereBO, businessObject);
    }

    public static long executeResultRowCount(DBStoredProcAccessor accessor,
                                     Object whereBO,
                                     Object businessObject) throws BusinessException
    {
        JDBCSQLExecutor executor = new JDBCSQLExecutor();
        return executor.executeResultRowCount(accessor, whereBO, businessObject);
    }
     
    
}
    
    
    
    
    
    
        
    