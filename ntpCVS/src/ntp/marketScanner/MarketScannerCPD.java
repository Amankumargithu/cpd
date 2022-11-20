package ntp.marketScanner;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.b4utrade.bo.MarketScannerBO;
import com.csvreader.CsvReader;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.StockRetriever;

public class MarketScannerCPD {

	public static void main(String[] args) {
		CPDProperty prop = CPDProperty.getInstance();
		try {
			new RMIServer().startRMIServer();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String fundamentalFile = prop.getProperty("SCANNER_FUNDAMENTAL_FILE");
		ConcurrentHashMap<String, MarketScannerBO> dbCacheMap = MarketScannerCache.getInstance().getCacheMap();
		File fundFile = new File(fundamentalFile);
		if (fundFile.exists()) {
			CsvReader reader = null;
			try {
				reader = new CsvReader(fundFile.getAbsolutePath());
				reader.readHeaders();
				while (reader.readRecord()) {
					String ticker = reader.get("TICKER");
					MarketScannerBO bo = dbCacheMap.get(ticker);
					if (bo == null) {
						bo = new MarketScannerBO();
					}
					bo.setTicker(ticker);
					bo.setSharesOutStd(reader.get("SHARES_OUTSTANDING"));
					bo.setCompanyName(reader.get("DESCRIPTION"));
					bo.setSector(reader.get("SECTOR"));
					bo.setFiftyTwoWeekHighValue(reader.get("FIFTY_TWO_WEEK_HIGH"));
					bo.setFiftyTwoWeekLowValue(reader.get("FIFTY_TWO_WEEK_LOW"));
					bo.setTicker(ticker);
					dbCacheMap.put(ticker, bo);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null)
					reader.close();
			}
		}
		StockRetriever retriever = StockRetriever.getInstance();
		retriever.populateOtcTickers();
		retriever.populateTickerCache("BBO");
		HashSet<String> tickerSet = retriever.getTickerList();
		String omitTickerFile = prop.getProperty("OMIT_TICKER_FILE");
		File omitFile = new File(omitTickerFile);
		HashSet<String> omitTickerSet = new HashSet<>();
		if (omitFile.exists()) {
			CsvReader reader = null;
			try {
				reader = new CsvReader(omitFile.getAbsolutePath());
				while (reader.readRecord()) {
					String ticker = reader.get(0);
					omitTickerSet.add(ticker);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null)
					reader.close();
			}
		}
		tickerSet.removeAll(omitTickerSet);
		omitTickerSet.stream().forEach(dbCacheMap::remove);
		int numberofChannels = 6;
		try {
			numberofChannels = Integer.parseInt(prop.getProperty("NO_OF_CHANNELS"));
		} catch (Exception e) {
			NTPLogger.missingProperty("NO_OF_CHANNELS");
			NTPLogger.defaultSetting("NO_OF_CHANNELS", "6");
			numberofChannels = 6;
		}
		int tickersPerChannel;
		tickersPerChannel = tickerSet.size() / (numberofChannels);
		NTPLogger.info("Total number of tickers: " + tickerSet.size());
		NTPLogger.info("Tickers per channel: " + tickersPerChannel);
		Collection<String> tickerPerChannelList = new ArrayList<String>();
		int count = 0;
		int channelCount = 1;
		MarketScannerManager subscriptionManager = new MarketScannerManager(numberofChannels + 1);
		for (String ticker : tickerSet) {
			++count;
			tickerPerChannelList.add(ticker);
			if (count == tickersPerChannel && channelCount != numberofChannels) {
				NTPLogger.info("Number of tickers subscribed to channel " + channelCount + " = " + count);
				subscriptionManager.subscribeTickers(tickerPerChannelList);
				tickerPerChannelList = new ArrayList<String>();
				count = 0;
				channelCount++;
			}
		}
		if (count > 0) {
			NTPLogger.info("Tickers subscribed in last channel " + channelCount + " = " + count);
			subscriptionManager.subscribeTickers(tickerPerChannelList);
		}
		tickerPerChannelList = new ArrayList<String>();
		tickerPerChannelList.add("ULTRACACHE_NEW-ISSUES");
		NTPLogger.info("Subscribed ULTRACACHE_NEW-ISSUES in new Issues channel");
		subscriptionManager.subscribeTickers(tickerPerChannelList);
		NTPLogger.info("Complete ticker list subscribed on all channels");
	}
}
