package ntp.options.streamer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.OptionsUtility;
import ntp.util.StockRetriever;

public class OptionsStreamingController {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private OptionsStreamingChannel[] channelArray = null;
	private OptionsPreStreamingChannel[] preChannelArray = null;
	private OptionsQTMessageQueue cache = OptionsQTMessageQueue.getInstance();
	private CPDProperty prop = CPDProperty.getInstance();
	private static OptionsStreamingController instance = new OptionsStreamingController();
	private ConcurrentMap<String, String> requestedSymbolMap = cache.getRequestedSymbolMap();
	private OptionsUtility utilty = OptionsUtility.getInstance();

	public static OptionsStreamingController getInstance() {
		if (instance == null)
			instance = new OptionsStreamingController();
		return instance;
	}

	private OptionsStreamingController() {
	}

	public void initChannels() {
		int streamerChannelCount = 5;
		String preIP = null;
		int prePort = 4321;
		try {
			streamerChannelCount = Integer.parseInt(prop.getProperty("STREAMER_CHANNEL_COUNT"));
		} catch (Exception e) {
			NTPLogger.missingProperty("STREAMER_CHANNEL_COUNT");
			streamerChannelCount = 6;
			NTPLogger.defaultSetting("STREAMER_CHANNEL_COUNT", "" + streamerChannelCount);
		}
		try {
			preIP = prop.getProperty("PRE_UC_IP");
		} catch (Exception e) {
			NTPLogger.missingProperty("PRE_UC_IP");
			preIP = "192.168.192.170";
			NTPLogger.defaultSetting("PRE_UC_IP", preIP);
		}
		try {
			prePort = Integer.parseInt(prop.getProperty("PRE_UC_PORT"));
		} catch (Exception e) {
			NTPLogger.missingProperty("PRE_UC_PORT");
			prePort = 4321;
			NTPLogger.defaultSetting("PRE_UC_PORT", "" + prePort);
		}
		this.numberOfChannels = streamerChannelCount;
		channelArray = new OptionsStreamingChannel[numberOfChannels];
		preChannelArray = new OptionsPreStreamingChannel[numberOfChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			OptionsStreamingChannel channel = new OptionsStreamingChannel("OpStr" + (i + 1));
			channelArray[i] = channel;
			OptionsPreStreamingChannel preChannel = new OptionsPreStreamingChannel("PreOpStr" + (i + 1), preIP,
					prePort);
			preChannelArray[i] = preChannel;
		}

		// Equity Channel run - 1 channel for capturing and maintaining trade cache
		StockRetriever.getInstance().populateTickerCache("BBO");
		Collection<String> tickerList = StockRetriever.getInstance().getTickerList();
		String eqIp = null;
		int eqPort = 4321;
		try {
			eqIp = prop.getProperty("EQUITY_UC_IP");
		} catch (Exception e) {
			NTPLogger.missingProperty("EQUITY_UC_IP");
			eqIp = "192.168.192.176";
			NTPLogger.defaultSetting("EQUITY_UC_IP", eqIp);
		}
		try {
			eqPort = Integer.parseInt(prop.getProperty("EQUITY_UC_PORT"));
		} catch (Exception e) {
			NTPLogger.missingProperty("EQUITY_UC_PORT");
			eqPort = 4321;
			NTPLogger.defaultSetting("EQUITY_UC_PORT", "" + eqPort);
		}
		OptionsEquityStreamingChannel eqStrChannel = new OptionsEquityStreamingChannel("OpEqStr", eqIp, eqPort);
		eqStrChannel.subscribeTickers(tickerList);

		Thread subscriptionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (requestedSymbolMap.size() > 0) {
						Set<String> temp = new HashSet<>();
						temp.addAll(requestedSymbolMap.keySet());
						requestedSymbolMap.clear();
						for (String s : temp)
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

	public void subscribeSymbol(String symbol) {
		if (cache.isSubscribedSymbol(symbol)) {
			return;
		}
		if (symbol.contains("ULTRACACHE_NEW-ISSUES")) {
			++channelCount;
			int currentChannel = channelCount % numberOfChannels;
			channelArray[currentChannel].subscribe("ULTRACACHE_NEW-ISSUES");
			preChannelArray[currentChannel].subscribe("ULTRACACHE_NEW-ISSUES");
			cache.addSubscribedSymbol(symbol, channelArray[currentChannel], preChannelArray[currentChannel]);
			return;
		}
		if (!utilty.validateOptionSymbol(symbol)) {
			NTPLogger.dropSymbol(symbol, "Invalid format");
			return;
		}
		++channelCount;
		int currentChannel = channelCount % numberOfChannels;
		channelArray[currentChannel].subscribe(symbol);
		preChannelArray[currentChannel].subscribe(symbol.replace("O:", "P:"));
		cache.addSubscribedSymbol(symbol, channelArray[currentChannel], preChannelArray[currentChannel]);
	}

	public void addRequestedSymbols(HashSet<String> symbolSet) {
		if (symbolSet != null) {
			for (String s : symbolSet) {
				s = OptionsUtility.getInstance().getUCFormattedTicker(s);
				requestedSymbolMap.put(s, cache.BLANK);
			}
		}
	}

	public void addRequestedSymbol(String s) {
		s = OptionsUtility.getInstance().getUCFormattedTicker(s);
		requestedSymbolMap.put(s, cache.BLANK);
	}
}
