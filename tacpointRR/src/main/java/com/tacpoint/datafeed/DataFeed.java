/** DataFeed.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kgentes@tacpoint.com
* @version 1.0
*
* Date created:  12/23/1999
* - 01/05/2000
*    - Use Logger class (KDN)
* - 01.26.2000 (JT)
*    - added cleanup method.
* - 03/11/2000 (KG)
*    - added setDataRetrievalTimes method.
* - 03/11/2000 (KG)
*    - added isTimeToGetMessage method.
*/
package com.tacpoint.datafeed;

import com.tacpoint.network.*;
import com.tacpoint.util.*;
import com.tacpoint.workflow.*;
import com.tacpoint.configuration.*;
import java.util.*;


/** Abstract class representing different types of datafeeds. */
public abstract class DataFeed extends WorkFlowTask
{
	////////////////////////////////////////////////////////////////////////////
	// D A T A	  M E M B E R S
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Network connection object to data feed.
	 */
	protected NetworkConnection mConnection = null;

	/**
	 * A list of times data feed should be retrieved
	 * List is (2-dimentional):
	 *	hour, minute
	 *	hour, minute  (etc.)
	 */
	private List mDataRetrievalTimes = null;

	/**
	 * A list of days of the week the data feed should be retrieved
	 */
	private int[] mDataRetrievalWeekDays = null;

	/**
	 * A list of days of the month the data feed should be retrieved
	 */
	private int[] mDataRetrievalDays = null;


	////////////////////////////////////////////////////////////////////////////
	// C O N S T R U C T O R S
	////////////////////////////////////////////////////////////////////////////

	public DataFeed()
	{
		try
		{
			Logger.init();
			Logger.log("DataFeed constructor.");
		}
		catch(Exception e)
		{
			String vMsg;
			vMsg = "DataFeed constructor: Unable to init Logger.";
			System.out.println(vMsg);
		}
	}

	/**
	 * Retrieve message and put message in the 'mOutQueue' to be parsed.
	 */
	public abstract void getMessage();

	/**
	 * Get the data feed retrieval days of a month, from the configuration file.
	 */
	protected boolean setDataRetrievalDays(String aGroup, String aField)
	{
		if (aGroup == null || aGroup.length() == 0)
		{
			String vMsg = "DataFeed.setDataRetrievalDays(): ";
			vMsg += "parameter [aGroup] was blank.";
			Logger.log(vMsg);
			return false;
		}
		if (aField == null || aField.length() == 0)
		{
			String vMsg = "DataFeed.setDataRetrievalDays(): ";
			vMsg += "parameter [aField] was blank.";
			Logger.log(vMsg);
			return false;
		}

		boolean vOK = false;
		try
		{
			// Retrieve the data feed days.
			FileConfiguration vConfigFile =
						FileConfiguration.getConfigurationFile();
			String[] vValues = vConfigFile.getFieldValueList(aGroup, aField);
			if (vValues == null || vValues.length == 0)
			{
				String vMsg = "Unable to retrieve item [" + aField;
				vMsg += "] from group [" + aGroup + "] from configuration file.";
				throw new Exception(vMsg);
			}

			int[] vDays = new int[vValues.length];
			for (int i=0; i < vValues.length; i++)
				vDays[i] = Integer.parseInt(vValues[i]);

			mDataRetrievalDays = vDays;
			vOK = true;
		}
		catch(Exception e)
		{
			Logger.log("DataFeed.setDataRetrievalDays(): " + e.getMessage());
		}

		return vOK;
	}

	/**
	 * Get the data feed retrieval days of the week, from the configuration file.
	 */
	protected boolean setDataRetrievalWeekDays(String aGroup, String aField)
	{
		if (aGroup == null || aGroup.length() == 0)
		{
			String vMsg = "DataFeed.setDataRetrievalWeekDays(): ";
			vMsg += "parameter [aGroup] was blank.";
			Logger.log(vMsg);
			return false;
		}
		if (aField == null || aField.length() == 0)
		{
			String vMsg = "DataFeed.setDataRetrievalWeekDays(): ";
			vMsg += "parameter [aField] was blank.";
			Logger.log(vMsg);
			return false;
		}

		boolean vOK = false;
		try
		{
			// Retrieve the data feed days.
			FileConfiguration vConfigFile =
						FileConfiguration.getConfigurationFile();
			String[] vValues = vConfigFile.getFieldValueList(aGroup, aField);
			if (vValues == null || vValues.length == 0)
			{
				String vMsg = "Unable to retrieve item [" + aField;
				vMsg += "] from group [" + aGroup + "] from configuration file.";
				throw new Exception(vMsg);
			}

			int[] vWeekDays = new int[vValues.length];
			for (int i=0; i < vValues.length; i++)
				vWeekDays[i] = Integer.parseInt(vValues[i]);

			mDataRetrievalWeekDays = vWeekDays;
			vOK = true;
		}
		catch(Exception e)
		{
			Logger.log("DataFeed.setDataRetrievalWeekDays(): " + e.getMessage());
		}

		return vOK;
	}

