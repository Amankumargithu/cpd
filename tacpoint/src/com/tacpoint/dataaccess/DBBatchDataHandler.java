 package com.tacpoint.dataaccess;
 
 import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.tacpoint.common.DefaultObject;
import com.tacpoint.exception.BusinessException;
 
 
 /** 
  * DBBatchDataHandler.java
  * <P>
  *    The db batch data handler handles data to be processed by 
  *    jdbc 2.0 batch processing.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @version 1.0
  * Date created: 10/17/2001
  * Date modified:
  * - 10/17/2001 Initial version
  */
  
 
  public class DBBatchDataHandler extends DefaultObject
  {

       private Hashtable accessorHash = null;
       private boolean   needToSetValues = false;

   /**
    * The constructor.
    */
       
       public  DBBatchDataHandler()
       {
           accessorHash = new Hashtable();
       }


   /**
    * This method adds the accessor and the vector.  The vector will be
    * used to create the batch.
    *
    * @param	accessor the name of the accessor including package name.
    * @param    Vector   the list of business Object to be insert in the batch.
    * @exception BusinessException when accessor or the vector is null.
    *
    */
       
       public void add(DBSQLAccessor accessor,
                       Vector boList,
                       Object wherebo,
                       Object businessobj,
                       boolean needToSetValues) throws BusinessException
       {
           if (accessorHash == null)
           {
               accessorHash = new Hashtable();
           }
           
           if (accessor == null || boList == null)
           {
               throw new BusinessException("DBBatchDataHandler: add(string, vector) method encounters error.");
           }
           
           add((accessor.getClass()).getName(), accessor, boList, wherebo, businessobj, needToSetValues);
           
       }



   /**
    * This method adds the accessor and the vector with the key that defined by the user.
    * The vector will be used to create the batch.
    *
    * @param    String   key the key to retrieve the accessor
    * @param	accessor the name of the accessor including package name.
    * @param    Vector   the list of business Object to be insert in the batch.
    * @param    Object   the wherebo the object to be used to build the statement
    * @param    Object   the businessobj the object to be used to build the statement
    * @param    boolean  the flag to tell the sql executor to use preparedStatement or normal statement
    * @exception BusinessException when accessor or the vector is null.
    *
    */
       
       public void add(String key,
                       DBSQLAccessor accessor,
                       Vector boList,
                       Object wherebo,
                       Object businessobj,
                       boolean needToSetValues) throws BusinessException
       {
           if (accessorHash == null)
           {
               accessorHash = new Hashtable();
           }
           
           if (accessor == null || boList == null || key == null)
           {
               throw new BusinessException("DBBatchDataHandler: add(string, vector) method encounters error.");
           }
           
           DBBatchData batchData = new DBBatchData(accessor, boList, wherebo, businessobj);
           this.needToSetValues = needToSetValues;
           
           accessorHash.put(key, batchData);
           
       }
       
       

   /**
    * Get the keys from the handler
    *
    * @return Enumeration of strings.
    *
    */
       public Enumeration keys()
       {
           return accessorHash.keys();
       }
       

   /**
    * Get the size from the handler
    *
    * @return int the size of the hash
    *
    */
       public int size()
       {
           return accessorHash.size();
       }
       
       
   /**
    * Get the DBBatchData which matched with key, the name of accessor
    * including the package name.
    *
    * @return DBBatchData the data to be processed.
    *
    */
       public DBBatchData get(String key)
       {
           return ((DBBatchData)accessorHash.get(key));
       }
       
       
   /**
    * Indicate this batch will use statement to process the whole batch
    *
    * @return boolean the indicator
    *
    */
       public boolean needToSetValues()
       {
           return this.needToSetValues;
       }
       
    
  }
  
       
       
       
       