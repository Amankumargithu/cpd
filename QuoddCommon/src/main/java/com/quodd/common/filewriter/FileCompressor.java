package com.quodd.common.filewriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import com.quodd.common.logger.QuoddLogger;

public class FileCompressor implements Runnable {

	private final ConcurrentLinkedQueue<File> queue = new ConcurrentLinkedQueue<>();
	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private boolean isRunning = false;
	private long threadWaitingTime = 1000;

	private Map<String, Long> fileCompressedSizeMap = new HashMap<>();

	public void add(File file) {
		if (this.isRunning) {
			this.queue.add(file);
		}
	}

	@Override
	public void run() {
		logger.info("Started Thread");
		if (!this.isRunning) {
			this.isRunning = true;
		}
		while (this.isRunning) {
			while (!this.queue.isEmpty()) {
				File inputFile = this.queue.remove();
				if (inputFile.exists()) {
					File outputFile = new File(inputFile.getAbsoluteFile() + ".gz");
					try {
						compressFile(inputFile, outputFile);
						Files.delete(inputFile.toPath());
					} catch (Exception e) {
						logger.log(Level.WARNING, inputFile.getAbsolutePath() + " " + e.getLocalizedMessage(), e);
					}
				}
			}
			try {
				TimeUnit.MILLISECONDS.sleep(this.threadWaitingTime);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
		while (!this.queue.isEmpty()) {
			File inputFile = this.queue.remove();
			File outputFile = new File(inputFile.getAbsoluteFile() + ".gz");
			try {
				compressFile(inputFile, outputFile);
				Files.delete(inputFile.toPath());
			} catch (Exception e) {
				logger.log(Level.WARNING, inputFile.getAbsolutePath() + " " + e.getLocalizedMessage(), e);
			}
		}
		logger.info("Ended Thread");
	}

	private void compressFile(File input, File output) throws FileNotFoundException, IOException {
		long inputFileSize = input.length();
		int hour = 0;
		int minute = 0;
		try {
			// OPRA2_Trade_1318.csv
			String fileName = input.getName();
			String[] tempFileNameArr = fileName.split("_");
			if (tempFileNameArr.length > 2) {
				String fileTime = tempFileNameArr[2];
				fileTime = fileTime.substring(0, fileTime.length() - 4);
				int hourmin = Integer.parseInt(fileTime);
				hour = hourmin / 100;
				minute = hourmin % 100;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		long fileCompletionTime = LocalDateTime.now().withHour(hour).withMinute(minute).plusMinutes(1)
				.toEpochSecond(ZoneOffset.UTC);
		logger.info("Compressing " + input.getAbsolutePath());
		long startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		try (InputStream is = new FileInputStream(input);
				GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(output));) {
			int data = -1;
			byte[] buffer = new byte[10240];
			while ((data = is.read(buffer)) > 0) {
				gos.write(buffer, 0, data);
			}
		}
		long endTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		this.fileCompressedSizeMap.put(input.getName(), output.length());
		logger.info("Compressed to " + output.getAbsolutePath() + " inputFileSize " + inputFileSize + " outputFileSize "
				+ output.length() + " compressionTime(sec) " + (endTime - startTime) + " totalFileDelay "
				+ (endTime - fileCompletionTime));
	}

	public void stopThread() {
		this.isRunning = false;
	}

	public Map<String, Long> getFileCompressedSizeMap() {
		return this.fileCompressedSizeMap;
	}

}
