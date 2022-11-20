package com.tacpoint.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//import com.tacpoint.util.Logger;

public class TMessageQueue
{
	
	private LinkedBlockingQueue mQueue = new LinkedBlockingQueue(2000000);
	
	public TMessageQueue() {
	      try
	      {
	         //Logger.init();

	         // Delete when testing is done!
	         //Logger.log("MessageQueue constructor");

	         //mQueue = new ArrayBlockingQueue(1000000);
	         //mQueue = new LinkedBlockingQueue(2000000);
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

	

	
	/*
   LinkedList messageQueue = new LinkedList();
   public TMessageQueue()
   {
   }
   
   public void addAll(java.util.Collection additions)  {
	   
	      synchronized (messageQueue)
	      {
	         messageQueue.addAll(additions);
	         messageQueue.notify();
	      }
	   
   }

   public Object remove() throws InterruptedException
   {
      synchronized (messageQueue)
      {
         while (messageQueue.isEmpty())
         {
            messageQueue.wait();
         }

         Object vItem = messageQueue.removeFirst();

         return vItem;
      }
   }

   public Object[] removeAll() throws InterruptedException
   {
      synchronized (messageQueue)
      {
         while (messageQueue.isEmpty())
         {
            messageQueue.wait();
         }

         Object[] items = messageQueue.toArray();
         messageQueue.clear();
         return items;
      }
   }
   
   public Object[] remove(int maxElems) throws InterruptedException {
	  
	  if (maxElems <= 0) return null;
	  
	  synchronized (messageQueue)
	  {
	     while (messageQueue.isEmpty())
	     {           
	    	 messageQueue.wait();
	     }
	     
	     int size = messageQueue.size();
	     
	     if (size <= maxElems) {
		     Object[] items = messageQueue.toArray();
		     messageQueue.clear();
		     return items;
	     }
	     else {
	    	 List subList = messageQueue.subList(0,size-1);
		     Object[] items = subList.toArray();
		     subList.clear();
		     return items;
	     }
	  }	      
   }

   public void add(Object aItem)
   {
      synchronized (messageQueue)
      {
         messageQueue.addLast(aItem);
         messageQueue.notify();
      }
   }


   public boolean isEmpty()
   {
      return messageQueue.isEmpty();
   }

   public int getSize()
   {
      return(messageQueue.size());
   }
   */


}

