package com.quodd.equity.util;

import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;
import static com.quodd.equity.EquityCPD.datacache;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

/**
 * It is a utility class for Meta files which include data from different
 * channels like CTA-A, CTA-B and UTP.
 * 
 * @author
 * @version 1.0
 */
public class EqMetaDataUtility extends CPDMetaDataUtility {
	/** Meta file containing data from UTP channel. */
	private final String metaUtpFile;
	/** Meta file containing data from CTA-A channel. */
	private final String metaCtaaFile;
	/** Meta file containing data from CTA-B channel. */
	private final String metaCtabFile;

	public EqMetaDataUtility() {
		super();
		this.metaUtpFile = cpdProperties.getStringProperty("META_UTP_FILE_NAME", "meta_UTP.csv");
		this.metaCtaaFile = cpdProperties.getStringProperty("META_CTAA_FILE_NAME", "meta_CTA-A.csv");
		this.metaCtabFile = cpdProperties.getStringProperty("META_CTAB_FILE_NAME", "meta_CTA-B.csv");
		init();
	}

	private void init() {
		ConcurrentMap<String, String> tickerSet = getMetaFromFile(this.metaUtpFile);
		if (datacache.isDelayed()) {
			datacache.getUtpTickerSet().putAll(EqMetaDataUtility.generateQuoddTickerSet(tickerSet));
		} else {
			datacache.getUtpTickerSet().putAll(tickerSet);
		}
		datacache.getMetaTickerSet().addAll(tickerSet.keySet());
		tickerSet = getMetaFromFile(this.metaCtaaFile);
		if (datacache.isDelayed()) {
			datacache.getCtaaTickerSet().putAll(EqMetaDataUtility.generateQuoddTickerSet(tickerSet));
		} else {
			datacache.getCtaaTickerSet().putAll(tickerSet);
		}
		datacache.getMetaTickerSet().addAll(tickerSet.keySet());
		tickerSet = getMetaFromFile(this.metaCtabFile);
		if (datacache.isDelayed()) {
			datacache.getCtabTickerSet().putAll(EqMetaDataUtility.generateQuoddTickerSet(tickerSet));
		} else {
			datacache.getCtabTickerSet().putAll(tickerSet);
		}
		datacache.getMetaTickerSet().addAll(tickerSet.keySet());
	}

	/**
	 * This method will give {@link HashSet} of tickers for file passed.
	 * 
	 * @param filename Name of file.
	 * @param dirPath  Directory path.
	 * @return tickerSet Gives set of tickers.
	 */
	private ConcurrentMap<String, String> getMetaFromFile(final String filename) {
		CsvReader reader = null;
		final ConcurrentMap<String, String> tickerSet = new ConcurrentHashMap<>();
		logger.info(() -> "Reading metaData file = " + filename);
		try {
			final String absoluteName = this.metaDir + "/" + filename;
			final File file = new File(absoluteName);
			if (!file.exists()) {
				logger.warning(() -> "File not exist " + absoluteName);
				return tickerSet;
			}
			reader = new CsvReader(absoluteName);
			// skipping headers
			reader.readHeaders();
			while (reader.readRecord()) {
				final String marketCode = reader.get("PrimaryMarketCode");
				if (marketCode != null && marketCode.trim().length() > 0) {
					tickerSet.put(reader.get("Symbol"), reader.get("Symbol"));
				}
				String marketTier = reader.get("MarketTier");
				if (marketTier != null && !marketTier.isEmpty()) {
					datacache.getMarketTierSet().put(generateQuoddTicker(reader.get("Symbol")),
							datacache.getTierMapping(marketTier));
				}
			}
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		logger.info(() -> "MetaDataUtility getTickersFromMetaFile " + filename + " size " + tickerSet.size());
		return tickerSet;
	}

	/**
	 * Data from particular file is added to dataCache.
	 * 
	 * @param filename A String
	 */
	@Override
	public void loadDataFromFile(final String filename) {
		if (!(this.metaUtpFile.equals(filename) || this.metaCtaaFile.equals(filename)
				|| this.metaCtabFile.equals(filename))) {
			return;
		}
		logger.info(() -> "FILE WATCHER Processing file " + filename);
		final ConcurrentMap<String, String> tickerSet = getMetaFromFile(filename);
		for (final String ticker : datacache.getMetaTickerSet()) {
			tickerSet.remove(ticker);
		}
		if (this.metaUtpFile.equals(filename)) {
			if (datacache.isDelayed()) {
				datacache.getUtpTickerSet().putAll(EqMetaDataUtility.generateQuoddTickerSet(tickerSet));
			} else {
				datacache.getUtpTickerSet().putAll(tickerSet);
			}
		} else if (this.metaCtaaFile.equals(filename)) {
			if (datacache.isDelayed()) {
				datacache.getCtaaTickerSet().putAll(EqMetaDataUtility.generateQuoddTickerSet(tickerSet));
			} else {
				datacache.getCtaaTickerSet().putAll(tickerSet);
			}
		} else if (this.metaCtabFile.equals(filename)) {
			if (datacache.isDelayed()) {
				datacache.getCtabTickerSet().putAll(EqMetaDataUtility.generateQuoddTickerSet(tickerSet));
			} else {
				datacache.getCtabTickerSet().putAll(tickerSet);
			}
		}
		logger.info(() -> "FILE WATCHER newSymbols:" + tickerSet);
		datacache.getMetaTickerSet().addAll(tickerSet.keySet());
		channelManager.subscribeTickers(tickerSet.keySet());
	}

	/**
	 * Gives {@link Set} Set of delayed tickers applicable if {@link }isDetailed is
	 * sets to true.
	 * 
	 * @param rootSymbolSet A Set of root symbols.
	 * @return delayedTickerSet Set of delayed tickers.
	 */
	private static ConcurrentMap<String, String> generateQuoddTickerSet(
			final ConcurrentMap<String, String> rootSymbolSet) {
		final ConcurrentMap<String, String> eqSymbolSet = new ConcurrentHashMap<>();
		rootSymbolSet.entrySet().forEach(entry -> eqSymbolSet.put(entry.getKey() + CPDConstants.DELAYED_SUFFIX,
				entry.getKey() + CPDConstants.DELAYED_SUFFIX));
		return eqSymbolSet;
	}

	private String generateQuoddTicker(String ticker) {
		if (datacache.isDelayed())
			return ticker + CPDConstants.DELAYED_SUFFIX;
		return ticker;
	}
}