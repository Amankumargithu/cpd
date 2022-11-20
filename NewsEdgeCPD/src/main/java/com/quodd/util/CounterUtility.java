package com.quodd.util;

import static com.quodd.controller.NewsEdgeDataController.logger;
import static com.quodd.controller.NewsEdgeDataController.newsProperties;

import java.util.logging.Level;

public class CounterUtility extends Thread {
	private static long collectorCounter = 0;
	private static long parserCounter = 0;
	private static long distributorCounter = 0;
	private boolean doRun = true;

	public static synchronized void incrementCollectorCounter() {
		++collectorCounter;
	}

	public static synchronized void incrementParserCounter() {
		++parserCounter;
	}

	public static synchronized void incrementDistributorCounter() {
		++distributorCounter;
	}

	@Override
	public void run() {
		logger.info("Start thread " + this.getName());
		int sleepTime = newsProperties.getIntProperty("COUNTER_THREAD_SLEEP_TIME", 30_000);
		while (doRun) {
			try {
				Thread.sleep(sleepTime);
				logger.info("CounterUtility: Collector: " + collectorCounter + " Parser: " + parserCounter
						+ " Distributor: " + distributorCounter);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	public void Stop() {
		doRun = false;
		logger.info("Stop thread " + this.getName());
	}

	public long getDistributorCount() {
		return distributorCounter;
	}
}