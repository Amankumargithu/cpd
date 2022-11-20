package ntp.optionsregional.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.optionsregional.streamer.OptionsRegionalPreStreamingChannel;
import ntp.optionsregional.streamer.OptionsRegionalStreamingChannel;
import ntp.queue.MappedMessageQueue;
import ntp.util.OptionsRegionalUtility;

public class OptionsRegionalQTMessageQueue {

	private Map<String, QTCPDMessageBean> subsData = new HashMap<String, QTCPDMessageBean>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private static OptionsRegionalQTMessageQueue qtMessageQueue = new OptionsRegionalQTMessageQueue();
	private ConcurrentHashMap<String, String> requestedSymbolMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, OptionsRegionalStreamingChannel> subscribedSymbolMap = new ConcurrentHashMap<String, OptionsRegionalStreamingChannel>();
	private ConcurrentHashMap<String, OptionsRegionalPreStreamingChannel> preSubscribedSymbolMap = new ConcurrentHashMap<String, OptionsRegionalPreStreamingChannel>();
	private ConcurrentHashMap<String, String> imageTickerMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> incorrectTickerMap = new ConcurrentHashMap<String, String>();

	public final String BLANK = "";

	private OptionsRegionalQTMessageQueue() {}

	public static OptionsRegionalQTMessageQueue getInstance(){
		return qtMessageQueue;
	}

	public ConcurrentHashMap<String, String> getRequestedSymbolMap()
	{
		return requestedSymbolMap;
	}

	public Map<String, QTCPDMessageBean> getSubsData() {
		return subsData;
	}

	public MappedMessageQueue getmQueue() {
		return mQueue;
	}

	public boolean isSubscribedSymbol(String symbol)
	{
		if(subscribedSymbolMap.containsKey(symbol))
			return true;
		return false;
	}

	public void addSubscribedSymbol(String symbol, OptionsRegionalStreamingChannel channel,OptionsRegionalPreStreamingChannel preChannel)
	{
		subscribedSymbolMap.put(symbol, channel);
		preSubscribedSymbolMap.put(symbol, preChannel);
	}

	public String getChannelName(String symbol)
	{
		return subscribedSymbolMap.get(symbol).getChannelName();
	}

	public ConcurrentHashMap<String, String> getImageTickerMap()
	{
		return imageTickerMap;
	}

	public ConcurrentHashMap<String, String> getIncorrectTickerMap()
	{
		return incorrectTickerMap;
	}

	public boolean isIncorrectSymbol(String ticker)
	{
		ticker = OptionsRegionalUtility.getInstance().getUCFormattedTicker(ticker);
		return incorrectTickerMap.containsKey(ticker);
	}

	public synchronized QTCPDMessageBean getCachedBean(String ticker)
	{
		Map<String, QTCPDMessageBean> qtMap = OptionsRegionalQTMessageQueue.getInstance().getSubsData();
		QTCPDMessageBean cachedBean = qtMap.get(ticker);
		if (cachedBean == null){
			cachedBean = new QTCPDMessageBean();
			qtMap.put(ticker,cachedBean);
		}
		return cachedBean;
	}
}
