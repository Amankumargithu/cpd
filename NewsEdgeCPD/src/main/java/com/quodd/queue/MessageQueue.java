package com.quodd.queue;

import java.util.LinkedList;

public class MessageQueue
{
	protected LinkedList<Object> mQueue;

	public MessageQueue()
	{
		mQueue = new LinkedList<Object>();
	}

	public Object[] removeAll() throws InterruptedException
	{
		synchronized (mQueue)
		{
			if(mQueue.isEmpty())
				mQueue.wait(1000);
			if(mQueue.isEmpty())
				return null;
			else
			{
				Object[] items = mQueue.toArray();
				mQueue.clear();
				return items;
			}
		}
	}

	public void add(Object aItem)
	{
		synchronized (mQueue)
		{
			mQueue.addLast(aItem);
			mQueue.notify();
		}
	}	
}