package com.quodd.equityplus.logging;

import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.Date;

public class ConsoleLogger {

	private static final int LOG_LEVEL_DEBUG = 0;
	private static final int LOG_LEVEL_INFO = 1;
	private static final int LOG_LEVEL_WARNING = 2;
	private static final int LOG_LEVEL_ERROR = 3;

	private static int ALERT_LOG_LEVEL = 1;
	private static int SNAP_LOG_LEVEL = 1;
	private static int GENERAL_LOG_LEVEL = 1;

	public static final String INFO = "INFO";
	public static final String WARNING = "WARNING";
	public static final String ERROR = "ERROR";
	public static final String DEBUG = "DEBUG";
	private static final String CATEGORY_STRING_ALERT = "ALERT";
	private static final String CATEGORY_STRING_GENERAL = "GENERAL";
	private static final String CATEGORY_STRING_SNAP = "SNAP";

	public static final int CATEGORY_GENERAL = 1;
	public static final int CATEGORY_ALERT = 2;
	public static final int CATEGORY_SNAP = 3;

	public static void setALERT_LOG_LEVEL(int aLERT_LOG_LEVEL) {
		ALERT_LOG_LEVEL = aLERT_LOG_LEVEL;
	}

	public static void setSNAP_LOG_LEVEL(int sNAP_LOG_LEVEL) {
		SNAP_LOG_LEVEL = sNAP_LOG_LEVEL;
	}

	public static void setGENERAL_LOG_LEVEL(int gENERAL_LOG_LEVEL) {
		GENERAL_LOG_LEVEL = gENERAL_LOG_LEVEL;
	}

	// private static int level = LOG_LEVEL_ERROR;
//	private static Timestamp timestamp = new Timestamp(new Date().getTime());

	private static boolean logToFile = false;
	private static FileWriter logFile = null;

	public static void setLogToFile(boolean b) {
		logToFile = b;
	}

	public static void setLogFile(FileWriter f) {
		logFile = f;
	}

	public static void info(String text, int category) {

		if ((category == CATEGORY_GENERAL && GENERAL_LOG_LEVEL <= LOG_LEVEL_INFO)
				|| (category == CATEGORY_ALERT && ALERT_LOG_LEVEL <= LOG_LEVEL_INFO)
				|| (category == CATEGORY_SNAP && SNAP_LOG_LEVEL <= LOG_LEVEL_INFO)) {
			processInfoLog(category, text);
		}

	}

	public static void warning(String text, int category) {
		if ((category == CATEGORY_GENERAL && GENERAL_LOG_LEVEL <= LOG_LEVEL_WARNING)
				|| (category == CATEGORY_ALERT && ALERT_LOG_LEVEL <= LOG_LEVEL_WARNING)
				|| (category == CATEGORY_SNAP && SNAP_LOG_LEVEL <= LOG_LEVEL_WARNING)) {
			processWarningLog(category, text);
		}

	}

	public static void error(String text, int category) {
		if ((category == CATEGORY_GENERAL && GENERAL_LOG_LEVEL <= LOG_LEVEL_ERROR)
				|| (category == CATEGORY_ALERT && ALERT_LOG_LEVEL <= LOG_LEVEL_ERROR)
				|| (category == CATEGORY_SNAP && SNAP_LOG_LEVEL <= LOG_LEVEL_ERROR)) {
			processErrorLog(category, text);
		}
	}

	public static void debug(String text, int category) {
		if ((category == CATEGORY_GENERAL && GENERAL_LOG_LEVEL <= LOG_LEVEL_DEBUG)
				|| (category == CATEGORY_ALERT && ALERT_LOG_LEVEL <= LOG_LEVEL_DEBUG)
				|| (category == CATEGORY_SNAP && SNAP_LOG_LEVEL <= LOG_LEVEL_DEBUG)) {
			processDebugLog(category, text);
		}
	}

	private static String getCategoryString(int category) {
		switch (category) {
		case CATEGORY_GENERAL:
			return CATEGORY_STRING_GENERAL;
		case CATEGORY_ALERT:
			return CATEGORY_STRING_ALERT;
		case CATEGORY_SNAP:
			return CATEGORY_STRING_SNAP;
		default:
			return CATEGORY_STRING_GENERAL;
		}
	}

	private static void writeLogToFile(String log) {
		try {
			logFile.write(log);
			logFile.write("\r\n");
			logFile.flush();
		} catch (Exception ex) {
		}
	}

	private static String processInfoLog(int category, String msg) {
		String catgry = getCategoryString(category);
		String log = "[" + catgry + "]  " + new Timestamp(new Date().getTime()) + "  [" + INFO + "]: "
				+ msg;
		System.out.println(log);
		if ((logToFile) && (logFile != null)) {
			writeLogToFile(log);
		}
		return log;

	}
	
	private static String processWarningLog(int category, String msg) {
		String catgry = getCategoryString(category);
		String log = "[" + catgry + "]  " + new Timestamp(new Date().getTime()) + "  [" + WARNING + "]: "
				+ msg;
		System.out.println(log);
		if ((logToFile) && (logFile != null)) {
			writeLogToFile(log);
		}
		return log;

	}

	private static String processErrorLog(int category, String msg) {
		String catgry = getCategoryString(category);
		String log = "[" + catgry + "]  " + new Timestamp(new Date().getTime()) + "  [" + ERROR + "]: "
				+ msg;
		System.out.println(log);
		if ((logToFile) && (logFile != null)) {
			writeLogToFile(log);
		}
		return log;

	}

	private static String processDebugLog(int category, String msg) {
		String catgry = getCategoryString(category);
		String log = "[" + catgry + "]  " + new Timestamp(new Date().getTime()) + "  [" + DEBUG + "]: "
				+ msg;
		System.out.println(log);
		if ((logToFile) && (logFile != null)) {
			writeLogToFile(log);
		}
		return log;

	}

}
