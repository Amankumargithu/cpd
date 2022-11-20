package com.tacpoint.cache;

import java.util.*;
import java.io.*;
import java.lang.*;
import com.tacpoint.util.Logger;

public class TimerCacheObject
{
	private Object cacheItem;

	private long expireTime;

	private long lastAccessTime;

	private String key;


    //setters

    public void setCacheItem(Object inItem)
    {
		cacheItem = inItem;
	}

	public void setExpireTime(long inTime)
	{
		expireTime = inTime;
    }
    public void setLastAccessTime(long inTime)
    {
		lastAccessTime = inTime;
    }
    public void setKey(String inKey)
    {
		key = inKey;
    }

    //getters

    public Object getCacheItem()
    {
		return cacheItem;
    }

	public long getExpireTime()
	{
		return expireTime;
    }
    public long getLastAccessTime()
    {
		return lastAccessTime;
    }
    public String getKey()
    {
		return key;
    }
}