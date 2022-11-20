package ntp.uf3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

import com.csvreader.CsvWriter;

public class ReadRecorderFile {

	private static long bufferSize = 1024*1024; //1MB
	private static String destinationDir = "";
	private static String rawFileDir = "";
	private static String tradeDate = "";
	private static String fileType = "";
	private static HashSet<String> processingFlags = new HashSet<>();

	private RandomAccessFile file;
	private FileChannel channel;
	private MappedByteBuffer mappedBuffer = null;
	private long filseSize = 0;
	private boolean doRun = true;
	private long previousOffset = 0;
	private SimpleDateFormat timeformatter = new SimpleDateFormat("HH:mm:ss:SSS");
	private SimpleDateFormat timeformat = new SimpleDateFormat("HHmmss");


	// Maps for data monitoring
	private HashMap<String, Integer> channelTimeMap = new HashMap<>();
	private HashMap<Short, String> marketCenterLocateMap = new HashMap<>();
	private HashMap<Short, String> marketMakerLocateMap = new HashMap<>();
	private HashMap<Integer, String> symbolLocateMap = new HashMap<>();
	private LinkedHashSet<Byte> metaType = new LinkedHashSet<>();
	private HashMap<Integer, HashMap<Byte, String>> metaValueMap = new HashMap<>();
	private Short nasdaqMCLocate;

	//File Writers
	private CsvWriter combinationLegWriter =null;
	private CsvWriter channelEventWriter = null;
	private CsvWriter marketMakerStatusWriter = null;
	private CsvWriter adminTextWriter = null;
	private CsvWriter imbalanceInfoWriter = null;
	private CsvWriter instrumentStatusWriter = null;
	private CsvWriter priceRangeIndicatorWriter = null;
	private CsvWriter valueUpdateWriter = null;
	private CsvWriter priorDayCancelWriter = null;
	private CsvWriter priorDayTradeReportWriter = null;
	private CsvWriter tradeCorrectionWriter = null;
	private CsvWriter tradeCancelWriter = null;
	private CsvWriter tradeExtendedWriter = null;
	private CsvWriter tradeLongWriter = null;
	private CsvWriter tradeShortWriter = null;
	private CsvWriter bbo2SidedShortWriter = null;
	private CsvWriter bbo2SidedLongWriter = null;
	private CsvWriter bbo2SidedExtendedWriter = null;
	private CsvWriter bbo1SidedShortWriter = null;
	private CsvWriter bbo1SidedLongWriter = null;
	private CsvWriter quote1SidedShortWriter = null;
	private CsvWriter quote1SidedLongWriter = null;
	private CsvWriter quote1SidedExtendedWriter = null;
	private CsvWriter quote2SidedExtendedWriter = null;
	private CsvWriter quote2SidedLongWriter = null;
	private CsvWriter quote2SidedShortWriter = null;
	private CsvWriter optionDeliveryWriter = null;

