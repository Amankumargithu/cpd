package com.tacpoint.jms;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

public interface ReducedMessageInflator {

   public Hashtable parseKeyValues(ByteArrayOutputStream baos);

   public Object getTopic(ByteArrayOutputStream baos);

}

