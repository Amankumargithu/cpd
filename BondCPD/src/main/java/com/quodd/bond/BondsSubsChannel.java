package com.quodd.bond;

import static com.quodd.bond.BondsCPD.datacache;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDUtility;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.BONDQuote;
import QuoddFeed.msg.BONDTrade;
import QuoddFeed.msg.Image;

public class BondsSubsChannel extends CPDChannel {

	public BondsSubsChannel(String ip, int port, String name) {
		super(ip, port, name, datacache);
		this.name = name;
		boolean doLog = cpdProperties.getBooleanProperty("LOG_CHANNEL", false);
		if (doLog)
			startLoggingThread();
		connectChannel();
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	@Override
	public void OnImage(String streamName, Image image) {
		try {
			this.messageCount++;
			String ticker = image.tkr();
			if (ticker == null) {
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img.tkr() is null"));
				return;
			}
			logger.info(() -> CommonLogMessage.image(this.name, image.tkr(), image._trdPrc, image._ask, image._bid,
					image.protocol()));
			this.imageSet.add(ticker);
			datacache.getImageTickerMap().put(ticker, ticker);
			datacache.getPendingTickerMap().remove(ticker);

			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			Map<String, Object> changedQuote = new ConcurrentHashMap<>();

			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rootTicker, ticker);
			changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(image._trdPrc));
			changedTrade.put(CPDConstants.tradeVolume, CPDUtility.changeDoubletoLong(image._trdVol));
			changedTrade.put(CPDConstants.totalVolume, image._acVol);
			changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(image._high));
			changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(image._low));
			changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(image._vwap));
			changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(image._netChg));
			changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(image._pctChg));
			changedTrade.put(CPDConstants.protocolId, datacache.getTradeProtocol());
			changedTrade.put(CPDConstants.tradeTime, image._trdTime);
			changedTrade.put(CPDConstants.ucProtocol, image.protocol());

			changedQuote.put(CPDConstants.ticker, ticker);
			changedQuote.put(CPDConstants.rootTicker, ticker);
			changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(image._bid));
			changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(image._ask));
			changedQuote.put(CPDConstants.bidSize, image._bidSize);
			changedQuote.put(CPDConstants.askSize, image._askSize);
			changedQuote.put(CPDConstants.askExchange, getExchangeCode(image._askMktCtr));
			changedQuote.put(CPDConstants.bidExchange, getExchangeCode(image._bidMktCtr));
			changedQuote.put(CPDConstants.protocolId, datacache.getQuoteProtocol());
			changedQuote.put(CPDConstants.ucProtocol, image.protocol());

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.ticker, ticker);
				legacyData.put(CPDConstants.rootTicker, ticker);
				legacyData.put(CPDConstants.ucProtocol, image.protocol());
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(image._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, CPDUtility.formatFourDecimals(image._trdVol));
				legacyData.put(CPDConstants.totalVolume, image._acVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(image._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(image._low));
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(image._vwap));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(image._netChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(image._pctChg));
				legacyData.put(CPDConstants.protocolId, datacache.getTradeProtocol());
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(image._trdTime));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(image._trdTime));
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(image._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(image._ask));
				legacyData.put(CPDConstants.bidSize, image._bidSize);
				legacyData.put(CPDConstants.askSize, image._askSize);
				legacyData.put(CPDConstants.askExchange, getExchangeCode(image._askMktCtr));
				legacyData.put(CPDConstants.bidExchange, getExchangeCode(image._bidMktCtr));
				Map<String, Object> completeMap = datacache.getCachedLegacyData().get(ticker);
				if (completeMap == null) {
					completeMap = new ConcurrentHashMap<>();
				}
				completeMap.putAll(legacyData);
				datacache.getCachedLegacyData().put(ticker, completeMap);
				datacache.getLegacymQueue().putAll(ticker, completeMap);
				datacache.getBondWriterQueue().putAll(ticker, completeMap);
			}

			stats.incrementProcessedUCQuoteMessages();
			stats.incrementProcessedUCTradeMessages();
			String key = ticker + "|" + datacache.getTradeProtocol();
			datacache.getTrademQueue().putAll(key, changedTrade);
			key = ticker + "|" + datacache.getQuoteProtocol();
			datacache.getQuotemQueue().putAll(key, changedQuote);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Callback invoked when an Bond quote is received.
	 * 
	 * @param streamName Name of this Data Stream
	 * @param qm         BONDQuote
	 */
	@Override
	public void OnUpdate(String streamName, BONDQuote quote) {
		try {
			this.messageCount++;
			String ticker = quote.tkr();
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "BONDQuote.tkr() is null"));
				return;
			}
			Map<String, Object> changedQuote = new ConcurrentHashMap<>();
			changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(quote._bid));
			changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(quote._ask));
			changedQuote.put(CPDConstants.bidSize, quote._bidSize);
			changedQuote.put(CPDConstants.askSize, quote._askSize);
			changedQuote.put(CPDConstants.askYield, CPDUtility.changeDoubletoLong(quote._askYield));
			changedQuote.put(CPDConstants.bidYield, CPDUtility.changeDoubletoLong(quote._bidYield));
			String exchange = getExchangeCode(quote._mktCtr);
			changedQuote.put(CPDConstants.askExchange, exchange);
			changedQuote.put(CPDConstants.bidExchange, exchange);
			changedQuote.put(CPDConstants.protocolId, datacache.getQuoteProtocol());

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(quote._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(quote._ask));
				legacyData.put(CPDConstants.bidSize, quote._bidSize);
				legacyData.put(CPDConstants.askSize, quote._askSize);
				legacyData.put(CPDConstants.askYield, CPDUtility.formatFourDecimals(quote._askYield));
				legacyData.put(CPDConstants.bidYield, CPDUtility.formatFourDecimals(quote._bidYield));
				legacyData.put(CPDConstants.askExchange, exchange);
				legacyData.put(CPDConstants.bidExchange, exchange);
				legacyData.put(CPDConstants.protocolId, datacache.getQuoteProtocol());
				if (datacache.getCachedLegacyData().get(ticker) != null) {
					datacache.getCachedLegacyData().get(ticker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(ticker, datacache.getCachedLegacyData().get(ticker));
					datacache.getBondWriterQueue().putAll(ticker, datacache.getCachedLegacyData().get(ticker));
				}
			}
			stats.incrementProcessedUCQuoteMessages();
			String key = ticker + "|" + datacache.getQuoteProtocol();
			datacache.getQuotemQueue().putAll(key, changedQuote);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Callback invoked when an Bond trade is received.
	 * 
	 * @param streamName Name of this Data Stream
	 * @param qm         BONDTrade
	 */
	@Override
	public void OnUpdate(String streamName, BONDTrade trade) {
		try {
			this.messageCount++;
			String ticker = trade.tkr();
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "BONDTrade.tkr() is null"));
				return;
			}
			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(trade._trdPrc));
			changedTrade.put(CPDConstants.tradeVolume, trade._trdVol);
			changedTrade.put(CPDConstants.totalVolume, trade._acVol);
			changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(trade._high));
			changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(trade._low));
			changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(trade._netChg));
			changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(trade._pctChg));
			changedTrade.put(CPDConstants.tradeTime, trade.MsgTimeMs());
			changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trade._vwap));
			changedTrade.put(CPDConstants.tradeExchange, getExchangeCode(trade._mktCtr));
			changedTrade.put(CPDConstants.protocolId, datacache.getTradeProtocol());

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(trade._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, trade._trdVol);
				legacyData.put(CPDConstants.totalVolume, trade._acVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(trade._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(trade._low));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(trade._netChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(trade._pctChg));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(trade.MsgTimeMs()));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(trade.MsgTimeMs()));
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trade._vwap));
				legacyData.put(CPDConstants.tradeExchange, getExchangeCode(trade._mktCtr));
				legacyData.put(CPDConstants.protocolId, datacache.getTradeProtocol());
				if (datacache.getCachedLegacyData().get(ticker) != null) {
					datacache.getCachedLegacyData().get(ticker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(ticker, datacache.getCachedLegacyData().get(ticker));
					datacache.getBondWriterQueue().putAll(ticker, datacache.getCachedLegacyData().get(ticker));
				}
			}
			stats.incrementProcessedUCTradeMessages();
			String key = ticker + "|" + datacache.getTradeProtocol();
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private String getExchangeCode(String exchgCode) {
		if (exchgCode == null || exchgCode.startsWith("??") || exchgCode.contains("?") || exchgCode.length() == 0)
			return " ";
		return exchgCode;
	}
}