package ntp.util;

import java.util.HashMap;

import QuoddFeed.msg.QuoddMsg;

public class EquityRegionalUtility {

	private EquityRegionalUtility(){}
	private static EquityRegionalUtility instance = new EquityRegionalUtility();


	public static EquityRegionalUtility getInstance(){
		return instance;
	}
	 /* 
	 * @param nasdaqExchangeCode
	 * @return The equity plus code corresponding to NASDAQ exchange code
	 */
	public String getEquityPlusExchangeCode(String nasdaqExchangeCode)
	{
		HashMap<String, String> exchgMap = ExchangeMapPopulator.getInstance().getExchangeMap();
		if(exchgMap.containsKey(nasdaqExchangeCode))
		{
			return exchgMap.get(nasdaqExchangeCode).toLowerCase();
		}
		return nasdaqExchangeCode;
	}
	
	public String getFormattedTime(long millis)
	{
		int[] t = QuoddMsg.timeHMSms(millis);
		
		return String.format( "%02d%02d%02d", t[0], t[1], t[2] );
			
	}
}
