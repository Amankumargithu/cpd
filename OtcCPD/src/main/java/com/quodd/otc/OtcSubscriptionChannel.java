package com.quodd.otc;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;
import static com.quodd.otc.OtcCPD.datacache;

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

public class OtcSubscriptionChannel extends CPDChannel {
	private static final String PINK_SHEET_MARKET_CENTER = "u";
	private final Map<String, String> imageTickerMap = datacache.getImageTickerMap();
	private final ConcurrentMap<String, String> duallyQuotedTickerMap = datacache.getOtcbbSymbolMap();
	private final Set<Integer> allowedProtocols = datacache.getAllowedProtocols();

	public OtcSubscriptionChannel(String ip, int port, String name) {
		super(ip, port, name, datacache);
		boolean doLog = cpdProperties.getBooleanProperty("LOG_CHANNEL", false);
		if (doLog) {
			startLoggingThread();
		}
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
			this.imageSet.add(ticker);
			if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) // only otc quotes will be regionals
			{
				ticker = ticker.substring(0, ticker.indexOf("/"));
				String rootTicker = ticker;
				ticker = ticker + datacache.getOtcSymbolSuffix();
				if (datacache.isDelayed()) {
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				}
				this.imageTickerMap.put(ticker, ticker);
				logger.info(() -> CommonLogMessage.image(this.name, img.tkr(), img._trdPrc, img._ask, img._bid,
						img.protocol()));
				Map<String, Object> changedQuote = new ConcurrentHashMap<>();
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
				changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(img._ask));
				changedQuote.put(CPDConstants.askSize, img._askSize);
				changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(img._bid));
				changedQuote.put(CPDConstants.bidSize, img._bidSize);
				changedQuote.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.ticker, ticker);
				changedQuote.put(CPDConstants.rootTicker, rootTicker);
				changedQuote.put(CPDConstants.protocolId, datacache.getOtcQuoteProtocol());
				changedQuote.put(CPDConstants.quoteTime, img.MsgTimeMs());
				changedQuote.put(CPDConstants.rtl, img.RTL());
				changedQuote.put(CPDConstants.tapeSet, datacache.getOtcTapeset());
				changedQuote.put(CPDConstants.ucProtocol, img.protocol());

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(img._ask));
					legacyData.put(CPDConstants.askSize, img._askSize);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(img._bid));
					legacyData.put(CPDConstants.bidSize, img._bidSize);
					legacyData.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.rootTicker, rootTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcQuoteProtocol());
					legacyData.put(CPDConstants.quoteTime, img.MsgTimeMs());
					legacyData.put(CPDConstants.ucProtocol, img.protocol());
					Map<String, Object> completeMap = datacache.getCachedLegacyData().get(legacyTicker);
					if (completeMap == null) {
						completeMap = new ConcurrentHashMap<>();
					}
					completeMap.putAll(legacyData);
					datacache.getCachedLegacyData().put(legacyTicker, completeMap);
					datacache.getLegacymQueue().putAll(legacyTicker, completeMap);
				}

				logger.info("Adding Otc Quote Image in queue for " + ticker);
				stats.incrementProcessedUCQuoteMessages();

				String key = ticker + "|" + datacache.getOtcQuoteProtocol();
				datacache.getQuotemQueue().putAll(key, changedQuote);
			} else { // it is a trade image, may be otcbb quote
				String rootTicker = ticker;
				ticker = ticker + datacache.getOtcSymbolSuffix();
				if (datacache.isDelayed()) {
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				}
				this.imageTickerMap.put(ticker, ticker);
				logger.info(() -> CommonLogMessage.image(this.name, img.tkr(), img._trdPrc, img._ask, img._bid,
						img.protocol()));
				Map<String, Object> changedTrade = new ConcurrentHashMap<>();
				Map<String, Object> legacyData = new ConcurrentHashMap<>();

				changedTrade.put(CPDConstants.ticker, ticker);
				changedTrade.put(CPDConstants.rootTicker, rootTicker);
				changedTrade.put(CPDConstants.protocolId, datacache.getOtcTradeProtocol());
				changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(img._trdPrc));
				changedTrade.put(CPDConstants.tradeVolume, img._trdVol);
				changedTrade.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
				changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(img._netChg));
				changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(img._pctChg));
				changedTrade.put(CPDConstants.tradeTime, img._trdTime);
				changedTrade.put(CPDConstants.ucProtocol, img.protocol());
				char upDown = img._prcTck == '\u0000' ? '-' : img._prcTck;
				changedTrade.put(CPDConstants.tickUpDown, upDown);
				changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(img.vwap(4)));
				changedTrade.put(CPDConstants.totalVolume, img._acVol);
				changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(img._high));
				changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(img._low));
				changedTrade.put(CPDConstants.dayHighTime, img.JavaTime(img._highTime));
				changedTrade.put(CPDConstants.dayLowTime, img.JavaTime(img._lowTime));
				changedTrade.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(img._close));
				changedTrade.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(img._trdPrc_ext));
				changedTrade.put(CPDConstants.extTradeVol, img._trdVol_ext);
				changedTrade.put(CPDConstants.extTradeExh, PINK_SHEET_MARKET_CENTER);
				changedTrade.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(img._netChg_ext));
				changedTrade.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(img._pctChg_ext));
				char extUpDown = img._prcTck_ext == '\u0000' ? '-' : img._prcTck_ext;
				changedTrade.put(CPDConstants.extTickUpDown, extUpDown);
				changedTrade.put(CPDConstants.extTradeTime, img._trdTime_ext);
				changedTrade.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(img._open));
				changedTrade.put(CPDConstants.openVol, img._openVol);
				changedTrade.put(CPDConstants.openTime, img.JavaTime(img._openTime));
				changedTrade.put(CPDConstants.tapeSet, datacache.getOtcTapeset());
				changedTrade.put(CPDConstants.rtl, img.RTL());
				if (img._regSHO == 0x01 || img._regSHO == 0x02) {
					changedTrade.put(CPDConstants.isSho, true);
					legacyData.put(CPDConstants.isSho, true);
				} else {
					changedTrade.put(CPDConstants.isSho, false);
					legacyData.put(CPDConstants.isSho, false);
				}
				if (img._halted == 0x01) {
					changedTrade.put(CPDConstants.isHalted, true);
					legacyData.put(CPDConstants.isHalted, true);
				} else {
					changedTrade.put(CPDConstants.isHalted, false);
					legacyData.put(CPDConstants.isHalted, false);
				}

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.rootTicker, rootTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcTradeProtocol());
					legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(img._trdPrc));
					legacyData.put(CPDConstants.tradeVolume, img._trdVol);
					legacyData.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(img._netChg));
					legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(img._pctChg));
					legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(img._trdTime));
					legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(img._trdTime));
					legacyData.put(CPDConstants.tickUpDown, upDown);
					legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(img.vwap(4)));
					legacyData.put(CPDConstants.totalVolume, img._acVol);
					legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(img._high));
					legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(img._low));
					legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(img._close));
					legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(img._trdPrc_ext));
					legacyData.put(CPDConstants.extTradeVol, img._trdVol_ext);
					legacyData.put(CPDConstants.extTradeExh, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(img._netChg_ext));
					legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(img._pctChg_ext));
					legacyData.put(CPDConstants.extTickUpDown, extUpDown);
					legacyData.put(CPDConstants.extTradeTime, CPDUtility.processTime(img._trdTime_ext));
					legacyData.put(CPDConstants.extTradeDate, CPDUtility.processDate(img._trdTime_ext));
					legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(img._open));
					legacyData.put(CPDConstants.openVol, img._openVol);
					legacyData.put(CPDConstants.rtl, img.RTL());
					legacyData.put(CPDConstants.ucProtocol, img.protocol());
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}
				logger.info("Image Ticker: " + ticker + " SHO: " + Integer.toHexString(img._regSHO) + " HLT: "
						+ Integer.toHexString(img._halted));
				logger.info("Adding Trades Image in queue for " + ticker);
				stats.incrementProcessedUCTradeMessages();
				String key = ticker + "|" + datacache.getOtcTradeProtocol();
				datacache.getTrademQueue().putAll(key, changedTrade);

				if (this.duallyQuotedTickerMap.containsKey(rootTicker)) {
					// dup trade bean, check if quotes exists
					ticker = rootTicker;
					if (datacache.isDelayed()) {
						ticker = ticker + CPDConstants.DELAYED_SUFFIX;
					}
					this.imageTickerMap.put(ticker, ticker);
					changedTrade = new ConcurrentHashMap<>();
					legacyData = new ConcurrentHashMap<>();
					changedTrade.put(CPDConstants.ticker, ticker);
					changedTrade.put(CPDConstants.rootTicker, rootTicker);
					changedTrade.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
					changedTrade.put(CPDConstants.tapeSet, datacache.getOtcbbTapeset());
					changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(img._trdPrc));
					changedTrade.put(CPDConstants.tradeVolume, img._trdVol);
					changedTrade.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
					changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(img._netChg));
					changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(img._pctChg));
					changedTrade.put(CPDConstants.tradeTime, img._trdTime);
					changedTrade.put(CPDConstants.tickUpDown, upDown);
					changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(img.vwap(4)));
					changedTrade.put(CPDConstants.totalVolume, img._acVol);
					changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(img._high));
					changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(img._low));
					changedTrade.put(CPDConstants.dayHighTime, img.JavaTime(img._highTime));
					changedTrade.put(CPDConstants.dayLowTime, img.JavaTime(img._lowTime));
					changedTrade.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(img._close));
					changedTrade.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(img._trdPrc_ext));
					changedTrade.put(CPDConstants.extTradeVol, img._trdVol_ext);
					changedTrade.put(CPDConstants.extTradeExh, PINK_SHEET_MARKET_CENTER);
					changedTrade.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(img._netChg_ext));
					changedTrade.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(img._pctChg_ext));
					extUpDown = img._prcTck_ext == '\u0000' ? '-' : img._prcTck_ext;
					changedTrade.put(CPDConstants.extTickUpDown, extUpDown);
					changedTrade.put(CPDConstants.extTradeTime, img._trdTime_ext);
					changedTrade.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(img._open));
					changedTrade.put(CPDConstants.openVol, img._openVol);
					changedTrade.put(CPDConstants.openTime, img.JavaTime(img._openTime));
					changedTrade.put(CPDConstants.rtl, img.RTL());
					changedTrade.put(CPDConstants.ucProtocol, img.protocol());
					if (img._regSHO == 0x01 || img._regSHO == 0x02) {
						changedTrade.put(CPDConstants.isSho, true);
						legacyData.put(CPDConstants.isSho, true);
					} else {
						changedTrade.put(CPDConstants.isSho, false);
						legacyData.put(CPDConstants.isSho, false);
					}
					if (img._halted == 0x01) {
						changedTrade.put(CPDConstants.isHalted, true);
						legacyData.put(CPDConstants.isHalted, true);
					} else {
						changedTrade.put(CPDConstants.isHalted, false);
						legacyData.put(CPDConstants.isHalted, false);
					}

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.ticker, legacyTicker);
						legacyData.put(CPDConstants.rootTicker, rootTicker);
						legacyData.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
						legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(img._trdPrc));
						legacyData.put(CPDConstants.tradeVolume, img._trdVol);
						legacyData.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
						legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(img._netChg));
						legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(img._pctChg));
						legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(img._trdTime));
						legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(img._trdTime));
						legacyData.put(CPDConstants.tickUpDown, upDown);
						legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(img.vwap(4)));
						legacyData.put(CPDConstants.totalVolume, img._acVol);
						legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(img._high));
						legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(img._low));
						legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(img._close));
						legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(img._trdPrc_ext));
						legacyData.put(CPDConstants.extTradeVol, img._trdVol_ext);
						legacyData.put(CPDConstants.extTradeExh, PINK_SHEET_MARKET_CENTER);
						legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(img._netChg_ext));
						legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(img._pctChg_ext));
						legacyData.put(CPDConstants.extTickUpDown, extUpDown);
						legacyData.put(CPDConstants.extTradeTime, CPDUtility.processTime(img._trdTime_ext));
						legacyData.put(CPDConstants.extTradeDate, CPDUtility.processDate(img._trdTime_ext));
						legacyData.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(img._open));
						legacyData.put(CPDConstants.openVol, img._openVol);
						legacyData.put(CPDConstants.rtl, img.RTL());
						legacyData.put(CPDConstants.ucProtocol, img.protocol());
						if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
							datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
							datacache.getLegacymQueue().putAll(legacyTicker,
									datacache.getCachedLegacyData().get(legacyTicker));
						}
					}
					logger.info("Image Ticker: " + ticker + " SHO: " + Integer.toHexString(img._regSHO) + " HLT: "
							+ Integer.toHexString(img._halted));
					logger.info("Adding Trades Image in queue for " + ticker);
					stats.incrementProcessedUCTradeMessages();
					key = ticker + "|" + datacache.getOtcbbTradeProtocol();
					datacache.getTrademQueue().putAll(key, changedTrade);

					if (!(img._bid == 0 && img._ask == 0 && img._bidSize == 0 && img._askSize == 0)) {
						Map<String, Object> changedQuote = new ConcurrentHashMap<>();
						legacyData = new ConcurrentHashMap<>();
						changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(img._ask));
						changedQuote.put(CPDConstants.askSize, img._askSize);
						changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(img._bid));
						changedQuote.put(CPDConstants.bidSize, img._bidSize);
						changedQuote.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
						changedQuote.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
						changedQuote.put(CPDConstants.ticker, ticker);
						changedQuote.put(CPDConstants.rootTicker, rootTicker);
						changedQuote.put(CPDConstants.protocolId, datacache.getOtcbbQuoteProtocol());
						changedQuote.put(CPDConstants.tapeSet, datacache.getOtcbbTapeset());
						changedQuote.put(CPDConstants.quoteTime, img.MsgTimeMs());
						changedQuote.put(CPDConstants.rtl, img.RTL());
						changedQuote.put(CPDConstants.ucProtocol, img.protocol());

						if (datacache.isLegacyDistribution()) {
							legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(img._ask));
							legacyData.put(CPDConstants.askSize, img._askSize);
							legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(img._bid));
							legacyData.put(CPDConstants.bidSize, img._bidSize);
							legacyData.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
							legacyData.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
							legacyData.put(CPDConstants.ticker, legacyTicker);
							legacyData.put(CPDConstants.rootTicker, rootTicker);
							legacyData.put(CPDConstants.protocolId, datacache.getOtcbbQuoteProtocol());
							legacyData.put(CPDConstants.quoteTime, img.MsgTimeMs());
							legacyData.put(CPDConstants.rtl, img.RTL());
							legacyData.put(CPDConstants.ucProtocol, img.protocol());
							if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
								datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
								datacache.getLegacymQueue().putAll(legacyTicker,
										datacache.getCachedLegacyData().get(legacyTicker));
							}
						}
						logger.info("Adding Otc Quote Image in queue for " + ticker);
						stats.incrementProcessedUCQuoteMessages();
						key = ticker + "|" + datacache.getOtcbbQuoteProtocol();
						datacache.getQuotemQueue().putAll(key, changedQuote);
					} else {
						logger.info(CommonLogMessage.dropSymbol(ticker, "IMAGE quotes are Zero"));
					}
				}
			}
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
			if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "Regional Ticker " + ticker));
				return;
			}
			String rootTicker = ticker;
			String legacyTicker = ticker;
			ticker = ticker + datacache.getOtcSymbolSuffix();
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd No Image " + trd.tkr()));
				return;
			}
			if (trd.IsCxl() || trd.IsCorrection() || trd.IsAsOf() || trd.IsAsOfCorrection() || trd.IsAsOfCxl()) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd Cancel trade " + ticker));
				return;
			}
			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();

			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.protocolId, datacache.getOtcTradeProtocol());
			changedTrade.put(CPDConstants.rtl, trd.RTL());

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.protocolId, datacache.getOtcTradeProtocol());
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

				String key = ticker + "|" + datacache.getOtcTradeProtocol();
				stats.incrementProcessedUCTradeMessages();
				datacache.getTrademQueue().putAll(key, changedTrade);

				if (this.duallyQuotedTickerMap.containsKey(rootTicker)) {
					// dup trade bean, check if quotes exists
					ticker = rootTicker;
					if (datacache.isDelayed()) {
						ticker = ticker + CPDConstants.DELAYED_SUFFIX;
					}
					changedTrade = new ConcurrentHashMap<>();
					legacyData = new ConcurrentHashMap<>();

					changedTrade.put(CPDConstants.ticker, ticker);
					changedTrade.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
					changedTrade.put(CPDConstants.rtl, trd.RTL());
					changedTrade.put(CPDConstants.totalVolume, trd._acVol);
					changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(trd._high));
					changedTrade.put(CPDConstants.dayHighTime, trd.JavaTime(trd._highTime));
					changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(trd._low));
					changedTrade.put(CPDConstants.dayLowTime, trd.JavaTime(trd._lowTime));

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.ticker, legacyTicker);
						legacyData.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
						legacyData.put(CPDConstants.rtl, trd.RTL());
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
					key = ticker + "|" + datacache.getOtcbbTradeProtocol();
					datacache.getTrademQueue().putAll(key, changedTrade);
				}
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
					changedTrade.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(trd._netChg_ext));
					changedTrade.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(trd._pctChg_ext));
					char extUpDown = trd._prcTck_ext == '\u0000' ? '-' : trd._prcTck_ext;
					changedTrade.put(CPDConstants.extTickUpDown, extUpDown);
					changedTrade.put(CPDConstants.extTradeTime,
							trd._trdTime_ext == 0 ? 0 : trd.JavaTime(trd._trdTime_ext));
					changedTrade.put(CPDConstants.extTradeExh, PINK_SHEET_MARKET_CENTER);

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.extLastPrice, CPDUtility.formatFourDecimals(trd._trdPrc_ext));
						legacyData.put(CPDConstants.extTradeVol, trd._trdVol_ext);
						legacyData.put(CPDConstants.extChangePrc, CPDUtility.formatFourDecimals(trd._netChg_ext));
						legacyData.put(CPDConstants.extPerChg, CPDUtility.formatFourDecimals(trd._pctChg_ext));
						legacyData.put(CPDConstants.extTickUpDown, extUpDown);
						legacyData.put(CPDConstants.extTradeDate,
								trd._trdTime_ext == 0 ? 0 : CPDUtility.processDate(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeTime,
								trd._trdTime_ext == 0 ? 0 : CPDUtility.processTime(trd._trdTime_ext));
						legacyData.put(CPDConstants.extTradeExh, PINK_SHEET_MARKET_CENTER);
					}
				}

				stats.incrementProcessedUCTradeMessages();
				String key = ticker + "|" + datacache.getOtcTradeProtocol();
				datacache.getTrademQueue().putAll(key, changedTrade);
				if (datacache.isLegacyDistribution()) {
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}
				if (this.duallyQuotedTickerMap.containsKey(rootTicker)) {
					// dup trade bean, check if quotes exists
					ticker = rootTicker;
					if (datacache.isDelayed()) {
						ticker = ticker + CPDConstants.DELAYED_SUFFIX;
					}
					changedTrade = new ConcurrentHashMap<>();
					legacyData = new ConcurrentHashMap<>();

					changedTrade.put(CPDConstants.ticker, ticker);
					changedTrade.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
					changedTrade.put(CPDConstants.rtl, trd.RTL());
					changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trd.vwap(4)));

					if (datacache.isLegacyDistribution()) {
						legacyData.put(CPDConstants.ticker, legacyTicker);
						legacyData.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
						legacyData.put(CPDConstants.rtl, trd.RTL());
						legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
					}
					if (trd.IsExtended()) {
						changedTrade.put(CPDConstants.extLastPrice, CPDUtility.changeDoubletoLong(trd._trdPrc_ext));
						changedTrade.put(CPDConstants.extTradeVol, trd._trdVol_ext);
						changedTrade.put(CPDConstants.extChangePrc, CPDUtility.changeDoubletoLong(trd._netChg_ext));
						changedTrade.put(CPDConstants.extPerChg, CPDUtility.changeDoubletoLong(trd._pctChg_ext));
						char extUpDown = trd._prcTck_ext == '\u0000' ? '-' : trd._prcTck_ext;
						changedTrade.put(CPDConstants.extTickUpDown, extUpDown);
						changedTrade.put(CPDConstants.extTradeTime,
								trd._trdTime_ext == 0 ? 0 : trd.JavaTime(trd._trdTime_ext));

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
						}
					}

					stats.incrementProcessedUCTradeMessages();
					key = ticker + "|" + datacache.getOtcbbTradeProtocol();
					datacache.getTrademQueue().putAll(key, changedTrade);
					if (datacache.isLegacyDistribution()) {
						if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
							datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
							datacache.getLegacymQueue().putAll(legacyTicker,
									datacache.getCachedLegacyData().get(legacyTicker));
						}
					}
				}
				return;
			}
			changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(trd._trdPrc));
			changedTrade.put(CPDConstants.tradeVolume, trd._trdVol);
			changedTrade.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
			changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(trd._netChg));
			changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(trd._pctChg));
			changedTrade.put(CPDConstants.tradeTime, trd._trdTime);
			char upDown = trd._prcTck == '\u0000' ? '-' : trd._prcTck;
			changedTrade.put(CPDConstants.tickUpDown, upDown);
			changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trd.vwap(4)));
			changedTrade.put(CPDConstants.totalVolume, trd._acVol);
			changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(trd._high));
			changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(trd._low));
			changedTrade.put(CPDConstants.dayHighTime, trd.JavaTime(trd._highTime));
			changedTrade.put(CPDConstants.dayLowTime, trd.JavaTime(trd._lowTime));
			changedTrade.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(trd._openPrc));
			changedTrade.put(CPDConstants.openVol, trd._openVol);
			changedTrade.put(CPDConstants.openTime, trd.JavaTime(trd._openTime));

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(trd._trdPrc));
				legacyData.put(CPDConstants.tradeVolume, trd._trdVol);
				legacyData.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(trd._netChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(trd._pctChg));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(trd._trdTime));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(trd._trdTime));
				legacyData.put(CPDConstants.tickUpDown, upDown);
				legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
				legacyData.put(CPDConstants.totalVolume, trd._acVol);
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(trd._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(trd._low));
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(trd._openPrc));
				legacyData.put(CPDConstants.openVol, trd._openVol);
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			stats.incrementProcessedUCTradeMessages();
			String key = ticker + "|" + datacache.getOtcTradeProtocol();
			datacache.getTrademQueue().putAll(key, changedTrade);

			if (this.duallyQuotedTickerMap.containsKey(rootTicker)) {
				// dup trade bean, check if quotes exists
				ticker = rootTicker;
				if (datacache.isDelayed()) {
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				}
				changedTrade = new ConcurrentHashMap<>();
				legacyData = new ConcurrentHashMap<>();

				changedTrade.put(CPDConstants.ticker, ticker);
				changedTrade.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
				changedTrade.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(trd._trdPrc));
				changedTrade.put(CPDConstants.tradeVolume, trd._trdVol);
				changedTrade.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
				changedTrade.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(trd._netChg));
				changedTrade.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(trd._pctChg));
				changedTrade.put(CPDConstants.tradeTime, trd.MsgTimeMs());
				upDown = trd._prcTck == '\u0000' ? '-' : trd._prcTck;
				changedTrade.put(CPDConstants.tickUpDown, upDown);
				changedTrade.put(CPDConstants.vwap, CPDUtility.changeDoubletoLong(trd.vwap(4)));
				changedTrade.put(CPDConstants.totalVolume, trd._acVol);
				changedTrade.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(trd._high));
				changedTrade.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(trd._low));
				changedTrade.put(CPDConstants.dayHighTime, trd.JavaTime(trd._highTime));
				changedTrade.put(CPDConstants.dayLowTime, trd.JavaTime(trd._lowTime));
				changedTrade.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(trd._openPrc));
				changedTrade.put(CPDConstants.openVol, trd._openVol);
				changedTrade.put(CPDConstants.openTime, trd.JavaTime(trd._openTime));

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcbbTradeProtocol());
					legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(trd._trdPrc));
					legacyData.put(CPDConstants.tradeVolume, trd._trdVol);
					legacyData.put(CPDConstants.tradeExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(trd._netChg));
					legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(trd._pctChg));
					legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(trd.MsgTimeMs()));
					legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(trd.MsgTimeMs()));
					upDown = trd._prcTck == '\u0000' ? '-' : trd._prcTck;
					legacyData.put(CPDConstants.tickUpDown, upDown);
					legacyData.put(CPDConstants.vwap, CPDUtility.formatFourDecimals(trd.vwap(4)));
					legacyData.put(CPDConstants.totalVolume, trd._acVol);
					legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(trd._high));
					legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(trd._low));
					legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(trd._openPrc));
					legacyData.put(CPDConstants.openVol, trd._openVol);
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}

				stats.incrementProcessedUCTradeMessages();

				key = ticker + "|" + datacache.getOtcbbTradeProtocol();
				datacache.getTrademQueue().putAll(key, changedTrade);

			}
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
			if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "Regional Ticker " + ticker));
				return;
			}
			String rootTicker = ticker;
			String legacyTicker = ticker;
			ticker = ticker + datacache.getOtcSymbolSuffix();
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(ticker)) {
				stats.incrementDroppedTradeMessages();
				logger.info(CommonLogMessage.dropSymbol(streamName, "trd No Image " + trdSts.tkr()));
				return;
			}
			Map<String, Object> changedTrade = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();

			changedTrade.put(CPDConstants.ticker, ticker);
			changedTrade.put(CPDConstants.rtl, trdSts.RTL());
			changedTrade.put(CPDConstants.isHalted, trdSts._halted);
			changedTrade.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.rtl, trdSts.RTL());
				legacyData.put(CPDConstants.isHalted, trdSts._halted);
				legacyData.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}

			String key = ticker + "|" + datacache.getOtcTradeProtocol();
			stats.incrementProcessedUCTradeMessages();
			datacache.getTrademQueue().putAll(key, changedTrade);

			if (this.duallyQuotedTickerMap.containsKey(rootTicker)) {
				ticker = rootTicker;
				if (datacache.isDelayed()) {
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				}
				changedTrade = new ConcurrentHashMap<>();
				legacyData = new ConcurrentHashMap<>();
				changedTrade.put(CPDConstants.ticker, ticker);
				changedTrade.put(CPDConstants.rtl, trdSts.RTL());
				changedTrade.put(CPDConstants.isHalted, trdSts._halted);
				changedTrade.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.rtl, trdSts.RTL());
					legacyData.put(CPDConstants.isHalted, trdSts._halted);
					legacyData.put(CPDConstants.isSho, trdSts.IsShortSaleRestricted());
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}

				key = ticker + "|" + datacache.getOtcbbTradeProtocol();
				stats.incrementProcessedUCTradeMessages();
				datacache.getTrademQueue().putAll(key, changedTrade);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker.
	 */
	public void OnUpdate(final String streamName, EQQuote q) {
		try {
			this.messageCount++;
			String ticker = q.tkr();
			String legacyTicker = ticker;
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQQuote.tkr() is null"));
				return;
			}
			final int prot = q.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote protocol is " + prot));
				return;
			}
			if (q._bid == 0 && q._ask == 0 && q._bidSize == 0 && q._askSize == 0) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQQuote quotes are Zero"));
				return;
			}
			if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
				ticker = ticker.substring(0, ticker.indexOf("/"));
				ticker = ticker + datacache.getOtcSymbolSuffix();
				legacyTicker = ticker;
				if (datacache.isDelayed()) {
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				}
				if (!this.imageTickerMap.containsKey(ticker)) {
					stats.incrementDroppedQuoteMessages();
					logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
					return;
				}
				Map<String, Object> changedQuote = new ConcurrentHashMap<>();
				Map<String, Object> legacyData = new ConcurrentHashMap<>();

				changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(q._ask));
				changedQuote.put(CPDConstants.askSize, q._askSize);
				changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(q._bid));
				changedQuote.put(CPDConstants.bidSize, q._bidSize);
				changedQuote.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.ticker, ticker);
				changedQuote.put(CPDConstants.protocolId, datacache.getOtcQuoteProtocol());
				changedQuote.put(CPDConstants.quoteTime, q.MsgTimeMs());
				changedQuote.put(CPDConstants.rtl, q.RTL());

				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(q._ask));
					legacyData.put(CPDConstants.askSize, q._askSize);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(q._bid));
					legacyData.put(CPDConstants.bidSize, q._bidSize);
					legacyData.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcQuoteProtocol());
					legacyData.put(CPDConstants.quoteTime, q.MsgTimeMs());
					legacyData.put(CPDConstants.rtl, q.RTL());
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}

				stats.incrementProcessedUCQuoteMessages();
				String key = ticker + "|" + datacache.getOtcQuoteProtocol();
				datacache.getQuotemQueue().putAll(key, changedQuote);

			} else {
				if (!this.imageTickerMap.containsKey(ticker)) {
					stats.incrementDroppedQuoteMessages();
					logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
					return;
				}
				Map<String, Object> changedQuote = new ConcurrentHashMap<>();
				changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(q._ask));
				changedQuote.put(CPDConstants.askSize, q._askSize);
				changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(q._bid));
				changedQuote.put(CPDConstants.bidSize, q._bidSize);
				changedQuote.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.ticker, ticker);
				changedQuote.put(CPDConstants.protocolId, datacache.getOtcbbQuoteProtocol());
				changedQuote.put(CPDConstants.quoteTime, q.MsgTimeMs());
				changedQuote.put(CPDConstants.rtl, q.RTL());

				if (datacache.isLegacyDistribution()) {
					Map<String, Object> legacyData = new ConcurrentHashMap<>();
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(q._ask));
					legacyData.put(CPDConstants.askSize, q._askSize);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(q._bid));
					legacyData.put(CPDConstants.bidSize, q._bidSize);
					legacyData.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcbbQuoteProtocol());
					legacyData.put(CPDConstants.quoteTime, q.MsgTimeMs());
					legacyData.put(CPDConstants.rtl, q.RTL());
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}

				stats.incrementProcessedUCQuoteMessages();
				String key = ticker + "|" + datacache.getOtcbbQuoteProtocol();
				datacache.getQuotemQueue().putAll(key, changedQuote);

			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	public void OnUpdate(String streamName, EQBbo q) {
		try {
			this.messageCount++;
			String ticker = q.tkr();
			String legacyTicker = ticker;
			if (ticker == null) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQQuote.tkr() is null"));
				return;
			}
			int prot = q.protocol();
			if (!this.allowedProtocols.contains(prot)) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote protocol is " + prot));
				return;
			}
			if (q._bid == 0 && q._ask == 0 && q._bidSize == 0 && q._askSize == 0) {
				stats.incrementDroppedQuoteMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQQuote quotes are Zero"));
				return;
			}
			if (CPDTickerValidator.isEquityRegionalSymbol(ticker)) {
				ticker = ticker.substring(0, ticker.indexOf("/"));
				ticker = ticker + datacache.getOtcSymbolSuffix();
				legacyTicker = ticker;
				if (datacache.isDelayed()) {
					ticker = ticker + CPDConstants.DELAYED_SUFFIX;
				}
				if (!this.imageTickerMap.containsKey(ticker)) {
					stats.incrementDroppedQuoteMessages();
					logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
					return;
				}
				Map<String, Object> changedQuote = new ConcurrentHashMap<>();
				changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(q._ask));
				changedQuote.put(CPDConstants.askSize, q._askSize);
				changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(q._bid));
				changedQuote.put(CPDConstants.bidSize, q._bidSize);
				changedQuote.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.ticker, ticker);
				changedQuote.put(CPDConstants.protocolId, datacache.getOtcQuoteProtocol());
				changedQuote.put(CPDConstants.quoteTime, q.MsgTimeMs());
				changedQuote.put(CPDConstants.rtl, q.RTL());

				if (datacache.isLegacyDistribution()) {
					Map<String, Object> legacyData = new ConcurrentHashMap<>();
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(q._ask));
					legacyData.put(CPDConstants.askSize, q._askSize);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(q._bid));
					legacyData.put(CPDConstants.bidSize, q._bidSize);
					legacyData.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcQuoteProtocol());
					legacyData.put(CPDConstants.quoteTime, q.MsgTimeMs());
					legacyData.put(CPDConstants.rtl, q.RTL());
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}
				stats.incrementProcessedUCQuoteMessages();

				String key = ticker + "|" + datacache.getOtcQuoteProtocol();
				datacache.getQuotemQueue().putAll(key, changedQuote);
			} else {
				if (!this.imageTickerMap.containsKey(ticker)) {
					stats.incrementDroppedQuoteMessages();
					logger.info(CommonLogMessage.dropSymbol(streamName, "quote No Image " + ticker));
					return;
				}
				Map<String, Object> changedQuote = new ConcurrentHashMap<>();
				changedQuote.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(q._ask));
				changedQuote.put(CPDConstants.askSize, q._askSize);
				changedQuote.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(q._bid));
				changedQuote.put(CPDConstants.bidSize, q._bidSize);
				changedQuote.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
				changedQuote.put(CPDConstants.ticker, ticker);
				changedQuote.put(CPDConstants.protocolId, datacache.getOtcbbQuoteProtocol());
				changedQuote.put(CPDConstants.quoteTime, q.MsgTimeMs());
				changedQuote.put(CPDConstants.rtl, q.RTL());

				if (datacache.isLegacyDistribution()) {
					Map<String, Object> legacyData = new ConcurrentHashMap<>();
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(q._ask));
					legacyData.put(CPDConstants.askSize, q._askSize);
					legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(q._bid));
					legacyData.put(CPDConstants.bidSize, q._bidSize);
					legacyData.put(CPDConstants.askExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.bidExchange, PINK_SHEET_MARKET_CENTER);
					legacyData.put(CPDConstants.ticker, legacyTicker);
					legacyData.put(CPDConstants.protocolId, datacache.getOtcbbQuoteProtocol());
					legacyData.put(CPDConstants.quoteTime, q.MsgTimeMs());
					legacyData.put(CPDConstants.rtl, q.RTL());
					if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
						datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
						datacache.getLegacymQueue().putAll(legacyTicker,
								datacache.getCachedLegacyData().get(legacyTicker));
					}
				}
				stats.incrementProcessedUCQuoteMessages();
				String key = ticker + "|" + datacache.getOtcbbQuoteProtocol();
				datacache.getQuotemQueue().putAll(key, changedQuote);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQQuoteMM update is received for an pink sheet ticker.
	 */
	@Override
	public void OnUpdate(String streamName, EQBboMM q) {
		this.messageCount++;
		// No EQBboMM quotes for otc
		logger.info(() -> CommonLogMessage.dropSymbol(streamName, "EQBboMM not processed " + q.tkr()));
	}

}
