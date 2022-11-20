package com.b4utrade.cache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bean.StockOptionBean;
import com.b4utrade.ejb.FutureDataHandler;
import com.b4utrade.ejb.OptionData;
import com.b4utrade.helper.CompanyReorgnizationHelper;
import com.b4utrade.helper.OTCDuallyQuotedSymbolHelper;
import com.b4utrade.helper.PinkSheetTickerDataHelper;
import com.b4utrade.helper.TickerExclusionHelper;
import com.tacpoint.objectpool.ObjectPoolManager;

public class SymbolUpdaterCache {

	private static Log log = LogFactory.getLog(SymbolUpdaterCache.class);
	private static SymbolUpdaterCache instance = new SymbolUpdaterCache();
	private static final String PINK_SHEET_IDENTIFIER = ".PK";
	private static final String MONTAGE_IDENTIFIER = "/";
	private static final int companyReorgDataInt = 1;
	private static final int tickerExclusionInt = 2;
	private static final int pinkSheetTickerInt = 3;
	private static final int otcDuallyQuotedTickerInt = 4;
	private static final int futuresCacheInt = 5;
	public static final String ALL = "ALL";
	public static final String TICKER_TYPE_EQUITY = "EQUITY";
	public static final String TICKER_TYPE_OPTIONS = "OPTIONS";
	public static final String TICKER_TYPE_FUTURES = "FUTURES";
	public static final String TICKER_TYPE_OPTIONS_MONTAGE = "OPREG";
	public static final String TICKER_TYPE_EQUITY_MONTAGE = "EQREG";
	public static final String TICKER_TYPE_OTC = "OTC";
	public static final String TICKER_TYPE_OTC_PINK = "PINK";
	public static final String TICKER_TYPE_DELAYED = "DELAYED";
	private LinkedHashMap<String, String> companyReorgDataMap;
	private List<String> tickerExclusionList;
	private List<String> pinkSheetTickerList;
	private List<String> otcDuallyQuotedTickerList;
	private HashMap<String, ArrayList<String>> futuresCache;
	private HashMap<String, String> futureExchangeCache = new HashMap<>();
	private ArrayList<String> volumeTickers = new ArrayList<>();
	private HashMap<String, String> monthMappingCall = null;
	private HashMap<String, String> monthMappingPut = null;

	public static SymbolUpdaterCache getInstance() {
		if (instance == null) {
			synchronized (SymbolUpdaterCache.class) {
				if (instance == null) {
					instance = new SymbolUpdaterCache();
				}
			}
		}
		return instance;
	}

	private SymbolUpdaterCache() {
		log.info("init");
		loadPinkSheetData();
		loadOTCDuallyQuotedData();
		loadTickerExclusionList();
		loadTickerReorganisationMap();
		loadFuturesSpotSymbolMap();
		initOptionMapping();
		initVolumeTickers();
	}

	private void loadPinkSheetData() {
		try {
			PinkSheetTickerDataHelper tickerDataHelper = PinkSheetTickerDataHelper.getInstance();
			pinkSheetTickerList = tickerDataHelper.getPinkSheetTickerList();
			if (pinkSheetTickerList == null)
				pinkSheetTickerList = new ArrayList<String>();
		} catch (Exception e) {
			log.error(new Timestamp(System.currentTimeMillis())
					+ "SymbolUpdaterCache Exception - fetching Pink Sheet Ticker List " + e.getMessage());
		}
	}

	private void loadOTCDuallyQuotedData() {
		try {
			OTCDuallyQuotedSymbolHelper duallyQuotedHelper = OTCDuallyQuotedSymbolHelper.getInstance();
			otcDuallyQuotedTickerList = duallyQuotedHelper.getOTCDuallyQuotedDataList();
			if (otcDuallyQuotedTickerList == null)
				otcDuallyQuotedTickerList = new ArrayList<String>();
		} catch (Exception e) {
			log.error(new Timestamp(System.currentTimeMillis())
					+ "SymbolUpdaterCache Exception - fetching OTC Dually Quoted Ticker List " + e.getMessage());
		}
	}

	private void loadTickerExclusionList() {
		try {
			TickerExclusionHelper tickerExclusionHelper = new TickerExclusionHelper();
			tickerExclusionList = tickerExclusionHelper.getTickerExclusionList();
			if (tickerExclusionList == null)
				tickerExclusionList = new ArrayList<>();
		} catch (Exception e) {
			log.error(new Timestamp(System.currentTimeMillis())
					+ "SymbolUpdaterCache Exception - fetching Ticker Exclusion Ticker List " + e.getMessage());
		}
	}

