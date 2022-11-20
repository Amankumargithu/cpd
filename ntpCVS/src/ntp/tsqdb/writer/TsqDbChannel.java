package ntp.tsqdb.writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.EquityQuotesBean;
import ntp.bean.TsqDbSummaryBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.ExchangeMapPopulator;
import ntp.util.MySQLDBManager;
import ntp.util.NTPConstants;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import ntp.util.TSQConditionCodeGenerator;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.EQBbo;
import QuoddFeed.msg.EQBboMM;
import QuoddFeed.msg.EQQuote;
import QuoddFeed.msg.EQTrade;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class TsqDbChannel extends UltraChan {
	private String name = "";
	private char toLimitChar = 'C';
	private char fromLimitChar = 'A';
	private boolean isRunning = false;
	private int idx = 0;
	private static final int TICKER_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private final static String CANCELLED_TRADE = new Short("0").toString();
	private final static String NON_CANCELLED_TRADE = new Short("1").toString();
	private final static String CORRECTION_TRADE = new Short("2").toString();
	private final static String AS_OF_TRADE = new Short("3").toString();
	private final static String AS_OF_CANCELLED_TRADE = new Short("4").toString();
		private final static String AS_OF_CORRECTION_TRADE = new Short("5").toString();
	private final static String TYPE_TRADE = new Short("1").toString();
	private final static String TYPE_COMPOSITE_QUOTE = new Short("2").toString();
	private final static String TYPE_REGIONAL_QUOTE = new Short("3").toString();
	private final static String INCLUDE_IN_VWAP = new Short("1").toString();
	private final static String EXCLUDE_IN_VWAP = new Short("0").toString();
	private static long messageSequence = 1;
	private final static HashSet<String> VWAP_EXCLUSIONS = new HashSet<String>();
	private String TODAY = new SimpleDateFormat("ddMMyy").format(new Date());
	private final String comma = ",";
	private TsqDbFileWriter tradesFileWriter = null;
	private TsqDbFileWriter quotesFileWriter = null;
	private TsqDbFileWriter cancelTradesFileWriter = null;
	private final String newLine = System.getProperty("line.separator");
	private String TORONTO_TSX_SUFFIX = ".T";
	private String VENTURE_TSX_SUFFIX = ".V";
	private ConcurrentHashMap<String, Long> cancelSymbolTradeIdMap = TsqDbCache.getCancelSymbolTradeIdMap();
	private ConcurrentHashMap<String,String> hashedTickers = TsqDbCache.getHashedTickers();
	private ConcurrentHashMap<String,String> tickersList = TsqDbCache.getTickersList();
	private ConcurrentHashMap<String, TsqDbSummaryBean> summaryMessagesMap = TsqDbCache.getSummaryMessagesMap();
	private ExchangeMapPopulator exchangePopulator = ExchangeMapPopulator.getInstance();

	static
	{
		VWAP_EXCLUSIONS.add("2");
		VWAP_EXCLUSIONS.add("3");
		VWAP_EXCLUSIONS.add("14");
		VWAP_EXCLUSIONS.add("17");
		VWAP_EXCLUSIONS.add("18");
		VWAP_EXCLUSIONS.add("20");
		VWAP_EXCLUSIONS.add("21");	    
	}

	public TsqDbChannel(String name, TsqDbFileWriter tradesFileWriter, TsqDbFileWriter quotesFileWriter , TsqDbFileWriter cancelTradesFileWriter)
	{
		super( NTPConstants.IP, NTPConstants.PORT, name, "password", false );
		this.name = name;
		this.tradesFileWriter = tradesFileWriter;
		this.quotesFileWriter = quotesFileWriter;
		this.cancelTradesFileWriter = cancelTradesFileWriter;
		try { toLimitChar = CPDProperty.getInstance().getProperty("TICKER_TO_LIMIT").trim().charAt(0); }
		catch (Exception e) {
			NTPLogger.missingProperty("TICKER_TO_LIMIT");
			toLimitChar = 'C';
			NTPLogger.defaultSetting("TICKER_TO_LIMIT", "" + toLimitChar);
		}
		try { fromLimitChar = CPDProperty.getInstance().getProperty("TICKER_FROM_LIMIT").trim().charAt(0);  }
		catch (Exception e) {
			NTPLogger.missingProperty("TICKER_FROM_LIMIT");
			fromLimitChar = 'A';
			NTPLogger.defaultSetting("TICKER_FROM_LIMIT", "" + fromLimitChar);
		}
		connectChannel();		
	}

	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public void subscribe(String ticker){
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		tickersList.put(ticker, ticker);
		NTPLogger.subscribe(ticker, name, streamID);
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
	public void OnStatus( String StreamName, Status sts )
	{
		char mt = sts.mt();
		if(mt== UltraChan._mtDEAD)
		{
			String ticker = sts.tkr();
			if(ticker != null)
			{
				NTPLogger.dead(ticker, name);
				tickersList.remove(ticker);
				NTPUtility.unsubscribeTicker(ticker);
			}
		}
		else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	@Override
	public void OnImage(String StreamName, Image img) {
		NTPLogger.image(name, img);
		String ticker = img.tkr();
		ticker = NTPTickerValidator.getRootSymbol(ticker);
		EquityQuotesBean tsqBean = TsqDbCache.getCachedBBOData(ticker);
		tsqBean.setTicker(ticker);
		tsqBean.setBidPrice(img._bid);
		tsqBean.setAskPrice(img._ask);
		tsqBean.setBidSize(img._bidSize);
		tsqBean.setAskSize(img._askSize);
		tsqBean.setAskMarketCenter(exchangePopulator.getEquityPlusExchangeCode(img._askMktCtr));
		tsqBean.setBidMarketCenter(exchangePopulator.getEquityPlusExchangeCode(img._bidMktCtr));
	}

	public void OnUpdate( String StreamName, EQTrade trade )
	{
		boolean isSpecialTrade = false;
		try {
			String ticker = trade.tkr();
			//Summary messages have trade id = 0
			if (trade.IsSummary()){
				TsqDbSummaryBean bean = summaryMessagesMap.get(ticker);
				if(bean == null){
					bean = new TsqDbSummaryBean();
					summaryMessagesMap.put(ticker, bean);
				}
				bean.setClosePrice(trade._trdPrc);
				bean.setVolume(trade._acVol);
				bean.setVwap(trade._vwap);
				bean.setTicker(ticker);
				NTPLogger.info("SUMM " + trade.tkr() + " " + trade._trdPrc + " " + trade._acVol + " " + trade._trdVol + " " + trade.RTL() + " " + trade._trdID);
				return;
			}
			else if (trade.IsCxl())
			{
				isSpecialTrade = true;
				//Handle cancel trades
				try {
					ticker = NTPTickerValidator.getRootSymbol(ticker);
					Long tradeId = cancelSymbolTradeIdMap.get(ticker);
					if(tradeId != null && tradeId == trade._trdID)
						return;
					else
						cancelSymbolTradeIdMap.put(ticker, trade._trdID);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
//			else if (trade.IsAsOf())
//			{
//				isSpecialTrade = true;
//				NTPLogger.info("ASOF TRADE " + ticker + " " + trade.pDateTimeMs() + " " + trade._trdVol + " " + trade._trdID);
//			}
//			else if (trade.IsAsOfCxl())
//			{
//				isSpecialTrade = true;
//				NTPLogger.info("ASOFCANCEL TRADE " + ticker + " " + trade.pDateTimeMs() + " " + trade._trdVol + " " + trade._trdID);
//			}
//			else if(trade.IsCorrection())
//			{
//				isSpecialTrade = true;
//				NTPLogger.info("CORRECTION TRADE " + ticker + " " + trade.pDateTimeMs() + " " + trade._trdVol + " " + trade._trdID + " " + trade._old_trdID + " " + trade._old_trdPrc + " " + trade._old_trdVol);
//			}
//			else if(trade.IsAsOfCorrection())
//			{
//				isSpecialTrade = true;
//				NTPLogger.info("ASOFCORRECTION TRADE " + ticker + " " + trade.pDateTimeMs() + " " + trade._trdVol + " " + trade._trdID + " " + trade._old_trdID + " " + trade._old_trdPrc + " " + trade._old_trdVol);
//			}
			else
			{
				try {
					ticker = NTPTickerValidator.canadianToQuoddSymbology(ticker);
					if(NTPTickerValidator.isEquityRegionalSymbol(ticker))
					{
						//Drop regional data
						return;
					}
					TsqDbSummaryBean bean = summaryMessagesMap.get(trade.tkr());
					if(bean == null){
						bean = new TsqDbSummaryBean();
						summaryMessagesMap.put(trade.tkr(), bean);
					}
					bean.setTicker(trade.tkr());
					bean.setDayHigh(trade._high);
					bean.setDayLow(trade._low);
					bean.setOpenPrice(trade._openPrc);
					bean.setClosePrice(trade._trdPrc);
					bean.setVolume(trade._acVol);
					bean.setVwap(trade._vwap);
					bean.setTradeId(trade._trdID);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			StringBuilder sb = new StringBuilder();
			if(isSpecialTrade)
			{
				sb.append(getHashedTicker(ticker));
				sb.append(comma);
				sb.append(sequence());
				sb.append(comma);
				sb.append(trade._trdID);
				sb.append(comma);
				boolean includedInVWAP = true;
				String tradeQuoteCondition = null;
				if(NTPTickerValidator.isCanadianStock(ticker))
					tradeQuoteCondition = TSQConditionCodeGenerator.generateCanadianConditionCode( trade._setlType, trade._rptType,trade._rptDetail,trade._rptFlags);
				else
					tradeQuoteCondition = TSQConditionCodeGenerator.generateConditionCode( trade._setlType, trade._rptType,trade._rptDetail, trade._rptFlags);	
				sb.append(CANCELLED_TRADE);
				includedInVWAP = false;
				sb.append(comma);
				sb.append(TYPE_TRADE);
				sb.append(comma);
				if (includedInVWAP)
					sb.append(INCLUDE_IN_VWAP);
				else
					sb.append(EXCLUDE_IN_VWAP);
				sb.append(comma);
				tradeQuoteCondition = tradeQuoteCondition.replace(',', '|');
				sb.append(tradeQuoteCondition);
				sb.append(comma);
				NTPUtility.parseDateLong(sb, trade.MsgTime(), false);
				sb.append(comma);
				sb.append(TODAY);
				sb.append(comma);
				NTPUtility.appendTo4(sb, trade._trdPrc);
				sb.append(comma);
				NTPUtility.appendTo4(sb, trade.vwap(4));
				sb.append(comma);
				sb.append(trade._trdVol);
				sb.append(comma);
				sb.append(trade._acVol);
				sb.append(comma);
				EquityQuotesBean cachedBean = TsqDbCache.getCachedBBOData(ticker);
				if (cachedBean.getBidPrice() != null)
					NTPUtility.appendTo4(sb, cachedBean.getBidPrice());
				sb.append(comma);
				if (cachedBean.getAskPrice() != null)
					NTPUtility.appendTo4(sb, cachedBean.getAskPrice());
				sb.append(comma);
				if (cachedBean.getBidSize() != null)
					sb.append(cachedBean.getBidSize());
				sb.append(comma);
				if (cachedBean.getAskSize() != null)
					sb.append(cachedBean.getAskSize());
				sb.append(comma);
				sb.append(trade._trdID);
				sb.append(comma);
				String mrktCentr =exchangePopulator.getEquityPlusExchangeCode(trade._mktCtr);
				if (mrktCentr != null)
					sb.append(mrktCentr);
				sb.append(comma);
				if (cachedBean.getBidMarketCenter() != null)
					sb.append(cachedBean.getBidMarketCenter());
				sb.append(comma);
				if(cachedBean.getAskMarketCenter() !=null)
					sb.append(cachedBean.getAskMarketCenter());
				sb.append(comma);
				sb.append(trade.RTL()+"");
				sb.append(newLine);
			}
			else
			{
				sb.append(getHashedTicker(ticker));
				sb.append(comma);
				sb.append(sequence());
				sb.append(comma);
				sb.append(trade._trdID);
				sb.append(comma);
				boolean includedInVWAP = true;
				String tradeQuoteCondition = null;
				if(NTPTickerValidator.isCanadianStock(ticker))
					tradeQuoteCondition = TSQConditionCodeGenerator.generateCanadianConditionCode( trade._setlType, trade._rptType,trade._rptDetail,trade._rptFlags);
				else
					tradeQuoteCondition = TSQConditionCodeGenerator.generateConditionCode( trade._setlType, trade._rptType,trade._rptDetail, trade._rptFlags);	
				sb.append(NON_CANCELLED_TRADE);
				if (doesConditionCodeExcludeFromVWAP(tradeQuoteCondition))
					includedInVWAP = false;
				sb.append(comma);
				sb.append(TYPE_TRADE);
				sb.append(comma);
				if (includedInVWAP)
					sb.append(INCLUDE_IN_VWAP);
				else
					sb.append(EXCLUDE_IN_VWAP);
				sb.append(comma);
				tradeQuoteCondition = tradeQuoteCondition.replace(',', '|');
				sb.append(tradeQuoteCondition);
				sb.append(comma);
				NTPUtility.parseDateLong(sb, trade.MsgTime(), false);
				sb.append(comma);
				sb.append(TODAY);
				sb.append(comma);
				NTPUtility.appendTo4(sb, trade._trdPrc);
				sb.append(comma);
				NTPUtility.appendTo4(sb, trade.vwap(4));
				sb.append(comma);
				sb.append(trade._trdVol);
				sb.append(comma);
				sb.append(trade._acVol);
				sb.append(comma);
				EquityQuotesBean cachedBean = TsqDbCache.getCachedBBOData(ticker);
				if (cachedBean.getBidPrice() != null)
					NTPUtility.appendTo4(sb, cachedBean.getBidPrice());
				sb.append(comma);
				if (cachedBean.getAskPrice() != null)
					NTPUtility.appendTo4(sb, cachedBean.getAskPrice());
				sb.append(comma);
				if (cachedBean.getBidSize() != null)
					sb.append(cachedBean.getBidSize());
				sb.append(comma);
				if (cachedBean.getAskSize() != null)
					sb.append(cachedBean.getAskSize());
				sb.append(comma);
				sb.append(trade._trdID);
				sb.append(comma);
				String mrktCentr =exchangePopulator.getEquityPlusExchangeCode(trade._mktCtr);
				if (mrktCentr != null)
					sb.append(mrktCentr);
				sb.append(comma);
				if (cachedBean.getBidMarketCenter() != null)
					sb.append(cachedBean.getBidMarketCenter());
				sb.append(comma);
				if(cachedBean.getAskMarketCenter() !=null)
					sb.append(cachedBean.getAskMarketCenter());
				sb.append(comma);
				sb.append(trade.RTL()+"");
				sb.append(newLine);
			}
			if(isSpecialTrade)
				cancelTradesFileWriter.add(sb.toString().getBytes());
			else
				tradesFileWriter.add(sb.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void OnUpdate( String StreamName, EQQuote quote )
	{
		if(quote._bid == 0 && quote._ask == 0 && quote._bidSize == 0 && quote._askSize == 0)
		{
			NTPLogger.dropSymbol(StreamName, "EQQuote quotes are Zero");
			return;
		}
		String ticker = quote.tkr();
		try {
			ticker = NTPTickerValidator.getRootSymbol(ticker);
			StringBuilder sb  = new StringBuilder();
			if(quote.protocol() == 14)	// for pinks
			{
				EquityQuotesBean tsqBean = TsqDbCache.getCachedBBOData(ticker);
				tsqBean.setTicker(ticker);
				tsqBean.setBidPrice(quote._bid);
				tsqBean.setAskPrice(quote._ask);
				tsqBean.setBidSize(quote._bidSize);
				tsqBean.setAskSize(quote._askSize);
				tsqBean.setAskMarketCenter("u");
				tsqBean.setBidMarketCenter("u");
				sb.append(getHashedTicker(ticker));
				sb.append(comma);
				sb.append(sequence());
				sb.append(comma);
				sb.append(TYPE_COMPOSITE_QUOTE);
				sb.append(comma);
				sb.append(EXCLUDE_IN_VWAP);
				sb.append(comma);
				sb.append("");
				sb.append(comma);
				NTPUtility.parseDateLong(sb, quote.MsgTime(), false);
				sb.append(comma);
				sb.append(TODAY);
				sb.append(comma);
				NTPUtility.appendTo4(sb, quote._bid);
				sb.append(comma);
				NTPUtility.appendTo4(sb, quote._ask);
				sb.append(comma);
				sb.append(quote._bidSize);
				sb.append(comma);
				sb.append(quote._askSize);
				sb.append(comma);
				sb.append("u");
				sb.append(comma);			
				sb.append("u");
				sb.append(comma);
				sb.append(quote.RTL());
				sb.append(newLine);
			}
			else
			{
				String marketCenter = exchangePopulator.getEquityPlusExchangeCode(quote._mktCtr);
				if(ticker.indexOf(TORONTO_TSX_SUFFIX) != -1 || ticker.indexOf(VENTURE_TSX_SUFFIX) != -1)
				{
					EquityQuotesBean tsqBean = TsqDbCache.getCachedBBOData(ticker);
					tsqBean.setTicker(ticker);
					tsqBean.setBidPrice(quote._bid);
					tsqBean.setAskPrice(quote._ask);
					tsqBean.setBidSize(quote._bidSize);
					tsqBean.setAskSize(quote._askSize);
					tsqBean.setAskMarketCenter(marketCenter);
					tsqBean.setBidMarketCenter(marketCenter);
				}
				sb.append(getHashedTicker(ticker));
				sb.append(comma);
				sb.append(sequence());
				sb.append(comma);
				sb.append(TYPE_REGIONAL_QUOTE);
				sb.append(comma);
				sb.append(EXCLUDE_IN_VWAP);
				sb.append(comma);
				sb.append(quote._qteCond);
				sb.append(comma);
				NTPUtility.parseDateLong(sb, quote.MsgTime(), false);
				sb.append(comma);			
				sb.append(TODAY);
				sb.append(comma);
				NTPUtility.appendTo4(sb, quote._bid);
				sb.append(comma);
				NTPUtility.appendTo4(sb, quote._ask);
				sb.append(comma);			
				sb.append(quote._bidSize);
				sb.append(comma);
				sb.append(quote._askSize);
				sb.append(comma);
				if(marketCenter !=null)
				{
					sb.append(marketCenter);
					sb.append(comma);
					sb.append(marketCenter);
				}
				else
					sb.append(comma);
				sb.append(comma);			
				sb.append(quote.RTL());
				sb.append(newLine);

			}
			quotesFileWriter.add(sb.toString().getBytes());			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void OnUpdate( String StreamName, EQBbo quote )
	{
		if(quote._bid == 0 && quote._ask == 0 && quote._bidSize == 0 && quote._askSize == 0)
		{
			NTPLogger.dropSymbol(StreamName, "EQBbo quotes are Zero");
			return;
		}
		try {
			String ticker = quote.tkr();
			ticker = NTPTickerValidator.getRootSymbol(ticker);
			EquityQuotesBean tsqBean = TsqDbCache.getCachedBBOData(ticker);
			tsqBean.setTicker(ticker);
			tsqBean.setBidPrice(quote._bid);
			tsqBean.setAskPrice(quote._ask);
			tsqBean.setBidSize(quote._bidSize);
			tsqBean.setAskSize(quote._askSize);
			tsqBean.setAskMarketCenter(exchangePopulator.getEquityPlusExchangeCode(quote._askMktCtr));
			tsqBean.setBidMarketCenter(exchangePopulator.getEquityPlusExchangeCode(quote._bidMktCtr));
			StringBuilder sb = new StringBuilder();
			sb.append(getHashedTicker(ticker));
			sb.append(comma);
			sb.append(sequence());
			sb.append(comma);
			sb.append(TYPE_COMPOSITE_QUOTE);
			sb.append(comma);
			sb.append(EXCLUDE_IN_VWAP);
			sb.append(comma);			
			sb.append(quote.LimitUpDown());
			sb.append(comma);
			NTPUtility.parseDateLong(sb, quote.MsgTime(), false);
			sb.append(comma);
			sb.append(TODAY);
			sb.append(comma);
			NTPUtility.appendTo4(sb, quote._bid);
			sb.append(comma);
			NTPUtility.appendTo4(sb, quote._ask);
			sb.append(comma);
			sb.append(quote._bidSize);
			sb.append(comma);
			sb.append(quote._askSize);
			sb.append(comma);
			sb.append(exchangePopulator.getEquityPlusExchangeCode(quote._bidMktCtr));
			sb.append(comma);
			sb.append(exchangePopulator.getEquityPlusExchangeCode(quote._askMktCtr));
			sb.append(comma);
			sb.append(quote.RTL());
			sb.append(newLine);
			quotesFileWriter.add(sb.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void OnUpdate( String StreamName, EQBboMM bbomm )
	{
		if(bbomm._bid == 0 && bbomm._ask == 0 && bbomm._bidSize == 0 && bbomm._askSize == 0)
		{
			NTPLogger.dropSymbol(StreamName, "EQBboMM quotes are Zero");
			return;
		}
		try {
			String ticker = bbomm.tkr();
			ticker = NTPTickerValidator.getRootSymbol(ticker);
			EquityQuotesBean tsqBean = TsqDbCache.getCachedBBOData(ticker);
			tsqBean.setTicker(ticker);
			tsqBean.setBidPrice(bbomm._bid);
			tsqBean.setAskPrice(bbomm._ask);
			tsqBean.setBidSize(bbomm._bidSize);
			tsqBean.setAskSize(bbomm._askSize);
			tsqBean.setAskMarketCenter("u");
			tsqBean.setBidMarketCenter("u");
			StringBuilder sb = new StringBuilder();
			sb.append(getHashedTicker(ticker));
			sb.append(comma);
			sb.append(sequence());
			sb.append(comma);
			sb.append(TYPE_COMPOSITE_QUOTE);
			sb.append(comma);
			sb.append(EXCLUDE_IN_VWAP);
			sb.append(comma);
			sb.append("");
			sb.append(comma);
			NTPUtility.parseDateLong(sb, bbomm.MsgTime(), false);
			sb.append(comma);
			sb.append(TODAY);
			sb.append(comma);
			NTPUtility.appendTo4(sb, bbomm._bid);
			sb.append(comma);
			NTPUtility.appendTo4(sb, bbomm._ask);
			sb.append(comma);
			sb.append(bbomm._bidSize);
			sb.append(comma);
			sb.append(bbomm._askSize);
			sb.append(comma);
			sb.append("u");
			sb.append(comma);			
			sb.append("u");
			sb.append(comma);
			sb.append(bbomm.RTL());
			sb.append(newLine);
			quotesFileWriter.add(sb.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void OnBlobTable( String qry, BlobTable blobTable )
	{
		try
		{
			if(blobTable != null)
			{
				int rowCount = blobTable.nRow();
				NTPLogger.info("NEWISSUE OnBlobTable rowCount is = " + rowCount);
				/*for (int count = 0; count < rowCount; count++)
				{
					for(int i =0 ; i < blobTable.nCol(); i++){
						System.out.print(blobTable.GetCell(count, i));
					}
					System.out.println();
				}*/
				for (int count = 0; count < rowCount; count++)
				{
					String ticker = blobTable.GetCell(count, TICKER_POS);
					String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
					NTPLogger.newIssue(ticker, instrument);
					if(ticker == null || ticker.length() == 0 || instrument == null || instrument.length() == 0 ){
						return;
					}
					if(ticker.charAt(0) < fromLimitChar || ticker.charAt(0) > toLimitChar){
						NTPLogger.info("NEWISSUE OnBlobTable symbol not in range " + fromLimitChar + " " + toLimitChar);
						continue;
					}
					//check if ticker is equity or not
					if(! instrument.equals("CTA_A") && !instrument.equals("CTA_B") && !instrument.equals("UTP") && !instrument.equals("OTC"))
					{
						NTPLogger.info("NEWISSUE OnBlobTable not a equity symbol instrument = " + instrument);
						continue;
					}
					if(ticker.contains("/"))
					{
						if(NTPTickerValidator.isCanadianStock(ticker))
						{
							subscribeNewIssue(ticker);
							continue;
						}
						if(ticker.endsWith("/BB"))
						{
							NTPLogger.info("NEWISSUE Not subscribing this ticker " + ticker);
							continue;
						}
						// handling for Pink sheet tickers [BEGIN]
						String splittedTicker [] = ticker.split("/"); 
						if(splittedTicker.length == 2)
						{
							String exchange = splittedTicker[1];
							if(exchange.length() != 4)
								subscribeNewIssue(ticker);
							else
							{
								NTPLogger.info("NEWISSUE TSQFeedChannel.OnBlobTable not a equity symbol " + ticker);
								continue;
							}
						}
						// handling for Pink sheet tickers [END]
					}
					else
						subscribeNewIssue(ticker);
				}
			}
		}
		catch(Exception e)
		{
			NTPLogger.warning("NEWISSUE OnBlobTable exception is : " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void subscribeNewIssue(String ticker)
	{
		if(tickersList.contains(ticker))
			NTPLogger.warning("NEWISSUE OnBlobTable ticker is already in ticker list");
		else
			subscribe(ticker);
	}

	private String getHashCode(String ticker)
	{
		char[] arr = ticker.toCharArray();
		StringBuffer buff = new StringBuffer();
		if(ticker.endsWith(TORONTO_TSX_SUFFIX))
		{
			int size = arr.length -2;
			for(int i=0;i<size;i++)
			{
				buff.append( ((int)arr[i]) %100 );
			}
			buff.append(32);	//using space ascii value to control overflow
		}
		else if(ticker.endsWith(VENTURE_TSX_SUFFIX))
		{
			int size = arr.length -2;
			for(int i=0;i<size;i++)
			{
				buff.append( ((int)arr[i]) %100 );
			}
			buff.append(33);	//using ! ascii value to control overflow
		}
		else
		{
			for(int i=0;i<arr.length;i++)
			{
				buff.append( ((int)arr[i]) %100 );
			}
		}
		NTPLogger.info("HASHCODE " + ticker + " " + buff.toString());
		addHashedTickerInDB(ticker, buff.toString());
		return buff.toString();
	}

	public synchronized static long sequence() {
		return messageSequence++;
	}

	private boolean doesConditionCodeExcludeFromVWAP(String condCode) 
	{
		if (condCode == null) 
			return false;
		if (VWAP_EXCLUSIONS.contains(condCode))
			return true;
		return false;
	}

	private String getHashedTicker(String ticker){
		String hashedTick = hashedTickers.get(ticker);
		if (hashedTick == null) 
		{
			hashedTick = getHashCode(ticker.toUpperCase().trim());
			hashedTickers.put(ticker,hashedTick);
		}	
		return hashedTick;
	}

	private void addHashedTickerInDB(String ticker, String hashcode)
	{
		try {
			Connection conn = MySQLDBManager.getConnection();
			String query = "INSERT INTO TSQ_SYMBOLS (TICKER, SYMBOL) values (?, ?)"; 
			PreparedStatement preSmt= conn.prepareStatement(query);
			preSmt.setString(1, hashcode);
			preSmt.setString(2, ticker);
			preSmt.executeUpdate();
			preSmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
