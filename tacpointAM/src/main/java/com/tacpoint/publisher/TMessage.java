/**
 * TMessage.java
 *
 * @author Copyright (c) 2004 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TMessage 
{
   private static Log log = LogFactory.getLog(TMessage.class);
   
   private Object key;
   private ByteArrayOutputStream baos;
   private Map map;


   public Object getKey() {
      return key;
   }

   public void setKey(Object key) {
      this.key = key;
   }

   public void setBaos(ByteArrayOutputStream baos) {
      this.baos = baos;
   }

   public ByteArrayOutputStream getBaos() {
      return baos;
   }

   public Map getMap() {
      return map;
   }

   public void setMap(Map map) {
      this.map = map;
   }


}
