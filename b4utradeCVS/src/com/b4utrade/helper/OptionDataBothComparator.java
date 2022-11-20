package com.b4utrade.helper;

import java.util.Calendar;
import java.util.Comparator;

import com.b4utrade.bean.StockOptionBean;

public class OptionDataBothComparator implements Comparator  
{

	private static final String[] monthArray = { "January","February","March","April","May","June","July","August","September","October","November","December" };
	public int compare(Object o1, Object o2)  {

		try
		{
			// ask data should be in ascending order...

			StockOptionBean b1 = (StockOptionBean)o1;
			StockOptionBean b2 = (StockOptionBean)o2;

			if (b1==null && b2==null) return 0;
			if (b1==null) return -1;
			if (b2==null) return 1;

			if(b1.getExpirationDate() == null && b2.getExpirationDate() == null) return 0;
			if(b1.getExpirationDate() == null) return -1;
			if(b2.getExpirationDate() == null) return 1;
			/*		     int type1 = b1.getOptionType();
		     int type2 = b2.getOptionType();

		     if (type1 > type2)
		        return 1;
		     else
		     if (type1 < type2)
		        return -1;
		     else{
			 */		     {  
				 // compare expired date

				 String b1StringDate = convertDateToDisplayString (b1.getExpirationDate());
				 String b2StringDate = convertDateToDisplayString (b2.getExpirationDate());
				 if (b1StringDate.equalsIgnoreCase(b2StringDate)) // case for Normal,Weekly and Quartly options in this day of expiration differ within the months
				 {
					 return Double.compare(b1.getStrikePrice(), b2.getStrikePrice());
					 /*if (b1.getStrikePrice() < b2.getStrikePrice())
						 return -1;
					 else
						 return 1;*/
				 }
				 if ((b1.getExpirationDate()).after(b2.getExpirationDate()))
					 return 1;
				 else
					 if ((b2.getExpirationDate()).after(b1.getExpirationDate()))
						 return -1;
					 else{
						 //compare price
						 /*if (b1.getStrikePrice() < b2.getStrikePrice())
							 return -1;
						 else
							 return 1;*/
						 return Double.compare(b1.getStrikePrice(), b2.getStrikePrice());
					 }
			 }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}

	}

	/**
	 * Format the given Calendar
	 *
	 * @param cal  Calendar to be formatted.
	 * @return Formatted Calendar date String.
	 */
	private String convertDateToDisplayString(Calendar cal)
	{

		if (cal == null) 
		{
			//	    	  log.info("Cal passed was null");
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(monthArray[cal.get(Calendar.MONTH)]);
		sb.append(" ");
		sb.append(cal.get(Calendar.YEAR));

		return(sb.toString());
	}

}
