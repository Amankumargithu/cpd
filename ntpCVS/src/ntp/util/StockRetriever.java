package ntp.util;

import java.util.HashSet;

import ntp.logger.NTPLogger;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class StockRetriever {
	private static final int TICKER_POS = 0;
	private HashSet<String> tickersList = new HashSet<String>();
	private static volatile StockRetriever instance = new StockRetriever();
	private HashSet<String> volumePlusSymbols = new HashSet<>();

	private StockRetriever() {
	}

	public static StockRetriever getInstance() {
		return instance;
	}

	public void populateNasdaqBasicTickers() {
		String exchange = "BBO";
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "ExchTick", "ExchTick", false);
		NTPLogger.connectChannel("ExchTick", NTPConstants.IP);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do {
			NTPLogger.requestUC("SyncGetExchTickers", exchange, ++resubscribeCount);
			table = query.SyncGetExchTickers(exchange, new Object());
			NTPLogger.responseUC("SyncGetExchTickers", exchange, table == null ? 0 : table.nRow());
		} while (table != null && table.len() == 0 && resubscribeCount < 3);
		if (table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetExchTickers", resubscribeCount);
		query.Stop();
		NTPLogger.disconnectChannel("ExchTick");
		int rowCount = table.nRow();
//		int duplicateCount=0;
		for (int count = 0; count < rowCount; count++) {
			String ticker = null;
			try {

				ticker = table.GetCell(count, TICKER_POS);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (ticker == null ||ticker.length()==0)
				continue;
			if (ticker.startsWith("Q:")) {
				tickersList.remove(ticker);
				tickersList.remove(ticker.substring(2));
				volumePlusSymbols.add(ticker.substring(2));
				tickersList.add(ticker + "/T");
			}
			if (ticker.startsWith("T:")) {
				tickersList.add(ticker);
//				tickersList.remove(ticker);
				volumePlusSymbols.add(ticker.substring(2));
//				tickersList.add(ticker + "/D1");
			}
			/*
			 * if (tickersList.contains(ticker)) { NTPLogger.info("Duplicate: " + ticker +
			 * " Exchange: " + exchange); duplicateCount++; }
			 */
//			tickersList.add(ticker);
		}
//		NTPLogger.info("Total Duplicate count: " + duplicateCount + " for exchange " + exchange);		
	}

	public void populateOtcTickers() {
		String exchange = "BBO";
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "ExchTick", "ExchTick", false);
		NTPLogger.connectChannel("ExchTick", NTPConstants.IP);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do {
			NTPLogger.requestUC("SyncGetExchTickers", exchange, ++resubscribeCount);
			table = query.SyncGetExchTickers(exchange, new Object());
			NTPLogger.responseUC("SyncGetExchTickers", exchange, table == null ? 0 : table.nRow());
		} while (table != null && table.len() == 0 && resubscribeCount < 3);
		if (table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetExchTickers", resubscribeCount);
		query.Stop();
		NTPLogger.disconnectChannel("ExchTick");
		int rowCount = table.nRow();
		int duplicateCount = 0;
		HashSet<String> otcExchanges = OTCExchangeMapPopulator.getInstance().getExchanges();
		for (int count = 0; count < rowCount; count++) {
			String ticker = null;
			try {

				ticker = table.GetCell(count, TICKER_POS);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (ticker == null ||ticker.length()==0 || ticker.endsWith(".CA"))
				continue;
			String marketCenter = table.GetCell(count, 2);
			if (marketCenter != null && marketCenter.trim().length() > 0 && otcExchanges.contains(marketCenter))
				ticker = ticker + "/" + marketCenter.trim();
			if (tickersList.contains(ticker)) {
				NTPLogger.info("Duplicate: " + ticker + " Exchange: " + exchange);
				duplicateCount++;
			}
			tickersList.add(ticker);
		}
		NTPLogger.info("Total Duplicate count: " + duplicateCount + " for exchange " + exchange);
	}

	public void populateTickerCache(String exchange) {
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "ExchTick", "ExchTick", false);
		NTPLogger.connectChannel("ExchTick", NTPConstants.IP);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do {
			NTPLogger.requestUC("SyncGetExchTickers", exchange, ++resubscribeCount);
			table = query.SyncGetExchTickers(exchange, new Object());
			NTPLogger.responseUC("SyncGetExchTickers", exchange, table == null ? 0 : table.nRow());
		} while (table != null && table.len() == 0 && resubscribeCount < 3);
		if (table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetExchTickers", resubscribeCount);
		query.Stop();
		NTPLogger.disconnectChannel("ExchTick");
		parseBlobTable(exchange, table);
	}

	private void parseBlobTable(String exchange, BlobTable blobTable) {
		int rowCount = blobTable.nRow();
		int duplicateCount = 0;
		for (int count = 0; count < rowCount; count++) {
			String ticker = null;
			try {

				ticker = blobTable.GetCell(count, TICKER_POS);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (ticker == null ||ticker.length()==0 || ticker.endsWith(".CA"))
				continue;
			if (tickersList.contains(ticker)) {
				NTPLogger.info("Duplicate: " + ticker + " Exchange: " + exchange);
				duplicateCount++;
			}
			tickersList.add(ticker);
		}
		NTPLogger.info("Total Duplicate count: " + duplicateCount + " for exchange " + exchange);
	}

	public HashSet<String> getTickerList() {
		return tickersList;
	}

	public HashSet<String> getVolumePlusSymbols() {
		return volumePlusSymbols;
	}
}
