package com.quodd.common.api.stat;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.csvreader.CsvWriter;
import com.quodd.common.collection.MessageQueue;
import com.quodd.common.logger.QuoddLogger;

//rotate stat file every 100,000 messages or every 30 mins
public class ApiStatRecorder implements Runnable, Closeable {
	private static final int STAT_ROTATE_COUNT = 100_000;
	private static final int STAT_ROTATE_TIME_MILLISECOND = 1_800_000;
	private MessageQueue statQueue;
	private int statCounter = 0;
	private boolean doRun = false;
	private String statDirectoryPath = null;
	private CsvWriter statWriter;
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private long fileStartTime = 0;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

	public ApiStatRecorder(String statDirPath) {
		this.statQueue = new MessageQueue();
		this.statDirectoryPath = statDirPath;
		if (statDirPath == null || statDirPath.trim().length() == 0)
			this.statDirectoryPath = "/home/stat";
		File statDirFile = new File(this.statDirectoryPath);
		if (!statDirFile.exists())
			statDirFile.mkdirs();
		this.statWriter = new CsvWriter(
				this.statDirectoryPath + '/' + "statlog_" + LocalDateTime.now().format(dtf) + ".csv");
		this.fileStartTime = System.currentTimeMillis();
		writeHeader();
		this.doRun = true;
	}

	// Add only if thread is running. Else disable
	public void addStat(ApiStatBean statBean) {
		if (this.doRun)
			this.statQueue.add(statBean);
	}

	private void writeHeader() {
		try {
			this.statWriter.write("sessionId");
			this.statWriter.write("requestIp");
			this.statWriter.write("userId");
			this.statWriter.write("companyCode");
			this.statWriter.write("isAuthenticated");
			this.statWriter.write("requestTimeStamp");
			this.statWriter.write("service");
			this.statWriter.write("identifier");
			this.statWriter.write("uniqueIdentifier");
			this.statWriter.write("reasonForFailure");
			this.statWriter.write("marketTier");
			this.statWriter.endRecord();
			this.statWriter.flush();
		} catch (IOException e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void writeRecord(ApiStatBean bean) {
		try {
			this.statWriter.write(bean.getSessionId());
			this.statWriter.write(bean.getRequestIp());
			this.statWriter.write("" + bean.getUserId());
			this.statWriter.write(bean.getCompanyCode());
			this.statWriter.write("" + bean.isAuthenticated());
			this.statWriter.write("" + bean.getRequestTimeStamp());
			this.statWriter.write("" + bean.getServiceId());
			this.statWriter.write(bean.getIdentifier());
			this.statWriter.write(bean.getUniqueIdentifier());
			this.statWriter.write(bean.getReasonForFailure());
			this.statWriter.write(bean.getMarketTier());
			this.statWriter.endRecord();
			this.statWriter.flush();
			this.statCounter++;
		} catch (IOException e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void rotateStatFile() {
		this.statWriter.close();
		this.statWriter = new CsvWriter(
				this.statDirectoryPath + '/' + "statlog_" + LocalDateTime.now().format(dtf) + ".csv");
		this.fileStartTime = System.currentTimeMillis();
		writeHeader();
		this.statCounter = 0;
	}

	@Override
	public void run() {
		while (this.doRun) {
			if (this.statCounter >= STAT_ROTATE_COUNT
					|| System.currentTimeMillis() - this.fileStartTime >= STAT_ROTATE_TIME_MILLISECOND) {
				rotateStatFile();
			}
			List<Object> statList = this.statQueue.removeAllWithoutWait();
			if (statList != null) {
				for (Object tickerObject : statList) {
					ApiStatBean bean = (ApiStatBean) tickerObject;
					writeRecord(bean);
				}
			} else {
				try {
					TimeUnit.MICROSECONDS.sleep(100);
				} catch (InterruptedException e) {
					this.logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		// Process remaining
		List<Object> statList = this.statQueue.removeAllWithoutWait();
		if (statList != null) {
			for (Object tickerObject : statList) {
				ApiStatBean bean = (ApiStatBean) tickerObject;
				writeRecord(bean);
			}
		}
		this.statWriter.close();
	}

	@Override
	public void close() throws IOException {
		this.doRun = false;
	}
}
