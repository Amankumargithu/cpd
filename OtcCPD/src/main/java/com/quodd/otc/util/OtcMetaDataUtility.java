package com.quodd.otc.util;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.otc.OtcCPD.datacache;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

public class OtcMetaDataUtility extends CPDMetaDataUtility {

	private final String metaFile;

	public OtcMetaDataUtility() {
		super();
		this.metaFile = cpdProperties.getStringProperty("META_FILE_NAME", "meta_OTC.csv");
		init();
	}

	private void init() {
		Map<String, String> otcMap = getOtcFromMeta(this.metaFile, this.metaDir);
		Map<String, String> otcbbMap = getOtcbbFromMeta(this.metaFile, this.metaDir);
		datacache.getOtcbbSymbolMap().putAll(otcbbMap);
		datacache.getOtcSymbolMap().putAll(otcMap);
		datacache.getOtcMetaQuoddTickerSet().putAll(getOtcQuoddTickerSet(otcMap.keySet()));
		datacache.getOtcbbMetaQuoddTickerSet().putAll(getOtcbbQuoddTickerSet(otcbbMap.keySet()));
		datacache.getMetaUcTickerSet().addAll(getUcTickerSet(otcMap));
	}

	private Map<String, String> getOtcFromMeta(final String filename, final String dirPath) {
		CsvReader reader = null;
		final Map<String, String> tickerMap = new HashMap<>();
		try {
			File file = new File(dirPath + "/" + filename);
			if (!file.exists()) {
				logger.warning("File not exist " + dirPath + "/" + filename);
				return tickerMap;
			}
			reader = new CsvReader(dirPath + "/" + filename);
			// skipping headers
			reader.readHeaders();
			while (reader.readRecord()) {
				String tier = reader.get("ListingMarket");
				if (tier != null && tier.trim().length() > 0)
					tickerMap.put(reader.get("Symbol"), tier);
				String marketTier = reader.get("MarketTier");
				if (marketTier != null && !marketTier.isEmpty()) {
					datacache.getMarketTierSet().put(getOtcQuoddTicker(reader.get("Symbol")),
							datacache.getTierMapping(marketTier));
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null)
				reader.close();
		}
		logger.info(() -> "MetaDataUtility getOtcFromMeta " + filename + " size " + tickerMap.size());
		return tickerMap;
	}

	private Map<String, String> getOtcbbFromMeta(final String filename, String dirPath) {
		CsvReader reader;
		final Map<String, String> tickerMap = new HashMap<>();
		try {
			File file = new File(dirPath + "/" + filename);
			if (!file.exists()) {
				logger.warning("File not exist " + dirPath + "/" + filename);
				return tickerMap;
			}
			reader = new CsvReader(dirPath + "/" + filename);
			// skipping headers
			reader.readHeaders();
			while (reader.readRecord()) {
				if ("1".equalsIgnoreCase(reader.get("BB_Quoted")))
					tickerMap.put(reader.get("Symbol"), "BB");
			}
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info(() -> "MetaDataUtility getOtcbbFromMeta " + filename + " size " + tickerMap.size());
		return tickerMap;
	}

	private ConcurrentMap<String, String> getOtcQuoddTickerSet(Set<String> rootSymbolSet) {
		ConcurrentMap<String, String> otcSymbolSet = new ConcurrentHashMap<>();
		if (datacache.isDelayed()) {
			for (String ticker : rootSymbolSet) {
				otcSymbolSet.put(ticker + datacache.getOtcSymbolSuffix() + CPDConstants.DELAYED_SUFFIX,
						ticker + datacache.getOtcSymbolSuffix() + CPDConstants.DELAYED_SUFFIX);
			}
		} else {
			for (String ticker : rootSymbolSet) {
				otcSymbolSet.put(ticker + datacache.getOtcSymbolSuffix(), ticker + datacache.getOtcSymbolSuffix());
			}
		}
		return otcSymbolSet;
	}

	private String getOtcQuoddTicker(String ticker) {
		if (datacache.isDelayed()) {
			ticker = ticker + datacache.getOtcSymbolSuffix() + CPDConstants.DELAYED_SUFFIX;
		} else {
			ticker = ticker + datacache.getOtcSymbolSuffix();
		}
		return ticker;
	}

	private ConcurrentMap<String, String> getOtcbbQuoddTickerSet(Set<String> rootSymbolSet) {
		ConcurrentMap<String, String> otcSymbolSet = new ConcurrentHashMap<>();
		if (datacache.isDelayed()) {
			for (String ticker : rootSymbolSet) {
				otcSymbolSet.put(ticker + CPDConstants.DELAYED_SUFFIX, ticker + CPDConstants.DELAYED_SUFFIX);
			}
		} else {
			for (String ticker : rootSymbolSet) {
				otcSymbolSet.put(ticker, ticker);
			}
		}
		return otcSymbolSet;
	}

	private Set<String> getUcTickerSet(Map<String, String> symbolMap) {
		Set<String> ucSymbolSet = new HashSet<>();
		for (String ticker : symbolMap.keySet()) {
			String tier = symbolMap.get(ticker);
			ucSymbolSet.add(ticker);
			ucSymbolSet.add(ticker + "/" + tier);
		}
		return ucSymbolSet;
	}

	@Override
	public void loadDataFromFile(final String filename) {
		if (!this.metaFile.equals(filename))
			return;
		logger.info(() -> "FILE WATCHER Processing file " + filename);
		final Map<String, String> newOtcSymbols = new HashMap<>();
		final Map<String, String> newOtcbbSymbols = new HashMap<>();
		newOtcSymbols.putAll(getOtcFromMeta(filename, this.metaDir));
		newOtcbbSymbols.putAll(getOtcbbFromMeta(filename, this.metaDir));
		Set<String> keySetotc = datacache.getOtcSymbolMap().keySet();
		Set<String> keySetotcbb = datacache.getOtcbbSymbolMap().keySet();
		newOtcSymbols.keySet().removeAll(keySetotc);
		newOtcbbSymbols.keySet().removeAll(keySetotcbb);
		if (newOtcSymbols.size() > 0) {
			logger.info(() -> "FILE WATCHER newOtcSymbols:" + newOtcSymbols);
			datacache.getOtcSymbolMap().putAll(newOtcSymbols);
			datacache.getOtcMetaQuoddTickerSet().putAll(getOtcQuoddTickerSet(newOtcSymbols.keySet()));
			final Set<String> newMetaUcSymbols = getUcTickerSet(newOtcSymbols);
			logger.info(() -> "FILE WATCHER newMetaUcSymbols:" + newMetaUcSymbols);
			datacache.getMetaUcTickerSet().addAll(newMetaUcSymbols);
			if (!newMetaUcSymbols.isEmpty())
				channelManager.subscribeTickers(newMetaUcSymbols);
		}
		if (newOtcbbSymbols.size() > 0) {
			logger.info(() -> "FILE WATCHER newOtcbbSymbols:" + newOtcbbSymbols);
			datacache.getOtcbbSymbolMap().putAll(newOtcbbSymbols);
			datacache.getOtcbbMetaQuoddTickerSet().putAll(getOtcbbQuoddTickerSet(newOtcbbSymbols.keySet()));
		}
	}

}