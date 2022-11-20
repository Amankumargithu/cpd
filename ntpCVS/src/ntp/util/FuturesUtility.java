package ntp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ntp.logger.NTPLogger;

public class FuturesUtility {

	private HashMap<Character, Integer> monthMappedMap = new HashMap<Character, Integer>();
	private static FuturesUtility instance = new FuturesUtility();
	private Calendar currentCal = Calendar.getInstance();

	private FuturesUtility() {
		monthMappedMap.put('F', 1);	//January
		monthMappedMap.put('G', 2);	//February
		monthMappedMap.put('H', 3);	//March
		monthMappedMap.put('J', 4);	//April
		monthMappedMap.put('K', 5);	//May
		monthMappedMap.put('M', 6);	//June
		monthMappedMap.put('N', 7);	//July
		monthMappedMap.put('Q', 8);	//August
		monthMappedMap.put('U', 9);	//September
		monthMappedMap.put('V', 10);	//October
		monthMappedMap.put('X', 11);	//November
		monthMappedMap.put('Z', 12);	//December
	}

	public static FuturesUtility getInstance(){
		return instance;
	}

	public boolean validateFuturesSymbol(String underlyingTicker, String ticker)
	{
		//Format is / + underlyingTicker + 1char month map + 2 digit year map
		String baseTicker = "/" + underlyingTicker;
		if(ticker.startsWith(baseTicker))
		{
			if(ticker.endsWith(".ICE") || ticker.endsWith(".CFE") || ticker.endsWith(".MGEX"))
				return true;
			String subVal = ticker.substring(baseTicker.length());
			if(subVal.endsWith(".P"))
				subVal = subVal.substring(0, subVal.length() -2);
			if(subVal.length() > 3 || subVal.length() == 0)
				return false;
			char c = subVal.charAt(0);
			Integer month = monthMappedMap.get(c);
			if(month != null)
			{
				try
				{
					int year = Integer.parseInt(subVal.substring(1)) + 2000;
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.MONTH, month -1);
					cal.set(Calendar.YEAR, year);
					if(cal.before(currentCal))
						return false;					
					return true;
				}
				catch(Exception e)
				{
					return false;
				}
			}
		}
		return false;
	}

	public String getExchange(int protocol, String ticker)
	{
		/*if(ticker.toUpperCase().endsWith(".ICE"))
			return "ICE";
		if(ticker.toUpperCase().endsWith(".CFE"))
			return "CME";*/
		switch (protocol)
		{
		case 80:
		case 81:
		case 130:
			return "CME";
		case 84:
		case 85:
		case 132:
			return "NYMEX";
		case 88:
		case 89:
		case 133:
			return "COMEX";
		case 92:
		case 93:
		case 131:
			return "CBOT";
		case 253:
			return "Minn Grain";
		case 254:
			return "CFE";
		case 255:
			return "ICE";
		}
		NTPLogger.defaultSetting("protocolMapping " + protocol + " " + ticker, "COMEX");
		return "COMEX";
	}

	public Calendar getExpirationDate(long expireMDY) 
	{
		try {
			if(expireMDY == 0)
				return null;
			Calendar expirationDate = Calendar.getInstance();
			String expiryDate = "" + expireMDY;
			if(expiryDate == null || expiryDate.length() < 7)
				return null;
			if(expiryDate.length() == 7)
				expiryDate = "0" + expiryDate;
			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
			Date date = sdf.parse(expiryDate);
			expirationDate.setTime(date);
			return expirationDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Exception " + e.getMessage() + " " + expireMDY);
			e.printStackTrace();
		}
		return null;
	}
}
