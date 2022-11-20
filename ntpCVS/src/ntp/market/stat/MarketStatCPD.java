package ntp.market.stat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

import ntp.ibus.IbusDispatcher;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;

import com.csvreader.CsvReader;

public class MarketStatCPD {

	public static void main(String[] args) {
		try {
			MarketStatChannel subscriptionManager = MarketStatChannel.getInstance();
			CPDProperty prop = CPDProperty.getInstance();
			// Following line will initialize the EquitySubscripition's Streamer process
			// which will ultimately push the data on streamer.
			IbusDispatcher dispacther = new IbusDispatcher();
			dispacther.startDispatcher("MktStat", MarketStatCache.getInstance().getmQueue());

			String snapIP = CPDProperty.getInstance().getProperty("SNAP_IP");
			if (snapIP != null) {
				String splitStr[] = snapIP.split(":");
				NTPConstants.IP = splitStr[0];
				NTPConstants.PORT = Integer.parseInt(splitStr[1]);
				NTPLogger.info("Setting IP = " + NTPConstants.IP + " PORT = " + NTPConstants.PORT);
			}
			String metaFilePath = prop.getProperty("META_FILE_PATH");
			if (metaFilePath == null) {
				NTPLogger.missingProperty("META_FILE_PATH");
				metaFilePath = "/home/mkt_stat/dataFile";
				NTPLogger.defaultSetting("META_FILE_PATH", metaFilePath);
			}
			HashSet<String> ignoredTickerSet = new HashSet<>();
			String ignoredTickers = CPDProperty.getInstance().getProperty("IGNORED_TICKERS");
			if (ignoredTickers != null) {
				String[] tempArr = ignoredTickers.split(",");
				for (String s : tempArr)
					ignoredTickerSet.add(s);
			}
			String primaryFileName = metaFilePath + "/outputPrimaryMcEquity.csv";
			NTPLogger.info("Started parsing " + primaryFileName);
			CsvReader primaryFileReader = new CsvReader(primaryFileName);
			ArrayList<String> nyseTickerList = new ArrayList<>();
			ArrayList<String> nasdaqTickerList = new ArrayList<>();
			ArrayList<String> amexTickerList = new ArrayList<>();
			ArrayList<String> nyseRegionalTickerList = new ArrayList<>();
			while (primaryFileReader.readRecord()) {
				String ticker = primaryFileReader.get(0);
				if (ignoredTickerSet.contains(ticker))
					continue;
				String exchange = primaryFileReader.get(1);
				switch (exchange) {
				case "N":
					nyseTickerList.add(ticker);
					nyseRegionalTickerList.add(ticker + "/N");
					break;
				case "T":
					nasdaqTickerList.add(ticker);
					break;
				case "A":
					amexTickerList.add(ticker);
					break;
				default:
					break;
				}
			}
			primaryFileReader.close();

			ArrayList<String> subscriptionList = new ArrayList<>();
			NASDAQMarketStatUpdater.getInstance().addAllTickers(nasdaqTickerList);
			NTPLogger.info("Total ticker subscribing under NASDAQ " + nasdaqTickerList.size());
			NYSEMarketStatUpdater.getInstance().addAllTickers(nyseTickerList);
			NTPLogger.info("Total ticker subscribing under NYSE " + nyseTickerList.size());
			AMEXMarketStatUpdater.getInstance().addAllTickers(amexTickerList);
			NTPLogger.info("Total ticker subscribing under AMEX " + amexTickerList.size());
			subscriptionList.addAll(nasdaqTickerList);
			subscriptionList.addAll(nyseTickerList);
			subscriptionList.addAll(amexTickerList);
			subscriptionList.addAll(MarketStatTickerManager.getDJIATickers());
			NTPLogger.info("Adding DJIA tickers " + MarketStatTickerManager.getDJIATickers());
			NYSERegionalMarketStatUpdater.getInstance().addAllTickers(nyseRegionalTickerList);
			subscriptionList.addAll(nyseRegionalTickerList);
			NTPLogger.info("Total ticker subscribing under NYSE Regional " + nyseTickerList.size());

			String subsIP = CPDProperty.getInstance().getProperty("SUBS_IP");
			if (subsIP != null) {
				String splitStr[] = subsIP.split(":");
				NTPConstants.IP = splitStr[0];
				NTPConstants.PORT = Integer.parseInt(splitStr[1]);
				NTPLogger.info("Setting IP = " + NTPConstants.IP + " PORT = " + NTPConstants.PORT);
			}
			String tradedListFilePath = "/home/mkt_stat/traded_list.txt";
			File tradedFile = new File(tradedListFilePath);
			if (tradedFile.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(tradedFile));) {
					String tradedTick = null;
					while ((tradedTick = reader.readLine()) != null) {
						if (tradedTick.length() > 0)
							MarketStatUpdater.getInstance().addTickertoTradedList(tradedTick);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			NTPLogger.info("Total Tickers in list: " + subscriptionList.size());
			subscriptionManager.subscribeTickers(subscriptionList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