	private void loadTickerReorganisationMap() {
		try {
			CompanyReorgnizationHelper reorgnisationHelper = CompanyReorgnizationHelper.getInstance();
			companyReorgDataMap = reorgnisationHelper.getCompanyReorganiztionDataMap();
			if (companyReorgDataMap == null)
				companyReorgDataMap = new LinkedHashMap<String, String>();
		} catch (Exception e) {
			log.error(new Timestamp(System.currentTimeMillis())
					+ "SymbolUpdaterCache Exception - fetching Company Re-organisation Map " + e.getMessage());
		}
	}

	private void loadFuturesSpotSymbolMap() {
		try {
			ObjectPoolManager opm = ObjectPoolManager.getInstance();
			FutureDataHandler handler = (FutureDataHandler) opm.getObject("FutureDataHandler", 1000);
			if (handler != null) {
				OptionData od = handler.getRemoteInterface();
				if (od != null)
					futuresCache = od.getSpotSymbolMap();
				else
					log.error(new Timestamp(System.currentTimeMillis())
							+ " SymbolUpdaterCache.loadFuturesSpotSymbolMap - OptionData remote interface object is null.");
			} else
				log.error(new Timestamp(System.currentTimeMillis())
						+ " SymbolUpdaterCache.loadFuturesSpotSymbolMap - FutureDataHandler is null.");
			if (handler != null)
				opm.freeObject("FutureDataHandler", handler);
			if (futuresCache == null)
				futuresCache = new HashMap<String, ArrayList<String>>();
		} catch (Exception e) {
			log.error(new Timestamp(System.currentTimeMillis())
					+ "SymbolUpdaterCache Exception - fetching Future Sopt Symbol Map " + e.getMessage());
		}
	}

	public String getUpdatedTicker(String inTicker) {
		boolean isPinkSheetTicker = false;
		try {
			if (tickerExclusionList.contains(inTicker))
				return inTicker;
			String pksymbol = null;
			if (inTicker.endsWith(PINK_SHEET_IDENTIFIER)) {
				pksymbol = inTicker;
				if (pinkSheetTickerList.contains(pksymbol)) {
					isPinkSheetTicker = true;
				}
				inTicker = inTicker.substring(0, inTicker.indexOf(PINK_SHEET_IDENTIFIER));
			}
			List<String> list = new ArrayList<String>();
			Collection<String> collection = companyReorgDataMap.keySet();
			list.addAll(collection);
			String oldSymbol = inTicker;
			while (companyReorgDataMap.containsKey(inTicker)) {
				if ((list.indexOf(inTicker) < list.indexOf(oldSymbol)))
					break;
				oldSymbol = inTicker;
				inTicker = companyReorgDataMap.get(inTicker);
			}
		} catch (Exception exception) {
			System.out.println(new Timestamp(System.currentTimeMillis())
					+ " Exception in SymbolUpdaterCache.getUpdatedTicker " + exception.getMessage());
			exception.printStackTrace();
		}
		if (isPinkSheetTicker) {
			inTicker += PINK_SHEET_IDENTIFIER;
		}
		if ((pinkSheetTickerList.contains(inTicker)) && (!(inTicker.endsWith(PINK_SHEET_IDENTIFIER)))) {
			inTicker += PINK_SHEET_IDENTIFIER;
		}
		return inTicker;
	}

	public String getMappedSymbol(String inTicker) {
		if (hasSpace(inTicker)) {
			inTicker = mapOptionSymbol(inTicker);
		}
		if (inTicker.startsWith(".")) {
			inTicker = mapFuturesSymbol(inTicker);
		}
		return inTicker;
	}

	public void reloadCache(int cacheType) {
		// Used to reload cache in case any of them is updated in between market - avoid
		// re-start
		switch (cacheType) {
		case companyReorgDataInt:
			loadTickerReorganisationMap();
			break;
		case tickerExclusionInt:
			loadTickerExclusionList();
			break;
		case pinkSheetTickerInt:
			loadPinkSheetData();
			break;
		case otcDuallyQuotedTickerInt:
			loadOTCDuallyQuotedData();
			break;
		case futuresCacheInt:
			loadFuturesSpotSymbolMap();
			break;
		}
	}

