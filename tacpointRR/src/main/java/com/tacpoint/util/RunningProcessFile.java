/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author John Tong 
 * @author jtong@tacpoint.com
 * @version 1.0
 * Date created: 6.11.2000
 * Date modified:
 * - 6.11.2000 Initial version
 *
 * The RunningProcessFile writes comma seperated values to disk 
 *
 */

package com.tacpoint.util;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.*;

/**
 * This Singleton class allows users to print output to a designated file.&nbsp;
 * The client is responsible for calling the static <code>init</code>
 * method prior to any <code>log</code> and/or <code>debug</code> 
 * method invokations.
 */
public final class RunningProcessFile 
{
   /**
    */
   private static PrintWriter mPrintWriter = null;

   /**
    */
   private static RunningProcessFile instance = null;

   /**
    * A private constructor since this is a Singleton
    */
   private RunningProcessFile() throws Exception {
      initialize();
   }

   /**
    * Initializes the internal PrintWriter reference with the properties 
    * supplied filename.&nbsp;This method must be invoked prior to any calls 
    * to either <code>log</code> or <code>debug</code>.
    *
    * @exception IOException  Throws IOException when application cannot open
    *                         the file. 
    */
   static synchronized public void init() throws Exception
   {
      if (instance == null)
         instance = new RunningProcessFile();
   }

   static synchronized public void write(String msg)
   {
      if (instance.mPrintWriter != null)  {
         synchronized (instance.mPrintWriter) {
            instance.mPrintWriter.println(msg);
         }
      }
   }

   static synchronized public void write()
   {
      if (instance.mPrintWriter != null)  {
         synchronized (instance.mPrintWriter) {
            instance.mPrintWriter.println(new Date());
         }
      }
   }

   /**
    * Read in the logger properties file.&nbsp;This file needs to exist 
    * somewhere in the application's classpath.
    */
   private void initialize() throws Exception
   {

      Properties vCSProps = new Properties();
      InputStream is = getClass().getResourceAsStream("/runningproc.properties");

      try {
          vCSProps.load(is);
      }
      catch (Exception e) {
          System.err.println("Can't read the runningproc.properties file. " +
              "Make sure runngproc.properties is in the CLASSPATH");
          throw e;
      }

      String vCSFile = vCSProps.getProperty("file", "running.process");
      boolean append = false;
      boolean flush = true;
//      String s = setName(vCSFile);
      
      try {
         instance.mPrintWriter = new PrintWriter(new FileWriter(vCSFile, append), flush);
      }
      catch (Exception e) {
          System.err.println("Can't create the running process file."); 
          throw e;
      } 
   }

   /**
    * Creates a unique filename extension based on the current date.
    *
    * @param base  Absolute filename 
    * @return Complete filename
    */
   static synchronized private String setName(String base)
   {
      Calendar c = Calendar.getInstance();
      int day = c.get(Calendar.DAY_OF_MONTH);
      int mon = c.get(Calendar.MONTH) + 1;
      int hour = c.get(Calendar.HOUR_OF_DAY);
      int min = c.get(Calendar.MINUTE);

      NumberFormat nf = NumberFormat.getInstance();
      nf.setMinimumIntegerDigits(2);
      String stamp = nf.format(mon) + nf.format(day) + "." + 
                     nf.format(hour) + nf.format(min);
      return base + "." + stamp;
   }

   /**
    * Closes the file stream.
    *
    * @exception Throwable Passes any exception encountered up to the calling 
    *                      method.
    */
   protected void finalize() throws Throwable 
   {
      if (instance.mPrintWriter!=null)
         instance.mPrintWriter.close();
   }
}
