
package com.tacpoint.task;

import com.tacpoint.exception.*;

/**
 * An Interface to be used in any Task implementation.  The client 
 * must implement the following methods.
 *
 * Each implementation of this interface MUST provide a default no
 * argument constructor.
 */
public interface Task {

    /**
     * This method should perform all necessary work by the Task.
     */
    public void performWork(Object o) throws BusinessException; 
        
}