	/**
	 * Get the data feed retrieval times from the configuration file,
	 * and parse the times into the 'mDataRetrievalTimes' list.
	 */
	protected boolean setDataRetrievalTimes(String aGroup, String aField)
	{
		if (aGroup == null || aGroup.length() == 0)
		{
			String vMsg = "DataFeed.setDataRetrievalTimes(): ";
			vMsg += "parameter [aGroup] was blank.";
			Logger.log(vMsg);
			return false;
		}
		if (aField == null || aField.length() == 0)
		{
			String vMsg = "DataFeed.setDataRetrievalTimes(): ";
			vMsg += "parameter [aField] was blank.";
			Logger.log(vMsg);
			return false;
		}

		boolean vOK = false;
		try
		{
			// 1. Retrieve the data feed times.
			FileConfiguration vConfigFile =
						FileConfiguration.getConfigurationFile();
			String vTimes = vConfigFile.getFieldValue(aGroup, aField);
			if (vTimes == null || vTimes.length() == 0)
			{
				String vMsg = "Unable to retrieve item [" + aField;
				vMsg += "] from group [" + aGroup + "] from configuration file.";
				throw new Exception(vMsg);
			}

			int vIndex = -1;
			mDataRetrievalTimes = new ArrayList();
			String vField = null;

			// Parse out all fields separated by comma.
			while (vTimes.length() > 0 &&
					(vIndex = vTimes.indexOf(',')) >= 0)
			{
				if (vIndex == 0)
					vField = new String();
				else
				{
					vField = vTimes.substring(0, vIndex);
					vField = vField.trim();
				}

				if (vIndex+1 < vTimes.length())
					vTimes = vTimes.substring(vIndex+1);
				else
					vTimes = new String();

				int[] vRetrievalTime = parseTime(vField);
				if (vRetrievalTime != null && vRetrievalTime.length == 2)
					mDataRetrievalTimes.add(vRetrievalTime);

         }

			// Add last field.
			if (vTimes.length() > 0)
			{
				int[] vRetrievalTime = parseTime(vTimes);
				if (vRetrievalTime != null && vRetrievalTime.length == 2)
					mDataRetrievalTimes.add(vRetrievalTime);
			}

			if (mDataRetrievalTimes.size() == 0)
				throw new Exception("Unable to parse time [" + vTimes + "].");

			vOK = true;
		}
		catch(Exception e)
		{
			Logger.log("DataFeed.setDataRetrievalTimes(): " + e.getMessage());
		}

		return vOK;
   }

	private int[] parseTime(String aTime)
	{
		if (aTime == null || aTime.length() == 0)
		{
			String vMsg = "DataFeed.parseTime(): ";
			vMsg += "parameter [aTime] was blank.";
			Logger.log(vMsg);
			return null;
		}

		int[] vRetrievalTime = null;
		int vHour = -1;
		int vMinute = -1;
		int vIndex = aTime.indexOf(':');
		try
		{
			if (vIndex < 0)
			{
				// No minutes
				vMinute = 0;
				vHour = Integer.parseInt(aTime);
			}
			else if (vIndex == 0)
			{
				// No hour (midnight)
				vHour = 0;
				vMinute = Integer.parseInt(aTime.substring(1));
			}
			else
			{
				vHour = Integer.parseInt(aTime.substring(0, vIndex));
				vMinute = Integer.parseInt(aTime.substring(vIndex+1));
			}

			if (vHour < 0 || vHour > 23)
				throw new Exception("Invalid hour [" + vHour + "] specified.");
			if (vMinute < 0 || vMinute > 59)
				throw new Exception("Invalid minute [" + vMinute + "] specified.");

			vRetrievalTime = new int[2];
			vRetrievalTime[0] = vHour;
			vRetrievalTime[1] = vMinute;
      }
      catch(Exception e)
      {
      	Logger.log("DataFeed.parseTime(): " + e.getMessage());
      }

		return vRetrievalTime;
   }

	/**
	 * Check to see if it time to retrieve data feed.
	 */
	protected boolean isTimeToGetMessage()
	{
		if (mDataRetrievalTimes == null || mDataRetrievalTimes.size() == 0)
			return true;

		// Get current time.
      Calendar vDate = Calendar.getInstance();
      int vCurrentHour = vDate.get(Calendar.HOUR_OF_DAY);
      int vCurrentMinute = vDate.get(Calendar.MINUTE);

		boolean isTime = false;
		boolean doStop = false;
		boolean gotTime = false;
		int i = 0;
		// Check the times in the 'mDataRetrievalTimes' list.
		while (!doStop && i < mDataRetrievalTimes.size())
		{
			int[] vTime = (int[])mDataRetrievalTimes.get(i++);
			if (vTime == null || vTime.length == 0)
				continue;

			gotTime = true;
			int vHour = vTime[0];
			int vMinute = vTime[1];
			if (vCurrentHour >= vHour && vCurrentHour <= vHour+1)
			{
				if (vCurrentHour == vHour && vCurrentMinute < vMinute)
					doStop = true;
				else
				{
					isTime = true;
					doStop = true;
				}
			}
      }

		// If all the list items were null, then go ahead to set 'isTime' = true.
		if (!gotTime)
			isTime = true;

		return isTime;
	}

	/**
	 * Check to see if the day of the month is right to retrieve data feed.
	 */
	protected boolean isDayToGetMessage()
	{
		return checkDate(mDataRetrievalDays, Calendar.DAY_OF_MONTH);
	}

	/**
	 * Check to see if the day of the week is right to retrieve data feed.
	 */
	protected boolean isWeekDayToGetMessage()
	{
		return checkDate(mDataRetrievalWeekDays, Calendar.DAY_OF_WEEK);
	}

	/**
	 * Check to see if the date is right to retrieve data feed.
	 */
	private boolean checkDate(int[] aDates, int aDayType)
	{
		if (aDates == null || aDates.length == 0)
			return isTimeToGetMessage();

		// Get current day.
		Calendar vDate = Calendar.getInstance();
		int vDay = vDate.get(aDayType);

		boolean vIsDay = false;
		for (int i=0; i < aDates.length; i++)
		{
			if (vDay == aDates[i])
			{
				vIsDay = true;
				break;
			}
		}

		if (!vIsDay)
			return false;

		return isTimeToGetMessage();
	}

}
