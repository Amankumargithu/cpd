package ntp.nb;

import java.util.ArrayList;
import java.util.Collection;

import ntp.ibus.IbusDispatcher;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.ExchangeMapPopulator;
import ntp.util.JVMMemoryMonitor;
import ntp.util.NTPConstants;
import ntp.util.StockRetriever;

public class NasdaqBasicCpd {

	public static void main(String[] args) {

		CPDProperty prop = CPDProperty.getInstance();
		int channelCount = 1;
		String [] insts;

		//Following line will initialize the EquitySubscripition's Streamer process which will ultimately push the data on streamer.
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("NBEquity", NbQTMessageQueue.getInstance().getmQueue());

		/*boolean isSubTypeTicker = prop.getProperty("SUBS_TYPE").equalsIgnoreCase("TICKERS");
		if(isSubTypeTicker)
		{
			NTPLogger.info("Equity CPD running in Ticker mode");
			String tickers = prop.getProperty("TICKERS");
			if(tickers == null)
			{
				NTPLogger.missingProperty("TICKERS");
				tickers = "AAPL";
				NTPLogger.defaultSetting("TICKERS", tickers);
			}
			insts = tickers.split(",");
			String snapIP = CPDProperty.getInstance().getProperty("SUBSCRIBE_IP");
			if(snapIP != null)
			{
				String splitStr[] = snapIP.split(":");
				NTPConstants.IP = splitStr[0];
				NTPConstants.PORT = Integer.parseInt(splitStr[1]);
				NTPLogger.info("Setting IP = " + NTPConstants.IP + " PORT = " + NTPConstants.PORT);
			}
			else
				NTPLogger.missingProperty("SUBSCRIBE_IP");
			ExchangeMapPopulator.getInstance();
			NbSubscriptionManager equitySubscriptionManager = new NbSubscriptionManager(1, isNasdaqbasic);
			equitySubscriptionManager.subscribeTickers(Arrays.asList(insts));
		}
		else*/
		//		{
		NTPLogger.info("Nasdaq Basic Equity CPD is running in Exchange mode");
		String subsIP = CPDProperty.getInstance().getProperty("TICKER_LIST_IP");
		if(subsIP != null)
		{
			String splitStr[] = subsIP.split(":");
			NTPConstants.IP = splitStr[0];
			NTPConstants.PORT = Integer.parseInt(splitStr[1]);	
			NTPLogger.info("Setting IP = " + NTPConstants.IP + " PORT = " + NTPConstants.PORT);
		}
		else
			NTPLogger.missingProperty("TICKER_LIST_IP");
		int numberofChannels = 6;
		try 
		{
			numberofChannels = Integer.parseInt(CPDProperty.getInstance().getProperty("NO_OF_CHANNELS"));
		} 
		catch (Exception e) {
			NTPLogger.missingProperty("NO_OF_CHANNELS");
			NTPLogger.defaultSetting("NO_OF_CHANNELS", "6");
			numberofChannels = 6;
		}
		String exchg = prop.getProperty("EXCHG");
		NTPLogger.info("Exchanges are: " + exchg);
		insts = exchg.split(",");
		ExchangeMapPopulator.getInstance();
		for (int i = 0; i < insts.length; i++) 
			StockRetriever.getInstance().populateTickerCache(insts [i]);
		StockRetriever.getInstance().populateNasdaqBasicTickers();
		Collection<String> tickerList = StockRetriever.getInstance().getTickerList();
		String snapIP = CPDProperty.getInstance().getProperty("SUBSCRIBE_IP");
		if(snapIP != null)
		{
			String splitStr[] = snapIP.split(":");
			NTPConstants.IP = splitStr[0];
			NTPConstants.PORT = Integer.parseInt(splitStr[1]);
			NTPLogger.info("Setting IP = " + NTPConstants.IP + " PORT = " + NTPConstants.PORT);
		}
		else
			NTPLogger.missingProperty("SUBSCRIBE_IP");
		NbSubscriptionManager equitySubscriptionManager = new NbSubscriptionManager(numberofChannels + 1);			
		int tickersPerChannel;
		tickersPerChannel = tickerList.size()/(numberofChannels);
		NTPLogger.info("Total number of tickers: " + tickerList.size());
		NTPLogger.info("Tickers per channel: " + tickersPerChannel);
		Collection<String> tickerPerChannelList = new ArrayList<String>();
		int count = 0;
		for (String ticker : tickerList) 
		{
			++count;
			tickerPerChannelList.add(ticker);
			if(count == tickersPerChannel && channelCount != numberofChannels)
			{
				NTPLogger.info("Number of tickers subscribed to channel " + channelCount + " = " + count);
				equitySubscriptionManager.subscribeTickers(tickerPerChannelList);
				tickerPerChannelList = new ArrayList<String>();
				count = 0;
				channelCount++;
			}
		}
		if(count > 0 )
		{
			NTPLogger.info("Tickers subscribed in last channel " + channelCount + " = " + count);
			equitySubscriptionManager.subscribeTickers(tickerPerChannelList);  
		}
		tickerPerChannelList = new ArrayList<String>();
		tickerPerChannelList.add("ULTRACACHE_NEW-ISSUES");
		NTPLogger.info("Subscribed ULTRACACHE_NEW-ISSUES in new Issues channel");
		equitySubscriptionManager.subscribeTickers(tickerPerChannelList);
		channelCount = 0;
		Collection<String> volumePlusSymbols = StockRetriever.getInstance().getVolumePlusSymbols();
		int volumePlusTickersPerChannel;
		volumePlusTickersPerChannel = volumePlusSymbols.size()/(numberofChannels);
		NTPLogger.info("Total number of tickers: " + volumePlusSymbols.size());
		NTPLogger.info("Tickers per channel: " + volumePlusTickersPerChannel);
		Collection<String> volumeTickerPerChannelList = new ArrayList<String>();
		count = 0;
		for (String ticker : volumePlusSymbols) 
		{
			++count;
			volumeTickerPerChannelList.add(ticker);
			if(count == volumePlusTickersPerChannel && channelCount != numberofChannels)
			{
				NTPLogger.info("Number of tickers subscribed to channel " + channelCount + " = " + count);
				equitySubscriptionManager.subscribeVolumePlusTickers(volumeTickerPerChannelList, channelCount);
				volumeTickerPerChannelList = new ArrayList<String>();
				count = 0;
				channelCount++;
			}
		}
		if(count > 0 )
		{
			NTPLogger.info("Tickers subscribed in last channel " + channelCount + " = " + count);
			equitySubscriptionManager.subscribeVolumePlusTickers(volumeTickerPerChannelList, channelCount);
		}
		//		}
		NTPLogger.info("Complete ticker list subscribed on all channels");
		//############# Static data requests ##############
		String runMemoryMonitor = CPDProperty.getInstance().getProperty("SHOULD_MONITOR_MEMORY");
		if(runMemoryMonitor != null && runMemoryMonitor.equalsIgnoreCase("true"))
		{
			NTPLogger.info("JVM memory monitor thread started");
			new Thread(new JVMMemoryMonitor()).start();
		}
	}
}
