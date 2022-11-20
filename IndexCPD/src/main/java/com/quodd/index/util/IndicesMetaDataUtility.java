package com.quodd.index.util;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.index.IndicesCPD.datacache;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

public class IndicesMetaDataUtility extends CPDMetaDataUtility {
	private String metaCtaidxFile = null;
	private String metaDjidxFile = null;
	private String metaGifFile = null;
	private String metaMdiFile = null;
	private String metaRsltckFile = null;
	private String metaSpidxFile = null;
	private String metaTsxidxFile = null;

	public IndicesMetaDataUtility() {
		super();
		this.metaCtaidxFile = cpdProperties.getStringProperty("META_CTA_FILE_NAME", "meta_CTAIDX.csv");
		this.metaDjidxFile = cpdProperties.getStringProperty("META_DJ_FILE_NAME", "meta_DJIDX.csv");
		this.metaGifFile = cpdProperties.getStringProperty("META_GIF_FILE_NAME", "meta_GIF.csv");
		this.metaMdiFile = cpdProperties.getStringProperty("META_MDI_FILE_NAME", "meta_MDI.csv");
		this.metaRsltckFile = cpdProperties.getStringProperty("META_RSLTCK_FILE_NAME", "meta_RSLTCK.csv");
		this.metaSpidxFile = cpdProperties.getStringProperty("META_SP_FILE_NAME", "meta_SPIDX.csv");
		this.metaTsxidxFile = cpdProperties.getStringProperty("META_TSX_FILE_NAME", "meta_TSXIDX.csv");
		init();
	}

	private void init() {
		processIndexTickers(this.metaCtaidxFile, datacache.getCtaTickerSet());
		processIndexTickers(this.metaDjidxFile, datacache.getDjTickerSet());
		processIndexTickers(this.metaGifFile, datacache.getGifTickerSet());
		processIndexTickers(this.metaMdiFile, datacache.getMdiTickerSet());
		processIndexTickers(this.metaRsltckFile, datacache.getRsltckTickerSet());
		processIndexTickers(this.metaSpidxFile, datacache.getSpTickerSet());
		processIndexTickers(this.metaTsxidxFile, datacache.getTsxTickerSet());
	}

	private void processIndexTickers(String filename, ConcurrentMap<String, String> tickerMap) {
		Set<String> tickerSet = getMetaFromFile(filename);
		datacache.getRootTickerSet().addAll(tickerSet);
		datacache.getMetaTickerSet().addAll(generateUcTickerSet(tickerSet));
		if (datacache.isDelayed()) {
			tickerMap.putAll(generateDelayedUcTickerMap(tickerSet));
		} else {
			tickerMap.putAll(generateUcTickerMap(tickerSet));
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
			Set<String> rootSet = new HashSet<>();
			if (filename.equalsIgnoreCase(this.metaCtaidxFile)) {
				for (String ticker : tickerSet) {
					if (ticker.length() > 3) {
						rootSet.add(ticker.substring(0, ticker.length() - 3));
					}
				}
			}
			tickerSet.addAll(rootSet);
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
		ConcurrentMap<String, String> symbolMap = new ConcurrentHashMap<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "I:" + ticker;
			symbolMap.put(indexTicker, indexTicker);
//			symbolMap.put(indexTicker + ".IV", indexTicker + ".IV");
//			symbolMap.put(indexTicker + ".NV", indexTicker + ".NV");
//			symbolMap.put(indexTicker + ".EU", indexTicker + ".EU");
//			symbolMap.put(indexTicker + ".TC", indexTicker + ".TC");
//			symbolMap.put(indexTicker + ".DV", indexTicker + ".DV");
//			symbolMap.put(indexTicker + ".SO", indexTicker + ".SO");
		}
		return symbolMap;
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
		ConcurrentMap<String, String> symbolMap = new ConcurrentHashMap<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "I:" + ticker;
			symbolMap.put(indexTicker + ".D", indexTicker + ".D");
//			symbolMap.put(indexTicker + ".IV.D", indexTicker + ".IV.D");
//			symbolMap.put(indexTicker + ".NV.D", indexTicker + ".NV.D");
//			symbolMap.put(indexTicker + ".EU.D", indexTicker + ".EU.D");
//			symbolMap.put(indexTicker + ".TC.D", indexTicker + ".TC.D");
//			symbolMap.put(indexTicker + ".DV.D", indexTicker + ".DV.D");
//			symbolMap.put(indexTicker + ".SO.D", indexTicker + ".SO.D");
		}
		return symbolMap;
	}

	@Override
	public void loadDataFromFile(String filename) {
		Set<String> tickerSet = getMetaFromFile(filename);
		tickerSet.removeAll(datacache.getRootTickerSet());
		if (tickerSet.isEmpty()) {
			return;
		}
		ConcurrentMap<String, String> tickerMap = null;
		if (datacache.isDelayed())
			tickerMap = generateDelayedUcTickerMap(tickerSet);
		else
			tickerMap = generateUcTickerMap(tickerSet);
		if (this.metaCtaidxFile.equals(filename)) {
			datacache.getCtaTickerSet().putAll(tickerMap);
		} else if (this.metaDjidxFile.equals(filename)) {
			datacache.getDjTickerSet().putAll(tickerMap);
		} else if (this.metaMdiFile.equals(filename)) {
			datacache.getMdiTickerSet().putAll(tickerMap);
		} else if (this.metaRsltckFile.equals(filename)) {
			datacache.getRsltckTickerSet().putAll(tickerMap);
		} else if (this.metaGifFile.equals(filename)) {
			datacache.getGifTickerSet().putAll(tickerMap);
		} else if (this.metaSpidxFile.equals(filename)) {
			datacache.getSpTickerSet().putAll(tickerMap);
		} else if (this.metaTsxidxFile.equals(filename)) {
			datacache.getTsxTickerSet().putAll(tickerMap);
		} else {
			return;
		}
		logger.info("FILE WATCHER newSymbols:" + tickerSet);
		Set<String> ucTickerSet = generateUcTickerSet(tickerSet);
		datacache.getMetaTickerSet().addAll(ucTickerSet);
		datacache.getRootTickerSet().addAll(tickerSet);
		channelManager.subscribeTickers(ucTickerSet);
	}

}