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
 * - 4/3/2001 add trace variables (Critical, info, debug, etc.)
 * - 4/20/2001 removed the TRACE_* constants in favor of the
 *             aforementioned trace variables.
 *
 * The Logger class provides static logging capability.
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
 * method prior to any <code>log</code> and/or <code>trace</code>
 * method invokations.
 */
public class Logger
{
   /**
    * Trace Level 1 - critical
    */
   public static final int CRITICAL = 1;

   /**
    * Trace Level 2 - Severe
    */
   public static final int SEVERE = 2;

   /**
    * Trace Level 3 - Warning
    */
   public static final int WARNING = 3;

   /**
    * Trace Level 4 - Info
    */
   public static final int INFO = 4;

   /**
    * Trace Level 5 - debug
    */
   public static final int DEBUG = 5;

   /**
    * This value is used to designate a particular logging level.
    * Any trace statements &lt; or equal to this value will be output.
    */
   private static int traceLevel = 5;

   /**
    * A private <code>PrintWriter</code> instance used for maintaining
    * a handle to the output file.
    */
   private static PrintWriter log = null;

   /**
    * A private <code>Logger</code> instance used for maintaining
    * a handle to the Logger object.
    */
   private static Logger instance = null;

   /**
    * A private constructor since this is a Singleton
    */
   private Logger() throws Exception {
      initialize();
   }

   /**
    * Initializes the internal PrintWriter reference with the properties
    * supplied filename.&nbsp;This method must be invoked prior to any calls
    * to either <code>log</code> or <code>trace</code>.
    *
    * @exception IOException  Throws IOException when application cannot open
    *                         the file.
    */
   static synchronized public void init() throws Exception
   {
      if (instance == null)
         instance = new Logger();
   }

   /**
    * This method will log the contents of <code>msg</code> to the active file
    * stream if the variable <code>traceLevel</code> is &lt; or equal to the
    * internal <code>traceLevel</code>
    *
    * @param traceLevel  Integer variable used for determining whether or not
    *                    the <code>msg</code> should be logged.
    * @param msg         String containing text to be logged.
    */
   static synchronized public void trace(int traceLevel, String msg)
   {
      if (traceLevel <= Logger.traceLevel)
      {
         if (instance.log != null)  {
            synchronized (instance.log) {
               instance.log.println(new Date() + ": " + msg);
            }
         }
      }
   }

   /**
    * This method will log the contents of <code>msg</code> to the active file
    * stream if the variable <code>traceLevel</code> is &lt; or equal to the
    * internal <code>traceLevel</code>
    *
    * @param traceLevel  Integer variable used for determining whether or not
    *                    the <code>msg</code> should be logged.
    * @param msg         String containing text to be logged.
    * @param ex          Throwable.
    */
   static synchronized public void trace(int traceLevel, String msg, Throwable ex)
   {
      if (traceLevel <= Logger.traceLevel)
      {
         if (instance.log != null)  {
            synchronized (instance.log) {
               instance.log.println(new Date() + ": " + msg);
               if (ex!=null)
                  ex.printStackTrace(instance.log);
            }
         }
      }
   }

   /**
    * This method will log the contents of msg to the active file stream.
    *
    * @param msg      String containing text to be logged.
    */
   static synchronized public void log(String msg)
   {
      if (instance.log != null)  {
         synchronized (instance.log) {
            instance.log.println(new Date() + ": " + msg);
         }
      }
   }

   /**
    * This method will log the contents of msg to the active file stream
    * and pass the local <code>PrintWriter</code> object to the Throwable
    * argument to print out the current stack trace.
    *
    * @param msg      String containing text to be logged.
    * @param ex       Throwable.
    */
   static synchronized public void log(String msg, Throwable ex)
   {
      if (instance.log != null)  {
         synchronized (instance.log) {
            instance.log.println(new Date() + ": " + msg);
            if (ex!=null)
               ex.printStackTrace(instance.log);
         }
      }
   }

