package com.tacpoint.dataaccess;

import java.sql.*;
import java.util.Vector;
import java.util.Enumeration;

import com.tacpoint.common.*;
import com.tacpoint.exception.*;
import com.tacpoint.util.*;

import com.tacpoint.dataconnection.DBConnectionManager;

 /** 
  * JDBCSQLExecutor.java
  * <P>
  *    The Class is responsible for executing the Accessor to retrieve data from
  *    database.  The class supports both PreparedStatement and CallableStatement.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  */
public class JDBCSQLExecutor extends DefaultObject
{  
   /** connection used to execute statements */
   transient Connection con;

   /**
    * Constructor to allow passing of a java.sql.Connection
    *
    * @param	aConnection	a connection to use for executing statements
    */
   public JDBCSQLExecutor() {
   }

   /**
    * Select from a database using an access object to fill an object
    *
    * @param    access          the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    aBusinessObject the business object to be populated
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeSingleSelect(DBSQLAccessor accessor, Object whereBO, Object businessObject) throws BusinessException
   {
    
      if ((accessor == null) || (businessObject == null) || (whereBO == null))
      {
           throw new BusinessException("Accessor, Condition Object, or BusinessObject is null.");
      }
      
      ResultSet rs = null;
      PreparedStatement preparedStmt = null;
      try {
        
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
        
        accessor.setStmtValues(preparedStmt, whereBO, businessObject);

        preparedStmt.execute();
        rs = preparedStmt.getResultSet();
        if (rs.next()) {
          accessor.inflateBusinessObject(rs, businessObject);
        } else {
          throw new NoDataFoundException("No rows returned from single select");
        }
        
      } catch (SQLException ex) {
          testConnection(con);
          throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
     
        
      }
   }   

   /**
    * Select multiple instances from a database using an access object to
    * build a cursored result set to handle
    * each row in the result set
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object containing additional keys to the select clause
    * @param    maxFetches      the maximum number of rows to fetch
    * @param    Vector          object that handles each fetched row in
    *                           resultset
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeCursorSelect(DBSQLAccessor accessor, Object whereBO, Object businessObject, int maxFetches, Object resultVector) throws BusinessException
   {
    
      if ((accessor == null) || (resultVector == null))
      {
        throw new BusinessException("Accessor or result Vector is null.");
      }
      
      ResultSet rs = null;
      PreparedStatement preparedStmt = null;
      try {
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
      
        accessor.setStmtValues(preparedStmt, whereBO, businessObject);
        
        // set the maximum result rows.
        if (maxFetches < DataAccessConstant.MAXRESULTROW)
        {
            preparedStmt.setMaxRows(maxFetches);
        }
        else
        {
            preparedStmt.setMaxRows(DataAccessConstant.MAXRESULTROW);
        }
        
        preparedStmt.execute();
        rs = preparedStmt.getResultSet();
        Vector tempResult = (Vector)resultVector;
        while(rs.next())
        {
          Object singlerow = accessor.inflateBusinessObject(rs);
          if (singlerow == null) continue;
          
          tempResult.addElement(singlerow);
        }
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }

        releaseConnection(accessor);
        
      }
   }   

   /**
    * Select multiple instances from a database using an access object to
    * build a cursored result set to handle
    * each row in the result set
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object containing additional keys to the select clause
    * @param    beginRow        the first row to fetch in a range
    * @param    endRow          the last row to fetch in a range
    * @param    Vector          object that handles each fetched row in
    *                           resultset
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeCursorSelect(DBSQLAccessor accessor, Object whereBO, Object businessObject, int beginRow, int endRow, Object resultVector) throws BusinessException
   {
    
      if ((accessor == null) || (resultVector == null))
      {
        throw new BusinessException("Accessor or result Vector is null.");
      }
      
      ResultSet rs = null;
      PreparedStatement preparedStmt = null;
      try {
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
      
        accessor.setStmtValues(preparedStmt, whereBO, businessObject);
        
        // set the maximum result rows.
        if (endRow < DataAccessConstant.MAXRESULTROW)
        {
          preparedStmt.setMaxRows(endRow);
        }
        else
        {
            preparedStmt.setMaxRows(DataAccessConstant.MAXRESULTROW);
        }
        
        preparedStmt.execute();
        rs = preparedStmt.getResultSet();

        int rowCount = 1;
        
        Vector tempResult = (Vector)resultVector;

        //find begin Row
        while ((rowCount < beginRow) && (rs.next())) 
           rowCount++;    
        
        // inflate only a fixed number of rows        
        while((rs.next()) && (rowCount <= endRow))
        {
          Object singlerow = accessor.inflateBusinessObject(rs);
          rowCount++;
          if (singlerow == null) continue;
          
          tempResult.addElement(singlerow);
        }
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }

        releaseConnection(accessor);
        
      }
   }   

   /**
    * Update an object from a database using an access object
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    aBusinessObject the business object to be updated
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeUpdate(DBSQLAccessor accessor, Object whereBO, Object businessObject) throws BusinessException
   {
      if ((accessor == null) || (whereBO == null) || (businessObject == null))
      {
        throw new BusinessException("Accessor, Condition Object, or Business Object is null.");
      }
      
      PreparedStatement preparedStmt = null;
      try {

        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
        accessor.setStmtValues(preparedStmt, whereBO, businessObject);
        preparedStmt.executeUpdate();
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
        
      }
     
   }      

   /**
    * Delete an object from a database using an access object
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeDelete(DBSQLAccessor accessor, Object whereBO) throws BusinessException
   {
    
      if ((accessor == null) || (whereBO == null))
      {
        throw new BusinessException("Accessor or Condition Object is null.");
      }
      
      PreparedStatement preparedStmt = null;
      try {
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, null);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
        accessor.setStmtValues(preparedStmt, whereBO, null);
        preparedStmt.executeUpdate();
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
        
      }
      
   }      

   /**
    * Insert an object into a database using an access object
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  The value of the business object to be inserted
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeInsert(DBSQLAccessor accessor, Object whereBO, Object businessObject) throws BusinessException
   {  
    
      if ((accessor == null) || (businessObject == null))
      {
        throw new BusinessException("Accessor or Business Object is null.");
      }
      
      PreparedStatement preparedStmt = null;
      try {
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
        accessor.setStmtValues(preparedStmt, whereBO, businessObject);
        preparedStmt.executeUpdate();
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
        
      }
      
   }


   /**
    * Insert multiple objects into a database using an access object
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object
    * @param    resultVector    The vector contains business objects to be inserted
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeInsert(DBSQLAccessor accessor, Object whereBO, Object businessObject, Object resultVector) throws BusinessException
   {  
    
      if ((accessor == null) || (businessObject == null))
      {
        throw new BusinessException("Accessor or Business Object is null.");
      }
      
      PreparedStatement preparedStmt = null;
      
      try {
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
      
        int vectorsize = ((Vector)resultVector).size();
      
        if (vectorsize != 0)
        {
            Object processObject = null;
            for(int i=0; i<vectorsize; ++i)
            {
                processObject = ((Vector)resultVector).elementAt(i);        
                accessor.setStmtValues(preparedStmt, whereBO, processObject);
                preparedStmt.executeUpdate();
            }
        }
        
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
        
      }
      
   }

   /**
    * Select the result count from the accessor execution.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object containing additional keys to the select clause
    * @return   long            the number of records in the result set.
    * @exception        BusinessException    if there is a database-related problem
    */
   public long executeResultRowCount(DBSQLAccessor accessor,
                                     Object whereBO,
                                     Object businessObject) throws BusinessException
   {
    
      if (accessor == null)
      {
        throw new BusinessException("Accessor or result Vector is null.");
      }
      long numberOfCount = 0;
      ResultSet rs = null;
      PreparedStatement preparedStmt = null;
      try {
 
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        preparedStmt = con.prepareStatement(tempStmt);
      
        accessor.setStmtValues(preparedStmt, whereBO, businessObject);
        
        preparedStmt.execute();
        rs = preparedStmt.getResultSet();
        while(rs.next())
        {
          numberOfCount++;    
        }
        
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (preparedStmt!= null) 
          try { preparedStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
        
      }
      
      return numberOfCount;
   }   
 
   /**
    * Execute a Store Procedure for single select
    * delete, update or single insert the database by stored proc.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object to be filled up
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeStoreProcedure(DBStoredProcAccessor accessor, Object whereBO, Object businessObject) throws BusinessException
   {
    
        if (accessor == null)
        {
            throw new BusinessException("Accessor is null.");
        }
        
        if (businessObject == null)
        {
            throw new BusinessException("Business Object is null.");
        }
        
        CallableStatement callableStmt = null;
        ResultSet rs = null;
        try {
        // get the named connection from the connection manager
            con = getConnection(accessor);

            String tempStmt = accessor.getStmt();
            if (tempStmt == null || "".equals(tempStmt))
            {
               tempStmt = accessor.getStmt(whereBO, businessObject);
            }
        
            callableStmt = con.prepareCall(tempStmt);
            accessor.setStmtValues(callableStmt, whereBO, businessObject);

            callableStmt.execute();
            
            int updateCount = callableStmt.getUpdateCount();
            
            //if the result is null, it will be insert, delete, or update
            // a result set is returned and not null, it is select
            rs = accessor.getResultSet(callableStmt);
            if (rs != null)
            {
                  if(rs.next())
                  {
                      accessor.inflateBusinessObject(rs, businessObject);
                  } else {
                    throw new NoDataFoundException("No rows returned from single select");
                  }
            }
            
            
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised: " + ex.getMessage(), ex);
      } finally {
        if (callableStmt != null) 
          try { callableStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }

        releaseConnection(accessor);
        
      }
   }   


 
   /**
    * Execute a Store Procedure for multiple insert.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object to be filled up
    * @param    resultVector    the result vector for more than one object
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeStoreProcedure(DBStoredProcAccessor accessor, Object whereBO, Object businessObject, Object resultVector) throws BusinessException
   {
    
        if (accessor == null)
        {
            throw new BusinessException("Accessor is null.");
        }
        
        if (resultVector == null)
        {
            throw new BusinessException("resultVector is null.");
        }
        
        if (businessObject == null)
        {
            throw new BusinessException("BusinessObject is null.");
        }
        
        
        CallableStatement callableStmt = null;
        ResultSet rs = null;
        try {
        // get the named connection from the connection manager
            con = getConnection(accessor);
        
            String tempStmt = accessor.getStmt();
            if (tempStmt == null || "".equals(tempStmt))
            {
               tempStmt = accessor.getStmt(whereBO, businessObject);
            }
        
            callableStmt = con.prepareCall(tempStmt);
            
            int vectorSize = ((Vector)resultVector).size();
            
            Object processObject = null;
            
            if (vectorSize != 0)
            {
                // if vector size is not zero, this is assumed for updating or inserting
                // to database with those objects in the vector.
                for (int i=0; i<vectorSize; ++i)
                {
                    processObject = ((Vector)resultVector).elementAt(i);
                    accessor.setStmtValues(callableStmt, whereBO, processObject);

                    callableStmt.execute();
                }
            }
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised: " + ex.getMessage(), ex);
      } finally {
        if (callableStmt != null) 
          try { callableStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }
          
        releaseConnection(accessor);
        
      }
   }   



 
   /**
    * Execute a Store Procedure for multiple select.
    * The result set is set to the maximum number before selection.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object to be filled up
    * @param    maxFetches      The maximum number of rows to be retrieved
    * @param    resultVector    the result vector for more than one object
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeStoreProcedure(DBStoredProcAccessor accessor, Object whereBO, Object businessObject, int maxFetches, Object resultVector) throws BusinessException
   {
    
        if (accessor == null)
        {
            throw new BusinessException("Accessor is null.");
        }
        
        if (resultVector == null)
        {
            throw new BusinessException("resultVector is null.");
        }
        
        if (businessObject == null)
        {
            throw new BusinessException("BusinessObject is null.");
        }
        
        
        boolean doLog=Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
        
        CallableStatement callableStmt = null;
        int nbrOfResult = 0;
        
        ResultSet rs = null;
        try {
        // get the named connection from the connection manager
            con = getConnection(accessor);
        
            String tempStmt = accessor.getStmt();
            if (tempStmt == null || "".equals(tempStmt))
            {
               tempStmt = accessor.getStmt(whereBO, businessObject);
            }
        
            callableStmt = con.prepareCall(tempStmt);
            
        // set the maximum result rows.
            if (maxFetches < DataAccessConstant.MAXRESULTROW)
            {
                nbrOfResult = maxFetches;
            }
            else
            {
                nbrOfResult = DataAccessConstant.MAXRESULTROW;
            }

            // Logger.debug("JDBCSQLExecutor.executeStoredProcedure: Max rows return = "+callableStmt.getMaxRows(),doLog); 
        
            accessor.setStmtValues(callableStmt, whereBO, businessObject);

            callableStmt.execute();
            
            int updateCount = callableStmt.getUpdateCount();
            
            //Logger.debug("JDBCSQLExecutor.executeStoredProcedure: update count = "+updateCount,doLog); 

            rs = accessor.getResultSet(callableStmt);
            
            Vector tempResult = (Vector)resultVector;

            // if result set == null, no data found.
            if (rs != null)
            {
                 //Logger.debug("JDBCSQLExecutor.executeStoredProcedure: rs != null",doLog); 
                 int i = 0;
                 
                 while((rs.next()) && (i < nbrOfResult))
                 {
                      Object singlerow = accessor.inflateBusinessObject(rs);
                      ++i;
                      if (singlerow == null) continue;
                      
                      tempResult.addElement(singlerow);
                 }

            }
            else
            {
                 //Logger.debug("JDBCSQLExecutor.executeStoredProcedure: rs = null",doLog); 
                 throw new NoDataFoundException("No rows returned from cursor select");
            }


      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised: " + ex.getMessage(), ex);
      } finally {
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }
        if (callableStmt != null) 
          try { callableStmt.close(); } catch (SQLException ex) { }

        releaseConnection(accessor);
        
      }
   }   

   /**
    * Execute a Store Procedure for multiple select.
    * The result set is set to the maximum number before selection.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object to be filled up
    * @param    beginRow        The first record of a range to retrieve.  The default is 0
    * @param    endRow          The last record of a range to retrieve.  The default is Integer.MaxValues
    * @param    resultVector    the result vector for more than one object
    * @exception        BusinessException    if there is a database-related problem
    */
   public void executeStoreProcedure(DBStoredProcAccessor accessor, Object whereBO, Object businessObject, int beginRow, int endRow, Object resultVector) throws BusinessException
   {
    
        if (accessor == null)
        {
            throw new BusinessException("Accessor is null.");
        }
        
        if (resultVector == null)
        {
            throw new BusinessException("resultVector is null.");
        }
        
        if (businessObject == null)
        {
            throw new BusinessException("BusinessObject is null.");
        }
        
        
        boolean doLog=Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
        
        CallableStatement callableStmt = null;
        int nbrOfResult = 0;
        
        ResultSet rs = null;
        try {
        // get the named connection from the connection manager
            con = getConnection(accessor);
        
            String tempStmt = accessor.getStmt();
            if (tempStmt == null || "".equals(tempStmt))
            {
               tempStmt = accessor.getStmt(whereBO, businessObject);
            }
        
            callableStmt = con.prepareCall(tempStmt);
            
            // set the endRow.
            if (endRow > DataAccessConstant.MAXRESULTROW)
            {
                endRow = DataAccessConstant.MAXRESULTROW;
            }

            // set the beginRow.
            if (beginRow < 0)
            {
                beginRow = 0;
            }

            // Logger.debug("JDBCSQLExecutor.executeStoredProcedure: Max rows return = "+callableStmt.getMaxRows(),doLog); 
        
            accessor.setStmtValues(callableStmt, whereBO, businessObject);

            callableStmt.execute();
            
            int updateCount = callableStmt.getUpdateCount();
            
            //Logger.debug("JDBCSQLExecutor.executeStoredProcedure: update count = "+updateCount,doLog); 

            rs = accessor.getResultSet(callableStmt);
            
            Vector tempResult = (Vector)resultVector;

            // if result set == null, no data found.
            if (rs != null)
            {
                 //Logger.debug("JDBCSQLExecutor.executeStoredProcedure: rs != null",doLog); 
                 int i = 0;
  
                 //find begin Row
                 while ((i < beginRow) && (rs.next())) 
                     ++i;    

                 // inflate only a fixed number of rows        
                 while((rs.next()) && (i <= endRow))
                 {
                     Object singlerow = accessor.inflateBusinessObject(rs);
                     ++i;
                     if (singlerow == null) continue;

                     tempResult.addElement(singlerow);
                 }

            }
            else
            {
                 //Logger.debug("JDBCSQLExecutor.executeStoredProcedure: rs = null",doLog); 
                 throw new NoDataFoundException("No rows returned from cursor select");
            }


      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised: " + ex.getMessage(), ex);
      } finally {
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }
        if (callableStmt != null) 
          try { callableStmt.close(); } catch (SQLException ex) { }

        releaseConnection(accessor);
        
      }
   }   

