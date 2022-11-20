package ntp.tsqdb.writer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.TsqDbSummaryBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.ExchangeMapPopulator;
import ntp.util.MySQLDBManager;
import ntp.util.NTPTickerValidator;
import ntp.util.NTPUtility;
import ntp.util.StockRetriever;

import com.csvreader.CsvWriter;

public class TsqDbSubsManager {
	
	private static TsqDbChannel[] channelArray = null;
	private int numberOfChannels = 1;
	private HashSet<String> exchangeList;
	private TsqDbFileWriter tradesFileWriter = null;
	private TsqDbFileWriter quotesFileWriter = null;
	private TsqDbFileWriter cancelTradesFileWriter = null;
	private int thresholdRecords = 10000;
	private int thresholdProcessingTime = 3000;
	private String dataFileDir;
	private SimpleDateFormat sd =  new SimpleDateFormat("yyyyMMddHHmm");
	private Thread tradeFileThread;
	private Thread quoteFileThread;
	private Thread cancelTradeFileThread;
	
	private Connection con = null;

	private static TsqDbSubsManager dbSubsManager;

	public static TsqDbSubsManager getInstance(){
		if(dbSubsManager == null)
			dbSubsManager = new TsqDbSubsManager();
		return dbSubsManager;
	}

	private TsqDbSubsManager() {		
		exchangeList = ExchangeMapPopulator.getInstance().getExchanges();
		exchangeList.remove("TO");
		exchangeList.remove("TV");
		exchangeList.remove("D1");
		ExchangeMapPopulator.getInstance().updateExchangeMap();
		dataFileDir = CPDProperty.getInstance().getProperty("DATA_FILE_DIR");
		if(dataFileDir == null)
		{
			NTPLogger.missingProperty("DATA_FILE_DIR");
			dataFileDir = "/home/datafeed/datafiles";
			NTPLogger.defaultSetting("DATA_FILE_DIR", dataFileDir);
		}
		File dir = new File(dataFileDir);
		if(!dir.exists())
			dir.mkdirs();
		try{ thresholdRecords = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD_RECORDS")); }
		catch(Exception e){
			NTPLogger.missingProperty("THRESHOLD_RECORDS");
			thresholdRecords = 25000;
			NTPLogger.defaultSetting("THRESHOLD_RECORDS", "" + thresholdRecords);
		}
		try{ thresholdProcessingTime = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD_PROCESSING_TIME")); }
		catch(Exception e){
			NTPLogger.missingProperty("THRESHOLD_PROCESSING_TIME");
			thresholdProcessingTime = 500;
			NTPLogger.defaultSetting("THRESHOLD_PROCESSING_TIME", "" + thresholdProcessingTime);
		}
		try{ numberOfChannels = Integer.parseInt(CPDProperty.getInstance().getProperty("NO_OF_CHANNELS")); }
		catch(Exception e){
			NTPLogger.missingProperty("NO_OF_CHANNELS");
			numberOfChannels = 4;
			NTPLogger.defaultSetting("NO_OF_CHANNELS", "" + numberOfChannels);
		}
		tradesFileWriter = new TsqDbFileWriter(dataFileDir, thresholdRecords, thresholdProcessingTime, 0);
		quotesFileWriter = new TsqDbFileWriter(dataFileDir, thresholdRecords, thresholdProcessingTime, 1);
		cancelTradesFileWriter = new TsqDbFileWriter(dataFileDir, thresholdRecords, thresholdProcessingTime, 2);
		tradeFileThread = new Thread(tradesFileWriter,"TradesWriterThread");
		tradeFileThread.start();
		quoteFileThread = new Thread(quotesFileWriter,"QuotesWriterThread");
		quoteFileThread.start();
		cancelTradeFileThread = new Thread(cancelTradesFileWriter, "CancelTradesFileWriter");
		cancelTradeFileThread.start();
		channelArray = new TsqDbChannel[numberOfChannels + 1];
		for (int i = 0; i <= numberOfChannels; i++) {
			TsqDbChannel channel = new TsqDbChannel("TSQDB_" + i ,tradesFileWriter, quotesFileWriter, cancelTradesFileWriter);
			channelArray[i] = channel;			
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				NTPLogger.info("Start of shutDownHook");
				stopProcess();
				NTPLogger.info("Finish of shutDownHook");
			}
		});
		try {
			con = MySQLDBManager.getConnection();
			PreparedStatement preSmt= con.prepareStatement("DELETE from TSQ_SYMBOLS");
			preSmt.executeUpdate();
			preSmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscribeTSQSymbol(TsqDbChannel channel,String ticker)
	{
		if(NTPTickerValidator.isCanadianStock(ticker))
		{
			channel.subscribe(ticker);
			return;
		}
		if(ticker.indexOf("/") > 0)
		{
			NTPLogger.dropSymbol(ticker, "Subscription for root ticker only");
			return;
		}
		channel.subscribe(ticker);
		for (String  exchange : exchangeList)
			channel.subscribe(ticker + "/" + exchange);
	}

	public void subscribeAll(){
		int count = 0;
		int channelNumber = 0;
		int threshold = 200;
		int sleepTime = 500;
		char toLimitChar = 'C';
		char fromLimitChar = 'A';
		try 
		{
			threshold = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD"));
			sleepTime = Integer.parseInt(CPDProperty.getInstance().getProperty("SLEEP_TIME"));
			toLimitChar = CPDProperty.getInstance().getProperty("TICKER_TO_LIMIT").trim().charAt(0);
			fromLimitChar = CPDProperty.getInstance().getProperty("TICKER_FROM_LIMIT").trim().charAt(0);
		} catch (Exception e) {
			NTPLogger.missingProperty("THRESHOLD");
			NTPLogger.missingProperty("SLEEP_TIME");
			NTPLogger.missingProperty("TICKER_TO_LIMIT");
			NTPLogger.missingProperty("TICKER_FROM_LIMIT");
			threshold = 200;
			sleepTime = 500;
			toLimitChar = 'C';
			fromLimitChar = 'A';
			NTPLogger.defaultSetting("THRESHOLD", "" + threshold);
			NTPLogger.defaultSetting("SLEEP_TIME", "" + sleepTime);
			NTPLogger.defaultSetting("TICKER_TO_LIMIT", "" + toLimitChar);
			NTPLogger.defaultSetting("TICKER_FROM_LIMIT", "" + fromLimitChar);
		}
		HashSet<String> tickerList = StockRetriever.getInstance().getTickerList();
		int tickersPerChannel;
		tickersPerChannel = tickerList.size()/(numberOfChannels);
		NTPLogger.info("Total number of tickers: " + tickerList.size());
		NTPLogger.info("Tickers per channel: " + tickersPerChannel);
		for (String ticker : tickerList) 
		{
			if(channelNumber == numberOfChannels){
				channelNumber = 0;
			}
			if(ticker.charAt(0) < fromLimitChar || ticker.charAt(0) > toLimitChar)
				continue;
			count ++;
			// Just to Slow down the subscription. UC issues
			if (count % threshold == 0){ 
				try {
					Thread.sleep(sleepTime);	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			subscribeTSQSymbol(channelArray[channelNumber], ticker);
			channelNumber++;
		}
		NTPLogger.info("Completed Subsscriptions. Count " + count);
		channelArray[numberOfChannels].subscribe("ULTRACACHE_NEW-ISSUES");
	}

	private void stopProcess(){

		NTPLogger.info("In Stop method");
		tradesFileWriter.stopThread();
		quotesFileWriter.stopThread();
		cancelTradesFileWriter.stopThread();
		try {
			tradeFileThread.join();
			quoteFileThread.join();
			cancelTradeFileThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		NTPLogger.info("Stopped all threads.");
		NTPLogger.info("Going to write summary messages");
		writeSummaryMessages();
		NTPLogger.info("Completed writing summary messages");
		for (TsqDbChannel channel : channelArray) {
			channel.Stop();
		}
		NTPLogger.info("Stopped all channels.");
		MySQLDBManager.closeConnection();
	}

	private void writeSummaryMessages() {
		String currentDate = sd.format(new Date());
		String file = dataFileDir+"summaryMessages"+currentDate+".csv";
		CsvWriter writer = new CsvWriter(file);
		System.out.println("file path is : "+file);
		try {
			writer.write("Symbol");
			writer.write("ClosePrice");
			writer.write("DayHigh");
			writer.write("DayLow");
			writer.write("OpenPrice");
			writer.write("TradeId");
			writer.write("Volume");
			writer.write("Vwap");
			writer.endRecord();
			ConcurrentHashMap<String, TsqDbSummaryBean> hashMap = TsqDbCache.getSummaryMessagesMap();
			for (String ticker : hashMap.keySet()) {
				TsqDbSummaryBean bean = hashMap.get(ticker);
				writer.write(bean.getTicker());
				writer.write(NTPUtility.appendTo6(bean.getClosePrice()));
				writer.write(NTPUtility.appendTo6(bean.getDayHigh()));
				writer.write(NTPUtility.appendTo6(bean.getDayLow()));
				writer.write(NTPUtility.appendTo6(bean.getOpenPrice()));
				writer.write(""+bean.getTradeId());
				writer.write(""+bean.getVolume());
				writer.write(NTPUtility.appendTo6(bean.getVwap()));
				writer.endRecord();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer.close();
	}	
}
