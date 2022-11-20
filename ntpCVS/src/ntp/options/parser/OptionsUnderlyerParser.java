package ntp.options.parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.csvreader.CsvReader;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class OptionsUnderlyerParser {

	private static OptionsUnderlyerParser parser = new OptionsUnderlyerParser();

	public static OptionsUnderlyerParser getInstance() {
		return parser;
	}

	private ConcurrentMap<String, String> underlyerMap = new ConcurrentHashMap<>();

	private OptionsUnderlyerParser() {
		try {
			String fileName = CPDProperty.getInstance().getProperty("UNDERLYER_FILE");
			if (fileName == null) {
				NTPLogger.missingProperty("UNDERLYER_FILE");
				fileName = "/home/underlyer/underlyer.opt";
				NTPLogger.defaultSetting("UNDERLYER_FILE", fileName);
			}
			NTPLogger.info("Underlyer File Name is " + fileName);
			CsvReader underlyerReader = new CsvReader(fileName);
			while (underlyerReader.readRecord())
				underlyerMap.put(underlyerReader.get(0), underlyerReader.get(1));
			underlyerReader.close();
			NTPLogger.info("Underlyers: no of records are " + underlyerMap.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getUnderlyer(String rootSymbol) {
		String underlyer = underlyerMap.get(rootSymbol);
		if (underlyer == null)
			underlyer = rootSymbol;
		return underlyer;
	}
}
