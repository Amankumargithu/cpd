package ntp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import com.b4utrade.bean.InterestRateBean;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsQTMessageQueue;

public class OptionsUtility {
	private static OptionsUtility instance = new OptionsUtility();
	private static String optionSymbolPattern = "[O][\\:][A-Z0-9]+[\\\\][0-9]{2}[A-Z]{1}[0-9]{2}[\\\\][0-9]+[\\.]*[0-9]*";
	private final String INDICIES_IDENTIFIER = "I:";
	private OptionsQTMessageQueue cache = OptionsQTMessageQueue.getInstance();
	private static Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	private InterestRateBean irb = cache.getInterestRatebean();

	private OptionsUtility() {
	}

	public static OptionsUtility getInstance() {
		return instance;
	}

	public String getRootSymbol(String optionSymbol) {
		try {
			return optionSymbol.split("\\\\")[0].substring(2);
		} catch (Exception e) {
			NTPLogger.warning("Parsing root symbol for " + optionSymbol);
			e.printStackTrace();
		}
		return optionSymbol;
	}

	public String getUCFormattedTicker(String ticker) {
		int precisionIndex = ticker.indexOf('.');
		if (precisionIndex != -1) {
			String precision = ticker.substring(precisionIndex + 1);
			if (precision.length() == 1)
				ticker = ticker + "0";
			else if (precision.length() == 0)
				ticker = ticker + "00";
		} else {
			ticker = ticker + ".00";
		}
		return ticker;
	}

	public String getEQPlusFormattedTicker(String ticker) {
		try {

			int precisionIndex = ticker.indexOf('.');
			if (precisionIndex != -1) {
				String precision = ticker.substring(precisionIndex + 1);
				if (precision.length() == 2 && precision.endsWith("0"))
					ticker = ticker.substring(0, ticker.length() - 1);
				else if (precision.length() == 0)
					ticker = ticker + "0";
			} else {
				ticker = ticker + ".0";
			}
		} catch (Exception e) {
			NTPLogger.error("getEQPlusFormattedTicker" + e.getMessage() + " for ticker: " + ticker);
		}
		return ticker;
	}

	public boolean validateOptionSymbol(String ticker) {
		if (ticker.length() >= 32)
			return false;
		if (!ticker.matches(optionSymbolPattern))
			return false;
		return true;
	}

	public String formatRootTicker(String tkr) {
		// We have option chain for I:SPX and others
		if (tkr != null && tkr.startsWith(INDICIES_IDENTIFIER))
			return tkr.replace(INDICIES_IDENTIFIER, "");
		return tkr;
	}

	/**
	 * 
	 * @param nasdaqExchangeCode
	 * @return The equity plus code corresponding to NASDAQ exchange code
	 */
	public String getEquityPlusExchangeCode(String nasdaqExchangeCode) {
		if (nasdaqExchangeCode == null)
			return "  ";
		HashMap<String, String> exchgMap = OptionsExchangeMapPopulator.getInstance().getExchangeMap();
		if (nasdaqExchangeCode.startsWith("??"))
			nasdaqExchangeCode = "  ";
		if (exchgMap.containsKey(nasdaqExchangeCode))
			return exchgMap.get(nasdaqExchangeCode).toLowerCase();
		if (nasdaqExchangeCode.equals("")) {
			NTPLogger.error("exchnage code was empty string");
			return "  ";
		}
		return nasdaqExchangeCode;
	}

