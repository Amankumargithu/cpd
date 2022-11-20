package com.quodd.common.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
	private final LinkedBlockingQueue<Object> mQueue;

	public MessageQueue() {
		this.mQueue = new LinkedBlockingQueue<>(2_000_000);
	}

	public Object[] removeAllByWaiting() throws InterruptedException {
		Object obj = this.mQueue.take();
		ArrayList<Object> list = new ArrayList<>(this.mQueue.size() + 1);
		list.add(obj);
		this.mQueue.drainTo(list);
		return list.toArray();
	}

	public List<Object> removeAllWithoutWait() {
		if (this.mQueue.isEmpty()) {
			return null;
		}
		ArrayList<Object> list = new ArrayList<>(this.mQueue.size());
		this.mQueue.drainTo(list);
		return list;
	}

	public void add(final Object aItem) {
		this.mQueue.add(aItem);
	}

	public void clear() {
		this.mQueue.clear();
	}

	public void addAll(final List<?> tickers) {
		this.mQueue.addAll(tickers);
	}

	public boolean remove(final Object aItem) {
		return this.mQueue.remove(aItem);
	}

	public boolean isEmpty() {
		return this.mQueue.isEmpty();
	}
}