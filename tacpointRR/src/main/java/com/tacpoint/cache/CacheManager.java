/**
 * Copyright 2001 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  Tacpoint Technologies, Inc.
 * @version 1.0
 * Date created: 4/2/2001
 * Date modified:
 * - 4/2/2001 Initial version
 *
 * The CacheManager class is a singleton object and responsible for providing
 * the cache functionality to other application. 
 *
 */

package com.tacpoint.cache;

import java.util.*;
import java.io.*;
import java.lang.*;
import com.tacpoint.util.Logger;

public class CacheManager implements Runnable
{
   /**
    * A private <code>Thread</code> instance used for maintaining
    * a handle to the Cache Manager thread.
    */

  	private static Thread thread = null;

   /**
    * A boolean used for controlling the running of the CacheManager
    * thread.
    */

	private static boolean doRun = false;

   /**
    * A long used for delaying the running of the CacheManager
    * thread.
    */
	private static long delay = 2000L;

   /**
    * A private <code>Hashtable</code> instance used for maintaining
    * a handle to the ICacheController.
    */
   private static Hashtable cacheList = new Hashtable();

   /**
    * A private handle to an <code>CacheManager</code> instance.
    */
   private static CacheManager instance = null;

   /**
    * A private constructor since this is a Singleton.
    */
   private CacheManager() throws Exception {
      initialize();
   }

   /**
    * Initializes the internal CacheManager reference.
    *
    */
   static synchronized public void init()
   {
      if (instance == null)
      {
        try
        {
          instance = new CacheManager();
        } catch (Exception e)
        {
            Logger.log("Error encountered in init() method.");
        } 

      }

   }

   /**
    * Returns the cache value to which the specified key is mapped in the 
    * cache list.
    *
    * @param key      a key in the cache object. 
    * @return         the value to which the key is mapped in the Cache
    *                 object; null if the key is not mapped to any value. 
    */
   static public Object get(Object key)
   {
      if (instance != null)
      {
         ICacheController tempController = (ICacheController)cacheList.get(key);
         if (tempController != null)
         {
            return (tempController.get());
         }
      }
      
      return null;
   }


   /**
    * Returns the cache value to which the first key is mapped in the 
    * cache list, and the second key matched to the cache in the controller.
    *
    * @param first key   a key in the cache object. 
    * @param second key  a key in the cache controller object.
    * @return         the value to which the key is mapped in the Cache
    *                 object; null if the key is not mapped to any value. 
    */
   static public Object get(Object firstKey, Object secondKey)
   {
      if (instance != null)
      {
         ICacheController tempController = (ICacheController)cacheList.get(firstKey);
         if (tempController != null)
         {
            return (tempController.get(secondKey));
         }
      }
      
      return null;
   }


   /**
    * Force a manual refresh of a particular cache.
    *
    * @param firstKey String   name of cache
    */
   static public void refreshCache (String firstKey) throws Exception
   {
      if (firstKey == null || firstKey.length() == 0)
         throw new Exception("Blank cache name.");

      ICacheController controller = (ICacheController)cacheList.get(firstKey);
      if (controller != null)
         controller.run();
      else
         throw new Exception("Cache [" + firstKey + "] not found.");

   }

   /**
    * Instantiate the cache list.
    */
   private void initialize()
   {
         
	cacheList = new Hashtable();
	doRun = true;

        InputStream is = getClass().getResourceAsStream("/cache_manager.properties");
        Properties props = new Properties();
        try {
            props.load(is);
        }
        catch (Exception e) {
            Logger.log("CacheManager.init - Unable to read the properties file.  Make sure "+
                       "cache_manager.properties is in the CLASSPATH",e);
            return;
        }


	String name = "";

        Enumeration propNames = props.propertyNames();

        while (propNames.hasMoreElements()) {

           name = (String) propNames.nextElement();
           Logger.trace(Logger.DEBUG,"CacheManager.init - hasMoreElements = " + propNames.hasMoreElements());
           Logger.trace(Logger.DEBUG,"CacheManager.init - propNames entry = " + name);


           if (name.endsWith(".cacheClassName")) {
              String poolName = name.substring(0, name.lastIndexOf("."));
              String cacheClassName = props.getProperty(poolName + ".cacheClassName");
              String delayString = props.getProperty(poolName + ".delay", "1800000");
              int iDelay;

              try {
                  iDelay = Integer.valueOf(delayString).intValue();
              }
              catch (Exception e) {
                  Logger.log("CacheManager.init - Invalid delay value " + delayString + " for " + poolName);
                  iDelay = 1800000;
              }

              Logger.trace(Logger.DEBUG,"CacheManager.init - pool name = " + poolName);
              Logger.trace(Logger.DEBUG,"CacheManager.init - cache class name = " + cacheClassName);
              Logger.trace(Logger.DEBUG,"CacheManager.init - delay = " + delayString);

              try {
                 ICacheController icc = (ICacheController)Class.forName(cacheClassName).newInstance();
                 icc.setDelay(iDelay);
                 cacheList.put(poolName,icc);
              }
              catch (Exception e) {
                 Logger.log("CacheManager.init - Unable to instantiate ICacheController ["+cacheClassName+"]",e);
              }
           }

        }


        Logger.trace(Logger.DEBUG,"CacheManager.init - Finished while loop.");

	thread = new Thread(this);
	thread.start();
   }
   
   /**
    * add the ICacheController and the key to the cache list.
    *
    * @param key      a key in the cache object.
    * @param ICachable a cache object.
    * @return void
    */
   static synchronized public void put(String key, ICacheController controller)
   {
      if (instance == null)
      {
        init();
      }
      
      if (controller != null)
      {
         cacheList.put(key, controller);
      }
   }

   
   /**
    * remove the ICacheController and the key to the cache list.
    *
    * @param key      a key in the cache object.
    * @return Object  the object that is removed from the hashtable
    */
   static synchronized public Object remove(String key)
   {
      if (instance == null)
      {
        init();
      }

      ICacheController tempController = (ICacheController)cacheList.get(key);
      
      if (tempController != null)
      {
         tempController.remove();
      }

      return (cacheList.remove(key));
   }
   
   /**
    * Running the thread to update each controller's cache.
    *
    * @return void
    */
   
  public void run() {

     while (doRun) {
        //Logger.log("running the controller.");
        try {
                
          for (Enumeration keys = cacheList.keys(); keys.hasMoreElements() ;) 
          {
             ICacheController controller = (ICacheController)cacheList.get((String)keys.nextElement());
             if (controller.isTimeToRun())
             {
                controller.run();
             }
          }
       } catch (Exception e) {
          Logger.log("CacheManager.run() - Error encountered ",e);
       }
      
       try {
            Thread.sleep(delay);
       } catch (InterruptedException ie) {}
     
     } 

   }
   
   
   /**
    * Clean up the class before the process completed.
    *
    * @return void
    */
   public void finalize() {
	      doRun = false;
	      thread = null;
	      
	      try {
                
             for (Enumeration keys = cacheList.keys(); keys.hasMoreElements() ;) 
             {
                 ICacheController controller = (ICacheController)cacheList.get((String)keys.nextElement());
                 controller.remove();
             }
             
         } catch (Exception e) {
            Logger.log("CacheManager.finalize() - Error encountered ",e);
         }
 
   }
   
}
