package com.b4utrade.helper;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.tacpoint.util.Utility;

public class NewsDateComparator implements Comparator<StockNewsUpdateHelper>{

	SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYYHHMISS);
	@Override
	public int compare(StockNewsUpdateHelper o1, StockNewsUpdateHelper o2) {
		String date1 = o1.getLastNewsDate();
		String date2 = o2.getLastNewsDate();
		try
		{
		Date d1 = aFormatter.parse(date1);
		Date d2 = aFormatter.parse(date2);
		if(d1.after(d2))
			return -1;
		else if(d1.before(d2))
			return 1;
		}
		catch(Exception e){}
		return 0;
	}
}
