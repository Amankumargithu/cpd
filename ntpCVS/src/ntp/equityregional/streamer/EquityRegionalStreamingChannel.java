package ntp.equityregional.streamer;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.equityregional.cache.EquityRegionalQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.EquityRegionalUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import QuoddFeed.msg.EQQuote;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class EquityRegionalStreamingChannel extends UltraChan {

	private String name;
	private boolean isRunning = false;

	//logging variables
	private int subscriptionCount = 0;
	private HashSet<String> imageSet = new HashSet<>();
	private HashSet<String> deadSet = new HashSet<>();
	private int messageCount = 0;
	private long logStartTime;
	private int idx = 0;

	private MappedMessageQueue queue =  EquityRegionalQTMessageQueue.getInstance().getmQueue();
	private EquityRegionalUtility utility = EquityRegionalUtility.getInstance();
	private ConcurrentHashMap<String, String> incorrectTickerMap = EquityRegionalQTMessageQueue.getInstance().getIncorrectTickerMap();

	public EquityRegionalStreamingChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
		try{
			boolean doLog = CPDProperty.getInstance().getProperty("LOG_CHANNEL").equalsIgnoreCase("TRUE") ? true : false;
			if(doLog)
				startLoggingThread();
		}
		catch(Exception e)
		{
			NTPLogger.missingProperty("LOG_CHANNEL");
		}
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
		++ subscriptionCount;
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
	public void OnImage(String StreamName, Image image) {
		messageCount++;
		String tkr = image.tkr();
		if(tkr == null || tkr.length()<1)
		{
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		try 
		{
			QTCPDMessageBean qtMessageBean = getQTMessageBean(tkr);
			qtMessageBean.setSystemTicker(tkr);
			qtMessageBean.setLastPrice(image._trdPrc);
			qtMessageBean.setChangePrice(image._netChg);
			qtMessageBean.setPercentChange(image._pctChg);
			qtMessageBean.setLastTradeVolume(image._trdVol);
			qtMessageBean.setVolume(image._acVol);
			qtMessageBean.setTicker(tkr);
			qtMessageBean.setDayHigh(image._high);
			qtMessageBean.setDayLow(image._low);
			qtMessageBean.setOpenPrice(image._open);
			qtMessageBean.setAskPrice(image._ask);
			qtMessageBean.setAskSize(image._askSize);
			qtMessageBean.setBidPrice(image._bid);
			qtMessageBean.setBidSize(image._bidSize);
			qtMessageBean.setOpenPrice(image._open);
			qtMessageBean.setOpenPrice(image._open);
			String marketCenter = image._priMktCtr;
			qtMessageBean.setLastClosedPrice(image._close);
			qtMessageBean.setExtendedLastPrice(image._trdPrc_ext);
			qtMessageBean.setExtendedChangePrice(image._netChg_ext);
			qtMessageBean.setExtendedPercentChange(image._pctChg_ext);
			qtMessageBean.setExtendedLastTradeVolume(image._trdVol_ext);
			DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, image._trdTime_ext);
			qtMessageBean.setExtendedMarketCenter(utility.getEquityPlusExchangeCode(image._trdMktCtr_ext));
			qtMessageBean.setMarketCenter(utility.getEquityPlusExchangeCode(marketCenter));
			qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(marketCenter));
			qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(marketCenter));
			DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
			EquityRegionalQTMessageQueue.getInstance().getmQueue().add(tkr,qtMessageBean);
		}
		catch (Exception e) 
		{
			NTPLogger.error("EquityRegionalStreamingChannel.onImage() " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void OnUpdate(String StreamName, EQQuote quote) {
		messageCount++;
		String tkr = quote.tkr();
		if(tkr == null || tkr.length()<1)
		{
			NTPLogger.dropSymbol(StreamName, "EQQuote.tkr() is null");
			return;
		}
		try 
		{
			QTCPDMessageBean qtMessageBean = getQTMessageBean(tkr);
			qtMessageBean.setSystemTicker(tkr);
			qtMessageBean.setBidPrice(quote._bid);
			qtMessageBean.setAskPrice(quote._ask);
			qtMessageBean.setBidSize(quote._bidSize);
			qtMessageBean.setAskSize(quote._askSize);
			qtMessageBean.setTicker(tkr);
			// As we will be getting quotes from regional exchange so bid and ask market centers will
			// be equal to trade market center
			String marketCenter = utility.getEquityPlusExchangeCode(quote._mktCtr);
			qtMessageBean.setBidExchangeCode(marketCenter);
			qtMessageBean.setAskExchangeCode(marketCenter);
			queue.add(tkr,qtMessageBean);
		}
		catch (Exception e) 
		{
			NTPLogger.error("EquityRegionalStreamingChannel.onEQQuote() " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void OnUpdate(String StreamName, EQTrade trade) {
		messageCount++;
		String tkr = trade.tkr();
		if(tkr == null || tkr.length()<1)
		{
			NTPLogger.dropSymbol(StreamName, "EQTrade.tkr() is null");
			return;
		}
		try 
		{
			QTCPDMessageBean qtMessageBean = getQTMessageBean(tkr);
			qtMessageBean.setSystemTicker(tkr);
			qtMessageBean.setTicker(tkr);
			if(!(trade.IsEligible()))
			{
				qtMessageBean.setVolume(trade._acVol);
				if(trade.IsExtended())
				{
					qtMessageBean.setExtendedLastPrice(trade._trdPrc_ext);
					qtMessageBean.setExtendedChangePrice(trade._netChg_ext);
					qtMessageBean.setExtendedPercentChange(trade._pctChg_ext);
					qtMessageBean.setExtendedLastTradeVolume(trade._trdVol_ext);
					DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, trade.MsgTimeMs());
					qtMessageBean.setExtendedMarketCenter(utility.getEquityPlusExchangeCode(trade._trdMktCtr_ext));
				}
				return;
			}
			qtMessageBean.setLastPrice(trade._trdPrc);
			qtMessageBean.setLastTradeVolume(trade._trdVol);
			qtMessageBean.setVolume(trade._acVol);
			qtMessageBean.setOpenPrice(trade._openPrc);
			qtMessageBean.setDayHigh(trade._high);
			qtMessageBean.setDayLow(trade._low);
			qtMessageBean.setChangePrice(trade._netChg);
			qtMessageBean.setPercentChange(trade._pctChg);
			qtMessageBean.setLastClosedPrice(trade.PrevClose());
			qtMessageBean.setMarketCenter(utility.getEquityPlusExchangeCode(trade._mktCtr));
			DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, trade.MsgTimeMs());
			qtMessageBean.setLastTradeTime(utility.getFormattedTime(trade.MsgTime()));
			queue.add(tkr,qtMessageBean);
		}
		catch (Exception e) 
		{
			NTPLogger.error("EquityRegionalStreamingChannel.onEQTrade() " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void OnStatus( String StreamName, Status sts )
	{
		messageCount++;
		char mt = sts.mt();
		if(mt== UltraChan._mtDEAD)
		{
			String ticker = sts.tkr();
			if(ticker != null)
			{
				NTPLogger.dead(ticker, name);
				deadSet.add(ticker);
				incorrectTickerMap.put(ticker, ticker);
			}
		}
		else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}
	
	private void startLoggingThread()
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true)
				{
					long timeDiff = System.currentTimeMillis() - logStartTime;
					if (timeDiff > 5000)
					{
						NTPLogger.info("CHANNEL ("+name+") Ticker Requested: " + subscriptionCount + " Image Received: " + imageSet.size()
								+ " DeadCount: "+ deadSet.size() + " Messages Processed: " + messageCount + " in time(ms) " + timeDiff);
						logStartTime = System.currentTimeMillis();
						messageCount = 0;
					}
					try {
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
			}
		},"EqRegstr_" + name + "_Logger").start();
	}

	public String getChannelName()
	{
		return this.name;
	}

	private QTCPDMessageBean getQTMessageBean(String ticker){
		QTCPDMessageBean qtMessageBean = EquityRegionalQTMessageQueue.getInstance().getSubsData().get(ticker);
		if (qtMessageBean == null){
			qtMessageBean = new QTCPDMessageBean();
			EquityRegionalQTMessageQueue.getInstance().getSubsData().put(ticker,qtMessageBean);
			qtMessageBean.setTicker(ticker);
			qtMessageBean.setSystemTicker(ticker);
		}
		return qtMessageBean;
	}
}
