package com.quodd.nb;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;
import static com.quodd.nb.NasdaqBasicCpd.datacache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDTickerValidator;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;

public class VolumePlusEquityChannel extends CPDChannel {

	private final String tickerSuffix = datacache.getSymbolSuffix();

	public VolumePlusEquityChannel(String ip, int port, String name) {
		super(ip, port, name, datacache);
		boolean doLog = cpdProperties.getBooleanProperty("LOG_CHANNEL", false);
		if (doLog)
			startLoggingThread();
		connectChannel();
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	@Override
	public void OnImage(String streamName, Image img) {
		this.messageCount++;
		String ticker = img.tkr();
		if (ticker == null) {
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img.tkr() is null"));
			return;
		}
		int prot = img.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img protocol is " + prot));
			return;
		}
		ticker = CPDTickerValidator.canadianToQuoddSymbology(ticker);
		if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img Incorrect symbol " + img.tkr()));
			return;
		}
		ticker = ticker + this.tickerSuffix;
		String legacyTicker = ticker;
		if (datacache.isDelayed())
			ticker = ticker + CPDConstants.DELAYED_SUFFIX;
		this.imageSet.add(ticker);
		Map<String, Object> changedTrades = new ConcurrentHashMap<>();
		changedTrades.put(CPDConstants.volume_plus, img._acVol);
		changedTrades.put(CPDConstants.ticker, ticker);

		if (datacache.isLegacyDistribution()) {
			Map<String, Object> legacyTrades = new ConcurrentHashMap<>();
			changedTrades.put(CPDConstants.volume_plus, img._acVol);
			changedTrades.put(CPDConstants.ticker, legacyTicker);
			Map<String, Object> completeMap = datacache.getCachedLegacyData().get(legacyTicker);
			if (completeMap == null) {
				completeMap = new ConcurrentHashMap<>();
			}
			completeMap.putAll(legacyTrades);
			datacache.getCachedLegacyData().put(legacyTicker, completeMap);
			datacache.getLegacymQueue().putAll(legacyTicker, completeMap);
		}

		String key = ticker + "|" + datacache.getTradeProtocol();
		stats.incrementProcessedUCTradeMessages();
		datacache.getTrademQueue().putAll(key, changedTrades);
	}

	/**
	 * Called when an EQTrade update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(String streamName, EQTrade trd) {
		this.messageCount++;
		String ticker = trd.tkr();
		if (ticker == null) {
			stats.incrementDroppedTradeMessages();
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd.tkr() is null"));
			return;
		}
		int prot = trd.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			stats.incrementDroppedTradeMessages();
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd protocol is " + prot));
			return;
		}
		ticker = CPDTickerValidator.canadianToQuoddSymbology(ticker);
		if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
			stats.incrementDroppedTradeMessages();
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd Incorrect symbol " + trd.tkr()));
			return;
		}
		ticker = ticker + this.tickerSuffix;
		String legacyTicker = ticker;
		if (datacache.isDelayed()) {
			ticker = ticker + CPDConstants.DELAYED_SUFFIX;
		}
		Map<String, Object> changedTrades = new ConcurrentHashMap<>();
		changedTrades.put(CPDConstants.volume_plus, trd._acVol);
		changedTrades.put(CPDConstants.ticker, ticker);

		if (datacache.isLegacyDistribution()) {
			Map<String, Object> legacyTrades = new ConcurrentHashMap<>();
			changedTrades.put(CPDConstants.volume_plus, trd._acVol);
			changedTrades.put(CPDConstants.ticker, legacyTicker);
			if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
				datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyTrades);
				datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
			}
		}
		String key = ticker + "|" + datacache.getTradeProtocol();
		stats.incrementProcessedUCTradeMessages();
		datacache.getTrademQueue().putAll(key, changedTrades);
	}
}