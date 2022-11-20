package ntp.futures.streamer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.b4utrade.bean.StockOptionBean;

import QuoddFeed.msg.FUTRMisc;
import QuoddFeed.msg.FUTRQuote;
import QuoddFeed.msg.FUTRSumm;
import QuoddFeed.msg.FUTRTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.bean.QTCPDMessageBean;
import ntp.futures.cache.FuturesMappingSymbolsCache;
import ntp.futures.cache.FuturesQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.queue.MappedMessageQueue;
import ntp.util.DateTimeUtility;
import ntp.util.FuturesUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;

/*
 * 30 Oct 2018 - Ankit - trade time have future date time in it, which is causing issues on UI. SO We are skipping UC trade time and adding server trade time in it.
 */
public class FuturesStreamingChannel extends UltraChan {

	private boolean isRunning = false;
	private String name;
	private int idx = 0;

	private MappedMessageQueue queue = FuturesQTMessageQueue.getInstance().getmQueue();
	private ConcurrentHashMap<String, String> imageTickerMap = FuturesQTMessageQueue.getInstance().getImageTickerMap();
	private ConcurrentHashMap<String, String> incorrectTickerMap = FuturesQTMessageQueue.getInstance()
			.getIncorrectTickerMap();
	private FuturesUtility utility = FuturesUtility.getInstance();
//	private SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd" );
//	private SimpleDateFormat timeFormatter = new SimpleDateFormat( "HHmmss" );
//	private SimpleDateFormat testFormatter = new SimpleDateFormat( "yyyyMMddHHmm" );

