package ntp.market.stat;

import java.util.ArrayList;

public class MarketStatTickerManager {

	public static final String SYMBOL_NYSE_UP_VOLUME = ".UVOL";
	public static final String SYMBOL_NYSE_DOWN_VOLUME = ".DVOL";
	public static final String SYMBOL_NYSE_TOTAL_VOLUME = ".VOL";
	public static final String SYMBOL_NYSE_TOTAL_VOLUME_1 = ".VOLN";
	public static final String SYMBOL_NYSE_UNCHANGED_ISSUE = ".UCHG";
	public static final String SYMBOL_NYSE_ADVANCING_ISSUE = ".ADV";
	public static final String SYMBOL_NYSE_DECLINING_ISSUE = ".DECL";
	public static final String SYMBOL_NYSE_ISSUES_UP_DOWN_RATIO = ".TICK";
	public static final String SYMBOL_NYSE_SHORT_TERM_TRADE_INDEX = ".TRIN";
	public static final String SYMBOL_NYSE_DOW_JONES_INDUSTRIAL_AVERAGE = ".TICKI";

	public static final String SYMBOL_NYSE_REGIONAL_UP_VOLUME = ".NUVOL";
	public static final String SYMBOL_NYSE_REGIONAL_DOWN_VOLUME = ".NDVOL";
	public static final String SYMBOL_NYSE_REGIONAL_TOTAL_VOLUME = ".NTVOL";
	public static final String SYMBOL_NYSE_REGIONAL_UNCHANGED_VOLUME = ".NCVOL";
	public static final String SYMBOL_NYSE_REGIONAL_UNCHANGED_ISSUE = ".NCHG";
	public static final String SYMBOL_NYSE_REGIONAL_ADVANCING_ISSUE = ".NADV";
	public static final String SYMBOL_NYSE_REGIONAL_DECLINING_ISSUE = ".NDEC";
	public static final String SYMBOL_NYSE_REGIONAL_ISSUES_UP_DOWN_RATIO = ".NTICK";

	public static final String SYMBOL_AMEX_UP_VOLUME = ".UVOLA";
	public static final String SYMBOL_AMEX_DOWN_VOLUME = ".DVOLA";
	public static final String SYMBOL_AMEX_TOTAL_VOLUME = ".VOLA";
	public static final String SYMBOL_AMEX_UNCHANGED_ISSUE = ".UCHGA";
	public static final String SYMBOL_AMEX_ADVANCING_ISSUE = ".ADVA";
	public static final String SYMBOL_AMEX_DECLINING_ISSUE = ".DECLA";

	public static final String SYMBOL_NASDAQ_UP_VOLUME = ".UVOLQ";
	public static final String SYMBOL_NASDAQ_DOWN_VOLUME = ".DVOLQ";
	public static final String SYMBOL_NASDAQ_TOTAL_VOLUME = ".VOLQ";
	public static final String SYMBOL_NASDAQ_UNCHANGED_ISSUE = ".UCHGQ";
	public static final String SYMBOL_NASDAQ_ADVANCING_ISSUE = ".ADVQ";
	public static final String SYMBOL_NASDAQ_DECLINING_ISSUE = ".DECLQ";
	public static final String SYMBOL_NASDAQ_ISSUES_UP_DOWN_RATIO = ".TICKQ";
	public static final String SYMBOL_NASDAQ_SHORT_TERM_TRADE_INDEX = ".TRINQ";

	private static ArrayList<String> djiaTickers = new ArrayList<>();

	static {
		djiaTickers.add("AA");
		djiaTickers.add("AXP");
		djiaTickers.add("T");
		djiaTickers.add("BAC");
		djiaTickers.add("BA");
		djiaTickers.add("CAT");
		djiaTickers.add("CVX");
		djiaTickers.add("C");
		djiaTickers.add("KO");
		djiaTickers.add("DD");
		djiaTickers.add("XOM");
		djiaTickers.add("GE");
		djiaTickers.add("GM");
		djiaTickers.add("HPQ");
		djiaTickers.add("HD");
		djiaTickers.add("INTC");
		djiaTickers.add("IBM");
		djiaTickers.add("JNJ");
		djiaTickers.add("JPM");
		djiaTickers.add("KFT");
		djiaTickers.add("MCD");
		djiaTickers.add("MRK");
		djiaTickers.add("MSFT");
		djiaTickers.add("MMM");
		djiaTickers.add("PFE");
		djiaTickers.add("PG");
		djiaTickers.add("UTX");
		djiaTickers.add("VZ");
		djiaTickers.add("WMT");
		djiaTickers.add("DIS");
	}

	public static boolean isDJIATicker(String tick) {
		return djiaTickers.contains(tick);
	}

	public static ArrayList<String> getDJIATickers() {
		return djiaTickers;
	}
}
