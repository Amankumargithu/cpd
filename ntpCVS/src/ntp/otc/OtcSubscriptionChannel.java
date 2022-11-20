package ntp.otc;

import java.util.Calendar;
import java.util.Collection;
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
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import ntp.util.OTCExchangeMapPopulator;
import ntp.util.StockRetriever;

public class OtcSubscriptionChannel extends UltraChan {

	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;
	private ConcurrentHashMap<String, String> imageTickerMap = EquityQTMessageQueue.getInstance().getImageTickerMap();
	private ConcurrentHashMap<String, String> duallyQuotedTickerMap = EquityQTMessageQueue.getInstance()
			.getDuallyQuotedMap();
	private static final String PINK_SHEET_MARKET_CENTER = "u";
	private boolean isExtendedEval = true;
	private OtcSubscriptionChannel channel;
	private OTCExchangeMapPopulator populator = OTCExchangeMapPopulator.getInstance();

	public OtcSubscriptionChannel(String name, OtcSubscriptionManager equitySubscriptionManager) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		channel = this;
		this.name = name;
		connectChannel();
		String evaluateExtended = CPDProperty.getInstance().getProperty("IS_EXTENDED_EVAL");
		isExtendedEval = evaluateExtended == null ? true : evaluateExtended.equalsIgnoreCase("TRUE") ? true : false;
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
			Thread resubscribeThread = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(5 * 60 * 1000);
						channel.ResubscribeZombieStreams();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, name + "_resub");
			resubscribeThread.start();
			NTPLogger.info("Started thread - " + name + "_resub");
		}
	}

	public void subscribe(String ticker) {
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
	}

	public void subscribeTickers(final Collection<String> tickerList) {
		new Thread(() -> {
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
			NTPLogger.info("Completed Subsscriptions for " + name);
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
		if (!(prot == 9 || prot == 34 || prot == 14 || prot == 0)) {
			NTPLogger.dropSymbol(StreamName, "img protocol is " + prot);
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			ticker = ticker.substring(0, ticker.indexOf("/"));
			NTPLogger.info("Converting " + img.tkr() + " to " + ticker);
		}
		int dually_quoted = (int) img._BBquoted;
		if (dually_quoted != 0)
			duallyQuotedTickerMap.put(ticker, ticker);
		String originalTicker = ticker;
		ticker = ticker + ".PK";
		QTCPDMessageBean bean = getCachedBean(ticker);
		NTPLogger.image(name, img);
		bean.setSystemTicker(ticker);
		bean.setTicker(ticker);
		if (prot == 34) {
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
				bean.setExtendedTickUpDown(img._prcTck_ext);
				DateTimeUtility.processExtendedDate(bean, img._trdTime_ext);
				bean.setExtendedMarketCenter(PINK_SHEET_MARKET_CENTER);
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
			DateTimeUtility.processDate(bean, img._trdTime);
			bean.setMarketCenter(PINK_SHEET_MARKET_CENTER);
			bean.setLastClosedPrice(img._close);
			bean.setTickUpDown(img._prcTck);
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
			bean.setExchangeId("34");
			bean.setLimitUpDown(NTPUtility.formatLimitUpDown(img.LimitUpDown()));
			bean.setExchangeCode(NTPUtility.getMappedExchangeCode(img._priMktCtr));
		}
		if ((prot == 14 || prot == 34) && !(img._bid == 0 && img._ask == 0 && img._bidSize == 0 && img._askSize == 0)) {
			bean.setAskPrice(img._ask);
			bean.setAskSize(img._askSize);
			bean.setBidPrice(img._bid);
			bean.setBidSize(img._bidSize);
			bean.setAskExchangeCode(PINK_SHEET_MARKET_CENTER);
			bean.setBidExchangeCode(PINK_SHEET_MARKET_CENTER);
		}
		NTPLogger.info("Adding Image in queue for " + bean.getTicker());
		imageTickerMap.put(bean.getTicker(), bean.getTicker());
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);

		if (duallyQuotedTickerMap.containsKey(originalTicker)) {
			NTPLogger.info("DUALLY_QUOTED_SYMBOL " + originalTicker);
			QTCPDMessageBean duallyQuotesBean = getCachedBean(originalTicker);
			duallyQuotesBean.setSystemTicker(originalTicker);
			duallyQuotesBean.setTicker(originalTicker);
			if (prot == 9 || prot == 34) {
				if (isExtendedEval) {
					duallyQuotesBean.setExtendedLastPrice(img._trdPrc_ext);
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(img._trdTime_ext);
					if (isToday(c)) {
						bean.setExtendedChangePrice(img._netChg_ext);
						bean.setExtendedPercentChange(img._pctChg_ext);
					} else {
						bean.setExtendedChangePrice(0);
						bean.setExtendedPercentChange(0);
					}
					duallyQuotesBean.setExtendedLastTradeVolume(img._trdVol_ext);
					duallyQuotesBean.setExtendedTickUpDown(img._prcTck_ext);
					DateTimeUtility.processExtendedDate(duallyQuotesBean, img._trdTime_ext);
					duallyQuotesBean.setExtendedMarketCenter(PINK_SHEET_MARKET_CENTER);
				}
				duallyQuotesBean.setLastPrice(img._trdPrc);
				duallyQuotesBean.setLastTradeVolume(img._trdVol);
				duallyQuotesBean.setDayHigh(img._high);
				duallyQuotesBean.setDayLow(img._low);
				duallyQuotesBean.setOpenPrice(img._open);
				duallyQuotesBean.setVWAP(img.vwap(4) + "");
				duallyQuotesBean.setVolume(img._acVol);
				duallyQuotesBean.setChangePrice(img._netChg);
				duallyQuotesBean.setPercentChange(img._pctChg);
				duallyQuotesBean.setTickUpDown(img._prcTck);
				DateTimeUtility.processDate(duallyQuotesBean, img._trdTime);
				duallyQuotesBean.setMarketCenter(PINK_SHEET_MARKET_CENTER);
				duallyQuotesBean.setLastClosedPrice(img._close);
				if (img._regSHO == MsgTypes._eqSubTRDCXL || img._regSHO == MsgTypes._eqSubTRDCORR || img._regSHO == 0x01
						|| img._regSHO == 0x02) {
					NTPLogger.info("Image Ticker: " + originalTicker + " SHO: " + true);
					duallyQuotesBean.setSHO(true);
				} else {
					duallyQuotesBean.setSHO(false);
					NTPLogger.info("Image Ticker: " + originalTicker + " SHO: " + Integer.toHexString(img._regSHO));
				}
				if (img._halted == 0x01) {
					NTPLogger.info("Image Ticker: " + originalTicker + " HLT: " + true);
					duallyQuotesBean.setHalted(true);
				} else {
					duallyQuotesBean.setHalted(false);
					NTPLogger.info("Image Ticker: " + originalTicker + " HLT: " + false + " "
							+ Integer.toHexString(img._halted));
				}
				duallyQuotesBean.setExchangeId("34");
				duallyQuotesBean.setLimitUpDown(NTPUtility.formatLimitUpDown(img.LimitUpDown()));
				duallyQuotesBean.setExchangeCode(NTPUtility.getMappedExchangeCode(img._priMktCtr));
			}
			if ((prot == 34 || prot == 9)
					&& !(img._bid == 0 && img._ask == 0 && img._bidSize == 0 && img._askSize == 0)) {
				duallyQuotesBean.setAskPrice(img._ask);
				duallyQuotesBean.setAskSize(img._askSize);
				duallyQuotesBean.setBidPrice(img._bid);
				duallyQuotesBean.setBidSize(img._bidSize);
				duallyQuotesBean.setAskExchangeCode(PINK_SHEET_MARKET_CENTER);
				duallyQuotesBean.setBidExchangeCode(PINK_SHEET_MARKET_CENTER);
			}
			NTPLogger.info("Adding Image in queue for " + duallyQuotesBean.getTicker());
			imageTickerMap.put(duallyQuotesBean.getTicker(), duallyQuotesBean.getTicker());
			EquityQTMessageQueue.getInstance().getmQueue().add(duallyQuotesBean.getTicker(), duallyQuotesBean);
		}
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
		if (!(prot == 9 || prot == 34)) {
			NTPLogger.dropSymbol(StreamName, "trd protocol is " + prot + " for " + ticker);
			return;
		}
		if (ticker.endsWith("/BB") || ticker.endsWith("/OB")) {
			NTPLogger.dropSymbol(StreamName, "trd symbol tier " + ticker);
			return;
		}
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			ticker = ticker.substring(0, ticker.indexOf("/"));
		}
		boolean isDuallyQuoted = duallyQuotedTickerMap.contains(ticker);
		QTCPDMessageBean duallyQuotedBean = null;
		if (isDuallyQuoted)
			duallyQuotedBean = getCachedBean(ticker);
		ticker = ticker + ".PK";
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trd No Image " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		if (trd.IsSummary()) {
			bean.setVolume(trd._acVol);
			bean.setDayHigh(trd._high);
			bean.setDayLow(trd._low);
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
			if (isDuallyQuoted) {
				duallyQuotedBean.setVolume(trd._acVol);
				duallyQuotedBean.setDayHigh(trd._high);
				duallyQuotedBean.setDayLow(trd._low);
				EquityQTMessageQueue.getInstance().getmQueue().add(duallyQuotedBean.getTicker(), duallyQuotedBean);
			}
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
				bean.setExtendedMarketCenter(PINK_SHEET_MARKET_CENTER);
			}
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
			if (isDuallyQuoted) {
				duallyQuotedBean.setVolume(trd._acVol);
				if (trd.IsExtended() && isExtendedEval) {
					duallyQuotedBean.setExtendedLastPrice(trd._trdPrc_ext);
					duallyQuotedBean.setExtendedChangePrice(trd._netChg_ext);
					duallyQuotedBean.setExtendedPercentChange(trd._pctChg_ext);
					duallyQuotedBean.setExtendedLastTradeVolume(trd._trdVol_ext);
					duallyQuotedBean.setExtendedTickUpDown(trd._prcTck_ext);
					DateTimeUtility.processExtendedDate(duallyQuotedBean, trd.MsgTimeMs());
					duallyQuotedBean.setExtendedMarketCenter(PINK_SHEET_MARKET_CENTER);
				}
				EquityQTMessageQueue.getInstance().getmQueue().add(duallyQuotedBean.getTicker(), duallyQuotedBean);
			}
			return;
		}
		if (trd.IsCxl()) {
			NTPLogger.dropSymbol(StreamName, "trd Cancel trade " + ticker);
			return;
		}
		bean.setLastPrice(trd._trdPrc);
		bean.setLastTradeVolume(trd._trdVol);
		bean.setVolume(trd._acVol);
		bean.setDayHigh(trd._high);
		bean.setDayLow(trd._low);
		bean.setChangePrice(trd._netChg);
		bean.setOpenPrice(trd._openPrc);
		bean.setTickUpDown(trd._prcTck);
		bean.setVWAP(trd.vwap(4) + "");
		// using unabridged for halted status - generally use this field in TSQs
		bean.setHalted(false);
		bean.setMarketCenter(PINK_SHEET_MARKET_CENTER);
		bean.setPercentChange(trd._pctChg);
		DateTimeUtility.processDate(bean, trd.MsgTimeMs());
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
		if (isDuallyQuoted) {
			duallyQuotedBean.setLastPrice(trd._trdPrc);
			duallyQuotedBean.setLastTradeVolume(trd._trdVol);
			duallyQuotedBean.setVolume(trd._acVol);
			duallyQuotedBean.setDayHigh(trd._high);
			duallyQuotedBean.setDayLow(trd._low);
			duallyQuotedBean.setChangePrice(trd._netChg);
			duallyQuotedBean.setOpenPrice(trd._openPrc);
			duallyQuotedBean.setTickUpDown(trd._prcTck);
			duallyQuotedBean.setVWAP(trd.vwap(4) + "");
			// using unabridged for halted status - generally use this field in TSQs
			duallyQuotedBean.setHalted(false);
			duallyQuotedBean.setMarketCenter(PINK_SHEET_MARKET_CENTER);
			duallyQuotedBean.setPercentChange(trd._pctChg);
			DateTimeUtility.processDate(duallyQuotedBean, trd.MsgTimeMs());
			EquityQTMessageQueue.getInstance().getmQueue().add(duallyQuotedBean.getTicker(), duallyQuotedBean);
		}
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
		if (!(prot == 9 || prot == 34)) {
			NTPLogger.dropSymbol(StreamName, "trdSts protocol is " + prot);
			return;
		}
		if (ticker.endsWith("/BB") || ticker.endsWith("/OB"))
			return;
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			ticker = ticker.substring(0, ticker.indexOf("/"));
		}
		boolean isDuallyQuoted = duallyQuotedTickerMap.contains(ticker);
		QTCPDMessageBean duallyQuotedBean = null;
		if (isDuallyQuoted)
			duallyQuotedBean = getCachedBean(ticker);
		ticker = ticker + ".PK";
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trdSts No Image " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setSHO(trdSts.IsRegSHO());
		bean.setHalted(trdSts._halted);
		NTPLogger.info("updateTradeSts ticker = " + ticker + "  SHO = " + trdSts.IsRegSHO() + "  HLT= " + trdSts._halted
				+ " SHO " + trdSts.mtSub());
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
		if (isDuallyQuoted) {
			duallyQuotedBean.setSHO(trdSts.IsRegSHO());
			duallyQuotedBean.setHalted(trdSts._halted);
			NTPLogger.info("updateTradeSts ticker = " + duallyQuotedBean.getTicker() + "  SHO = " + trdSts.IsRegSHO()
					+ "  HLT= " + trdSts._halted + " SHO " + trdSts.mtSub());
			EquityQTMessageQueue.getInstance().getmQueue().add(duallyQuotedBean.getTicker(), duallyQuotedBean);
		}
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, EQQuote q) {
		String ticker = q.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "EQQuote.tkr() is null");
			return;
		}
		int prot = q.protocol();
		if (!(prot == 9 || prot == 14)) {
			NTPLogger.dropSymbol(StreamName, "EQQuote protocol is " + prot);
			return;
		}
		if (q._bid == 0 && q._ask == 0 && q._bidSize == 0 && q._askSize == 0) {
			NTPLogger.dropSymbol(StreamName, "EQQuote quotes are Zero");
			return;
		}
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker))
			ticker = ticker.substring(0, ticker.indexOf("/"));
		if (prot == 14)
			ticker = ticker + ".PK";
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "EQQuote No Image " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(q._bid);
		bean.setAskPrice(q._ask);
		bean.setBidSize(q._bidSize);
		bean.setAskSize(q._askSize);
		bean.setAskExchangeCode(PINK_SHEET_MARKET_CENTER);
		bean.setBidExchangeCode(PINK_SHEET_MARKET_CENTER);
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, EQBbo q) {
		String ticker = q.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "EqBbo.tkr() is null");
			return;
		}
		int prot = q.protocol();
		if (!(prot == 9 || prot == 14)) {
			NTPLogger.dropSymbol(StreamName, "EqBbo protocol is " + prot);
			return;
		}
		if (q._bid == 0 && q._ask == 0 && q._bidSize == 0 && q._askSize == 0) {
			NTPLogger.dropSymbol(StreamName, "EQBbo quotes are Zero");
			return;
		}
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			ticker = ticker.substring(0, ticker.indexOf("/"));
		}
		if (prot == 14)
			ticker = ticker + ".PK";
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "EqBbo No Image " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(q._bid);
		bean.setAskPrice(q._ask);
		bean.setBidSize(q._bidSize);
		bean.setAskSize(q._askSize);
		bean.setAskExchangeCode(PINK_SHEET_MARKET_CENTER);
		bean.setBidExchangeCode(PINK_SHEET_MARKET_CENTER);
		bean.setLimitUpDown(NTPUtility.formatLimitUpDown(q.LimitUpDown()));
		EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an EQQuoteMM update is received for an pink sheet ticker.
	 */
	public void OnUpdate(String StreamName, EQBboMM q) {
		String ticker = q.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "EQBboMM.tkr() is null");
			return;
		}
		int prot = q.protocol();
		if (!(prot == 9 || prot == 14)) {
			NTPLogger.dropSymbol(StreamName, "EQBboMM protocol is " + prot);
			return;
		}
		if (q._bid == 0 && q._ask == 0 && q._bidSize == 0 && q._askSize == 0) {
			NTPLogger.dropSymbol(StreamName, "EQBboMM quotes are Zero");
			return;
		}
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			ticker = ticker.substring(0, ticker.indexOf("/"));
		}
		if (prot == 14)
			ticker = ticker + ".PK";
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "EQBboMM No Image " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(q._bid);
		bean.setAskPrice(q._ask);
		bean.setBidSize(q._bidSize);
		bean.setAskSize(q._askSize);
		bean.setAskExchangeCode(PINK_SHEET_MARKET_CENTER);
		bean.setBidExchangeCode(PINK_SHEET_MARKET_CENTER);
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
					if (!instrument.equals("OTC") && !instrument.equals("OTCBB")) {
						NTPLogger.info("NEWISSUE OnBlobTable not a otc symbol instrument = " + instrument);
						continue;
					}
					if (ticker.contains("/")) {
						String[] arr = ticker.split("/");
						if (arr.length == 2) // else it is level2 symbol
						{
							subscribeNewIssue(arr[0]);
							if (populator.isOtcExchangeId(arr[1]))
								subscribeNewIssue(ticker);
						} else {
							NTPLogger.info("NEWISSUE OnBlobTable not a otc symbol instrument = " + instrument
									+ ", ticker = " + ticker);
							continue;
						}
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
		if (tickerList.contains(ticker)) {
			NTPLogger.info("NEWISSUE OnBlobTable ticker is alreay in ticker list - un-subscribing");
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

	private boolean isToday(Calendar cal1) {
		Calendar cal2 = Calendar.getInstance();
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
}
