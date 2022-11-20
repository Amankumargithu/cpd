package com.quodd.common.ibus;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

class IbusStats implements AutoCloseable {

	private long startTime;
	private HashMap<String, Long> statCountMap = new HashMap<>();
	private Thread loggerThread = null;
	private boolean doRunLogger = false;
	private final Logger logger = QuoddLogger.getInstance().getLogger();

	public IbusStats(final long startTime) {
		this.startTime = startTime;
		this.loggerThread = new Thread(() -> {
			while (this.doRunLogger) {
				StringBuilder sb = new StringBuilder();
				sb.append("IbusStats: ");
				for (Entry<String, Long> entry : this.statCountMap.entrySet()) {
					sb.append(entry.getKey() + " " + entry.getValue());
				}
				this.logger.info(() -> sb.toString());
				try {
					TimeUnit.MINUTES.sleep(5);
				} catch (Exception e) {
					this.logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}, "IbusStatThread");
	}

	public long getStartTime() {
		return this.startTime;
	}

	public void incrementStat(String statType) {
		Long count = this.statCountMap.get(statType);
		if (count == null)
			count = 0l;
		count++;
		this.statCountMap.put(statType, count);
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void startLoggerThread() {
		if (this.doRunLogger) // Thread is already running
			return;
		this.doRunLogger = true;
		this.loggerThread.start();
	}

	@Override
	public void close() {
		this.doRunLogger = false;
		if (this.loggerThread != null) {
			try {
				this.loggerThread.join();
			} catch (InterruptedException e) {
				this.logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
}