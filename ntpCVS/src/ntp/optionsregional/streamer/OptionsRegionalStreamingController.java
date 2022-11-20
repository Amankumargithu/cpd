package ntp.optionsregional.streamer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.logger.NTPLogger;
import ntp.optionsregional.cache.OptionsRegionalQTMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.OptionsRegionalUtility;

public class OptionsRegionalStreamingController {

	private int numberOfChannels = 5;
	private int channelCount = 0;
	private OptionsRegionalStreamingChannel[] channelArray = null;
	private OptionsRegionalPreStreamingChannel[] preChannelArray = null;

	private volatile static OptionsRegionalStreamingController instance = new OptionsRegionalStreamingController();
	private CPDProperty prop = CPDProperty.getInstance();
	private OptionsRegionalQTMessageQueue cache = OptionsRegionalQTMessageQueue.getInstance();
	private ConcurrentHashMap<String, String> requestedSymbolMap = cache.getRequestedSymbolMap();

	private OptionsRegionalStreamingController(){}

	public static OptionsRegionalStreamingController getInstance() {
		if (instance == null)
			instance = new OptionsRegionalStreamingController();
		return instance;
	}

	public void initChannels()
	{
		int streamerChannelCount = 5;
		String preIP = null;
		int prePort = 4321;

		try{ streamerChannelCount = Integer.parseInt(prop.getProperty("STREAMER_CHANNEL_COUNT")); }
		catch(Exception e)
		{
			NTPLogger.missingProperty("STREAMER_CHANNEL_COUNT");
			streamerChannelCount = 6;
			NTPLogger.defaultSetting("STREAMER_CHANNEL_COUNT", ""+streamerChannelCount);
		}
		try{ preIP = prop.getProperty("PRE_UC_IP"); }
		catch(Exception e)
		{
			NTPLogger.missingProperty("PRE_UC_IP");
			preIP = "192.168.192.170";
			NTPLogger.defaultSetting("PRE_UC_IP", preIP);
		}
		try{ prePort = Integer.parseInt(prop.getProperty("PRE_UC_PORT")); }
		catch(Exception e)
		{
			NTPLogger.missingProperty("PRE_UC_PORT");
			prePort = 4321;
			NTPLogger.defaultSetting("PRE_UC_PORT", ""+prePort);
		}
		this.numberOfChannels = streamerChannelCount;
		channelArray = new OptionsRegionalStreamingChannel[numberOfChannels];
		preChannelArray = new OptionsRegionalPreStreamingChannel[numberOfChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			OptionsRegionalStreamingChannel channel = new OptionsRegionalStreamingChannel("OpRegStr " + (i + 1));
			OptionsRegionalPreStreamingChannel preChannel = new OptionsRegionalPreStreamingChannel("PreOpRegStr " +(i+1) , preIP, prePort);
			channelArray[i] = channel;
			preChannelArray[i] = preChannel;
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
		}, "OptionsRegionalSubscriber");
		subscriptionThread.start();
	}

	public void subscribeSymbol(String symbol)
	{
		symbol = OptionsRegionalUtility.getInstance().getUCFormattedTicker(symbol);
		if(cache.isSubscribedSymbol(symbol))
		{
//			NTPLogger.info("Already subscribed " + symbol + " to " + cache.getChannelName(symbol) );
			return;
		}
		if(symbol.contains("ULTRACACHE_NEW-ISSUES"))
		{
			++channelCount;
			int currentChannel = channelCount % numberOfChannels;
			channelArray[currentChannel].subscribe("ULTRACACHE_NEW-ISSUES");
			preChannelArray[currentChannel].subscribe("ULTRACACHE_NEW-ISSUES");
			cache.addSubscribedSymbol(symbol, channelArray[currentChannel], preChannelArray[currentChannel]);
			return;
		}

		if (!OptionsRegionalUtility.getInstance().validateOptionRegionalSymbol(symbol)) {
			NTPLogger.dropSymbol(symbol, "Invalid format");
			return;
		}
		++channelCount;
		int currentChannel = channelCount % numberOfChannels;
		channelArray[currentChannel].subscribe(symbol);
		preChannelArray[currentChannel].subscribe(symbol.replace("O:", "P:"));
		cache.addSubscribedSymbol(symbol, channelArray[currentChannel],preChannelArray[currentChannel]);
	}

	public void addRequestedSymbol(String s)
	{
		s = OptionsRegionalUtility.getInstance().getUCFormattedTicker(s);
		requestedSymbolMap.put(s, cache.BLANK);
	}

	public void addRequestedSymbols(HashSet<String> symbolSet)
	{
		if(symbolSet != null)
		{
			for(String s : symbolSet)
			{
				s = OptionsRegionalUtility.getInstance().getUCFormattedTicker(s);
				requestedSymbolMap.put(s, cache.BLANK);
			}
		}
	}
}
