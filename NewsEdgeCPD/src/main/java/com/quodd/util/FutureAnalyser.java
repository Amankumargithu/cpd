package com.quodd.util;

import static com.quodd.controller.NewsEdgeDataController.logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class FutureAnalyser extends Thread {

	private ConcurrentLinkedQueue<Future<QueryInfoBean>> queue = null;
	private boolean doRun = true;

	public FutureAnalyser(ConcurrentLinkedQueue<Future<QueryInfoBean>> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while (doRun) {
			Future<QueryInfoBean> future = queue.poll();
			if (future != null) {
				try {
					QueryInfoBean bean = future.get(10, TimeUnit.SECONDS);
					int result = bean.getResult();
					if (!(result > 0)) {
						logger.warning("Query Failed : " + bean.getQuery());
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		while (!queue.isEmpty()) {
			Future<QueryInfoBean> future = queue.poll();
			try {
				QueryInfoBean bean = future.get(10, TimeUnit.SECONDS);
				int result = bean.getResult();
				if (!(result > 0)) {
					logger.warning("Query Failed : " + bean.getQuery());
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	public void finish() {
		doRun = false;
	}
}