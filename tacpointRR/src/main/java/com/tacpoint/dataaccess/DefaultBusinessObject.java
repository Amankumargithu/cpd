 package com.tacpoint.dataaccess;
 
 import com.tacpoint.common.DefaultObject;
 import com.tacpoint.exception.BusinessException;
 import com.tacpoint.exception.NoDataFoundException;
 import com.tacpoint.util.*;
 
 import java.sql.Connection;
 import java.util.Vector;
 
 
 
 /** 
  * DefaultBusinessObject.java
  * <P>
  *     DefaultBusinessObject is responsible for working with AccessorBroker
  *     and DBSQLExecutor to retrieve data from database.  It can run both
  *     preparestatement and stored procedure accessors.
  * <P>
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 12/27/1999
  * Date modified:
  * - 12/27/1999 Initial version
  *   The base business object.
  */
  
  public class DefaultBusinessObject extends DefaultObject
  {

          
      // the connection object that uses for sql execution.
      private transient Connection connection = null;

      private int id;
      
      public DefaultBusinessObject()
      {
      }
      
      public void setID(int inID)
      {
          id = inID;
      }
      
      public int getID()
      {
         return id;
      }

      
      public void setId(int inID)
      {
          id = inID;
      }
      
      public int getId()
      {
         return id;
      }

      
      
   /**
    * This method sets the connection
    *
    * @param	aConnection	a connection for the accessor
    */

     public void setConnection(Connection inConnection)
     {
           connection = inConnection;
     }
     

           
   /**
    * This method gets the connection
    *
    * @return	aConnection	a connection for the accessor
    */

     public Connection getConnection()
     {
           return connection;
     }
      
      /**
       * 
       *    Single Select.  
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The criteria object for conditions.
       * @exception BusinessException
       */
      protected void select(int accessorKey, Object whereBO) throws BusinessException
      {
             DBSQLAccessor accessor = getAccessor(accessorKey);
             select(accessor, whereBO);
      }

      
      /**
       * 
       *    Single Select.  
       *
       * @param String accessorName The Name to instantiate the accessor
       * @param Object whereBO The criteria object for conditions.
       * @exception BusinessException
       */
      protected void select(String accessorName, Object whereBO) throws BusinessException
      {
             DBSQLAccessor accessor = getAccessor(accessorName);
             select(accessor, whereBO);
      }

      
      /**
       * 
       *    Single Select.  
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The criteria object for conditions.
       * @exception BusinessException
       */
      protected void select(DBSQLAccessor accessor, Object whereBO) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                if (accessor.isStoredProcAccessor())
                {
                    runStoreProcedure((DBStoredProcAccessor)accessor, whereBO, this);
                }
                else
                {
                    DBSQLExecutor.executeSingleSelect(accessor, whereBO, this);
                }
            } 
            catch (NoDataFoundException ndfe)
            {
                Logger.log("DefaultBusinessObject.select No Rows Found for single select.");
                throw ndfe;
            }
            catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.select has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
            
      }
 
 
      /**
       *
       *
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  This cursor select will use default number
       *     of the returned result row for select operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @exception BusinessException
       */
      protected void select(int accessorKey, Object whereBO, Object resultVector) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorKey);
            cursorSelect(accessor, whereBO, resultVector, DataAccessConstant.MAXRESULTROW);
      }

      /**
       *
       *
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  This cursor select will use default number
       *     of the returned result row for select operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @param int beginResultRow The row number to begin selection from
       * @param int endResultRow The row number to end selection
       * @exception BusinessException
       */
      protected void select(int accessorKey, Object whereBO, Object resultVector, int beginResultRow, int endResultRow) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorKey);
            cursorSelect(accessor, whereBO, resultVector, beginResultRow, endResultRow);
      }
  
      /**
       *
       *
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  This cursor select will use default number
       *     of the returned result row for select operation.
       * @param String accessorName The Name to instantiate the accessor
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @exception BusinessException
       */
      protected void select(String accessorName, Object whereBO, Object resultVector) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorName);
            cursorSelect(accessor, whereBO, resultVector, DataAccessConstant.MAXRESULTROW);
      }

      
      /**
       * 
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  This cursor select will select the maximum number
       *     of the returned result row based on the number passed in the operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @param int numberOfResultRow The maximum number of result row returned from selection
       * @exception BusinessException
       */
      protected void select(int accessorKey, Object whereBO, Object resultVector, int numberOfResultRow) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorKey);
            cursorSelect(accessor, whereBO, resultVector, numberOfResultRow);
      }

      /**
       * 
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  This cursor select will select the maximum number
       *     of the returned result row based on the number passed in the operation.
       * @param String accessorName The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @param int numberOfResultRow The maximum number of result row returned from selection
       * @exception BusinessException
       */
      protected void select(String accessorName, Object whereBO, Object resultVector, int numberOfResultRow) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorName);
            cursorSelect(accessor, whereBO, resultVector, numberOfResultRow);
      }

      /**
       * 
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  This cursor select will select a range
       *     of result rows based on values passed in the operation.
       * @param String accessorName The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @param int beginResultRow The row number to begin selection from
       * @param int endResultRow The row number to end selection
       * @exception BusinessException
       */
      protected void select(String accessorName, Object whereBO, Object resultVector, int beginResultRow, int endResultRow) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorName);
            cursorSelect(accessor, whereBO, resultVector, beginResultRow, endResultRow);
      }

      /**
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  The Accessor can be
       *     an Accessor contains sql statement or stored procedure.
       * @param DBSQLAccessor accessor  The Accessor to retrieve the data
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @exception BusinessException
       */
      private void cursorSelect(DBSQLAccessor accessor, Object whereBO, Object resultVector, int numberOfResult) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                if (accessor.isStoredProcAccessor())
                {
                    DBSQLExecutor.executeStoreProcedure((DBStoredProcAccessor)accessor, whereBO, this, numberOfResult, resultVector);
                }
                else
                {
                    DBSQLExecutor.executeCursorSelect(accessor, whereBO, this, numberOfResult, resultVector);
                }
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.cursorSelect has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
      }
      
      /**
       *     Cursor Select.  The result vector contains all objects
       *     returned from the sql result.  The Accessor can be
       *     an Accessor contains sql statement or stored procedure.
       * @param DBSQLAccessor accessor  The Accessor to retrieve the data
       * @param Object whereBO The object to populate conditions
       * @param Object resultVector The object to capture result
       * @param int beginResultRow The row number to begin selection from
       * @param int endResultRow The row number to end selection
       * @exception BusinessException
       */
      private void cursorSelect(DBSQLAccessor accessor, Object whereBO, Object resultVector, int beginResultRow, int endResultRow) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                if (accessor.isStoredProcAccessor())
                {
                    DBSQLExecutor.executeStoreProcedure((DBStoredProcAccessor)accessor, whereBO, this, beginResultRow, endResultRow, resultVector);
                }
                else
                {
                    DBSQLExecutor.executeCursorSelect(accessor, whereBO, this, beginResultRow, endResultRow, resultVector);
                }
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.cursorSelect has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
      }
      
      /**
       * 
       *    Count Rows.  
       *
       * @param String accessorName The Class Name to retrieve an Accessor
       * @param Object whereBO The criteria object for conditions.
       * @exception BusinessException
       */
      protected long countRows(String accessorName, Object whereBO) throws BusinessException
      {
             DBSQLAccessor accessor = getAccessor(accessorName);
             return countRows(accessor, whereBO);
      }
      
      /**
       * 
       *    Count Rows.  
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The criteria object for conditions.
       * @exception BusinessException
       */
      protected long countRows(int accessorKey, Object whereBO) throws BusinessException
      {
             DBSQLAccessor accessor = getAccessor(accessorKey);
             return countRows(accessor, whereBO);
      }

      /**
       * Count Rows.  
       * @param DBSQLAccessor accessor  The Accessor to retrieve the data
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */
      private long countRows(DBSQLAccessor accessor, Object whereBO) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                if (accessor.isStoredProcAccessor())
                {
                    return DBSQLExecutor.executeResultRowCount((DBStoredProcAccessor)accessor, whereBO, this);
                }
                else
                {
                    return DBSQLExecutor.executeResultRowCount(accessor, whereBO, this);
                }
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.countRows has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
      }

      /**
       * 
       *   Single Update a table.  The business object should be
       *   inflated before doing this operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */

      protected void update(int accessorKey, Object whereBO) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorKey);
            update(accessor, whereBO);
      }

            
      /**
       * 
       *   Single Update a table.  The business object should be
       *   inflated before doing this operation.
       * @param String accessorName The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */

      protected void update(String accessorName, Object whereBO) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorName);
            update(accessor, whereBO);
      }

      /**
       * 
       *   Single Update a table.  The business object should be
       *   inflated before doing this operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */

      protected void update(DBSQLAccessor accessor, Object whereBO) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                if (accessor.isStoredProcAccessor())
                {
                    runStoreProcedure((DBStoredProcAccessor)accessor, whereBO, this);
                }
                else
                {
                    DBSQLExecutor.executeUpdate(accessor, whereBO, this);
                }
                
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.update has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
      }


      /**
       * Delete a row or rows of a table by using the accessor.
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */
      
      protected void delete(int accessorKey, Object whereBO) throws BusinessException
      {
                DBSQLAccessor accessor = getAccessor(accessorKey);
                delete(accessor, whereBO);
      }
      

      
      /**
       * Delete a row or rows of a table by using the accessor.
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */
      
      protected void delete(String accessorName, Object whereBO) throws BusinessException
      {
                DBSQLAccessor accessor = getAccessor(accessorName);
                delete(accessor, whereBO);
      }
      
      
        
        
      /**
       * Delete a row or rows of a table by using the accessor.
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */
      
      protected void delete(DBSQLAccessor accessor, Object whereBO) throws BusinessException
      {
            try
            {
         
                accessor.setConnection(getConnection());
                if (accessor.isStoredProcAccessor())
                {
                    runStoreProcedure((DBStoredProcAccessor)accessor, whereBO, this);
                }
                else
                {
                    DBSQLExecutor.executeDelete(accessor, whereBO);
                }
                
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.delete has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
      }


      /**
       *    Single Insert a row to a table by using the accessor.
       *    The Business Object should be inflated before using 
       *    this operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param businessObject  The Object contains the values to be inserted
       * @exception BusinessException
       */
      
      protected void save(int accessorKey, Object whereBO,  Object businessObject) throws BusinessException
      {
                DBSQLAccessor accessor = getAccessor(accessorKey);
                save(accessor, whereBO, businessObject);
      }


      /**
       *    Single Insert a row to a table by using the accessor.
       *    The Business Object should be inflated before using 
       *    this operation.
       * @param String accessorName The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param businessObject  The Object contains the values to be inserted
       * @exception BusinessException
       */
      
      protected void save(String accessorName, Object whereBO,  Object businessObject) throws BusinessException
      {
                DBSQLAccessor accessor = getAccessor(accessorName);
                save(accessor, whereBO, businessObject);
      }

      
      /**
       *    Single Insert a row to a table by using the accessor.
       *    The Business Object should be inflated before using 
       *    this operation.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param businessObject  The Object contains the values to be inserted
       * @exception BusinessException
       */
      
      protected void save(DBSQLAccessor accessor, Object whereBO,  Object businessObject) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                
                if (accessor.isStoredProcAccessor())
                {
                    runStoreProcedure((DBStoredProcAccessor)accessor, whereBO, businessObject);
                }
                else
                {
                    DBSQLExecutor.executeInsert(accessor, whereBO, businessObject);
                }
                
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.save has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
            
      }
  
      
      /**
       *   Multiple Insert rows to a table by using the accessor.  The Business
       *   Objects are put into the Vector before calling this operation.
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object businessObject The business object
       * @param Object resultVector The Vector contains the business Objects that to be inserted
       * @exception BusinessException
       */
      
      protected void save(int accessorKey, Object whereBO,  Object businessObject, Object resultVector) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorKey);
            save(accessor, whereBO, businessObject, resultVector);
      }
                
      
      /**
       *   Multiple Insert rows to a table by using the accessor.  The Business
       *   Objects are put into the Vector before calling this operation.
       *
       * @param String accessorName The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object businessObject The business object
       * @param Object resultVector The Vector contains the business Objects that to be inserted
       * @exception BusinessException
       */
      
      protected void save(String accessorName, Object whereBO,  Object businessObject, Object resultVector) throws BusinessException
      {
            DBSQLAccessor accessor = getAccessor(accessorName);
            save(accessor, whereBO, businessObject, resultVector);
      }
                

      
      /**
       *   Multiple Insert rows to a table by using the accessor.  The Business
       *   Objects are put into the Vector before calling this operation.
       *
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @param Object businessObject The business object
       * @param Object resultVector The Vector contains the business Objects that to be inserted
       * @exception BusinessException
       */
      
      protected void save(DBSQLAccessor accessor, Object whereBO,  Object businessObject, Object resultVector) throws BusinessException
      {
            try
            {
                accessor.setConnection(getConnection());
                
                if (accessor.isStoredProcAccessor())
                {
                    DBSQLExecutor.executeStoreProcedure((DBStoredProcAccessor)accessor, whereBO, businessObject, resultVector);                }
                else
                {
                    DBSQLExecutor.executeInsert(accessor, whereBO, businessObject, resultVector);
                }
                
            } catch (Exception e)
            {
                Logger.log("DefaultBusinessObject.save has error: " + e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
            
      }

  
      /**
       *
       * Runs a store procedure for single select.
       * @param int accessorKey The key to retrieve an Accessor
       * @param Object whereBO The object to populate conditions
       * @exception BusinessException
       */
  
      private void runStoreProcedure(DBStoredProcAccessor accessor, Object whereBO, Object businessObject) throws BusinessException
      {
            DBSQLExecutor.executeStoreProcedure(accessor, whereBO, businessObject);
      }
  
  
      
      /**
       *
       *     Get an accessor from accessor Factory by calling AccessorBroker.
       * @param int inKey The key to retrieve an Accessor
       * @return DBSQLAccessor The Accessor object
       * @exception BusinessException
       */
      
      private DBSQLAccessor getAccessor(int inKey) throws BusinessException
      {
            AccessorFactory af = AccessorBroker.getAccessorFactory(this.getClass());
            if (af != null)
            {
                DBSQLAccessor accessor = af.getAccessor(inKey);
                return accessor;
            }
            else
            {
                throw new BusinessException("Accessor is not found for the key: " + inKey);
            }
      }


      /**
       *
       *     Get an accessor from accessor Factory by calling AccessorBroker.
       * @param String inName The key to retrieve an Accessor
       * @return DBSQLAccessor The Accessor object
       * @exception BusinessException
       */
      
      private DBSQLAccessor getAccessor(String inName) throws BusinessException
      {
            //String accessorPackageName = Environment.get("ACCESSOR_PACKAGE_NAME");
            
            //if (accessorPackageName != null)
            //{
                try
                {
                    Class accessorClass = Class.forName(inName);
                
                    DBSQLAccessor accessorInstance = (DBSQLAccessor)accessorClass.newInstance();
                
                    return accessorInstance;
                    
                } catch (Exception e) 
                {
                    Logger.log("DefaultBusinessObject.getAccessor has error: " + e.getMessage());
                    throw new BusinessException("Unable to instantiate the Accessor: " + inName + "\n" + e.getMessage());
                }
                    
            //}
            //else
            //{
             //   throw new BusinessException("Accessor Package is not found for the Name: " + inName);
            //}
      }      

      
      /** 
       *
       *   Inflated test vector used to test BusinessObject working with JSP or servlet.
       * @param Object obj  the object that is to be put into vector
       * @return Vector testVector the Vector contains empty objects
       * @exception BusinessException
       */
       protected Vector inflateTestVector(Object obj) throws BusinessException
       {
            try
            {
                Class boClass = Class.forName(obj.getClass().getName());
                Vector testVector = new Vector();
                testVector.addElement(boClass.newInstance());
                testVector.addElement(boClass.newInstance());
                
                return testVector;
                
            } catch (Exception e) 
            {
                throw new BusinessException("Object: " + 
                    obj.getClass().getName() + " inflateTestVector encouters Exception.", e);
            }
        
       }

       
      /** 
       *
       *   doesTestJSP is to check if the system is running for testing.
       * @return boolean doesTestJSP true if the test is undergoing.
       */
       
       protected boolean doesTestJSP() 
       {
            try
            {
               boolean doesTestJSP =
                    Boolean.valueOf(Environment.get(Constants.TEST_JSP)).booleanValue();
               return doesTestJSP;
            } catch (Exception e)
            { 
                return false;
            }
       }
                       

      
  }
  
