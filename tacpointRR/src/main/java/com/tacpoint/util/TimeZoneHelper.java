package com.tacpoint.util;

import java.sql.Date;
import java.text.*;
import java.util.*;

import com.tacpoint.util.TimeZoneConstants;

/**
 *Description of the Class
 *
 * @author     Tacpoint, Inc.
 * @created    December 17, 2001
 */
public class TimeZoneHelper {

    public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";

	/**
	 *Constructor for the TimeZoneHelper object
	 *
	 * @since
	 */
	public TimeZoneHelper() { }


	/**
	 *Gets the calendar attribute of the TimeZoneHelper class
	 *
	 * @param  inTimeZone  Description of Parameter
	 * @return             The calendar value
	 * @since
	 */
	public static Calendar getCalendar(int inTimeZone) {

		String mTimeZoneID = (String) (TimeZoneConstants.getTimeZoneHash()).get(new Integer(inTimeZone));

		TimeZone mTimeZone = TimeZone.getDefault();

		if (mTimeZoneID != null) {
			mTimeZone = TimeZone.getTimeZone(mTimeZoneID);
		}

		Calendar c = new GregorianCalendar(mTimeZone);

		return c;
	}


	/**
	 *Gets the sQLDate attribute of the TimeZoneHelper class
	 *
	 * @param  inTimeZone  Description of Parameter
	 * @return             The sQLDate value
	 * @since
	 */
	public static Date getSQLDate(int inTimeZone) {

		String mTimeZoneID = (String) (TimeZoneConstants.getTimeZoneHash()).get(new Integer(inTimeZone));

		TimeZone mTimeZone = TimeZone.getDefault();

		if (mTimeZoneID != null) {
			mTimeZone = TimeZone.getTimeZone(mTimeZoneID);
		}

		Calendar c = new GregorianCalendar(mTimeZone);
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY);
        f.setTimeZone(mTimeZone);
        String mDateString = f.format(c.getTime());
		java.util.Date ud = new java.util.Date();

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY);
        try
        {
           ud = mSimpleDateFormat.parse(mDateString);
        } catch (ParseException pe)
        {
        	System.out.println(pe.getMessage());
        }

        Date d = new java.sql.Date(ud.getTime());

		return d;
	}


	/**
	 *The main program for the TimeZoneHelper class
	 *
	 * @param  args  The command line arguments
	 * @since
	 */
	public static void main(String[] args) {

		if (args.length < 1) {

			System.out.println("\n\nWrong number of arguments!\n");
			System.out.println("Usage: java com.nexant.common.TimeZoneHelper <time zone id>\n");

			System.exit(0);
		}

		int zoneid = 0;

		try {
			zoneid = Integer.parseInt(args[0]);
		} catch (NumberFormatException ne) {
		}

		Calendar c = TimeZoneHelper.getCalendar(zoneid);

		System.out.println("-------------- Calendar begin -----------------------");
		System.out.println("ERA: " + c.get(Calendar.ERA));
		System.out.println("YEAR: " + c.get(Calendar.YEAR));
		System.out.println("MONTH: " + c.get(Calendar.MONTH));
		System.out.println("WEEK_OF_YEAR: " + c.get(Calendar.WEEK_OF_YEAR));
		System.out.println("WEEK_OF_MONTH: " + c.get(Calendar.WEEK_OF_MONTH));
		System.out.println("DATE: " + c.get(Calendar.DATE));
		System.out.println("DAY_OF_MONTH: " + c.get(Calendar.DAY_OF_MONTH));
		System.out.println("DAY_OF_YEAR: " + c.get(Calendar.DAY_OF_YEAR));
		System.out.println("DAY_OF_WEEK: " + c.get(Calendar.DAY_OF_WEEK));
		System.out.println("DAY_OF_WEEK_IN_MONTH: " + c.get(Calendar.DAY_OF_WEEK_IN_MONTH));
		System.out.println("AM_PM: " + c.get(Calendar.AM_PM));
		System.out.println("HOUR: " + c.get(Calendar.HOUR));
		System.out.println("HOUR_OF_DAY: " + c.get(Calendar.HOUR_OF_DAY));
		System.out.println("MINUTE: " + c.get(Calendar.MINUTE));
		System.out.println("SECOND: " + c.get(Calendar.SECOND));
		System.out.println("MILLISECOND: " + c.get(Calendar.MILLISECOND));
		System.out.println("-------------- Calendar end -------------------------");

		Date d = TimeZoneHelper.getSQLDate(zoneid);

		System.out.println("-------------- SQL date begin -----------------------");
		System.out.println(d.toString());
		System.out.println("-------------- SQL date end -------------------------");

	}

}

