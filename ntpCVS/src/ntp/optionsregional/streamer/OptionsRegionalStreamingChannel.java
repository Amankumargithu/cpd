package ntp.optionsregional.streamer;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.optionsregional.cache.OptionsRegionalQTMessageQueue;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import ntp.util.OptionsRegionalUtility;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.OPQuote;
import QuoddFeed.msg.OPTrade;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class OptionsRegionalStreamingChannel extends UltraChan {

	private String name;
	private boolean isRunning = false;
	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	//logging variables
	private int subscriptionCount = 0;
	private HashSet<String> imageSet = new HashSet<>();
	private HashSet<String> deadSet = new HashSet<>();
	private int messageCount = 0;
	private long logStartTime;
	private int idx = 0;

	private MappedMessageQueue queue =  OptionsRegionalQTMessageQueue.getInstance().getmQueue();
	private ConcurrentHashMap<String, String> imageTickerMap = OptionsRegionalQTMessageQueue.getInstance().getImageTickerMap();
	private ConcurrentHashMap<String, String> incorrectTickerMap = OptionsRegionalQTMessageQueue.getInstance().getIncorrectTickerMap();

	public OptionsRegionalStreamingChannel(String name) {
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

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	@Override
	public void OnImage(String StreamName, Image image) {
		messageCount++;
		String ticker = image.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		ticker = OptionsRegionalUtility.getInstance().getEQPlusFormattedTicker(image.tkr());
		imageSet.add(ticker);
		QTCPDMessageBean qtMessageBean =  OptionsRegionalQTMessageQueue.getInstance().getCachedBean(ticker);
		NTPLogger.image(name, image);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setLastPrice(image._trdPrc);
		qtMessageBean.setLastTradeVolume(image._trdVol);
		qtMessageBean.setRegularOptionVolume(image._acVol);
		qtMessageBean.setVolume(qtMessageBean.getPreOptionVolume() + image._acVol);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setDayHigh(image._high);
		qtMessageBean.setDayLow(image._low);
		qtMessageBean.setChangePrice(image._netChg);
		qtMessageBean.setPercentChange(image._pctChg);
		qtMessageBean.setOpenPrice(image._open);
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
		if( !(image._ask == 0 &&  image._askSize == 0))
		{
			qtMessageBean.setAskPrice(image._ask);
			qtMessageBean.setAskSize(image._askSize);
			qtMessageBean.setAskExchangeCode(image._iAskMktCtr + "");
		}
		if(!(image._bid == 0 && image._bidSize == 0 ))
		{
			qtMessageBean.setBidPrice(image._bid);
			qtMessageBean.setBidSize(image._bidSize);
			qtMessageBean.setBidExchangeCode(image._iBidMktCtr + "");		
		}
		imageTickerMap.put(ticker, ticker);
		queue.add(ticker, qtMessageBean);
	}

	public void OnUpdate(String StreamName, OPQuote quote) {
		messageCount++;
		String ticker = quote.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "OPQuote.tkr() is null");
			return;
		}
		ticker = OptionsRegionalUtility.getInstance().getEQPlusFormattedTicker(ticker);
		if(!imageTickerMap.containsKey(ticker))
		{
			NTPLogger.dropSymbol(StreamName, "OPTrade No Image "+ ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = OptionsRegionalQTMessageQueue.getInstance().getCachedBean(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setBidPrice(quote._bid);
		qtMessageBean.setAskPrice(quote._ask);
		qtMessageBean.setBidSize(quote._bidSize);
		qtMessageBean.setAskSize(quote._askSize);
		qtMessageBean.setTicker(ticker);
		queue.add(ticker, qtMessageBean);
	}

	/**
	 * Called when an OPTrade update is received for an option ticker.
	 */
	@Override
	public void OnUpdate(String StreamName, OPTrade trade) {
		messageCount++;
		String ticker = trade.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "OPTrade.tkr() is null");
			return;
		}
		ticker = OptionsRegionalUtility.getInstance().getEQPlusFormattedTicker(ticker);
		if(!imageTickerMap.containsKey(ticker))
		{
			NTPLogger.dropSymbol(StreamName, "OPTrade No Image "+ ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = OptionsRegionalQTMessageQueue.getInstance().getCachedBean(ticker);
		if(shouldUpdateLast(trade.TradeFlags()))
			qtMessageBean.setLastPrice(trade._trdPrc);

		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setRegularOptionVolume(trade._acVol);
		qtMessageBean.setVolume(qtMessageBean.getPreOptionVolume() + trade._acVol);
		qtMessageBean.setLastTradeVolume(trade._trdVol);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setDayHigh(trade._high);
		qtMessageBean.setDayLow(trade._low);
		qtMessageBean.setChangePrice(trade._netChg);
		qtMessageBean.setPercentChange(trade._pctChg);
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, trade.MsgTimeMs());
		queue.add(ticker, qtMessageBean);	
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

	/**
	 * Called when an ticker is added in between the market
	 */
	@Override
	public void OnBlobTable( String qry, BlobTable blobTable )
	{
		messageCount++;
		try
		{
			if(blobTable != null)
			{
				int rowCount = blobTable.nRow();
				NTPLogger.info("NEWISSUE OnBlobTable rowCount is = " + rowCount);
				for (int count = 0; count < rowCount; count++)
				{
					String ticker = blobTable.GetCell(count, TICKER_POS);
					//check if ticker is equity or not
					String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
					NTPLogger.newIssue(ticker, instrument);

					if( !(ticker.startsWith("O:") && instrument.startsWith("OPR")) )
					{
						NTPLogger.info("NEWISSUE OnBlobTable not a option regional symbol instrument = " + instrument);
						return;
					}
					if(ticker.contains("/") ){
						if(incorrectTickerMap.containsKey(ticker))
							incorrectTickerMap.remove(ticker);
						NTPLogger.newIssue(ticker, instrument);
						if(OptionsRegionalQTMessageQueue.getInstance().isSubscribedSymbol(ticker))
							return;
						// Do not subscribe all new issue symbols as there will be more than 100K symbols on particular days
						//Currently we do not want to subscribe them at all, as it is request based model.
//						subscribe(ticker);
					}
					else
					{
						NTPLogger.info("NEWISSUE OnBlobTable not a option regional symbol instrument = " + instrument);
						return;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
		},"OpRegstr_" + name + "_Logger").start();
	}

	private boolean shouldUpdateLast(String tradeFlags)
	{
		String flags[] = tradeFlags.split(",");
		for (int i = 0; i < flags.length; i++) {
			flags[i] = flags[i].toUpperCase();
			if(flags[i].contains("LAST"))
				return true;
		}
		return false;
	}

	public String getChannelName()
	{
		return this.name;
	}
}