   /**
    * Select the result count from the accessor execution.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @param    whereBO         the business object containing the keys to
    *                           go into the &quot;WHERE&quot; clause
    * @param    businessObject  the business object containing additional keys to the select clause
    * @return   long            the number of records in the result set.
    * @exception        BusinessException    if there is a database-related problem
    */
   public long executeResultRowCount(DBStoredProcAccessor accessor,
                                     Object whereBO,
                                     Object businessObject) throws BusinessException
   {
    
      if (accessor == null)
      {
        throw new BusinessException("Accessor or result Vector is null.");
      }
      long numberOfCount = 0;
      ResultSet rs = null;
      CallableStatement callableStmt = null;
      try {
 
        // get the named connection from the connection manager
        con = getConnection(accessor);
        
        String tempStmt = accessor.getStmt();
        if (tempStmt == null || "".equals(tempStmt))
        {
            tempStmt = accessor.getStmt(whereBO, businessObject);
        }
        
        callableStmt = con.prepareCall(tempStmt);
      
        accessor.setStmtValues(callableStmt, whereBO, businessObject);
        
        callableStmt.execute();
            
        rs = accessor.getResultSet(callableStmt);

            // if result set == null, no data found.
        if (rs != null)
        {
            while(rs.next())
            {
              numberOfCount++;    
            }
        }
        else
        {
             throw new NoDataFoundException("No rows returned from cursor select");
        }
        
      } catch (SQLException ex) {
        testConnection(con);
        throw new DatabaseException("SQL Exception raised", ex);
      } finally {
        if (callableStmt != null) 
          try { callableStmt.close(); } catch (SQLException ex) { }
        if (rs != null) 
          try { rs.close(); } catch (SQLException ex) { }
        releaseConnection(accessor);
        
      }
      
      return numberOfCount;

   }    

