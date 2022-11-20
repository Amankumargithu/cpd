package com.quodd.fileWriter;

import static com.quodd.controller.DJController.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class RawFileWriter implements Runnable {
	private static final long SIZE = 512 * 1024 * 1024;

	private ConcurrentLinkedQueue<byte[]> dataQueue = new ConcurrentLinkedQueue<>();
	private boolean isRunning = false;
	private File dataFile = null;
	private FileChannel dataChannel = null;
	private RandomAccessFile dataAccessFile = null;
	private MappedByteBuffer dataBuffer = null;
	private long buffersize = SIZE;
	private long currentWriteCount = 0;
	private long fileSize = 0;

	private MappedByteBuffer indexBuffer;
	private RandomAccessFile indexRwfile;

	public RawFileWriter(String dataFilePath, String indexFilePath, long bufferSize) {
		this.buffersize = bufferSize;
		createDataFile(dataFilePath);
		createIndexFile(indexFilePath);
	}

	private void createDataFile(String filename) {
		try {
			dataFile = new File(filename);
			logger.info("Created Eod file " + dataFile.getAbsolutePath());
			dataAccessFile = new RandomAccessFile(dataFile, "rwd");
			fileSize = dataFile.length();
			dataAccessFile.seek(this.fileSize);
			dataChannel = dataAccessFile.getChannel();
			dataBuffer = dataChannel.map(FileChannel.MapMode.READ_WRITE, fileSize, buffersize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createIndexFile(String filename) {
		try {
			File indexFile = new File(filename);
			logger.info("Created Eod file " + indexFile.getAbsolutePath());
			indexRwfile = new RandomAccessFile(indexFile, "rwd");
			indexRwfile.seek(0);
			FileChannel indexChannel = indexRwfile.getChannel();
			indexBuffer = indexChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(byte[] s) {
		dataQueue.add(s);
	}

	@Override
	public void run() {
		if (!isRunning)
			isRunning = true;
		while (isRunning) {
			try {
				byte[] arr = dataQueue.remove();
				save(arr);
			} catch (NoSuchElementException e) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (dataQueue != null && dataQueue.size() > 0) {
			for (byte[] o : dataQueue) {
				try {
					save(o);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fileSize += currentWriteCount;
			dataAccessFile.setLength(fileSize);
			dataBuffer.force();
			dataChannel.close();
			dataBuffer.clear();
			dataAccessFile.close();
			indexBuffer.force();
			indexBuffer.clear();
			indexRwfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Closing " + dataFile.getAbsolutePath());
	}

	public void finish() {
		isRunning = false;
	}

	private void save(byte[] byteArr) throws Exception {
		if (currentWriteCount + byteArr.length < buffersize) {
			dataBuffer.put(byteArr);
			currentWriteCount += byteArr.length;
		} else {
			logger.info("Reallocating memory to " + dataFile.getAbsolutePath());
			dataBuffer.force();
			fileSize += currentWriteCount;
			dataAccessFile.setLength(fileSize);
			dataChannel.close();
			currentWriteCount = 0;
			dataBuffer.clear();
			dataAccessFile.close();
			createDataFile(this.dataFile.getAbsolutePath());
			if (currentWriteCount + byteArr.length < buffersize) {
				dataBuffer.put(byteArr);
				currentWriteCount += byteArr.length;
			}
		}
		indexBuffer.position(0);
		indexBuffer.putLong(fileSize + currentWriteCount);
	}

	public void printPending() {
		logger.info(dataFile.getAbsolutePath() + " " + dataQueue.size());
	}
}
