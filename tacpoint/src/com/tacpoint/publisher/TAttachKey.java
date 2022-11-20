/**
 * TAttachKey.java
 *
 * @author Copyright (c) 2006 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.util.HashMap;
import java.util.ArrayList;

public class TAttachKey {

   public String userId;
   public long lastWriteTime = System.currentTimeMillis();
   public HashMap topics = new HashMap();
   public boolean cancelProcessed = false;
   public ArrayList initializationMessages = null;

   // the following two attrs are used by the *compressed* selectors
   public java.io.OutputStream os = null;
   public java.io.ByteArrayOutputStream baos = null;

   public TAttachKey() {}

}