   /**
    * Execute the Batch update operations.  It only applied to update, 
    * delete, or insert operations.
    *   
    * @param DBBatchDataHandler  The handler of the batch data to be used by this operation.
    * @exception BusinessException    if there is a database-related problem
    */
   public void executeBatch(DBBatchDataHandler handler,
                             Connection aConnection) throws BusinessException
   {
      if ((handler == null) || (aConnection == null))
      {
        throw new BusinessException("JDBCSQLExecutor.executeBatch, handler or connection is null.");
      }
      
      String key = null;
      Enumeration enumofKeys = handler.keys();

      PreparedStatement preparedStmt = null;
      Statement stmt = null;

      try {

          if (handler.needToSetValues())
          {
              Object businessObject = null;
              Object whereObject = null;
              while(enumofKeys.hasMoreElements())
              {
        
                  key = (String)enumofKeys.nextElement();
                  DBBatchData bdata = handler.get(key);
                  if (bdata == null)
                  {
                     Logger.log("JDBCSQLExecutor.executeBatch unable to locate batch data with key: "+key);
                     continue;
                  }
                  DBSQLAccessor accessor = bdata.getAccessor();

                  Vector bolist = bdata.getBOList();
                  if (bolist == null)
                  {
                      Logger.log("JDBCSQLExecutor.executeBatch key: "+key+" has no bo list.");
                      continue;
                  }
                
                  int listSize = bolist.size();
                  String tempStmt = null;

                  try
                  {
                     if (listSize > 0)
                     {
                        businessObject = bdata.getBusinessObj();
                        whereObject = bdata.getWhereBO();
                        tempStmt = accessor.getStmt();
                        if (tempStmt == null || "".equals(tempStmt))
                        {
                           tempStmt = accessor.getStmt(whereObject, businessObject);
                        }
                    
                        preparedStmt = aConnection.prepareStatement(tempStmt);
                    
                        for (int i=0; i< listSize; ++i)
                        {
                           businessObject = bolist.elementAt(i);
                           accessor.setStmtValues(preparedStmt, whereObject, businessObject);
                           preparedStmt.addBatch();
                        }
                  
                        preparedStmt.executeBatch();
                     }
                  } catch (Exception ex) {
                        Logger.log("JDBCSQLExecutor.executeBatch exception in preparedStatement processing.", ex);
                        throw new DatabaseException("SQL Exception raised", ex);
                  } finally {
                    if (preparedStmt!= null)
                    { 
                        try { preparedStmt.close(); } catch (SQLException ex) { }
                        preparedStmt = null;
                    }
                  }
              }
          }
          else
          {

              stmt = aConnection.createStatement();

              while(enumofKeys.hasMoreElements())
              {
        
                  key = (String)enumofKeys.nextElement();
                  DBBatchData bdata = handler.get(key);
                  if (bdata == null)
                  {
                     Logger.log("JDBCSQLExecutor.executeBatch unable to locate batch data with key: "+key);
                     continue;
                  }
          
                  DBSQLAccessor accessor = bdata.getAccessor();
          
                  Vector bolist = bdata.getBOList();
                  if (bolist == null)
                  {
                      Logger.log("JDBCSQLExecutor.executeBatch key: "+key+" has no bo list.");
                      continue;
                  }
                
                  int listSize = bolist.size();
                  String tempStmt = null;
              
                  if (listSize > 0)
                  {
                 
                    Object businessObject = null;
                    Object whereObject = bdata.getWhereBO();


                    for (int i=0; i< listSize; ++i)
                    {
                       businessObject = bolist.elementAt(i);
                       tempStmt = accessor.getStmt();
                       if (tempStmt == null || "".equals(tempStmt))
                       {
                          tempStmt = accessor.getStmt(whereObject, businessObject);
                       }

                       stmt.addBatch(tempStmt);
                    }
                  }
              }

              stmt.executeBatch();
          }
      } catch (Exception ex) {
             Logger.log("JDBCSQLExecutor.executeBatch exception in statement processing.", ex);
             throw new DatabaseException("SQL Exception raised", ex);
      } finally {
         if (preparedStmt!= null) 
              try { preparedStmt.close(); } catch (SQLException ex) { }
         if (stmt!= null) 
              try { stmt.close(); } catch (SQLException ex) { }
      }
   }


