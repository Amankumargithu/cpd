package ntp.futureOptions.streamer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.futureOptions.cache.FutureOptionsQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.FuturesOptionsUtility;

public class FutureOptionsStreamingController {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private FutureOptionsStreamingChannel[] channelArray = null;

	private FutureOptionsQTMessageQueue cache = FutureOptionsQTMessageQueue.getInstance();
	private CPDProperty prop = CPDProperty.getInstance();
	private volatile static FutureOptionsStreamingController instance = new FutureOptionsStreamingController();
	private ConcurrentHashMap<String, String> requestedSymbolMap = cache.getRequestedSymbolMap();
	private FuturesOptionsUtility utilty = FuturesOptionsUtility.getInstance();

	public static FutureOptionsStreamingController getInstance() {
		if (instance == null)
			instance = new FutureOptionsStreamingController();
		return instance;
	}

	private FutureOptionsStreamingController(){}

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
		channelArray = new FutureOptionsStreamingChannel[numberOfChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			FutureOptionsStreamingChannel channel = new FutureOptionsStreamingChannel("FutOpStr" + (i + 1));	
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
		}, "OptionSubscriber");
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
		if(!utilty.validateFutureOptionSymbol(symbol))
		{
			NTPLogger.dropSymbol(symbol, "Invalid format");
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
//				s = utilty.getUCFormattedTicker(s);
				requestedSymbolMap.put(s, cache.BLANK);
			}
		}
	}

	public void addRequestedSymbol(String s)
	{
//		s = utilty.getUCFormattedTicker(s);
		requestedSymbolMap.put(s, cache.BLANK);
	}
}
