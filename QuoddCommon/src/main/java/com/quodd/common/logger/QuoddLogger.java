package com.quodd.common.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.util.QuoddProperty;

public final class QuoddLogger {
	private static final String LOGGER_NAME = "QuoddLogger";
	private static QuoddLogger instance = null;
	private Logger logger;

	private String logFileName = null;

	private QuoddLogger(String logFileName) {
		this.logger = Logger.getLogger(LOGGER_NAME);
		setLogFileName(logFileName);
	}

	/*
	 * Pass log file name as argument
	 */
	public static QuoddLogger getInstance(String logFileName) {
		if (instance == null) {
			instance = new QuoddLogger(logFileName);
			instance.initLogger();
		} else {
			instance.setLogFileName(logFileName);
		}
		return instance;
	}

	public static QuoddLogger getInstance() {
		if (instance == null) {
			instance = new QuoddLogger(null);
			instance.initLogger();
		}
		return instance;
	}

	private void initLogger() {
		String filePath;
		QuoddProperty logProperties = new QuoddProperty("/log.properties");
		String logL = logProperties.getStringProperty("LOG_LEVEL", "INFO");
		switch (logL) {
		case "ALL":
			this.logger.setLevel(Level.ALL);
			break;
		case "CONFIG":
			this.logger.setLevel(Level.CONFIG);
			break;
		case "FINE":
			this.logger.setLevel(Level.FINE);
			break;
		case "FINER":
			this.logger.setLevel(Level.FINER);
			break;
		case "FINEST":
			this.logger.setLevel(Level.FINEST);
			break;
		case "INFO":
			this.logger.setLevel(Level.INFO);
			break;
		case "OFF":
			this.logger.setLevel(Level.OFF);
			break;
		case "SEVERE":
			this.logger.setLevel(Level.SEVERE);
			break;
		case "WARNING":
			this.logger.setLevel(Level.WARNING);
			break;
		default:
			this.logger.setLevel(Level.INFO);
			break;
		}
		filePath = logProperties.getProperty("FILE_PATH");
		if (filePath == null) {

			this.logger.warning(() -> CommonLogMessage.missingProperty("FILE_PATH"));
			this.logger.info("Initialising console Logger as FILE_PATH is missing");

		} else {
			int fileCount = logProperties.getIntProperty("FILE_COUNT", 6);
			int fileSize = logProperties.getIntProperty("FILE_SIZE", 5 * 1024 * 1024);
			boolean isAppendable = logProperties.getBooleanProperty("IS_APPENDABLE", false);
			try {
				File pathFile = new File(filePath);
				if (!pathFile.exists()) {
					pathFile.mkdirs();
				}
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				filePath = filePath + '/' + this.logFileName + "_" + timeStamp + "_" + "%g.log";
				FileHandler fh = new FileHandler(filePath, fileSize, fileCount, isAppendable);
				String format = logProperties.getStringProperty("LOG_FORMAT",
						"[%1$tF %1$tT] [%2$s] %3$s %4$s %5$s  %6$s %n");
				fh.setFormatter(new CustomFormatter(format));
				fh.setLevel(this.logger.getLevel());
				this.logger.setUseParentHandlers(false);
				this.logger.addHandler(fh);
			} catch (Exception e) {
				this.logger.log(Level.WARNING, "Exception " + e.getMessage(), e);
			}
		}
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void setLogFileName(final String logFileName) {
		if (logFileName == null) {
			this.logFileName = "quoddLog";
		} else {
			this.logFileName = logFileName;
		}
	}
}