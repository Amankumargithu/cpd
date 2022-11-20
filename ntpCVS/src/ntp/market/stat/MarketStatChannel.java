package ntp.market.stat;

import java.util.Collection;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class MarketStatChannel extends UltraChan{

	private static MarketStatChannel instance = new MarketStatChannel("MktStat");
	private String name = "";
	private boolean isRunning = false;
	private int idx = 0;

	private MarketStatChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
	}

	public static MarketStatChannel getInstance(){
		return instance;
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public void subscribe(String ticker)
	{
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
	}

	public void subscribeTickers(final Collection<String> tickerList) 
	{
		new Thread(new Runnable() {
			public void run() {
				int count = 0;
				int threshold = 200;
				int sleepTime = 500;
				try 
				{
					threshold = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD"));
					sleepTime = Integer.parseInt(CPDProperty.getInstance().getProperty("SLEEP_TIME"));
				} 	
				catch (Exception e) 
				{
					NTPLogger.missingProperty("THRESHOLD");
					NTPLogger.defaultSetting("THRESHOLD", "200");
					NTPLogger.missingProperty("SLEEP_TIME");
					NTPLogger.defaultSetting("SLEEP_TIME", "500");
					threshold = 200;
					sleepTime = 500;
				}
				for (String ticker : tickerList)
				{
					count ++;
					subscribe(ticker);
					if(count % threshold == 0)
					{
						try 
						{
							Thread.sleep(sleepTime);
						} 
						catch (Exception e) {
							NTPLogger.warning("MarketStatChannel: exception while threshold thread sleep");
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
	public void OnStatus( String StreamName, Status sts )
	{
		char mt = sts.mt();
		if(mt== UltraChan._mtDEAD)
		{
			String ticker = sts.tkr();
			if(ticker != null)
				NTPLogger.dead(ticker, name);
		}
		else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	public void OnImage( String StreamName, Image img )
	{
		String ticker = img.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		int prot = img.protocol();
		if(prot == 9 || prot ==  10  || prot == 13 ||  prot ==14 || prot == 34 )
		{
			NTPLogger.dropSymbol(StreamName, "img protocol is "+ prot);
			NTPUtility.unsubscribeTicker(ticker);
			return;
		}
		NTPLogger.image(name, img);
		QTCPDMessageBean qtCPDMessageBean = new QTCPDMessageBean();
		if (img._nTrade == 0)
		{
			NTPLogger.info("_nTrade 0 for " + ticker);
			qtCPDMessageBean.setLastPrice(0);		
			qtCPDMessageBean.setVolume(0);
			img._prcTck = '-';
		}
		else
		{
			qtCPDMessageBean.setLastPrice(img._trdPrc);		
			qtCPDMessageBean.setVolume(img._acVol);
		}
		qtCPDMessageBean.setSystemTicker(ticker);
		qtCPDMessageBean.setTicker(ticker);
		qtCPDMessageBean.setLastClosedPrice(img._close);
		MarketStatUpdater.getInstance().onUpdate(qtCPDMessageBean, img._prcTck);
	}

	/**
	 * Called when an EQTrade update is received for an equity ticker.
	 */
	public void OnUpdate( String StreamName, EQTrade trd )
	{
		String ticker = trd.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "trd.tkr() is null");
			return;
		}
		int prot = trd.protocol();
		if(prot == 9 || prot ==  10  || prot == 13 ||  prot ==14 || prot == 34)
		{
			NTPLogger.dropSymbol(StreamName, "trd protocol is "+ prot);
			return;
		}
		if(trd.IsCxl())
		{
			NTPLogger.dropSymbol(StreamName, "trd Cancel trade "+ ticker);
			return;
		}
		if (!MarketStatUpdater.getInstance().isTradedTicker(ticker))
		{
			MarketStatUpdater.getInstance().addTickertoTradedList(ticker);
			MarketStatUpdater.getInstance().writeTradedTicker(ticker);
		}
		QTCPDMessageBean bean = new QTCPDMessageBean();
		/*if(trd.IsSummary() || !(trd.IsEligible()))
		{
			bean.setVolume(trd._acVol);
			bean.setLastClosedPrice(trd.PrevClose());
		}
		else
		{*/
			bean.setLastPrice(trd._trdPrc);
			bean.setSystemTicker(ticker);
			bean.setTicker(ticker);
			bean.setVolume(trd._acVol);		
			bean.setLastClosedPrice(trd.PrevClose());		
//		}
		MarketStatUpdater.getInstance().onUpdate(bean, trd._prcTck);
	}
}
