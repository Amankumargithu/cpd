/** 
 * DynamicDBSQLAccessor.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Kim Gentes
 * @author kgentes@tacpoint.com
 * @version 1.0
 * Date created: 04/13/2000
 */

package com.tacpoint.dataaccess;
 
import java.sql.*;
 
import com.tacpoint.exception.BusinessException;
 
  
public interface DynamicDBSQLAccessor extends DBSQLAccessor
{

   /**
    * getStmt
    *
    *     get the SQL statement
    *
    * @param Object businessObject the business object.
    * @return String the SQL Statement
    * @exception BusinessException
    */
   public String getStmt(Object businessObject) throws BusinessException;

}
