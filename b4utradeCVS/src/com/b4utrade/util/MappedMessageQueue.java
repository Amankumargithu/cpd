package com.b4utrade.util;

import java.util.LinkedHashMap;

import com.tacpoint.util.Logger;

public class MappedMessageQueue
{
   /** default constructor
   */
//	private int messagesProcessed = 0;
//	private long eachruntime = System.currentTimeMillis();
	
   public MappedMessageQueue()
   {
      try
      {
         Logger.init();

         // Delete when testing is done!
         Logger.log("MessageQueue constructor");

         mQueue = new LinkedHashMap();
      }
      catch(Exception e)
      {
         System.err.println("Server exception: " +  e.getMessage());
         e.printStackTrace();
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
         
        /* turned off as it was causing  a lot of logs
         * long timeDiff = System.currentTimeMillis() - eachruntime;
         Logger.log("QUEUE Processed Messages " + messagesProcessed + " for Symbols " + mQueue.size() + " in time " + timeDiff);
         messagesProcessed = 0;
         eachruntime = System.currentTimeMillis();*/
         
         Object[] items = mQueue.values().toArray();
         mQueue.clear();
         return items;
      }
   }

   public void add(String key,Object aItem)
   {
      synchronized (mQueue)
      {
//    	  messagesProcessed++;
         mQueue.put(key,aItem);
         mQueue.notify();
      }
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

   protected LinkedHashMap mQueue;
}
