package ntp.util;

import ntp.logger.NTPLogger;

public class OptionsRegionalUtility {

	private static OptionsRegionalUtility instance = new OptionsRegionalUtility();

	private OptionsRegionalUtility() {}

	public static OptionsRegionalUtility getInstance(){
		return instance;
	}

	public String getUCFormattedTicker(String ticker)
	{
		try
		{
			String tickerRegionalPart = ticker.substring(ticker.lastIndexOf("/"));
			ticker = ticker.substring(0, ticker.lastIndexOf("/"));
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
			return ticker + tickerRegionalPart;		
		}
		catch(Exception e){}
		return ticker;
	}

	public String getEQPlusFormattedTicker(String ticker) 
	{
		String tickerRegionalPart = ticker.substring(ticker.lastIndexOf("/"));
		ticker = ticker.substring(0, ticker.lastIndexOf("/"));
		try 
		{

			int precisionIndex = ticker.indexOf('.');
			if(precisionIndex != -1)
			{
				String precision = ticker.substring(precisionIndex + 1);
				if(precision.length() == 2 && precision.endsWith("0"))
					ticker = ticker.substring(0, ticker.length() -1);
				else if(precision.length() == 0)
					ticker = ticker + "0";
			}
			else
			{
				ticker = ticker + ".0";
			}
		} 
		catch (Exception e) 
		{
			NTPLogger.error("getEQPlusFormattedTicker" + e.getMessage() + " for ticker: " + ticker);
		}
		return ticker + tickerRegionalPart;
	}

	public String getRootSymbol(String optionRegionalSymbol)
	{
		try
		{
			return optionRegionalSymbol.split("\\\\")[0].substring(2);
		}
		catch(Exception e)
		{
			NTPLogger.warning("Parsing root symbol for " + optionRegionalSymbol);
			e.printStackTrace();
		}
		return optionRegionalSymbol;
	}

	public boolean validateOptionRegionalSymbol(String ticker)
	{
		if(ticker.startsWith("O:") && ticker.contains("/"))
			return true;
		return false;
	}
}
