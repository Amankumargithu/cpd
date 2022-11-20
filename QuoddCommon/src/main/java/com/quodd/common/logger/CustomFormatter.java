package com.quodd.common.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomFormatter extends SimpleFormatter {

	// "[%1$tF %1$tT] [%2$s] %3$s %4$s %5$s %6$s %n";
	private final String logFormat;

	public CustomFormatter(String format) {
		this.logFormat = format;
	}

	@Override
	public synchronized String format(LogRecord logRecord) {
		String throwable = "";
		if (logRecord.getThrown() != null) {
			StringWriter sw = new StringWriter();
			try (PrintWriter pw = new PrintWriter(sw);) {
				pw.println();
				logRecord.getThrown().printStackTrace(pw);
			}
			throwable = sw.toString();
		}
		return String.format(this.logFormat, new Date(logRecord.getMillis()), logRecord.getLevel().getLocalizedName(),
				logRecord.getSourceClassName(), logRecord.getSourceMethodName(), formatMessage(logRecord), throwable);
	}
}