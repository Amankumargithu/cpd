package com.quodd.index.util;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.index.GidsCPD.datacache;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

public class GidsMetaDataUtility extends CPDMetaDataUtility {
	private final String metaGidsFile;

	public GidsMetaDataUtility() {
		super();
		this.metaGidsFile = cpdProperties.getStringProperty("META_GIDS_FILE_NAME", "meta_GIDS.csv");
		init();
	}

	private void init() {
		Set<String> tickerSet = getMetaFromFile(this.metaGidsFile);
		datacache.getRootTickerSet().addAll(tickerSet);
		datacache.getMetaTickerSet().addAll(generateUcTickerSet(tickerSet));
		if (datacache.isDelayed()) {
			datacache.getGidsTickerMap().putAll(generateDelayedUcTickerMap(tickerSet));
		} else {
			datacache.getGidsTickerMap().putAll(generateUcTickerMap(tickerSet));
		}
	}

	private Set<String> getMetaFromFile(String filename) {
		CsvReader reader = null;
		Set<String> tickerSet = new HashSet<>();
		logger.info("Reading metaData file = " + this.metaDir + "/" + filename);
		try {
			File file = new File(this.metaDir + "/" + filename);
			if (!file.exists()) {
				logger.warning("File not exist " + this.metaDir + "/" + filename);
				return tickerSet;
			}
			reader = new CsvReader(this.metaDir + "/" + filename);
			// skipping headers
			reader.readHeaders();
			while (reader.readRecord()) {
				String ticker = reader.get("Symbol");
				tickerSet.add(ticker);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null)
				reader.close();
		}
		logger.info("MetaDataUtility getTickersFromMetaFile " + filename + " size " + tickerSet.size());
		return tickerSet;
	}

	private ConcurrentMap<String, String> generateUcTickerMap(Set<String> rootSymbolSet) {
		ConcurrentMap<String, String> symbolSet = new ConcurrentHashMap<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "I:" + ticker;
			symbolSet.put(indexTicker, indexTicker);
//			symbolSet.put(indexTicker + ".IV", indexTicker + ".IV");
//			symbolSet.put(indexTicker + ".NV", indexTicker + ".NV");
//			symbolSet.put(indexTicker + ".EU", indexTicker + ".EU");
//			symbolSet.put(indexTicker + ".TC", indexTicker + ".TC");
//			symbolSet.put(indexTicker + ".DV", indexTicker + ".DV");
//			symbolSet.put(indexTicker + ".SO", indexTicker + ".SO");
		}
		return symbolSet;
	}

	private Set<String> generateUcTickerSet(Set<String> rootSymbolSet) {
		Set<String> symbolSet = new HashSet<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "I:" + ticker;
			symbolSet.add(indexTicker);
//			symbolSet.add(indexTicker + ".IV");
//			symbolSet.add(indexTicker + ".NV");
//			symbolSet.add(indexTicker + ".EU");
//			symbolSet.add(indexTicker + ".TC");
//			symbolSet.add(indexTicker + ".DV");
//			symbolSet.add(indexTicker + ".SO");
		}
		return symbolSet;
	}

	private ConcurrentMap<String, String> generateDelayedUcTickerMap(Set<String> rootSymbolSet) {
		ConcurrentMap<String, String> symbolSet = new ConcurrentHashMap<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "I:" + ticker;
			symbolSet.put(indexTicker + ".D", indexTicker + ".D");
//			symbolSet.put(indexTicker + ".IV.D", indexTicker + ".IV.D");
//			symbolSet.put(indexTicker + ".NV.D", indexTicker + ".NV.D");
//			symbolSet.put(indexTicker + ".EU.D", indexTicker + ".EU.D");
//			symbolSet.put(indexTicker + ".TC.D", indexTicker + ".TC.D");
//			symbolSet.put(indexTicker + ".DV.D", indexTicker + ".DV.D");
//			symbolSet.put(indexTicker + ".SO.D", indexTicker + ".SO.D");
		}
		return symbolSet;
	}

	@Override
	public void loadDataFromFile(String filename) {
		if (!this.metaGidsFile.equals(filename))
			return;
		logger.info("FILE WATCHER Processing file " + filename);
		Set<String> tickerSet = getMetaFromFile(filename);
		tickerSet.removeAll(datacache.getRootTickerSet());
		if (datacache.isDelayed())
			datacache.getGidsTickerMap().putAll(generateDelayedUcTickerMap(tickerSet));
		else
			datacache.getGidsTickerMap().putAll(generateUcTickerMap(tickerSet));
		logger.info("FILE WATCHER newSymbols:" + tickerSet);
		datacache.getMetaTickerSet().addAll(generateUcTickerSet(tickerSet));
		datacache.getRootTickerSet().addAll(tickerSet);
		channelManager.subscribeTickers(tickerSet);
	}

}