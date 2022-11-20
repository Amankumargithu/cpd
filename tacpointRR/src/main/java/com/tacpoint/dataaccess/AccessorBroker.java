package com.tacpoint.dataaccess;


/** 
 * AccessorBroker.java
 * <P>
 *    The AccessorBroker maintains a AccessorFactory.  The Factory will
 *    return a DBSQLAccessor based on a key.
 * <P>
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Ming Lau
 * @author mlau@tacpoint.com
 * @version 1.0
 * Date created: 12/27/1999
 * Date modified:
 * - 12/27/1999 Initial version
 */

import java.util.Properties;

import com.tacpoint.util.Utility;
import com.tacpoint.common.DefaultObject;
import com.tacpoint.exception.BusinessException;

public class AccessorBroker extends DefaultObject 
{
    private static AccessorFactory mFactory=null;
    
    /**
     *
     *   Get a Accessor Factory.  If the factory is not loaded, look for
     *   the config.properties file based on the Class Path.  Load the 
     *   Factory class.
     *
     *   @param Class sourceClass the Class of the source 
     *   @return AccessorFactory the Accessor Factory which maintains a list of Accessors.
     *
     */
    public static AccessorFactory getAccessorFactory(Class sourceClass) throws BusinessException
    {
        if (mFactory != null)
        {
            return mFactory;
        }
        
            String fileName = "/config.properties";
        
            Properties aProp = Utility.getProperties(sourceClass, fileName);
            
            synchronized(aProp) { // synch to create the mFactory
        
            if (mFactory == null)
            {
       
        
                String factoryName = aProp.getProperty("AccessorFactory");
        
                AccessorFactory af = null;
                try
                {
            
                    Class afClass = Class.forName(factoryName);
        
                    af = (AccessorFactory)afClass.newInstance();
            
                    mFactory = af;
            
                } catch (Exception e)
                {
                    //log to logManager for the error message
                    throw new BusinessException("AccessorFactory is not found.");
                }
            }
        
            return mFactory;
        }
        
    }
}


