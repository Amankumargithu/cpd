package ntp.equity;

import java.util.Collection;
import java.util.Map;

import ntp.bean.QTCPDMessageBean;
import ntp.equity.subs.EquityQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class NBDelayedEquityChannel extends UltraChan {

	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;
	private String exchgFilter[];
	private String ip = NTPConstants.IP;

	public NBDelayedEquityChannel(String ip, int port, String name) {
		super(ip, port, name, name, false);
		this.ip = ip;
		this.name = name;
		connectChannel();
		String exchangeFilter = CPDProperty.getInstance().getProperty("EXCHANGE_FILTER");
		exchangeFilter = (exchangeFilter == null) ? "20,21" : exchangeFilter;
		exchgFilter = exchangeFilter.split(",");
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, this.ip);
		}
	}

	public void subscribe(String ticker) {
		if (!NTPTickerValidator.isCanadianStock(ticker) && NTPTickerValidator.isEquityRegionalSymbol(ticker))
			return;
		int streamID = Subscribe(ticker, ++idx);
		// Not using map to avoid overriding of Basdaq Basic Equity symbols
		// NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
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
							NTPLogger.warning("NBDelayedEquityChannel: exception while threshold thread sleep");
						}
					}
				}
				NTPLogger.info("Completed Subsscriptions for " + name);
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
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "img Incorrect symbol " + ticker);
			return;
		}
		if (isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "img Excluded protocol " + ticker);
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		if (bean != null) {
			bean.setVolumePlus(img._acVol);
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
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
		if (prot == 9 || prot == 10 || prot == 13 || prot == 14 || prot == 34) {
			NTPLogger.dropSymbol(StreamName, "trd protocol is " + prot);
			return;
		}
		ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
		if (NTPTickerValidator.isEquityRegionalSymbol(ticker)) {
			NTPLogger.dropSymbol(StreamName, "trd Incorrect symbol " + ticker);
			return;
		}
		if (isProtocolExcluded(prot)) {
			NTPLogger.dropSymbol(StreamName, "trd Excluded protocol " + ticker);
			return;
		}
		QTCPDMessageBean bean = getCachedBean(ticker);
		if (bean != null) {
			bean.setVolumePlus(trd._acVol);
			EquityQTMessageQueue.getInstance().getmQueue().add(bean.getTicker(), bean);
		}
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

	private boolean isProtocolExcluded(int protocol) {
		for (int i = 0; i < exchgFilter.length; i++) {
			if (exchgFilter[i].equals(protocol + ""))
				return true;
		}
		return false;
	}
}
