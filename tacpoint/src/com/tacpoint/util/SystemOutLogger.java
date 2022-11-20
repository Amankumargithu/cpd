/**
 * SystemOutLogger.java
 *
 * @author Copyright (c) 2006 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */
package com.tacpoint.util;

import java.io.File;
import java.io.FileWriter;

public final class SystemOutLogger implements java.io.Serializable
{
   public static final int LOG_LEVEL_DEBUG = 0;
   public static final int LOG_LEVEL_INFO = 1;
   public static final int LOG_LEVEL_WARNING = 2;
   public static final int LOG_LEVEL_ERROR = 3;
   
   private static int level = LOG_LEVEL_INFO;

   private static boolean logToFile = false;
   private static FileWriter logFile = null;

   public SystemOutLogger()
   {
   }

   public static void setLogToFile(boolean b)   { logToFile = b; }
   public static void setLogFile(FileWriter f)  { logFile = f; }

   public static int getLevel()        { return(level); }
   public static void setLevel(int l)  { level = l; }

   public static boolean isLevelDebug()   { return(level == LOG_LEVEL_DEBUG); }
   public static boolean isLevelInfo()    { return(level == LOG_LEVEL_INFO); }
   public static boolean isLevelWarning() { return(level == LOG_LEVEL_WARNING); }
   public static boolean isLevelError()   { return(level == LOG_LEVEL_ERROR); }

   public static boolean isLevelDebug(int i)   { return(i == LOG_LEVEL_DEBUG); }
   public static boolean isLevelInfo(int i)    { return(i == LOG_LEVEL_INFO); }
   public static boolean isLevelWarning(int i) { return(i == LOG_LEVEL_WARNING); }
   public static boolean isLevelError(int i)   { return(i == LOG_LEVEL_ERROR); }
   
   public static void debug(String text)
   {
      if (level == LOG_LEVEL_DEBUG)
      {
         System.out.println(text);
         if ( (logToFile) && (logFile != null) )
         {
            try
            {
               logFile.write(text);
               logFile.write("\r\n");
               logFile.flush();
            }
            catch(Exception ex)
            {}
         }
      }
   }
   public static void debugEntry(String text)
   {
      if (level == LOG_LEVEL_DEBUG)
      {
         System.out.println("ENTER - " + text);
         if ( (logToFile) && (logFile != null) )
         {
            try
            {
               logFile.write(text);
               logFile.write("\r\n");
               logFile.flush();
            }
            catch(Exception ex)
            {}
         }
      }
   }
   public static void debugExit(String text)
   {
      if (level == LOG_LEVEL_DEBUG)
      {
         System.out.println("EXIT - " + text);
         if ( (logToFile) && (logFile != null) )
         {
            try
            {
               logFile.write(text);
               logFile.write("\r\n");
               logFile.flush();
            }
            catch(Exception ex)
            {}
         }
      }
   }
   public static void info(String text)
   {
      if (level <= LOG_LEVEL_INFO)
         System.out.println(text);
   }
   public static void warning(String text)
   {
      if (level <= LOG_LEVEL_WARNING)
         System.out.println(text);
   }
   public static void error(String text)
   {
      if (level <= LOG_LEVEL_ERROR)
         System.out.println(text);
   }
}
