/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  andy katz
 * @author akatz@tacpoint.com
 * @version 1.0
 * Date created: 1/2/2000
 * Date modified:
 * - 1/2/2000 Initial version
 *
 * The Environment class stores environment and runtime information.  It
 * will inflate itself from the environment.properties file which must
 * be located somewhere in the classpath.
 *
 */

package com.tacpoint.util;

import java.util.*;
import java.io.*;
import java.lang.*;

/**
 * This Singleton class allows users to retrieve environment and runtime
 * information stored in an external environment.properties file.
 * The client is responsible for calling the static <code>init</code>
 * method prior to any <code>get</code> method invokations.
 */
public class Environment
{
   /**
    * A private <code>PrintWriter</code> instance used for maintaining
    * a handle to the output file.
    */
   private static Properties props = new Properties();

   /**
    * A private handle to an <code>Environment</code> instance.
    */
   private static Environment instance = null;

   /**
    * A private constructor since this is a Singleton.
    */
   private Environment() throws Exception {
      initialize();
   }

   /**
    * Initializes the internal Environment reference with the properties 
    * supplied filename.  This method must be invoked prior to any calls 
    * to <code>get</code>. 
    *
    * @exception IOException  Throws IOException when application cannot open
    *                         the file. 
    */
   static synchronized public void init() throws Exception
   {
      if (instance == null)
         instance = new Environment();
   }

   /**
    * Returns the value to which the specified key is mapped in the 
    * properties file.
    *
    * @param key      a key in the Properties object. 
    * @return         the value to which the key is mapped in the Properties 
    *                 object; null if the key is not mapped to any value. 
    */
   static public String get(String key)
   {
      return (instance != null) ? props.getProperty(key) : null;
   }

   /**
    * Read in the environment.properties file.  This file needs to exist 
    * somewhere in the application's classpath.
    */
   private void initialize() throws Exception
   {
      InputStream is=getClass().getResourceAsStream("/environment.properties");
      try {
          props.load(is);
      }
      catch (Exception e) {
          System.err.println("Can't read the environment.properties file. " +
              "Make sure environment.properties is in the CLASSPATH");
          throw e;
      }
   }
}
