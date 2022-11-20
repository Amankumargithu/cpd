package ntp.marketScanner;

import java.util.concurrent.ConcurrentHashMap;

import com.b4utrade.bo.MarketScannerBO;

public class MarketScannerCache {

	private ConcurrentHashMap<String, MarketScannerBO> cacheMap = new ConcurrentHashMap<>();
	private static MarketScannerCache instance = new MarketScannerCache();

	private MarketScannerCache() {
	}

	public static MarketScannerCache getInstance() {
		if (instance == null)
			instance = new MarketScannerCache();
		return instance;
	}

	public ConcurrentHashMap<String, MarketScannerBO> getCacheMap() {
		return cacheMap;
	}

	public MarketScannerBO getCachedBean(String ticker) {
		MarketScannerBO bo = cacheMap.get(ticker);
		if (bo == null) {
			bo = new MarketScannerBO();
			bo.setTicker(ticker);
			cacheMap.put(ticker, bo);
		}
		return bo;
	}
}
