/**
 * TSQTableMapper.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2006.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.tsq;

import java.util.ArrayList;

/**
 * The object for associating ticker symbols with the respective table names.
 */
public class TSQTableMapper {

	public final static Integer DAY_0 = new Integer(0);

	public static ArrayList DAILY_TABLES = new ArrayList();

	static {
		DAILY_TABLES.add("TSQ_DAY_0_1");
		DAILY_TABLES.add("TSQ_DAY_0_2");
		DAILY_TABLES.add("TSQ_DAY_0_3");
		DAILY_TABLES.add("TSQ_DAY_0_4");
		DAILY_TABLES.add("TSQ_DAY_0_5");
		DAILY_TABLES.add("TSQ_DAY_0_6");
		DAILY_TABLES.add("TSQ_DAY_0_7");
		DAILY_TABLES.add("TSQ_DAY_0_8");
		DAILY_TABLES.add("TSQ_DAY_0_9");
		DAILY_TABLES.add("TSQ_DAY_0_10");
		DAILY_TABLES.add("TSQ_DAY_0_11");

	}

	public static String findCancelledTableName(Integer day) {
		Integer absDay = new Integer(day.intValue() * -1);
		return "TSQ_DAY_" + absDay.toString() + "_X";
	}

	public static String findTableName(String ticker, Integer day) {
		return mapTickerToDailyTable(ticker, day);
	}

	private static String mapTickerToDailyTable(String ticker, Integer day) {

		Integer absDay = new Integer(day.intValue() * -1);

		byte[] bytes = ticker.getBytes();

		if (bytes[0] < 66)
			return "TSQ_DAY_" + absDay.toString() + "_1";
		if (bytes[0] < 67)
			return "TSQ_DAY_" + absDay.toString() + "_2";
		if (bytes[0] < 68)
			return "TSQ_DAY_" + absDay.toString() + "_3";
		if (bytes[0] < 70)
			return "TSQ_DAY_" + absDay.toString() + "_4";
		if (bytes[0] < 72)
			return "TSQ_DAY_" + absDay.toString() + "_5";
		if (bytes[0] < 75)
			return "TSQ_DAY_" + absDay.toString() + "_6";
		if (bytes[0] < 77)
			return "TSQ_DAY_" + absDay.toString() + "_7";
		if (bytes[0] < 80)
			return "TSQ_DAY_" + absDay.toString() + "_8";
		if (bytes[0] < 83)
			return "TSQ_DAY_" + absDay.toString() + "_9";
		if (bytes[0] < 86)
			return "TSQ_DAY_" + absDay.toString() + "_10";

		return "TSQ_DAY_" + absDay.toString() + "_11";

	}

}
