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
 * The CommaSeperatedFile writes comma seperated values to disk 
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
public final class CommaSeperatedFile 
{
   /**
    */
   private static PrintWriter mPrintWriter = null;

   /**
    */
   private static CommaSeperatedFile instance = null;

   /**
    * A private constructor since this is a Singleton
    */
   private CommaSeperatedFile() throws Exception {
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
         instance = new CommaSeperatedFile();
   }

   static synchronized public void write(String msg)
   {
      if (instance.mPrintWriter != null)  {
         synchronized (instance.mPrintWriter) {
            instance.mPrintWriter.println(msg);
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
      InputStream is = getClass().getResourceAsStream("/commasep.properties");

      try {
          vCSProps.load(is);
      }
      catch (Exception e) {
          System.err.println("Can't read the commasep.properties file. " +
              "Make sure commasep.properties is in the CLASSPATH");
          throw e;
      }

      String vCSFile = vCSProps.getProperty("file", "stock.quote");
      boolean append = false;
      boolean flush = true;
      String s = setName(vCSFile);
      
      try {
         instance.mPrintWriter = new PrintWriter(new FileWriter(s, append), flush);
      }
      catch (Exception e) {
          System.err.println("Can't create the comma sep file."); 
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