	private String mapFuturesSymbol(String symbol) {
		ArrayList<String> list = getFuturesRealSymbol(symbol);
		Iterator<String> iterator = list.iterator();
		StringBuffer sb = new StringBuffer();
		while (iterator.hasNext()) {
			sb.append(",");
			sb.append(iterator.next());
		}
		symbol = sb.toString().substring(1);
		return symbol;
	}

	private String mapOptionSymbol(String symbol) {
		if (symbol.matches("[a-zA-Z0-9]+[ ][0-9]{4}[A-Z]{1}[0-9]+[\\.]*[0-9]*")) {
			symbol = convertStandardToOriginalOptionSymbol(symbol);
			return symbol;
		} else if (symbol.matches("[a-zA-Z0-9]+[ ][0-9]{6}[C|P][0-9]+[\\.]*[0-9]*")) {
			symbol = convertAlternateToOriginalOptionSymbol(symbol);
			return symbol;
		} else {
			return symbol;
		}
	}

	private String convertStandardToOriginalOptionSymbol(String symbol) {
		StringBuffer sb = new StringBuffer();
		int spaceIndex = symbol.indexOf(" ");
		sb.append("O:");
		sb.append(symbol.substring(0, spaceIndex));
		sb.append("\\");
		sb.append(symbol.substring(spaceIndex + 1, spaceIndex + 3));
		sb.append(symbol.substring(spaceIndex + 5, spaceIndex + 6));
		sb.append(symbol.substring(spaceIndex + 3, spaceIndex + 5));
		sb.append("\\");

		int decimalIndex = symbol.indexOf(".");
		if (decimalIndex == -1) {
			if (!isMontageSymbol(symbol)) {
				sb.append(symbol.substring(spaceIndex + 6, symbol.length()));
				sb.append(".0");
			} else {
				sb.append(symbol.substring(spaceIndex + 6, symbol.indexOf("/")));
				sb.append(".0");
				sb.append(symbol.substring(symbol.indexOf("/")));
			}
		} else {
			sb.append(symbol.substring(spaceIndex + 6, symbol.length()));
		}
		symbol = sb.toString();

		return symbol;
	}

	private String convertAlternateToOriginalOptionSymbol(String symbol) {
		StringBuffer sb = new StringBuffer();
		// MSQ 100220C26
		int spaceIndex = symbol.indexOf(" ");
		sb.append("O:");
		sb.append(symbol.substring(0, spaceIndex));
		sb.append("\\");
		sb.append(symbol.substring(spaceIndex + 1, spaceIndex + 3));
		String month = symbol.substring(spaceIndex + 3, spaceIndex + 5);

		boolean call = symbol.substring(spaceIndex + 7, spaceIndex + 8).equalsIgnoreCase("C");
		if (call) {
			String mappedMonth = (String) monthMappingCall.get(month);
			if (mappedMonth == null) {
				return symbol;
			}
			sb.append(mappedMonth);
		} else {
			String mappedMonth = (String) monthMappingPut.get(month);
			if (mappedMonth == null) {
				return symbol;
			}
			sb.append(mappedMonth);
		}

		sb.append(symbol.substring(spaceIndex + 5, spaceIndex + 7));
		sb.append("\\");
		int decimalIndex = symbol.indexOf(".");
		if (decimalIndex == -1) {
			if (!isMontageSymbol(symbol)) {
				sb.append(symbol.substring(spaceIndex + 8, symbol.length()));
				sb.append(".0");
			} else {
				sb.append(symbol.substring(spaceIndex + 8, symbol.indexOf("/")));
				sb.append(".0");
				sb.append(symbol.substring(symbol.indexOf("/")));
			}
		} else {
			sb.append(symbol.substring(spaceIndex + 8, symbol.length()));
		}
		symbol = sb.toString();
		return symbol;
	}

	private void initOptionMapping() {
		monthMappingCall = new HashMap<String, String>();
		monthMappingPut = new HashMap<String, String>();

		monthMappingCall.put("01", "A"); // call month jan
		monthMappingCall.put("02", "B");
		monthMappingCall.put("03", "C");
		monthMappingCall.put("04", "D");
		monthMappingCall.put("05", "E");
		monthMappingCall.put("06", "F");
		monthMappingCall.put("07", "G");
		monthMappingCall.put("08", "H");
		monthMappingCall.put("09", "I");
		monthMappingCall.put("10", "J");
		monthMappingCall.put("11", "K");
		monthMappingCall.put("12", "L");

		monthMappingPut.put("01", "M"); // put month jan ... so on
		monthMappingPut.put("02", "N");
		monthMappingPut.put("03", "O");
		monthMappingPut.put("04", "P");
		monthMappingPut.put("05", "Q");
		monthMappingPut.put("06", "R");
		monthMappingPut.put("07", "S");
		monthMappingPut.put("08", "T");
		monthMappingPut.put("09", "U");
		monthMappingPut.put("10", "V");
		monthMappingPut.put("11", "W");
		monthMappingPut.put("12", "X");
	}