	public QTCPDMessageBean evaluateGreeks(String ticker, QTCPDMessageBean qtMessageBean) {
		SimpleDateFormat expirationformatter = new SimpleDateFormat("yyyyMMdd");
		String[] arr = ticker.split("\\\\");
		String rootTicker = arr[0].substring(2);
		String underlyingTicker = cache.getUnderlyer(rootTicker);
		double stockVolatility = cache.getVolatility(underlyingTicker);
//		stockVolatility = stockVolatility /100;
		double stockPrice = cache.getEquityLast(underlyingTicker);
//		System.out.println("Greeks " + ticker + " " + underlyingTicker + " " + stockPrice);
		String[] array = arr[2].split("/");
		double strikePrice = Double.parseDouble(array[0]);
		String tickerDate = arr[1];
		Integer year = Integer.parseInt(tickerDate.substring(0, 2)) + 2000;
		String month = "" + tickerDate.charAt(2);
		Integer day = Integer.parseInt(tickerDate.substring(3));
		boolean putFlag = false;
		Calendar expirationCalendar = Calendar.getInstance();
		Integer mn;
		if (PumpUtil.getCallMap(month) != null) {
			putFlag = false;
			mn = Integer.parseInt(PumpUtil.getCallMap(month));
			expirationCalendar.set(year, mn - 1, day);
		} else {
			putFlag = true;
			mn = Integer.parseInt(PumpUtil.getPutMap(month));
			expirationCalendar.set(year, mn - 1, day);
		}
		String expirationDate = expirationformatter.format(expirationCalendar.getTime());
		qtMessageBean.setExpirationDate("" + year + "" + mn + "" + day);
		try {
			expirationCalendar.setTime(expirationformatter.parse(expirationDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		double days = ((double) (expirationCalendar.getTime().getTime() - currentCal.getTime().getTime()) / 86400000);
		days = Math.ceil(days);
		if (days <= 0)
			days = 1.0;
		qtMessageBean.setDaysToExpire((int) days);
		double opExpTime = getTimeToExpire(expirationCalendar);
		double interestRate = 0d;
		if (opExpTime >= 1)
			interestRate = irb.get360DayRate();
		else if (opExpTime >= .5)
			interestRate = irb.get180DayRate();
		else if (opExpTime >= .25)
			interestRate = irb.get90DayRate();
		else
			interestRate = irb.get30DayRate();
		// interestRate = interestRate /100;
		// if(stockPrice != 0D && strikePrice != 0D && opExpTime != 0D && interestRate
		// != 0d && stockVolatility != 0d)
		BlackScholesCalculator bsc = new BlackScholesCalculator(stockPrice, strikePrice, opExpTime, interestRate,
				stockVolatility, putFlag);
		double lastPrice = qtMessageBean.getLastPrice();
		double askPrice = qtMessageBean.getAskPrice();
		double intrinsicValue = 0.0;
		if (lastPrice > 0)
			bsc.calculatePriceAndPartials();
		if (putFlag)
			intrinsicValue = (strikePrice - stockPrice < 0) ? 0 : (strikePrice - stockPrice);
		else
			intrinsicValue = (stockPrice - strikePrice < 0) ? 0 : (stockPrice - strikePrice);
		qtMessageBean.setIntrinsicValue(intrinsicValue);
		qtMessageBean.setTimev(askPrice - intrinsicValue);
		qtMessageBean.setDiff(askPrice - bsc.getOptionPrice());
		qtMessageBean.setTheoV(bsc.getOptionPrice());
		qtMessageBean.setDelta(bsc.getDelta());
		qtMessageBean.setGamma(bsc.getGamma());
		qtMessageBean.setTheta(bsc.getTheta());
		double prevIvol = 0d;
		if(qtMessageBean.getIvol() != null)
			prevIvol = qtMessageBean.getIvol();
		qtMessageBean.setIvol(BlackScholesCalculator.impliedVolatilityBisections(stockPrice, strikePrice, opExpTime,
				interestRate, stockVolatility, lastPrice, putFlag));
		if(qtMessageBean.getIvol() != null)
			qtMessageBean.setIvolChg(qtMessageBean.getIvol() - prevIvol);
		else
			qtMessageBean.setIvolChg(0d);

		qtMessageBean.setEquityLast(stockPrice);
		return qtMessageBean;
	}

	private double getTimeToExpire(Calendar expirationTime) {
		double daysLeft = ((double) (expirationTime.getTime().getTime() - currentCal.getTime().getTime()) / 86400000);
		if (daysLeft <= 0)
			daysLeft = (double) 1 / (365 * 24);
		else
			daysLeft /= 365;
		return daysLeft;
	}
}
