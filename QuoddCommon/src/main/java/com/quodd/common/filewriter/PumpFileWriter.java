package com.quodd.common.filewriter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

public class PumpFileWriter implements Runnable {
	private static final long BUFFER_SIZE = 512 * 1024 * 1024l;
	private final ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();
	private boolean isRunning = false;
	private final File parentDir;
	private File eodFile;
	private final String name;
	private final long buffersize;
	private long currentWrite = 0;
	private MappedByteBuffer wrBuf;
	private FileChannel rwChannel;
	private RandomAccessFile rwfile;
	private long fileSize = 0;
	private long count = 0;
	private final Logger logger = QuoddLogger.getInstance().getLogger();

	public PumpFileWriter(File parentDir, String filename, long bufferSize) {
		this.buffersize = bufferSize;
		this.name = filename;
		this.parentDir = parentDir;
		try {
			if (!parentDir.exists())
				parentDir.mkdirs();
			this.eodFile = new File(parentDir, filename);
			this.logger.info("Created Eod file " + this.eodFile.getAbsolutePath());
			this.rwfile = new RandomAccessFile(this.eodFile, "rw");
			this.rwChannel = this.rwfile.getChannel();
			this.wrBuf = this.rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, this.buffersize);
		} catch (IOException e) {
			this.logger.log(Level.WARNING, "Error in constructor of PumpFileWriter " + e.getLocalizedMessage(), e);
		}
	}

	public void add(byte[] s) {
		if (this.isRunning) {
			this.queue.add(s);
			this.count++;
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
				this.logger.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
		try {
			while (!this.queue.isEmpty()) {
				byte[] o = this.queue.remove();
				save(o);
			}
		} catch (Exception e) {
			this.logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		try {
			this.fileSize += this.currentWrite;
			this.rwfile.setLength(this.fileSize);
			this.wrBuf.force();
			this.rwChannel.close();
			this.wrBuf.clear();
			this.rwfile.close();
			if (this.count <= 1) {
				// Delete file as only header is present
				this.logger.info("Deleting file " + this.eodFile.getAbsolutePath());
				boolean isdeleted = this.eodFile.delete();
				this.logger.info(() -> "Deleting file " + this.eodFile.getAbsolutePath() + " " + isdeleted);
			}
		} catch (Exception e) {
			this.logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		this.logger.info(() -> "Closing " + this.name);
	}

	public void stopThread() {
		this.isRunning = false;
	}

	private void save(byte[] byteArr) {
		try {
			if (this.currentWrite + byteArr.length < this.buffersize) {
				this.wrBuf.put(byteArr);
				this.currentWrite += byteArr.length;
			} else {
				this.logger.info(() -> "Reallocating memory to " + this.name);
				this.wrBuf.force();
				this.fileSize += this.currentWrite;
				this.rwfile.setLength(this.fileSize);
				this.rwChannel.close();
				this.currentWrite = 0;
				this.wrBuf.clear();
				this.rwfile.close();
				this.eodFile = new File(this.eodFile.getAbsolutePath());
				this.rwfile = new RandomAccessFile(this.eodFile, "rw");
				this.rwfile.seek(this.eodFile.length());
				this.rwChannel = this.rwfile.getChannel();
				this.wrBuf = this.rwChannel.map(FileChannel.MapMode.READ_WRITE, this.eodFile.length(), this.buffersize);
				if (this.currentWrite + byteArr.length < this.buffersize) {
					this.wrBuf.put(byteArr);
					this.currentWrite += byteArr.length;
				}
			}
		} catch (Exception e) {
			this.logger.log(Level.WARNING, "Error while saving " + e.getLocalizedMessage(), e);
		}
	}

	public void printPending() {
		this.logger.info(() -> this.name + " " + this.queue.size());
	}
}