	public ReadRecorderFile(String filename)
	{
		try {
			file = new RandomAccessFile(filename, "r");
			channel =file.getChannel();
			filseSize = channel.size();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public void startProcessing()
	{
		try
		{
			TreeMap<Byte, Long> byteMap = new TreeMap<>();
			loadBuffer(0);
			if(processingFlags.contains("50"))
			{
				quote2SidedShortWriter = new CsvWriter(destinationDir+"/quote2SidedShort.csv");
				quote2SidedShortWriter.write("TimestampNano");
				quote2SidedShortWriter.write("Channel");
				quote2SidedShortWriter.write("Protocol");
				quote2SidedShortWriter.write("SequenceNumber");
				quote2SidedShortWriter.write("HeaderFlag");
				quote2SidedShortWriter.write("Symbol");
				quote2SidedShortWriter.write("MarketCenter");
				quote2SidedShortWriter.write("MarketMaker");
				quote2SidedShortWriter.write("BidPrice");
				quote2SidedShortWriter.write("BidSize");
				quote2SidedShortWriter.write("AskPrice");
				quote2SidedShortWriter.write("AskSize");
				quote2SidedShortWriter.write("Condition");
				quote2SidedShortWriter.write("QuoteFlags");
				quote2SidedShortWriter.write("BidFlags");
				quote2SidedShortWriter.write("AskFlags");
				quote2SidedShortWriter.write("MetaIfAny");
				quote2SidedShortWriter.endRecord();
			}
			if(processingFlags.contains("51"))
			{
				quote2SidedLongWriter = new CsvWriter(destinationDir+"/quote2SidedLong.csv");
				quote2SidedLongWriter.write("TimestampNano");
				quote2SidedLongWriter.write("Channel");
				quote2SidedLongWriter.write("Protocol");
				quote2SidedLongWriter.write("SequenceNumber");
				quote2SidedLongWriter.write("HeaderFlag");
				quote2SidedLongWriter.write("Symbol");
				quote2SidedLongWriter.write("MarketCenter");
				quote2SidedLongWriter.write("MarketMaker");
				quote2SidedLongWriter.write("BidPrice");
				quote2SidedLongWriter.write("BidSize");
				quote2SidedLongWriter.write("AskPrice");
				quote2SidedLongWriter.write("AskSize");
				quote2SidedLongWriter.write("Condition");
				quote2SidedLongWriter.write("QuoteFlags");
				quote2SidedLongWriter.write("BidFlags");
				quote2SidedLongWriter.write("AskFlags");
				quote2SidedLongWriter.write("MetaIfAny");
				quote2SidedLongWriter.endRecord();
			}
			if(processingFlags.contains("52"))
			{
				quote2SidedExtendedWriter = new CsvWriter(destinationDir+"/quote2SidedExtended.csv");
				quote2SidedExtendedWriter.write("TimestampNano");
				quote2SidedExtendedWriter.write("Channel");
				quote2SidedExtendedWriter.write("Protocol");
				quote2SidedExtendedWriter.write("SequenceNumber");
				quote2SidedExtendedWriter.write("HeaderFlag");
				quote2SidedExtendedWriter.write("Symbol");
				quote2SidedExtendedWriter.write("MarketCenter");
				quote2SidedExtendedWriter.write("MarketMaker");
				quote2SidedExtendedWriter.write("BidPrice");
				quote2SidedExtendedWriter.write("BidSize");
				quote2SidedExtendedWriter.write("AskPrice");
				quote2SidedExtendedWriter.write("AskSize");
				quote2SidedExtendedWriter.write("Condition");
				quote2SidedExtendedWriter.write("QuoteFlags");
				quote2SidedExtendedWriter.write("BidFlags");
				quote2SidedExtendedWriter.write("AskFlags");
				quote2SidedExtendedWriter.write("MetaIfAny");
				quote2SidedExtendedWriter.endRecord();
			}
			if(processingFlags.contains("53"))
			{
				quote1SidedShortWriter = new CsvWriter(destinationDir+"/quote1SidedShort.csv");
				quote1SidedShortWriter.write("TimestampNano");
				quote1SidedShortWriter.write("Channel");
				quote1SidedShortWriter.write("Protocol");
				quote1SidedShortWriter.write("SequenceNumber");
				quote1SidedShortWriter.write("HeaderFlag");
				quote1SidedShortWriter.write("Symbol");
				quote1SidedShortWriter.write("MarketCenter");
				quote1SidedShortWriter.write("MarketMaker");
				quote1SidedShortWriter.write("Price");
				quote1SidedShortWriter.write("Size");
				quote1SidedShortWriter.write("Side");
				quote1SidedShortWriter.write("Condition");
				quote1SidedShortWriter.write("QuoteFlags");
				quote1SidedShortWriter.write("PriceFlags");
				quote1SidedShortWriter.write("MetaIfAny");
				quote1SidedShortWriter.endRecord();
			}
			if(processingFlags.contains("54"))
			{
				quote1SidedLongWriter = new CsvWriter(destinationDir+"/quote1SidedLong.csv");
				quote1SidedLongWriter.write("TimestampNano");
				quote1SidedLongWriter.write("Channel");
				quote1SidedLongWriter.write("Protocol");
				quote1SidedLongWriter.write("SequenceNumber");
				quote1SidedLongWriter.write("HeaderFlag");
				quote1SidedLongWriter.write("Symbol");
				quote1SidedLongWriter.write("MarketCenter");
				quote1SidedLongWriter.write("MarketMaker");
				quote1SidedLongWriter.write("Price");
				quote1SidedLongWriter.write("Size");
				quote1SidedLongWriter.write("Side");
				quote1SidedLongWriter.write("Condition");
				quote1SidedLongWriter.write("QuoteFlags");
				quote1SidedLongWriter.write("PriceFlags");
				quote1SidedLongWriter.write("MetaIfAny");
				quote1SidedLongWriter.endRecord();
			}
			if(processingFlags.contains("55"))
			{
				quote1SidedExtendedWriter = new CsvWriter(destinationDir+"/quote1SidedExtended.csv");
				quote1SidedExtendedWriter.write("TimestampNano");
				quote1SidedExtendedWriter.write("Channel");
				quote1SidedExtendedWriter.write("Protocol");
				quote1SidedExtendedWriter.write("SequenceNumber");
				quote1SidedExtendedWriter.write("HeaderFlag");
				quote1SidedExtendedWriter.write("Symbol");
				quote1SidedExtendedWriter.write("MarketCenter");
				quote1SidedExtendedWriter.write("MarketMaker");
				quote1SidedExtendedWriter.write("Price");
				quote1SidedExtendedWriter.write("Size");
				quote1SidedExtendedWriter.write("Side");
				quote1SidedExtendedWriter.write("Condition");
				quote1SidedExtendedWriter.write("QuoteFlags");
				quote1SidedExtendedWriter.write("PriceFlags");
				quote1SidedExtendedWriter.write("MetaIfAny");
				quote1SidedExtendedWriter.endRecord();
			}
			if(processingFlags.contains("60"))
			{
				bbo2SidedShortWriter = new CsvWriter(destinationDir+"/bbo2SidedShort.csv");
				bbo2SidedShortWriter.write("TimestampNano");
				bbo2SidedShortWriter.write("Channel");
				bbo2SidedShortWriter.write("Protocol");
				bbo2SidedShortWriter.write("SequenceNumber");
				bbo2SidedShortWriter.write("HeaderFlag");
				bbo2SidedShortWriter.write("Symbol");
				bbo2SidedShortWriter.write("BidMarketCenter");
				bbo2SidedShortWriter.write("BidPrice");
				bbo2SidedShortWriter.write("BidSize");
				bbo2SidedShortWriter.write("AskMarketCenter");
				bbo2SidedShortWriter.write("AskPrice");
				bbo2SidedShortWriter.write("AskSize");
				bbo2SidedShortWriter.write("Condition");
				bbo2SidedShortWriter.write("Flags");
				bbo2SidedShortWriter.write("MetaIfAny");
				bbo2SidedShortWriter.endRecord();
			}
			if(processingFlags.contains("61"))
			{
				bbo2SidedLongWriter = new CsvWriter(destinationDir+"/bbo2SidedLong.csv");
				bbo2SidedLongWriter.write("TimestampNano");
				bbo2SidedLongWriter.write("Channel");
				bbo2SidedLongWriter.write("Protocol");
				bbo2SidedLongWriter.write("SequenceNumber");
				bbo2SidedLongWriter.write("HeaderFlag");
				bbo2SidedLongWriter.write("Symbol");
				bbo2SidedLongWriter.write("BidMarketCenter");
				bbo2SidedLongWriter.write("BidPrice");
				bbo2SidedLongWriter.write("BidSize");
				bbo2SidedLongWriter.write("AskMarketCenter");
				bbo2SidedLongWriter.write("AskPrice");
				bbo2SidedLongWriter.write("AskSize");
				bbo2SidedLongWriter.write("Condition");
				bbo2SidedLongWriter.write("Flags");
				bbo2SidedLongWriter.write("MetaIfAny");
				bbo2SidedLongWriter.endRecord();
			}
			if(processingFlags.contains("62"))
			{
				bbo2SidedExtendedWriter = new CsvWriter(destinationDir+"/bbo2SidedExtended.csv");
				bbo2SidedExtendedWriter.write("TimestampNano");
				bbo2SidedExtendedWriter.write("Channel");
				bbo2SidedExtendedWriter.write("Protocol");
				bbo2SidedExtendedWriter.write("SequenceNumber");
				bbo2SidedExtendedWriter.write("HeaderFlag");
				bbo2SidedExtendedWriter.write("Symbol");
				bbo2SidedExtendedWriter.write("BidMarketCenter");
				bbo2SidedExtendedWriter.write("BidPrice");
				bbo2SidedExtendedWriter.write("BidSize");
				bbo2SidedExtendedWriter.write("AskMarketCenter");
				bbo2SidedExtendedWriter.write("AskPrice");
				bbo2SidedExtendedWriter.write("AskSize");
				bbo2SidedExtendedWriter.write("Condition");
				bbo2SidedExtendedWriter.write("Flags");
				bbo2SidedExtendedWriter.write("MetaIfAny");
				bbo2SidedExtendedWriter.endRecord();
			}
			if(processingFlags.contains("63"))
			{
				bbo1SidedShortWriter = new CsvWriter(destinationDir+"/bbo1SidedShort.csv");
				bbo1SidedShortWriter.write("TimestampNano");
				bbo1SidedShortWriter.write("Channel");
				bbo1SidedShortWriter.write("Protocol");
				bbo1SidedShortWriter.write("SequenceNumber");
				bbo1SidedShortWriter.write("HeaderFlag");
				bbo1SidedShortWriter.write("Symbol");
				bbo1SidedShortWriter.write("MarketCenter");
				bbo1SidedShortWriter.write("Price");
				bbo1SidedShortWriter.write("Size");
				bbo1SidedShortWriter.write("Side");
				bbo1SidedShortWriter.write("Condition");
				bbo1SidedShortWriter.write("Flags");
				bbo1SidedShortWriter.write("MetaIfAny");
				bbo1SidedShortWriter.endRecord();
			}
			if(processingFlags.contains("64"))
			{
				bbo1SidedLongWriter = new CsvWriter(destinationDir+"/bbo1SidedLong.csv");
				bbo1SidedLongWriter.write("TimestampNano");
				bbo1SidedLongWriter.write("Channel");
				bbo1SidedLongWriter.write("Protocol");
				bbo1SidedLongWriter.write("SequenceNumber");
				bbo1SidedLongWriter.write("HeaderFlag");
				bbo1SidedLongWriter.write("Symbol");
				bbo1SidedLongWriter.write("MarketCenter");
				bbo1SidedLongWriter.write("Price");
				bbo1SidedLongWriter.write("Size");
				bbo1SidedLongWriter.write("Side");
				bbo1SidedLongWriter.write("Condition");
				bbo1SidedLongWriter.write("Flags");
				bbo1SidedLongWriter.write("MetaIfAny");
				bbo1SidedLongWriter.endRecord();
			}
			if(processingFlags.contains("70"))
			{
				tradeShortWriter = new CsvWriter(destinationDir+"/tradeShort.csv");
				tradeShortWriter.write("TimestampNano");
				tradeShortWriter.write("Channel");
				tradeShortWriter.write("Protocol");
				tradeShortWriter.write("SequenceNumber");
				tradeShortWriter.write("HeaderFlag");
				tradeShortWriter.write("Symbol");
				tradeShortWriter.write("MarketCenter");
				tradeShortWriter.write("TradeId");
				tradeShortWriter.write("Price");
				tradeShortWriter.write("Size");
				tradeShortWriter.write("PriceFlags");
				tradeShortWriter.write("EligibilityFlags");
				tradeShortWriter.write("ReportFlags");
				tradeShortWriter.write("ChangeFlags");
				tradeShortWriter.write("MetaIfAny");
				tradeShortWriter.endRecord();
			}			
			if(processingFlags.contains("71"))
			{
				tradeLongWriter = new CsvWriter(destinationDir+"/tradeLong.csv");
				tradeLongWriter.write("TimestampNano");
				tradeLongWriter.write("Channel");
				tradeLongWriter.write("Protocol");
				tradeLongWriter.write("SequenceNumber");
				tradeLongWriter.write("HeaderFlag");
				tradeLongWriter.write("Symbol");
				tradeLongWriter.write("MarketCenter");
				tradeLongWriter.write("TradeId");
				tradeLongWriter.write("Price");
				tradeLongWriter.write("Size");
				tradeLongWriter.write("PriceFlags");
				tradeLongWriter.write("EligibilityFlags");
				tradeLongWriter.write("ReportFlags");
				tradeLongWriter.write("ChangeFlags");
				tradeLongWriter.write("MetaIfAny");
				tradeLongWriter.endRecord();
			}			
			if(processingFlags.contains("72"))
			{
				tradeExtendedWriter = new CsvWriter(destinationDir+"/tradeExtended.csv");
				tradeExtendedWriter.write("TimestampNano");
				tradeExtendedWriter.write("Channel");
				tradeExtendedWriter.write("Protocol");
				tradeExtendedWriter.write("SequenceNumber");
				tradeExtendedWriter.write("HeaderFlag");
				tradeExtendedWriter.write("Symbol");
				tradeExtendedWriter.write("MarketCenter");
				tradeExtendedWriter.write("TradeId");
				tradeExtendedWriter.write("Price");
				tradeExtendedWriter.write("Size");
				tradeExtendedWriter.write("PriceFlags");
				tradeExtendedWriter.write("EligibilityFlags");
				tradeExtendedWriter.write("ReportFlags");
				tradeExtendedWriter.write("ChangeFlags");
				tradeExtendedWriter.write("MetaIfAny");
				tradeExtendedWriter.endRecord();
			}

			if(processingFlags.contains("73"))
			{
				tradeCancelWriter = new CsvWriter(destinationDir+"/tradeCancel.csv");
				tradeCancelWriter.write("TimestampNano");
				tradeCancelWriter.write("Channel");
				tradeCancelWriter.write("Protocol");
				tradeCancelWriter.write("SequenceNumber");
				tradeCancelWriter.write("HeaderFlag");
				tradeCancelWriter.write("Symbol");
				tradeCancelWriter.write("MarketCenter");
				tradeCancelWriter.write("TradeId");
				tradeCancelWriter.write("Price");
				tradeCancelWriter.write("Size");
				tradeCancelWriter.write("PriceFlags");
				tradeCancelWriter.write("EligibilityFlags");
				tradeCancelWriter.write("ReportFlags");
				tradeCancelWriter.write("CancelFlags");
				tradeCancelWriter.write("MetaIfAny");
				tradeCancelWriter.endRecord();
			}
			if(processingFlags.contains("74"))
			{
				tradeCorrectionWriter = new CsvWriter(destinationDir+"/tradeCorrection.csv");
				tradeCorrectionWriter.write("TimestampNano");
				tradeCorrectionWriter.write("Channel");
				tradeCorrectionWriter.write("Protocol");
				tradeCorrectionWriter.write("SequenceNumber");
				tradeCorrectionWriter.write("HeaderFlag");
				tradeCorrectionWriter.write("Symbol");
				tradeCorrectionWriter.write("MarketCenter");
				tradeCorrectionWriter.write("OriginalTradeId");
				tradeCorrectionWriter.write("OriginalPrice");
				tradeCorrectionWriter.write("OriginalSize");
				tradeCorrectionWriter.write("OriginalPriceFlags");
				tradeCorrectionWriter.write("OriginalEligibilityFlags");
				tradeCorrectionWriter.write("OriginalReportFlags");
				tradeCorrectionWriter.write("CorrectedTradeId");
				tradeCorrectionWriter.write("CorrectedPrice");
				tradeCorrectionWriter.write("CorrectedSize");
				tradeCorrectionWriter.write("CorrectedPriceFlags");
				tradeCorrectionWriter.write("CorrectedEligibilityFlags");
				tradeCorrectionWriter.write("CorrectedReportFlags");
				tradeCorrectionWriter.write("CorrectionFlags");
				tradeCorrectionWriter.write("MetaIfAny");
				tradeCorrectionWriter.endRecord();
			}
			if(processingFlags.contains("75"))
			{
				priorDayTradeReportWriter = new CsvWriter(destinationDir+"/priorDayTradeReport.csv");
				priorDayTradeReportWriter.write("TimestampNano");
				priorDayTradeReportWriter.write("Channel");
				priorDayTradeReportWriter.write("Protocol");
				priorDayTradeReportWriter.write("SequenceNumber");
				priorDayTradeReportWriter.write("HeaderFlag");
				priorDayTradeReportWriter.write("Symbol");
				priorDayTradeReportWriter.write("MarketCenter");
				priorDayTradeReportWriter.write("TradeId");
				priorDayTradeReportWriter.write("Price");
				priorDayTradeReportWriter.write("Size");
				priorDayTradeReportWriter.write("PriceFlags");
				priorDayTradeReportWriter.write("EligibilityFlags");
				priorDayTradeReportWriter.write("ReportFlags");
				priorDayTradeReportWriter.write("Date");
				priorDayTradeReportWriter.write("Time");
				priorDayTradeReportWriter.write("MetaIfAny");
				priorDayTradeReportWriter.endRecord();
			}
			if(processingFlags.contains("76"))
			{
				priorDayCancelWriter = new CsvWriter(destinationDir+"/priorDayCancel.csv");
				priorDayCancelWriter.write("TimestampNano");
				priorDayCancelWriter.write("Channel");
				priorDayCancelWriter.write("Protocol");
				priorDayCancelWriter.write("SequenceNumber");
				priorDayCancelWriter.write("HeaderFlag");
				priorDayCancelWriter.write("Symbol");
				priorDayCancelWriter.write("MarketCenter");
				priorDayCancelWriter.write("TradeId");
				priorDayCancelWriter.write("Price");
				priorDayCancelWriter.write("Size");
				priorDayCancelWriter.write("PriceFlags");
				priorDayCancelWriter.write("EligibilityFlags");
				priorDayCancelWriter.write("ReportFlags");
				priorDayCancelWriter.write("OriginalDate");
				priorDayCancelWriter.write("OriginalTime");
				priorDayCancelWriter.write("CancelFlags");
				priorDayCancelWriter.write("MetaIfAny");
				priorDayCancelWriter.endRecord();
			}
			if(processingFlags.contains("80"))
			{
				valueUpdateWriter = new CsvWriter(destinationDir+"/valueUpdate.csv");
				valueUpdateWriter.write("TimestampNano");
				valueUpdateWriter.write("Channel");
				valueUpdateWriter.write("Protocol");
				valueUpdateWriter.write("SequenceNumber");
				valueUpdateWriter.write("HeaderFlag");
				valueUpdateWriter.write("Symbol");
				valueUpdateWriter.write("MarketCenter");
				valueUpdateWriter.write("Flags");
				valueUpdateWriter.write("MetaIfAny");
				valueUpdateWriter.endRecord();
			}
			if(processingFlags.contains("b0"))
			{
				channelEventWriter = new CsvWriter(destinationDir+"/channelEvents.csv");
				channelEventWriter.write("TimestampNano");
				channelEventWriter.write("Channel");
				channelEventWriter.write("Protocol");
				channelEventWriter.write("SequenceNumber");
				channelEventWriter.write("HeaderFlag");
				channelEventWriter.write("EventCode");
				channelEventWriter.write("MarketCenter");
				channelEventWriter.write("MetaIfAny");
				channelEventWriter.endRecord();
			}
			if(processingFlags.contains("b1"))
			{
				marketMakerStatusWriter = new CsvWriter(destinationDir+"/marketMakerStatus.csv");
				marketMakerStatusWriter.write("TimestampNano");
				marketMakerStatusWriter.write("Channel");
				marketMakerStatusWriter.write("Protocol");
				marketMakerStatusWriter.write("SequenceNumber");
				marketMakerStatusWriter.write("HeaderFlag");
				marketMakerStatusWriter.write("Symbol");
				marketMakerStatusWriter.write("MarketCenter");
				marketMakerStatusWriter.write("MarketMaker");
				marketMakerStatusWriter.write("MetaIfAny");
				marketMakerStatusWriter.endRecord();
			}
			if(processingFlags.contains("b2"))
			{
				adminTextWriter = new CsvWriter(destinationDir+"/adminText.csv");
				adminTextWriter.write("TimestampNano");
				adminTextWriter.write("Channel");
				adminTextWriter.write("Protocol");
				adminTextWriter.write("SequenceNumber");
				adminTextWriter.write("HeaderFlag");
				adminTextWriter.write("Text");
				adminTextWriter.endRecord();
			}
			if(processingFlags.contains("c5"))
			{
				combinationLegWriter = new CsvWriter(destinationDir+"/combinationLeg.csv");
				combinationLegWriter.write("Protocol");
				combinationLegWriter.write("Symbol");
				combinationLegWriter.write("LegSymbol");
				combinationLegWriter.write("Ratio");
				combinationLegWriter.write("MetaIfAny");
				combinationLegWriter.endRecord();
			}
			if(processingFlags.contains("90"))
			{
				instrumentStatusWriter = new CsvWriter(destinationDir+"/instrumentStatus.csv");
				instrumentStatusWriter.write("TimestampNano");
				instrumentStatusWriter.write("Channel");
				instrumentStatusWriter.write("Protocol");
				instrumentStatusWriter.write("SequenceNumber");
				instrumentStatusWriter.write("HeaderFlag");
				instrumentStatusWriter.write("Symbol");
				instrumentStatusWriter.write("MarketCenter");
				instrumentStatusWriter.write("StatusType");
				instrumentStatusWriter.write("StatusCode");
				instrumentStatusWriter.write("ReasonCode");
				instrumentStatusWriter.write("StatusFlag");
				instrumentStatusWriter.write("ReasonDetail");
				instrumentStatusWriter.write("MetaIfAny");
				instrumentStatusWriter.endRecord();
			}
			if(processingFlags.contains("92"))
			{
				imbalanceInfoWriter = new CsvWriter(destinationDir+"/imbalanceInfo.csv");
				imbalanceInfoWriter.write("TimestampNano");
				imbalanceInfoWriter.write("Channel");
				imbalanceInfoWriter.write("Protocol");
				imbalanceInfoWriter.write("SequenceNumber");
				imbalanceInfoWriter.write("HeaderFlag");
				imbalanceInfoWriter.write("Symbol");
				imbalanceInfoWriter.write("ImbalanceType");
				imbalanceInfoWriter.write("ImbalanceSide");
				imbalanceInfoWriter.write("ImbalanceVolume");
				imbalanceInfoWriter.write("MetaIfAny");
				imbalanceInfoWriter.endRecord();
			}
			if(processingFlags.contains("82"))
			{
				priceRangeIndicatorWriter = new CsvWriter(destinationDir+"/priceRangeindicator.csv");
				priceRangeIndicatorWriter.write("TimestampNano");
				priceRangeIndicatorWriter.write("Channel");
				priceRangeIndicatorWriter.write("Protocol");
				priceRangeIndicatorWriter.write("SequenceNumber");
				priceRangeIndicatorWriter.write("HeaderFlag");
				priceRangeIndicatorWriter.write("Symbol");
				priceRangeIndicatorWriter.write("MarketCenter");
				priceRangeIndicatorWriter.write("IndicationType");
				priceRangeIndicatorWriter.write("Flags");
				priceRangeIndicatorWriter.write("LowBidPrice");
				priceRangeIndicatorWriter.write("HighAskPrice");
				priceRangeIndicatorWriter.write("MetaIfAny");
				priceRangeIndicatorWriter.endRecord();
			}
			if(processingFlags.contains("c3"))
			{
				optionDeliveryWriter = new CsvWriter(destinationDir+"/optionDelivery.csv");
				
				optionDeliveryWriter.write("RootSymbol");
				optionDeliveryWriter.write("ComponentIndex");
				optionDeliveryWriter.write("ComponentTotal");
				optionDeliveryWriter.write("DeliverableUnits");
				optionDeliveryWriter.write("SettlementMethod");
				optionDeliveryWriter.write("FixedAmount");	
				optionDeliveryWriter.write("CurrencyCode");
				optionDeliveryWriter.write("StrikePercent");
				optionDeliveryWriter.write("ComponentSymbol");
				optionDeliveryWriter.endRecord();
			}
			while(doRun)
			{
				if(mappedBuffer.remaining() < 2)
					loadBuffer(mappedBuffer.position());
				int size = mappedBuffer.getShort();
				if(size == 0)
				{
					NTPLogger.warning("zero size - " + previousOffset + " " + mappedBuffer.position());
					break;
				}
				if(mappedBuffer.remaining() < size)
					loadBuffer(mappedBuffer.position());
				if(size == 1)
				{
					mappedBuffer.get();
					continue;
				}
				ByteBuffer buffer = mappedBuffer.slice();
				buffer.limit(size);
				//				displayByteMessage(buffer);
				//				buffer.flip();
				mappedBuffer.position(mappedBuffer.position() + size);			
				if(buffer.get() != 0x53)		// Every message should have 3rd byte as 0x533 - soup protocol
					displayByteMessage(buffer);
				else
				{
					byte b = buffer.get();
					switch (b) {
					case 0x20:	// System Event			
						parseSystemEvent(buffer);
						break;
					case 0x21:	//Channel Day
						parseChannelDay(buffer);
						break;
					case 0x22:	//Channel Seconds
						parseChannelSeconds(buffer);
						break;
					case 0x30:	//Market center Locate
						parseMarketCenterLocate(buffer);
						break;
					case 0x31:	//Market maker Locate
						parseMarketMakerLocate(buffer);
						break;
					case 0x33:	//Instrument System Locate
						parseInstrumentLocate(buffer);
						break;
					case 0x40:	//Order Book Clear
						break;
					case 0x41:	//Order Add- Short
						break;
					case 0x42:	//Order Add- Long
						break;
					case 0x43:	//Order Add- Extended
						break;
					case 0x44:	//Order Executed
						break;
					case 0x45:	//Order Executed at Price
						break;
					case 0x46:	//Order Executed at Price/Size
						break;
					case 0x47:	//Order Partial Cancel
						break;
					case 0x48:	//Order Deleted
						break;
					case 0x49:	//Order Modified
						break;
					case 0x50:	//Two Sided Quote - Short
						parseQuote2SidedShort(buffer);
						break;
					case 0x51:	//Two Sided Quote - Long
						parseQuote2SidedLong(buffer);
						break;
					case 0x52:	//Two Sided Quote - Extended
						parseQuote2SidedExtended(buffer);
						break;
					case 0x53:	//One Sided Quote - Short
						parseQuote1SidedShort(buffer);
						break;
					case 0x54:	//One Sided Quote - Long
						parseQuote1SidedLong(buffer);
						break;
					case 0x55:	//One Sided Quote - Extended
						parseQuote1SidedExtended(buffer);
						break;
					case 0x56:	//Two Sided Quote - Fractional
						break;
					case 0x57:	//One Sided Quote - Fractional
						break;
					case 0x60:	//Two Sided NBBO - Short
						parseBbo2SidedShort(buffer);
						break;
					case 0x61:	//Two Sided NBBO - Long
						parseBbo2SidedLong(buffer);
						break;
					case 0x62:	//Two Sided NBBO - Extended
						parseBbo2SidedExtended(buffer);
						break;
					case 0x63:	//One Sided NBBO - Short
						parseBbo1SidedShort(buffer);
						break;
					case 0x64:	//One Sided NBBO - Long
						parseBbo1SidedLong(buffer);
						break;
					case 0x65:	//One Sided NBBO - Extended
						break;
					case 0x70:	//Trade - short
						parseTradeShort(buffer);
						break;
					case 0x71:	//Trade - long
						parseTradeLong(buffer);
						break;
					case 0x72:	//Trade - extended
						parseTradeExtended(buffer);
						break;
					case 0x73:	//Trade - Cancel
						parseTradeCancel(buffer);
						break;
					case 0x74:	//Trade - Correction
						parseTradeCorrection(buffer);
						break;
					case 0x75:	//Prior day Trade report
						parsePriorDayTradeReport(buffer);
						break;
					case 0x76:	//Prior day Trade Cancel
						parsePriorDayTradeCancel(buffer);
						break;
					case 0x77:	//Prior day Trade Correction
						break;
					case 0x78:	//Trade Fractional Trade
						break;
					case 0x79:	//Prior day Trade report - Fractional
						break;
					case (byte) 0x80:	//Instrument Value Update
						parseValueUpdate(buffer);
					break;
					case (byte) 0x81:	//Instrument As-of Value Update
						break;
					case (byte) 0x82:	//Price range Indication
						parsePriceRangeIndication(buffer);
					break;
					case (byte) 0x90:	//Trading Action
						parseInstrumentStatus(buffer);
					break;
					case (byte) 0x92:	//Instrument Imbalance Information
						parseImbalanceInformation(buffer);
					break;
					case (byte) 0xA0:	//Price Level Add - Short
						break;
					case (byte) 0xA1:	//Price Level Add - Long
						break;
					case (byte) 0xA2:	//Price Level Add - Extended
						break;
					case (byte) 0xA3:	//Price Level Modify - Short
						break;
					case (byte) 0xA4:	//Price Level Modify - Long
						break;
					case (byte) 0xA5:	//Price Level Modify - Extended
						break;
					case (byte) 0xA6:	//Price Level Delete - Short
						break;
					case (byte) 0xA7:	//Price Level Delete - Long
						break;
					case (byte) 0xA8:	//Price Level Delete - Extended
						break;
					case (byte) 0xA9:	//Price Level Book Clear
						break;
					case (byte) 0xAA:	//Price Level Add - Fractional
						break;
					case (byte) 0xAB:	//Price Level Modify - Fractional
						break;
					case (byte) 0xB0:	// Channel Event
						parseChannelEvent(buffer);
					break;
					case (byte) 0xB1:	//Market Maker Status
						parseMarketMakerStatus(buffer);
					break;
					case (byte) 0xB2:	//Administrative Text
						parseAdminText(buffer);
					break;
					case (byte) 0xB3:	//News Update
						break;
					case (byte) 0xB4:	//Request for Quote
						break;
					case (byte) 0xB5:	//Corporate Action Dividend
						break;
					case (byte) 0xB6:	//Corporate Action Capital Distributions
						break;
					case (byte) 0xC0:	//Instrument Meta Data
						parseMetaData(buffer);
					break;
					case (byte) 0xC1:	//Alternate Instrument Symbol
						parseAlternateSymbol(buffer);
					break;
					case (byte) 0xC3:	//Option Delivery Component
						parseOptionDeliveryComponent(buffer);
						break;
					case (byte) 0xC4:	//Index Symbol Participation
						break;
					case (byte) 0xC5:	//Combination Leg
						parseCombinationLeg(buffer);
					break;
					case (byte) 0xFF:	//End of Snapshot Message
						break;
					default:
						NTPLogger.warning(Integer.toHexString(b) + " ");
						displayByteMessage(buffer);
						break;
					}
					//Counting types of messages
					if(byteMap.get(b) == null)
					{
						byteMap.put(b, new Long(1));
					}
					else
					{
						byteMap.put(b, byteMap.get(b) + 1);
					}
				}				
			}
			if(processingFlags.contains("50"))
				quote2SidedShortWriter.close();
			if(processingFlags.contains("51"))
				quote2SidedLongWriter.close();
			if(processingFlags.contains("52"))
				quote2SidedExtendedWriter.close();
			if(processingFlags.contains("53"))
				quote1SidedShortWriter.close();
			if(processingFlags.contains("54"))
				quote1SidedLongWriter.close();
			if(processingFlags.contains("55"))
				quote1SidedExtendedWriter.close();
			if(processingFlags.contains("60"))
				bbo2SidedShortWriter.close();
			if(processingFlags.contains("61"))
				bbo2SidedLongWriter.close();
			if(processingFlags.contains("62"))
				bbo2SidedExtendedWriter.close();
			if(processingFlags.contains("63"))
				bbo1SidedShortWriter.close();
			if(processingFlags.contains("64"))
				bbo1SidedLongWriter.close();
			if(processingFlags.contains("70"))
				tradeShortWriter.close();
			if(processingFlags.contains("71"))
				tradeLongWriter.close();
			if(processingFlags.contains("72"))
				tradeExtendedWriter.close();
			if(processingFlags.contains("73"))
				tradeCancelWriter.close();
			if(processingFlags.contains("74"))
				tradeCorrectionWriter.close();
			if(processingFlags.contains("75"))
				priorDayTradeReportWriter.close();
			if(processingFlags.contains("76"))
				priorDayCancelWriter.close();
			if(processingFlags.contains("80"))
				valueUpdateWriter.close();
			if(processingFlags.contains("82"))
				priceRangeIndicatorWriter.close();
			if(processingFlags.contains("90"))
				instrumentStatusWriter.close();
			if(processingFlags.contains("92"))
				imbalanceInfoWriter.close();
			if(processingFlags.contains("b0"))
				channelEventWriter.close();
			if(processingFlags.contains("b1"))
				marketMakerStatusWriter.close();
			if(processingFlags.contains("b2"))
				adminTextWriter.close();
			if(processingFlags.contains("c5"))
				combinationLegWriter.close();
			if(processingFlags.contains("c3"))
				optionDeliveryWriter.close();
			//writing Meta starts
			if(processingFlags.contains("c0"))
			{
				CsvWriter writer = new CsvWriter(destinationDir + "/meta_" + fileType + "--" + tradeDate +"--"+timeformat.format(new Date())+"--"+InetAddress.getLocalHost().getHostName()+ ".csv");
				writer.write("LocateCode");
				writer.write("Symbol");
				metaType = RecorderMetaHelper.updateMetaHeaders(fileType, metaType);
				NTPLogger.info("All Meta Types are");
				for(Byte b : metaType)
				{
					if(RecorderHelper.metaCodeMap.containsKey(b))
						System.out.printf("%x %s\n",b, RecorderHelper.metaCodeMap.get(b));
					else
						NTPLogger.warning("No Meta mapping for " + Integer.toHexString(b));
					writer.write(RecorderHelper.metaCodeMap.get(b));
				}
				writer.endRecord();
				for(Integer i : symbolLocateMap.keySet())
				{
					HashMap<Byte, String> map = metaValueMap.get(i);
					writer.write("" + i);
					String ticker = symbolLocateMap.get(i);
					writer.write(ticker);
					String mappedValue = null;
					if(fileType.equalsIgnoreCase("UTP"))
						map.put((byte) 0x19,"" + nasdaqMCLocate);
					for(Byte b : metaType)
					{
						mappedValue = "";
						String value = map.get(b);
						//						if(map.get(b) == null && b != (byte) 0xf8 && b != (byte) 0x0d)
						//							mappedValue = "";
						//						else
						//						{
						switch (b) {
						case 0x0d:	//Market Tier
							if(fileType.equalsIgnoreCase("CTA-A") || fileType.equalsIgnoreCase("CTA-B")) //Market Tier is derived from Listing Exchange
							{
								String mcLocate  = map.get((byte)0x19);
								if(mcLocate == null || mcLocate.length() == 0)
								{
									NTPLogger.missingMetaValue(ticker, "ListingExchange:0x19", mcLocate);
									break;
								}
								String mc = "";
								try{ mc = marketCenterLocateMap.get(Short.parseShort(mcLocate));} 
								catch(Exception e)
								{
									NTPLogger.missingMetaValue(ticker, "MarketCenterLocate:0x19", mcLocate);
									break;
								}
								if(mc == null || mc.length() == 0)
								{
									NTPLogger.missingMetaValue(ticker, "MarketCenter:0x19", mcLocate);
									break;
								}
								mappedValue = RecorderHelper.marketTier.get(mc);
								if(mappedValue == null || mappedValue.length() == 0) 
									NTPLogger.missingMetaValue(ticker, "MarketTier:0x0d", mc);
							}
							else
							{
								String tier = map.get(b);
								if(tier == null || tier.length() == 0)
								{
									NTPLogger.missingMetaValue(ticker, "MarketTierMap:0x0d", tier);
									break;
								}
								mappedValue = RecorderHelper.marketTier.get(tier);
								if(mappedValue == null || mappedValue.length() == 0) 
									NTPLogger.missingMetaValue(ticker, "MarketTier:0x0d", tier);
							}								
							break;
						case 0x0e:	//Financial Status
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "FinancialStatusMap:0x0e", value);
								break;
							}
							mappedValue = RecorderHelper.financialStatus.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "FinancialStatus:0x0e", value);
							break;
						case 0x0f:	//Authenticity
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "AuthenticityMap:0x0f", value);
								break;
							}
							mappedValue = RecorderHelper.authenticity.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "Authenticity:0x0f", value);
							break;
						case 0x10:	//ShortSaleThresholdIndicator
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "ShortSaleThresholdIndicator:0x10", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x13:	//Contract Multiplier Type
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "ContractMultiplierMap:0x13", value);
								break;
							}
							mappedValue = RecorderHelper.contractMultiplierType.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "ContractMultiplier:0x13", value);
							break;
						case 0x18:	//Matching Algorithm
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "MatchingAlgorithmMap:0x18", value);
								break;
							}
							mappedValue = RecorderHelper.matchingAlgo.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "MatchingAlgorithm:0x18", value);
							break;
						case 0x19:	//Listing Market	
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "ListingMarketMap:0x19", value);
								break;
							}
							try {mappedValue = marketCenterLocateMap.get(Short.parseShort(value)); }
							catch (Exception e) {
								NTPLogger.missingMetaValue(ticker, "ListingMarketParsing:0x19", value);
								break;
							}
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "ListingMarket:0x19", value);
							break;
						case 0x1A:	//Round Lot
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "RoundLot:0x1A", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x2A:	//CaveatEmptor
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "CaveatEmptor:0x2A", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x2c:	//BB_Quoted
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "BB_Quoted:0x2c", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x26:	// Strategy
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "StrategyMap:0x26", value);
								break;
							}
							mappedValue = RecorderHelper.strategy.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "Strategy:0x26", value);
							break;
						case 0x29:	//PiggybackExempt
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "PiggybackExempt:0x29", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x2b:	//UnsolicitedOnly
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "UnsolicitedOnly:0x2b", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x2d:	//MessagingDisabled
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "MessagingDisabled:0x2d", value);
								break;
							}
						mappedValue = value;
						break;						
						case 0x2e:	//Issue Name
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "IssueName:0x2e", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x2f:	//Old Symbol
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "OldSymbol:0x2f", value);
								break;
							}
						mappedValue = value;
						break;
						case 0x30:	//Issue Type
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "IssueTypeMap:0x30", value);
								break;
							}
							mappedValue = RecorderHelper.issueType.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "IssueType:0x30", value);
							break;
						case 0x31:	// Calculation Method
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "CalculationMethodMap:0x31", value);
								break;
							}
							mappedValue = RecorderHelper.calculationMethod.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "CalculationMethod:0x31", value);
							break;
						case 0x32:	// Settlement Time
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "SettlementTimeMap:0x32", value);
								break;
							}
							mappedValue = RecorderHelper.settlementTime.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "SettlementTime:0x32", value);
							break;
						case 0x34: // Dissemination Type
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "DisseminationTypeMap:0x34", value);
								break;
							}
							mappedValue = RecorderHelper.disseminationFrequency.get(value);
							if(mappedValue == null || mappedValue.length() == 0) 
								NTPLogger.missingMetaValue(ticker, "DisseminationType:0x34", value);
							break;
						case (byte) 0x51:	//Parent Symbol
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "ParentSymbolMap:0x51", value);
								break;
							}
						try {mappedValue = symbolLocateMap.get(Integer.parseInt(value)); }
						catch (Exception e) {
							NTPLogger.missingMetaValue(ticker, "ParentSymbolParsing:0x51", value);
							break;
						}
						if(mappedValue == null || mappedValue.length() == 0) 
							NTPLogger.missingMetaValue(ticker, "ParentSymbol:0x51", value);
						break;
						case (byte) 0xd0:	//Rule144aFlag
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "Rule144aFlag:0xd0", value);
								break;
							}
						mappedValue = value;
						break;
						case (byte) 0xd1:	//SaturationEligibleFlag
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "SaturationEligibleFlag:0xd1", value);
								break;
							}
						mappedValue = value;
						break;
						case (byte) 0xf8:	//Primary Market Center Code - 1/2 digit exchange code used across bottom of system
							value = map.get((byte)0x19);
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "ParentSymbolCodeMap:0xf8", value);
								break;
							}
						String mc = "";
						try {mc = marketCenterLocateMap.get(Short.parseShort(value)); }
						catch (Exception e) {
							NTPLogger.missingMetaValue(ticker, "ParentSymbolParsing:0xf8", value);
							break;
						}
						if(mc == null || mc.length() == 0)
						{
							NTPLogger.missingMetaValue(ticker, "ParentSymbolMarketCenter:0xf8", value);
							break;
						}
						mappedValue = RecorderHelper.primaryMarketCode.get(mc);
						if(mappedValue == null || mappedValue.length() == 0) 
							NTPLogger.missingMetaValue(ticker, "ParentSymbol:0xf8", value);
						break;
						case (byte) 0xf9:	//Symbology Type
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "SymbologyType:0xf9", value);
								break;
							}
						mappedValue = value;
						break;
						case (byte) 0xfa:	//Alternate Symbol
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "AlternateSymbol:0xfa", value);
								break;
							}
						mappedValue = value;
						break;
						case (byte) 0xfb:	//Product Type
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "ProductTypeMap:0xfb", value);
								break;
							}
						mappedValue = RecorderHelper.productType.get(value);
						if(mappedValue == null || mappedValue.length() == 0) 
							NTPLogger.missingMetaValue(ticker, "ProductType:0xfb", value);
						break;
						case (byte) 0xfd:	//Currency
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "Currency:0xfd", value);
								break;
							}
						mappedValue = value;
						break;
						case (byte) 0xfe:	//Country
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "Country:0xfe", value);
								break;
							}
						mappedValue = value;
						break;
						default:
							if(value == null || value.length() == 0)
							{
								NTPLogger.missingMetaValue(ticker, "UNKNOWN-Map:0x" + b, value);
								break;
							}
							mappedValue = map.get(b);
							NTPLogger.missingMetaValue(ticker, "UNKNOWN:" + b, value);
							break;
						}
						//						}
						writer.write(mappedValue);
					}
					writer.endRecord();
				}
				writer.close();
			}

			// Writing Meta ends

			NTPLogger.info("Record Summary for this file is");
			for(Byte b : byteMap.keySet())
			{
				System.out.printf("%x " , b);
				System.out.println(byteMap.get(b));
			}
			channel.close();
			file.close();
			NTPLogger.info("Process completed");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Previous Offset " + previousOffset + " Buffer pointer " + mappedBuffer.position());
		}
	}

	private void displayByteMessage(ByteBuffer buf)
	{
		int left =  buf.remaining();
		//		System.out.println(previousOffset + " "  + mappedBuffer.position());
		for(int i =0; i <left; i++)
			System.out.print(Integer.toHexString(buf.get()) + " ");
		System.out.println();
	}

	private void loadBuffer(long offset) throws IOException
	{
		previousOffset += offset;
		//		System.out.println("Loaded Buffer Offset " + previousOffset);
		if(previousOffset + bufferSize < filseSize )
			mappedBuffer = channel.map(MapMode.READ_ONLY, previousOffset, bufferSize);
		else if(previousOffset == filseSize)
			doRun = false;
		else
			mappedBuffer = channel.map(MapMode.READ_ONLY, previousOffset, filseSize-offset);
	}
	private Integer getTimeFromSeconds(int seconds)
	{
		int hh = seconds / 3600;
		seconds = seconds - (hh * 3600);
		int mm = seconds / 60;
		seconds =  seconds - (mm * 60);
		return  ((hh*10000) + (mm * 100) + seconds );
	}
	private void parseSystemEvent(ByteBuffer buffer)
	{
		long timestamp = buffer.getLong();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String eventCode = new String(bytes);
		Date date = new Date(timestamp);
		System.out.println("SYS_EVT " + timeformatter.format(date) + " " + eventCode + " " + timestamp);
	}

	private void parseChannelDay(ByteBuffer buffer)
	{
		byte protocolId = buffer.get();
		byte channelIndex = buffer.get();
		byte dayOfMonth = buffer.get();
		System.out.println("CHL_DAY " + protocolId + " " + channelIndex + " " + dayOfMonth);
	}

	private void parseChannelSeconds(ByteBuffer buffer)
	{
		byte protocolId = buffer.get();
		byte channelIndex = buffer.get();
		int seconds = buffer.getInt();
		channelTimeMap.put(channelIndex + "_" + protocolId, getTimeFromSeconds(seconds));
		//		System.out.println("CHL_SEC " +  protocolId + " " + channelIndex + " " + getTimeFromSeconds(seconds));
	}

	private void parseMarketCenterLocate(ByteBuffer buffer)
	{
		short code = buffer.getShort();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String mic = new String(bytes);
		marketCenterLocateMap.put(code, mic);
		if(mic.equalsIgnoreCase("XNAS"))
			nasdaqMCLocate = code;
		System.out.println("MC_LOC " + code + " " + mic);
	}

	private void parseMarketMakerLocate(ByteBuffer buffer)
	{
		short code = buffer.getShort();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String mmid = new String(bytes);
		marketMakerLocateMap.put(code, mmid);
		System.out.println("MM_LOC " + code + " " + mmid);
	}

	private void parseInstrumentLocate(ByteBuffer buffer)
	{
		int locate = buffer.getInt();
		byte[] bytes = new byte[2];
		buffer.get(bytes);
		String country = new String(bytes);
		bytes = new byte[3];
		buffer.get(bytes);
		String currency = new String(bytes);
		bytes = new byte[4];
		buffer.get(bytes);
		String mic = new String(bytes);
		byte productType = buffer.get();
		Byte length = buffer.get();
		bytes = new byte[length.intValue()];
		buffer.get(bytes);
		String symbol = new String(bytes);
		symbolLocateMap.put(locate, symbol);
		HashMap<Byte, String> metaMap = metaValueMap.get(locate);
		if(metaMap == null)
		{
			metaMap = new HashMap<>();
			metaValueMap.put(locate, metaMap);
		}
		if(country.trim().length() > 0){
			metaMap.put((byte)0xfe, country);
			metaType.add((byte) 0xfe);
		}
		if(currency.trim().length() > 0){
			metaMap.put((byte)0xfd, currency);
			metaType.add((byte) 0xfd);
		}
		if(mic.trim().length() > 0){
			metaMap.put((byte)0xfc, mic);
			metaType.add((byte) 0xfc);
		}
		metaType.add((byte) 0xfb);
		metaMap.put((byte)0xfb, "" +productType);
		//		System.out.println("INS_LOC " + locate + " - " + country + " - " + currency + " - " + mic + " - " + productType + " - " + symbol );
		HashMap<Byte, String> appMap = parseAppendageElements(buffer);
		if(appMap != null && appMap.size() > 0)
		{
			metaType.addAll(appMap.keySet());
			metaMap.putAll(appMap);
			//			for(Byte b : appMap.keySet())
			//				System.out.printf("INS_META %x %s\n" , b, appMap.get(b));
		}
	}

	private void parseHeader(ByteBuffer buffer, CsvWriter writer) throws IOException
	{
		byte protocolId = buffer.get();
//		int unsignedProt = protocolId;
//		if(protocolId < 0)
//			unsignedProt = 256 + protocolId;
		byte channelIndex = buffer.get();
		byte messageFlag = buffer.get();
		int sequenceNumber = buffer.getInt();
		int seconds = buffer.getInt();
		writer.write("" + (channelTimeMap.get(channelIndex+"_"+protocolId) + (seconds * .000000001)));
		writer.write("" + channelIndex);
		writer.write("" + protocolId);
		writer.write("" + sequenceNumber);
		writer.write(Integer.toHexString(messageFlag));
	}

	private void parseQuote2SidedShort(ByteBuffer buffer)
	{
		if(!processingFlags.contains("50"))
			return;
		try
		{
			parseHeader(buffer, quote2SidedShortWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			short bidPrice = buffer.getShort();
			short bidSize = buffer.getShort();
			short askPrice = buffer.getShort();
			short askSize = buffer.getShort();
			byte condition = buffer.get();
			byte quoteFlag = buffer.get();
			byte bidFlag = buffer.get();
			byte askFlag = buffer.get();
			quote2SidedShortWriter.write(symbolLocateMap.get(locate));
			quote2SidedShortWriter.write(marketCenterLocateMap.get(mcLocate));
			quote2SidedShortWriter.write(marketMakerLocateMap.get(mmLocate));
			quote2SidedShortWriter.write(setDecimal("" + bidPrice, 2));
			quote2SidedShortWriter.write("" + bidSize);
			quote2SidedShortWriter.write(setDecimal("" + askPrice, 2));
			quote2SidedShortWriter.write("" + askSize);
			quote2SidedShortWriter.write(Integer.toHexString(condition));
			quote2SidedShortWriter.write(Integer.toHexString(quoteFlag));
			quote2SidedShortWriter.write(Integer.toHexString(bidFlag));
			quote2SidedShortWriter.write(Integer.toHexString(askFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				quote2SidedShortWriter.write(Integer.toHexString(b));
				quote2SidedShortWriter.write(appMap.get(b));
			}
			quote2SidedShortWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseQuote2SidedLong(ByteBuffer buffer)
	{
		if(!processingFlags.contains("51"))
			return;
		try
		{
			parseHeader(buffer, quote2SidedLongWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			int bidPrice = buffer.getInt();
			short bidSize = buffer.getShort();
			int askPrice = buffer.getInt();
			short askSize = buffer.getShort();
			byte condition = buffer.get();
			byte quoteFlag = buffer.get();
			byte bidFlag = buffer.get();
			byte askFlag = buffer.get();
			quote2SidedLongWriter.write(symbolLocateMap.get(locate));
			quote2SidedLongWriter.write(marketCenterLocateMap.get(mcLocate));
			quote2SidedLongWriter.write(marketMakerLocateMap.get(mmLocate));
			quote2SidedLongWriter.write(setDecimal("" + bidPrice, 4));
			quote2SidedLongWriter.write("" + bidSize);
			quote2SidedLongWriter.write(setDecimal("" + askPrice, 4));
			quote2SidedLongWriter.write("" + askSize);
			quote2SidedLongWriter.write(Integer.toHexString(condition));
			quote2SidedLongWriter.write(Integer.toHexString(quoteFlag));
			quote2SidedLongWriter.write(Integer.toHexString(bidFlag));
			quote2SidedLongWriter.write(Integer.toHexString(askFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				quote2SidedLongWriter.write(Integer.toHexString(b));
				quote2SidedLongWriter.write(appMap.get(b));
			}
			quote2SidedLongWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseQuote2SidedExtended(ByteBuffer buffer)
	{
		if(!processingFlags.contains("52"))
			return;
		try
		{
			parseHeader(buffer, quote2SidedExtendedWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			byte bidDen = buffer.get();
			long bidPrice = buffer.getLong();
			int bidSize = buffer.getInt();
			byte askDen = buffer.get();
			long askPrice = buffer.getLong();
			int askSize = buffer.getInt();
			byte condition = buffer.get();
			byte quoteFlag = buffer.get();
			byte bidFlag = buffer.get();
			byte askFlag = buffer.get();
			quote2SidedExtendedWriter.write(symbolLocateMap.get(locate));
			quote2SidedExtendedWriter.write(marketCenterLocateMap.get(mcLocate));
			quote2SidedExtendedWriter.write(marketMakerLocateMap.get(mmLocate));
			quote2SidedExtendedWriter.write(setDecimal("" + bidPrice, bidDen));
			quote2SidedExtendedWriter.write("" + bidSize);
			quote2SidedExtendedWriter.write(setDecimal("" + askPrice, askDen));
			quote2SidedExtendedWriter.write("" + askSize);
			quote2SidedExtendedWriter.write(Integer.toHexString(condition));
			quote2SidedExtendedWriter.write(Integer.toHexString(quoteFlag));
			quote2SidedExtendedWriter.write(Integer.toHexString(bidFlag));
			quote2SidedExtendedWriter.write(Integer.toHexString(askFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				quote2SidedExtendedWriter.write(Integer.toHexString(b));
				quote2SidedExtendedWriter.write(appMap.get(b));
			}
			quote2SidedExtendedWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseQuote1SidedShort(ByteBuffer buffer)
	{
		if(!processingFlags.contains("53"))
			return;
		try
		{
			parseHeader(buffer, quote1SidedShortWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			short price = buffer.getShort();
			short size = buffer.getShort();
			byte side = buffer.get();
			byte condition = buffer.get();
			byte quoteFlag = buffer.get();
			byte priceFlag = buffer.get();
			quote1SidedShortWriter.write(symbolLocateMap.get(locate));
			quote1SidedShortWriter.write(marketCenterLocateMap.get(mcLocate));
			quote1SidedShortWriter.write(marketMakerLocateMap.get(mmLocate));
			quote1SidedShortWriter.write(setDecimal("" + price, 2));
			quote1SidedShortWriter.write("" + size);
			if(side == 'B')
				quote1SidedShortWriter.write("Buy");
			else
				quote1SidedShortWriter.write("Sell");
			quote1SidedShortWriter.write(Integer.toHexString(condition));
			quote1SidedShortWriter.write(Integer.toHexString(quoteFlag));
			quote1SidedShortWriter.write(Integer.toHexString(priceFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				quote1SidedShortWriter.write(Integer.toHexString(b));
				quote1SidedShortWriter.write(appMap.get(b));
			}
			quote1SidedShortWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseQuote1SidedLong(ByteBuffer buffer)
	{
		if(!processingFlags.contains("54"))
			return;
		try
		{
			parseHeader(buffer, quote1SidedLongWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			int price = buffer.getInt();
			short size = buffer.getShort();
			byte side = buffer.get();
			byte condition = buffer.get();
			byte quoteFlag = buffer.get();
			byte priceFlag = buffer.get();
			quote1SidedLongWriter.write(symbolLocateMap.get(locate));
			quote1SidedLongWriter.write(marketCenterLocateMap.get(mcLocate));
			quote1SidedLongWriter.write(marketMakerLocateMap.get(mmLocate));
			quote1SidedLongWriter.write(setDecimal("" + price, 4));
			quote1SidedLongWriter.write("" + size);
			if(side == 'B')
				quote1SidedLongWriter.write("Buy");
			else
				quote1SidedLongWriter.write("Sell");
			quote1SidedLongWriter.write(Integer.toHexString(condition));
			quote1SidedLongWriter.write(Integer.toHexString(quoteFlag));
			quote1SidedLongWriter.write(Integer.toHexString(priceFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				quote1SidedLongWriter.write(Integer.toHexString(b));
				quote1SidedLongWriter.write(appMap.get(b));
			}
			quote1SidedLongWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseQuote1SidedExtended(ByteBuffer buffer)
	{
		if(!processingFlags.contains("55"))
			return;
		try
		{
			parseHeader(buffer, quote1SidedExtendedWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			byte den = buffer.get();
			long price = buffer.getLong();
			int size = buffer.getInt();
			byte side = buffer.get();
			byte condition = buffer.get();
			byte quoteFlag = buffer.get();
			byte priceFlag = buffer.get();
			quote1SidedExtendedWriter.write(symbolLocateMap.get(locate));
			quote1SidedExtendedWriter.write(marketCenterLocateMap.get(mcLocate));
			quote1SidedExtendedWriter.write(marketMakerLocateMap.get(mmLocate));
			quote1SidedExtendedWriter.write(setDecimal("" + price, den));
			quote1SidedExtendedWriter.write("" + size);
			if(side == 'B')
				quote1SidedExtendedWriter.write("Buy");
			else
				quote1SidedExtendedWriter.write("Sell");
			quote1SidedExtendedWriter.write(Integer.toHexString(condition));
			quote1SidedExtendedWriter.write(Integer.toHexString(quoteFlag));
			quote1SidedExtendedWriter.write(Integer.toHexString(priceFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				quote1SidedExtendedWriter.write(Integer.toHexString(b));
				quote1SidedExtendedWriter.write(appMap.get(b));
			}
			quote1SidedExtendedWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void parseBbo2SidedShort(ByteBuffer buffer)
	{
		if(!processingFlags.contains("60"))
			return;
		try
		{
			parseHeader(buffer, bbo2SidedShortWriter);
			int locate = buffer.getInt();
			short bidMcLocate = buffer.getShort();
			short bidPrice = buffer.getShort();
			short bidSize = buffer.getShort();
			short askMcLocate = buffer.getShort();
			short askPrice = buffer.getShort();
			short askSize = buffer.getShort();
			byte condition = buffer.get();
			byte flag = buffer.get();
			bbo2SidedShortWriter.write(symbolLocateMap.get(locate));
			bbo2SidedShortWriter.write(marketCenterLocateMap.get(bidMcLocate));
			bbo2SidedShortWriter.write(setDecimal("" + bidPrice, 2));
			bbo2SidedShortWriter.write("" + bidSize);
			bbo2SidedShortWriter.write(marketCenterLocateMap.get(askMcLocate));
			bbo2SidedShortWriter.write(setDecimal("" + askPrice, 2));
			bbo2SidedShortWriter.write("" + askSize);
			bbo2SidedShortWriter.write(Integer.toHexString(condition));
			bbo2SidedShortWriter.write(Integer.toHexString(flag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				bbo2SidedShortWriter.write(Integer.toHexString(b));
				bbo2SidedShortWriter.write(appMap.get(b));
			}
			bbo2SidedShortWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseBbo2SidedLong(ByteBuffer buffer)
	{
		if(!processingFlags.contains("61"))
			return;
		try
		{
			parseHeader(buffer, bbo2SidedLongWriter);
			int locate = buffer.getInt();
			short bidMcLocate = buffer.getShort();
			int bidPrice = buffer.getInt();
			short bidSize = buffer.getShort();
			short askMcLocate = buffer.getShort();
			int askPrice = buffer.getInt();
			short askSize = buffer.getShort();
			byte condition = buffer.get();
			byte flag = buffer.get();
			bbo2SidedLongWriter.write(symbolLocateMap.get(locate));
			bbo2SidedLongWriter.write(marketCenterLocateMap.get(bidMcLocate));
			bbo2SidedLongWriter.write(setDecimal("" + bidPrice, 4));
			bbo2SidedLongWriter.write("" + bidSize);
			bbo2SidedLongWriter.write(marketCenterLocateMap.get(askMcLocate));
			bbo2SidedLongWriter.write(setDecimal("" + askPrice, 4));
			bbo2SidedLongWriter.write("" + askSize);
			bbo2SidedLongWriter.write(Integer.toHexString(condition));
			bbo2SidedLongWriter.write(Integer.toHexString(flag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				bbo2SidedLongWriter.write(Integer.toHexString(b));
				bbo2SidedLongWriter.write(appMap.get(b));
			}
			bbo2SidedLongWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseBbo2SidedExtended(ByteBuffer buffer)
	{
		if(!processingFlags.contains("62"))
			return;
		try
		{
			parseHeader(buffer, bbo2SidedExtendedWriter);
			int locate = buffer.getInt();
			short bidMcLocate = buffer.getShort();
			byte bidDen = buffer.get();
			long bidPrice = buffer.getLong();
			int bidSize = buffer.getInt();
			short askMcLocate = buffer.getShort();
			byte askDen = buffer.get();
			long askPrice = buffer.getLong();
			int askSize = buffer.getInt();
			byte condition = buffer.get();
			byte flag = buffer.get();
			bbo2SidedExtendedWriter.write(symbolLocateMap.get(locate));
			bbo2SidedExtendedWriter.write(marketCenterLocateMap.get(bidMcLocate));
			bbo2SidedExtendedWriter.write(setDecimal("" + bidPrice, bidDen));
			bbo2SidedExtendedWriter.write("" + bidSize);
			bbo2SidedExtendedWriter.write(marketCenterLocateMap.get(askMcLocate));
			bbo2SidedExtendedWriter.write(setDecimal("" + askPrice, askDen));
			bbo2SidedExtendedWriter.write("" + askSize);
			bbo2SidedExtendedWriter.write(Integer.toHexString(condition));
			bbo2SidedExtendedWriter.write(Integer.toHexString(flag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				bbo2SidedExtendedWriter.write(Integer.toHexString(b));
				bbo2SidedExtendedWriter.write(appMap.get(b));
			}
			bbo2SidedExtendedWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void parseBbo1SidedShort(ByteBuffer buffer)
	{
		if(!processingFlags.contains("63"))
			return;
		try
		{
			parseHeader(buffer, bbo1SidedShortWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short price = buffer.getShort();
			short size = buffer.getShort();
			byte side = buffer.get();
			byte condition = buffer.get();
			byte flag = buffer.get();
			bbo1SidedShortWriter.write(symbolLocateMap.get(locate));
			bbo1SidedShortWriter.write(marketCenterLocateMap.get(mcLocate));
			bbo1SidedShortWriter.write(setDecimal("" + price, 2));
			bbo1SidedShortWriter.write("" + size);
			bbo1SidedShortWriter.write("" + side);
			bbo1SidedShortWriter.write(Integer.toHexString(condition));
			bbo1SidedShortWriter.write(Integer.toHexString(flag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				bbo1SidedShortWriter.write(Integer.toHexString(b));
				bbo1SidedShortWriter.write(appMap.get(b));
			}
			bbo1SidedShortWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void parseBbo1SidedLong(ByteBuffer buffer)
	{
		if(!processingFlags.contains("64"))
			return;
		try
		{
			parseHeader(buffer, bbo1SidedLongWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int price = buffer.getShort();
			short size = buffer.getShort();
			byte side = buffer.get();
			byte condition = buffer.get();
			byte flag = buffer.get();
			bbo1SidedLongWriter.write(symbolLocateMap.get(locate));
			bbo1SidedLongWriter.write(marketCenterLocateMap.get(mcLocate));
			bbo1SidedLongWriter.write(setDecimal("" + price, 4));
			bbo1SidedLongWriter.write("" + size);
			bbo1SidedLongWriter.write("" + side);
			bbo1SidedLongWriter.write(Integer.toHexString(condition));
			bbo1SidedLongWriter.write(Integer.toHexString(flag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				bbo1SidedLongWriter.write(Integer.toHexString(b));
				bbo1SidedLongWriter.write(appMap.get(b));
			}
			bbo1SidedLongWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void parseTradeShort(ByteBuffer buffer)
	{
		if(!processingFlags.contains("70"))
			return;
		try
		{
			parseHeader(buffer, tradeShortWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int tradeId = buffer.getInt();
			short price = buffer.getShort();
			short size = buffer.getShort();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte changeFlag = buffer.get();
			tradeShortWriter.write(symbolLocateMap.get(locate));
			tradeShortWriter.write(marketCenterLocateMap.get(mcLocate));
			tradeShortWriter.write("" + tradeId);
			tradeShortWriter.write(setDecimal("" + price, 2));
			tradeShortWriter.write("" + size);
			tradeShortWriter.write(Integer.toHexString(priceFlag));
			tradeShortWriter.write(Integer.toHexString(eligibilityFLag));
			tradeShortWriter.write("" + reportFLag);
			tradeShortWriter.write(Integer.toHexString(changeFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				tradeShortWriter.write(Integer.toHexString(b));
				tradeShortWriter.write(appMap.get(b));
			}
			tradeShortWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseTradeLong(ByteBuffer buffer)
	{
		if(!processingFlags.contains("71"))
			return;
		try
		{
			parseHeader(buffer, tradeLongWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int tradeId = buffer.getInt();
			int price = buffer.getInt();
			short size = buffer.getShort();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte changeFlag = buffer.get();
			tradeLongWriter.write(symbolLocateMap.get(locate));
			tradeLongWriter.write(marketCenterLocateMap.get(mcLocate));
			tradeLongWriter.write("" + tradeId);
			tradeLongWriter.write(setDecimal("" + price, 4));
			tradeLongWriter.write("" + size);
			tradeLongWriter.write(Integer.toHexString(priceFlag));
			tradeLongWriter.write(Integer.toHexString(eligibilityFLag));
			tradeLongWriter.write("" + reportFLag);
			tradeLongWriter.write(Integer.toHexString(changeFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				tradeLongWriter.write(Integer.toHexString(b));
				tradeLongWriter.write(appMap.get(b));
			}
			tradeLongWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseTradeExtended(ByteBuffer buffer)
	{
		if(!processingFlags.contains("72"))
			return;
		try
		{
			parseHeader(buffer, tradeExtendedWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int tradeId = buffer.getInt();
			byte denominator = buffer.get();
			long price = buffer.getLong();
			long size = buffer.getLong();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte changeFlag = buffer.get();
			tradeExtendedWriter.write(symbolLocateMap.get(locate));
			tradeExtendedWriter.write(marketCenterLocateMap.get(mcLocate));
			tradeExtendedWriter.write("" + tradeId);
			tradeExtendedWriter.write(setDecimal("" + price, denominator));
			tradeExtendedWriter.write("" + size);
			tradeExtendedWriter.write(Integer.toHexString(priceFlag));
			tradeExtendedWriter.write(Integer.toHexString(eligibilityFLag));
			tradeExtendedWriter.write("" + reportFLag);
			tradeExtendedWriter.write(Integer.toHexString(changeFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				tradeExtendedWriter.write(Integer.toHexString(b));
				tradeExtendedWriter.write(appMap.get(b));
			}
			tradeExtendedWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseTradeCancel(ByteBuffer buffer)
	{
		if(!processingFlags.contains("73"))
			return;
		try
		{
			parseHeader(buffer, tradeCancelWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int tradeId = buffer.getInt();
			byte denominator = buffer.get();
			long price = buffer.getLong();
			long size = buffer.getLong();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte cancelFlag = buffer.get();
			tradeCancelWriter.write(symbolLocateMap.get(locate));
			tradeCancelWriter.write(marketCenterLocateMap.get(mcLocate));
			tradeCancelWriter.write("" + tradeId);
			tradeCancelWriter.write(setDecimal("" + price, denominator));
			tradeCancelWriter.write("" + size);
			tradeCancelWriter.write(Integer.toHexString(priceFlag));
			tradeCancelWriter.write(Integer.toHexString(eligibilityFLag));
			tradeCancelWriter.write("" + reportFLag);
			tradeCancelWriter.write(Integer.toHexString(cancelFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				tradeCancelWriter.write(Integer.toHexString(b));
				tradeCancelWriter.write(appMap.get(b));
			}
			tradeCancelWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseTradeCorrection(ByteBuffer buffer)
	{
		if(!processingFlags.contains("74"))
			return;
		try
		{
			parseHeader(buffer, tradeCorrectionWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int originalTradeId = buffer.getInt();
			byte originalDenominator = buffer.get();
			long originalPrice = buffer.getLong();
			long originalSize = buffer.getLong();
			byte originalPriceFlag = buffer.get();
			byte originalEligibilityFLag = buffer.get();
			short originalReportFLag = buffer.getShort();
			int tradeId = buffer.getInt();
			byte denominator = buffer.get();
			long price = buffer.getLong();
			long size = buffer.getLong();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte correctionFlag = buffer.get();
			tradeCorrectionWriter.write(symbolLocateMap.get(locate));
			tradeCorrectionWriter.write(marketCenterLocateMap.get(mcLocate));
			tradeCorrectionWriter.write("" + originalTradeId);
			tradeCorrectionWriter.write(setDecimal("" + originalPrice, originalDenominator));
			tradeCorrectionWriter.write("" + originalSize);
			tradeCorrectionWriter.write(Integer.toHexString(originalPriceFlag));
			tradeCorrectionWriter.write(Integer.toHexString(originalEligibilityFLag));
			tradeCorrectionWriter.write("" + originalReportFLag);
			tradeCorrectionWriter.write("" + tradeId);
			tradeCorrectionWriter.write(setDecimal("" + price, denominator));
			tradeCorrectionWriter.write("" + size);
			tradeCorrectionWriter.write(Integer.toHexString(priceFlag));
			tradeCorrectionWriter.write(Integer.toHexString(eligibilityFLag));
			tradeCorrectionWriter.write("" + reportFLag);
			tradeCorrectionWriter.write(Integer.toHexString(correctionFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				tradeCorrectionWriter.write(Integer.toHexString(b));
				tradeCorrectionWriter.write(appMap.get(b));
			}
			tradeCorrectionWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parsePriorDayTradeReport(ByteBuffer buffer)
	{
		if(!processingFlags.contains("75"))
			return;
		try
		{
			parseHeader(buffer, priorDayTradeReportWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int tradeId = buffer.getInt();
			byte denominator = buffer.get();
			long price = buffer.getLong();
			long size = buffer.getLong();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte month = buffer.get();
			byte day = buffer.get();
			short year = buffer.getShort();
			long date = (year * 10000) + (month * 100) + day;
			long timestamp = buffer.getLong();
			priorDayTradeReportWriter.write(symbolLocateMap.get(locate));
			priorDayTradeReportWriter.write(marketCenterLocateMap.get(mcLocate));
			priorDayTradeReportWriter.write("" + tradeId);
			priorDayTradeReportWriter.write(setDecimal("" + price, denominator));
			priorDayTradeReportWriter.write("" + size);
			priorDayTradeReportWriter.write(Integer.toHexString(priceFlag));
			priorDayTradeReportWriter.write(Integer.toHexString(eligibilityFLag));
			priorDayTradeReportWriter.write("" + reportFLag);
			priorDayTradeReportWriter.write("" + date);
			priorDayTradeReportWriter.write(nanoSecToTime(timestamp));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				priorDayTradeReportWriter.write(Integer.toHexString(b));
				priorDayTradeReportWriter.write(appMap.get(b));
			}
			priorDayTradeReportWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parsePriorDayTradeCancel(ByteBuffer buffer)
	{
		if(!processingFlags.contains("76"))
			return;
		try
		{
			parseHeader(buffer, priorDayCancelWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int tradeId = buffer.getInt();
			byte denominator = buffer.get();
			long price = buffer.getLong();
			long size = buffer.getLong();
			byte priceFlag = buffer.get();
			byte eligibilityFLag = buffer.get();
			short reportFLag = buffer.getShort();
			byte month = buffer.get();
			byte day = buffer.get();
			short year = buffer.getShort();
			long date = (year * 10000) + (month * 100) + day;
			long timestamp = buffer.getLong();
			byte cancelFlag = buffer.get();
			priorDayCancelWriter.write(symbolLocateMap.get(locate));
			priorDayCancelWriter.write(marketCenterLocateMap.get(mcLocate));
			priorDayCancelWriter.write("" + tradeId);
			priorDayCancelWriter.write(setDecimal("" + price, denominator));
			priorDayCancelWriter.write("" + size);
			priorDayCancelWriter.write(Integer.toHexString(priceFlag));
			priorDayCancelWriter.write(Integer.toHexString(eligibilityFLag));
			priorDayCancelWriter.write("" + reportFLag);
			priorDayCancelWriter.write("" + date);
			priorDayCancelWriter.write(nanoSecToTime(timestamp));
			priorDayCancelWriter.write(Integer.toHexString(cancelFlag));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				priorDayCancelWriter.write(Integer.toHexString(b));
				priorDayCancelWriter.write(appMap.get(b));
			}
			priorDayCancelWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseValueUpdate(ByteBuffer buffer)
	{
		//Testing Manually right now
		if(!processingFlags.contains("80"))
			return;
		try
		{
			parseHeader(buffer, valueUpdateWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			int flags = buffer.getInt();
			valueUpdateWriter.write(symbolLocateMap.get(locate));
			valueUpdateWriter.write(marketCenterLocateMap.get(mcLocate));
			//Need to check bitmast flag
			valueUpdateWriter.write(Integer.toHexString(flags));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				valueUpdateWriter.write(Integer.toHexString(b));
				valueUpdateWriter.write(appMap.get(b));
			}
			valueUpdateWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parsePriceRangeIndication(ByteBuffer buffer)
	{
		if(!processingFlags.contains("82"))
			return;
		try
		{
			parseHeader(buffer, priceRangeIndicatorWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			byte indicationType = buffer.get();
			int flags = buffer.getInt();
			byte lowBidDenominator = buffer.get();
			long lowBidPrice = buffer.getLong();
			byte highAskDenominator = buffer.get();
			long highAskPrice = buffer.getLong();
			priceRangeIndicatorWriter.write(symbolLocateMap.get(locate));
			priceRangeIndicatorWriter.write(marketCenterLocateMap.get(mcLocate));
			String type = RecorderHelper.priceRangeIndicationType.get(indicationType);
			if(type == null) NTPLogger.warning("No Mapping found for price Range Indication type " + indicationType);
			priceRangeIndicatorWriter.write("" + indicationType);
			if(flags != 0x00) NTPLogger.warning("No Mapping found for price Range Indication flags " + flags);
			priceRangeIndicatorWriter.write("" + flags);
			priceRangeIndicatorWriter.write(setDecimal("" + lowBidPrice, lowBidDenominator));
			priceRangeIndicatorWriter.write(setDecimal("" + highAskPrice, highAskDenominator));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			String val = null;
			for(Byte b : appMap.keySet())
			{
				priceRangeIndicatorWriter.write(Integer.toHexString(b));

				switch (b) {
				case 0x52:
					val = RecorderHelper.luldPriceBandIndicator.get(appMap.get(b));
					if(val == null) NTPLogger.warning("No Mapping found for LULD price BAnd Indication " + appMap.get(b));
					val = appMap.get(b);
					break;
				case 0x55:
					val = appMap.get(b);
					try
					{
						long time = Long.parseLong(val);
						val = milliSecToTime(time);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					NTPLogger.warning("Did not mapped Appandage code " + Integer.toHexString(b));
					val = appMap.get(b);
					break;
				}
				priceRangeIndicatorWriter.write(val);
			}
			priceRangeIndicatorWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseInstrumentStatus(ByteBuffer buffer)
	{
		if(!processingFlags.contains("90"))
			return;
		try
		{
			parseHeader(buffer, instrumentStatusWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			byte statusType = buffer.get();
			byte statusCode = buffer.get();
			byte reasonCode = buffer.get();
			byte statusFlag =  buffer.get();
			byte length = buffer.get();
			String reasonDetail = "";
			if(length > 0)
			{
				byte[] bytes = new byte[length];
				buffer.get(bytes);
				reasonDetail = new String(bytes);
			}
			instrumentStatusWriter.write(symbolLocateMap.get(locate));
			instrumentStatusWriter.write(marketCenterLocateMap.get(mcLocate));
			String tmp = null;
			if(statusType == 0x01)
			{
				tmp = RecorderHelper.statusType.get(statusType);
				if(tmp == null) NTPLogger.warning("No Mapping found for status type " + statusType);
				instrumentStatusWriter.write(tmp);
				tmp = RecorderHelper.statusCodeTrading.get(statusCode);
				if(tmp == null) NTPLogger.warning("No Mapping found for Trading status code " + statusCode);
				instrumentStatusWriter.write(tmp);
				tmp = RecorderHelper.reasonCodeTrading.get(reasonCode);
				if(tmp == null) NTPLogger.warning("No Mapping found for Trading reason code " + reasonCode);
				instrumentStatusWriter.write(tmp);
				tmp = RecorderHelper.statusFlagTrading.get(statusFlag);
				if(tmp == null) NTPLogger.warning("No Mapping found for Trading status flag " + statusFlag);
				instrumentStatusWriter.write(tmp);
			}
			else
			{
				tmp = RecorderHelper.statusType.get(statusType);
				if(tmp == null) NTPLogger.warning("No Mapping found for status type " + statusType);
				instrumentStatusWriter.write(tmp);
				tmp = RecorderHelper.statusCodeSHO.get(statusCode);
				if(tmp == null) NTPLogger.warning("No Mapping found for SHO status code " + statusCode);
				instrumentStatusWriter.write(tmp);
				tmp = RecorderHelper.reasonCodeSHO.get(reasonCode);
				if(tmp == null) NTPLogger.warning("No Mapping found for SHO reason code " + reasonCode);
				instrumentStatusWriter.write(tmp);
				if(statusFlag != 0x00) NTPLogger.warning("No Mapping found for SHO status flag " + statusFlag);
				instrumentStatusWriter.write("");
			}			
			instrumentStatusWriter.write(reasonDetail);
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				instrumentStatusWriter.write(Integer.toHexString(b));
				instrumentStatusWriter.write(appMap.get(b));
			}
			instrumentStatusWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseImbalanceInformation(ByteBuffer buffer)
	{
		if(!processingFlags.contains("92"))
			return;
		try
		{
			parseHeader(buffer, imbalanceInfoWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			byte imbalanceType = buffer.get();
			byte imbalanceSide = buffer.get();
			long imbalanceVolume = buffer.getLong();
			imbalanceInfoWriter.write(symbolLocateMap.get(locate));
			imbalanceInfoWriter.write(marketCenterLocateMap.get(mcLocate));
			String tmp = null;
			tmp = RecorderHelper.imbalanceType.get(imbalanceType);
			if(tmp == null) NTPLogger.warning("No Mapping found for imbalance type " + imbalanceType);
			imbalanceInfoWriter.write(tmp);
			if(imbalanceSide == 'B')
				imbalanceInfoWriter.write("Buy");
			else if(imbalanceSide == 'S')
				imbalanceInfoWriter.write("Sell");
			else
			{
				NTPLogger.warning("No Mapping found for imbalance side " + imbalanceSide);
				imbalanceInfoWriter.write("");
			}
			imbalanceInfoWriter.write("" + imbalanceVolume);
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				imbalanceInfoWriter.write(Integer.toHexString(b));
				imbalanceInfoWriter.write(appMap.get(b));
			}
			imbalanceInfoWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	private void parseAdminText(ByteBuffer buffer)
	{
		if(!processingFlags.contains("b2"))
			return;
		try
		{
			parseHeader(buffer, adminTextWriter);
			short length = buffer.getShort();
			byte[] bytes = new byte[length];
			buffer.get(bytes);
			String text = new String(bytes);
			adminTextWriter.write(text);
			adminTextWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseChannelEvent(ByteBuffer buffer)
	{
		if(!processingFlags.contains("b0"))
			return;
		try
		{
			parseHeader(buffer, channelEventWriter);
			byte[] bytes = new byte[4];
			buffer.get(bytes);
			String eventCode = new String(bytes);
			String mappedCode = RecorderHelper.channelEventMap.get(eventCode);
			if(mappedCode == null)
				NTPLogger.warning("Missing channel Event 4.7 mapping for " + eventCode);
			short mcLoc = buffer.getShort();
			String mc =  mcLoc!= 0? marketCenterLocateMap.get(mcLoc): "N/A";
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			channelEventWriter.write(mappedCode);
			channelEventWriter.write(mc);
			for(Byte b : appMap.keySet())
			{
				channelEventWriter.write(Integer.toHexString(b));
				channelEventWriter.write(appMap.get(b));
			}
			channelEventWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseMarketMakerStatus(ByteBuffer buffer)
	{
		if(!processingFlags.contains("b1"))
			return;
		try
		{
			parseHeader(buffer, marketMakerStatusWriter);
			int locate = buffer.getInt();
			short mcLocate = buffer.getShort();
			short mmLocate = buffer.getShort();
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			marketMakerStatusWriter.write(symbolLocateMap.get(locate));
			marketMakerStatusWriter.write(marketCenterLocateMap.get(mcLocate));
			marketMakerStatusWriter.write(marketMakerLocateMap.get(mmLocate));
			for(Byte b : appMap.keySet())
			{
				marketMakerStatusWriter.write(Integer.toHexString(b));
				marketMakerStatusWriter.write(appMap.get(b));
			}
			marketMakerStatusWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseAlternateSymbol(ByteBuffer buffer)
	{
		if(!processingFlags.contains("c0"))	//This is c1, but clubbed with Meta
			return;
		try
		{
			int locate = buffer.getInt();
			byte symbolType = buffer.get();
			byte length = buffer.get();
			byte[] bytes = new byte[length];
			buffer.get(bytes);
			String alternateSymbol = new String(bytes);
			HashMap<Byte, String> metaMap = metaValueMap.get(locate);
			if(metaMap == null)
			{
				metaMap = new HashMap<>();
				metaValueMap.put(locate, metaMap);
			}
			metaType.add((byte) 0xf9);
			metaType.add((byte) 0xfa);
			String type = RecorderHelper.symbologyType.get(symbolType);
			if(type == null) NTPLogger.warning("Missing Mapping for Symbology type 4.57 " + Integer.toHexString(symbolType));
			metaMap.put((byte)0xf9, type);
			metaMap.put((byte) 0xfa, alternateSymbol);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void parseOptionDeliveryComponent(ByteBuffer buffer)
	{
		if(!processingFlags.contains("c3"))
			return;
		try
		{
			int rootCodeLocate = buffer.getInt();
			int componentIndex = buffer.getInt();
			int componentTotal = buffer.getInt();
			int deliverableUnits = buffer.getInt();
			byte settlementMethod = buffer.get();
			byte fixedAmontDenominator = buffer.get();
			long fixedAmountNumerator = buffer.getLong();
			byte[] bytes = new byte[3];
			buffer.get(bytes);
			String currencyCode = new String(bytes);
			short strikePercent = buffer.getShort();
			int componentSymbolLocate = buffer.getInt();
			optionDeliveryWriter.write(symbolLocateMap.get(rootCodeLocate));
			optionDeliveryWriter.write("" + componentIndex);
			optionDeliveryWriter.write("" + componentTotal);
			optionDeliveryWriter.write("" + deliverableUnits);
			optionDeliveryWriter.write("" + settlementMethod);
			if(fixedAmontDenominator != 0)
				optionDeliveryWriter.write("" + (fixedAmountNumerator / fixedAmontDenominator));
			else
				optionDeliveryWriter.write("0");
			optionDeliveryWriter.write(currencyCode);
			optionDeliveryWriter.write(setDecimal("" + strikePercent, 2));
			if(componentSymbolLocate != 0)
				optionDeliveryWriter.write(symbolLocateMap.get(componentSymbolLocate));
			else
				optionDeliveryWriter.write("0");
			optionDeliveryWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseCombinationLeg(ByteBuffer buffer)
	{
		if(!processingFlags.contains("c5"))
			return;
		try
		{
			byte protocol = buffer.get();
			int locate = buffer.getInt();
			int legLocate = buffer.getInt();
			byte side = buffer.get();
			byte ratioDen = buffer.get();
			long ratio = buffer.getLong();
			combinationLegWriter.write("" + protocol);
			combinationLegWriter.write(symbolLocateMap.get(locate));
			combinationLegWriter.write(symbolLocateMap.get(legLocate));
			if(side == 'B')
				combinationLegWriter.write("Buy");
			else
				combinationLegWriter.write("Sell");
			combinationLegWriter.write(setDecimal("" + ratio, ratioDen));
			HashMap<Byte, String> appMap = parseAppendageElements(buffer);
			for(Byte b : appMap.keySet())
			{
				combinationLegWriter.write(Integer.toHexString(b));
				combinationLegWriter.write(appMap.get(b));
			}
			combinationLegWriter.endRecord();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void parseMetaData(ByteBuffer buffer)
	{
		int locate = buffer.getInt();
		HashMap<Byte, String> metaMap = metaValueMap.get(locate);
		if(metaMap == null)
		{
			metaMap = new HashMap<>();
			metaValueMap.put(locate, metaMap);
		}
		HashMap<Byte, String> appMap = parseAppendageElements(buffer);
		if(appMap != null && appMap.size() > 0)
		{
			metaType.addAll(appMap.keySet());
			metaMap.putAll(appMap);
		}
	}

	private String nanoSecToTime(long timestamp)
	{
		long nanoSec = timestamp % 1000000000;
		long remainder = timestamp / 1000000000;
		long sec = remainder % 60;
		remainder = remainder / 60;
		long min = remainder % 60;
		long hours = remainder / 60;
		long time = (hours *10000) + (min*100) + sec;
		return time + "." + nanoSec;
	}
	private String milliSecToTime(long timestamp)
	{
		long nanoSec = timestamp % 1000;
		long remainder = timestamp / 1000;
		long sec = remainder % 60;
		remainder = remainder / 60;
		long min = remainder % 60;
		long hours = remainder / 60;
		long time = (hours *10000) + (min*100) + sec;
		return time + "." + nanoSec;
	}
	private String setDecimal(String value, int place)
	{
		StringBuffer sb = new StringBuffer();
		//		byte[] arr = value.getBytes();
		int size = value.length();
		if(place >= size)
		{
			sb.append("0.");
			while(place > size){
				sb.append('0');
				place--;
			}
			sb.append(value);
			return sb.toString();
		}
		int t = size-place;
		sb.append(value.substring(0, t));
		sb.append('.');
		sb.append(value.substring(t));
		return sb.toString();
	}
	private HashMap<Byte, String> parseAppendageElements(ByteBuffer buffer)
	{
		HashMap<Byte, String> map  = new HashMap<>();	
		byte typeCode = 0;
		String val = null;
		while(buffer.hasRemaining())
		{
			byte length = buffer.get();
			byte code = buffer.get();
			switch (code) {
			case 1:	//Decimal Short		
				typeCode = buffer.get();
				short value = buffer.getShort();
				val = setDecimal("" + value, 2);
				map.put(typeCode, val);
				//				System.out.println("APD " + typeCode + " " + value + " " + length);
				break;
			case 2:	//Decimal Long	
				typeCode = buffer.get();
				int l = buffer.getInt();
				val = setDecimal("" + l, 4);
				map.put(typeCode, val);
				//				System.out.println("APD " + typeCode + " " + l + " " + length);
				break;
			case 3:	//Decimal Extended		
				typeCode = buffer.get();
				byte den = buffer.get();
				long num = buffer.getLong();
				val = setDecimal("" + num, den);
				map.put(typeCode, val);
				//				System.out.println("APD " + typeCode + " " + den + " " + num + " " + length + " " + code);
				break;
			case 7:	//Byte Value			
				typeCode = buffer.get();
				byte num1 = buffer.get();
				map.put(typeCode, "" + num1);
				//				System.out.println("APD " + typeCode + " " + num1 + " " + length);
				break;
			case 8:	//Short Value			
				typeCode = buffer.get();
				short sht = buffer.getShort();
				map.put(typeCode, "" + sht);
				//				System.out.println("APD " + typeCode + " " + sht + " " + length);
				break;
			case 9:	//Int32 Value			
				typeCode = buffer.get();
				int numint = buffer.getInt();
				map.put(typeCode, "" + numint);
				//				System.out.println("APD " + typeCode + " " + numint + " " + length);
				break;
			case 10:	//Int64 Value		
				typeCode = buffer.get();
				long ln = buffer.getLong();
				map.put(typeCode, "" + ln);
				//				System.out.println("APD " + typeCode + " " + ln + " " + length);
				break;
			case 15:	//String
				typeCode = buffer.get();
				int len2Com = length;
				if(length < 0)
					len2Com = 256 + length;
				byte[] bytes = new byte[len2Com];
				buffer.get(bytes);
				map.put(typeCode, new String(bytes));
				//				
				break;
			case 16:	//Date		
				typeCode = buffer.get();
				byte month = buffer.get();
				byte day = buffer.get();
				short year = buffer.getShort();
				long date = (year * 10000) + (month * 100) + day;
				val = "" +date;
				map.put(typeCode, val);
				//				System.out.println("APD " + typeCode + " " + month + " " + day + " " + year + " " + length);
				break;
			case 20:	//Fractional Price
				typeCode = buffer.get();
				byte den1 = buffer.get();
				long num2 = buffer.getLong();
				//				int frac = buffer.getInt();
				val = setDecimal("" + num2, den1);
				//				System.out.println("APD " + typeCode + " " + den1 + " " + num2 + " " + frac + " " + length);
				break;
			case 21:	//Boolean Value	
				typeCode = buffer.get();
				byte bol = buffer.get();
				val = bol == 0x01 ? "1" : "0";
				map.put(typeCode, val);
				//				System.out.println("APD " + typeCode + " " + bol + " " + length);
				break;
			case 22:	//Char Value	
				typeCode = buffer.get();
				byte chr = buffer.get();
				map.put(typeCode, "" + chr);
				//				System.out.println("APD " + typeCode + " " + chr + " " + length);
				break;
			default:
				System.out.print("APD ERROR " + length + " " + code + " ");
				displayByteMessage(buffer);
				break;
			}
		}
		return map;
	}

	public static void main(String[] args) {
		NTPLogger.info("Process started");
		String filename = null;
		try
		{
			bufferSize = Long.parseLong(CPDProperty.getInstance().getProperty("BUFFER_SIZE"));
			NTPLogger.info("File read Buffer size " + bufferSize);
		}
		catch(Exception e)
		{
			NTPLogger.missingProperty("BUFFER_SIZE");
			bufferSize = 1024*1024;
			NTPLogger.defaultSetting("BUFFER_SIZE", "" + bufferSize);
		}
		destinationDir = CPDProperty.getInstance().getProperty("DESTINATION_DIR");
		if(destinationDir == null)
		{
			NTPLogger.missingProperty("DESTINATION_DIR");
			destinationDir = "/home/uf3/datafiles";
			NTPLogger.defaultSetting("DESTINATION_DIR", destinationDir);
		}
		rawFileDir = CPDProperty.getInstance().getProperty("RAW_FILE_DIR");
		if(rawFileDir == null)
		{
			NTPLogger.missingProperty("RAW_FILE_DIR");
			rawFileDir = "/data6/uf3/UDP";
			NTPLogger.defaultSetting("RAW_FILE_DIR", rawFileDir);
		}
		tradeDate = CPDProperty.getInstance().getProperty("TRADE_DATE");
		if(tradeDate == null)
		{
			tradeDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
			NTPLogger.defaultSetting("TRADE_DATE", tradeDate);
		}
		String fileDate  = new SimpleDateFormat("yyyyMMdd_HH").format(new Date());
		destinationDir = destinationDir + "_" + fileDate;
		File file = new File(destinationDir);
		if(!file.exists())
			file.mkdirs();
		if(args.length == 0)
		{
			NTPLogger.error("Please pass FileType and flags as 2 arguments. Exiting system for now");
			System.exit(-1);
		}
		if(args.length == 1)
		{
			NTPLogger.error("Please flags as 2nd arguments. Exiting system for now");
			System.exit(-1);
		}
		fileType = args[0];
		if(fileType == null || fileType.length() == 1)
		{
			NTPLogger.error("Did not specified filetype as first argument. Exiting system for now.");
			System.exit(-1);
		}
		filename = rawFileDir + /*"_" + tradeDate +*/ "/" + fileType + ".mold64";
		NTPLogger.info("File to be parsed " + filename);
		file = new File(filename);
		if(!file.exists())
		{
			NTPLogger.error("File does not exist on path " + filename + ". Please check. Exiting system for now");
			System.exit(-1);
		}
		String flags = args[1];
		if(flags != null)
		{
			String[] arr = flags.split(",");
			for(String s :arr)
				processingFlags.add(s);
			if(processingFlags.size() == 0)
			{
				NTPLogger.error("Please provide correct flags as second argument. Exiting system for now");
				System.exit(-1);
			}
		}
		ReadRecorderFile recorder = new ReadRecorderFile(filename);
		recorder.startProcessing();
	}
}