   /**
    * get a connection.  If the accessor has a connection, the process uses it.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @exception        SQLException    if there is a database-related problem
    */
 
   private Connection getConnection(DBSQLAccessor accessor) throws SQLException
   {
    
        Connection aConnection = accessor.getConnection();
        if (aConnection == null)
        {
            DBConnectionManager connMgr =  DBConnectionManager.getInstance();
            aConnection = connMgr.getConnection("oracle",1000);
            
        }
        
        return aConnection;
   }

   /**
    * Gets a database connection from pool.
    *
    * @exception SQLException    if there is a database-related problem
    */
   public Connection getConnection() throws SQLException
   {

      DBConnectionManager connMgr =  DBConnectionManager.getInstance();
      Connection aConnection = connMgr.getConnection("oracle",1000);

      return aConnection;
   }


   /**
    * Release the Connection.  If the accessor has a connection, don't do anything. 
    * The connection will be handled by other calling modules.
    *
    * @param    accessor        the access object to delegate statement
    *                           preparation and result handling to
    * @exception        BusinessException    if there is a database-related problem
    */
 
   private void releaseConnection(DBSQLAccessor accessor)
   {
        if (accessor != null)
        {
            Connection aConnection = accessor.getConnection();
            if (aConnection == null)
            {
                DBConnectionManager connMgr =  DBConnectionManager.getInstance();
                connMgr.freeConnection("oracle", con);
            }
        }
   }

