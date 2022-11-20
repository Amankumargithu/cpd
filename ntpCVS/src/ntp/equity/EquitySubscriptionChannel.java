package ntp.equity;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.EQBbo;
import QuoddFeed.msg.EQBboMM;
import QuoddFeed.msg.EQQuote;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.EQTradeSts;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.MsgTypes;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.bean.QTCPDMessageBean;
import ntp.equity.subs.EquityQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.ExchangeMapPopulator;
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import ntp.util.StockRetriever;

public class EquitySubscriptionChannel extends UltraChan {

	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;
	private HashMap<String, String> exchgMap = ExchangeMapPopulator.getInstance().getExchangeMap();
	private ConcurrentHashMap<String, String> imageTickerMap = EquityQTMessageQueue.getInstance().getImageTickerMap();
	private boolean isNasdaqbasic = false;
	private boolean isExtendedEval = true;
	private String exchgFilter[];
	private final String NASDAQ_BASIC_SUFFIX = "/T";

	public EquitySubscriptionChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
		String nasdaqBasicStr = CPDProperty.getInstance().getProperty("IS_NASDAQ_BASIC");
		nasdaqBasicStr = (nasdaqBasicStr == null) ? "false" : nasdaqBasicStr;
		if (nasdaqBasicStr.equalsIgnoreCase("TrUe")) {
			isNasdaqbasic = true;
			String exchangeFilter = CPDProperty.getInstance().getProperty("EXCHANGE_FILTER");
			exchangeFilter = (exchangeFilter == null) ? "20,21" : exchangeFilter;
			exchgFilter = exchangeFilter.split(",");
		}
		String evaluateExtended = CPDProperty.getInstance().getProperty("IS_EXTENDED_EVAL");
		isExtendedEval = evaluateExtended == null ? true : evaluateExtended.equalsIgnoreCase("TRUE") ? true : false;
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public void subscribe(String ticker) {
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
	}

	public void subscribeTickers(final Collection<String> tickerList) {
		new Thread(new Runnable() {
			public void run() {
				int count = 0;
				int threshold = 200;
				int sleepTime = 500;
				try {
					threshold = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD"));
					sleepTime = Integer.parseInt(CPDProperty.getInstance().getProperty("SLEEP_TIME"));
				} catch (Exception e) {
					NTPLogger.missingProperty("THRESHOLD");
					NTPLogger.defaultSetting("THRESHOLD", "200");
					NTPLogger.missingProperty("SLEEP_TIME");
					NTPLogger.defaultSetting("SLEEP_TIME", "500");
					threshold = 200;
					sleepTime = 500;
				}
				for (String ticker : tickerList) {
					count++;
					subscribe(ticker);
					if (count % threshold == 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (Exception e) {
							NTPLogger.warning("EquitySubscriptionChannel: exception while threshold thread sleep");
						}
					}
				}
				NTPLogger.info("Completed Subscriptions for " + name);
			}
		}, "SUBSCRIPTION_" + name).start();
	}

	////////////////////////////////////////////////
	//// CALL BACK METHODS
	///////////////////////////////////////////////

	@Override
	public void OnConnect() {
		NTPLogger.onConnectUC(name);
	}

	@Override
	public void OnDisconnect() {
		NTPLogger.onDisconnectUC(name);
	}

	@Override
	public void OnSession(String txt, boolean bUP) {
		NTPLogger.onSessionUC(name, txt, bUP);
	}

