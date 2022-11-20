package com.quodd.nb;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;
import static com.quodd.nb.NasdaqBasicCpd.datacache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDTickerValidator;
import com.quodd.common.cpd.util.CPDUtility;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.EQBbo;
import QuoddFeed.msg.EQBboMM;
import QuoddFeed.msg.EQQuote;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.EQTradeSts;
import QuoddFeed.msg.Image;

public class NbSubscriptionChannel extends CPDChannel {

	private static final String NASDAQ_BASIC_SUFFIX = "/T";
	private static final String NASDAQ_BASIC_TRADE_PREFIX = "T:";
	private static final String NASDAQ_BASIC_QUOTE_PREFIX = "Q:";
	private static final String NASDAQ_BASIC_EXCHANGE_CODE = "t";

	private ConcurrentMap<String, String> imageTickerMap = datacache.getImageTickerMap();
	private String tickerSuffix = datacache.getSymbolSuffix();
	private Set<Integer> allowedProtocols = datacache.getAllowedProtocols();

	public NbSubscriptionChannel(String ip, int port, String name) {
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
			ticker = ticker.toUpperCase();
			if (ticker.startsWith(NASDAQ_BASIC_QUOTE_PREFIX) && ticker.endsWith(NASDAQ_BASIC_SUFFIX)) // like Q:AAPL/T
			{
				// Populate Quotes from /T symbols - only for Nasdaq basic
				logger.info(() -> CommonLogMessage.image(this.name, img.tkr(), img._trdPrc, img._ask, img._bid,
						img.protocol()));
				this.imageTickerMap.put(ticker, ticker);
				this.imageSet.add(ticker);
				datacache.getPendingTickerMap().remove(ticker);
				ticker = ticker.substring(2, ticker.length() - 2);
				String rootTicker = ticker;
				String legacyTicker = ticker;
				ticker = ticker + this.tickerSuffix;
				if (datacache.isDelayed())
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				Map<String, Object> changedQuotes = new ConcurrentHashMap<>();
				changedQuotes.put(CPDConstants.askExchange, NASDAQ_BASIC_EXCHANGE_CODE);
				changedQuotes.put(CPDConstants.bidExchange, NASDAQ_BASIC_EXCHANGE_CODE);
				changedQuotes.put(CPDConstants.protocolId, datacache.getQuoteProtocol());
				changedQuotes.put(CPDConstants.quoteTime, img.MsgTimeMs());
				changedQuotes.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(img._ask));
				changedQuotes.put(CPDConstants.askSize, img._askSize);
				changedQuotes.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(img._bid));
				changedQuotes.put(CPDConstants.bidSize, img._bidSize);
				changedQuotes.put(CPDConstants.rtl, img.RTL());
				changedQuotes.put(CPDConstants.ticker, ticker);
				changedQuotes.put(CPDConstants.rootTicker, rootTicker);
				changedQuotes.put(CPDConstants.tapeSet, datacache.getTapeset());
				changedQuotes.put(CPDConstants.ucProtocol, img.protocol());
				logger.info("Adding NB Quote Image in queue for " + ticker);

