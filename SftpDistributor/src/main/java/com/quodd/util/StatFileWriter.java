package com.quodd.util;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

public class StatFileWriter implements Closeable, Runnable {

	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private final String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
	private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
	private boolean doRun = true;
	private FileWriter writer = null;

	public StatFileWriter(String clientName) throws IOException {
		this.writer = new FileWriter(clientName + "_" + this.currentDate + ".csv", true);
	}

	public void addMessage(String messgae) {
		this.queue.add(messgae);
	}

	@Override
	public void close() throws IOException {
		this.doRun = false;
		if (this.writer != null)
			this.writer.close();
	}

	@Override
	public void run() {
		while (this.doRun) {
			if (this.queue.isEmpty()) {
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			} else {
				String message = this.queue.remove();
				try {
					this.writer.write(message);
				} catch (IOException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
	}
}
