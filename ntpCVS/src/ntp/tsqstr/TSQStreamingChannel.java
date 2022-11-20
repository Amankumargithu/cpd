package ntp.tsqstr;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.EquityQuotesBean;
import ntp.bean.TSQBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.ExchangeMapPopulator;
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import ntp.util.TSQConditionCodeGenerator;
import QuoddFeed.msg.EQBbo;
import QuoddFeed.msg.EQBboMM;
import QuoddFeed.msg.EQQuote;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class TSQStreamingChannel extends UltraChan {

	private boolean isRunning = false;
	private String name;
	private HashSet<String> exchangeList = ExchangeMapPopulator.getInstance().getExchanges();
	private ConcurrentHashMap<String, EquityQuotesBean> cacheMap = TsqQTMessageQueue.getInstance().getTsqBBOCacheMap();
	private int subscriptionCount = 0;
	private HashSet<String> imageSet = new HashSet<String>();
	private HashSet<String> deadSet = new HashSet<String>();
	private int messageCount = 0;
	private long logStartTime;
	private int idx = 0;	
	private Timestamp timeStamp = new Timestamp(new Date().getTime());
	private Thread t = null;
	private boolean isLogging = true;
	private ConcurrentHashMap<String, Long> symbolTradeIdMap = TsqQTMessageQueue.getInstance().getSymbolTradeIdMap();
	private ExchangeMapPopulator exchangePopulator = ExchangeMapPopulator.getInstance();

	public TSQStreamingChannel(String name) {
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
			NTPLogger.defaultSetting("LOG_CHANNEL", "false");
		}
		exchangeList.remove("TO");
		exchangeList.remove("TV");
		exchangeList.remove("D1");
		exchangePopulator.updateExchangeMap();
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

	public void subscribeTSQSymbol(String ticker)
	{
		if(NTPTickerValidator.isCanadianStock(ticker))
		{
			subscribe(ticker);
			return;
		}
		if(ticker.indexOf("/") > 0)
		{
			NTPLogger.dropSymbol(ticker, "Subscription for root ticker only");
			return;
		}
		subscribe(ticker);
		for (String  exchange : exchangeList)
			subscribe(ticker + "/" + exchange);
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
			{
				NTPLogger.dead(ticker, name);
				deadSet.add(ticker);
				NTPUtility.unsubscribeTicker(ticker);
			}
		}
		else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	public void OnImage( String StreamName, Image image )
	{
		messageCount++;
		String ticker = image.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(ticker, "Null value for Stream " + StreamName);
			return;
		}
		ticker = NTPTickerValidator.getRootSymbol(ticker);
		imageSet.add(ticker);
		NTPLogger.image(name, image);
		EquityQuotesBean tsqBean = getCachedBBOData(ticker);
		tsqBean.setAskPrice(NTPUtility.parseFourDecimal(image._ask));
		tsqBean.setAskSize(image._askSize);
		tsqBean.setBidPrice(NTPUtility.parseFourDecimal(image._bid));
		tsqBean.setBidSize(image._bidSize);
		tsqBean.setAskMarketCenter(exchangePopulator.getEquityPlusExchangeCode(image._askMktCtr));
		tsqBean.setBidMarketCenter(exchangePopulator.getEquityPlusExchangeCode(image._bidMktCtr));
	}

	/**
	 * Called when an EQQuote update is received for an equity ticker from regionals.
	 */
	public void OnUpdate( String StreamName, EQQuote quote )
	{
		messageCount++;
		String ticker = quote.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(ticker, "Null value for Stream " + StreamName);
			return;
		}
		if(quote._bid == 0 && quote._ask == 0 && quote._bidSize == 0 && quote._askSize == 0)
		{
			NTPLogger.dropSymbol(StreamName, "EQQuote quotes are Zero");
			return;
		}
		TSQBean tsqBean = new TSQBean();
		ticker = NTPTickerValidator.getRootSymbol(ticker);
		tsqBean.setTicker(ticker);
		if(quote.protocol() == 14)
		{
			tsqBean.setMessageType(TSQBean.TYPE_COMPOSITE_QUOTE);
			tsqBean.setMsgSequence(quote.RTL());
			tsqBean.setBidPrice(NTPUtility.parseFourDecimal(quote._bid));
			tsqBean.setAskPrice(NTPUtility.parseFourDecimal(quote._ask));
			tsqBean.setBidSize(quote._bidSize);
			tsqBean.setAskSize(quote._askSize);
			tsqBean.setAskMarketCenter("u");
			tsqBean.setBidMarketCenter("u");
			tsqBean.setCreationDateTime(timeStamp);
			tsqBean.setTradeQuoteTime(NTPUtility.getFormattedTime(quote.MsgTime()));
			tsqBean.setExchangeId(quote.protocol() + "");
			resetTradeConditions(tsqBean);
			updateBBOCache(tsqBean);
		}
		else
		{
			tsqBean.setMessageType(TSQBean.TYPE_REGIONAL_QUOTE);
			tsqBean.setMsgSequence(quote.RTL());
			tsqBean.setBidPrice(NTPUtility.parseFourDecimal(quote._bid));
			tsqBean.setAskPrice(NTPUtility.parseFourDecimal(quote._ask));
			tsqBean.setBidSize(quote._bidSize);
			tsqBean.setAskSize(quote._askSize);
			tsqBean.setCreationDateTime(timeStamp);
			// Quotes comes from regional exchanges, hence ask and bid market centers will be equivalent to quote market center
			String marketCenter = exchangePopulator.getEquityPlusExchangeCode(quote._mktCtr);
			tsqBean.setAskMarketCenter(marketCenter);
			tsqBean.setBidMarketCenter(marketCenter);
			tsqBean.setTradeQuoteTime(NTPUtility.getFormattedTime(quote.MsgTime()));
			tsqBean.setExchangeId(quote.protocol() + "");
			resetTradeConditions(tsqBean);
		}		
		TsqQTMessageQueue.getInstance().getmQueue().add(tsqBean);
	}

	/**
	 * Called when an EQBbo update is received for an equity ticker.
	 */
	public void OnUpdate( String StreamName, EQBbo bbo )
	{
		messageCount++;
		String ticker = bbo.tkr();
		if(ticker == null )
		{
			NTPLogger.dropSymbol(ticker, "Null value for Stream " + StreamName);
			return;
		}
		if(bbo._bid == 0 && bbo._ask == 0 && bbo._bidSize == 0 && bbo._askSize == 0)
		{
			NTPLogger.dropSymbol(StreamName, "EQBbo quotes are Zero");
			return;
		}
		TSQBean tsqBean = new TSQBean();
		ticker = NTPTickerValidator.getRootSymbol(ticker);
		tsqBean.setTicker(ticker);
		tsqBean.setMessageType(TSQBean.TYPE_COMPOSITE_QUOTE);
		tsqBean.setMsgSequence(bbo.RTL());
		tsqBean.setBidPrice(NTPUtility.parseFourDecimal(bbo._bid));
		tsqBean.setAskPrice(NTPUtility.parseFourDecimal(bbo._ask));
		tsqBean.setBidSize(bbo._bidSize);
		tsqBean.setAskSize(bbo._askSize);
		tsqBean.setAskMarketCenter(exchangePopulator.getEquityPlusExchangeCode(bbo._askMktCtr));
		tsqBean.setBidMarketCenter(exchangePopulator.getEquityPlusExchangeCode(bbo._bidMktCtr));
		tsqBean.setCreationDateTime(timeStamp);
		tsqBean.setTradeQuoteTime(NTPUtility.getFormattedTime(bbo.MsgTime()));
		tsqBean.setExchangeId(bbo.protocol() + "");
		NTPUtility.formatTSQLimitUpDown(bbo.LimitUpDown(), tsqBean);
		resetTradeConditions(tsqBean);
		updateBBOCache(tsqBean);
		TsqQTMessageQueue.getInstance().getmQueue().add(tsqBean);
	}

	/**
	 * Called when an EQTrade update is received for an equity ticker.
	 */
	public void OnUpdate( String StreamName, EQTrade trd )
	{
		messageCount++;
		String tkr = trd.tkr();
		if (tkr == null)
		{
			NTPLogger.dropSymbol(tkr, "Null symbol for " + StreamName);
			return;
		}
		if (trd.IsSummary()){
			NTPLogger.dropSymbol(tkr, "Summary Message");
			return;
		}
		else if (trd.IsCxl())
		{
			NTPLogger.dropSymbol(tkr, "Cancel Trade Message");
			return;
		}
		tkr = NTPTickerValidator.getRootSymbol(tkr);
		Long tradeId = symbolTradeIdMap.get(tkr);
		if(tradeId != null){
			if(tradeId == trd._trdID){
				if(!trd.tkr().contains("/"))
					NTPLogger.dropSymbol(trd.tkr(), "Already processed trade_id : "+trd._trdID +" rtl : "+trd.RTL());
				return;
			}
			else if(tradeId < trd._trdID){
				if(trd.tkr().contains("/") && !NTPTickerValidator.isCanadianStock(trd.tkr()))
				{
					NTPLogger.dropSymbol(trd.tkr(), "Regional ticker trade_id : "+trd._trdID +" rtl : "+trd.RTL());
					return;
				}
				symbolTradeIdMap.put(tkr, trd._trdID);
			}
			else if(tradeId > trd._trdID){
				NTPLogger.dropSymbol(trd.tkr(), "New Trade ID " + trd._trdID + " old " + tradeId );
				return;
			}
		}
		else
			symbolTradeIdMap.put(tkr, trd._trdID);
		/*if(!NTPTickerValidator.isCanadianStock(tkr) && tkr.indexOf("/") != -1)  // we need to ignore the trades from the regional market
		{							// For example, trade from AAPL/D , will duplicate the trade from AAPL from market center D
			return;
		}*/
		TSQBean tsqBean = new TSQBean();
//		tkr = NTPTickerValidator.getRootSymbol(tkr);
		tsqBean.setTicker(tkr);
		tsqBean.setMessageType(TSQBean.TYPE_TRADE);
		tsqBean.setTradeMarketCenter(exchangePopulator.getEquityPlusExchangeCode(trd._mktCtr));
		tsqBean.setTradePrice(NTPUtility.parseFourDecimal(trd._trdPrc));
		tsqBean.setTradeSize(trd._trdVol);
		String tradeQuoteCondition = null;
		if(NTPTickerValidator.isCanadianTicker(tkr))
			tradeQuoteCondition = TSQConditionCodeGenerator.generateCanadianConditionCode( trd._setlType, trd._rptType,
					trd._rptDetail,trd._rptFlags);
		else
			tradeQuoteCondition = TSQConditionCodeGenerator.generateConditionCode( trd._setlType, trd._rptType, 
					trd._rptDetail,trd._rptFlags);
		tsqBean.setTradeQuoteCondCode1(tradeQuoteCondition);
		tsqBean.setCreationDateTime(timeStamp);
		tsqBean.setMsgSequence(trd.RTL());
		tsqBean.setTradeQuoteTime(NTPUtility.getFormattedTime(trd.MsgTime()));
		tsqBean.setExchangeId(trd.protocol() + "");
		tsqBean.setVwap(trd.vwap(4));
		tsqBean.setTradeSequence(trd._trdID);
		EquityQuotesBean cachedBean = getCachedBBOData(tkr);
		tsqBean.setBidPrice(cachedBean.getBidPrice());
		tsqBean.setAskPrice(cachedBean.getAskPrice());
		tsqBean.setBidSize(cachedBean.getBidSize());
		tsqBean.setAskSize(cachedBean.getAskSize());
		tsqBean.setAskMarketCenter(cachedBean.getAskMarketCenter());
		tsqBean.setBidMarketCenter(cachedBean.getBidMarketCenter());
		TsqQTMessageQueue.getInstance().getmQueue().add(tsqBean);
	}

	/**
	 * Callback invoked when an Short-or Long-Form Equity Market Maker BBO 
	 * Quote message is received
	 *
	 * @param StreamName Name of this Data Stream
	 * @param qm EQBboMM
	 */
	public void OnUpdate( String StreamName, EQBboMM qm )
	{
		messageCount++;
		String ticker = qm.tkr();
		if(ticker == null )
		{
			NTPLogger.dropSymbol(ticker, "Null symbol for " + StreamName);
			return;
		}
		if(qm._bid == 0 && qm._ask == 0 && qm._bidSize == 0 && qm._askSize == 0)
		{
			NTPLogger.dropSymbol(StreamName, "EQBboMM quotes are Zero");
			return;
		}
		TSQBean tsqBean = new TSQBean();
		ticker = NTPTickerValidator.getRootSymbol(ticker);
		tsqBean.setTicker(ticker);
		tsqBean.setMessageType(TSQBean.TYPE_COMPOSITE_QUOTE);
		tsqBean.setMsgSequence(qm.RTL());
		tsqBean.setBidPrice(NTPUtility.parseFourDecimal(qm._bid));
		tsqBean.setAskPrice(NTPUtility.parseFourDecimal(qm._ask));
		tsqBean.setBidSize(qm._bidSize);
		tsqBean.setAskSize(qm._askSize);
		tsqBean.setAskMarketCenter("u");
		tsqBean.setBidMarketCenter("u");
		tsqBean.setCreationDateTime(timeStamp);
		tsqBean.setTradeQuoteTime(NTPUtility.getFormattedTime(qm.MsgTime()));
		tsqBean.setExchangeId(qm.protocol() + "");
		resetTradeConditions(tsqBean);
		updateBBOCache(tsqBean);
		TsqQTMessageQueue.getInstance().getmQueue().add(tsqBean);
	}

	private void startLoggingThread()
	{
		t =new Thread(new Runnable() {
			@Override
			public void run() {
				while (isLogging)
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
		},"TSQstr_" + name + "_Logger");
		t.start();
	}

	public void stopLoggingThread(){
		isLogging = false;
		NTPLogger.info("Stopping Logging thread");
	}

	private EquityQuotesBean getCachedBBOData(String ticker)
	{
//		ticker = NTPTickerValidator.getRootSymbol(ticker);
		EquityQuotesBean cachedBean = cacheMap.get(ticker);
		if(cachedBean == null)
		{
			cachedBean = new EquityQuotesBean();
			cacheMap.put(ticker, cachedBean);
		}
		return cachedBean;
	}

	private void resetTradeConditions(TSQBean tsqBean) {
		tsqBean.setTradePrice(null);
		tsqBean.setTradeSize(null);
		tsqBean.setTradeMarketCenter(null);
		tsqBean.setTradeSequence(null);
	}

	private void updateBBOCache(TSQBean tsqBean) 
	{
		String ticker = tsqBean.getTicker();
		EquityQuotesBean cachedBean = getCachedBBOData(ticker);
		cachedBean.setBidPrice(tsqBean.getBidPrice());
		cachedBean.setAskPrice(tsqBean.getAskPrice());
		cachedBean.setBidSize(tsqBean.getBidSize());
		cachedBean.setAskSize(tsqBean.getAskSize());
		cachedBean.setAskMarketCenter(tsqBean.getAskMarketCenter());
		cachedBean.setBidMarketCenter(tsqBean.getBidMarketCenter());
	}
}