	public FuturesStreamingChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
	}

	public String getChannelName() {
		return this.name;
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
			if (ticker != null) {
				NTPLogger.dead(ticker, name);
				incorrectTickerMap.put(ticker, ticker);
			}
		} else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	public void OnImage(String StreamName, Image image) {
		String ticker = image.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		NTPLogger.image(name, image);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setSystemTicker(ticker);
		if (image._trdPrc > 0.0)
			qtMessageBean.setLastPrice(image._trdPrc);
		else {
			image._trdPrc = image._close;
			qtMessageBean.setLastPrice(image._close);
		}
		qtMessageBean.setLastClosedPrice(image._close);
		qtMessageBean.setLastTradeVolume(image._trdVol);
		qtMessageBean.setVolume(image._acVol);
		qtMessageBean.setDayHigh(image._high);
		qtMessageBean.setDayLow(image._low);
		qtMessageBean.setAskPrice(image._ask);
		qtMessageBean.setAskSize(image._askSize);
		qtMessageBean.setBidPrice(image._bid);
		qtMessageBean.setBidSize(image._bidSize);
		qtMessageBean.setChangePrice(image._netChg);
		qtMessageBean.setPercentChange(image._pctChg);
		qtMessageBean.setOpenPrice(image._open);
		qtMessageBean.setOpenPriceRange1(image._open);
		qtMessageBean.setBidExchangeCode(image._iBidMktCtr + "");
		qtMessageBean.setAskExchangeCode(image._iAskMktCtr + "");
		qtMessageBean.setSettlementPrice(image._settlePrc);
		qtMessageBean.setExchangeId("" + image.protocol());
		qtMessageBean.setExchangeCode(utility.getExchange(image.protocol(), ticker));
		// Test
		/*
		 * long serverTime = 0; try { serverTime =
		 * Long.parseLong(testFormatter.format(new Date())); } catch(Exception e) {
		 * serverTime = 0; }
		 */
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, System.currentTimeMillis());
		/*
		 * long currentTime = 0; try { currentTime=
		 * Long.parseLong(testFormatter.format(image._trdTime)); } catch(Exception e) {
		 * currentTime = 0; } if(currentTime > serverTime)
		 * NTPLogger.warning("LTT IMAGE " + ticker + " " + serverTime + " " +
		 * currentTime);
		 */
		qtMessageBean.setTickUpDown(image._prcTck);
		String uiSymbol = addMappedUISymbolBeanToQueue(qtMessageBean);
		if (uiSymbol != null) {
			imageTickerMap.put(uiSymbol, uiSymbol);
			NTPLogger.info("ADDED to queue  " + uiSymbol);
		}
		queue.add(ticker, qtMessageBean);
		imageTickerMap.put(ticker, ticker);
		NTPLogger.info("ADDED to queue  " + qtMessageBean.getTicker());

		// Updating Fundamentals

		StockOptionBean sob = new StockOptionBean();
		sob.setTickerInDB(ticker);
		sob.setTicker(ticker);
		sob.setExchange(utility.getExchange(image.protocol(), ticker));
		sob.setExpirationDate(FuturesUtility.getInstance().getExpirationDate(image._settleDate));
		sob.setContractSize(99999999);
		sob.setLastClosedPrice(image._close);
		sob.setSecurityDesc(image.Description());
		sob.setHighIn52Week(0.0);
		sob.setLowIn52Week(0.0);
		sob.setOpenInterest(image._openVol);
		sob.setOpenPrice(image._open);
		sob.setOpenPriceRange2(image._open2);
		FuturesMappingSymbolsCache.getInstance().addFundamentalBean(ticker, sob);

		if (uiSymbol != null) {
			sob = new StockOptionBean();
			sob.setTickerInDB(uiSymbol);
			sob.setTicker(uiSymbol);
			sob.setExchange(utility.getExchange(image.protocol(), uiSymbol));
			sob.setExpirationDate(FuturesUtility.getInstance().getExpirationDate(image._settleDate));
			sob.setContractSize(99999999);
			sob.setLastClosedPrice(image._close);
			sob.setSecurityDesc(image.Description());
			sob.setHighIn52Week(0.0);
			sob.setLowIn52Week(0.0);
			sob.setOpenInterest(image._openVol);
			sob.setOpenPrice(image._open);
			sob.setOpenPriceRange2(image._open2);
			FuturesMappingSymbolsCache.getInstance().addFundamentalBean(uiSymbol, sob);
		}
	}

	/**
	 * Called when FUTRTrade message is received
	 */
	public void OnUpdate(String StreamName, FUTRTrade trade) {
		String ticker = trade.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "FUTRTrade.tkr() is null");
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "FUTRTrade No Image " + ticker);
			return;
		}
		if (trade.IsCancel() || trade.IsCorrect()) {
			NTPLogger.dropSymbol(StreamName, "FUTRTrade Cancel/Correction " + ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);

		/*
		 * long serverTime = 0; try { serverTime =
		 * Long.parseLong(testFormatter.format(new Date())); } catch(Exception e) {
		 * serverTime = 0; } long currentTime = 0; try { currentTime=
		 * Long.parseLong(testFormatter.format( trade.MsgTimeMs() )); } catch(Exception
		 * e) { currentTime = 0; } if(currentTime > serverTime) {
		 * NTPLogger.warning("LTT FUTRTrade " + ticker + " " + serverTime + " " +
		 * currentTime + "  " + trade.RTL()); return; }
		 */
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, System.currentTimeMillis());
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setLastTradeVolume(trade._trdVol);
		qtMessageBean.setVolume(trade._acVol);
		if (trade._trdPrc > 0)
			qtMessageBean.setLastPrice(trade._trdPrc);
		else {
			trade._trdPrc = trade.PrevClose();
			qtMessageBean.setLastPrice(trade.PrevClose());
		}
		qtMessageBean.setLastPrice(trade._trdPrc);

		// DateTimeUtility.getDefaultInstance().processDate(qtMessageBean,
		// trade.MsgTimeMs());
		qtMessageBean.setLastClosedPrice(trade.PrevClose());
		qtMessageBean.setDayHigh(trade._high);
		qtMessageBean.setDayLow(trade._low);
		qtMessageBean.setOpenPrice(trade._openPrc);
		qtMessageBean.setOpenPriceRange1(trade._openPrc);
		qtMessageBean.setChangePrice(trade._netChg);
		qtMessageBean.setPercentChange(trade._pctChg);
		addMappedUISymbolBeanToQueue(qtMessageBean);
		queue.add(ticker, qtMessageBean);
	}

	/**
	 * Called when FUTRQuote message is received
	 */
	public void OnUpdate(String StreamName, FUTRQuote quote) {
		String ticker = quote.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "FUTRQuote.tkr() is null");
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "FUTRQuote No Image " + ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		qtMessageBean.setBidPrice(quote._bid);
		qtMessageBean.setAskPrice(quote._ask);
		qtMessageBean.setBidSize(quote._bidSize);
		qtMessageBean.setAskSize(quote._askSize);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setSystemTicker(ticker);
		addMappedUISymbolBeanToQueue(qtMessageBean);
		queue.add(ticker, qtMessageBean);
	}

	/**
	 * Callback invoked when an Futures Misc - Hi/Lo/Last or OpenInt - is received.
	 *
	 * @param StreamName Name of this Data Stream
	 * @param qm         FUTRMisc
	 */
	public void OnUpdate(String StreamName, FUTRMisc trade) {
		String ticker = trade.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "FUTRMisc.tkr() is null");
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "FUTRMisc No Image " + ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		if (trade.IsHiLo()) {
			qtMessageBean.setDayHigh(trade._highPrc);
			qtMessageBean.setDayLow(trade._lowPrc);
			qtMessageBean.setLastPrice(trade._lastPrc);
			double chg = qtMessageBean.getLastPrice() - qtMessageBean.getLastClosedPrice();
			qtMessageBean.setChangePrice(chg);
			double perChange = (chg / qtMessageBean.getLastClosedPrice()) * 100;
			qtMessageBean.setPercentChange(perChange);
			/*
			 * long serverTime = 0; try { serverTime =
			 * Long.parseLong(testFormatter.format(new Date())); } catch(Exception e) {
			 * serverTime = 0; }
			 */
			DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, System.currentTimeMillis());
			/*
			 * long currentTime = 0; try { currentTime=
			 * Long.parseLong(testFormatter.format(trade.MsgTimeMs())); } catch(Exception e)
			 * { currentTime = 0; } if(currentTime > serverTime)
			 * NTPLogger.warning("LTT FUTRMisc " + ticker + " " + serverTime + " " +
			 * currentTime);
			 */

			// DateTimeUtility.getDefaultInstance().processDate(qtMessageBean,
			// trade.MsgTime());
			addMappedUISymbolBeanToQueue(qtMessageBean);
			queue.add(ticker, qtMessageBean);
		} else {
			NTPLogger.info("Got Open Interest Update for " + ticker + " values " + trade._openInt);
			StockOptionBean bean = FuturesMappingSymbolsCache.getInstance().getFundamentalData(ticker);
			if (bean != null)
				bean.setOpenInterest(trade._openInt);
			String uiSymbol = FuturesMappingSymbolsCache.getInstance().getUIMappedSymbol(qtMessageBean.getTicker());
			if (uiSymbol != null) {
				bean = FuturesMappingSymbolsCache.getInstance().getFundamentalData(ticker);
				if (bean != null)
					bean.setOpenInterest(trade._openInt);
			}
		}
	}

	/**
	 * Callback invoked when an Futures Summary is received.
	 *
	 * @param StreamName Name of this Data Stream
	 * @param qm         FUTRSumm
	 */
	public void OnUpdate(String StreamName, FUTRSumm trade) {
		String ticker = trade.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "FUTRSumm.tkr() is null");
			return;
		}
		if (!imageTickerMap.containsKey(ticker)) {
			NTPLogger.dropSymbol(StreamName, "FUTRSumm No Image " + ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		qtMessageBean.setOpenPriceRange1(trade._openPrc1);
		qtMessageBean.setOpenPriceRange2(trade._openPrc2);
		qtMessageBean.setLastClosedPriceRange1(trade._closePrc1);
		qtMessageBean.setLastClosedPriceRange2(trade._closePrc2);
		/*
		 * long serverTime = 0; try { serverTime =
		 * Long.parseLong(testFormatter.format(new Date())); } catch(Exception e) {
		 * serverTime = 0; }
		 */
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, System.currentTimeMillis());
		/*
		 * long currentTime = 0; try { currentTime=
		 * Long.parseLong(testFormatter.format(trade.MsgTimeMs())); } catch(Exception e)
		 * { currentTime = 0; } if(currentTime > serverTime)
		 * NTPLogger.warning("LTT FUTRSumm " + ticker + " " + serverTime + " " +
		 * currentTime);
		 */

		// DateTimeUtility.getDefaultInstance().processDate(qtMessageBean,
		// trade.MsgTime());
		qtMessageBean.setLastPrice(trade._settlePrc);
		qtMessageBean.setSettlementPrice(trade._settlePrc);
		qtMessageBean.setVolume(trade._acVol);
		qtMessageBean.setDayHigh(trade._highPrc);
		qtMessageBean.setDayLow(trade._lowPrc);
		addMappedUISymbolBeanToQueue(qtMessageBean);
		queue.add(ticker, qtMessageBean);
	}

	private String addMappedUISymbolBeanToQueue(QTCPDMessageBean qtMessageBean) {
		String uiSymbol = FuturesMappingSymbolsCache.getInstance().getUIMappedSymbol(qtMessageBean.getTicker());
		if (uiSymbol != null) {
			QTCPDMessageBean bean = null;
			try {
				bean = (QTCPDMessageBean) qtMessageBean.clone();
				bean.setTicker(uiSymbol);
				bean.setSystemTicker(uiSymbol);
				FuturesQTMessageQueue.getInstance().getSubsData().put(uiSymbol, bean);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			queue.add(uiSymbol, bean);
		}
		return uiSymbol;
	}

	private QTCPDMessageBean getCachedBean(String ticker) {
		Map<String, QTCPDMessageBean> qtMap = FuturesQTMessageQueue.getInstance().getSubsData();
		QTCPDMessageBean cachedBean = qtMap.get(ticker);
		if (cachedBean == null) {
			cachedBean = new QTCPDMessageBean();
			qtMap.put(ticker, cachedBean);
		}
		return cachedBean;
	}
}
