package com.b4utrade.tsq;

import com.b4utrade.bean.TSQCriteriaBean;
import com.b4utrade.bean.TSQBean;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;
import com.tacpoint.http.HttpConfiguration;
import com.tacpoint.http.HttpRequestExecutor12;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class OptionsTSQMessageSelector {

	private static OptionsTSQMessageSelector instance;

	private static Hashtable<String, String> HOLIDAY_MAP = new Hashtable<String, String>();
	private static String OPTIONS_URL = null;
	private static String OPTIONS_SYMBOLOGY_URL = null;
	private static String NUM_RECORDS = "50";
	private static String KEY_SYMBOL = "s=";
	private static String KEY_NUM_RECORDS = "&n=";
	private static String KEY_DATE = "&date=";
	private static String KEY_TYPE = "&type=";
	private static String KEY_COMMINGLE_BID_ASK = "&format=";
	private static String KEY_START_TIME = "&starttime=";
	private static String KEY_END_TIME = "&endtime=";
	private static String KEY_PREV = "&next=";
	private static String KEY_NEXT = "&prev=";
	private static String KEY_EXCHANGE = "&exch=";
	private static String KEY_MIN_TRADE_SIZE = "&minsize=";
	private static String KEY_MAX_TRADE_SIZE = "&maxsize=";
	private static String KEY_MIN_TRADE_PRICE = "&minprice=";
	private static String KEY_MAX_TRADE_PRICE = "&maxprice=";
	private static String COMMINGLE_BID_ASK = "combine";
	private static String COMMA = ",";
	private static int INDEX_TIME = 0;
	private static int INDEX_CONDITION = 1;
	private static int INDEX_REF_NUMBER = 2;
	private static int INDEX_TYPE1 = 3;
	private static int INDEX_EXCHANGE1 = 4;
	private static int INDEX_PRICE1 = 5;
	private static int INDEX_SIZE1 = 6;
	private static int INDEX_BID_PRICE = 7;
	private static int INDEX_ASK_PRICE = 8;
	private static int INDEX_BID_SIZE = 9;
	private static int INDEX_ASK_SIZE = 10;
	private static int INDEX_BID_EXCHANGE = 11;
	private static int INDEX_ASK_EXCHANGE = 12;
	private static int INDEX_TRADE_ID = 15;
	private static int INDEX_Underlying_Bid = 16;
	private static int INDEX_Underlying_Ask = 17;
	private static int INDEX_Underlying_Bid_Size = 18;
	private static int INDEX_Underlying_Ask_Size = 19;
	private static int INDEX_Underlying_Bid_Exchange = 20;
	private static int INDEX_Underlying_Ask_Exchange = 21;
	private static String TYPE_TRADE = "1";
	private static String TYPE_BID = "8";
	private static String TYPE_ASK = "9";
	private static String TYPE_BEST_BID = "13";
	private static String TYPE_BEST_ASK = "14";
	private static Map<String, String> monthMapping;
	private static HashMap<String, String> scottradeMonthMapping;
	private static char minCall = 'A';
	private static char maxCall = 'L';
	private static char minPut = 'M';
	private static char maxPut = 'X';
	private static Object lock = new Object();

	private OptionsTSQMessageSelector() {
		init();
	}

	public static void printResults(List<TSQBean> results) {
		int i = 0;
		for (TSQBean tsqbean : results) {
			StringBuffer sb = new StringBuffer();
			sb.append("Trade/Quote Time : " + tsqbean.getTradeQuoteTime() + " message type : "
					+ tsqbean.getMessageType());
			if ((tsqbean.getMessageType() != null) && tsqbean.getMessageType().equals(TSQBean.TYPE_TRADE)) {
				sb.append(" Trade Market Center : " + tsqbean.getTradeMarketCenter());
				sb.append(" Condition Code : " + tsqbean.getTradeQuoteCondCode1());
				sb.append(" Trade Price : " + tsqbean.getTradePrice());
				sb.append(" Trade Size : " + tsqbean.getTradeSize());
				sb.append(" Und Ask Price : " + tsqbean.getUnderlyingAskPrice());
				sb.append(" Und Bid Price : " + tsqbean.getUnderlyingBidPrice());
				sb.append(" Und Ask Size : " + tsqbean.getUnderlyingAskSize());
				sb.append(" Und Bid Size : " + tsqbean.getUnderlyingBidSize());
				sb.append(" Und Ask Exch : " + tsqbean.getUnderlyingAskExchange());
				sb.append(" Und Bid Exch : " + tsqbean.getUnderlyingBidExchnage());
			} else {
				sb.append(" Bid Market Center : " + tsqbean.getBidMarketCenter());
				sb.append(" Bid Price : " + tsqbean.getBidPrice());
				sb.append(" Bid Size : " + tsqbean.getBidSize());
				sb.append(" Ask Market Center : " + tsqbean.getAskMarketCenter());
				sb.append(" Ask Price : " + tsqbean.getAskPrice());
				sb.append(" Ask Size : " + tsqbean.getAskSize());
			}
			System.out.println("TSQBean[" + (i++) + "] - " + sb.toString());
		}
	}

	private static synchronized void init() {
		monthMapping = new HashMap<String, String>();
		monthMapping.put("A", "01"); // call month jan
		monthMapping.put("M", "01"); // put month jan ... so on
		monthMapping.put("B", "02");
		monthMapping.put("N", "02");
		monthMapping.put("C", "03");
		monthMapping.put("O", "03");
		monthMapping.put("D", "04");
		monthMapping.put("P", "04");
		monthMapping.put("E", "05");
		monthMapping.put("Q", "05");
		monthMapping.put("F", "06");
		monthMapping.put("R", "06");
		monthMapping.put("G", "07");
		monthMapping.put("S", "07");
		monthMapping.put("H", "08");
		monthMapping.put("T", "08");
		monthMapping.put("I", "09");
		monthMapping.put("U", "09");
		monthMapping.put("J", "10");
		monthMapping.put("V", "10");
		monthMapping.put("K", "11");
		monthMapping.put("W", "11");
		monthMapping.put("L", "12");
		monthMapping.put("X", "12");
		scottradeMonthMapping = new HashMap<String, String>();
		scottradeMonthMapping.put("JAN", "01"); // call month jan
		scottradeMonthMapping.put("FEB", "02"); // put month jan ... so on
		scottradeMonthMapping.put("MAR", "03");
		scottradeMonthMapping.put("APR", "04");
		scottradeMonthMapping.put("MAY", "05");
		scottradeMonthMapping.put("JUN", "06");
		scottradeMonthMapping.put("JUL", "07");
		scottradeMonthMapping.put("AUG", "08");
		scottradeMonthMapping.put("SEP", "09");
		scottradeMonthMapping.put("OCT", "10");
		scottradeMonthMapping.put("NOV", "11");
		scottradeMonthMapping.put("DEC", "12");
		try {
			Logger.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Environment.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		OPTIONS_URL = Environment.get("OPTIONS_URL");
		OPTIONS_SYMBOLOGY_URL = Environment.get("OPTIONS_SYMBOLOGY_URL");
		NUM_RECORDS = Environment.get("NUM_RECORDS");
		String holidays = Environment.get("TRADING_HOLIDAYS");
		if (holidays == null)
			holidays = "";
		StringTokenizer st = new StringTokenizer(holidays, ",");
		while (st.hasMoreTokens()) {
			String holiday = st.nextToken();
			Logger.log("OptionsTSQMessageSelector holiday : " + holiday);
			HOLIDAY_MAP.put(holiday, holiday);
		}
	}

	private static void printFilterCriteria(TSQCriteriaBean criteria, String tradingDate, String convertedSymbol) {
		Logger.log("############### BEGIN OPTIONS MESSAGE SELECTOR CRITERIA ###################");
		Logger.log("TSQCriteriaBean.symbol            : " + criteria.getSymbol());
		Logger.log("TSQCriteriaBean.converted symbol  : " + convertedSymbol);
		Logger.log("TSQCriteriaBean.message seq time  : " + criteria.getFromMessageSequenceTime());
		Logger.log("TSQCriteriaBean.message seq       : " + criteria.getFromMessageSequence());
		Logger.log("TSQCriteriaBean.(prev/next)       : " + criteria.getPageDirection());
		Logger.log("TSQCriteriaBean.day               : " + criteria.getDay());
		Logger.log("TSQCriteriaBean.query date        : " + tradingDate);
		Logger.log("TSQCriteriaBean.display           : " + criteria.getDisplay());
		Logger.log("TSQCriteriaBean.exchange          : " + criteria.getExchange());
		Logger.log("TSQCriteriaBean.from time         : " + criteria.getFromTime());
		Logger.log("TSQCriteriaBean.to time           : " + criteria.getToTime());
		Logger.log("TSQCriteriaBean.min price         : " + criteria.getFromPrice());
		Logger.log("TSQCriteriaBean.max price         : " + criteria.getToPrice());
		Logger.log("TSQCriteriaBean.min size          : " + criteria.getFromSize());
		Logger.log("TSQCriteriaBean.max size          : " + criteria.getToSize());
		Logger.log("############### END OPTIONS MESSAGE SELECTOR CRITERIA   ###################");
	}

	public static List<TSQBean> execute(TSQCriteriaBean criteria) {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new OptionsTSQMessageSelector();
				}
			}
		}
		String tradingDate = mapDayToDate(criteria.getDay());
		String symbol = criteria.getSymbol();
		if (!symbol.endsWith(".OP")) {
			symbol = convertToNewStandardOptionSymbology(criteria.getSymbol());
		} else {
			symbol = convertToStandardOptionSymbology(criteria.getSymbol());
		}
		printFilterCriteria(criteria, tradingDate, symbol);
		HttpConfiguration config = new HttpConfiguration();
		StringBuffer sb = new StringBuffer(200);
		if (!criteria.getSymbol().endsWith(".OP")) {
			sb.append(OPTIONS_SYMBOLOGY_URL);
		} else {
			sb.append(OPTIONS_URL);
		}
		sb.append(KEY_SYMBOL);
		sb.append(symbol);
		sb.append(KEY_DATE);
		sb.append(tradingDate);
		sb.append(KEY_NUM_RECORDS);
		sb.append(NUM_RECORDS);
		if (!criteria.getDisplay().equals(TSQCriteriaBean.TS)) {
			sb.append(KEY_COMMINGLE_BID_ASK);
			sb.append(COMMINGLE_BID_ASK);
			if (criteria.isBboOnly()) {
				sb.append(KEY_TYPE);
				sb.append(TYPE_BEST_ASK);
				sb.append(KEY_TYPE);
				sb.append(TYPE_BEST_BID);
				if (criteria.getDisplay().equals(TSQCriteriaBean.TSQ)) {
					sb.append(KEY_TYPE);
					sb.append(TYPE_TRADE);
				}
			} else if (criteria.getDisplay().equals(TSQCriteriaBean.TQ)) // need input type of quote, in case of TSQ
																			// they don't need this value
			{
				sb.append(KEY_TYPE);
				sb.append("quote");
			}
			if (criteria.getQuoteFromPrice() != null && criteria.getQuoteToPrice() != null) {
				sb.append(KEY_MIN_TRADE_PRICE);
				sb.append(criteria.getQuoteFromPrice());
				sb.append(KEY_MAX_TRADE_PRICE);
				sb.append(criteria.getQuoteToPrice());
			}
			if (criteria.getPageDirection() != null
					&& criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_FORWARD)) {
				sb.append(KEY_NEXT);
				sb.append(formatSequenceTime(criteria.getFromMessageSequenceTime()));
				sb.append(COMMA);
				sb.append(criteria.getFromMessageSequence());
				sb.append(COMMA);
				sb.append(criteria.getFromMessageType());
			}
			if (criteria.getPageDirection() != null
					&& criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_BACKWARD)) {
				sb.append(KEY_PREV);
				sb.append(formatSequenceTime(criteria.getFromMessageSequenceTime()));
				sb.append(COMMA);
				sb.append(criteria.getFromMessageSequence());
				sb.append(COMMA);
				sb.append(criteria.getFromMessageType());
			}
		} else {
			sb.append(KEY_TYPE);
			sb.append(TYPE_TRADE);
			if (criteria.getFromSize() != null) {
				sb.append(KEY_MIN_TRADE_SIZE);
				sb.append(criteria.getFromSize());
			}
			if (criteria.getToSize() != null) {
				sb.append(KEY_MAX_TRADE_SIZE);
				sb.append(criteria.getToSize());
			}
			if (criteria.getFromPrice() != null) {
				sb.append(KEY_MIN_TRADE_PRICE);
				sb.append(criteria.getFromPrice());
			}
			if (criteria.getToPrice() != null) {
				sb.append(KEY_MAX_TRADE_PRICE);
				sb.append(criteria.getToPrice());
			}
			if (criteria.getPageDirection() != null
					&& criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_FORWARD)) {
				sb.append(KEY_NEXT);
				sb.append(formatSequenceTime(criteria.getFromMessageSequenceTime()));
				sb.append(COMMA);
				sb.append(criteria.getFromMessageSequence());
				sb.append(COMMA);
				sb.append(TYPE_TRADE);
			}
			if (criteria.getPageDirection() != null
					&& criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_BACKWARD)) {
				sb.append(KEY_PREV);
				sb.append(formatSequenceTime(criteria.getFromMessageSequenceTime()));
				sb.append(COMMA);
				sb.append(criteria.getFromMessageSequence());
				sb.append(COMMA);
				sb.append(TYPE_TRADE);
			}
		}
		if (criteria.getFromTime() != null) {
			sb.append(KEY_START_TIME);
			sb.append(formatTime(criteria.getFromTime()));
		}
		if (criteria.getToTime() != null) {
			sb.append(KEY_END_TIME);
			sb.append(formatTime(criteria.getToTime()));
		}
		if (criteria.getExchange() != null) {
			sb.append(KEY_EXCHANGE);
			sb.append(criteria.getExchange().toUpperCase());
		}
		String url = sb.toString();
		config.setURL(url);
		System.out.println("URL: " + url);
		HttpRequestExecutor12 executor = new HttpRequestExecutor12();
		try {
			executor.execute(config);
		} catch (Exception e) {
			Logger.log("OptionsTSQMessageSelector - exception encountered : " + e.getMessage(), e);
			return null;
		}
		ArrayList<TSQBean> results = parseResults(executor.getResults(), symbol, tradingDate, criteria);
		return results;
	}

	private static ArrayList<TSQBean> parseResults(byte[] data, String symbol, String day, TSQCriteriaBean criteria) {
		ArrayList<TSQBean> results = new ArrayList<TSQBean>();
		String rawData = new String(data);
		Logger.log("Raw Data : " + rawData);
		String[] splits = rawData.split("<br>");
		for (int i = 0; i < splits.length; i++) {
			try {
				String row = splits[i];
				Logger.log("Raw Data Row : " + row);
				String[] cols = row.split(",");
				TSQBean bean = new TSQBean();
				String type = cols[INDEX_TYPE1];
				if (type.equals(TYPE_TRADE) || type.equals(TYPE_BID) || type.equals(TYPE_ASK)
						|| type.equals(TYPE_BEST_BID) || type.equals(TYPE_BEST_ASK)) {
					bean.setTicker(symbol);
					bean.setCreationDateTime(parseDate(day));
					bean.setTradeQuoteCondCode1(cols[INDEX_CONDITION]);
					bean.setTradeQuoteTime(parseTime(cols[INDEX_TIME]));
					double price = Double.parseDouble(cols[INDEX_PRICE1]);
					int size = Integer.parseInt(cols[INDEX_SIZE1]);
					long msgSequence = Long.parseLong(cols[INDEX_REF_NUMBER]);
					bean.setMsgSequence(new Long(msgSequence));
					if (type.equals(TYPE_TRADE) && !criteria.getDisplay().equals(TSQCriteriaBean.TQ)) {
						bean.setMessageType(TSQBean.TYPE_TRADE);
						bean.setTradeMarketCenter(cols[INDEX_EXCHANGE1].toLowerCase());
						bean.setTradePrice(new Double(price));
						bean.setTradeSize(new Long(size));
						if (cols.length > 8) {
							bean.setBidPrice(Double.parseDouble(cols[INDEX_BID_PRICE]));
							bean.setAskPrice(Double.parseDouble(cols[INDEX_ASK_PRICE]));
							bean.setBidSize(Long.parseLong(cols[INDEX_BID_SIZE]));
							bean.setAskSize(Long.parseLong(cols[INDEX_ASK_SIZE]));
							bean.setBidMarketCenter(cols[INDEX_BID_EXCHANGE]);
							bean.setAskMarketCenter(cols[INDEX_ASK_EXCHANGE]);
							try {
								bean.setTradeSequence(Long.parseLong(cols[INDEX_TRADE_ID]));
								// System.out.println(Double.parseDouble(cols[INDEX_Underlying_Ask])+" und
								// ask");
								bean.setUnderlyingAskPrice(Double.parseDouble(cols[INDEX_Underlying_Ask]));
								bean.setUnderlyingBidPrice(Double.parseDouble(cols[INDEX_Underlying_Bid]));
								bean.setUnderlyingAskSize(Long.parseLong(cols[INDEX_Underlying_Ask_Size]));
								bean.setUnderlyingBidSize(Long.parseLong(cols[INDEX_Underlying_Bid_Size]));
								if (cols.length > INDEX_Underlying_Ask_Exchange) {
									String underlyingAskExchange = cols[INDEX_Underlying_Ask_Exchange];
									String underlyingBidExchange = cols[INDEX_Underlying_Bid_Exchange];
									if (underlyingAskExchange != null)
										bean.setUnderlyingAskExchange(underlyingAskExchange.toLowerCase());
									if (underlyingBidExchange != null)
										bean.setUnderlyingBidExchnage(underlyingBidExchange.toLowerCase());
								}
							} catch (Exception e) {
								System.out.println(cols[INDEX_TRADE_ID] + " " + cols[INDEX_Underlying_Ask]);
								e.printStackTrace();
							}
						}
						results.add(bean);
						continue;
					}
					if (type.equals(TYPE_BID) || type.equals(TYPE_ASK)) {
						bean.setMessageType(TSQBean.TYPE_REGIONAL_QUOTE);
					}
					if (type.equals(TYPE_BEST_BID) || type.equals(TYPE_BEST_ASK)) {
						bean.setMessageType(TSQBean.TYPE_COMPOSITE_QUOTE);
					}
					bean.setBidMarketCenter(cols[INDEX_EXCHANGE1].toLowerCase());
					bean.setBidPrice(new Double(price));
					bean.setBidSize(new Long(size));
					try {
						price = Double.parseDouble(cols[9]);
						size = Integer.parseInt(cols[10]);
						bean.setAskMarketCenter(cols[8].toLowerCase());
						bean.setAskPrice(new Double(price));
						bean.setAskSize(new Long(size));
					} catch (Exception e) {
						e.printStackTrace();
					}
					results.add(bean);
				}
			} catch (Exception e) {
				System.out
						.println("OptionsTSQMessageSelector.parseResults - exception encountered : " + e.getMessage());
				e.printStackTrace();
				Logger.log("OptionsTSQMessageSelector.parseResults - exception encountered : " + e.getMessage(), e);
			}
		}
		// finally, sort the messages so that they're in descending order!
		TSQTimeComparator comparator = new TSQTimeComparator();
		comparator.setDirection(TSQTimeComparator.DESCENDING);
		Collections.sort(results, comparator);
		return results;
	}

	private static Timestamp parseDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			Date now = sdf.parse(date);
			return new Timestamp(now.getTime());
		} catch (Exception e) {
			Logger.log("Exception encountered : " + e.getMessage(), e);
			return null;
		}
	}

	private static Integer parseTime(String time) {
		String[] hhmmss = time.split(":");
		// should have 3 elements ...
		if (hhmmss.length != 3)
			return new Integer(0);
		String millisecods = "000";
		// truncate the last 4 bytes ".nnn" ...
		if (hhmmss[2].indexOf(".") > 0) {
			millisecods = hhmmss[2].substring(hhmmss[2].indexOf(".") + 1);
			hhmmss[2] = hhmmss[2].substring(0, hhmmss[2].indexOf("."));
		}
		return new Integer(hhmmss[0] + hhmmss[1] + hhmmss[2] + millisecods);
	}

	private static String convertToStandardOptionSymbology(String symbol) {
		if (symbol == null || (!symbol.endsWith(".OP"))) {
			Logger.log("Incorrect symbol format : " + symbol);
			return symbol;
		}
		// remove the .OP extension and strike code (2 bytes)
		String temp = symbol.substring(0, symbol.length() - 5);
		// should have the root symbol now ...
		String standardSym = "." + temp + "%20" + symbol.substring(symbol.length() - 5, symbol.length() - 3);
		Logger.log("Old Symbol : " + symbol + " Converted Symbol : " + standardSym);
		return standardSym;
	}

	private static String convertToNewStandardOptionSymbology(String symbol) {
		if (symbol == null || (!symbol.startsWith("O:"))) {
			Logger.log("Incorrect symbol format : " + symbol);
			return symbol;
		}
		String standardSym = ".";
		symbol = symbol.toUpperCase().trim(); // manage small chars if any
		try {
			int indexOfDelimeter = symbol.indexOf("\\");
			standardSym += symbol.substring(2, indexOfDelimeter) + "%20";
			String year = symbol.substring(indexOfDelimeter + 1, (indexOfDelimeter + 3)); // 2 length year
			indexOfDelimeter = indexOfDelimeter + 3;
			standardSym += year;
			String monthAndCallPutIndicator = symbol.substring(indexOfDelimeter, (indexOfDelimeter + 1));
			indexOfDelimeter = indexOfDelimeter + 1;
			String month = (String) monthMapping.get(monthAndCallPutIndicator);
			if (month == null)
				throw new Exception("Month Indicator is invalid ");
			standardSym += month;
			String dayOfMonth = symbol.substring(indexOfDelimeter, (indexOfDelimeter + 2));
			standardSym += dayOfMonth;
			indexOfDelimeter = indexOfDelimeter + 2;
			char code = monthAndCallPutIndicator.charAt(0);
			if (code >= minCall && code <= maxCall)
				standardSym += "C";
			else if (code >= minPut && code <= maxPut)
				standardSym += "P";
			else
				throw new Exception("Call put indicator was wrong " + code);
			String strikePrice = symbol.substring(indexOfDelimeter + 1);
			String[] splitArr = strikePrice.split("\\.");
			String decimalPart = "000";
			if (splitArr.length > 1) {
				decimalPart = splitArr[1];
				if (decimalPart.length() > 3)
					decimalPart = decimalPart.substring(0, 3);
				else {
					while (decimalPart.length() < 3) {
						decimalPart = decimalPart + "0";
					}
				}
			}
//			System.out.println(splitArr[0] + " " + splitArr[1]);
//			int sp = (int)(Double.parseDouble(strikePrice)*1000);
//			standardSym += sp;
			standardSym += splitArr[0] + decimalPart;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.log("Incorrect symbol format : " + symbol + "  " + e.getMessage());
			return symbol;
		}
		return standardSym;
	}

	private static String mapDayToDate(Integer day) {
		GregorianCalendar gc = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int histTradingDay = 0;
		int currentTradingDay = 0;
		if (day == null || day.intValue() > 0) {
			day = new Integer(0);
		}
		// day requested will be zero or < 0 ... multiply by -1 to get abs value
		// for use in comparison.
		histTradingDay = day.intValue() * -1;
		while (true) {
			int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
				gc.add(Calendar.DAY_OF_MONTH, -1);
				continue;
			}
			// check to see if current day is a holiday ...
			String tradingDate = sdf.format(gc.getTime());
			if (HOLIDAY_MAP.containsKey(tradingDate)) {
				gc.add(Calendar.DAY_OF_MONTH, -1);
				continue;
			}
			if (currentTradingDay == histTradingDay) {
				Logger.log("OptionsTSQMessageSelector - trading day returned for [" + histTradingDay + "] : "
						+ tradingDate);
				return tradingDate;
			} else {
				currentTradingDay++;
				gc.add(Calendar.DAY_OF_MONTH, -1);
				continue;
			}
		}
	}

	private static String formatTime(String criteriaTime) {
		String defaultTime = "00:00:00";
		if (criteriaTime == null) {
			return defaultTime;
		}
		if (criteriaTime.length() > 6) {
			criteriaTime = criteriaTime.substring(0, criteriaTime.length() - 3);
		}
		StringBuffer formattedTime = new StringBuffer();
		if (criteriaTime.length() == 5) {
			formattedTime.append("0");
			formattedTime.append(criteriaTime.substring(0, 1));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(1, 3));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(3));
			return formattedTime.toString();
		}
		if (criteriaTime.length() == 6) {
			formattedTime.append(criteriaTime.substring(0, 2));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(2, 4));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(4));
			return formattedTime.toString();
		}
		return defaultTime;
	}

	private static String formatSequenceTime(String criteriaTime) {
		String defaultTime = "00:00:00.000";
		if (criteriaTime == null) {
			return defaultTime;
		}
		if (criteriaTime.length() <= 6) {
			criteriaTime = criteriaTime + "000";
		}
		StringBuffer formattedTime = new StringBuffer();
		if (criteriaTime.length() == 8) {
			formattedTime.append("0");
			formattedTime.append(criteriaTime.substring(0, 1));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(1, 3));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(3, 5));
			formattedTime.append(".");
			formattedTime.append(criteriaTime.substring(5));
			return formattedTime.toString();
		}
		if (criteriaTime.length() == 9) {
			formattedTime.append(criteriaTime.substring(0, 2));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(2, 4));
			formattedTime.append(":");
			formattedTime.append(criteriaTime.substring(4, 6));
			formattedTime.append(".");
			formattedTime.append(criteriaTime.substring(6));
			return formattedTime.toString();
		}
		return defaultTime;
	}
}
