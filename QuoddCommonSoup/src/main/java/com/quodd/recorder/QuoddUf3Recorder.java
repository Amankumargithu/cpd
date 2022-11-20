package com.quodd.recorder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;
import com.quodd.soup.common.QuoddUf3Listener;

public class QuoddUf3Recorder implements Runnable {

	private static final long BUFFER_SIZE = 1024 * 1024; // 1MB
	protected static final Logger logger = QuoddLogger.getInstance().getLogger();
	private final RandomAccessFile file;
	private final FileChannel channel;
	private MappedByteBuffer mappedBuffer = null;
	private final long filseSize;
	private boolean doRun = true;
	private long previousOffset = 0;
	private long actualFileSize;
	private final QuoddUf3Listener listener;

	public QuoddUf3Recorder(String filename, QuoddUf3Listener listener) throws IOException {
		this.listener = listener;
		this.file = new RandomAccessFile(filename, "rw");
		this.channel = this.file.getChannel();
		this.filseSize = this.channel.size();
		this.actualFileSize = this.filseSize;
	}

	private void loadBuffer(long offset) throws IOException {
		this.previousOffset += offset;
		if (this.previousOffset + BUFFER_SIZE < this.filseSize)
			this.mappedBuffer = this.channel.map(MapMode.READ_ONLY, this.previousOffset, BUFFER_SIZE);
		else if (this.previousOffset == this.filseSize)
			this.doRun = false;
		else
			this.mappedBuffer = this.channel.map(MapMode.READ_ONLY, this.previousOffset,
					this.filseSize - this.previousOffset);
	}

	public void stop(String reasonForClose) {
		if (this.doRun) {
			logger.info(" Requested thread close");
			this.doRun = false;
			try {
				if (this.channel != null)
					this.channel.close();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			try {
				if (this.file != null)
					this.file.close();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			this.listener.stopListener(reasonForClose);
		}
	}

	@Override
	public void run() {
		try {
			loadBuffer(0);
			while (this.doRun) {
				if (this.mappedBuffer.remaining() < 2) {
					loadBuffer(this.mappedBuffer.position());
					continue;
				}
				int size = Short.toUnsignedInt(this.mappedBuffer.getShort());
				if (size == 0) {
					logger.warning("zero size - " + this.previousOffset + " " + this.mappedBuffer.position());
					this.actualFileSize = this.previousOffset + this.mappedBuffer.position() - 2;
					this.file.setLength(this.actualFileSize);
					break;
				}
				if (this.mappedBuffer.remaining() < size)
					loadBuffer(this.mappedBuffer.position());
				if (size == 1) {
					this.mappedBuffer.get();
					continue;
				}
				ByteBuffer buffer = this.mappedBuffer.slice();
				buffer.limit(size);
				this.mappedBuffer.position(this.mappedBuffer.position() + size);
				if (buffer.get() != 0x53) {
					// Every message should have 3rd byte as 0x533 - soup protocol
					logger.warning("Unknown Message " + buffer.remaining());
				} else {
					this.listener.message(buffer);
				}
			}
		} catch (Exception e) {
			stop("Intruppted " + e.getMessage());
		}
	}

}
