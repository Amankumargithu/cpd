package ntp.futures.streamer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.futures.cache.FuturesQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;


public class FuturesStreamingController {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private FuturesStreamingChannel[] channelArray = null;

	private FuturesQTMessageQueue cache = FuturesQTMessageQueue.getInstance();
	private CPDProperty prop = CPDProperty.getInstance();
	private volatile static FuturesStreamingController instance = new FuturesStreamingController();
	private ConcurrentHashMap<String, String> requestedSymbolMap = new ConcurrentHashMap<String, String>();

	public static FuturesStreamingController getInstance() {
		if (instance == null)
			instance = new FuturesStreamingController();
		return instance;
	}

	private FuturesStreamingController(){}

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
		channelArray = new FuturesStreamingChannel[numberOfChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			FuturesStreamingChannel channel = new FuturesStreamingChannel("FutStr " + (i + 1));	
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
		}, "FutureSubscriber");
		subscriptionThread.start();
	}

	public void subscribeSymbol(String symbol)
	{
		if(cache.isSubscribedSymbol(symbol))
		{
			NTPLogger.info("Already subscribed " + symbol + " to " + cache.getChannelName(symbol) );
			return;
		}
		++channelCount;
		int currentChannel = channelCount % numberOfChannels;
		channelArray[currentChannel].subscribe(symbol);
		cache.addSubscribedSymbol(symbol, channelArray[currentChannel]);
	}

	public void addRequestedSymbols(HashSet<String> symbolSet)
	{
		if(symbolSet != null)
		{
			for(String s : symbolSet)
			{
				requestedSymbolMap.put(s, cache.BLANK);
			}
		}
	}

	public void addRequestedSymbol(String symbol)
	{
		if(symbol != null)
			requestedSymbolMap.put(symbol, cache.BLANK);
	}
}
