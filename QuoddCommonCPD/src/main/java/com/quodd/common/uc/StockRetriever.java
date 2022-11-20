package com.quodd.common.uc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import com.quodd.common.logger.CommonLogMessage;
import com.quodd.common.logger.QuoddLogger;

import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class StockRetriever {
	private static final int TICKER_POS = 0;
	private HashSet<String> tickersList = new HashSet<>();
	private HashSet<String> volumePlusSymbols = new HashSet<>();
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private String ip;
	private int port;

	public StockRetriever(String ip, int port) {
		this.ip = Objects.requireNonNull(ip);
		this.port = port;
	}

	public void populateNasdaqBasicTickers() {
		String exchange = "BBO";
		UltraChan query = new UltraChan(this.ip, this.port, "ExchTick", "ExchTick", false);
		this.logger.info(CommonLogMessage.connectChannel("ExchTick", this.ip));
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do {
			this.logger.info(CommonLogMessage.requestUC("SyncGetExchTickers", exchange, ++resubscribeCount));
			table = query.SyncGetExchTickers(exchange, new Object());
			this.logger.info(
					CommonLogMessage.responseUC("SyncGetExchTickers", exchange, table == null ? 0 : table.nRow()));
		} while (table != null && table.len() == 0 && resubscribeCount < 3);
		if (table != null && table.len() == 0 && resubscribeCount == 3)
			this.logger.info(CommonLogMessage.syncAPIOverrun("SyncGetExchTickers", resubscribeCount));
		query.Stop();
		this.logger.info(CommonLogMessage.disconnectChannel("ExchTick"));
		int rowCount = table.nRow();
		for (int count = 0; count < rowCount; count++) {
			String ticker = table.GetCell(count, TICKER_POS);
			if (ticker.startsWith("Q:")) {
				this.tickersList.remove(ticker);
				this.tickersList.remove(ticker.substring(2));
				this.volumePlusSymbols.add(ticker.substring(2));
				this.tickersList.add(ticker + "/T");
			}
			if (ticker.startsWith("T:")) {
				this.tickersList.add(ticker);
				this.volumePlusSymbols.add(ticker.substring(2));
			}
		}
	}

	public void populateOtcTickers(String otcExchangeFilePath) {
		String exchange = "BBO";
		UltraChan query = new UltraChan(this.ip, this.port, "ExchTick", "ExchTick", false);
		this.logger.info(CommonLogMessage.connectChannel("ExchTick", this.ip));
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do {
			this.logger.info(CommonLogMessage.requestUC("SyncGetExchTickers", exchange, ++resubscribeCount));
			table = query.SyncGetExchTickers(exchange, new Object());
			this.logger.info(
					CommonLogMessage.responseUC("SyncGetExchTickers", exchange, table == null ? 0 : table.nRow()));
		} while (table != null && table.len() == 0 && resubscribeCount < 3);
		if (table != null && table.len() == 0 && resubscribeCount == 3)
			this.logger.info(CommonLogMessage.syncAPIOverrun("SyncGetExchTickers", resubscribeCount));
		query.Stop();
		this.logger.info(CommonLogMessage.disconnectChannel("ExchTick"));
		int rowCount = table.nRow();
		int duplicateCount = 0;
		OTCExchangeMapPopulator octPopulator = new OTCExchangeMapPopulator(otcExchangeFilePath);
		Set<String> otcExchanges = octPopulator.populateExchangeMap();
		for (int count = 0; count < rowCount; count++) {
			String ticker = table.GetCell(count, TICKER_POS);
			if (ticker.endsWith(".CA"))
				continue;
			String marketCenter = table.GetCell(count, 2);
			if (marketCenter != null && marketCenter.trim().length() > 0 && otcExchanges.contains(marketCenter))
				ticker = ticker + "/" + marketCenter.trim();
			if (this.tickersList.contains(ticker)) {
				this.logger.info("Duplicate: " + ticker + " Exchange: " + exchange);
				duplicateCount++;
			}
			this.tickersList.add(ticker);
		}
		this.logger.info("Total Duplicate count: " + duplicateCount + " for exchange " + exchange);
	}

	public void populateTickerCache(String exchange) {
		UltraChan query = new UltraChan(this.ip, this.port, "ExchTick", "ExchTick", false);
		this.logger.info(CommonLogMessage.connectChannel("ExchTick", this.ip));
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do {
			this.logger.info(CommonLogMessage.requestUC("SyncGetExchTickers", exchange, ++resubscribeCount));
			table = query.SyncGetExchTickers(exchange, new Object());
			this.logger.info(
					CommonLogMessage.responseUC("SyncGetExchTickers", exchange, table == null ? 0 : table.nRow()));
		} while (table != null && table.len() == 0 && resubscribeCount < 3);
		if (table != null && table.len() == 0 && resubscribeCount == 3)
			this.logger.info(CommonLogMessage.syncAPIOverrun("SyncGetExchTickers", resubscribeCount));
		query.Stop();
		this.logger.info(CommonLogMessage.disconnectChannel("ExchTick"));
		parseBlobTable(exchange, table);
	}

	private void parseBlobTable(String exchange, BlobTable blobTable) {
		int rowCount = blobTable.nRow();
		int duplicateCount = 0;
		for (int count = 0; count < rowCount; count++) {
			String ticker = blobTable.GetCell(count, TICKER_POS);
			if (ticker.endsWith(".CA"))
				continue;
			if (this.tickersList.contains(ticker)) {
				this.logger.info("Duplicate: " + ticker + " Exchange: " + exchange);
				duplicateCount++;
			}
			this.tickersList.add(ticker);
		}
		this.logger.info("Total Duplicate count: " + duplicateCount + " for exchange " + exchange);
	}

	public HashSet<String> getTickerList() {
		return this.tickersList;
	}

	public HashSet<String> getVolumePlusSymbols() {
		return this.volumePlusSymbols;
	}

}
