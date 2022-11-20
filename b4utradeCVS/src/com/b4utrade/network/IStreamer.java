package com.b4utrade.network;

import java.io.InputStream;

import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;

public interface IStreamer {

   public void init(MessageQueue quoteQueue, MessageQueue newsQueue, InputStream inputStream,
               NetworkConfiguration networkConfiguration, NetworkStreamer networkStreamer);

   public void setDoRun(boolean doRun);

   public void setReadAttemptsBeforeTerminating(int readAttemptsBeforeTerminating);

   public boolean getIsBlocking();
   public void setIsBlocking(boolean isBlocking);
   public void setUserId(String userId);
   public void shutDownIO();
   public void setUdpBindAddress(String bindingAddress);
   public void setUdpPort(int port);
   public void setStreamerId(String streamerId);


}




