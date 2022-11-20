/** MessageQueue.java
* Copyright: Tacpoint Technologies, Inc. (c) 1999, 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 12.23.1999
* Date modified:
*/

package com.tacpoint.messagequeue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.tacpoint.util.Logger;

/** MessageQueue class
 */

/* TODO:
 * 1. serialize the queue entries to file upon thread stop request.
 */

public class MessageQueue

{


	//private ArrayBlockingQueue mQueue;
	private LinkedBlockingQueue mQueue;
	
	public MessageQueue() {
	      try
	      {
	       //  Logger.init();

	         // Delete when testing is done!
	         //Logger.log("MessageQueue constructor");

	         //mQueue = new ArrayBlockingQueue(1000000);
	         mQueue = new LinkedBlockingQueue();
	      }
	      catch(Exception e)
	      {
	         System.err.println("BlockingMessageQueue exception: " +  e.getMessage());
	         e.printStackTrace();
	      }
	}
	

	public boolean isEmpty() {
	   return mQueue.isEmpty();
	}
	
	public Object remove() throws InterruptedException {
		return mQueue.take();
	}
	
	public Object removeWithTimeout(long waitTime) throws InterruptedException {
		return mQueue.poll(waitTime, TimeUnit.MILLISECONDS);
	}
	
	public Object[] remove(int maxObjects) throws InterruptedException {

		Object o = mQueue.take();
		
		ArrayList list = new ArrayList(maxObjects);
		list.add(o);
		
		mQueue.drainTo(list, maxObjects - 1);
		
		return list.toArray();			
	}
	
	public Object[] removeAll() throws InterruptedException {
		Object o = mQueue.take();
		
		ArrayList list = new ArrayList(mQueue.size() + 1);
		list.add(o);
		
		mQueue.drainTo(list);
		
		return list.toArray();	
	}
	
	public Object[] removeAllWithTimeout(long waitTime) throws InterruptedException {
		Object o = mQueue.poll(waitTime, TimeUnit.MILLISECONDS);
		
		ArrayList list = new ArrayList(mQueue.size() + 1);
		list.add(o);
		
		mQueue.drainTo(list);
		
		return list.toArray();			
	}
	
	public void add(Object aItem) {
		mQueue.add(aItem);
	}
	
	public boolean addAll(Collection items) {
		return mQueue.addAll(items);
	}
	
	public int getSize() {
		return mQueue.size();
	}
	
	
	


	
   /*public MessageQueue()
   {
      try
      {
         Logger.init();

         // Delete when testing is done!
         Logger.log("MessageQueue constructor");

         mQueue = new LinkedList();
      }
      catch(Exception e)
      {
         System.err.println("Server exception: " +  e.getMessage());
         e.printStackTrace();
      }
   }

   public Object removeWithTimeout(long waitTime) throws InterruptedException
   {
      synchronized (mQueue)
      {
         while (mQueue.isEmpty())
         {
            mQueue.wait(waitTime);
         }

         Object vItem = mQueue.removeFirst();

         return vItem;
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

         Object vItem = mQueue.removeFirst();

         return vItem;
      }
   }

   public Object[] removeAllWithTimeout(long waitTime) throws InterruptedException
   {
      synchronized (mQueue)
      {
    	  
    	 
    	 boolean needsWaiting = true;
         while (mQueue.isEmpty() && needsWaiting)
         {
            mQueue.wait(waitTime);
            needsWaiting = false;
         }
         
         if (mQueue.isEmpty()) return null;

         Object[] items = mQueue.toArray();
         mQueue.clear();
         return items;
      }
   }
   
   public Object[] remove(int maxObjects) throws InterruptedException {

	      synchronized (mQueue)
	      {
	    	  
		      while (mQueue.isEmpty())
		      {
		         mQueue.wait();
		      }
		         
	    	  if (mQueue.size() <= maxObjects) {
	 	         Object[] items = mQueue.toArray();
		         mQueue.clear();
		         return items;
	    	  }
	    	  else {
	    		  Object[] items = new Object[maxObjects];
	    		  for (int i=0; i<maxObjects; i++)
	    			  items[i] = mQueue.removeFirst();
	    		  return items;
	    	  }

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
      while (mQueue.isEmpty())
      {
         mQueue.wait();
      }

      Object vItem = mQueue.removeFirst();

      return vItem;
   }

   public void add(Object aItem)
   {
      synchronized (mQueue)
      {
         mQueue.addLast(aItem);
         mQueue.notify();
      }
   }

   public void addUnSync(Object aItem)
   {
      mQueue.addLast(aItem);
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
   
	public boolean addAll(Collection items) {
		synchronized (mQueue) 
		{
			return mQueue.addAll(items);
		}		
	}

   static String CLASS_NAME = "MessageQueue";

   protected LinkedList mQueue;*/

   
   
	
}
