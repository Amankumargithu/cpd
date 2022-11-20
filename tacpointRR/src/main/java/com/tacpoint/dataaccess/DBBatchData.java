 package com.tacpoint.dataaccess;
 
 import java.util.Vector;

import com.tacpoint.common.DefaultObject;
import com.tacpoint.exception.BusinessException;
 
 
 /** 
  * DBBatchData.java
  * <P>
  *    The batch data to be used by the batch Data handler.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @version 1.0
  * Date created: 10/17/2001
  * Date modified:
  * - 10/17/2001 Initial version
  */
  
 
  public class DBBatchData extends DefaultObject
  {

      private String key = null;
      private Vector boList = null;
      private DBSQLAccessor accessor = null;
      private Object wherebo = null;
      private Object businessobj = null;


      /**
       *
       * Constructor.  Also instantiating the accessor class.
       * @param String key The name of the accessor, including package name.
       * @param Vector boList, the vector of the business objects for the accessor.
       * @exception BusinessException
       */
      public DBBatchData(DBSQLAccessor accessor,
                         Vector boList,
                         Object wherebo,
                         Object businessobj) throws BusinessException
      {
          this.key = (accessor.getClass()).getName();
          this.boList = boList;
          this.wherebo = wherebo;
          this.businessobj = businessobj;
          
          this.accessor = accessor;
      }
      
 
      /**
       *
       * Get the accessor.
       * @return DBSQLAccessor The Accessor object
       */
      public DBSQLAccessor getAccessor() 
      {
           return this.accessor;
      }
      

      /**
       *
       * Get the business object list
       * @return Vector The business object list
       */
      public Vector getBOList() 
      {
           return this.boList;
      }

      /**
       *
       * Get the business object for parameter
       * @return Object The business object list
       */
      public Object getBusinessObj() 
      {
           return this.businessobj;
      }      
      

      /**
       *
       * Get the where object
       * @return Object The where object
       */
      public Object getWhereBO() 
      {
           return this.wherebo;
      }      
     
    
  }
  
       
       
       
       