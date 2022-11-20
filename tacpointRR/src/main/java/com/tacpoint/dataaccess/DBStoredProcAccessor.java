 package com.tacpoint.dataaccess;

 /** 
  * DBStoredProcAccessor.java
  * <P>
  *   The interface for all DBStoredProcAccessor
  * <P>
  *
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The interface for all Stored Procedure Accessors.
  */

 
 import java.sql.*;
 
 import com.tacpoint.exception.BusinessException;
 
  
  public interface DBStoredProcAccessor extends DBSQLAccessor
  {
    /**
     *  Return a result set from the stored Procedure
     *
     *  @param CallableStatement input CallableStatement
     *  @return ResultSet  return result set
     *
     */
     
     public ResultSet getResultSet(CallableStatement inStmt);

 
  }
  