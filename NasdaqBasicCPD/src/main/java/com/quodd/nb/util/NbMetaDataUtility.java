package com.quodd.nb.util;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.nb.NasdaqBasicCpd.datacache;
import static com.quodd.nb.NasdaqBasicCpd.volPlusChannelManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

public class NbMetaDataUtility extends CPDMetaDataUtility {

	private String metaNlsFile = null;
	private String metaQbboFile = null;

	public NbMetaDataUtility() {
		super();
		this.metaNlsFile = cpdProperties.getStringProperty("META_NLS_FILE_NAME", "meta_NLS.csv");
		this.metaQbboFile = cpdProperties.getStringProperty("META_QBBO_FILE_NAME", "meta_QBBO.csv");
		init();
	}

	private void init() {
		HashSet<String> rootSymbolSet = new HashSet<>();
		rootSymbolSet.addAll(getTickersFromMetaFile(metaNlsFile, this.metaDir));
		rootSymbolSet.addAll(getTickersFromMetaFile(metaQbboFile, this.metaDir));
		datacache.getMetaTickerSet().addAll(rootSymbolSet);
		datacache.getNbMetaUcTickerSet().addAll(generateNbUcTickerSet(rootSymbolSet));
		datacache.getNbMetaQuoddTickerSet().addAll(generateNbQuoddTickerSet(rootSymbolSet));
	}

	@Override
	public void loadDataFromFile(String filename) {
		if (!(this.metaNlsFile.equals(filename) || this.metaQbboFile.equals(filename))) {
			return;
		}
		logger.info(() -> "FILE WATCHER Processing file " + filename);
		HashSet<String> newRootSymbols = new HashSet<>();
		newRootSymbols.addAll(getTickersFromMetaFile(filename, this.metaDir));
		newRootSymbols.removeAll(datacache.getMetaTickerSet());
		if (!newRootSymbols.isEmpty()) {
			datacache.getMetaTickerSet().addAll(newRootSymbols);
			Set<String> nbUcSymbols = generateNbUcTickerSet(newRootSymbols);
			datacache.getNbMetaUcTickerSet().addAll(nbUcSymbols);
			datacache.getNbMetaQuoddTickerSet().addAll(generateNbQuoddTickerSet(newRootSymbols));
			logger.info(() -> "FILE WATCHER nbUcSymbols:" + nbUcSymbols);
			logger.info(() -> "FILE WATCHER newRootSymbols" + newRootSymbols);
			channelManager.subscribeTickers(nbUcSymbols);
			volPlusChannelManager.subscribeTickers(newRootSymbols);
		}
	}

	private Set<String> getTickersFromMetaFile(String filename, String dirPath) {
		CsvReader reader = null;
		Set<String> tickerSet = new HashSet<>();
		logger.info(() -> "Reading metaData file = " + dirPath + "/" + filename);
		try {
			File file = new File(dirPath + "/" + filename);
			if (!file.exists()) {
				logger.warning(() -> "File not exist " + dirPath + "/" + filename);
				return tickerSet;
			}
			reader = new CsvReader(dirPath + "/" + filename);
			// skipping headers
			reader.readHeaders();
			while (reader.readRecord()) {
				tickerSet.add(reader.get("Symbol"));
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null)
				reader.close();
		}
		logger.info(() -> "MetaDataUtility getTickersFromMetaFile " + filename + " size " + tickerSet.size());
		return tickerSet;
	}

	private Set<String> generateNbUcTickerSet(Set<String> rootSymbolSet) {
		Set<String> nbSymbolSet = new HashSet<>();
		for (String ticker : rootSymbolSet) {
			String quoteTicker = "Q:" + ticker + "/T";
			String tradeTicker = "T:" + ticker;
			nbSymbolSet.add(quoteTicker);
			nbSymbolSet.add(tradeTicker);
		}
		return nbSymbolSet;
	}

	private Set<String> generateNbQuoddTickerSet(Set<String> rootSymbolSet) {
		Set<String> nbSymbolSet = new HashSet<>();
		if (datacache.isDelayed()) {
			for (String ticker : rootSymbolSet) {
				nbSymbolSet.add(ticker + datacache.getSymbolSuffix() + CPDConstants.DELAYED_SUFFIX);
			}
		} else {
			for (String ticker : rootSymbolSet) {
				nbSymbolSet.add(ticker + datacache.getSymbolSuffix());
			}
		}
		return nbSymbolSet;
	}
}