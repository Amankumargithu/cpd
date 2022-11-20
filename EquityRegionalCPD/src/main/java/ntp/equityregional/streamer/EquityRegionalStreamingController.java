package ntp.equityregional.streamer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.equityregional.cache.EquityRegionalQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPTickerValidator;

public class EquityRegionalStreamingController {

	private int numberOfChannels = 5;
	private int channelCount = 0;
	private EquityRegionalStreamingChannel[] channelArray = null;
	private volatile static EquityRegionalStreamingController instance = new EquityRegionalStreamingController();
	private CPDProperty prop = CPDProperty.getInstance();
	private EquityRegionalQTMessageQueue cache = EquityRegionalQTMessageQueue.getInstance();
	private ConcurrentHashMap<String, String> requestedSymbolMap = cache.getRequestedSymbolMap();

	private EquityRegionalStreamingController(){}

	public static EquityRegionalStreamingController getInstance() {
		if (instance == null)
			instance = new EquityRegionalStreamingController();
		return instance;
	}

	public void initChannels()
	{
		int streamerChannelCount = 5;
		try{ streamerChannelCount = Integer.parseInt(prop.getProperty("STREAMER_CHANNEL_COUNT")); }
		catch(Exception e)
		{
			NTPLogger.missingProperty("STREAMER_CHANNEL_COUNT");
			streamerChannelCount = 6;
			NTPLogger.defaultSetting("STREAMER_CHANNEL_COUNT", ""+streamerChannelCount);
		}
		this.numberOfChannels = streamerChannelCount;
		channelArray = new EquityRegionalStreamingChannel[numberOfChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			EquityRegionalStreamingChannel channel = new EquityRegionalStreamingChannel("EqRegStr " + (i + 1));	
			channelArray[i] = channel;
		}	
		Thread subscriptionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true)
				{
					if(requestedSymbolMap.size() > 0)
					{
						Set<String> temp = new HashSet<>();
						temp.addAll(requestedSymbolMap.keySet());
						requestedSymbolMap.clear();
						for(String s : temp)
							subscribeSymbol(s);
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "EquityRegionalSubscriber");
		subscriptionThread.start();
	}

	public void subscribeSymbol(String symbol)
	{
		if(cache.isSubscribedSymbol(symbol))
		{
			NTPLogger.info("Already subscribed " + symbol + " to " + cache.getChannelName(symbol) );
			return;
		}
		if(symbol.contains("ULTRACACHE_NEW-ISSUES"))
		{
			++channelCount;
			int currentChannel = channelCount % numberOfChannels;
			channelArray[currentChannel].subscribe("ULTRACACHE_NEW-ISSUES");
			cache.addSubscribedSymbol(symbol, channelArray[currentChannel]);
			return;
		}
		if (!NTPTickerValidator.validationSucceeded(symbol)) {
			NTPLogger.dropSymbol(symbol, "Invalid format");
			return;
		}
		++channelCount;
		int currentChannel = channelCount % numberOfChannels;
		channelArray[currentChannel].subscribe(symbol);
		cache.addSubscribedSymbol(symbol, channelArray[currentChannel]);
	}

	public void addRequestedSymbol(String s)
	{
		requestedSymbolMap.put(s, cache.BLANK);
	}
	
	public void addRequestedSymbols(HashSet<String> symbolSet)
	{
		if(symbolSet != null)
		{
			for(String s : symbolSet)
				requestedSymbolMap.put(s, cache.BLANK);
		}
	}
}
