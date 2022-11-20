package ntp.futureOptions.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.futureOptions.streamer.FutureOptionsStreamingChannel;
import ntp.options.parser.OptionsUnderlyerParser;
import ntp.queue.MappedMessageQueue;

public class FutureOptionsQTMessageQueue {
	
	private static volatile FutureOptionsQTMessageQueue qtMessageQueue = new FutureOptionsQTMessageQueue();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private Map<String, QTCPDMessageBean> subsData = new ConcurrentHashMap<String, QTCPDMessageBean>();
	private ConcurrentHashMap<String, String> imageTickerMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> incorrectTickerMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> markedTickersMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, FutureOptionsStreamingChannel> subscribedSymbolMap = new ConcurrentHashMap<String, FutureOptionsStreamingChannel>();
	private ConcurrentHashMap<String, String> requestedSymbolMap = new ConcurrentHashMap<String, String>();
	public final String BLANK = ""; 
//	private OptionsVolatilityDataParser volatilityParser = OptionsVolatilityDataParser.getInstance();
	private OptionsUnderlyerParser underlyerParser = OptionsUnderlyerParser.getInstance();
//	private InterestRateParser interestRateParser = InterestRateParser.getInstance();
	
	private FutureOptionsQTMessageQueue() {}

	public static FutureOptionsQTMessageQueue getInstance(){
		return qtMessageQueue;
	}
	/**
	 * @return the mQueue
	 */
	public MappedMessageQueue getmQueue() {
		return mQueue;
	}

	/**
	 * @param mQueue the mQueue to set
	 */
	public void setmQueue(MappedMessageQueue mQueue) {
		this.mQueue = mQueue;
	}
	/**
	 * @return the subsData
	 */
	public Map<String, QTCPDMessageBean> getSubsData() {
		return subsData;
	}
	public ConcurrentHashMap<String, String> getImageTickerMap()
	{
		return imageTickerMap;
	}
	public ConcurrentHashMap<String, String> getIncorrectTickerMap()
	{
		return incorrectTickerMap;
	}
	
	public void addMarkedSymbol(String symbol)
	{
		if(symbol != null)
			markedTickersMap.put(symbol, symbol);
	}
	
	public boolean isMarkedSymbol(String symbol)
	{
		return markedTickersMap.containsKey(symbol);
	}
	public void removeMarkedSymbol(String symbol)
	{
		if(symbol != null)
			markedTickersMap.remove(symbol);
	}
	public String getUnderlyer(String rootSymbol)
	{
		return underlyerParser.getUnderlyer(rootSymbol);
	}
	
	public void addSubscribedSymbol(String symbol, FutureOptionsStreamingChannel channel)
	{
		subscribedSymbolMap.put(symbol, channel);
	}
	public boolean isSubscribedSymbol(String symbol)
	{
		if(subscribedSymbolMap.contains(symbol))
			return true;
		return false;
	}
	public String getChannelName(String symbol)
	{
		return subscribedSymbolMap.get(symbol).getChannelName();
	}
	
	public ConcurrentHashMap<String, String> getRequestedSymbolMap()
	{
		return requestedSymbolMap;
	}
	
	/*public double getVolatility(String symbol)
	{
		return volatilityParser.getVolatility(symbol);
	}
	
	public InterestRateBean getInterestRatebean()
	{
		return interestRateParser.getInterestRateBean();
	}*/
	public boolean isIncorrectSymbol(String ticker)
	{
//		ticker = OptionsUtility.getInstance().getUCFormattedTicker(ticker);
		return incorrectTickerMap.containsKey(ticker);
	}
}
