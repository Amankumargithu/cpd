package ntp.futureOptions.streamer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.futureOptions.cache.FutureOptionsMemoryDB;
import ntp.futureOptions.cache.FutureOptionsQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;
import ntp.util.DateTimeUtility;
import ntp.util.FuturesOptionsUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.FUTRQuote;
import QuoddFeed.msg.FUTRTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

import com.b4utrade.bean.StockOptionBean;

public class FutureOptionsStreamingChannel extends UltraChan{

	private boolean isRunning = false;
	private String name;
	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;

	private MappedMessageQueue queue =  FutureOptionsQTMessageQueue.getInstance().getmQueue();
	private ConcurrentHashMap<String, String> imageTickerMap = FutureOptionsQTMessageQueue.getInstance().getImageTickerMap();
	private ConcurrentHashMap<String, String> incorrectTickerMap = FutureOptionsQTMessageQueue.getInstance().getIncorrectTickerMap();
	private FuturesOptionsUtility utility = FuturesOptionsUtility.getInstance();

	//logging variables
	private int subscriptionCount = 0;
	private HashSet<String> imageSet = new HashSet<>();
	private HashSet<String> deadSet = new HashSet<>();
	private int messageCount = 0;
	private long logStartTime;
	private int idx = 0;
	private SimpleDateFormat expirationformatter = new SimpleDateFormat("MMddyyyy");

	public FutureOptionsStreamingChannel(String name) {
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

	public String getChannelName()
	{
		return this.name;
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
		//		ticker = OptionsUtility.getInstance().getEQPlusFormattedTicker(image.tkr());
		imageSet.add(ticker);
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		NTPLogger.image(name, image);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setLastPrice(image._trdPrc);
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
		qtMessageBean.setLastClosedPrice(image._close);
		qtMessageBean.setOpenPrice(image._open);
		qtMessageBean.setExchangeId("" + image.protocol());
		qtMessageBean.setTickUpDown(image._prcTck);
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
		imageTickerMap.put(ticker, ticker);
		//Update Fundamental Cache
		try
		{
			StockOptionBean bean = new StockOptionBean();
			String expiryDate = "" +  image._settleDate;
			Date expiry = expirationformatter.parse(expiryDate);
			Calendar c = Calendar.getInstance();
			c.setTime(expiry);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			bean.setExpirationDate(c);
			String type = ticker.substring(ticker.length() -1);
			if (type.equals("C"))
				bean.setOptionType(1);
			else
				bean.setOptionType(2);
			bean.setOpenInterest(image._openVol);
			bean.setSecurityDesc(image.Description());
			bean.setTicker(ticker);			
			try{
				String[] arr = ticker.split("\\\\");
				bean.setStrikePrice(Double.parseDouble(arr[2].substring(0, arr[2].length() -1)));
			}
			catch(Exception e)
			{
				NTPLogger.warning("Cannot process future Option Strike - John Issue");
			}
			FutureOptionsMemoryDB.getInstance().addStockFundamentalData(ticker, bean);
		}
		catch(Exception e){}
		queue.add(ticker, qtMessageBean);
	}

	/**
	 * Called when an OPBbo update is received for an option ticker.
	 */
	@Override
	public void OnUpdate(String StreamName, FUTRQuote bbo) {
		messageCount++;
		String ticker = bbo.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "FUTRQuote.tkr() is null");
			return;
		}
		if(!imageTickerMap.containsKey(ticker))
		{
			NTPLogger.dropSymbol(StreamName, "FUTRQuote No Image "+ ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setBidPrice(bbo._bid);
		qtMessageBean.setAskPrice(bbo._ask);
		qtMessageBean.setBidSize(bbo._bidSize);
		qtMessageBean.setAskSize(bbo._askSize);
		qtMessageBean.setExchangeId("" + bbo.protocol());
		queue.add(ticker, qtMessageBean);
	}

	/**
	 * Called when an OPTrade update is received for an option ticker.
	 */
	@Override
	public void OnUpdate(String StreamName, FUTRTrade trade) {
		messageCount++;
		String ticker = trade.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "FUTRTrade.tkr() is null");
			return;
		}
		if(!imageTickerMap.containsKey(ticker))
		{
			NTPLogger.dropSymbol(StreamName, "FUTRTrade No Image "+ ticker);
			return;
		}
		QTCPDMessageBean qtMessageBean = getCachedBean(ticker);
		qtMessageBean.setLastPrice(trade._trdPrc);
		qtMessageBean.setSystemTicker(ticker);
		qtMessageBean.setTicker(ticker);
		qtMessageBean.setLastTradeVolume(trade._trdVol);
		qtMessageBean.setVolume(trade._acVol);
		qtMessageBean.setOpenPrice(trade._openPrc);
		qtMessageBean.setDayHigh(trade._high);
		qtMessageBean.setDayLow(trade._low);
		qtMessageBean.setChangePrice(trade._netChg);
		qtMessageBean.setPercentChange(trade._pctChg);
		double lastClosedPrice = trade._trdPrc - trade._netChg;
		qtMessageBean.setLastClosedPrice(lastClosedPrice);
		qtMessageBean.setExchangeId("" + trade.protocol());
		DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, trade.MsgTimeMs());
		qtMessageBean.setTickUpDown(trade._prcTck);
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

					if( !(ticker.startsWith("FO:")) )
					{
						NTPLogger.info("NEWISSUE OnBlobTable not a future option symbol instrument = " + instrument);
						return;
					}
					if(! ticker.contains("/") ){
						if(incorrectTickerMap.containsKey(ticker))
							incorrectTickerMap.remove(ticker);
						NTPLogger.newIssue(ticker, instrument);
						String rootSymbol = utility.getRootSymbol(ticker);
						String underlyerSymbol = FutureOptionsQTMessageQueue.getInstance().getUnderlyer(rootSymbol);
						FutureOptionsQTMessageQueue.getInstance().addMarkedSymbol(underlyerSymbol);					
					}
					else
					{
						NTPLogger.info("NEWISSUE OnBlobTable not a option symbol instrument = " + instrument);
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
		},"Opstr_" + name + "_Logger").start();
	}

	private QTCPDMessageBean getCachedBean(String ticker)
	{
		Map<String, QTCPDMessageBean> qtMap = FutureOptionsQTMessageQueue.getInstance().getSubsData();
		QTCPDMessageBean cachedBean = qtMap.get(ticker);
		if (cachedBean == null){
			cachedBean = new QTCPDMessageBean();
			qtMap.put(ticker,cachedBean);
		}
		return cachedBean;
	}
}
