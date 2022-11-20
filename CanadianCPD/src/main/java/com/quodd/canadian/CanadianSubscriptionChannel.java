package com.quodd.canadian;

import static com.quodd.canadian.CanadianCPD.datacache;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDUtility;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.EQBbo;
import QuoddFeed.msg.EQBboMM;
import QuoddFeed.msg.EQQuote;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.EQTradeSts;
import QuoddFeed.msg.Image;

public class CanadianSubscriptionChannel extends CPDChannel {

	private ConcurrentMap<String, String> imageTickerMap = datacache.getImageTickerMap();
	private Set<Integer> allowedProtocols = datacache.getAllowedProtocols();

	public CanadianSubscriptionChannel(String ip, int port, String name) {
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
		try {
			this.messageCount++;
			String ticker = img.tkr();
			if (ticker == null) {
				logger.info(CommonLogMessage.dropSymbol(streamName, "img.tkr() is null"));
				return;
			}
			int prot = img.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				logger.info(CommonLogMessage.dropSymbol(streamName, "img protocol is " + prot));
				datacache.unsubscribeTicker(ticker);
				return;
			}
			String ucTicker = ticker;
			ticker = datacache.canadianToQuoddSymbology(ticker);
			String legacyTicker = ticker;
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			int tradeServiceCode = getTradeServiceCode(ticker);
			String tradeTapeset = "";
			if (tradeServiceCode == datacache.getXtseTradeProtocol())
				tradeTapeset = datacache.getXtseTapeset();
			if (tradeServiceCode == datacache.getXtsxTradeProtocol())
				tradeTapeset = datacache.getXtsxTapeset();

			int quoteServiceCode = getQuoteServiceCode(ticker);
			String quoteTapeset = "";
			if (quoteServiceCode == datacache.getXtseQuoteProtocol())
				quoteTapeset = datacache.getXtseTapeset();
			if (quoteServiceCode == datacache.getXtsxQuoteProtocol())
				quoteTapeset = datacache.getXtsxTapeset();
			Map<String, Object> changedQuotes = new ConcurrentHashMap<>();
			Map<String, Object> changedTrades = new ConcurrentHashMap<>();

			logger.info(CommonLogMessage.image(this.name, img.tkr(), img._trdPrc, img._ask, img._bid, img.protocol()));
			this.imageSet.add(ticker);

			changedQuotes.put(CPDConstants.protocolId, quoteServiceCode);
			changedQuotes.put(CPDConstants.tapeSet, quoteTapeset);
			changedQuotes.put(CPDConstants.ucProtocol, img.protocol());
			String exchangeCode = CPDUtility.getMappedExchangeCode(img._priMktCtr);
			if (exchangeCode != null)
				changedQuotes.put(CPDConstants.exchangeCode, exchangeCode);
			String limitUpDown = CPDUtility.formatLimitUpDown(img.LimitUpDown());
			changedQuotes.put(CPDConstants.limitUpDown, limitUpDown);
			changedQuotes.put(CPDConstants.askExchange, getEquityPlusExchangeCode(img._askMktCtr));
			changedQuotes.put(CPDConstants.bidExchange, getEquityPlusExchangeCode(img._bidMktCtr));
			changedQuotes.put(CPDConstants.quoteTime, img.MsgTimeMs());
			changedQuotes.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(img._ask));
			changedQuotes.put(CPDConstants.askSize, img._askSize);
			changedQuotes.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(img._bid));
			changedQuotes.put(CPDConstants.bidSize, img._bidSize);
			changedQuotes.put(CPDConstants.ticker, ticker);
			changedQuotes.put(CPDConstants.rootTicker, ticker);
			changedQuotes.put(CPDConstants.rtl, img.RTL());

			changedTrades.put(CPDConstants.protocolId, tradeServiceCode);
			changedTrades.put(CPDConstants.tapeSet, tradeTapeset);
			changedTrades.put(CPDConstants.ucProtocol, img.protocol());
			changedTrades.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(img._trdPrc_ext));
			changedTrades.put(CPDConstants.extTradeTime, img._trdTime_ext);
			changedTrades.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(img._netChg_ext));
			changedTrades.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(img._pctChg_ext));
			changedTrades.put(CPDConstants.extTradeVol, img._trdVol_ext);
			changedTrades.put(CPDConstants.extTradeExh, getEquityPlusExchangeCode(img._trdMktCtr_ext));
			char extUpDown = img._prcTck_ext == '\u0000' ? '-' : img._prcTck_ext;
			changedTrades.put(CPDConstants.extTickUpDown, extUpDown);
			changedTrades.put(CPDConstants.tradeExchange, getEquityPlusExchangeCode(img._trdMktCtr));
			changedTrades.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(img._trdPrc));
			if (exchangeCode != null)
				changedTrades.put(CPDConstants.exchangeCode, exchangeCode);
			changedTrades.put(CPDConstants.limitUpDown, limitUpDown);
			changedTrades.put(CPDConstants.tradeVolume, img._trdVol);
			changedTrades.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(img._high));
			changedTrades.put(CPDConstants.dayHighTime, img.JavaTime(img._highTime));
			changedTrades.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(img._low));
			changedTrades.put(CPDConstants.dayLowTime, img.JavaTime(img._lowTime));
			changedTrades.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(img._netChg));
			changedTrades.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(img._open));
			changedTrades.put(CPDConstants.openVol, img._openVol);
			changedTrades.put(CPDConstants.openTime, img.JavaTime(img._openTime));
			changedTrades.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(img.vwap(4)));
			char UpDown = img._prcTck == '\u0000' ? '-' : img._prcTck;
			changedTrades.put(CPDConstants.tickUpDown, UpDown);
			changedTrades.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(img._pctChg));
			changedTrades.put(CPDConstants.tradeTime, img._trdTime);
			changedTrades.put(CPDConstants.totalVolume, img._acVol);
			changedTrades.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(img._close));
			if (img._regSHO == 0x01 || img._regSHO == 0x02) {
				changedTrades.put(CPDConstants.isSho, true);
			} else {
				changedTrades.put(CPDConstants.isSho, false);
			}
			if (img._halted == 0x01) {
				changedTrades.put(CPDConstants.isHalted, true);
			} else {
				changedTrades.put(CPDConstants.isHalted, false);
			}

			changedTrades.put(CPDConstants.ticker, ticker);
			changedTrades.put(CPDConstants.rootTicker, ticker);
			changedTrades.put(CPDConstants.rtl, img.RTL());

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.ucProtocol, img.protocol());
				legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(img._trdPrc_ext));
				legacyData.put(CPDConstants.extTradeTime, CPDUtility.processTime(img._trdTime_ext));
				legacyData.put(CPDConstants.extTradeDate, CPDUtility.processDate(img._trdTime_ext));
				legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(img._netChg_ext));
				legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(img._pctChg_ext));
				legacyData.put(CPDConstants.extTradeVol, img._trdVol_ext);
				legacyData.put(CPDConstants.extTradeExh, getEquityPlusExchangeCode(img._trdMktCtr_ext));
				legacyData.put(CPDConstants.extTickUpDown, img._prcTck_ext);
				legacyData.put(CPDConstants.extTickUpDown, extUpDown);
				legacyData.put(CPDConstants.tradeExchange, getEquityPlusExchangeCode(img._trdMktCtr));
				legacyData.put(CPDConstants.exchangeCode, exchangeCode);
				legacyData.put(CPDConstants.limitUpDown, CPDUtility.formatLimitUpDown(img.LimitUpDown()));
				legacyData.put(CPDConstants.exchangeCode, exchangeCode);
				legacyData.put(CPDConstants.limitUpDown, CPDUtility.formatLimitUpDown(img.LimitUpDown()));
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(img._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, img._trdVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(img._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(img._low));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(img._netChg));
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(img._open));
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(img.vwap(4)));
				legacyData.put(CPDConstants.tickUpDown, UpDown);
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(img._pctChg));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(img._trdTime));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(img._trdTime));
				legacyData.put(CPDConstants.askExchange, getEquityPlusExchangeCode(img._askMktCtr));
				legacyData.put(CPDConstants.bidExchange, getEquityPlusExchangeCode(img._bidMktCtr));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(img._ask));
				legacyData.put(CPDConstants.askSize, img._askSize);
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(img._bid));
				legacyData.put(CPDConstants.bidSize, img._bidSize);
				legacyData.put(CPDConstants.totalVolume, img._acVol);
				legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(img._close));
				legacyData.put(CPDConstants.isSho, (img._regSHO == 0x01 || img._regSHO == 0x02));
				legacyData.put(CPDConstants.isHalted, (img._halted == 0x01));
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.rootTicker, legacyTicker);
				legacyData.put(CPDConstants.rtl, img.RTL());
				Map<String, Object> completeMap = datacache.getCachedLegacyData().get(legacyTicker);
				if (completeMap == null) {
					completeMap = new ConcurrentHashMap<>();
				}
				completeMap.putAll(legacyData);
				datacache.getCachedLegacyData().put(legacyTicker, completeMap);
				datacache.getLegacymQueue().putAll(legacyTicker, completeMap);
			}

			this.imageTickerMap.put(ucTicker, ucTicker);// UC ticker
			datacache.getPendingTickerMap().remove(ucTicker);
			logger.info("Adding Image in queue for " + ticker);

			stats.incrementProcessedUCQuoteMessages();
			stats.incrementProcessedUCTradeMessages();

			String key = ticker + "|" + tradeServiceCode;
			datacache.getTrademQueue().putAll(key, changedTrades);

			key = ticker + "|" + quoteServiceCode;
			datacache.getQuotemQueue().putAll(key, changedQuotes);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker.
	 */
	public void OnUpdate(String streamName, EQQuote quote) {
		try {
			this.messageCount++;
			String ticker = quote.tkr();
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "quote.tkr() is null"));
				return;
			}
			int prot = quote.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "quote protocol is " + prot));
				return;
			}
			if (quote._bid == 0 && quote._ask == 0 && quote._bidSize == 0 && quote._askSize == 0) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "quote quotes are Zero"));
				return;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
				return;
			}
			ticker = datacache.canadianToQuoddSymbology(ticker);
			String legacyTicker = ticker;
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;

			Map<String, Object> changedQuotes = new ConcurrentHashMap<>();
			changedQuotes.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(quote._bid));
			changedQuotes.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(quote._ask));
			changedQuotes.put(CPDConstants.bidSize, quote._bidSize);
			changedQuotes.put(CPDConstants.askSize, quote._askSize);
			changedQuotes.put(CPDConstants.ticker, ticker);
			changedQuotes.put(CPDConstants.quoteTime, quote.MsgTimeMs());
			changedQuotes.put(CPDConstants.rtl, quote.RTL());
			String exchange = getEquityPlusExchangeCode(quote._mktCtr);
			changedQuotes.put(CPDConstants.askExchange, exchange);
			changedQuotes.put(CPDConstants.bidExchange, exchange);

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(quote._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(quote._ask));
				legacyData.put(CPDConstants.bidSize, quote._bidSize);
				legacyData.put(CPDConstants.askSize, quote._askSize);
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.quoteTime, quote.MsgTimeMs());
				legacyData.put(CPDConstants.rtl, quote.RTL());
				legacyData.put(CPDConstants.askExchange, exchange);
				legacyData.put(CPDConstants.bidExchange, exchange);
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCQuoteMessages();
			int quoteServiceCode = getQuoteServiceCode(ticker);
			String key = ticker + "|" + quoteServiceCode;
			datacache.getQuotemQueue().putAll(key, changedQuotes);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	public void OnUpdate(String streamName, EQBbo bbo) {
		try {
			this.messageCount++;
			String ticker = bbo.tkr();
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "bbo.tkr() is null"));
				return;
			}
			int prot = bbo.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "bbo protocol is " + prot));
				return;
			}
			if (bbo._bid == 0 && bbo._ask == 0 && bbo._bidSize == 0 && bbo._askSize == 0) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "bbo quotes are Zero"));
				return;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "bbo No Image " + ticker));
				return;
			}
			ticker = datacache.canadianToQuoddSymbology(ticker);
			String legacyTicker = ticker;
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;

			Map<String, Object> changedQuotes = new ConcurrentHashMap<>();
			changedQuotes.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(bbo._bid));
			changedQuotes.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(bbo._ask));
			changedQuotes.put(CPDConstants.bidSize, bbo._bidSize);
			changedQuotes.put(CPDConstants.askSize, bbo._askSize);
			changedQuotes.put(CPDConstants.ticker, ticker);
			changedQuotes.put(CPDConstants.quoteTime, bbo.MsgTimeMs());
			changedQuotes.put(CPDConstants.rtl, bbo.RTL());
			changedQuotes.put(CPDConstants.askExchange, getEquityPlusExchangeCode(bbo._askMktCtr));
			changedQuotes.put(CPDConstants.bidExchange, getEquityPlusExchangeCode(bbo._bidMktCtr));

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(bbo._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(bbo._ask));
				legacyData.put(CPDConstants.bidSize, bbo._bidSize);
				legacyData.put(CPDConstants.askSize, bbo._askSize);
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.quoteTime, bbo.MsgTimeMs());
				legacyData.put(CPDConstants.rtl, bbo.RTL());
				legacyData.put(CPDConstants.askExchange, getEquityPlusExchangeCode(bbo._askMktCtr));
				legacyData.put(CPDConstants.bidExchange, getEquityPlusExchangeCode(bbo._bidMktCtr));
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCQuoteMessages();
			int quoteServiceCode = getQuoteServiceCode(ticker);
			String key = ticker + "|" + quoteServiceCode;
			datacache.getQuotemQueue().putAll(key, changedQuotes);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQTrade update is received for an equity ticker.
	 */
	public void OnUpdate(String streamName, EQTrade trd) {
		try {
			this.messageCount++;
			String ticker = trd.tkr();
			if (ticker == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd.tkr() is null"));
				return;
			}
			int prot = trd.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd protocol is " + prot));
				return;
			}

			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd No Image " + ticker));
				return;
			}
			ticker = datacache.canadianToQuoddSymbology(ticker);
			String legacyTicker = ticker;
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;

			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();
			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rtl, trd.RTL());
			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, ticker);
				legacyData.put(CPDConstants.rtl, trd.RTL());
			}

			if (trd.IsSummary()) {
				changedTrade.put(CPDConstants.totalVolume, trd._acVol);
				changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(trd._high));
				changedTrade.put(CPDConstants.dayHighTime, trd.JavaTime(trd._highTime));
				changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(trd._low));
				changedTrade.put(CPDConstants.dayLowTime, trd.JavaTime(trd._lowTime));

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.totalVolume, trd._acVol);
					legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(trd._high));
					legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(trd._low));
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}
				stats.incrementProcessedUCTradeMessages();

				int tradeServiceCode = getTradeServiceCode(ticker);
				String key = ticker + "|" + tradeServiceCode;
				datacache.getTrademQueue().putAll(key, changedTrade);
				return;
			}
			if (trd.IsCxl() || trd.IsCorrection() || trd.IsAsOf() || trd.IsAsOfCorrection() || trd.IsAsOfCxl()) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd Cancel trade " + ticker));
				return;
			}
			if (!(trd.IsEligible())) {
				changedTrade.put(CPDConstants.totalVolume, trd._acVol);
				changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trd.vwap(4)));

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.totalVolume, trd._acVol);
					legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
				}

				if (trd.IsExtended() && !trd.IsOddLot()) {
					changedTrade.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(trd._trdPrc_ext));
					changedTrade.put(CPDConstants.extTradeVol, trd._trdVol_ext);
					changedTrade.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(trd._netChg_ext));
					changedTrade.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(trd._pctChg_ext));
					char extUpDown = trd._prcTck_ext == '\u0000' ? '-' : trd._prcTck_ext;
					changedTrade.put(CPDConstants.extTickUpDown, extUpDown);
					changedTrade.put(CPDConstants.extTradeTime,
							trd._trdTime_ext == 0 ? 0 : trd.JavaTime(trd._trdTime_ext));
					changedTrade.put(CPDConstants.extTradeExh, getEquityPlusExchangeCode(trd._trdMktCtr_ext));

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(trd._trdPrc_ext));
						legacyData.put(CPDConstants.extTradeVol, trd._trdVol_ext);
						legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(trd._netChg_ext));
						legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(trd._pctChg_ext));
						legacyData.put(CPDConstants.extTickUpDown, extUpDown);
						legacyData.put(CPDConstants.extTradeTime,
								trd._trdTime_ext == 0 ? 0 : CPDUtility.processTime(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeDate,
								trd._trdTime_ext == 0 ? 0 : CPDUtility.processDate(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeExh, getEquityPlusExchangeCode(trd._trdMktCtr_ext));
					}
				}

				stats.incrementProcessedUCTradeMessages();

				int tradeServiceCode = getTradeServiceCode(ticker);
				String key = ticker + "|" + tradeServiceCode;
				datacache.getTrademQueue().putAll(key, changedTrade);
				if (datacache.isLegacyDistribution()) {
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}
				return;
			}
			changedTrade.put(CPDConstants.tradeExchange, getEquityPlusExchangeCode(trd._mktCtr));
			changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(trd._trdPrc));
			changedTrade.put(CPDConstants.tradeVolume, trd._trdVol);
			changedTrade.put(CPDConstants.totalVolume, trd._acVol);
			changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(trd._high));
			changedTrade.put(CPDConstants.dayHighTime, trd.JavaTime(trd._highTime));
			changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(trd._low));
			changedTrade.put(CPDConstants.dayLowTime, trd.JavaTime(trd._lowTime));
			changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(trd._netChg));
			changedTrade.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(trd._openPrc));
			changedTrade.put(CPDConstants.openVol, trd._openVol);
			changedTrade.put(CPDConstants.openTime, trd.JavaTime(trd._openTime));
			changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trd.vwap(4)));
			char UpDown = trd._prcTck == '\u0000' ? '-' : trd._prcTck;
			changedTrade.put(CPDConstants.tickUpDown, UpDown);
			changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(trd._pctChg));
			changedTrade.put(CPDConstants.tradeTime, trd._trdTime);

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.tradeExchange, getEquityPlusExchangeCode(trd._mktCtr));
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(trd._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, trd._trdVol);
				legacyData.put(CPDConstants.totalVolume, trd._acVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(trd._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(trd._low));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(trd._netChg));
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(trd._openPrc));
				legacyData.put(CPDConstants.openVol, trd._openVol);
				legacyData.put(CPDConstants.openTime, trd.JavaTime(trd._openTime));
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
				legacyData.put(CPDConstants.tickUpDown, UpDown);
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(trd._pctChg));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(trd._trdTime));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(trd._trdTime));
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCTradeMessages();

			int tradeServiceCode = getTradeServiceCode(ticker);
			String key = ticker + "|" + tradeServiceCode;
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void OnUpdate(String streamName, EQBboMM bbomm) {
		try {
			this.messageCount++;
			logger.info(CommonLogMessage.dropSymbol(streamName, "EQBboMM not processed " + bbomm.tkr()));
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQTradeSts update is received for an equity ticker.
	 */
	public void OnUpdate(String streamName, EQTradeSts trdSts) {
		try {
			this.messageCount++;
			String ticker = trdSts.tkr();
			if (ticker == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trdSts.tkr() is null"));
				return;
			}
			int prot = trdSts.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trdSts protocol is " + prot));
				return;
			}
			if (!this.imageTickerMap.containsKey(trdSts.tkr())) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd No Image " + trdSts.tkr()));
				return;
			}
			ticker = datacache.canadianToQuoddSymbology(ticker);
			String legacyTicker = ticker;
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;

			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rtl, trdSts.RTL());
			changedTrade.put(CPDConstants.isHalted, trdSts._halted);
			changedTrade.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.rtl, trdSts.RTL());
				legacyData.put(CPDConstants.isHalted, trdSts._halted);
				legacyData.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}

			int tradeServiceCode = getTradeServiceCode(ticker);
			String key = ticker + "|" + tradeServiceCode;
			stats.incrementProcessedUCTradeMessages();
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public int getTradeServiceCode(String ticker) {
		if (datacache.getXtseQuoddTickerSet().get(ticker) != null)
			return datacache.getXtseTradeProtocol();
		else if (datacache.getXtsxQuoddTickerSet().get(ticker) != null)
			return datacache.getXtsxTradeProtocol();
		return 0;
	}

	public int getQuoteServiceCode(String ticker) {
		if (datacache.getXtseQuoddTickerSet().get(ticker) != null)
			return datacache.getXtseQuoteProtocol();
		else if (datacache.getXtsxQuoddTickerSet().get(ticker) != null)
			return datacache.getXtsxQuoteProtocol();
		return 0;
	}
}