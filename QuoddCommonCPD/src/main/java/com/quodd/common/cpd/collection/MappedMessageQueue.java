package com.quodd.common.cpd.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MappedMessageQueue {
	private ConcurrentMap<String, Map<String, Object>> mQueue;

	public MappedMessageQueue() {
		this.mQueue = new ConcurrentHashMap<>();
	}

	public Map<String, Object> removeAsMap() throws InterruptedException {
		synchronized (this.mQueue) {
			while (this.mQueue.isEmpty()) {
				this.mQueue.wait();
			}
			Map<String, Object> map = new HashMap<>();
			map.putAll(this.mQueue);
			this.mQueue.clear();
			return map;
		}
	}

	public boolean isEmpty() {
		return this.mQueue.isEmpty();
	}

	public int getSize() {
		return (this.mQueue.size());
	}

	public void putAll(String key, Map<String, Object> map) {
		synchronized (this.mQueue) {
			if (this.mQueue.containsKey(key))
				this.mQueue.get(key).putAll(map);
			else {
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.putAll(map);
				this.mQueue.put(key, tempMap);
			}
			this.mQueue.notify();
		}
	}
}