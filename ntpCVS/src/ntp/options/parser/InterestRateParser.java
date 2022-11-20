package ntp.options.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

import com.b4utrade.bean.InterestRateBean;

public class InterestRateParser {

	private static final String BILL = "BILL";
	private static final String ONE_MONTH = "BILL 1MO";
	private static InterestRateParser parser = new InterestRateParser();
	private static final String SIX_MONTHS = "BILL 6MO";
	private static final String THREE_MONTHS = "BILL 3MO";
	private static final String TWELVE_MONTHS = "BILL 1YR";

	public static InterestRateParser getInstance() {
		return parser;
	}

	private InterestRateBean irb;

	private InterestRateParser() {
		Properties prop = new Properties();
		irb = new InterestRateBean();
		try {
			String fileName = CPDProperty.getInstance().getProperty("STREETSOFT_FILE");
			if (fileName == null) {
				NTPLogger.missingProperty("STREETSOFT_FILE");
				fileName = "/home/streetSoft/QUODD.CSV";
				NTPLogger.defaultSetting("STREETSOFT_FILE", fileName);
			}
			File file = new File(fileName);
			try (FileInputStream stream = new FileInputStream(file);) {
				prop.load(stream);
			}
			String billData = prop.getProperty(BILL);
			String[] billDataArray = billData.split("\\|");
			for (String s : billDataArray) {
				String[] monthArray = s.split(",");
				String issue = monthArray[0].trim();
				String rate = monthArray[2].trim();
				updateBean(issue, Double.parseDouble(rate) / 100);
			}
			NTPLogger.info("Interest Rate 30 days: " + irb.get30DayRate());
			NTPLogger.info("Interest Rate 90 days: " + irb.get90DayRate());
			NTPLogger.info("Interest Rate 180 days: " + irb.get180DayRate());
			NTPLogger.info("Interest Rate 360 days: " + irb.get360DayRate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InterestRateBean getInterestRateBean() {
		return irb;
	}

	private void updateBean(String issue, double rate) {
		switch (issue) {
		case ONE_MONTH:
			irb.set30DayRate(rate);
			break;
		case THREE_MONTHS:
			irb.set90DayRate(rate);
			break;
		case SIX_MONTHS:
			irb.set180DayRate(rate);
			break;
		case TWELVE_MONTHS:
			irb.set360DayRate(rate);
			break;
		}
	}
}
