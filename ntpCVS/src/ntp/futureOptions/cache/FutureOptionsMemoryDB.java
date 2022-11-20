package ntp.futureOptions.cache;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.b4utrade.bean.StockOptionBean;

public final class FutureOptionsMemoryDB 
{
	private static FutureOptionsMemoryDB optionDB =  new FutureOptionsMemoryDB ();
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, StockOptionBean>> chainMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, StockOptionBean>>();
	private static ConcurrentHashMap<String, StockOptionBean> fundamentalMap = new ConcurrentHashMap<String, StockOptionBean>();
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, StockOptionBean>> expirationDateMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, StockOptionBean>>();
		
	// Constructor
	private FutureOptionsMemoryDB()	{}
	
	/**
	 * getInstance
	 *    get the handle to the option chain memory database.
	 *
	 * @return OptionChainMemoryDB
	 */
	public static synchronized FutureOptionsMemoryDB getInstance()
	{
		if(optionDB == null)
			optionDB = new FutureOptionsMemoryDB();
		return optionDB;
	}
	
	/**
	 * getOptionList
	 *    get a list of option tickers based on the underlyingTicker.
	 *
	 * @param String underlyingTicker
	 * @return HashMap a list of option Tickers
	 */
	public HashMap<String,StockOptionBean> getOptionChain(String underlyingTicker)
	{
		if (underlyingTicker == null)
			return null;
		ConcurrentHashMap<String, StockOptionBean> conMap = chainMap.get(underlyingTicker);
		if(conMap == null )
			return null;
		else
		{
			HashMap<String,StockOptionBean> map = new  HashMap<String,StockOptionBean>();
			map.putAll(conMap);
			return map;
		}
	}
	
	public void add(StockOptionBean optionbean)
	{
		ConcurrentHashMap<String, StockOptionBean> tempMap = chainMap.get(optionbean.getUnderlyingStockTicker());
		if (tempMap == null)
			tempMap = new ConcurrentHashMap<String, StockOptionBean>();
		tempMap.put(optionbean.getTicker(), optionbean);
		chainMap.put(optionbean.getUnderlyingStockTicker(), tempMap);
		ConcurrentHashMap<String, StockOptionBean> tempExpireMap = expirationDateMap.get(optionbean.getUnderlyingStockTicker());
		if (tempExpireMap == null)
			tempExpireMap = new ConcurrentHashMap<String, StockOptionBean>();
		tempExpireMap.put(getCalendarAsString( optionbean.getExpirationDate()), optionbean);
		expirationDateMap.put(optionbean.getUnderlyingStockTicker(), tempExpireMap);
	}
	
	/**
	 * getOption
	 *    get a option bean by the option ticker
	 *
	 * @param String optionTicker
	 * @return StockOptionBean an option
	 */
	public StockOptionBean getFundamentalBean(String optionTicker)
	{
		if(fundamentalMap == null || !fundamentalMap.containsKey(optionTicker))
			return null;
		return fundamentalMap.get(optionTicker);
	}

	public void addStockFundamentalData(String ticker, StockOptionBean bean)
	{
		fundamentalMap.put(ticker, bean);
	}
	
	/**
	 * getExpirationChain
	 *    get a list of expiration dates for a underlying ticker
	 *
	 * @param String underlying ticker
	 * @return HashMap a list of expiration dates
	 */
	public HashMap<String, StockOptionBean> getExpirationChain(String underlyingTicker)
	{
		if (underlyingTicker == null)
			return null;
		ConcurrentHashMap<String, StockOptionBean> conMap = expirationDateMap.get(underlyingTicker);
		if(conMap == null )
			return null;
		HashMap<String, StockOptionBean> results = new HashMap<String, StockOptionBean>();
		results.putAll(conMap);
		return results;
	}
	
	private String getCalendarAsString(Calendar inputCal)
	{
		if (inputCal == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append(inputCal.get(Calendar.YEAR));
		sb.append("-");
		sb.append((inputCal.get(Calendar.MONTH)) +1);  //add 1 to the display month since calendar start with 0
		sb.append("-");
		sb.append(inputCal.get(Calendar.DAY_OF_MONTH));
		return(sb.toString());
	}
}
