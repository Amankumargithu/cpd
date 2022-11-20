package com.quodd.common.cpd.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface FormatterUtility {
	 SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z z");

	public static String formatDate(long timestamp) {
		Date d = new Date(timestamp);
		return format.format(d);
	}
}
