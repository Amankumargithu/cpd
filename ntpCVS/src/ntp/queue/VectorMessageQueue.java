package ntp.queue;

import java.util.Vector;

public class VectorMessageQueue
{
	protected Vector<Object> mQueue;

	public VectorMessageQueue()
	{
		try
		{
			mQueue = new Vector<Object>();
		}
		catch(Exception e)
		{
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
			Object[] items = mQueue.toArray();
			mQueue.clear();
			return items;
		}
	}

	public void add(Object aItem)
	{
		synchronized (mQueue)
		{
			mQueue.add(aItem);
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

}
