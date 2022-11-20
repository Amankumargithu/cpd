package ntp.tsqstr;

import java.util.concurrent.ConcurrentHashMap;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class TSQSubscriptionManager{

	private static volatile TSQSubscriptionManager instance = new TSQSubscriptionManager();
	private TSQStreamingChannel channelArray[] = null;
	private int numberOfChannels = 4;
	private int channelCount = 0;
	private ConcurrentHashMap<String, String> tickerMap = new ConcurrentHashMap<>();

	private TSQSubscriptionManager() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				NTPLogger.info("Start of shutDownHook");
				stopProcess();
				NTPLogger.info("Finish of shutDownHook");
			}
		});
	}

	public static TSQSubscriptionManager getInstance(){
		return instance;
	}

	public void subscribeTSQSymbol(String ticker)
	{
		if(tickerMap.contains(ticker))
			return;
		channelCount = channelCount % numberOfChannels;
		TSQStreamingChannel channel = channelArray[channelCount];
		channelCount++;
		channel.subscribeTSQSymbol(ticker);
		tickerMap.put(ticker, ticker);
	}

	public void initChannels() 
	{
		try { numberOfChannels = Integer.parseInt(CPDProperty.getInstance().getProperty("NUMBER_OF_SUBSCRIPTION_CHANNELS")); }
		catch (Exception e) 
		{
			NTPLogger.missingProperty("NUMBER_OF_SUBSCRIPTION_CHANNELS");
			numberOfChannels = 4;
			NTPLogger.defaultSetting("NUMBER_OF_SUBSCRIPTION_CHANNELS", "" + numberOfChannels);
		}
		channelArray = new TSQStreamingChannel[numberOfChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			TSQStreamingChannel channel = new TSQStreamingChannel("TSQstr " + i);
			channelArray[i] = channel;
		}
	}

	private void stopProcess(){
		for (int i = 0; i < numberOfChannels; i++) {
			channelArray[i].stopLoggingThread();
			channelArray[i].Stop();
		}
	}
}
