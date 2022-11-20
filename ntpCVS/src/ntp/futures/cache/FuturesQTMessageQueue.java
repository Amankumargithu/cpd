package ntp.futures.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.futures.streamer.FuturesStreamingChannel;
import ntp.queue.MappedMessageQueue;

public class FuturesQTMessageQueue {

	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private static FuturesQTMessageQueue qtMessageQueue = new FuturesQTMessageQueue();
	private Map<String, QTCPDMessageBean> subsData = new ConcurrentHashMap<String, QTCPDMessageBean>();
	private ConcurrentHashMap<String, String> imageTickerMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> incorrectTickerMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, FuturesStreamingChannel> subscribedSymbolMap = new ConcurrentHashMap<String, FuturesStreamingChannel>();
	public final String BLANK = ""; 

	public static FuturesQTMessageQueue getInstance(){
		return qtMessageQueue;
	}
	public MappedMessageQueue getmQueue() {
		return mQueue;
	}
	public void setmQueue(MappedMessageQueue mQueue) {
		this.mQueue = mQueue;
	}	
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
	public void addSubscribedSymbol(String symbol, FuturesStreamingChannel channel)
	{
		subscribedSymbolMap.put(symbol, channel);
	}
	public boolean isSubscribedSymbol(String symbol)
	{
		if(subscribedSymbolMap.containsKey(symbol))
			return true;
		return false;
	}
	public String getChannelName(String symbol)
	{
		return subscribedSymbolMap.get(symbol).getChannelName();
	}	
	public boolean isIncorrectSymbol(String ticker)
	{
		return incorrectTickerMap.containsKey(ticker);
	}
}
