 package com.tacpoint.dataaccess;

 /** 
  * DefaultAccessorFactory.java
  * <P>
  *     The default Accessor Factory for the AccessorFactory Interface.  Other Implementation
  *     has to be inherited from this class.
  * <P>
  *
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 1/27/2000
  * Date modified:
  * - 1/27/2000 Initial version
  */


import com.tacpoint.dataaccess.AccessorFactory;
import com.tacpoint.dataaccess.DBSQLAccessor;
import com.tacpoint.common.DefaultObject;
import com.tacpoint.exception.BusinessException;

import java.util.Hashtable;


public class DefaultAccessorFactory extends DefaultObject implements AccessorFactory 
{
    private Hashtable mFactory = null;
  
    /**
     *  Constructor
     */
    public DefaultAccessorFactory()
    {
        mFactory = new Hashtable();
        
    }
    
        
    /**
     *     Get a Accessor based on the key.  If the requested Accessor is found,
     *     a copy is cloned.
     *
     * @param int accessorKey the key to retrieve an accessor
     * @return DBSQLAccessor the DBSQLAccessor 
     *
     */
    public DBSQLAccessor getAccessor(int accessorKey) throws BusinessException
    {
        DBSQLAccessor dba = ((DBSQLAccessor)mFactory.get(new Integer(accessorKey)));
        if (dba == null)
        {
            throw new BusinessException("Accessor for the key: " + accessorKey + " not found.");
        }
        
        return((DBSQLAccessor)dba.clone());
        
    }
    
    /**
     *
     *   Add an accessor to the factory
     *
     * @param int accessorKey  The key to be used for retrieving the Accessor
     * @param DBSQLAccessor accessor The accessor to be used.
     *
     */
    protected void add(int accessorKey, DBSQLAccessor accessor)
    {
        mFactory.put(new Integer(accessorKey), accessor);
    }
}