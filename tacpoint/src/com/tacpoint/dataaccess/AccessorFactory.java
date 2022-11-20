package com.tacpoint.dataaccess;

/** 
 * AccessorFactory.java
 * <P>
 *    The interface of Accessor Factory.
 * <P>
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Ming Lau
 * @author mlau@tacpoint.com
 * @version 1.0
 * Date created: 1/27/2000
 * Date modified:
 * - 1/27/2000 Initial version
 */

import com.tacpoint.exception.BusinessException;


public interface AccessorFactory 
{
    /**
     *    Get a DBSQLAccessor based on the key
     *
     * @param  int inKey  the key from AccessorConstants
     * @return DBSQLAccessor
     *
     */
    public DBSQLAccessor getAccessor(int inKey) throws BusinessException;
}