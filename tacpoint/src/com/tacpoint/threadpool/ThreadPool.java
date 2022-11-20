package com.tacpoint.threadpool;

public interface ThreadPool {
	public void addJob(java.lang.Runnable job);
	public Stats getStats();
}