   /**
    * This method will log the contents of msg to the active file stream if
    * the boolean variable doPrint is set to true.&nbsp;Else no action is taken.
    *
    * @param msg      String containing text to be logged.
    * @param doPrint  Boolean variable used for determining whether or not
    *                 the <code>msg</code> should be logged.
    * @deprecated
    */
   static synchronized public void debug(String msg, boolean doPrint)
   {
      if (!doPrint)
         return;
      if (instance.log != null)  {
         synchronized (instance.log) {
            instance.log.println(new Date() + ": " + msg);
         }
      }
   }

   /**
    * This method will log the contents of msg to the active file stream and
    * invoke the printStackTrace(PrintWriter) method on the Throwable argument
    * if the boolean variable doPrint is set to true.&nbsp;Else no action is
    * taken.
    *
    * @param msg      String containing text to be logged.
    * @param ex       Throwable.
    * @param doPrint  Boolean variable used for determining whether or not
    *                 the <code>msg</code> should be logged.
    * @deprecated
    */
   static synchronized public void debug(String msg, Throwable ex,
                                         boolean doPrint)
   {
      if (!doPrint)
         return;
      if (instance.log != null)  {
         synchronized (instance.log) {
            instance.log.println(new Date() + ": " + msg);
            if (ex!=null)
               ex.printStackTrace(instance.log);
         }
      }
   }


   /**
    * Read in the logger properties file.&nbsp;This file needs to exist
    * somewhere in the application's classpath.
    */
   private void initialize() throws Exception
   {

      Properties logProps = new Properties();
      InputStream is = getClass().getResourceAsStream("/logfile.properties");

      try {
          logProps.load(is);
      }
      catch (Exception e) {
          System.err.println("Can't read the logfile.properties file. " +
              "Make sure logfile.properties is in the CLASSPATH");
          throw e;
      }

      String logFile = logProps.getProperty("logfile", "app.log");

      try {
         traceLevel =
            Integer.parseInt(logProps.getProperty("traceLevel","5"));
      }
      catch (NumberFormatException nfe) {}
      
      String logDailyFile = logProps.getProperty("useShortFilename", "FALSE");
      
      String appendToFile = logProps.getProperty("appendToExistingFile", "FALSE");

      boolean append = false;
      boolean flush = true;
      String s = null;
            
      if (appendToFile.equalsIgnoreCase("TRUE"))
         append = true;
      
      if (logDailyFile.equalsIgnoreCase("TRUE"))
         s = setDaiylLogName(logFile);
      else 
         s = setLogName(logFile);
         
      try {
         instance.log = new PrintWriter(new FileWriter(s, append), flush);
      }
      catch (Exception e) {
          System.err.println("Can't create the log file.");
          throw e;
      }

      dumpSystemEnv();
   }

   /**
    * Sends all the <code>System</code> properties to the active file stream.
    */
   static synchronized private void dumpSystemEnv()
   {
      instance.log.println("System Properties:");
      Properties sys = System.getProperties();
      Enumeration e = sys.propertyNames();

      synchronized (instance.log) {
         while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
    	    instance.log.println(key + ": " + sys.getProperty(key));
         }
         instance.log.println("-----------------");
      }
   }

   /**
    * Creates a unique filename extension based on the current date.
    *
    * @param base  Absolute filename
    * @return Complete filename
    */
   static synchronized private String setLogName(String base)
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
    * Creates a unique filename extension based on the current date only.
    *
    * @param base  Absolute filename
    * @return Complete filename
    */
   static synchronized private String setDaiylLogName(String base)
   {
      Calendar c = Calendar.getInstance();
      int day = c.get(Calendar.DAY_OF_MONTH);
      int mon = c.get(Calendar.MONTH) + 1;

      NumberFormat nf = NumberFormat.getInstance();
      nf.setMinimumIntegerDigits(2);
      String stamp = nf.format(mon) + nf.format(day);
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
      if (instance.log!=null)
         instance.log.close();
   }
}
