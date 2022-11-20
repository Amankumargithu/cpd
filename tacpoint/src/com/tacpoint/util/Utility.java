 /**
  * Utility.java
  *    Utility class for all packages.
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Ming Lau
  * @author mlau@tacpoint.com
  * @version 1.0
  * Date created: 1/27/2000
  * Date modified:
  * - 1/27/2000 Initial version
  */
 package com.tacpoint.util;

 import java.util.*;
 import java.text.*;

 import java.io.*;

import com.tacpoint.dataconnection.DBConnectionManager;
import java.sql.*;

 public final class Utility
 {

    public static final String DATE_PATTERN_DDMMMYYYYHHMISS = "dd-MMM-yyyy:HH:mm:ss";
    public static final String DATE_PATTERN_DDMMMYYYY = "dd-MMM-yyyy";
    private static final String NUMBER_PATTERN_VOLUME = "#,###,###,##0";
    private static final String NUMBER_PATTERN_PRICE = "#,###,##0.00";
    private static final String NUMBER_PATTERN_PERCENT_PRICE = "#,###,##0.00";
    private static final String NUMBER_PATTERN_THREE_DECIMAL = "#,###,##0.000";
    private static final String NUMBER_PATTERN_FOUR_DECIMAL = "#,###,##0.0000";
    private static final String NUMBER_PATTERN_PERCENT = "#,##0.00%";
    private static final String TIME_PATTERN_HHMMAMPM = "h:mm a";
    private static final long ONE_KB = 1024;
    private static final long ONE_MB = 1024 * 1024;


    /**
     *
     *  getProperties
     *
     *    get Properties from config.properties file.  The file
     *    has to be located at the class path
     *
     *    @param Class sourceClass the Class of the source object
     *    @return Properties the Properties object loaded by the file.
     *
     */

    public static Properties getProperties(Class sourceClass, String fileName)
    {

        if (fileName == null)
        {
            return null;
        }

        Properties configProperties = new Properties();

        InputStream inputFile = sourceClass.getResourceAsStream(fileName);


        if (inputFile != null) {
            try {
                configProperties.load(inputFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return configProperties;
        }



        if (inputFile == null) {
            try {
                inputFile = new FileInputStream(fileName);
             } catch (Exception ex) {
                System.err.println("WARNING: Cannot open " + fileName + " file");
            }
        }

        return null;
    }



  /**
   * Convert an object to a byte array
   * Java serialization is used, so take care to note that transient
   * and static members will <b>not</b> be put into the new array.
   *
   * @param	anObject	the object to convert
   * @return	a byte array representation of the object
   */
  public static byte[] objectToByteArray(Object anObject) {
        byte[] returnArray = null;
        if (anObject == null) return null;

        try {
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream);
            objectStream.writeObject(anObject);
            objectStream.close();
            byteArrayStream.close();
            returnArray = byteArrayStream.toByteArray();

            } catch (Exception ex) {
                ex.printStackTrace();
        }
        return returnArray;
  }

  /**
   * Convert a byte array to an object.
   * Java serialization is used, so take care to note that transient
   * and static members will <b>not</b> be put into the new object.
   *
   * @param	aByteArray	the byte array to convert
   * @return	a object representation of the byte array
   */
  public static Object byteArrayToObject(byte[] aByteArray) {
        Object objectRead = null;
        if (aByteArray == null) return null;

        try {
            ByteArrayInputStream byteArrayStream =
            new ByteArrayInputStream(aByteArray);
            ObjectInputStream objectStream = new ObjectInputStream(byteArrayStream);
            objectRead = objectStream.readObject();
            objectStream.close();
            byteArrayStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
            return objectRead;
    }



  /**
   * Get time in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getTimeAsPattern(String inPattern, java.util.Date inDate)
  {

        String aPattern = null;

        if (inPattern == null)
        {
            inPattern = Utility.DATE_PATTERN_DDMMMYYYY;
        }

        aPattern = inPattern;

        SimpleDateFormat aFormatter = new SimpleDateFormat(aPattern);
        return (aFormatter.format(inDate));


  }




  /**
   * Get time in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getCurrentTimeAsString(String inPattern) {


        GregorianCalendar aCalendar = new GregorianCalendar();
        String aPattern = null;

        if (inPattern == null)
        {
            aPattern = Utility.DATE_PATTERN_DDMMMYYYY;
        }
        else
            aPattern = inPattern;


        return(Utility.getTimeAsPattern(aPattern, aCalendar.getTime()));


    }


  /**
   * Get time in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getMarketCloseTimeAsString() {


        GregorianCalendar aCalendar = new GregorianCalendar();

        SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);
        String datesection = aFormatter.format(aCalendar.getTime());
        String timeString = datesection + ":16:00:00";
        return (timeString);


  }


  /**
   * Get time in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getDayBeginTimeAsString() {


        GregorianCalendar aCalendar = new GregorianCalendar();

        SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);
        String datesection = aFormatter.format(aCalendar.getTime());
        String timeString = datesection + ":00:00:00";
        return (timeString);


  }


  /**
   * Get time in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getDayEndTimeAsString() {


        GregorianCalendar aCalendar = new GregorianCalendar();

        SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);
        String datesection = aFormatter.format(aCalendar.getTime());
        String timeString = datesection + ":23:59:59";
        return (timeString);


  }


  /**
   * Get time in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getNextDayAsString() {


        GregorianCalendar aCalendar = new GregorianCalendar();

        aCalendar.add(Calendar.DATE, 1);

        SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);

        String datesection = aFormatter.format(aCalendar.getTime());
        String timeString = datesection + ":00:00:00";
        return (timeString);


  }

  /**
     * Get time in DD-MM-YYYY HH:MI:SS format as String
     *
     * @param     an input pattern.  If it is null, the method will default.
     * @return	a object representation of the byte array
     */
    public static String getDayFromTodayAsString(int numDay) {


          GregorianCalendar aCalendar = new GregorianCalendar();

          aCalendar.add(Calendar.DATE, numDay);

          SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);

          String datesection = aFormatter.format(aCalendar.getTime());
          String timeString = datesection + ":00:00:00";
          return (timeString);


  }

  /**
   * Get first date of the week in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getFirstDateOfWeekAsString(GregorianCalendar aCalendar) {

        boolean doLog=Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();

        SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);

        int firstdateofweek = aCalendar.getMinimum(Calendar.DAY_OF_WEEK);
        //Logger.debug("Utility: first date of week: "+firstdateofweek,doLog);

        int datediff = firstdateofweek - aCalendar.get(Calendar.DAY_OF_WEEK);
        //Logger.debug("Utility: datediff: "+datediff,doLog);

        aCalendar.add(Calendar.DATE, datediff);

        String datesection = aFormatter.format(aCalendar.getTime());
        String timeString = datesection + ":00:00:00";
        return (timeString);


  }


  /**
   * Get last date of the week in DD-MM-YYYY HH:MI:SS format as String
   *
   * @param     an input pattern.  If it is null, the method will default.
   * @return	a object representation of the byte array
   */
  public static String getLastDateOfWeekAsString(GregorianCalendar aCalendar) {


        boolean doLog=Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();

        SimpleDateFormat aFormatter = new SimpleDateFormat(Utility.DATE_PATTERN_DDMMMYYYY);

        int lastdateofweek = aCalendar.getMaximum(Calendar.DAY_OF_WEEK);
        //Logger.debug("Utility: Last date of week "+lastdateofweek,doLog);

        int datediff = lastdateofweek - aCalendar.get(Calendar.DAY_OF_WEEK);
        //Logger.debug("Utility: datediff "+datediff,doLog);

        aCalendar.add(Calendar.DATE, datediff);

        String datesection = aFormatter.format(aCalendar.getTime());
        String timeString = datesection + ":23:59:59";
        return (timeString);


  }

    /**
     *
     *  formatDate
     *
     *    formats a java.util.Date object into a gregorian String.
     *
     *    @param Date the Date to be formatted.
     *    @return String the gregorian formatted date (mm/dd/yyyy)
     *
     */

    public static String formatDate(java.util.Date date)
    {
       SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
       if (date!=null)
          return formatter.format(date);
       return "";
    }


    /**
     *
     *  formatVolume
     *
     *    formats a String object into a formated string
     *
     *    @param String the string to be formatted.
     *    @return String the string formatted in 9,999,999,999
     *
     */
    public static String formatVolume(String inVolume)
    {
        DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_VOLUME);

        if (inVolume == null)
           return "";

        try
        {
            return df.format(Long.parseLong(inVolume.trim()));

        } catch (NumberFormatException ne)
        {
            return inVolume;
        }

    }


    /**
     *
     *  formatPrice
     *
     *    formats a String object into a formated price string.
     *
     *    @param String the String to be formatted.
     *    @return String the string formatted as 9,999,999.99
     *
     */
    public static String formatPrice(String inPrice)
    {
        DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_PRICE);

        if (inPrice == null)
           return "";

        try
        {
            return df.format(Float.parseFloat(inPrice.trim()));

        } catch (NumberFormatException ne)
        {
            return inPrice;
        }

    }
    
    /**
    *
    *  formatPrice
    *
    *    formats a String object into a formated price string.
    *
    *    @param String the String to be formatted.
    *    @return String the string formatted as 9,999,999.99
    *
    */
   public static String formatPricePercent(String inPrice)
   {
       DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_PERCENT_PRICE);

       if (inPrice == null)
          return "";

       try
       {
           return df.format(Float.parseFloat(inPrice.trim())*100);

       } catch (NumberFormatException ne)
       {
           return inPrice;
       }

   }
    /**
     *
     *
     *  parseThreeDecimal
     *
     *    formats a String object into a 3 decimal floating String
     *
     *    @param String the String to be formatted.
     *    @return String the String formatted as 9.999.999.999
     *
     */
    public static String parseThreeDecimal(String inString)
    {
		DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_THREE_DECIMAL);

		if (inString == null)
		return "";

		try
		{
		    return df.format(Float.parseFloat(inString.trim()));

		} catch (NumberFormatException ne)
		{
		   return inString;
        }
	}
    /**
     *
     *
     *  parseThreeDecimal
     *
     *    formats a String object into a 3 decimal floating String
     *
     *    @param String the String to be formatted.
     *    @return String the String formatted as 9.999.999.999
     *
     */
    public static String parseFourDecimal(String inString)
    {
		DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_FOUR_DECIMAL);

		if (inString == null)
		return "";

		try
		{
		    return df.format(Double.parseDouble(inString.trim()));

		} catch (NumberFormatException ne)
		{
		   return inString;
        }
	}
	public static String reduceFraction(int top, int bottom)
	{
		float tempTop, tempBottom;
		String result;
		while ((top%2) == 0 && (bottom%2) == 0)
		{
		    tempTop = top/2;
		    top = (int) tempTop;
		    tempBottom = bottom/2;
		    bottom = (int) tempBottom;
		}
		result = top + "/" + bottom;
		return result;
	}

	public static String parseDecToFrac(String inString)
	{
		String intString;
		String decString;
		String topString;
		String bottomString = "64";
		String fraction = parseThreeDecimal(inString);
		String result;
		int top;
		int bottom = 64;
		int temp;
		float tempFloat;


		if (fraction.equals("N/A"))
		{
			return inString;
	    }
	    else
	    {
			intString = fraction.substring(0, fraction.indexOf('.'));
			if (intString == null)
			   intString = "";
			decString = fraction.substring(fraction.indexOf('.') + 1);
			temp = Integer.parseInt(decString);
			tempFloat = (temp * 10) / 156;
			Float temFloat = new Float(tempFloat);
			top = temFloat.intValue();
			if (top == 0)
			{
			   return intString;
			}
			else
			{
				if (intString.equals("0"))
				  result = reduceFraction(top, bottom);
				else
				  result = intString + " " +  reduceFraction(top, bottom);
				return result;
			}


		}

	}



    /**
     *
     *  formatPrice
     *
     *    formats a double into a formated price string.
     *
     *    @param String the String to be formatted.
     *    @return String the string formatted as 9,999,999.99
     *
     */
    public static String formatPrice(double inPrice)
    {
        return (formatDecimal(inPrice));

    }



    /**
     *
     *  formatPercent
     *
     *    formats a String object into a formated percent string.
     *
     *    @param String the String to be formatted.
     *    @return String the string formatted as 999.99%
     *
     */
    public static String formatPercent(String inPercent)
    {
        DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_PERCENT);

        if (inPercent == null)
           return "";

        try
        {
            return df.format(Float.parseFloat(inPercent.trim()));

        } catch (NumberFormatException ne)
        {
            return inPercent;
        }

    }


    public static String getDateTime(String inDateTime)
    {
            if (inDateTime != null)
            {
                String tempDate = inDateTime.trim();
                if (tempDate.length() == 20)
                {
                    String time_segment = tempDate.substring(11);
                    if (time_segment.equals(":00:00:00"))
                    {
                        return (tempDate.substring(0, 11));
                    }
                }

            }

            return inDateTime;


    }


    public static String transposeSpecialHtmlChars(String inText)
    {

        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(inText,"\"");
        while (st.hasMoreTokens()) {
           sb.append(st.nextToken());
           if (st.hasMoreTokens())
              sb.append("&quot");
        }

        return sb.toString();
    }

    public static String getDateOnly(String inDateTime)
    {
        if (inDateTime != null)
        {
            String tempDate = inDateTime.trim();
            if (tempDate.length() > 8)
            {
                int first_colon_pos = tempDate.indexOf(":");
                if (first_colon_pos < 0 ) { return inDateTime; }
                return(tempDate.substring(0, first_colon_pos));
            }
        }

        return inDateTime;

    }

     /**
     *
     *  formatTime
     *
     *    formats a java.util.Date object into a String.
     *
     *    @param Date the Date to be formatted.
     *    @return String the formatted time (h:mm a) e.g. 1:15 PM
     *
     */

    public static String formatTime(java.util.Date date)
    {
       SimpleDateFormat formatter = new SimpleDateFormat(TIME_PATTERN_HHMMAMPM);
       return formatter.format(date);
    }

   // get the connection and turn off the auto commit.
   // to use this method, needs to get DBConnectionManager first by calling
   //      DBConnectionManager connMgr = DBConnectionManager.getInstance();
   public static Connection getConnection(DBConnectionManager connMgr)
   {
      try
      {
          Connection c=connMgr.getConnection(Constants.DB_POOL,Constants.CONNECTION_WAIT_TIME);
          c.setAutoCommit(false);
          return c;
      } catch (Exception e)
      {
          Logger.log("Utility.getConnection has error. ", e);
          return null;
      }

   }

   // free the connection and turn on auto commit.
   public static void freeConnection(DBConnectionManager connMgr, Connection c)
   {
      try
      {
         c.setAutoCommit(true);
         connMgr.freeConnection(Constants.DB_POOL,c);
      } catch (Exception e)
      {
        Logger.log("Utility.freeConnection has error. ", e);
      }

   }

   // format double to two decimal point string


    /**
     *
     *  formatDecimal
     *
     *    formats a double into a formated price string.
     *
     *    @param String the String to be formatted.
     *    @return String the string formatted as 9,999,999.99
     *
     */
    public static String formatDecimal(double inDecimal)
    {
        DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_PRICE);


        try
        {
            return df.format(inDecimal);

        } catch (NumberFormatException ne)
        {
            return String.valueOf(inDecimal);
        }

    }


    /**
     *
     *  formatNumber
     *
     *    formats a long into a formated number string.
     *
     *    @param String the String to be formatted.
     *    @return String the string formatted as 9,999,999.99
     *
     */
    public static String formatNumber(long inNumber)
    {
        DecimalFormat df = new DecimalFormat(NUMBER_PATTERN_VOLUME);

        try
        {
            return df.format(inNumber);

        } catch (NumberFormatException ne)
        {
            return String.valueOf(inNumber);
        }

    }



    /**
     *
     *  getFileSize()
     *
     *    get a file size in kb of mb depends on the file size.
     *
     *    @param inSize the size to be formatted.
     *    @return String the string formatted as 9,999,999.99 kb or mb
     *
     */
    public static String getFileSize(long inSize)
    {
        String filesize_unit = " KB";
        double filesize = 0.0;
        if (inSize < ONE_KB)
        {
            filesize_unit = " B";
            return(formatNumber(inSize)+filesize_unit);
        }
        else
        if (inSize < ONE_MB)
        {
            filesize = ((double)inSize * 1.0) / ONE_KB;
        }
        else
        {
            filesize_unit = " MB";
            filesize = ((double)inSize * 1.0) / ONE_MB;
        }

        return(formatDecimal(filesize)+filesize_unit);

    }


    /**
     *
     *  getCurrentDateAsSQLDate()
     *
     *    get current date as a sql date.
     *
     *
     *    @return sql date
     *
     */
    public static java.sql.Date getCurrentDateAsSQLDate()
    {
         GregorianCalendar aCalendar = new GregorianCalendar();
         java.util.Date adate = aCalendar.getTime();
         return(new java.sql.Date(adate.getTime()));
    }


 }

