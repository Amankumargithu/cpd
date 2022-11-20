package ntp.indicesmf;

import java.util.Collection;
import java.util.Map;

import QuoddFeed.msg.BONDQuote;
import QuoddFeed.msg.BONDTrade;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.FUNDnav;
import QuoddFeed.msg.IDXSummary;
import QuoddFeed.msg.IDXValue;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.IndicesMFTickersPopulator;
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;

public class IndicesMFSubsChannel extends UltraChan {

	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;
	private IndicesMFTickersPopulator populator = IndicesMFTickersPopulator.getInstance();
	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;

	public IndicesMFSubsChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public String getName() {
		return name;
	}

	public void subscribe(String ticker) {
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
	}

	public void subscribeTickers(final Collection<String> tickerList) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 0;
				int threshold = 200;
				int sleepTime = 500;
				try {
					threshold = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD"));
					sleepTime = Integer.parseInt(CPDProperty.getInstance().getProperty("SLEEP_TIME"));
				} catch (Exception e) {

					NTPLogger.missingProperty("THRESHOLD");
					threshold = 200;
					NTPLogger.defaultSetting("THRESHOLD", "" + threshold);
					NTPLogger.missingProperty("SLEEP_TIME");
					sleepTime = 500;
					NTPLogger.defaultSetting("SLEEP_TIME", "" + sleepTime);
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
	public void OnImage(String StreamName, Image image) {
		String ticker = image.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		NTPLogger.image(name, image);
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setTicker(ticker);
		bean.setSystemTicker(ticker);
		if (ticker.startsWith("I:")) {
			bean.setOpenPrice(image._open);
			bean.setDayHigh(image._high);
			bean.setDayLow(image._low);
			bean.setLastClosedPrice(image._close);
			bean.setChangePrice(image._netChg);
			bean.setPercentChange(image._pctChg);
			bean.setAskPrice(image._ask);
			bean.setBidPrice(image._bid);
			String suffix = null;
			double lastPrice = 0.0;
			try {
				suffix = ticker.substring(ticker.length() - 3);
			} catch (Exception e) {
				suffix = "";
			}
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
			if (lastPrice != 0.0)
				bean.setLastPrice(lastPrice);
			bean.setExchangeId(image.protocol() + "");
			bean.setExchangeCode(getExchangeCode(image.protocol()));
			DateTimeUtility.processDate(bean, image.JavaTime(image._tUpd));
		} else if (ticker.startsWith("B:")) {
			bean.setBidPrice(image._bid);
			bean.setAskPrice(image._ask);
			bean.setBidSize(image._bidSize);
			bean.setAskSize(image._askSize);
			bean.setAskExchangeCode(getExchangeCode(image._askMktCtr));
			bean.setBidExchangeCode(getExchangeCode(image._bidMktCtr));
			bean.setMarketCenter(getExchangeCode(image._priMktCtr));
			bean.setLastPrice(image._trdPrc);
			bean.setLastTradeVolume(image._trdVol);
			bean.setVolume(image._acVol);
			bean.setDayHigh(image._high);
			bean.setDayLow(image._low);
			bean.setVWAP(image._vwap + "");
			bean.setChangePrice(image._netChg);
			bean.setPercentChange(image._pctChg);
			bean.setExchangeCode("ARCA Bonds");
			bean.setExchangeId(image.protocol() + "");
			DateTimeUtility.processDate(bean, image._trdTime);
			IndicesMFQTMessageQueue.getInstance().getBondsWriterQueue().add(bean);
		} else {
			char fundType = image._fundType;
			char fundCode = image._fundCode;
			if ((fundType == '2' || fundType == '4') && (fundCode == 'A' || fundCode == 'G' || fundCode == 'X')) {
				bean.setLastPrice(1);
				bean.setAskPrice(image._yield7DayAnnualized);
			} else {
				bean.setLastPrice(image._fundNav);
				bean.setAskPrice(image._fundPrc);
			}
			bean.setLastClosedPrice(image._fundClose);
			bean.setChangePrice(image._fundNetChg);
			bean.setPercentChange(image._fundPctChg);
			int protocol = image.protocol();
			bean.setExchangeId(protocol + "");
			String exchangeCode = getExchangeCode(70);
			DateTimeUtility.processDate(bean, image._trdTime);
			bean.setExchangeCode(exchangeCode);
		}
		IndicesMFQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker.
	 */
	public void OnUpdate(String StreamName, FUNDnav nav) {
		String tkr = nav.tkr();
		try {
			if (tkr == null) {
				NTPLogger.dropSymbol(StreamName, "FUNDnav.tkr() is null");
				return;
			}
			QTCPDMessageBean bean = new QTCPDMessageBean();
			if (!((nav._flags & 0x4) == 0x4)) {
				NTPLogger.dropSymbol(tkr, "FUND last:" + nav._nav + " _distroType:" + nav._distroType + " _flags: "
						+ nav._flags + " totalCashDist : " + nav._totalCashDist + " exDate: " + nav._exDate);
				return;
			}
			char fundType = nav._fundType;
			char fundCode = nav._fundCode;
			if ((fundType == '2' || fundType == '4') && (fundCode == 'A' || fundCode == 'G' || fundCode == 'X')) {
				bean.setLastPrice(1);
				bean.setAskPrice(nav._yield7DayAnnualized);
			} else {
				bean.setLastPrice(nav._nav);
				bean.setAskPrice(nav._price);
			}
			bean.setSystemTicker(tkr);
			bean.setTicker(tkr);
			bean.setLastClosedPrice(nav._close);
			bean.setChangePrice(nav._netChg);
			bean.setPercentChange(nav._pctChg);
			bean.setExchangeId(nav.protocol() + "");
			String exchangeCode = getExchangeCode(70);
			bean.setExchangeCode(exchangeCode);
			DateTimeUtility.processDate(bean, nav.MsgTimeMs());
			NTPLogger.info("Mf updates: " + tkr + " last:" + nav._nav + " ask: " + nav._price + " close: " + nav._close
					+ " net chg :" + nav._netChg + " pct chg: " + nav._pctChg + " prot:" + nav.protocol()
					+ " _distroType:" + nav._distroType + " _flags: " + nav._flags + " " + nav._fundType + " "
					+ nav._fundCode);
			IndicesMFQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when an IDXValue update is received for an index ticker.
	 */
	public void OnUpdate(String StreamName, IDXValue val) {
		String tkr = val.tkr();
		if (tkr == null) {
			NTPLogger.dropSymbol(StreamName, "IDXValue.tkr() is null");
			return;
		}
		QTCPDMessageBean bean = getCachedBean(tkr);
		bean.setOpenPrice(val._open);
		bean.setDayHigh(val._high);
		bean.setDayLow(val._low);
		String calc = val.calcMethod();
		updateLastConditionaly(bean, val, calc);
		bean.setChangePrice(val._netChg);
		bean.setPercentChange(val._pctChg);
		bean.setExchangeId(val.protocol() + "");
		DateTimeUtility.processDate(bean, val.MsgTimeMs());
		IndicesMFQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Called when an IDXSummary update is received for an index ticker.
	 */
	public void OnUpdate(String StreamName, IDXSummary q) {
		String tkr = q.tkr();
		if (tkr == null) {
			NTPLogger.dropSymbol(StreamName, "IDXSummary.tkr() is null");
			return;
		}
		QTCPDMessageBean bean = getCachedBean(tkr);
		bean.setExchangeId(q.protocol() + "");
		IndicesMFQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Callback invoked when an Bond quote is received.
	 * 
	 * @param StreamName Name of this Data Stream
	 * @param qm         BONDQuote
	 */
	public void OnUpdate(String StreamName, BONDQuote quote) {
		String ticker = quote.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "BONDQuote.tkr() is null");
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setBidPrice(quote._bid);
		bean.setAskPrice(quote._ask);
		bean.setBidSize(quote._bidSize);
		bean.setAskSize(quote._askSize);
		bean.setOpenPriceRange1(quote._askYield);
		bean.setOpenPriceRange2(quote._bidYield);
		bean.setAskExchangeCode(getExchangeCode(quote._mktCtr));
		bean.setBidExchangeCode(getExchangeCode(quote._mktCtr));
		bean.setExchangeId(quote.protocol() + "");
		IndicesMFQTMessageQueue.getInstance().getBondsWriterQueue().add(bean);
		IndicesMFQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
	}

	/**
	 * Callback invoked when an Bond trade is received.
	 * 
	 * @param StreamName Name of this Data Stream
	 * @param qm         BONDTrade
	 */
	public void OnUpdate(String StreamName, BONDTrade trade) {
		String ticker = trade.tkr();
		if (ticker == null) {
			NTPLogger.dropSymbol(StreamName, "BONDTrade.tkr() is null");
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		bean.setLastPrice(trade._trdPrc);
		bean.setLastTradeVolume(trade._trdVol);
		bean.setVolume(trade._acVol);
		bean.setDayHigh(trade._high);
		bean.setDayLow(trade._low);
		bean.setVWAP(trade._vwap + "");
		bean.setChangePrice(trade._netChg);
		bean.setPercentChange(trade._pctChg);
		bean.setMarketCenter(getExchangeCode(trade._mktCtr));
		bean.setExchangeId(trade.protocol() + "");
		bean.setExchangeCode("ARCA Bonds");
		DateTimeUtility.processDate(bean, trade.MsgTimeMs());
		IndicesMFQTMessageQueue.getInstance().getBondsWriterQueue().add(bean);
		IndicesMFQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
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
					if (instrument.equals("ARCABOND") && populator.isBonds()) {
						subscribeNewIssue(ticker);
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

	private void subscribeNewIssue(String ticker) {
		Collection<String> tickerList = populator.getAllTickers();
		if (tickerList.contains(ticker)) {
			NTPLogger.info("NEWISSUE OnBlobTable ticker is already in ticker list - un-subscribing");
			NTPUtility.unsubscribeTicker(ticker);
		}
		NTPLogger.info("NEWISSUE Going to subscribe : " + ticker);
		subscribe(ticker);
		tickerList.add(ticker);
	}

	private void updateLastConditionaly(QTCPDMessageBean bean, IDXValue val, String calc) {
		double dVal = val.dVal();
		if (val.IsETPDividend()) {
			dVal = val.ETPDividend();
			bean.setLastPrice(dVal);
		} else if (val.IsETPEstimatedCashPCU()) {
			dVal = val.ETPEstimatedCashPCU();
			bean.setLastPrice(dVal);
		} else if (val.IsETPIntradayValue()) {
			dVal = val.ETPIntradayValue();
			bean.setLastPrice(dVal);
		} else if (val.IsETPNetAssetValue()) {
			dVal = val.ETPNetAssetValue();
			bean.setLastPrice(dVal);
		} else if (val.IsETPSharesOutstanding()) {
			dVal = val.ETPSharesOutstanding();
			bean.setLastPrice(dVal);
		} else if (val.IsETPTotalCashPCU()) {
			dVal = val.ETPTotalCashPCU();
			bean.setLastPrice(dVal);
		}
		if (calc.equals("LAST"))
			bean.setLastPrice(dVal);
		else if (calc.equals("ASK"))
			bean.setAskPrice(dVal);
		else if (calc.equals("BID"))
			bean.setBidPrice(dVal);
	}

	private QTCPDMessageBean getCachedBean(String ticker) {
		Map<String, QTCPDMessageBean> qtMap = IndicesMFQTMessageQueue.getInstance().getSubsData();
		QTCPDMessageBean bean = qtMap.get(ticker);
		if (bean == null) {
			bean = new QTCPDMessageBean();
			bean.setTicker(ticker);
			bean.setSystemTicker(ticker);
			qtMap.put(ticker, bean);
		}
		return bean;
	}

	private String getExchangeCode(int protocol) {
		switch (protocol) {
		case 50:
			return "INDEX OPRA";
		case 70:
			return "NASDAQ Mutual Funds";
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

	private String getExchangeCode(String exchgCode) {
		if (exchgCode == null || exchgCode.startsWith("??") || exchgCode.contains("?") || exchgCode.length() == 0)
			return " ";
		return exchgCode;
	}
}
