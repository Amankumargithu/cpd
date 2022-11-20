package com.quodd.equity;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;
import static com.quodd.equity.EquityCPD.datacache;

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

public class EquitySubscriptionChannel extends CPDChannel {

	private final ConcurrentMap<String, String> imageTickerMap = datacache.getImageTickerMap();
	private final Set<Integer> allowedProtocols = datacache.getAllowedProtocols();

	public EquitySubscriptionChannel(String ip, int port, String name) {
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
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img.tkr() is null"));
				return;
			}
			int prot = img.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img protocol is " + prot));
				datacache.unsubscribeTicker(ticker);
				return;
			}
			datacache.getPendingTickerMap().remove(ticker);
			String legacyTicker = img.tkr();
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			int tradeServiceCode = datacache.getTradeServiceCode(ticker);
			String tradeTapeset = "";
			if (tradeServiceCode == datacache.getUtpTradeProtocol()) {
				tradeTapeset = datacache.getUtpTapeset();
			} else if (tradeServiceCode == datacache.getCtaaTradeProtocol()) {
				tradeTapeset = datacache.getCtaaTapeset();
			} else if (tradeServiceCode == datacache.getCtabTradeProtocol()) {
				tradeTapeset = datacache.getCtabTapeset();
			}
			int quoteServiceCode = datacache.getQuoteServiceCode(ticker);
			String quoteTapeSet = null;
			if (quoteServiceCode == datacache.getUtpQuoteProtocol()) {
				quoteTapeSet = datacache.getUtpTapeset();
			} else if (quoteServiceCode == datacache.getCtaaQuoteProtocol()) {
				quoteTapeSet = datacache.getCtaaTapeset();
			} else if (quoteServiceCode == datacache.getCtabQuoteProtocol()) {
				quoteTapeSet = datacache.getCtabTapeset();
			}
			this.imageSet.add(ticker);
			this.imageTickerMap.put(ticker, ticker);
			Map<String, Object> changedQuotes = new ConcurrentHashMap<>();
			Map<String, Object> changedTrades = new ConcurrentHashMap<>();

			logger.info(() -> CommonLogMessage.image(this.name, img.tkr(), img._trdPrc, img._ask, img._bid,
					img.protocol()));
			changedQuotes.put(CPDConstants.protocolId, quoteServiceCode);
			changedQuotes.put(CPDConstants.tapeSet, quoteTapeSet);
			String exchangeCode = CPDUtility.getMappedExchangeCode(img._priMktCtr);
			if (exchangeCode != null) {
				changedQuotes.put(CPDConstants.exchangeCode, exchangeCode);
			}
			changedQuotes.put(CPDConstants.limitUpDown, CPDUtility.formatLimitUpDown(img.LimitUpDown()));
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
			changedQuotes.put(CPDConstants.ucProtocol, img.protocol());

			changedTrades.put(CPDConstants.protocolId, tradeServiceCode);
			changedTrades.put(CPDConstants.ucProtocol, img.protocol());
			changedTrades.put(CPDConstants.tapeSet, tradeTapeset);
			changedTrades.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(img._trdPrc_ext));
			changedTrades.put(CPDConstants.extTradeTime, img._trdTime_ext);
			changedTrades.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(img._netChg_ext));
			changedTrades.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(img._pctChg_ext));
			changedTrades.put(CPDConstants.extTradeVol, img._trdVol_ext);
			changedTrades.put(CPDConstants.extTradeExh, getEquityPlusExchangeCode(img._trdMktCtr_ext));
			char extUpDown = img._prcTck_ext == '\u0000' ? '-' : img._prcTck_ext;
			changedTrades.put(CPDConstants.extTickUpDown, extUpDown);
			changedTrades.put(CPDConstants.tradeExchange, getEquityPlusExchangeCode(img._trdMktCtr));
			if (exchangeCode != null) {
				changedTrades.put(CPDConstants.exchangeCode, exchangeCode);
			}
			changedTrades.put(CPDConstants.limitUpDown, CPDUtility.formatLimitUpDown(img.LimitUpDown()));
			long lastPrc = CPDUtility.changeDoubletoLong(img._trdPrc);
			changedTrades.put(CPDConstants.lastPrice, lastPrc);
			changedTrades.put(CPDConstants.tradeVolume, img._trdVol);
			long high = CPDUtility.changeDoubletoLong(img._high);
			changedTrades.put(CPDConstants.dayHigh, high);
			changedTrades.put(CPDConstants.dayHighTime, img.JavaTime(img._highTime));
			long low = CPDUtility.changeDoubletoLong(img._low);
			changedTrades.put(CPDConstants.dayLow, low);
			changedTrades.put(CPDConstants.dayLowTime, img.JavaTime(img._lowTime));
			long changePrc = CPDUtility.changeDoubletoLong(img._netChg);
			changedTrades.put(CPDConstants.changePrice, changePrc);
			long openPrc = CPDUtility.changeDoubletoLong(img._open);
			changedTrades.put(CPDConstants.openPrice, openPrc);
			changedTrades.put(CPDConstants.openVol, img._openVol);
			changedTrades.put(CPDConstants.openTime, img.JavaTime(img._openTime));
			long vwap = CPDUtility.changeDoubletoLong(img.vwap(4));
			changedTrades.put(CPDConstants.vwap, vwap);
			char upDown = img._prcTck == '\u0000' ? '-' : img._prcTck;
			changedTrades.put(CPDConstants.tickUpDown, upDown);
			long perChg = CPDUtility.changeDoubletoLong(img._pctChg);
			changedTrades.put(CPDConstants.percentChange, perChg);
			changedTrades.put(CPDConstants.tradeTime, img._trdTime);
			changedTrades.put(CPDConstants.totalVolume, img._acVol);
			long prevClose = CPDUtility.changeDoubletoLong(img._close);
			changedTrades.put(CPDConstants.previousClose, prevClose);
			changedTrades.put(CPDConstants.isSho, (img._regSHO == 0x01 || img._regSHO == 0x02));
			changedTrades.put(CPDConstants.isHalted, (img._halted == 0x01));
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
				legacyData.put(CPDConstants.extTickUpDown, extUpDown);
				legacyData.put(CPDConstants.tradeExchange, getEquityPlusExchangeCode(img._trdMktCtr));
				legacyData.put(CPDConstants.exchangeCode, exchangeCode);
				legacyData.put(CPDConstants.limitUpDown, CPDUtility.formatLimitUpDown(img.LimitUpDown()));
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(img._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, img._trdVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(img._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(img._low));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(img._netChg));
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(img._open));
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(img.vwap(4)));
				legacyData.put(CPDConstants.tickUpDown, upDown);
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
	@Override
	public void OnUpdate(String streamName, EQQuote quote) {
		try {
			this.messageCount++;
			String ticker = quote.tkr();
			String legacyTicker = quote.tkr();
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote.tkr() is null"));
				return;
			}
			int prot = quote.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote protocol is " + prot));
				return;
			}
			if (quote._bid == 0 && quote._ask == 0 && quote._bidSize == 0 && quote._askSize == 0) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote quotes are Zero"));
				return;
			}
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
				return;
			}
			Map<String, Object> changedQuotes = new ConcurrentHashMap<>();

			changedQuotes.put(CPDConstants.ticker, ticker);
			changedQuotes.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(quote._bid));
			changedQuotes.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(quote._ask));
			changedQuotes.put(CPDConstants.bidSize, quote._bidSize);
			changedQuotes.put(CPDConstants.askSize, quote._askSize);
			changedQuotes.put(CPDConstants.quoteTime, quote.MsgTimeMs());
			changedQuotes.put(CPDConstants.rtl, quote.RTL());
			String exchnge = getEquityPlusExchangeCode(quote._mktCtr);
			changedQuotes.put(CPDConstants.askExchange, exchnge);
			changedQuotes.put(CPDConstants.bidExchange, exchnge);

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(quote._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(quote._ask));
				legacyData.put(CPDConstants.bidSize, quote._bidSize);
				legacyData.put(CPDConstants.askSize, quote._askSize);
				legacyData.put(CPDConstants.rtl, quote.RTL());
				legacyData.put(CPDConstants.askExchange, exchnge);
				legacyData.put(CPDConstants.bidExchange, exchnge);
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCQuoteMessages();
			int quoteServiceCode = datacache.getQuoteServiceCode(ticker);
			String key = ticker + "|" + quoteServiceCode;
			datacache.getQuotemQueue().putAll(key, changedQuotes);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(final String streamName, EQBbo bbo) {
		try {
			this.messageCount++;
			String ticker = bbo.tkr();
			String legacyTicker = bbo.tkr();
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "bbo.tkr() is null"));
				return;
			}
			int prot = bbo.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "bbo protocol is " + prot));
				return;
			}
			if (bbo._bid == 0 && bbo._ask == 0 && bbo._bidSize == 0 && bbo._askSize == 0) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "bbo quotes are Zero"));
				return;
			}
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "bbo No Image " + ticker));
				return;
			}
			Map<String, Object> changedQuotes = new ConcurrentHashMap<>();
			changedQuotes.put(CPDConstants.ticker, ticker);
			changedQuotes.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(bbo._bid));
			changedQuotes.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(bbo._ask));
			changedQuotes.put(CPDConstants.bidSize, bbo._bidSize);
			changedQuotes.put(CPDConstants.askSize, bbo._askSize);
			changedQuotes.put(CPDConstants.quoteTime, bbo.MsgTimeMs());
			changedQuotes.put(CPDConstants.rtl, bbo.RTL());
			String askExchange = getEquityPlusExchangeCode(bbo._askMktCtr);
			changedQuotes.put(CPDConstants.askExchange, askExchange);
			String bidExchange = getEquityPlusExchangeCode(bbo._bidMktCtr);
			changedQuotes.put(CPDConstants.bidExchange, bidExchange);
			String limitupDown = CPDUtility.formatLimitUpDown(bbo.LimitUpDown());
			changedQuotes.put(CPDConstants.limitUpDown, limitupDown);

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(bbo._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(bbo._ask));
				legacyData.put(CPDConstants.bidSize, bbo._bidSize);
				legacyData.put(CPDConstants.askSize, bbo._askSize);
				legacyData.put(CPDConstants.rtl, bbo.RTL());
				legacyData.put(CPDConstants.askExchange, askExchange);
				legacyData.put(CPDConstants.bidExchange, bidExchange);
				legacyData.put(CPDConstants.limitUpDown, limitupDown);
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCQuoteMessages();
			int quoteServiceCode = datacache.getQuoteServiceCode(ticker);
			String key = ticker + "|" + quoteServiceCode;
			datacache.getQuotemQueue().putAll(key, changedQuotes);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQTrade update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(String streamName, EQTrade trd) {
		try {
			this.messageCount++;
			String ticker = trd.tkr();
			String legacyTicker = trd.tkr();
			if (ticker == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd.tkr() is null"));
				return;
			}
			int prot = trd.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd protocol is " + prot));
				return;
			}
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd No Image " + ticker));
				return;
			}
			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rtl, trd.RTL());

			Map<String, Object> legacyData = new ConcurrentHashMap<>();
			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, legacyTicker);
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

				int tradeServiceCode = datacache.getTradeServiceCode(ticker);
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
				long vwap = CPDUtility.changeDoubletoLong(trd.vwap(4));
				changedTrade.put(CPDConstants.vwap, vwap);
				// we donot send vwap in this
				// not odd lot check in prod instead a boolean
				// round lot value - 100 shares
				// less than 100 - is odd lot trade
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
					String extExch = getEquityPlusExchangeCode(trd._trdMktCtr_ext);
					changedTrade.put(CPDConstants.extTradeExh, extExch);

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(trd._trdPrc_ext));
						legacyData.put(CPDConstants.extTradeVol, trd._trdVol_ext);
						legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(trd._netChg_ext));
						legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(trd._pctChg_ext));
						legacyData.put(CPDConstants.extTickUpDown, extUpDown);
						legacyData.put(CPDConstants.extTradeTime, CPDUtility.processTime(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeDate, CPDUtility.processDate(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeExh, extExch);
						if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
							datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
							datacache.getLegacymQueue().putAll(legacyTicker,
									datacache.getCachedLegacyData().get(legacyTicker));
						}
					}
				}

				stats.incrementProcessedUCTradeMessages();
				final int tradeServiceCode = datacache.getTradeServiceCode(ticker);
				final String key = ticker + "|" + tradeServiceCode;
				datacache.getTrademQueue().putAll(key, changedTrade);
				return;
			}
			String exchange = getEquityPlusExchangeCode(trd._mktCtr);
			changedTrade.put(CPDConstants.tradeExchange, exchange);
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
			char upDown = trd._prcTck == '\u0000' ? '-' : trd._prcTck;
			changedTrade.put(CPDConstants.tickUpDown, upDown);
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
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
				legacyData.put(CPDConstants.tickUpDown, upDown);
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(trd._pctChg));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(trd._trdTime));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(trd._trdTime));
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCTradeMessages();
			int tradeServiceCode = datacache.getTradeServiceCode(ticker);
			String key = ticker + "|" + tradeServiceCode;
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void OnUpdate(String streamName, EQBboMM bbomm) {
		this.messageCount++;
		logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQBboMM not processed " + bbomm.tkr()));
	}

	/**
	 * Called when an EQTradeSts update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(String streamName, EQTradeSts trdSts) {
		try {
			this.messageCount++;
			String ticker = trdSts.tkr();
			String legacyTicker = trdSts.tkr();
			if (ticker == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trdSts.tkr() is null"));
				return;
			}
			int prot = trdSts.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trdSts protocol is " + prot));
				return;
			}
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			if (!this.imageTickerMap.containsKey(trdSts.tkr())) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd No Image " + trdSts.tkr()));
				return;
			}
			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rtl, trdSts.RTL());
			if (!trdSts.IsShortSaleRestricted())
				changedTrade.put(CPDConstants.isHalted, trdSts._halted);
			changedTrade.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());
			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.rtl, trdSts.RTL());
				if (!trdSts.IsShortSaleRestricted())
					legacyData.put(CPDConstants.isHalted, trdSts._halted);
				legacyData.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCTradeMessages();
			int tradeServiceCode = datacache.getTradeServiceCode(ticker);
			String key = ticker + "|" + tradeServiceCode;
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

}