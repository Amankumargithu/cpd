package com.quodd.populator;

import static com.quodd.controller.DJController.logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import com.quodd.bean.QueryInfoBean;

public class FutureAnalyser extends Thread {

	private ConcurrentLinkedQueue<Future<QueryInfoBean>> queue = null;
	private boolean doRun = true;

	public FutureAnalyser(ConcurrentLinkedQueue<Future<QueryInfoBean>> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while (this.doRun) {
			Future<QueryInfoBean> future = this.queue.poll();
			if (future != null) {
				try {
					QueryInfoBean bean = future.get(10, TimeUnit.SECONDS);
					int result = bean.getResult();
					if (result <= 0) {
						logger.warning("Query Failed : " + bean.getQuery());
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		while (!this.queue.isEmpty()) {
			Future<QueryInfoBean> future = this.queue.poll();
			try {
				QueryInfoBean bean = future.get(10, TimeUnit.SECONDS);
				int result = bean.getResult();
				if (result <= 0) {
					logger.warning("Query Failed : " + bean.getQuery());
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	public void finish() {
		this.doRun = false;
	}
}