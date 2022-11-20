package com.quodd.bond.util;

import static com.quodd.bond.BondsCPD.datacache;
import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

public class BondsMetaDataUtility extends CPDMetaDataUtility {

	private String metaFileNames;

	public BondsMetaDataUtility() {
		super();
		this.metaFileNames = cpdProperties.getStringProperty("META_FILES", null);
		init();
	}

	private void init() {
		if (this.metaDir != null && this.metaFileNames != null) {
			String[] metaFiles = this.metaFileNames.split(",");
			for (String metaFile : metaFiles) {
				if (metaFile != null && metaFile.trim().length() > 0)
					datacache.getMetaTickerSet().addAll(generateUcTickerSet(getTickersFromMetaFile(metaFile)));
			}
		}
	}

	@Override
	public void loadDataFromFile(String filename) {
		logger.info("FILE WATCHER Processing file " + filename);
		Set<String> tickerSet = getTickersFromMetaFile(filename);
		tickerSet = generateUcTickerSet(tickerSet);
		tickerSet.removeAll(datacache.getMetaTickerSet());
		datacache.getMetaTickerSet().addAll(tickerSet);
		logger.info("FILE WATCHER newSymbols:" + tickerSet);
		datacache.getMetaTickerSet().addAll(tickerSet);
		channelManager.subscribeTickers(tickerSet);
	}

	private Set<String> getTickersFromMetaFile(String filename) {
		CsvReader reader;
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
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info("MetaDataUtility getTickersFromMetaFile " + filename + " size " + tickerSet.size());
		return tickerSet;
	}

	private Set<String> generateUcTickerSet(Set<String> rootSymbolSet) {
		Set<String> symbolSet = new HashSet<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "B:" + ticker;
			symbolSet.add(indexTicker);
		}
		return symbolSet;
	}
}