	private void initVolumeTickers() {
		volumeTickers.add(".UVOLA");
		volumeTickers.add(".DVOLA");
		volumeTickers.add(".VOLA");
		volumeTickers.add(".NUVOL");
		volumeTickers.add(".NDVOL");
		volumeTickers.add(".NTVOL");
		volumeTickers.add(".NCVOL");
		volumeTickers.add(".UVOL");
		volumeTickers.add(".DVOL");
		volumeTickers.add(".VOL");
		volumeTickers.add(".VOLN");
		volumeTickers.add(".UVOLQ");
		volumeTickers.add(".DVOLQ");
		volumeTickers.add(".VOLQ");
		volumeTickers.add(".TRINQ");
		volumeTickers.add(".TICKQ");
		volumeTickers.add(".DECLQ");
		volumeTickers.add(".ADVQ");
		volumeTickers.add(".UCHGQ");
		volumeTickers.add(".TICKI");
		volumeTickers.add(".TRIN");
		volumeTickers.add(".TICK");
		volumeTickers.add(".DECL");
		volumeTickers.add(".ADV");
		volumeTickers.add(".UCHG");
		volumeTickers.add(".NTICK");
		volumeTickers.add(".NDEC");
		volumeTickers.add(".NADV");
		volumeTickers.add(".NCHG");
		volumeTickers.add(".DECLA");
		volumeTickers.add(".ADVA");
		volumeTickers.add(".UCHGA");
	}

	public boolean isVolTicker(String ticker) {
		if (volumeTickers.contains(ticker))
			return true;
		return false;
	}

	private boolean hasSpace(String symbol) {
		return symbol.trim().contains(" ");
	}

	private boolean isMontageSymbol(String ticker) {

		ticker = ticker.trim();
		if (ticker == null)
			return false;
		if (ticker.length() < 3)
			return false;
		if (!("" + ticker.charAt(ticker.length() - 2)).equalsIgnoreCase(MONTAGE_IDENTIFIER)) {

			return false;
		} else {
			return true;
		}

	}

	private ArrayList<String> getFuturesRealSymbol(String ticker) {

		ArrayList<String> list = new ArrayList<String>();
		list.add(ticker);
		if (futuresCache != null) {
			if (futuresCache.containsKey(ticker))
				list = futuresCache.get(ticker);
		}
		return (list);
	}

	public String getFuturesExchange(String inTicker) {

		if (inTicker == null || inTicker.length() == 0)
			return null;
		String exchange = null;
		String exch = futureExchangeCache.get(inTicker);
		if (exch == null) {
			exchange = loadFutureExchange(inTicker);
			if (exchange != null) {
				futureExchangeCache.put(inTicker, exchange);
			}
		} else {
			exchange = exch;
		}
		return exchange;
	}

	private String loadFutureExchange(String inTicker) {
		StockOptionBean sobean = null;
		try {
			if (inTicker == null) {
				inTicker = "N/A";
			}
			if (inTicker.startsWith("/")) {
				ObjectPoolManager opm = null;
				FutureDataHandler odh = null;
				opm = ObjectPoolManager.getInstance();
				odh = (FutureDataHandler) opm.getObject("FutureDataHandler", 1000);
				try {
					if (odh != null) {
						OptionData od = odh.getRemoteInterface();
						if (od != null) {
							sobean = od.getStockOption(inTicker.toUpperCase());
						} else {
							log.error("OptionData remote interface object is null.");
							throw new Exception("FutureData Remote interface is null.");
						}
					} else
						log.error("FutureDataHandler is null.");
				} catch (Exception exc) {
					log.error("Exception encountered while trying to get Option Data.", exc);
					if (odh != null)
						odh.init();
				}
				if (odh != null)
					opm.freeObject("FutureDataHandler", odh);
			} else {
				log.error("invalid ticker " + inTicker);
				return null;
			}
		} catch (Exception e) {
			String msg = "Unable to retrieve data.";
			log.error(msg, e);
		}
		if (sobean == null) {
			return null;
		}
		return sobean.getExchange();
	}
}
