package com.quodd.mf;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;
import static com.quodd.mf.MfCPD.datacache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDUtility;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.FUNDnav;
import QuoddFeed.msg.Image;

public class MfSubsChannel extends CPDChannel {

	private static final String EXCHANGE_NAME = "NASDAQ Mutual Funds";
	private static final Long DEFAULT_LONG_VALUE = Long.valueOf(1);
	private final ConcurrentMap<String, String> imageTickerMap = datacache.getImageTickerMap();

	public MfSubsChannel(String ip, int port, String name) {
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
	public void OnImage(String streamName, Image image) {
		try {
			this.messageCount++;
			String ticker = image.tkr();
			if (ticker == null) {
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "img.tkr() is null"));
				return;
			}

			this.imageSet.add(ticker);
			datacache.getImageTickerMap().put(ticker, ticker);
			datacache.getPendingTickerMap().remove(ticker);
			stats.incrementProcessedUCTradeMessages();
			Map<String, Object> changedData = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();

			changedData.put(CPDConstants.ticker, ticker);
			changedData.put(CPDConstants.rootTicker, ticker);

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, ticker);
				legacyData.put(CPDConstants.rootTicker, ticker);
			}

			char fundType = image._fundType;
			char fundCode = image._fundCode;
			if ((fundType == '2' || fundType == '4') && (fundCode == 'A' || fundCode == 'G' || fundCode == 'X')) {
				changedData.put(CPDConstants.lastPrice, DEFAULT_LONG_VALUE);
				changedData.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(image._yield7DayAnnualized));
				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.lastPrice, DEFAULT_LONG_VALUE);
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(image._yield7DayAnnualized));
				}
			} else {
				changedData.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(image._fundNav));
				changedData.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(image._fundPrc));
				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(image._fundNav));
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(image._fundPrc));
				}
			}
			changedData.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(image._fundClose));
			changedData.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(image._fundNetChg));
			changedData.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(image._fundPctChg));
			changedData.put(CPDConstants.protocolId, datacache.getServiceCode());
			changedData.put(CPDConstants.tradeExchange, EXCHANGE_NAME);
			changedData.put(CPDConstants.tradeTime, image._trdTime);
			changedData.put(CPDConstants.ucProtocol, image.protocol());
			logger.info(() -> CommonLogMessage.image(this.name, image.tkr(), image._trdPrc, image._ask, image._bid,
					image.protocol()));
			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(image._fundClose));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(image._fundNetChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(image._fundPctChg));
				legacyData.put(CPDConstants.protocolId, datacache.getServiceCode());
				legacyData.put(CPDConstants.tradeExchange, EXCHANGE_NAME);
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(image._trdTime));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(image._trdTime));
				legacyData.put(CPDConstants.vwap, 0l);
				legacyData.put(CPDConstants.ucProtocol, image.protocol());
				Map<String, Object> completeMap = datacache.getCachedLegacyData().get(ticker);
				if (completeMap == null) {
					completeMap = new ConcurrentHashMap<>();
				}
				completeMap.putAll(legacyData);
				datacache.getCachedLegacyData().put(ticker, completeMap);
				datacache.getLegacymQueue().putAll(ticker, completeMap);
			}

			String key = ticker + "|" + datacache.getServiceCode();
			datacache.getTrademQueue().putAll(key, changedData);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker.
	 */
	@Override
	public void OnUpdate(String streamName, FUNDnav nav) {
		this.messageCount++;
		String tkr = nav.tkr();
		try {
			if (tkr == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "FUNDnav.tkr() is null"));
				return;
			}
			if (!this.imageTickerMap.containsKey(tkr)) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "quote No Image " + tkr));
				return;
			}
			if ((nav._flags & 0x4) != 0x4) {
				logger.info(() -> CommonLogMessage.dropSymbol(tkr,
						"FUND last:" + nav._nav + " _distroType:" + nav._distroType + " _flags: " + nav._flags
								+ " totalCashDist : " + nav._totalCashDist + " exDate: " + nav._exDate));
				return;
			}
			char fundType = nav._fundType;
			char fundCode = nav._fundCode;
			stats.incrementProcessedUCTradeMessages();
			Map<String, Object> changedData = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();

			changedData.put(CPDConstants.ticker, tkr);
			changedData.put(CPDConstants.rootTicker, tkr);
			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, tkr);
				legacyData.put(CPDConstants.rootTicker, tkr);
			}
			if ((fundType == '2' || fundType == '4') && (fundCode == 'A' || fundCode == 'G' || fundCode == 'X')) {
				changedData.put(CPDConstants.lastPrice, DEFAULT_LONG_VALUE);
				changedData.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(nav._yield7DayAnnualized));
				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.lastPrice, DEFAULT_LONG_VALUE);
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(nav._yield7DayAnnualized));
				}
			} else {
				changedData.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(nav._nav));
				changedData.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(nav._price));
				if (datacache.isLegacyDistribution()) {
					legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(nav._nav));
					legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(nav._price));
				}
			}

			changedData.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(nav._close));
			changedData.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(nav._netChg));
			changedData.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(nav._pctChg));
			changedData.put(CPDConstants.protocolId, datacache.getServiceCode());
			changedData.put(CPDConstants.tradeExchange, EXCHANGE_NAME);
			changedData.put(CPDConstants.tradeTime, nav.MsgTimeMs());
			logger.info("Mf updates: " + tkr + " last:" + nav._nav + " ask: " + nav._price + " close: " + nav._close
					+ " net chg :" + nav._netChg + " pct chg: " + nav._pctChg + " prot:" + nav.protocol()
					+ " _distroType:" + nav._distroType + " _flags: " + nav._flags + " " + nav._fundType + " "
					+ nav._fundCode);

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(nav._close));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(nav._netChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(nav._pctChg));
				legacyData.put(CPDConstants.protocolId, datacache.getServiceCode());
				legacyData.put(CPDConstants.tradeExchange, EXCHANGE_NAME);
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(nav.MsgTimeMs()));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(nav.MsgTimeMs()));
				if (datacache.getCachedLegacyData().get(tkr) != null) {
					datacache.getCachedLegacyData().get(tkr).putAll(legacyData);
					datacache.getLegacymQueue().putAll(tkr, datacache.getCachedLegacyData().get(tkr));
				}
			}

			String key = tkr + "|" + datacache.getServiceCode();
			datacache.getTrademQueue().putAll(key, changedData);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}