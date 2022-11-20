package com.quodd.common.cpd.monitoring;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

public class JVMMemoryMonitor {
	private static final int MB = 1024 * 1024;
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private Thread jvmMonitorThread;
	private boolean doRun = true;
	private final Runtime runtime = Runtime.getRuntime();
	private long startTime = System.currentTimeMillis();
	private long timeInterval;

	public JVMMemoryMonitor(long timeInterval) {
		this.timeInterval = timeInterval;
		this.jvmMonitorThread = new Thread(() -> {
			while (this.doRun) {
				try {
					TimeUnit.SECONDS.sleep(1);
					if (System.currentTimeMillis() - this.startTime > this.timeInterval) {
						float usedMemory = this.runtime.totalMemory() - this.runtime.freeMemory();
						this.logger.info(() -> "#############################################" + "\n"
								+ "##### JVM memory utilization statistics #####" + "\n" + "Used Memory: "
								+ usedMemory / JVMMemoryMonitor.MB + "\n" + "Free Memory: "
								+ this.runtime.freeMemory() / JVMMemoryMonitor.MB + "\n" + "Total Memory: "
								+ this.runtime.totalMemory() / JVMMemoryMonitor.MB + "\n" + "Max Memory: "
								+ this.runtime.maxMemory() / JVMMemoryMonitor.MB + "\n"
								+ "#############################################");
						this.startTime = System.currentTimeMillis();
					}
				} catch (Exception e) {
					this.logger.log(Level.WARNING, "Exception in JVMMemoryMonitor Thread " + e.getMessage(), e);
				}
			}
		}, "JvmMonitoringThread");
	}

	public void startThread() {
		if (!this.doRun) {
			this.doRun = true;
			this.jvmMonitorThread.start();
		}
	}

	public void stopThread() {
		this.logger.info("Stopping JvmMonitoringThread");
		this.doRun = false;
	}
}
