package ntp.options.streamer;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;

public class OptionsEquityStreamingChannel extends UltraChan {

	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;
	private String ip;

	private ConcurrentMap<String, Double> equityPriceMap = OptionsQTMessageQueue.getInstance().getEquityPriceMap();

	public OptionsEquityStreamingChannel(String name, String ip, int port) {
		super(ip, port, name, "password", false);
		this.name = name;
		this.ip = ip;
		connectChannel();
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, this.ip);
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
					threshold = Integer.parseInt(CPDProperty.getInstance().getProperty("EQUITY_THRESHOLD"));
					sleepTime = Integer.parseInt(CPDProperty.getInstance().getProperty("EQUITY_SLEEPTIME"));
				} catch (Exception e) {
					NTPLogger.missingProperty("EQUITY_THRESHOLD");
					NTPLogger.defaultSetting("EQUITY_THRESHOLD", "200");
					NTPLogger.missingProperty("EQUITY_SLEEPTIME");
					NTPLogger.defaultSetting("EQUITY_SLEEPTIME", "500");
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
							NTPLogger.warning("OptionsEquityStreamingChannel: exception while threshold thread sleep");
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
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "img Incorrect symbol " + ticker);
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		NTPLogger.image(name, img);
		Double lastPrice = 0d;
		if (img._trdTime_ext > img._trdTime)
			lastPrice = img._trdPrc_ext;
		else
			lastPrice = img._trdPrc;
		equityPriceMap.put(ticker, lastPrice);
	}

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
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trd Incorrect symbol " + ticker);
			return;
		}
		if (!(trd.IsEligible())) {
			if (trd.IsExtended())
				equityPriceMap.put(ticker, trd._trdPrc_ext);
		} else if (trd.IsCxl()) {
			NTPLogger.dropSymbol(StreamName, "trd Cancel trade " + ticker);
			return;
		} else
			equityPriceMap.put(ticker, trd._trdPrc);
	}
}
