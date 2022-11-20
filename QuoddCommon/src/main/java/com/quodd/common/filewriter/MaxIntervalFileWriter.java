package com.quodd.common.filewriter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

public class MaxIntervalFileWriter implements Runnable {
//	private static final long BUFFER_SIZE = 512 * 1024 * 1024l;
	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private final ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();
	private boolean isRunning = false;
	private final File parentDir;
	private File eodFile;
	private final long buffersize;
	private long currentWrite = 0;
	private int currentRecordCount = 0;
	private MappedByteBuffer wrBuf;
	private FileChannel rwChannel;
	private RandomAccessFile rwfile;
	private long fileSize = 0;
	private final long maxIntervalMillis;
	private long fileStartTime = 0;
	private final String baseFilename;
	private final String extension;
	private final int maxRecords;

	public MaxIntervalFileWriter(File parentDir, String baseFilename, long bufferSize, long maxInterval,
			String extension, int maxRecords) {
		this.buffersize = bufferSize;
		this.maxIntervalMillis = maxInterval;
		this.maxRecords = maxRecords;
		this.fileStartTime = System.currentTimeMillis();
		this.baseFilename = baseFilename;
		this.extension = extension;
		String name = baseFilename + "_" + this.fileStartTime + extension;
		this.parentDir = parentDir;
		try {
			if (!parentDir.exists())
				parentDir.mkdirs();
			createFile(name);
			logger.info("Created Eod file " + this.eodFile.getAbsolutePath());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error in constructor of PumpFileWriter " + e.getLocalizedMessage(), e);
		}
	}

	public void add(byte[] s) {
		if (this.isRunning) {
			this.queue.add(s);
		}
	}

	@Override
	public void run() {
		if (!this.isRunning) {
			this.isRunning = true;
		}
		while (this.isRunning) {
			try {
				while (!this.queue.isEmpty()) {
					byte[] o = this.queue.remove();
					save(o);
				}
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
		try {
			while (!this.queue.isEmpty()) {
				byte[] o = this.queue.remove();
				save(o);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		try {
			closeFile();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		logger.info(() -> "Closing " + this.eodFile.getAbsolutePath());
	}

	public void stopThread() {
		this.isRunning = false;
	}

	private void save(byte[] byteArr) {
		try {
			long currentTime = System.currentTimeMillis();
			if (currentTime - this.fileStartTime >= this.maxIntervalMillis
					|| this.currentRecordCount >= this.maxRecords) {
				closeFile();
				if (this.eodFile.exists()) {
					Path source = this.eodFile.toPath();
					Files.move(source, source.resolveSibling(this.eodFile.getName() + ".C"));
				}
				if (this.fileStartTime == currentTime)
					this.fileStartTime = currentTime + 1;
				else
					this.fileStartTime = currentTime;
				String name = this.baseFilename + "_" + this.fileStartTime + this.extension;
				this.currentRecordCount = 0;
				createFile(name);
			}
			if (this.currentWrite + byteArr.length >= this.buffersize) {
				logger.info(() -> "Reallocating memory to " + this.eodFile.getAbsolutePath());
				closeFile();
				createFile(this.eodFile.getName());
			}
			// check again in case memory did not get allocated
			if (this.currentWrite + byteArr.length < this.buffersize) {
				writeData(byteArr);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while saving " + e.getLocalizedMessage(), e);
		}
	}

	private void writeData(byte[] byteArr) {
		this.currentRecordCount++;
		this.wrBuf.put(byteArr);
		this.currentWrite += byteArr.length;
	}

	private void createFile(String filename) throws IOException {
		this.eodFile = new File(this.parentDir, filename);
		this.rwfile = new RandomAccessFile(this.eodFile, "rw");
		this.fileSize = this.eodFile.length();
		this.rwfile.seek(this.fileSize);
		this.rwChannel = this.rwfile.getChannel();
		this.wrBuf = this.rwChannel.map(FileChannel.MapMode.READ_WRITE, this.fileSize, this.buffersize);
	}

	private void closeFile() throws IOException {
		this.wrBuf.force();
		this.fileSize += this.currentWrite;
		this.rwfile.setLength(this.fileSize);
		this.rwChannel.close();
		this.currentWrite = 0;
		this.wrBuf.clear();
		this.rwfile.close();
		deleteFile();
	}

	private void deleteFile() throws IOException {
		if (this.fileSize == 0) {
			Files.delete(this.eodFile.toPath());
			logger.info("Deleting file " + this.eodFile.getAbsolutePath());
		}
	}

	public void printPending() {
		logger.info(() -> this.eodFile.getAbsolutePath() + " " + this.queue.size());
	}
}
