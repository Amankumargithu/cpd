package ntp.tsqdb.writer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.EquityQuotesBean;
import ntp.bean.TsqDbSummaryBean;

public class TsqDbCache {

	private static Map<String, EquityQuotesBean> cacheMap = new ConcurrentHashMap<String, EquityQuotesBean>();
	private static ConcurrentHashMap<String,String> tickersList = new ConcurrentHashMap<String,String>();
	private static ConcurrentHashMap<String,String> hashedTickers = new ConcurrentHashMap<String,String>();
	private static ConcurrentHashMap<String, Long> cancelSymbolTradeIdMap = new ConcurrentHashMap<String, Long>();
	private static ConcurrentHashMap<String, TsqDbSummaryBean> summaryMessagesMap = new ConcurrentHashMap<String, TsqDbSummaryBean>();

	public static EquityQuotesBean getCachedBBOData(String ticker) {
		EquityQuotesBean cachedBean = cacheMap.get(ticker);
		if (cachedBean == null) {
			cachedBean = new EquityQuotesBean();
			cacheMap.put(ticker, cachedBean);
		}
		return cachedBean;
	}

	public static ConcurrentHashMap<String,String> getTickersList() {
		return tickersList;
	}

	/*public static ConcurrentHashMap<String, HashSet<Long>> getSymbolTradeIdMap() {
		return symbolTradeIdMap;
	}*/

	public static ConcurrentHashMap<String, Long> getCancelSymbolTradeIdMap() {
		return cancelSymbolTradeIdMap;
	}

	public static ConcurrentHashMap<String,String> getHashedTickers() {
		return hashedTickers;
	}

	public static ConcurrentHashMap<String, TsqDbSummaryBean> getSummaryMessagesMap() {
		return summaryMessagesMap;
	}
}