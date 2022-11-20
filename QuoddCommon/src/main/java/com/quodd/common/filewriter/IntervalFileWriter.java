package com.quodd.common.filewriter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.quodd.common.logger.QuoddLogger;

public class IntervalFileWriter implements Runnable {
	private static final long BUFFER_SIZE = 512 * 1024 * 1024l;
	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	private static final Gson gson = new Gson();
	private final ConcurrentLinkedQueue<IntervalBean> queue = new ConcurrentLinkedQueue<>();
	private boolean isRunning = false;
	private final File parentDir;
	private File eodFile;
	private final long buffersize;
	private long currentWrite = 0;
	private MappedByteBuffer wrBuf;
	private FileChannel rwChannel;
	private RandomAccessFile rwfile;
	private long fileSize = 0;
	private final long fixedIntervalFactor;
	private long currentQuantizedTimestamp = 0;
	private String currentMessageDate = null;
	private final String baseFilename;
	private final String extension;
	private long threadWaitingTime = 50;
	private final long fileRotationMillis;
	private FileCompressor compressor = null;
	private long fileRecordCount = 0;
	private FileWriter statWriter;
	private String serverName;
	private final String statUrl;

	private final Map<String, Long> fileNameCountMap = new LinkedHashMap<>();
	private final Map<String, Long> fileNameSizeMap = new LinkedHashMap<>();
	private final ExecutorService executor;
	private final int fileNameTimestampLength;
	private final boolean isPadding;

	public IntervalFileWriter(File parentDir, String baseFilename, long bufferSize, long fixedInterval,
			String extension, long fileRotationTime, ExecutorService executor, String statUrl, boolean isPadding) {
		this.buffersize = bufferSize;
		this.isPadding = isPadding;
		this.fixedIntervalFactor = fixedInterval;
		this.currentQuantizedTimestamp = 0;
		this.baseFilename = baseFilename;
		this.fileRotationMillis = fileRotationTime;
		this.extension = extension;
		this.executor = executor;
		this.statUrl = statUrl;
		this.fileNameTimestampLength = Integer.toString((int) (999_999 / fixedInterval)).length();
		File statFile = new File(parentDir, baseFilename + "_stats.st");
		this.parentDir = parentDir;
		try {
			if (!parentDir.exists())
				parentDir.mkdirs();
			createFile(getFileName());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error in constructor of PumpFileWriter " + e.getLocalizedMessage(), e);
		}
		try {
			this.statWriter = new FileWriter(statFile);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error in constructor of PumpFileWriter " + e.getLocalizedMessage(), e);
		}
		try {
			this.serverName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			this.serverName = "localhost";
		}
	}

	public void add(IntervalBean bean) {
		if (this.isRunning) {
			this.queue.add(bean);
		}
	}

