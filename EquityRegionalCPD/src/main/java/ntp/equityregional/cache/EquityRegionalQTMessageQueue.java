package ntp.equityregional.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.equityregional.streamer.EquityRegionalStreamingChannel;
import ntp.queue.MappedMessageQueue;

public class EquityRegionalQTMessageQueue {

	private Map<String, QTCPDMessageBean> subsData = new HashMap<String, QTCPDMessageBean>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private static EquityRegionalQTMessageQueue qtMessageQueue = new EquityRegionalQTMessageQueue();
	private ConcurrentHashMap<String, String> requestedSymbolMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, EquityRegionalStreamingChannel> subscribedSymbolMap = new ConcurrentHashMap<String, EquityRegionalStreamingChannel>();
	private ConcurrentHashMap<String, String> incorrectTickerMap = new ConcurrentHashMap<String, String>();

	public final String BLANK = "";

	private EquityRegionalQTMessageQueue() {}

	public static EquityRegionalQTMessageQueue getInstance(){
		return qtMessageQueue;
	}
	/**
	 * @return the subsData
	 */
	public Map<String, QTCPDMessageBean> getSubsData() {
		return subsData;
	}
		
	/**
	 * @return the mQueue
	 */
	public MappedMessageQueue getmQueue() {
		return mQueue;
	}
	
	public ConcurrentHashMap<String, String> getRequestedSymbolMap()
	{
		return requestedSymbolMap;
	}
	
	public boolean isSubscribedSymbol(String symbol)
	{
		if(subscribedSymbolMap.contains(symbol))
			return true;
		return false;
	}
	
	public void addSubscribedSymbol(String symbol, EquityRegionalStreamingChannel channel)
	{
		subscribedSymbolMap.put(symbol, channel);
	}
	
	public ConcurrentHashMap<String, String> getIncorrectTickerMap()
	{
		return incorrectTickerMap;
	}
	
	public boolean isIncorrectSymbol(String ticker)
	{
		return incorrectTickerMap.containsKey(ticker);
	}
	
	public String getChannelName(String symbol)
	{
		return subscribedSymbolMap.get(symbol).getChannelName();
	}

}
