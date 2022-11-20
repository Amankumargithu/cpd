package ntp.util;

import java.util.HashMap;

import ntp.logger.NTPLogger;

public class FuturesOptionsUtility {
	private static FuturesOptionsUtility instance = new FuturesOptionsUtility();
	private static String futureOptionSymbolPattern = "[F][O][\\:][A-Z0-9]+[\\\\][0-9]{6}[\\\\][0-9]+[\\.]*[0-9]*[A-Z]{1}";
	private HashMap<String, String> monthMappingCall = new HashMap<String, String>();
	private HashMap<String, String> monthMappingPut = new HashMap<String, String>();
	
	private FuturesOptionsUtility() {
		monthMappingCall.put("01", "A"); // call month jan
		monthMappingCall.put("02", "B");
		monthMappingCall.put("03", "C");
		monthMappingCall.put("04", "D");
		monthMappingCall.put("05", "E");
		monthMappingCall.put("06", "F");
		monthMappingCall.put("07", "G");
		monthMappingCall.put("08", "H");
		monthMappingCall.put("09", "I");
		monthMappingCall.put("10", "J");
		monthMappingCall.put("11", "K");
		monthMappingCall.put("12", "L");
		monthMappingPut.put("01", "M"); // put month jan ... so on
		monthMappingPut.put("02", "N");
		monthMappingPut.put("03", "O");
		monthMappingPut.put("04", "P");
		monthMappingPut.put("05", "Q");
		monthMappingPut.put("06", "R");
		monthMappingPut.put("07", "S");
		monthMappingPut.put("08", "T");
		monthMappingPut.put("09", "U");
		monthMappingPut.put("10", "V");
		monthMappingPut.put("11", "W");
		monthMappingPut.put("12", "X");		
	}

	public static FuturesOptionsUtility getInstance(){
		return instance;
	}

	public String getRootSymbol(String optionSymbol)
	{
		try
		{
			return optionSymbol.split("\\\\")[0].substring(2);
		}
		catch(Exception e)
		{
			NTPLogger.warning("Parsing root symbol for " + optionSymbol);
			e.printStackTrace();
		}
		return optionSymbol;
	}

	public String getUCFormattedTicker(String ticker)
	{
		int precisionIndex = ticker.indexOf('.');
		if(precisionIndex != -1)
		{
			String precision = ticker.substring(precisionIndex + 1);
			if(precision.length() == 1)
				ticker = ticker + "0";
			else if(precision.length() == 0)
				ticker = ticker + "00";
		}
		else
		{
			ticker = ticker + ".00";
		}
		return ticker;
	}

	public String getEQPlusFormattedTicker(String ticker) 
	{
		//root <space> year, month, Strike, Call/Put  EW1 15V2000C
		//   FO:ES\151100\2000.00C
		try 
		{
			String[] arr = ticker.split("\\\\");
			if(arr.length != 3)
				return ticker;
			StringBuffer sb = new StringBuffer();
			sb.append(arr[0].substring(3));
			sb.append(" ");
			sb.append(arr[1].substring(0,2));
			String month = arr[1].substring(2,4);
			String type = ticker.substring(ticker.length() -1);
			Double strike = Double.parseDouble(arr[2].substring(0, arr[2].length() -1));
			if(type.equals("C"))
				sb.append(monthMappingCall.get(month));
			else
				sb.append(monthMappingPut.get(month));
			sb.append(strike);
			sb.append(type);
			return sb.toString();
		} 
		catch (Exception e) 
		{
			NTPLogger.error("getEQPlusFormattedTicker" + e.getMessage() + " for ticker: " + ticker);
		}
		return ticker;
	}

	public boolean validateFutureOptionSymbol(String ticker)
	{
		if( ticker.length() >= 32 )
			return false;
		if(!ticker.matches(futureOptionSymbolPattern))
			return false;
		return true;
	}
}
