 package com.tacpoint.dataaccess;
 
 import com.tacpoint.common.DefaultObject;
 import com.tacpoint.exception.BusinessException;
 
 import java.sql.*;
 
 
 /** 
  * DefaultDBSQLAccessor.java
  * <P>
  *    The default implementation of DBSQLAccessor.  Other Accessors that used Prepared
  *    Statements should be inherited from this class.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  */
  
 
  public class DefaultDBSQLAccessor extends DefaultObject implements DBSQLAccessor, Cloneable
  {
      // the connection object that uses for sql execution.
      private transient Connection con = null;
      
      
   /**
    * This method sets the connection
    *
    * @param	aConnection	a connection for the accessor
    */

     public void setConnection(Connection inputConnection)
     {
           con = inputConnection;
     }
     

           
   /**
    * This method gets the connection
    *
    * @return	aConnection	a connection for the accessor
    */

     public Connection getConnection()
     {
           return con;
     }

      
   /**
    * This method inflates a business object based on the values from the
    * database.  This method handles multiple row selections.
    *
    * @param	aResultSet	a resultSet that is returned from the database
    * @return   aBusinessObject a inflated business Object
    */
       
       public Object inflateBusinessObject(ResultSet rs) throws BusinessException
       {
           return null;
        }
       
       
   /**
    * This method builds a business object based on the values from the
    * database.  This method handles single object selection.
    *
    * @param	aResultSet	a resultSet that is returned from the database
    */
       
       public void inflateBusinessObject(ResultSet rs, Object businessObject) throws BusinessException
       {}       
   
   /**
    * This method sets the parameters' values for a prepared statement
    * before the prepared statement is run.
    *
    * @param	aPreparedStatment a prepared Statment that is to be run
    * @param    aWhereBO   a business object that contains the parameters'
    *                      values for the preparedStatement
    * @param    aBusinessObject a business object that could contains other parameters'
    *                      values for the preparedStatement
    */
   
       public void setStmtValues(Statement preparedStmt,
                                 Object whereBO,
                                 Object businessObject) throws BusinessException
       {}
  
 
   /**
    * This method gets the sql statement for building a prepared statement
    *
    * @return   String  the sql statement
    */
  
    public String getStmt()
    {
        return null;
    }

   /**
    * This method gets the sql statement for building a prepared statement
    *
    * @return   String  the sql statement
    */
  
    public String getStmt(Object whereBO, Object businessObject)
    {
        return null;
    }
    

   /**
    * This method clones the Accessor
    *
    * @return   Accessor the cloned accessor
    */       
    public Object clone()
    {
        try
        {
            DefaultDBSQLAccessor dba = (DefaultDBSQLAccessor)super.clone();
            dba.con = null;
            return dba;
        } catch (CloneNotSupportedException e)
        {
            return null;
        }
    }
        

           
   /**
    * This method returns true or false depends on the accessor 
    * whether the accessor is stored proc or not
    *
    * @return	false  The SQLAccessor returns false for the method
    */

     public boolean isStoredProcAccessor()
     {
           return false;
     }
        
    
  }
  
       
       
       
       