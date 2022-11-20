/**
  * TProtocolHandler.java
  *
  * @author Copyright (c) 2005 by Tacpoint Technologie, Inc.
  *    All rights reserved.
  * @version 1.0
  */

package com.tacpoint.publisher;

public interface TProtocolHandler
{
   public void addMessage(TMessage message);
   public void setConfigBean(TPublisherConfigBean configBean);
   
}
