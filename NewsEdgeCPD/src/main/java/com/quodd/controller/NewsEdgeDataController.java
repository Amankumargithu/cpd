package com.quodd.controller;

import java.util.logging.Logger;

import com.quodd.collector.NewsEdgeDataCollector;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.QuoddProperty;
import com.quodd.distributor.NewsEdgeDataDistributor;
import com.quodd.parser.NewsEdgeDataParser;
import com.quodd.queue.MessageQueue;
import com.quodd.util.CounterUtility;

public class NewsEdgeDataController {
	public static final Logger logger = QuoddLogger.getInstance("edgenews_cpd").getLogger();
	public static final QuoddProperty newsProperties = new QuoddProperty("/news.properties");
	public static final QuoddProperty dbProperties = new QuoddProperty("/db.properties");
	private NewsEdgeDataCollector collector = null;
	private CounterUtility counterUtilityThread = null;
	private NewsEdgeDataParser[] parserArray = null;
	private MessageQueue[] inputQueueArray = null;
	private NewsEdgeDataDistributor distributor = null;

	public NewsEdgeDataController() {
		logger.info("PROCESS STARTED");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (collector != null) {
						collector.Stop();
						collector.join();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (parserArray != null) {
					for (NewsEdgeDataParser parser : parserArray) {
						try {
							if (parser != null) {
								parser.Stop();
								parser.join();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					if (distributor != null) {
						distributor.stopThread();
						distributor.join();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long recordCount = 0;
				try {
					if (counterUtilityThread != null) {
						recordCount = counterUtilityThread.getDistributorCount();
						counterUtilityThread.Stop();
						counterUtilityThread.join();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.info("PROCESS STOPPED");
			}
		}, "EdgeShutdownHookThread"));
		int noOfParsers = newsProperties.getIntProperty("parserCount", 4);
		distributor = new NewsEdgeDataDistributor(); // Distributor parser took little time to initialize. So to avoid
														// filling queues during high data time, required to initialize
														// earlier
		parserArray = new NewsEdgeDataParser[noOfParsers];
		inputQueueArray = new MessageQueue[noOfParsers];
		MessageQueue distributorMessageQueue = new MessageQueue();
		for (int i = 0; i < noOfParsers; i++) {
			inputQueueArray[i] = new MessageQueue();
			parserArray[i] = new NewsEdgeDataParser(distributorMessageQueue, inputQueueArray[i]);
			parserArray[i].setName("NewsEdgeDataParser_" + i);
			parserArray[i].start();
		}
		collector = new NewsEdgeDataCollector(inputQueueArray);
		collector.setName("NewsEdgeDataCollector");
		collector.start();

		distributor.setInputQueue(distributorMessageQueue);
		distributor.setName("NewsEdgeDataDistributor");
		distributor.start();
		counterUtilityThread = new CounterUtility();
		counterUtilityThread.setName("NewsEdgeCounterThread");
		counterUtilityThread.start();
	}

	public static void main(String[] args) {
		new NewsEdgeDataController();
	}
}