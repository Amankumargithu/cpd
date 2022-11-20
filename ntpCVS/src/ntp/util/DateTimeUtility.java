package ntp.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;

public class DateTimeUtility {
	private static DateTimeUtility utils = null;
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

	public static synchronized DateTimeUtility getDefaultInstance() {
		if (utils == null)
			utils = new DateTimeUtility();
		return utils;
	}

	private DateTimeUtility() {
	}

	public static void processDate(QTCPDMessageBean qtbean, long time) {
		if (qtbean == null)
			return;
		if (time == 0) {
			qtbean.setTradeDate(null);
			qtbean.setLastTradeTime(null);
			return;
		}
		LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		qtbean.setTradeDate(datetime.format(dateFormatter));
		qtbean.setLastTradeTime(datetime.format(timeFormatter));
		if (time > System.currentTimeMillis()) {
			NTPLogger.info("processDate " + qtbean.getTicker() + "," + qtbean.getTradeDate() + ","
					+ qtbean.getLastTradeTime());
		}
	}

	public static void processExtendedDate(QTCPDMessageBean qtbean, long time) {
		if (qtbean == null)
			return;
		if (time == 0) {
			qtbean.setExtendedTradeDate(null);
			qtbean.setExtendedLastTradeTime(null);
			return;
		}
		LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		qtbean.setExtendedTradeDate(datetime.format(dateFormatter));
		qtbean.setExtendedLastTradeTime(datetime.format(timeFormatter));
		if (time > System.currentTimeMillis()) {
			NTPLogger.info("processExtendedDate " + qtbean.getTicker() + "," + qtbean.getExtendedTradeDate() + ","
					+ qtbean.getExtendedLastTradeTime());
		}
	}
}