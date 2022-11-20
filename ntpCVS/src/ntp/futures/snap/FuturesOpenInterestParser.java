package ntp.futures.snap;

import java.util.concurrent.ConcurrentHashMap;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

import com.csvreader.CsvReader;

public class FuturesOpenInterestParser {

	private static volatile FuturesOpenInterestParser parser = new FuturesOpenInterestParser();
	private ConcurrentHashMap<String, String> openInterestMap = new ConcurrentHashMap<String, String>(); 

	private FuturesOpenInterestParser() {
		try 
		{
			String fileName = CPDProperty.getInstance().getProperty("OPEN_INTEREST_FILE");
			if(fileName == null)
			{
				NTPLogger.missingProperty("OPEN_INTEREST_FILE");
				fileName = "/home/futureOI/eod/OpenInterest.csv";
				NTPLogger.defaultSetting("OPEN_INTEREST_FILE", fileName);
			}
			NTPLogger.info("Open Interest File Name is " + fileName);
			CsvReader openInterestReader = new CsvReader(fileName);
			while(openInterestReader.readRecord())
				openInterestMap.put(openInterestReader.get(0), openInterestReader.get(1));
			openInterestReader.close();
			NTPLogger.info("OpenInterest: no of records are " + openInterestMap.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FuturesOpenInterestParser getInstance()
	{
		return parser;
	}

	public String getOpenInterest(String rootSymbol) 
	{
		String openInterest = openInterestMap.get(rootSymbol);
		return openInterest;
	}
}
