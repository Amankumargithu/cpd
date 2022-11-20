package ntp.otc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import ntp.equity.subs.EquityQTMessageQueue;
import ntp.ibus.IbusDispatcher;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.ExchangeMapPopulator;
import ntp.util.JVMMemoryMonitor;
import ntp.util.NTPConstants;
import ntp.util.StockRetriever;

/**
 * @author Ankit
 * 
 * This CPD will only get ticker list from BBO exchange 
 * as it require only protocol 9 and 10 trades to stream, excluding pink sheet 
 * 
 */
public class OtcCPD {

	public static void main(String[] args) {
		CPDProperty prop = CPDProperty.getInstance();
		int channelCount = 1;

		//Following line will initialize the EquitySubscripition's Streamer process which will ultimately push the data on streamer.
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("Equity", EquityQTMessageQueue.getInstance().getmQueue());
		
		boolean isSubTypeTicker = prop.getProperty("SUBS_TYPE").equalsIgnoreCase("TICKERS");
		if(isSubTypeTicker)
		{
			NTPLogger.info("OTCEquity CPD running in Ticker mode");
			String tickers = prop.getProperty("TICKERS");
			if(tickers == null)
			{
				NTPLogger.missingProperty("TICKERS");
				tickers = "NSRGY";
				NTPLogger.defaultSetting("TICKERS", tickers);
			}
			String [] insts = tickers.split(",");
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
			OtcSubscriptionManager otcSubscriptionManager = new OtcSubscriptionManager(1);
			otcSubscriptionManager.subscribeTickers(Arrays.asList(insts));
		}
		else
		{
			NTPLogger.info("OTCEquity CPD is running in Exchange mode");
			String subsIP = CPDProperty.getInstance().getProperty("TICKER_LIST_IP");
			if(subsIP != null)
			{
				NTPLogger.info("TICKER_LIST_IP: " + subsIP );
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
			/*String exchg = prop.getProperty("EXCHG");
			NTPLogger.info("Exchanges are: " + exchg);
			String[] insts = exchg.split(",");
			ExchangeMapPopulator.getInstance();
			for (int i = 0; i < insts.length; i++) 
				StockRetriever.getInstance().populateTickerCache(insts [i]);*/
			StockRetriever.getInstance().populateOtcTickers();
			Collection<String> tickerList = StockRetriever.getInstance().getTickerList();
			String snapIP = CPDProperty.getInstance().getProperty("SUBSCRIBE_IP");
			if(snapIP != null)
			{
				NTPLogger.info("SUBSCRIBE_IP: " + snapIP );
				String splitStr[] = snapIP.split(":");
				NTPConstants.IP = splitStr[0];
				NTPConstants.PORT = Integer.parseInt(splitStr[1]);
				NTPLogger.info("Setting IP = " + NTPConstants.IP + " PORT = " + NTPConstants.PORT);
			}
			else
				NTPLogger.missingProperty("SUBSCRIBE_IP");
			OtcSubscriptionManager otcSubscriptionManager = new OtcSubscriptionManager(numberofChannels + 1);			
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
					otcSubscriptionManager.subscribeTickers(tickerPerChannelList);
					tickerPerChannelList = new ArrayList<String>();
					count = 0;
					channelCount++;
				}
			}
			if(count > 0 )
			{
				//For reminder or remaining tickers
				NTPLogger.info("Tickers subscribed in last channel " + channelCount + " = " + count);
				otcSubscriptionManager.subscribeTickers(tickerPerChannelList);  
			}
			tickerPerChannelList = new ArrayList<String>();
			tickerPerChannelList.add("ULTRACACHE_NEW-ISSUES");
			otcSubscriptionManager.subscribeTickers(tickerPerChannelList);
			NTPLogger.info("Subscribed ULTRACACHE_NEW-ISSUES in new Issues channel");
		}
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