	@Override
	public void run() {
		if (!this.isRunning) {
			this.isRunning = true;
		}
		long lastProcessedTime = 0;
		long lastMessageTimestamp = 0;
		while (this.isRunning) {
			try {
				while (!this.queue.isEmpty()) {
					IntervalBean bean = this.queue.remove();
					lastProcessedTime = System.currentTimeMillis();
					lastMessageTimestamp = bean.getMessageTimestamp();
					save(bean);
				}
				TimeUnit.MILLISECONDS.sleep(this.threadWaitingTime);
				if (System.currentTimeMillis() - lastProcessedTime > this.fileRotationMillis
						&& lastMessageTimestamp > 0) {
					add(new IntervalBean(lastMessageTimestamp + this.fixedIntervalFactor, null));
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
		try {
			while (!this.queue.isEmpty()) {
				IntervalBean bean = this.queue.remove();
				save(bean);
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
		if (this.compressor != null) {
			this.compressor.add(new File(this.eodFile.getAbsolutePath()));
		}
	}

	public void stopThread() {
		this.isRunning = false;
		try {
			if (this.statWriter != null)
				this.statWriter.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	private String getFileName() {
		String timestamp = Long.toString(this.currentQuantizedTimestamp);
		StringBuilder sb = new StringBuilder();
		sb.append(this.baseFilename);
		sb.append("_");
		if (this.isPadding) {
			int timestampLength = timestamp.length();
			for (int i = this.fileNameTimestampLength; i > timestampLength; i--) {
				sb.append("0");
			}
		}
		sb.append(timestamp);
		sb.append(this.extension);
		return sb.toString();
	}

	private void save(IntervalBean bean) {
		long quantizedTime = getQunatizedTime(bean.getMessageTimestamp());
		byte[] byteArr = bean.getMessage();
		try {
			// In case of futures, session moves to next day
			if (quantizedTime > this.currentQuantizedTimestamp || (bean.getMessageDate() != null
					&& !this.currentMessageDate.equalsIgnoreCase(bean.getMessageDate()))) {
				closeFile();
				writeStat();
				this.fileRecordCount = 0;
				// if set, gzip this file
				if (this.compressor != null) {
					this.compressor.add(new File(this.eodFile.getAbsolutePath()));
				}
				this.currentQuantizedTimestamp = quantizedTime;
				this.currentMessageDate = bean.getMessageDate();
				createFile(getFileName());
			}
			if (byteArr != null) {
				if (this.currentWrite + byteArr.length >= this.buffersize) {
					logger.info(() -> "Reallocating memory to " + this.eodFile.getAbsolutePath());
					closeFile();
					createFile(this.eodFile.getName());
				}
				// check again in case memory did not get allocated
				if (this.currentWrite + byteArr.length < this.buffersize) {
					this.currentMessageDate = bean.getMessageDate();
					writeData(byteArr);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while saving " + e.getLocalizedMessage(), e);
		}
	}

	private void writeData(byte[] byteArr) {
		this.wrBuf.put(byteArr);
		this.currentWrite += byteArr.length;
		this.fileRecordCount++;
	}

	private long getQunatizedTime(long messageTime) {
		return messageTime / this.fixedIntervalFactor;
	}

	private void createFile(String filename) throws IOException {
		this.eodFile = new File(this.parentDir, filename);
		this.rwfile = new RandomAccessFile(this.eodFile, "rw");
		this.fileSize = this.eodFile.length();
		this.rwfile.seek(this.fileSize);
		this.rwChannel = this.rwfile.getChannel();
		this.wrBuf = this.rwChannel.map(FileChannel.MapMode.READ_WRITE, this.fileSize, this.buffersize);
		logger.info("Created " + this.eodFile.getAbsolutePath());
	}

	private void closeFile() throws IOException {
		this.wrBuf.force();
		this.fileSize += this.currentWrite;
		this.rwfile.setLength(this.fileSize);
		this.rwChannel.close();
		this.currentWrite = 0;
		this.wrBuf.clear();
		this.rwfile.close();
		logger.info("Closed " + this.eodFile.getAbsolutePath());
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

	public void setThreadWaitingTime(long threadWaitingTime) {
		this.threadWaitingTime = threadWaitingTime;
	}

	public void setCompressor(FileCompressor compressor) {
		this.compressor = compressor;
	}

	private void writeStat() {
		String filename = this.eodFile.getName();
		if (this.statWriter != null && this.fileSize > 0) {
			this.fileNameCountMap.put(filename, this.fileRecordCount);
			this.fileNameSizeMap.put(filename, this.fileSize);
			schedulePushApi(filename, this.fileRecordCount, this.fileSize);
			StringBuilder sb = new StringBuilder();
			sb.append(this.eodFile.getName());
			sb.append(',');
			sb.append(this.fileSize);
			sb.append(',');
			sb.append(this.fileRecordCount);
			sb.append('\n');
			try {
				this.statWriter.write(sb.toString());
				this.statWriter.flush();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
	}

	private void schedulePushApi(String filename, long recordCount, long actualFileSize) {
		if (this.executor == null || this.statUrl == null)
			return;
		this.executor.execute(() -> {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("server_name", this.serverName);
			requestMap.put("file_name", filename);
			requestMap.put("file_record_count", recordCount);
			requestMap.put("file_size", actualFileSize);
			URL url;
			try {
				url = new URL(this.statUrl);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());) {
					wr.writeBytes(gson.toJson(requestMap));
					wr.flush();
				}
				int responseCode = con.getResponseCode();
				logger.info(() -> "ResponseCode: " + this.statUrl + " " + responseCode);
				con.disconnect();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		});
	}

	public Map<String, Long> getFileCountMap() {
		return this.fileNameCountMap;
	}

	public Map<String, Long> getFileSizeMap() {
		return this.fileNameSizeMap;
	}

	public Map<String, Object> getCurrentFileStat() {
		Map<String, Object> resultmap = new HashMap<>();
		resultmap.put("file_name", this.eodFile.getName());
		resultmap.put("record_count", this.fileRecordCount);
		resultmap.put("file_size", this.fileSize);
		return resultmap;
	}
}
