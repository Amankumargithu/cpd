package com.tacpoint.date;

/** Date.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  3/1/2000
*/

import com.tacpoint.util.*;

/**
 * Date formatting, or conversion class.
 */
public class Date
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A   M E M B E R S
   ////////////////////////////////////////////////////////////////////////////


   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////

   /** Constructor:
   *
   * @exception Exception Passes exception encountered up to the calling
   *                 method.
   */
   public Date() throws Exception
   {
      Logger.init();

      // Delete when testing is done!
      Logger.log("Date constructor.");

   }

   /**
   *  Formats a date to DD-MON-YYYY:HH:MM:SS
   *
   *  @return String   a formatted date.
   */
   public static String formatToDBDate(int aMonth, int aDay, int aYear,
                                 int aHour, int aMinute, int aSecond)
   {
      if (aMonth < 1 || aMonth > 12)
         return new String();
      if (aDay < 1 || aDay > 31)
         return new String();
      if (aYear < 100)
      {
         if (aYear < 0)
            return new String();
      }
      else
      {
         if (aYear < 1970)
            return new String();
      }
      if (aHour < 0 || aHour > 23)
         return new String();
      if (aMinute < 0 || aMinute > 59)
         return new String();
      if (aSecond < 0 || aSecond > 59)
         return new String();

      String vYear = null;
      if (aYear < 100)
      {
         if (aYear < 80)
         {
            if (aYear < 10)
               vYear = "200" + aYear;
            else
               vYear = "20" + aYear;
         }
         else
            vYear = "19" + aYear;
      }
      else
         vYear = String.valueOf(aYear);

      String vShortMonth = null;
      switch(aMonth)
      {
         case 1: vShortMonth = "JAN"; break;
         case 2: vShortMonth = "FEB"; break;
         case 3: vShortMonth = "MAR"; break;
         case 4: vShortMonth = "APR"; break;
         case 5: vShortMonth = "MAY"; break;
         case 6: vShortMonth = "JUN"; break;
         case 7: vShortMonth = "JUL"; break;
         case 8: vShortMonth = "AUG"; break;
         case 9: vShortMonth = "SEP"; break;
         case 10: vShortMonth = "OCT"; break;
         case 11: vShortMonth = "NOV"; break;
         case 12: vShortMonth = "DEC"; break;
         default: return new String();
      }

      // DD-MON-YYYY:HH:MM:SS
      String vNewDate = null;
      if (aDay < 10)
         vNewDate = "0" + aDay + "-" + vShortMonth + "-" + vYear + ":";
      else
         vNewDate = aDay + "-" + vShortMonth + "-" + vYear + ":";
      // Add Hour
      if (aHour < 10)
         vNewDate += "0" + aHour + ":";
      else
         vNewDate += aHour + ":";
      // Add minute
      if (aMinute < 10)
         vNewDate += "0" + aMinute + ":";
      else
         vNewDate += aMinute + ":";
      if (aSecond < 10)
         vNewDate += "0" + aSecond;
      else
         vNewDate += aSecond;

      return vNewDate;
   }

   /**
   *  Formats a date to DD-MON-YYYY:HH:MM:SS
   *
   *  @return String   a formatted date.
   */
   public static String formatToDBDate(String aMonth, String aDay, String aYear,
                                 String aHour, String aMinute, String aSecond)
   {
      if (aMonth == null || aMonth.length() == 0 || aMonth.length() > 2)
         return new String();
      if (aDay == null || aDay.length() == 0 || aDay.length() > 2)
         return new String();
      if ((aYear == null) || (aYear.length() != 2 && aYear.length() != 4))
         return new String();
      if (aHour == null || aHour.length() == 0 || aHour.length() > 2)
         return new String();
      if (aMinute == null || aMinute.length() == 0 || aMinute.length() > 2)
         return new String();
      if (aSecond == null || aSecond.length() == 0 || aSecond.length() > 2)
         return new String();

      String vYear = aYear;
      if (aMonth.length() == 1)
         aMonth = "0" + aMonth;
      if (aDay.length() == 1)
         aDay = "0" + aDay;
      if (aYear.length() == 2)
      {
         try
         {
            int vShortYear = Integer.parseInt(aYear);
            if (vShortYear < 80)
               vYear = "20" + aYear;
            else
               vYear = "19" + aYear;
         }
         catch (NumberFormatException e)
         {
            return new String();
         }
      }
      if (aHour.length() == 1)
         aHour = "0" + aHour;
      if (aMinute.length() == 1)
         aMinute = "0" + aMinute;
      if (aSecond.length() == 1)
         aSecond = "0" + aSecond;

      String vShortMonth = null;
      if (aMonth.equalsIgnoreCase("01"))
         vShortMonth = "JAN";
      else if (aMonth.equalsIgnoreCase("02"))
         vShortMonth = "FEB";
      else if (aMonth.equalsIgnoreCase("03"))
         vShortMonth = "MAR";
      else if (aMonth.equalsIgnoreCase("04"))
         vShortMonth = "APR";
      else if (aMonth.equalsIgnoreCase("05"))
         vShortMonth = "MAY";
      else if (aMonth.equalsIgnoreCase("06"))
         vShortMonth = "JUN";
      else if (aMonth.equalsIgnoreCase("07"))
         vShortMonth = "JUL";
      else if (aMonth.equalsIgnoreCase("08"))
         vShortMonth = "AUG";
      else if (aMonth.equalsIgnoreCase("09"))
         vShortMonth = "SEP";
      else if (aMonth.equalsIgnoreCase("10"))
         vShortMonth = "OCT";
      else if (aMonth.equalsIgnoreCase("11"))
         vShortMonth = "NOV";
      else if (aMonth.equalsIgnoreCase("12"))
         vShortMonth = "DEC";
      else
         return new String();

      // DD-MON-YYYY:HH:MM:SS
      String vNewDate = aDay + "-" + vShortMonth + "-" + vYear + ":" +
                        aHour + ":" + aMinute + ":" + aSecond;

      return vNewDate;
   }

   /**
   *  @param aDBDate  a date in format DD-MON-YYYY:HH:MM:SS
   *
   *  @return String  a date in format YYYYMMDD
   */
   public static String formatDBDateToYYYYMMDD(String aDBDate)
   {
      if (aDBDate == null || aDBDate.length() <= 11)
         return new String();

      String vDay = aDBDate.substring(0, 2);
      String aMonth = aDBDate.substring(3, 6);
      String vYear = aDBDate.substring(7, 11);

      String vShortMonth = null;
      if (aMonth.equalsIgnoreCase("JAN"))
         vShortMonth = "01";
      else if (aMonth.equalsIgnoreCase("FEB"))
         vShortMonth = "02";
      else if (aMonth.equalsIgnoreCase("MAR"))
         vShortMonth = "03";
      else if (aMonth.equalsIgnoreCase("APR"))
         vShortMonth = "04";
      else if (aMonth.equalsIgnoreCase("MAY"))
         vShortMonth = "05";
      else if (aMonth.equalsIgnoreCase("JUN"))
         vShortMonth = "06";
      else if (aMonth.equalsIgnoreCase("JUL"))
         vShortMonth = "07";
      else if (aMonth.equalsIgnoreCase("AUG"))
         vShortMonth = "08";
      else if (aMonth.equalsIgnoreCase("SEP"))
         vShortMonth = "09";
      else if (aMonth.equalsIgnoreCase("OCT"))
         vShortMonth = "10";
      else if (aMonth.equalsIgnoreCase("NOV"))
         vShortMonth = "11";
      else if (aMonth.equalsIgnoreCase("DEC"))
         vShortMonth = "12";
      else
         return new String();

      return (vYear + vShortMonth + vDay);
   }

   /**
   *  @param aDBDate:  a date in format DD-MON-YYYY:HH:MM:SS
   *                   or  DD-MON-YYYY
   *
   *  @return int array:  array[0] = month, array[1] = day, array[2] = year,
   *                      array[3] = hour, array[4] = minute, array[5] = second
   */
   public static int[] parseDBDate(String aDBDate)
   {
      int[] vDate = new int[6];
      for (int i=0; i < vDate.length; i++)
         vDate[i] = 0;

      if (aDBDate == null || aDBDate.length() < 11)
         return null;

      String vMonth = aDBDate.substring(3, 6);
      if (vMonth.equalsIgnoreCase("JAN"))
         vDate[0] = 1;
      else if (vMonth.equalsIgnoreCase("FEB"))
         vDate[0] = 2;
      else if (vMonth.equalsIgnoreCase("MAR"))
         vDate[0] = 3;
      else if (vMonth.equalsIgnoreCase("APR"))
         vDate[0] = 4;
      else if (vMonth.equalsIgnoreCase("MAY"))
         vDate[0] = 5;
      else if (vMonth.equalsIgnoreCase("JUN"))
         vDate[0] = 6;
      else if (vMonth.equalsIgnoreCase("JUL"))
         vDate[0] = 7;
      else if (vMonth.equalsIgnoreCase("AUG"))
         vDate[0] = 8;
      else if (vMonth.equalsIgnoreCase("SEP"))
         vDate[0] = 9;
      else if (vMonth.equalsIgnoreCase("OCT"))
         vDate[0] = 10;
      else if (vMonth.equalsIgnoreCase("NOV"))
         vDate[0] = 11;
      else if (vMonth.equalsIgnoreCase("DEC"))
         vDate[0] = 12;
      else
         return null;

      try
      {
         String vDay = aDBDate.substring(0, 2);
         vDate[1] = Integer.parseInt(vDay);

         String vYear = null;
         if (aDBDate.length() == 11)
            vYear = aDBDate.substring(7);
         else
            vYear = aDBDate.substring(7, 11);
         vDate[2] = Integer.parseInt(vYear);

         if (aDBDate.length() == 20)
         {
            String vHour = aDBDate.substring(12, 14);
            vDate[3] = Integer.parseInt(vHour);
            String vMinute = aDBDate.substring(15, 17);
            vDate[4] = Integer.parseInt(vMinute);
            String vSecond = aDBDate.substring(18);
            vDate[5] = Integer.parseInt(vSecond);
         }
      }
      catch(Exception e)
      {
         vDate = null;
      }

      return vDate;
   }

   public static int getMonthEndDay(int aMonth, int aYear)
   {
      int vDay = 0;
      switch(aMonth)
      {
      case 1:
         vDay = 31;
         break;

      case 2:
         if (isLeapYear(aYear))
            vDay = 29;
         else
            vDay = 28;
         break;

      case 3:
         vDay = 31;
         break;

      case 4:
         vDay = 30;
         break;

      case 5:
         vDay = 31;
         break;

      case 6:
         vDay = 30;
         break;

      case 7:
         vDay = 31;
         break;

      case 8:
         vDay = 31;
         break;

      case 9:
         vDay = 30;
         break;

      case 10:
         vDay = 31;
         break;

      case 11:
         vDay = 30;
         break;

      case 12:
         vDay = 31;
         break;
      }

      return vDay;
   }

   public static boolean isLeapYear(int aYear)
   {
      int vYear = aYear;
      if (vYear < 100)
      {
         if (vYear < 80)
            vYear += 2000;
         else
            vYear += 1900;
      }

      if (vYear % 4 != 0)
         return false;
      if (vYear % 400 == 0)
         return true;
      if (vYear % 100 == 0)
         return false;

      return true;
   }

   public static boolean isValidDate(int aMonth, int aDay, int aYear)
   {
      if (aMonth < 1 || aMonth > 12)
         return false;
      if (aDay < 1 || aDay > 31)
         return false;
      if (aYear < 1000)
         return false;

      int vDay = getMonthEndDay(aMonth, aYear);
      if (aDay > vDay)
         return false;

      return true;
   }

}
