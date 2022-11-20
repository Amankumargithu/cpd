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
 * The Interface class that will be implemented by Cache Controller.
 *
 */

package com.tacpoint.cache;

import java.util.*;
import java.io.*;
import java.lang.*;

/**
 * This Interface Class provides the methods for concrete implemenation
 */
public interface ICacheController
{

   /**
    * Get the cache object from the controller.  The result will return
    * to the CacheManager and return back to the requested client.
    *
    * @return Object  the cache object, could be any kind of Container object,
    *                 and the requested class needs to cast to the right type.
    */
   public Object get();

   /**
    * Sets the delay time (in milliseconds)
    *
    * @param delay long
    */
   public void setDelay(long delay);

   /**
    * Gets the delay time (in milliseconds)
    *
    * @return long delay
    */
   public long getDelay();

   /**
    * Get the cache object from the controller based on the key.  The result will return
    * to the CacheManager and return back to the requested client.
    *
    * @param  String  the key value to retrieve the cache.
    * @return Object  the cache object, could be any kind of Container object,
    *                 and the requested class needs to cast to the right type.
    */
   public Object get(Object key);

   /**
    * Remove the cache in the controller.
    *
    * @return void
    */
   public void remove();

   /**
    * To check if the time is up for the controller to refresh its cache.
    *
    * @return boolean true = it is time to run, false = it is not the time.
    */
   public boolean isTimeToRun();


   /**
    * To run and update the cache pool in the cache controller.
    *
    * @return void
    */
   public void run();


}