   /**
    * Release the given database Connection.
    *
    * @param aConnection  The database connection to release.
    */
   public void releaseConnection(Connection aConnection)
   {
      if (aConnection != null)
      {
         DBConnectionManager connMgr =  DBConnectionManager.getInstance();
         connMgr.freeConnection("oracle", aConnection);
      }
   }


   /**
    * Test the Connection.  If the execution experienced a SQLExeception, test
    * the connection to make sure it is fine.  Otherwise, close the connection.
    *
    * @param    Connection      the connection to be tested.  If exception happens, closed the connection
    *
    */
 
   private void testConnection(Connection aConnection)
   {
        if (aConnection != null)
        {
            try
            {
                Statement stmt = aConnection.createStatement();
                String query = "select sysdate from dual";
            
                ResultSet rs = stmt.executeQuery(query);
                if (rs != null) 
                    try { rs.close(); } catch (SQLException ex) { }
                
            } catch (SQLException e)
            {
                Logger.log("JDBCSQLExecutor.testConnection has exception.  Assume the connection is not good.", e);
                try
                {
                   aConnection.close();
                } catch (Exception e1)
                {
                    Logger.log("JDBCSQLExecutor.testConnection has exception.  Unable to close a connection.", e1);

                    try
                    {
                        Logger.log("JDBCSQLExecutor in testConnection exception: is connection closed="+aConnection.isClosed());
                    } catch (Exception e2)
                    {
                        Logger.log("JDBCSQLExecutor testConnection error in testconnection closed.", e2);
                    }
                    
                }
            }
        }

   }
        
        
}
