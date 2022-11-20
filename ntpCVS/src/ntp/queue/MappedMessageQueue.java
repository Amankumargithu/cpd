package ntp.queue;

import java.util.LinkedHashMap;

public class MappedMessageQueue
{
	protected LinkedHashMap<String, Object> mQueue;

	public MappedMessageQueue()
	{
		try
		{
			mQueue = new LinkedHashMap<String, Object>();
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
			Object[] items = mQueue.values().toArray();
			mQueue.clear();
			return items;
		}
	}

	public void add(String key,Object aItem)
	{
		synchronized (mQueue)
		{
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

}
