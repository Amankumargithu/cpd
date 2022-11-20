package com.b4utrade.network;

import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;


 /**
  * IPostNetworkRequestHandler - provides a callback method for the NetworkRequestExecutor
  * to callback on when the client needs a chance to alter the contents of the payload.
  *
  * Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
  * @author Tacpoint Technologies, Inc.
  */
public interface HttpPollingHandler
{
    public void execute(MessageQueue queue, MessageQueue newsQueue, NetworkConfiguration nc) throws Exception;
    public void stop() throws Exception;

}
