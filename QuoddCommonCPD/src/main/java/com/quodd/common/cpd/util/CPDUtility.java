package com.quodd.common.cpd.util;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

import QuoddFeed.msg.QuoddMsg;

public interface CPDUtility {

	final long msinday = 86_400_000;// milliseconds in a day
	final long timdif = TimeZone.getTimeZone("America/New_York").getOffset(Calendar.getInstance().getTimeInMillis());
	Logger logger = QuoddLogger.getInstance().getLogger();
	DecimalFormat df = new DecimalFormat("0.0000");
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

	static String getMappedExchangeCode(String exchange) {
		if (exchange.equals("ARCX"))
			return "NYSE ARCA";
		else if (exchange.equals("XASE"))
			return "NYSE MKT";
		else if (exchange.equals("XNYS"))
			return "NYSE";
		else if (exchange.equals("XOTC"))
			return "OTCBB";
		else if (exchange.equals("XTSE"))
			return "Toronto";
		else if (exchange.equals("XTSX"))
			return "TSX Venture";
		else if (exchange.equals("XNAS"))
			return "NASDAQ";
		else if (exchange.equals(""))
			return " ";
		else if (exchange.equals("????"))
			return " ";
		return exchange;
	}

	static String formatLimitUpDown(String limitUpDown) {
		if (limitUpDown == null || limitUpDown.length() < 1)
			return " , ";
		char limitChar = limitUpDown.charAt(0);
		switch (limitChar) {
		case 'A':
			return " , ";
		case 'B':
			return "N, ";
		case 'C':
			return " ,N";
		case 'D':
			return "N,N";
		case 'E':
			return "L, ";
		case 'F':
			return " ,L";
		case 'G':
			return "L,N";
		case 'H':
			return "N,L";
		case 'I':
			return "L,L";
		default:
			return " , ";
		}
	}

	static Integer getFormattedTime(long millis) {
		int[] time = QuoddMsg.timeHMSms(millis);
		String strTime = String.format("%02d%02d%02d%03d", time[0], time[1], time[2], time[3]);
		Integer intTime = 99_999_999;
		try {
			intTime = Integer.parseInt(strTime);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return intTime;
	}

	static void parseDateLong(StringBuilder sb, long ms, boolean timeDiff) {
		long hh = 0;
		long mm = 0;
		long ss = 0;
		if (timeDiff)
			ms = ms + timdif;// changing GMT to Local Time
		ms = ms % msinday;// Finding total milliseconds of current day //passed till now
		hh = ms / (1000 * 60 * 60);
		ms = ms % (1000 * 60 * 60);
		mm = ms / (1000 * 60);
		ms = ms % (1000 * 60);
		ss = ms / 1000;
		ms = ms % 1000;
		if (hh < 10)
			sb.append("0");
		sb.append(hh);
		if (mm < 10)
			sb.append("0");
		sb.append(mm);
		if (ss < 10)
			sb.append("0");
		sb.append(ss);
		if (ms < 10)
			sb.append("00");
		else if (ms < 100)
			sb.append("0");
		sb.append(ms);
	}

	static void lPadZero(StringBuilder sb, long in, int fill) {
		boolean negative = false;
		long value;
		long len = 0;
		if (in >= 0) {
			value = in;
		} else {
			negative = true;
			value = -in;
			in = -in;
			len++;
		}
		if (value == 0) {
			len = 1;
		} else {
			for (; value != 0; len++) {
				value /= 10;
			}
		}
		if (negative) {
			sb.append('-');
		}
		if (fill < len)
			logger.severe("Length Exceed " + in + " " + fill);
		for (int i = fill; i > len; i--) {
			sb.append('0');
		}
		sb.append(in);
	}

	static void doublePadZero(StringBuilder builder, double d, int padding) {
		if (d < 0) {
			builder.append("-");
			d = d * (-1);
			padding--;
		}
		long scaled = (long) (d * 1e4 + 0.5);
		long factor = 10_000;
		int scale = 5;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if (scale < padding) {
				builder.append('0');
				padding--;
			} else {
				long c = scaled / factor % 10;
				factor /= 10;
				builder.append((char) ('0' + c));
				scale--;
				padding--;
			}
		}
	}

	static void stringPadLeftSpace(StringBuilder builder, String string, int length) {
		int size = string.length();
		if (size > length)
			logger.severe("Length Exceed " + string + " " + length);
		while (size < length) {
			builder.append(" ");
			length--;
		}
		builder.append(string);
	}

	static void stringPadRightSpace(StringBuilder builder, String string, int length) {
		builder.append(string);
		int size = string.length();
		while (size < length) {
			builder.append(" ");
			length--;
		}
	}

	static void stringPadZero(StringBuilder builder, String string, int length) {
		int size = string.length();
		while (size < length) {
			builder.append("0");
			length--;
		}
		builder.append(string);
	}

	static void appendTo4(StringBuilder builder, double d) {
		if (d < 0) {
			builder.append("-");
			d = d * (-1);
		}
		long scaled = (long) (d * 1e4 + 0.5);
		long factor = 10_000;
		int scale = 5;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if (scale == 4)
				builder.append('.');
			long c = scaled / factor % 10;
			factor /= 10;
			builder.append((char) ('0' + c));
			scale--;
		}
	}

	static String appendTo6(double d) {
		StringBuilder builder = new StringBuilder();
		if (d < 0) {
			builder.append("-");
			d = d * (-1);
		}
		long scaled = (long) (d * 1e6 + 0.5);
		long factor = 1_000_000;
		int scale = 7;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if (scale == 6)
				builder.append('.');
			long c = scaled / factor % 10;
			factor /= 10;
			builder.append((char) ('0' + c));
			scale--;
		}
		return builder.toString();
	}

	static long changeDoubletoLong(double input) {
		Double value = input * 10_000;
		return value.longValue();
	}

	static String formatFourDecimals(double l) {
		String value = " ";
		try {
			value = df.format(l);
		} catch (Exception e) {
			return value;
		}
		return value;
	}

	static String processDate(Object t) {
		if (t == null) {
			return " ";
		}
		Long time = (Long) t;
		if (time == 0) {
			return " ";
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(dateFormatter);
	}

	static String processTime(Object t) {
		if (t == null) {
			return " ";
		}
		Long time = (Long) t;
		if (time == 0) {
			return " ";
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(timeFormatter);
	}
}
