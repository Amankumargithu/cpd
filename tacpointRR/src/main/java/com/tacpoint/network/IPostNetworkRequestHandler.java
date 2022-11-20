package com.tacpoint.network;

import java.util.*;


 /** 
  * IPostNetworkRequestHandler - provides a callback method for the NetworkRequestExecutor
  * to callback on when the client needs a chance to alter the contents of the payload.
  *
  * Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
  * @author Tacpoint Technologies, Inc.
  */
public interface IPostNetworkRequestHandler
{  

    public void execute(Hashtable hashtable) throws Exception;
        
}