	@Override
	public void OnStatus(String StreamName, Status sts) {
		char mt = sts.mt();
		if (mt == UltraChan._mtDEAD) {
			String ticker = sts.tkr();
			if (ticker != null)
				NTPLogger.dead(ticker, name);
		} else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	public void OnImage(String StreamName, Image img) {
		String ticker = img.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		int prot = img.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "img protocol is " + prot);
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (ticker.startsWith("T:"))
			ticker = ticker.substring(2);
		if (isNasdaqbasic) {
			if (isProtocolExcluded(prot)) {
				NTPLogger.dropSymbol(StreamName, "img NB Excluded protocol " + ticker);
				NTPUtility.unsubscribeTicker(ticker);
				return;
			}
			if (ticker.toUpperCase().endsWith(NASDAQ_BASIC_SUFFIX)) {
				// Populate Quotes from /T symbols - only for Nasdaq basic
				ticker = ticker.substring(2, ticker.length() - 2);
				QTCPDMessageBean bean = getCachedBean(ticker);
				NTPLogger.image(name, img);
				bean.setAskExchangeCode(" ");
				bean.setBidExchangeCode(" ");
				bean.setExchangeId("16");
				bean.setAskPrice(img._ask);
				bean.setAskSize(img._askSize);
				bean.setBidPrice(img._bid);
				bean.setBidSize(img._bidSize);
				bean.setSystemTicker(ticker);
				bean.setTicker(ticker);
				NTPLogger.info("Adding NB Image in queue for " + bean.getTicker());
				imageTickerMap.put(bean.getTicker(), bean.getTicker());
				EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
				return;
			} else {
				if (NTPTickerValidator.isEquityRegionalSymbol(ticker))

				{
					NTPLogger.dropSymbol(StreamName, "img NB Incorrect symbol " + ticker);
					NTPUtility.unsubscribeTicker(ticker);
					return;
				}
				// Populate trade info from composite symbols - Only for Nasdaq basic
				QTCPDMessageBean bean = getCachedBean(ticker);
				NTPLogger.image(name, img);
				if (isExtendedEval) {
					bean.setExtendedLastPrice(img._trdPrc_ext);
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(img._trdTime_ext);
					if (isToday(c)) {
						bean.setExtendedChangePrice(img._netChg_ext);
						bean.setExtendedPercentChange(img._pctChg_ext);
					} else {
						bean.setExtendedChangePrice(0);
						bean.setExtendedPercentChange(0);
					}
					bean.setExtendedLastTradeVolume(img._trdVol_ext);
					DateTimeUtility.processExtendedDate(bean, img._trdTime_ext);
					bean.setExtendedMarketCenter(getEquityPlusExchangeCode(img._trdMktCtr_ext));
					bean.setExtendedTickUpDown(img._prcTck_ext);
				}
				bean.setLastPrice(img._trdPrc);
				bean.setLastTradeVolume(img._trdVol);
				bean.setDayHigh(img._high);
				bean.setDayLow(img._low);
				bean.setOpenPrice(img._open);
				bean.setVWAP(img.vwap(4) + "");
				bean.setVolume(img._acVol);
				bean.setChangePrice(img._netChg);
				bean.setPercentChange(img._pctChg);
				bean.setTickUpDown(img._prcTck);
				DateTimeUtility.processDate(bean, img._trdTime);
				bean.setMarketCenter(" ");
				bean.setExchangeId("16");
				bean.setLastClosedPrice(img._close);
				if (img._regSHO == MsgTypes._eqSubTRDCXL || img._regSHO == MsgTypes._eqSubTRDCORR || img._regSHO == 0x01
						|| img._regSHO == 0x02) {
					NTPLogger.info("Image Ticker: " + ticker + " SHO: " + true);
					bean.setSHO(true);
				} else {
					bean.setSHO(false);
					NTPLogger.info("Image Ticker: " + ticker + " SHO: " + Integer.toHexString(img._regSHO));
				}
				if (img._halted == 0x01) {
					NTPLogger.info("Image Ticker: " + ticker + " HLT: " + true);
					bean.setHalted(true);
				} else {
					bean.setHalted(false);
					NTPLogger.info(
							"Image NB Ticker: " + ticker + " HLT: " + false + " " + Integer.toHexString(img._halted));
				}
				bean.setLimitUpDown(NTPUtility.formatLimitUpDown(img.LimitUpDown()));
				bean.setSystemTicker(ticker);
				bean.setTicker(ticker);
				String exchangeCode = NTPUtility.getMappedExchangeCode(img._priMktCtr);
				bean.setExchangeCode(exchangeCode);
				NTPLogger.info("Adding Image NB in queue for " + bean.getTicker());
				imageTickerMap.put(bean.getTicker(), bean.getTicker());
				EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
				return;
			}
		} else {
			if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
				NTPLogger.dropSymbol(StreamName, "img Incorrect symbol " + ticker);
				NTPUtility.unsubscribeTicker(ticker);
				return;
			}
			QTCPDMessageBean bean = getCachedBean(ticker);
			NTPLogger.image(name, img);
			if (isExtendedEval) {
				bean.setExtendedLastPrice(img._trdPrc_ext);
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(img._trdTime_ext);
				if (isToday(c)) {
					bean.setExtendedChangePrice(img._netChg_ext);
					bean.setExtendedPercentChange(img._pctChg_ext);
				} else {
					bean.setExtendedChangePrice(0);
					bean.setExtendedPercentChange(0);
				}
				bean.setExtendedLastTradeVolume(img._trdVol_ext);
				DateTimeUtility.processExtendedDate(bean, img._trdTime_ext);
				bean.setExtendedMarketCenter(getEquityPlusExchangeCode(img._trdMktCtr_ext));
				bean.setExtendedTickUpDown(img._prcTck_ext);
			}
			bean.setLastPrice(img._trdPrc);
			bean.setLastTradeVolume(img._trdVol);
			bean.setDayHigh(img._high);
			bean.setDayLow(img._low);
			bean.setAskPrice(img._ask);
			bean.setAskSize(img._askSize);
			bean.setBidPrice(img._bid);
			bean.setBidSize(img._bidSize);
			bean.setOpenPrice(img._open);
			bean.setVWAP(img.vwap(4) + "");
			bean.setVolume(img._acVol);
			bean.setChangePrice(img._netChg);
			bean.setPercentChange(img._pctChg);
			bean.setTickUpDown(img._prcTck);
			DateTimeUtility.processDate(bean, img._trdTime);
			bean.setMarketCenter(getEquityPlusExchangeCode(img._trdMktCtr));
			bean.setAskExchangeCode(getEquityPlusExchangeCode(img._askMktCtr));
			bean.setBidExchangeCode(getEquityPlusExchangeCode(img._bidMktCtr));
			bean.setExchangeId(prot + "");
			bean.setLastClosedPrice(img._close);
			if (img._regSHO == MsgTypes._eqSubTRDCXL || img._regSHO == MsgTypes._eqSubTRDCORR || img._regSHO == 0x01
					|| img._regSHO == 0x02) {
				NTPLogger.info("Image Ticker: " + ticker + " SHO: " + true);
				bean.setSHO(true);
			} else {
				bean.setSHO(false);
				NTPLogger.info("Image Ticker: " + ticker + " SHO: " + Integer.toHexString(img._regSHO));
			}
			if (img._halted == 0x01) {
				NTPLogger.info("Image Ticker: " + ticker + " HLT: " + true);
				bean.setHalted(true);
			} else {
				bean.setHalted(false);
				NTPLogger.info("Image Ticker: " + ticker + " HLT: " + false + " " + Integer.toHexString(img._halted));
			}
			bean.setLimitUpDown(NTPUtility.formatLimitUpDown(img.LimitUpDown()));
			bean.setSystemTicker(ticker);
			bean.setTicker(ticker);
			String exchangeCode = NTPUtility.getMappedExchangeCode(img._priMktCtr);
			bean.setExchangeCode(exchangeCode);
			NTPLogger.info("Adding Image in queue for " + bean.getTicker());
			imageTickerMap.put(bean.getTicker(), bean.getTicker());
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
		}
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, EQQuote quote) {
		// For Canadian Symbols
		String ticker = quote.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "quote.tkr() is null");
			return;
		}
		if (!isNasdaqbasic && !NTPTickerValidator.isCanadianStock(ticker)) {
			NTPLogger.dropSymbol(StreamName, "quote.tkr() is not Canandian");
			return;
		}
		int prot = quote.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "quote protocol is " + prot);
			return;
		}
		if (quote._bid == 0 && quote._ask == 0 && quote._bidSize == 0 && quote._askSize == 0) {
			NTPLogger.dropSymbol(StreamName, "quote quotes are Zero");
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (ticker.toUpperCase().endsWith(NASDAQ_BASIC_SUFFIX) && isNasdaqbasic)
			ticker = ticker.substring(2, ticker.length() - 2);
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "quote Incorrect symbol " + ticker);
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "quote No Image " + ticker);
			return;
		}
		if (isNasdaqbasic && isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "quote Excluded protocol " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(quote._bid);
		bean.setAskPrice(quote._ask);
		bean.setBidSize(quote._bidSize);
		bean.setAskSize(quote._askSize);
		if (isNasdaqbasic) {
			bean.setAskExchangeCode(" ");
			bean.setBidExchangeCode(" ");
			bean.setExchangeId("16");
		} else {
			bean.setAskExchangeCode(getEquityPlusExchangeCode(quote._mktCtr));
			bean.setBidExchangeCode(getEquityPlusExchangeCode(quote._mktCtr));
			bean.setExchangeId(prot + "");
		}
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, EQBbo bbo) {
		String ticker = bbo.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "bbo.tkr() is null");
			return;
		}
		int prot = bbo.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "bbo protocol is " + prot);
			return;
		}
		if (bbo._bid == 0 && bbo._ask == 0 && bbo._bidSize == 0 && bbo._askSize == 0) {
			NTPLogger.dropSymbol(StreamName, "bbo quotes are Zero");
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (ticker.toUpperCase().endsWith(NASDAQ_BASIC_SUFFIX) && isNasdaqbasic)
			ticker = ticker.substring(2, ticker.length() - 2);
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "bbo Incorrect symbol " + ticker);
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "bbo No Image " + ticker);
			return;
		}
		if (isNasdaqbasic && isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "bbo Excluded protocol " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(bbo._bid);
		bean.setAskPrice(bbo._ask);
		bean.setBidSize(bbo._bidSize);
		bean.setAskSize(bbo._askSize);
		if (isNasdaqbasic) {
			bean.setAskExchangeCode(" ");
			bean.setBidExchangeCode(" ");
			bean.setExchangeId("16");
		} else {
			bean.setAskExchangeCode(getEquityPlusExchangeCode(bbo._askMktCtr));
			bean.setBidExchangeCode(getEquityPlusExchangeCode(bbo._bidMktCtr));
			bean.setExchangeId(prot + "");
		}
		bean.setLimitUpDown(NTPUtility.formatLimitUpDown(bbo.LimitUpDown()));
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an EQTrade update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, EQTrade trd) {
		String ticker = trd.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "trd.tkr() is null");
			return;
		}
		int prot = trd.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "trd protocol is " + prot);
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (ticker.startsWith("T:"))
			ticker = ticker.substring(2);

		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trd Incorrect symbol " + ticker);
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trd No Image " + ticker);
			return;
		}
		if (isNasdaqbasic && isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "trd Excluded protocol " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		if (trd.IsSummary()) {
			bean.setVolume(trd._acVol);
			bean.setDayHigh(trd._high);
			bean.setDayLow(trd._low);
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
			return;
		}
		if (trd.IsCxl()) {
			NTPLogger.dropSymbol(StreamName, "trd Cancel trade " + ticker);
			return;
		}
		if (!(trd.IsEligible())) {
			bean.setVolume(trd._acVol);
			if (trd.IsExtended() && isExtendedEval) {
				bean.setExtendedLastPrice(trd._trdPrc_ext);
				bean.setExtendedChangePrice(trd._netChg_ext);
				bean.setExtendedPercentChange(trd._pctChg_ext);
				bean.setExtendedLastTradeVolume(trd._trdVol_ext);
				bean.setExtendedTickUpDown(trd._prcTck_ext);
				DateTimeUtility.processExtendedDate(bean, trd.MsgTimeMs());
				if (isNasdaqbasic)
					bean.setExtendedMarketCenter(" ");
				else
					bean.setExtendedMarketCenter(getEquityPlusExchangeCode(trd._trdMktCtr_ext));
			}
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
			return;
		}
		bean.setLastPrice(trd._trdPrc);
		bean.setSystemTicker(ticker);
		bean.setTicker(ticker);
		bean.setLastTradeVolume(trd._trdVol);
		bean.setVolume(trd._acVol);
		bean.setDayHigh(trd._high);
		bean.setDayLow(trd._low);
		bean.setChangePrice(trd._netChg);
		bean.setOpenPrice(trd._openPrc);
		bean.setVWAP(trd.vwap(4) + "");
		bean.setTickUpDown(trd._prcTck);
		// using unabridged for halted status - generally use this field in TSQs
		bean.setHalted(false);
		if (isNasdaqbasic) {
			bean.setMarketCenter(" ");
			bean.setExchangeId("16");
		} else {
			bean.setMarketCenter(getEquityPlusExchangeCode(trd._mktCtr));
			bean.setExchangeId(prot + "");
		}
		bean.setPercentChange(trd._pctChg);
		DateTimeUtility.processDate(bean, trd.MsgTimeMs());
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	public void OnUpdate(String StreamName, EQBboMM bbomm) {
		String ticker = bbomm.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "bboMM.tkr() is null");
			return;
		}
		int prot = bbomm.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "bboMM protocol is " + prot);
			return;
		}
		if (bbomm._bid == 0 && bbomm._ask == 0 && bbomm._bidSize == 0 && bbomm._askSize == 0) {
			NTPLogger.dropSymbol(StreamName, "bboMM quotes are Zero");
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (ticker.toUpperCase().endsWith(NASDAQ_BASIC_SUFFIX) && isNasdaqbasic)
			ticker = ticker.substring(2, ticker.length() - 2);
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "bboMM Incorrect symbol " + ticker);
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "bboMM No Image " + ticker);
			return;
		}
		if (isNasdaqbasic && isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "bboMM Excluded protocol " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(bbomm._bid);
		bean.setAskPrice(bbomm._ask);
		bean.setBidSize(bbomm._bidSize);
		bean.setAskSize(bbomm._askSize);
		if (isNasdaqbasic) {
			bean.setAskExchangeCode(" ");
			bean.setBidExchangeCode(" ");
			bean.setExchangeId("16");
		} else {
			bean.setAskExchangeCode(getEquityPlusExchangeCode(bbomm._mktCtr));
			bean.setBidExchangeCode(getEquityPlusExchangeCode(bbomm._mktCtr));
			bean.setExchangeId(prot + "");
		}
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an EQTradeSts update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, EQTradeSts trdSts) {
		String ticker = trdSts.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "trdSts.tkr() is null");
			return;
		}
		int prot = trdSts.protocol();
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "trdSts protocol is " + prot);
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (ticker.startsWith("T:"))
			ticker = ticker.substring(2);
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trdSts Incorrect symbol " + ticker);
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trdSts No Image " + ticker);
			return;
		}
		if (isNasdaqbasic && isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "trdSts Excluded protocol " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setSHO(trdSts.IsRegSHO());
		if (!trdSts.IsRegSHO())
			bean.setHalted(trdSts._halted);
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an ticker is added in between the market
	 */
	public void OnBlobTable(String qry, BlobTable blobTable) {
		try {
			if (blobTable != null) {
				int rowCount = blobTable.nRow();
				NTPLogger.info("NEWISSUE OnBlobTable rowCount is = " + rowCount);
				for (int count = 0; count < rowCount; count++) {
					String ticker = blobTable.GetCell(count, TICKER_POS);
					// check if ticker is equity or not
					String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
					NTPLogger.newIssue(ticker, instrument);
					if (ticker == null || ticker.length() <= 0) {
						NTPLogger.info("NEWISSUE OnBlobTable missing symbol instrument = " + instrument);
						continue;
					}
					if (!(instrument.equals("CTA_A") || instrument.equals("UTP") || instrument.equals("CTA_B")
							|| instrument.equals("TSX"))) {
						NTPLogger.info("NEWISSUE OnBlobTable not a equity symbol instrument = " + instrument);
						continue;
					}
					if (NTPTickerValidator.isCanadianStock(ticker))
						subscribeNewIssue(ticker);
					// We can even skip these regionals. THey are in place in cases where
					// root got dropped due to some reason.
					else if (ticker.contains("/")) {
						NTPLogger.info("NEWISSUE OnBlobTable not a equity symbol instrument = " + instrument
								+ ", ticker = " + ticker);
						continue;
					} else
						subscribeNewIssue(ticker);
				}
			}
		} catch (Exception e) {
			NTPLogger.warning("NEWISSUE OnBlobTable exception is : " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void OnResubscribe(String tkr, int oldStreamID, int newStreamID) {
		NTPLogger.onResubscribeUC(tkr, oldStreamID, newStreamID);
		NTPUtility.updateTickerStreamIDMap(tkr, newStreamID, this);
	}

	private void subscribeNewIssue(String ticker) {
		Collection<String> tickerList = StockRetriever.getInstance().getTickerList();
		if (tickerList.contains(ticker) || imageTickerMap.containsKey(ticker)) {
			NTPLogger.info("NEWISSUE OnBlobTable ticker is already in ticker list - un-subscribing");
			NTPUtility.unsubscribeTicker(ticker);
			imageTickerMap.remove(ticker);
		}
		NTPLogger.info("NEWISSUE Going to subscribe : " + ticker);
		subscribe(ticker);
		tickerList.add(ticker);
	}

	private QTCPDMessageBean getCachedBean(String ticker) {
		Map<String, QTCPDMessageBean> qtMap = EquityQTMessageQueue.getInstance().getSubsData();
		QTCPDMessageBean cachedBean = qtMap.get(ticker);
		if (cachedBean == null) {
			NTPLogger.info("Creating new bean for " + ticker);
			cachedBean = new QTCPDMessageBean();
			qtMap.put(ticker, cachedBean);
		}
		return cachedBean;
	}

	private String getEquityPlusExchangeCode(String nasdaqExchangeCode) {
		if (nasdaqExchangeCode.startsWith("??") || nasdaqExchangeCode.length() < 1)
			nasdaqExchangeCode = " ";
		nasdaqExchangeCode = nasdaqExchangeCode.toUpperCase();
		if (exchgMap.containsKey(nasdaqExchangeCode))
			return exchgMap.get(nasdaqExchangeCode).toLowerCase();
		return nasdaqExchangeCode;
	}

	private boolean isProtocolExcluded(int protocol) {
		for (int i = 0; i < exchgFilter.length; i++) {
			if (exchgFilter[i].equals(protocol + ""))
				return true;
		}
		return false;
	}

	private boolean isToday(Calendar cal1) {
		Calendar cal2 = Calendar.getInstance();
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
}
