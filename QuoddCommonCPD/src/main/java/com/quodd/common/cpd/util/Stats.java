package com.quodd.common.cpd.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

public class Stats implements AutoCloseable{

	private long startTime;
	private long droppedTradeMessages;
	private long processedUCTradeMessages;
	private long iBusWriteTradeMessages;
	private long droppedQuoteMessages;
	private long processedUCQuoteMessages;
	private long iBusWriteQuoteMessages;
	private Thread loggerThread = null;
	private boolean doRun = false;
	private final Logger logger = QuoddLogger.getInstance().getLogger();

	public Stats(long startTime) {
		this.startTime = startTime;
		this.loggerThread = new Thread(() -> {
			while (this.doRun) {
				this.logger.info(() -> "Stats: ucProcessedTrade " + this.processedUCTradeMessages + " iBusWriteTrades "
						+ this.iBusWriteTradeMessages + " tradeDropCount " + this.droppedTradeMessages
						+ " ucProcessedQuote " + this.processedUCQuoteMessages + " iBusWriteQuotes "
						+ this.iBusWriteQuoteMessages + " quoteDropCount " + this.droppedQuoteMessages);
				try {
					TimeUnit.MINUTES.sleep(5);
				} catch (Exception e) {
					this.logger.fine("Exception in sleep thread " + e.getLocalizedMessage());
				}
			}
		}, "STAT_LOGGER");
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getDroppedTradeMessages() {
		return this.droppedTradeMessages;
	}

	public long getProcessedUCTradeMessages() {
		return this.processedUCTradeMessages;
	}

	public long getiBusWriteTradeMessages() {
		return this.iBusWriteTradeMessages;
	}

	public long getDroppedQuoteMessages() {
		return this.droppedQuoteMessages;
	}

	public long getProcessedUCQuoteMessages() {
		return this.processedUCQuoteMessages;
	}

	public long getiBusWriteQuoteMessages() {
		return this.iBusWriteQuoteMessages;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void incrementDroppedTradeMessages() {
		this.droppedTradeMessages++;
	}

	public void incrementProcessedUCTradeMessages() {
		this.processedUCTradeMessages++;
	}

	public void incrementIBusWriteTradeMessages() {
		this.iBusWriteTradeMessages++;
	}

	public void incrementDroppedQuoteMessages() {
		this.droppedQuoteMessages++;
	}

	public void incrementProcessedUCQuoteMessages() {
		this.processedUCQuoteMessages++;
	}

	public void incrementiBusWriteQuoteMessages() {
		this.iBusWriteQuoteMessages++;
	}

	public void startLoggerThread() {
		if (this.doRun) // THread is already running
			return;
		this.doRun = true;
		this.loggerThread.start();
	}

	@Override
	public void close() {
		this.doRun = false;
	}
}