				if (datacache.isLegacyDistribution()) {
					Map<String, Object> legacyData = new ConcurrentHashMap<>();
					legacyData.put(CPDConstants.askExchange, NASDAQ_BASIC_EXCHANGE_CODE);
					legacyData.put(CPDConstants.bidExchange, NASDAQ_BASIC_EXCHANGE_CODE);
					legacyData.put(CPDConstants.ucProtocol, img.protocol());
					legacyData.put(CPDConstants.protocolId, datacache.getQuoteProtocol());
					legacyData.put(CPDConstants.quoteTime, img.MsgTimeMs());
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(img._ask));
					legacyData.put(CPDConstants.askSize, img._askSize);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(img._bid));
					legacyData.put(CPDConstants.bidSize, img._bidSize);
					legacyData.put(CPDConstants.rtl, img.RTL());
					legacyData.put(CPDConstants.ticker, ticker);
					legacyData.put(CPDConstants.rootTicker, rootTicker);
					legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.extTradeVol, 0l);
					legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.totalVolume, 0l);
					legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.tradeVolume, 0l);
					legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.openVol, 0l);
					legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(0d));
					Map<String, Object> completeMap = datacache.getCachedLegacyData().get(legacyTicker);
					if (completeMap == null) {
						completeMap = new ConcurrentHashMap<>();
					}
					completeMap.putAll(legacyData);
					datacache.getCachedLegacyData().put(legacyTicker, completeMap);
					datacache.getLegacymQueue().putAll(legacyTicker, completeMap);
				}
				stats.incrementProcessedUCQuoteMessages();

				String key = ticker + "|" + datacache.getQuoteProtocol();
				datacache.getQuotemQueue().putAll(key, changedQuotes);

			} else if (ticker.startsWith(NASDAQ_BASIC_TRADE_PREFIX)) {
				if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
					logger.info(() -> CommonLogMessage.dropSymbol(streamName,
							"img NB Incorrect trade symbol " + img.tkr()));
					datacache.unsubscribeTicker(ticker);
					return;
				}
				logger.info(() -> CommonLogMessage.image(this.name, img.tkr(), img._trdPrc, img._ask, img._bid,
						img.protocol()));
				datacache.getPendingTickerMap().remove(ticker);
				this.imageTickerMap.put(ticker, ticker);
				ticker = ticker.substring(2);
				String rootTicker = ticker;
				ticker = ticker + this.tickerSuffix;
				String legacyTicker = ticker;
				if (datacache.isDelayed())
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				Map<String, Object> changedTrades = new ConcurrentHashMap<>();
				Map<String, Object> legacyData = new ConcurrentHashMap<>();

				changedTrades.put(CPDConstants.tapeSet, datacache.getTapeset());
				changedTrades.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(img._trdPrc_ext));
				changedTrades.put(CPDConstants.extTradeVol, img._trdVol_ext);
				changedTrades.put(CPDConstants.extTradeExh, NASDAQ_BASIC_EXCHANGE_CODE);
				changedTrades.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(img._netChg_ext));
				char extUpDown = img._prcTck_ext == '\u0000' ? '-' : img._prcTck_ext;
				changedTrades.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(img._pctChg_ext));
				changedTrades.put(CPDConstants.extTickUpDown, extUpDown);
				changedTrades.put(CPDConstants.ucProtocol, img.protocol());
				changedTrades.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(img.vwap(4)));
				changedTrades.put(CPDConstants.totalVolume, img._acVol);
				changedTrades.put(CPDConstants.extTradeTime, img._trdTime_ext);
				changedTrades.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(img._trdPrc));
				changedTrades.put(CPDConstants.tradeVolume, img._trdVol);
				changedTrades.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(img._high));
				changedTrades.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(img._low));
				changedTrades.put(CPDConstants.dayHighTime, img.JavaTime(img._highTime));
				changedTrades.put(CPDConstants.dayLowTime, img.JavaTime(img._lowTime));
				changedTrades.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(img._open));
				changedTrades.put(CPDConstants.openVol, img._openVol);
				changedTrades.put(CPDConstants.openTime, img.JavaTime(img._openTime));
				changedTrades.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(img._netChg));
				changedTrades.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(img._pctChg));
				char UpDown = img._prcTck == '\u0000' ? '-' : img._prcTck;
				changedTrades.put(CPDConstants.tickUpDown, UpDown);
				changedTrades.put(CPDConstants.rtl, img.RTL());
				changedTrades.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(img._close));
				if (img._regSHO == 0x01 || img._regSHO == 0x02) {
					changedTrades.put(CPDConstants.isSho, true);
					legacyData.put(CPDConstants.isSho, true);
				} else {
					changedTrades.put(CPDConstants.isSho, false);
					legacyData.put(CPDConstants.isSho, false);
				}
				if (img._halted == 0x01) {
					changedTrades.put(CPDConstants.isHalted, true);
					legacyData.put(CPDConstants.isHalted, true);
				} else {
					changedTrades.put(CPDConstants.isHalted, false);
					legacyData.put(CPDConstants.isHalted, false);
				}
				logger.info("Image Ticker: " + ticker + " SHO: " + Integer.toHexString(img._regSHO) + " HLT: "
						+ Integer.toHexString(img._halted));
				changedTrades.put(CPDConstants.tradeExchange, NASDAQ_BASIC_EXCHANGE_CODE);
				changedTrades.put(CPDConstants.tradeTime, img._trdTime);
				changedTrades.put(CPDConstants.protocolId, datacache.getTradeProtocol());
				changedTrades.put(CPDConstants.ticker, ticker);
				changedTrades.put(CPDConstants.rootTicker, rootTicker);

				logger.info("Adding Trades Image NB in queue for " + ticker);

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(img._trdPrc_ext));
					legacyData.put(CPDConstants.ucProtocol, img.protocol());
					legacyData.put(CPDConstants.extTradeVol, img._trdVol_ext);
					legacyData.put(CPDConstants.extTradeExh, NASDAQ_BASIC_EXCHANGE_CODE);
					legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(img._netChg_ext));
					legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(img._pctChg_ext));
					legacyData.put(CPDConstants.extTickUpDown, extUpDown);
					legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(img.vwap(4)));
					legacyData.put(CPDConstants.totalVolume, img._acVol);
					legacyData.put(CPDConstants.extTradeTime, CPDUtility.processTime(img._trdTime_ext));
					legacyData.put(CPDConstants.extTradeDate, CPDUtility.processDate(img._trdTime_ext));
					legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(img._trdPrc));
					legacyData.put(CPDConstants.tradeVolume, img._trdVol);
					legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(img._high));
					legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(img._low));
					legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(img._open));
					legacyData.put(CPDConstants.openVol, img._openVol);
					legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(img._netChg));
					legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(img._pctChg));
					legacyData.put(CPDConstants.tickUpDown, UpDown);
					legacyData.put(CPDConstants.rtl, img.RTL());
					legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(img._close));
					legacyData.put(CPDConstants.tradeExchange, NASDAQ_BASIC_EXCHANGE_CODE);
					legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(img._trdTime));
					legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(img._trdTime));
					legacyData.put(CPDConstants.protocolId, datacache.getTradeProtocol());
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.rootTicker, rootTicker);
					legacyData.put(CPDConstants.askExchange, NASDAQ_BASIC_EXCHANGE_CODE);
					legacyData.put(CPDConstants.bidExchange, NASDAQ_BASIC_EXCHANGE_CODE);
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.askSize, 0l);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(0d));
					legacyData.put(CPDConstants.bidSize, 0l);
					Map<String, Object> completeMap = datacache.getCachedLegacyData().get(legacyTicker);
					if (completeMap == null) {
						completeMap = new ConcurrentHashMap<>();
					}
					completeMap.putAll(legacyData);
					datacache.getCachedLegacyData().put(legacyTicker, completeMap);
					datacache.getLegacymQueue().putAll(legacyTicker, completeMap);
				}
				String key = ticker + "|" + datacache.getTradeProtocol();
				stats.incrementProcessedUCTradeMessages();
				datacache.getTrademQueue().putAll(key, changedTrades);
			} else {
				datacache.unsubscribeTicker(ticker);
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "undefined symbol format " + img.tkr()));
			}
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
			if (ticker.startsWith(NASDAQ_BASIC_QUOTE_PREFIX) && ticker.endsWith(NASDAQ_BASIC_SUFFIX))
				ticker = ticker.substring(2, ticker.length() - 2);
			else {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote Incorrect symbol " + quote.tkr()));
				return;
			}
			if (!this.imageTickerMap.containsKey(quote.tkr())) {
				stats.incrementDroppedQuoteMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
				return;
			}
			ticker = ticker + this.tickerSuffix;
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

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(quote._bid));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(quote._ask));
				legacyData.put(CPDConstants.bidSize, quote._bidSize);
				legacyData.put(CPDConstants.askSize, quote._askSize);
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.quoteTime, quote.MsgTimeMs());
				legacyData.put(CPDConstants.rtl, quote.RTL());
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			// ask exchange and bid exchange are constant for Nasdaq basic
			String key = ticker + "|" + datacache.getQuoteProtocol();
			stats.incrementProcessedUCQuoteMessages();
			datacache.getQuotemQueue().putAll(key, changedQuotes);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(String streamName, EQBbo bbo) {
		try {
			this.messageCount++;
			// No EQBbo quotes for nasdaq basic
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQBbo not processed " + bbo.tkr()));
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
			if (ticker.startsWith(NASDAQ_BASIC_TRADE_PREFIX))
				ticker = ticker.substring(2);
			else {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd Incorrect symbol " + trd.tkr()));
				return;
			}
			if (!this.imageTickerMap.containsKey(trd.tkr())) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd No Image " + trd.tkr()));
				return;
			}
			ticker = ticker + this.tickerSuffix;
			String legacyTicker = ticker;
			if (datacache.isDelayed())
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			if (trd.IsCxl() || trd.IsCorrection() || trd.IsAsOf() || trd.IsAsOfCorrection() || trd.IsAsOfCxl()) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd Cancel trade " + trd.tkr()));
				return;
			}
			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();

			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rtl, trd.RTL());

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
				String key = ticker + "|" + datacache.getTradeProtocol();
				datacache.getTrademQueue().putAll(key, changedTrade);
				return;
			}
			if (!(trd.IsEligible())) {
				changedTrade.put(CPDConstants.totalVolume, trd._acVol);
				changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trd.vwap(4)));

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.totalVolume, trd._acVol);
					legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
				}
				if (trd.IsExtended()) {
					changedTrade.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(trd._trdPrc_ext));
					changedTrade.put(CPDConstants.extTradeVol, trd._trdVol_ext);
					// trade_market_extended is constant for Nasdaq Basic
					changedTrade.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(trd._netChg_ext));
					changedTrade.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(trd._pctChg_ext));
					char extUpDown = trd._prcTck_ext == '\u0000' ? '-' : trd._prcTck_ext;
					changedTrade.put(CPDConstants.extTickUpDown, extUpDown);
					changedTrade.put(CPDConstants.extTradeTime,
							trd._trdTime_ext == 0 ? 0 : trd.JavaTime(trd._trdTime_ext));
					changedTrade.put(CPDConstants.extTradeExh, NASDAQ_BASIC_EXCHANGE_CODE);

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(trd._trdPrc_ext));
						legacyData.put(CPDConstants.extTradeVol, trd._trdVol_ext);
						// trade_market_extended is constant for Nasdaq Basic
						legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(trd._netChg_ext));
						legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(trd._pctChg_ext));
						legacyData.put(CPDConstants.extTickUpDown, extUpDown);
						legacyData.put(CPDConstants.extTradeTime,
								trd._trdTime_ext == 0 ? 0 : CPDUtility.processTime(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeDate,
								trd._trdTime_ext == 0 ? 0 : CPDUtility.processDate(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeExh, NASDAQ_BASIC_EXCHANGE_CODE);
					}
				}
				stats.incrementProcessedUCTradeMessages();
				String key = ticker + "|" + datacache.getTradeProtocol();
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
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(trd._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, trd._trdVol);
				legacyData.put(CPDConstants.totalVolume, trd._acVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(trd._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(trd._low));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(trd._netChg));
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(trd._openPrc));
				legacyData.put(CPDConstants.openVol, trd._openVol);
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
			String key = ticker + "|" + datacache.getTradeProtocol();
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void OnUpdate(String streamName, EQBboMM bbomm) {
		try {
			this.messageCount++;
			// No EQBboMM quotes for nasdaq basic
			logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQBboMM not processed " + bbomm.tkr()));
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQTradeSts update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(String streamName, EQTradeSts trdSts) {
		try {
			this.messageCount++;
			// keeping it commented as we have removed halted and SHO field from bean
			String ticker = trdSts.tkr();
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
			if (ticker.startsWith(NASDAQ_BASIC_TRADE_PREFIX))
				ticker = ticker.substring(2);
			else {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "trd Incorrect symbol " + trdSts.tkr()));
				return;
			}
			ticker = ticker + this.tickerSuffix;
			String legacyTicker = ticker;
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
			changedTrade.put(CPDConstants.isHalted, trdSts._halted);
			changedTrade.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyTrade = new ConcurrentHashMap<>();
				changedTrade.put(CPDConstants.ticker, legacyTicker);
				changedTrade.put(CPDConstants.rtl, trdSts.RTL());
				changedTrade.put(CPDConstants.isHalted, trdSts._halted);
				changedTrade.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyTrade);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			String key = ticker + "|" + datacache.getTradeProtocol();
			stats.incrementProcessedUCTradeMessages();
			datacache.getTrademQueue().putAll(key, changedTrade);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}