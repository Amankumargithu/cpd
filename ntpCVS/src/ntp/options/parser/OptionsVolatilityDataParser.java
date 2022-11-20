package ntp.options.parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.csvreader.CsvReader;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class OptionsVolatilityDataParser {

	private static volatile OptionsVolatilityDataParser parser = new OptionsVolatilityDataParser();

	public static OptionsVolatilityDataParser getInstance() {
		return parser;
	}

	private ConcurrentMap<String, Double> optionsVolatilityMap = new ConcurrentHashMap<>();

	private OptionsVolatilityDataParser() {
		try {
			String fileName = CPDProperty.getInstance().getProperty("VOLATILITY_FILE");
			if (fileName == null) {
				NTPLogger.missingProperty("VOLATILITY_FILE");
				fileName = "/home/spryware/dd7stock.txt";
				NTPLogger.defaultSetting("VOLATILITY_FILE", fileName);
			}
			NTPLogger.info("Volatility File Name is " + fileName);
			CsvReader optionsData = new CsvReader(fileName);
			optionsData.readHeaders();
			while (optionsData.readRecord()) {
				String optionTicker = optionsData.get("Ticker");
				String volatility = optionsData.get("20-Day HisVol");
				Double volatilityDouble = Double.parseDouble(volatility);
				volatilityDouble = volatilityDouble / 100;
				if (optionTicker.startsWith("$")) {
					optionTicker = optionTicker.replaceFirst("\\$", "I:");
				}
				optionsVolatilityMap.put(optionTicker, volatilityDouble);
			}
			optionsData.close();
			NTPLogger.info("Volatilities: no of records are " + optionsVolatilityMap.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getVolatility(String ticker) {
		try {
			if (optionsVolatilityMap.containsKey(ticker))
				return optionsVolatilityMap.get(ticker);

		} catch (Exception e) {
			NTPLogger.warning("No volatility for ticker " + ticker);
		}
		return 0.0;
	}
}
