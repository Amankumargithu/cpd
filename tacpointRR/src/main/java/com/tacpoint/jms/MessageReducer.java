package com.tacpoint.jms;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;

public interface MessageReducer {

   public HashMap parseKeyValues(ByteArrayOutputStream baos);

   public void conflateReducedMessages(ByteArrayOutputStream baos, Map existing, Map additions);

   public ByteArrayOutputStream reduceMessage(String ticker, byte[] message);

   public void resetAttribute(String attribute, String value);
   
   public MessageReducer cloneMe();

}
