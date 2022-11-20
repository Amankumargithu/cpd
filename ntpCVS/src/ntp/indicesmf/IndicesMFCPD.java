package ntp.indicesmf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ntp.ibus.IbusDispatcher;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.IndicesMFTickersPopulator;

public class IndicesMFCPD {

	public IndicesMFCPD() {
	}

	public static void main(String[] args) {

		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("Indices", IndicesMFQTMessageQueue.getInstance().getmQueue());

		CPDProperty prop = CPDProperty.getInstance();
		int channelCount = 1;
		String subscritionType = prop.getProperty("SUBS_TYPE");
		if (subscritionType == null || subscritionType.isEmpty())
			subscritionType = "EXCHANGE";
		else
			subscritionType = subscritionType.toUpperCase();
		Set<String> tickerList = new HashSet<>();

		switch (subscritionType) {
		case "TICKERS": {
			NTPLogger.info("Running IndicesMf CPD running in ticker mode");
			String tickers = prop.getProperty("TICKERS");
			if (tickers == null) {
				NTPLogger.missingProperty("TICKERS");
				tickers = "I:DJI";
				NTPLogger.defaultSetting("TICKERS", tickers);
			}
			String[] tkrs = tickers.split(",");
			for (String tick : tkrs)
				tickerList.add(tick);
		}
			break;
		case "META": {
			IndicesMetaDataUtility metaUtility = new IndicesMetaDataUtility();
			tickerList = metaUtility.getMetaTickerList();
		}
			break;
		default: {
			NTPLogger.info("IndicesMF CPD is running in Exchange mode");
			String tickerType = "I,M,B";
			try {
				tickerType = prop.getProperty("TICKER_TYPE");
			} catch (Exception e) {
				NTPLogger.missingProperty("TICKER_TYPE");
				tickerType = "I,M,B";
				NTPLogger.defaultSetting("TICKER_TYPE", tickerType);
			}
			NTPLogger.info("Ticker types are " + tickerType);
			String[] types = tickerType.split(",");
			for (String s : types) {
				if (s.equalsIgnoreCase("I"))
					IndicesMFTickersPopulator.getInstance().populateIndicesTickers();
				else if (s.equalsIgnoreCase("M"))
					IndicesMFTickersPopulator.getInstance().populateMFTickers();
				else if (s.equalsIgnoreCase("B")) {
					Thread bondThread = new BondsDataFileWriter();
					bondThread.setName("BondWriterThread");
					bondThread.start();
					IndicesMFTickersPopulator.getInstance().populateBondsTickers();
				}
			}
			tickerList = IndicesMFTickersPopulator.getInstance().getAllTickers();
		}
			break;
		}
		int numberofChannels = 6;
		try {
			numberofChannels = Integer.parseInt(prop.getProperty("NO_OF_CHANNELS"));
		} catch (Exception e) {
			NTPLogger.missingProperty("NO_OF_CHANNELS");
			numberofChannels = 6;
			NTPLogger.defaultSetting("NO_OF_CHANNELS", "" + numberofChannels);
		}
		IndicesMFSubscriptionManager indicesSubscriptionManager = new IndicesMFSubscriptionManager(numberofChannels);
		int tickersPerChannel;
		tickersPerChannel = tickerList.size() / (numberofChannels);
		NTPLogger.info("Total number of tickers: " + tickerList.size());
		NTPLogger.info("Tickers per channel: " + tickersPerChannel);
		ArrayList<String> tickerPerChannelList = new ArrayList<>();
		int count = 0;
		for (String ticker : tickerList) {
			++count;
			tickerPerChannelList.add(ticker);
			if (count == tickersPerChannel && channelCount != numberofChannels) {
				NTPLogger.info("Number of tickers subscribed to channel " + channelCount + " = " + count);
				indicesSubscriptionManager.subscribeTickers(tickerPerChannelList);
				tickerPerChannelList = new ArrayList<>();
				count = 0;
				channelCount++;
			}
		}
		tickerPerChannelList.add("ULTRACACHE_NEW-ISSUES");
		// For reminder or remaining tickers
		NTPLogger.info("Tickers subscribed in last channel " + channelCount + " = " + count);
		indicesSubscriptionManager.subscribeTickers(tickerPerChannelList);

		NTPLogger.info("Complete ticker list subscribed on all channels");
	}
}
