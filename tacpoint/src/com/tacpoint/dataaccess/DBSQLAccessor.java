 package com.tacpoint.dataaccess;
 
 import java.sql.*;
 
 import com.tacpoint.exception.BusinessException;

 /** 
  * DBSQLAccessor.java
  * <P>
  *   The interface for all DBSQLAccessor which using the prepared statement
  *   to retrieve data from database.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The interface for all SQL Accessors.
  */
 
  
  public interface DBSQLAccessor 
  {
    /**
     *   To inflate a business object, and returns it to the calling object.  This method
     *   is used for multiple result objects.
     *
     *  @param ResultSet rs the result set from SQL statement.
     *  @return Object
     */
    public Object inflateBusinessObject(ResultSet rs) throws BusinessException;
    
    /**
     *   To inflate a business object which calls the Accessor.  This method
     *   is used for a single objects.
     *
     *   @param ResultSet rs the result set from SQL Statement.
     *   @param Object bo    the business object
     *
     */
     
    public void inflateBusinessObject(ResultSet rs, Object bo) throws BusinessException;
    
    /**
     *
     *   Set the Statement Value for Prepared Statement or Callable Statement.
     *
     *  @param PreparedStatement preparedStmt The prepareStatement for the SQL
     *  @param Object wherebo  the business object that contains criteria for the
     *                         where statement.
     *  @param Object businessObject the business object.
     *  @exception BusinessException
     */
    public void setStmtValues(Statement preparedStmt,
                                 Object whereBO,
                                 Object businessObject) throws BusinessException;
    /**
     *
     *     get the SQL statement
     * @return String the SQL Statement
     */
    public String getStmt();
    
    /**
     *
     *     get the SQL statement
     * @return String the SQL Statement
     */
    public String getStmt(Object whereBO, Object businessObject);
    
    /**
     *
     *    get the Connection for the SQL.
     *
     * @return Connection
     */
    public Connection getConnection();
    
    /**
     *
     *   set the Connection for the SQL.
     *
     * @param Connection
     */
    public void setConnection(Connection inConnection);
    
    
    /**
     *
     *   Clone the Accessor.  The Factory has an Accessor prototype, and when
     *   an Accessor is requested, the Factory clone an Accessor for the requesting
     *   object.
     *
     * @return Accessor
     */
    
    public Object clone();
    
    /**
     *
     *     Test if the accessor is coded for Stored Procedure, i.e. using Callable Statement.
     *
     * 
     */
    public boolean isStoredProcAccessor();
    
  }
  