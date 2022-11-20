/** MessageQueue.java
* Copyright: Tacpoint Technologies, Inc. (c) 1999, 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 12.23.1999
* Date modified:
*/

package com.b4utrade.util;

import java.util.Vector;
//import com.tacpoint.util.*;

/** MessageQueue class
 */

/* TODO:
 * 1. serialize the queue entries to file upon thread stop request.
 */

public class MessageQueue
{
   /** default constructor
   */
   public MessageQueue()
   {
      try
      {
         //Logger.init();

         // Delete when testing is done!
         //Logger.log("MessageQueue constructor");

         mQueue = new Vector();
      }
      catch(Exception e)
      {
         System.err.println("Server exception: " +  e.getMessage());
         e.printStackTrace();
      }
   }

   public Object remove() throws InterruptedException
   {
      synchronized (mQueue)
      {
         while (mQueue.isEmpty())
         {
            mQueue.wait();
         }

         Object vItem = mQueue.firstElement();
         mQueue.removeElementAt(0);

         return vItem;
      }
   }

   public Object[] removeAll() throws InterruptedException
   {
      synchronized (mQueue)
      {
         while (mQueue.isEmpty())
         {
            mQueue.wait();
         }

         Object[] items = mQueue.toArray();
         mQueue.clear();
         return items;
      }
   }

   public Object removeUnSync() throws InterruptedException
   {
      while (mQueue.isEmpty() )
      {
         mQueue.wait();
      }

      Object vItem = mQueue.remove(0);

      return vItem;
   }

   public void add(Object aItem)
   {
      synchronized (mQueue)
      {
         mQueue.addElement(aItem);
         mQueue.notify();
      }
   }

   public void addUnSync(Object aItem)
   {
      mQueue.addElement(aItem);
      mQueue.notify();
   }

   public boolean isEmpty()
   {
      return mQueue.isEmpty();
   }

   public int getSize()
   {
      return(mQueue.size());
   }

   static String CLASS_NAME = "MessageQueue";

   protected Vector mQueue;
}
