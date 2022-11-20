package com.quodd.collector;

import static com.quodd.controller.NewsEdgeDataController.logger;
import static com.quodd.controller.NewsEdgeDataController.newsProperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.quodd.queue.MessageQueue;
import com.quodd.util.CounterUtility;

public class NewsEdgeDataCollector extends Thread {
	private MessageQueue[] parserQueueArray;
	private String inputFilePath = null;
	private String localFilePath = null;
	private boolean doRun = true;
	private File inputFileDir = null;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
	private int index = 0;

	public NewsEdgeDataCollector(MessageQueue[] parserQueueArray) {
		this.parserQueueArray = parserQueueArray;
		inputFilePath = newsProperties.getStringProperty("inputFolder", "/home/nadmin/rxcontent");
		inputFileDir = new File(inputFilePath);
		localFilePath = newsProperties.getStringProperty("tempFolder", "/home/process/temp");
	}

	@Override
	public void run() {
		logger.info("Start thread " + this.getName());
		while (doRun) {
			String[] listOfFiles = inputFileDir.list();
			if (listOfFiles == null || listOfFiles.length < 1) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				continue;
			}
			distributeFilesToParsers(listOfFiles);
		}
	}

	public void Stop() {
		this.doRun = false;
		logger.info("Stop thread " + this.getName());
	}

	private void distributeFilesToParsers(String[] listOfFiles) {
		if (listOfFiles == null || listOfFiles.length < 1)
			return;
		int parserCount = parserQueueArray.length;
		for (int i = 0; i < listOfFiles.length; i++) {
			String fileName = listOfFiles[i];
			if (!fileName.endsWith(".xml")) {
				continue;
			}
			File file = new File(inputFilePath, fileName);
			if (file.isFile()) {
				File newLocationPath = new File(localFilePath, formatter.format(new Date()));
				if (!newLocationPath.exists())
					newLocationPath.mkdirs();
				File newFile = new File(newLocationPath.getAbsolutePath(), fileName);
				boolean doesRenamed = file.renameTo(newFile);
				if (!doesRenamed)
					logger.warning("Cannot move file " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath());
				else {
					MessageQueue queue = parserQueueArray[index % parserCount];
					queue.add(newFile);
					CounterUtility.incrementCollectorCounter();
					index++;
					logger.info("COLLECTED FILE " + newFile.getAbsolutePath());
				}
			} else {
				logger.warning("Could not find file " + file.getAbsolutePath());
			}
		}
	}
}