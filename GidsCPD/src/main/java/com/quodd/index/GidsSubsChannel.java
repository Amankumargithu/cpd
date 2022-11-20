package com.quodd.index;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.common.cpd.CPD.stats;
import static com.quodd.index.GidsCPD.datacache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.quodd.common.cpd.channel.CPDChannel;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDUtility;
import com.quodd.common.logger.CommonLogMessage;

import QuoddFeed.msg.IDXSummary;
import QuoddFeed.msg.IDXValue;
import QuoddFeed.msg.Image;

public class GidsSubsChannel extends CPDChannel {

	private final ConcurrentMap<String, String> imageTickerMap = datacache.getImageTickerMap();

	public GidsSubsChannel(String ip, int port, String name) {
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
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "image.tkr() is null"));
				return;
			}
			String suffix = null;
			try {
				suffix = ticker.substring(ticker.length() - 3);
			} catch (Exception e) {
				suffix = "";
			}
			String legacyTicker = ticker;
			if (datacache.isDelayed()) {
				ticker = ticker + CPDConstants.DELAYED_SUFFIX;
			}
			logger.info(() -> CommonLogMessage.image(this.name, image.tkr(), image._trdPrc, image._ask, image._bid,
					image.protocol()));
			this.imageSet.add(ticker);
			this.imageTickerMap.put(ticker, ticker);
			datacache.getPendingTickerMap().remove(ticker);
			stats.incrementProcessedUCTradeMessages();
			Map<String, Object> legacyData = new ConcurrentHashMap<>();
			Map<String, Object> changedData = new ConcurrentHashMap<>();
			changedData.put(CPDConstants.ticker, ticker);
			changedData.put(CPDConstants.rootTicker, ticker);
			changedData.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(image._open));
			changedData.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(image._high));
			changedData.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(image._low));
			changedData.put(CPDConstants.previousClose, CPDUtility.changeDoubletoLong(image._close));
			changedData.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(image._netChg));
			changedData.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(image._pctChg));
			changedData.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(image._ask));
			changedData.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(image._bid));
			changedData.put(CPDConstants.ucProtocol, image.protocol());

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.ticker, legacyTicker);
				legacyData.put(CPDConstants.ucProtocol, image.protocol());
				legacyData.put(CPDConstants.rootTicker, ticker);
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(image._open));
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(image._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(image._low));
				legacyData.put(CPDConstants.previousClose, CPDUtility.formatFourDecimals(image._close));
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(image._netChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(image._pctChg));
				legacyData.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(image._ask));
				legacyData.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(image._bid));
				legacyData.put(CPDConstants.bidSize, 0.0d);
				legacyData.put(CPDConstants.askSize, 0.0d);
				legacyData.put(CPDConstants.totalVolume, 0l);
				legacyData.put(CPDConstants.tradeVolume, 0l);
				legacyData.put(CPDConstants.extLastPrice, 0.0d);
				legacyData.put(CPDConstants.extPerChg, 0.0d);
				legacyData.put(CPDConstants.extChangePrc, 0.0d);
				legacyData.put(CPDConstants.extTradeVol, 0);
				legacyData.put(CPDConstants.exchangeCode, getExchangeCode(image.protocol()));
				legacyData.put(CPDConstants.askExchange, " ");
				legacyData.put(CPDConstants.bidExchange, " ");
				legacyData.put(CPDConstants.vwap, " ");
				legacyData.put(CPDConstants.extTradeDate,0);
				legacyData.put(CPDConstants.extTradeTime,0);
				legacyData.put(CPDConstants.limitUpDown, " ");
				legacyData.put(CPDConstants.isSho, false);
				legacyData.put(CPDConstants.isHalted, false);
				legacyData.put(CPDConstants.rtl, image.RTL());
				legacyData.put(CPDConstants.tickUpDown, "F");
				legacyData.put(CPDConstants.extTickUpDown, "F");

			}
			double lastPrice = 0.0d;
			switch (suffix) {
			case ".IV":
				lastPrice = image._ETPIntraDay;
				break;
			case ".NV":
				lastPrice = image._ETPNav;
				break;
			case ".EU":
				lastPrice = image._ETPEstCashPCU;
				break;
			case ".TC":
				lastPrice = image._ETPTotCashPCU;
				break;
			case ".DV":
				lastPrice = image._ETPDividend;
				break;
			case ".SO":
				lastPrice = image._ETPSharesOut;
				break;
			default:
				lastPrice = image._value;
				break;
			}
			if (lastPrice != 0.0) {
				changedData.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(lastPrice));
			}
			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(lastPrice));
			}
			
			changedData.put(CPDConstants.tapeSet, datacache.getTapeset());
			changedData.put(CPDConstants.protocolId, getServiceCode(ticker));
			changedData.put(CPDConstants.tradeExchange, " ");
			changedData.put(CPDConstants.tradeTime, image.JavaTime(image._tUpd));

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.tradeExchange, getExchangeCode(image.protocol()));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(image._tUpd));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(image.JavaTime(image._tUpd)));
				Map<String, Object> completeMap = datacache.getCachedLegacyData().get(legacyTicker);
				if (completeMap == null) {
					completeMap = new ConcurrentHashMap<>();
				}
				completeMap.putAll(legacyData);
				datacache.getCachedLegacyData().put(legacyTicker, completeMap);
				datacache.getLegacymQueue().putAll(legacyTicker, completeMap);
			}
			String key = ticker + "|" + getServiceCode(ticker);
			datacache.getTrademQueue().putAll(key, changedData);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an IDXValue update is received for an index ticker.
	 */
	public void OnUpdate(String streamName, IDXValue val) {
		try {
			this.messageCount++;
			String tkr = val.tkr();
			if (tkr == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "IDXValue.tkr() is null"));
				return;
			}
			String legacyTicker = tkr;
			if (datacache.isDelayed()) {
				tkr = tkr + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(tkr)) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "IDXValue No Image " + val.tkr()));
				return;
			}
			Map<String, Object> changedData = new ConcurrentHashMap<>();
			Map<String, Object> legacyData = datacache.getCachedLegacyData().getOrDefault(tkr, new ConcurrentHashMap<>());

			changedData.put(CPDConstants.openPrice, CPDUtility.changeDoubletoLong(val._open));
			changedData.put(CPDConstants.dayHigh, CPDUtility.changeDoubletoLong(val._high));
			changedData.put(CPDConstants.dayLow, CPDUtility.changeDoubletoLong(val._low));

			if (datacache.isLegacyDistribution()) {
				legacyData.put(CPDConstants.openPrice, CPDUtility.formatFourDecimals(val._open));
				legacyData.put(CPDConstants.dayHigh, CPDUtility.formatFourDecimals(val._high));
				legacyData.put(CPDConstants.dayLow, CPDUtility.formatFourDecimals(val._low));
			}
			String calc = val.calcMethod();
			String ke = tkr + "|" + getServiceCode(tkr);
			Map<String, Object> dataMap = datacache.getCachedTradeData().get(ke);
			if (dataMap == null) {
				logger.info("Missing data for key : " + ke);
				return;
			}
			updateLastConditionaly(dataMap, val, calc);

			stats.incrementProcessedUCTradeMessages();
			changedData.put(CPDConstants.lastPrice, dataMap.get(CPDConstants.lastPrice));
			if (calc.equals("LAST")) {
				changedData.put(CPDConstants.lastPrice, dataMap.get(CPDConstants.lastPrice));
			} else if (calc.equals("ASK")) {
				changedData.put(CPDConstants.askPrice, dataMap.get(CPDConstants.askPrice));
			} else if (calc.equals("BID")) {
				changedData.put(CPDConstants.bidPrice, dataMap.get(CPDConstants.bidPrice));
			}
			changedData.put(CPDConstants.changePrice, CPDUtility.changeDoubletoLong(val._netChg));
			changedData.put(CPDConstants.percentChange, CPDUtility.changeDoubletoLong(val._pctChg));
			changedData.put(CPDConstants.protocolId, getServiceCode(tkr));
			changedData.put(CPDConstants.tradeTime, val.MsgTimeMs());

			if (datacache.isLegacyDistribution()) {
				updateLegacyLastConditionaly(legacyData, val, calc);
				legacyData.put(CPDConstants.changePrice, CPDUtility.formatFourDecimals(val._netChg));
				legacyData.put(CPDConstants.percentChange, CPDUtility.formatFourDecimals(val._pctChg));
//				legacyData.put(CPDConstants.protocolId, getServiceCode(tkr));
				legacyData.put(CPDConstants.tradeDate, CPDUtility.processDate(val.MsgTimeMs()));
				legacyData.put(CPDConstants.tradeTime, CPDUtility.processTime(val.MsgTimeMs()));
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}
			String key = tkr + "|" + getServiceCode(tkr);
			datacache.getTrademQueue().putAll(key, changedData);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Called when an IDXSummary update is received for an index ticker.
	 */
	public void OnUpdate(String streamName, IDXSummary q) {
		try {
			this.messageCount++;
			String tkr = q.tkr();
			if (tkr == null) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "IDXSummary.tkr() is null"));
				return;
			}
			String legacyTicker = tkr;
			if (datacache.isDelayed()) {
				tkr = tkr + CPDConstants.DELAYED_SUFFIX;
			}
			if (!this.imageTickerMap.containsKey(tkr)) {
				stats.incrementDroppedTradeMessages();
				logger.info(() -> CommonLogMessage.dropSymbol(streamName, "IDXSummary No Image " + q.tkr()));
				return;
			}
			Map<String, Object> changedData = new ConcurrentHashMap<>();
			changedData.put(CPDConstants.protocolId, getServiceCode(tkr));
			changedData.put(CPDConstants.ticker, tkr);

			if (datacache.isLegacyDistribution()) {
				Map<String, Object> legacyData = new ConcurrentHashMap<>();
//				legacyData.put(CPDConstants.protocolId, getServiceCode(tkr));
				legacyData.put(CPDConstants.ticker, legacyTicker);
				if (datacache.getCachedLegacyData().get(legacyTicker) != null) {
					datacache.getCachedLegacyData().get(legacyTicker).putAll(legacyData);
					datacache.getLegacymQueue().putAll(legacyTicker, datacache.getCachedLegacyData().get(legacyTicker));
				}
			}

			stats.incrementProcessedUCTradeMessages();
			String key = tkr + "|" + getServiceCode(tkr);
			datacache.getTrademQueue().putAll(key, changedData);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void updateLastConditionaly(Map<String, Object> bean, IDXValue val, String calc) {
		double dVal = val.dVal();
		if (val.IsETPDividend()) {
			dVal = val.ETPDividend();
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		} else if (val.IsETPEstimatedCashPCU()) {
			dVal = val.ETPEstimatedCashPCU();
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		} else if (val.IsETPIntradayValue()) {
			dVal = val.ETPIntradayValue();
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		} else if (val.IsETPNetAssetValue()) {
			dVal = val.ETPNetAssetValue();
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		} else if (val.IsETPSharesOutstanding()) {
			dVal = val.ETPSharesOutstanding();
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		} else if (val.IsETPTotalCashPCU()) {
			dVal = val.ETPTotalCashPCU();
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		}
		if (calc.equals("LAST"))
			bean.put(CPDConstants.lastPrice, CPDUtility.changeDoubletoLong(dVal));
		else if (calc.equals("ASK"))
			bean.put(CPDConstants.askPrice, CPDUtility.changeDoubletoLong(dVal));
		else if (calc.equals("BID"))
			bean.put(CPDConstants.bidPrice, CPDUtility.changeDoubletoLong(dVal));
	}

	private void updateLegacyLastConditionaly(Map<String, Object> bean, IDXValue val, String calc) {
		double dVal = val.dVal();
		if (val.IsETPDividend()) {
			dVal = val.ETPDividend();
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		} else if (val.IsETPEstimatedCashPCU()) {
			dVal = val.ETPEstimatedCashPCU();
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		} else if (val.IsETPIntradayValue()) {
			dVal = val.ETPIntradayValue();
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		} else if (val.IsETPNetAssetValue()) {
			dVal = val.ETPNetAssetValue();
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		} else if (val.IsETPSharesOutstanding()) {
			dVal = val.ETPSharesOutstanding();
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		} else if (val.IsETPTotalCashPCU()) {
			dVal = val.ETPTotalCashPCU();
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		}
		if (calc.equals("LAST"))
			bean.put(CPDConstants.lastPrice, CPDUtility.formatFourDecimals(dVal));
		else if (calc.equals("ASK"))
			bean.put(CPDConstants.askPrice, CPDUtility.formatFourDecimals(dVal));
		else if (calc.equals("BID"))
			bean.put(CPDConstants.bidPrice, CPDUtility.formatFourDecimals(dVal));
	}

	private String getExchangeCode(int protocol) {
		switch (protocol) {
		case 50:
			return "INDEX OPRA";
		case 47:
			return "INDEX Dow Jones";
		case 40:
			return "INDEX NASDAQ (GIDS)";
		case 42:
			return "INDEX CBOE";
		case 41:
			return "INDEX NYSE GIF";
		case 46:
			return "INDEX S&P";
		default:
			return "N.A";
		}
	}

	private int getServiceCode(String ticker) {
		if (datacache.getGidsTickerMap().get(ticker) != null)
			return datacache.getServiceCodeGids();
		return 0;
	}
}
