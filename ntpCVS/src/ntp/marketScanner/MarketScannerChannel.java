package ntp.marketScanner;

import java.util.Collection;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import ntp.util.OTCExchangeMapPopulator;
import ntp.util.StockRetriever;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

import com.b4utrade.bo.MarketScannerBO;

public class MarketScannerChannel extends UltraChan {

	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;
	private MarketScannerCache cache = MarketScannerCache.getInstance();

	private static final int MARKET_CAP_DEFAULT = 0;
	private static final int MARKET_CAP_SMALL = 2;
	private static final int MARKET_CAP_MID = 3;
	private static final int MARKET_CAP_LARGE = 4;

	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private OTCExchangeMapPopulator populator = OTCExchangeMapPopulator.getInstance();

	public MarketScannerChannel(String name) {
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
					if (ticker.contains("/"))
						continue;
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
		if (NTPTickerValidator.isCanadianStock(ticker)) {
			NTPLogger.dropSymbol(ticker, "Canadian ticker");
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		NTPLogger.image(name, img);
		int protocol = img.protocol();
		MarketScannerBO bo = cache.getCachedBean(ticker);
		int exchange;
		if ((protocol == 3) || (protocol == 1) || (protocol == 192) || (protocol == 190))
			exchange = 2; // NYSE
		else if ((protocol == 34) || (protocol == 9))
			exchange = 5; // OTC
		else if ((protocol == 186) || (protocol == 187))
			exchange = 3; // NASDAQ
		else if ((protocol == 4) || (protocol == 2) || (protocol == 193) || (protocol == 191))
			exchange = 4; // AMEX
		else
			exchange = 9;
		bo.setTicker(ticker);
		bo.setPrice(img._trdPrc);
		bo.setVolume(img._acVol);
		bo.setDollarChange(img._netChg);
		bo.setPercentChange(img._pctChg);
		bo.setExchange(exchange);
		double lastPrice = img._trdPrc;
		if (bo.getFiftyTwoWeekHighValue() != null && !(bo.getFiftyTwoWeekHighValue().equalsIgnoreCase("N/A"))) {
			if (lastPrice > Double.parseDouble(bo.getFiftyTwoWeekHighValue()))
				bo.setFiftyTwoWeekHi(true);
		}
		if (bo.getFiftyTwoWeekLowValue() != null && !(bo.getFiftyTwoWeekLowValue().equalsIgnoreCase("N/A"))) {
			if (lastPrice < Double.parseDouble(bo.getFiftyTwoWeekLowValue())) {
				bo.setFiftyTwoWeekLow(true);
			}
		}
		if (bo.getSharesOutStd() != null && !(bo.getSharesOutStd().equalsIgnoreCase("N/A"))) {
			bo.setMarketCapID(calculateMCap(lastPrice, Double.parseDouble(bo.getSharesOutStd())));
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
		if (NTPTickerValidator.isCanadianStock(ticker)) {
			NTPLogger.dropSymbol(ticker, "Canadian ticker");
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		int protocol = trd.protocol();
		MarketScannerBO bo = cache.getCachedBean(ticker);
		int exchange;
		if ((protocol == 3) || (protocol == 1) || (protocol == 192) || (protocol == 190))
			exchange = 2; // NYSE
		else if ((protocol == 34) || (protocol == 9))
			exchange = 5; // OTC
		else if ((protocol == 186) || (protocol == 187))
			exchange = 3; // NASDAQ
		else if ((protocol == 4) || (protocol == 2) || (protocol == 193) || (protocol == 191))
			exchange = 4; // AMEX
		else
			exchange = 9;
		bo.setTicker(ticker);
		bo.setPrice(trd._trdPrc);
		bo.setVolume(trd._acVol);
		bo.setDollarChange(trd._netChg);
		bo.setPercentChange(trd._pctChg);
		bo.setExchange(exchange);
		double lastPrice = trd._trdPrc;
		if (bo.getFiftyTwoWeekHighValue() != null && !(bo.getFiftyTwoWeekHighValue().equalsIgnoreCase("N/A"))) {
			if (lastPrice > Double.parseDouble(bo.getFiftyTwoWeekHighValue()))
				bo.setFiftyTwoWeekHi(true);
		}
		if (bo.getFiftyTwoWeekLowValue() != null && !(bo.getFiftyTwoWeekLowValue().equalsIgnoreCase("N/A"))) {
			if (lastPrice < Double.parseDouble(bo.getFiftyTwoWeekLowValue())) {
				bo.setFiftyTwoWeekLow(true);
			}
		}
		if (bo.getSharesOutStd() != null && !(bo.getSharesOutStd().equalsIgnoreCase("N/A"))) {
			bo.setMarketCapID(calculateMCap(lastPrice, Double.parseDouble(bo.getSharesOutStd())));
		}
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
							|| instrument.equals("TSX") || instrument.equals("OTC") || instrument.equals("OTCBB"))) {
						NTPLogger.info("NEWISSUE OnBlobTable not a marketScanner symbol instrument = " + instrument);
						continue;
					}
					if (NTPTickerValidator.isCanadianStock(ticker))
						subscribeNewIssue(ticker);
					else if (ticker.contains("/")) {
						String[] arr = ticker.split("/");
						if (arr.length == 2) // else it is level2 symbol
						{
							subscribeNewIssue(arr[0]);
							if (populator.isOtcExchangeId(arr[1]))
								subscribeNewIssue(ticker);
						} else {
							NTPLogger.info("NEWISSUE OnBlobTable not a marketScanner symbol instrument = " + instrument
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

	private int calculateMCap(Double last, Double sharesOutStd) {
		try {
			// Shares outstanding is in millions, which is fetched from market_guide_data
			double mCapd = last * sharesOutStd;
			double tenBillion = 10 * 1000.0;// 1000 represents 1 billion
			double twoBillion = 2 * 1000.0;// 1000 represents 1 billion
			if (mCapd > tenBillion)
				return MARKET_CAP_LARGE;
			if (mCapd >= twoBillion && mCapd <= tenBillion)
				return MARKET_CAP_MID;
			if (mCapd < twoBillion)
				return MARKET_CAP_SMALL;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MARKET_CAP_DEFAULT;
	}

	private void subscribeNewIssue(String ticker) {
		Collection<String> tickerList = StockRetriever.getInstance().getTickerList();
		if (tickerList.contains(ticker)) {
			NTPLogger.info("NEWISSUE OnBlobTable ticker is already in ticker list - un-subscribing");
			NTPUtility.unsubscribeTicker(ticker);
		}
		NTPLogger.info("NEWISSUE Going to subscribe : " + ticker);
		subscribe(ticker);
		tickerList.add(ticker);
	}
}
