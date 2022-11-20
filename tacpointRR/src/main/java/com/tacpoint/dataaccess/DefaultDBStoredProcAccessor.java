 package com.tacpoint.dataaccess;
 
 import com.tacpoint.exception.BusinessException;
 
 import java.sql.*;
 
 
 /** 
  * DefaultStoredProcAccessor.java
  * <P>
  *   This accessor is only applied to the accessor that use Stored Procedure.
  * <P>
  *
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  */
  
 
  public class DefaultDBStoredProcAccessor extends DefaultDBSQLAccessor implements DBStoredProcAccessor
  {
       
   /**
    * This method gets the result set from a Callable Statement.
    *
    * @param	callableStmt	a CallableStatement
    * @return   ResultSet       a result set
    */
    public ResultSet getResultSet(CallableStatement callableStmt)
    {
        try
        {
            
            return callableStmt.getResultSet();
        } catch (SQLException se) { return null;}
    }
    
    
               
   /**
    * This method clones the Accessor
    * @return	Object The Accessor
    */

    public Object clone()
    {
            return( (DefaultDBStoredProcAccessor)super.clone());
            
    }
    
    
               
   /**
    * This method returns true or false depends on the accessor 
    * whether the accessor is stored proc or not
    *
    * @return	boolean	a true value for the Stored Proc Accessor
    */

     public boolean isStoredProcAccessor()
     {
           return true;
     }

  
  }
  
       
       
       
       