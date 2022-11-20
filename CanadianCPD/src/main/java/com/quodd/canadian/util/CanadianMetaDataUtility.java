package com.quodd.canadian.util;

import static com.quodd.canadian.CanadianCPD.datacache;
import static com.quodd.common.cpd.CPD.channelManager;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.canadian.CanadianCache;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.cpd.util.CPDMetaDataUtility;

public class CanadianMetaDataUtility extends CPDMetaDataUtility {

	private String metaTsxFile = null;

	public CanadianMetaDataUtility() {
		super();
		this.metaTsxFile = cpdProperties.getStringProperty("META_TSX_FILE_NAME", "meta_TSX.csv");
		init();
	}

	private void init() {
		Set<String> tickerSet = getMetaFromFile(this.metaTsxFile, this.metaDir);
		datacache.getMetaTickerSet().addAll(tickerSet);
	}

	private Set<String> getMetaFromFile(String filename, String dirPath) {
		CsvReader reader = null;
		logger.info(() -> "Reading metaData file = " + filename);
		Set<String> tickerSet = new HashSet<>();
		try {
			File file = new File(dirPath + "/" + filename);
			if (!file.exists()) {
				logger.warning("File not exist " + dirPath + "/" + filename);
				return tickerSet;
			}
			reader = new CsvReader(dirPath + "/" + filename);
			// skipping headers
			reader.readHeaders();
			while (reader.readRecord()) {
				String marketCode = reader.get("ListingMarket");
				String ticker = reader.get("Symbol");
				if (marketCode != null && marketCode.trim().length() > 0) {
					if (CanadianCache.EXCHANGE_XTSE.equals(marketCode)) {
						datacache.getXtseUcTickerSet().put(ticker + CanadianCache.SUFFIX_UC_XTSE,ticker + CanadianCache.SUFFIX_UC_XTSE);
						if (datacache.isDelayed())
							datacache.getXtseQuoddTickerSet()
									.put(ticker + datacache.getXtseQuoddSuffix() + CPDConstants.DELAYED_SUFFIX,ticker + datacache.getXtseQuoddSuffix() + CPDConstants.DELAYED_SUFFIX);
						else
							datacache.getXtseQuoddTickerSet().put(ticker + datacache.getXtseQuoddSuffix(),ticker + datacache.getXtseQuoddSuffix());
						tickerSet.add(ticker + CanadianCache.SUFFIX_UC_XTSE);
					} else if (CanadianCache.EXCHANGE_XTSX.equals(marketCode)) {
						datacache.getXtsxUcTickerSet().put(ticker + CanadianCache.SUFFIX_UC_XTSX,ticker + CanadianCache.SUFFIX_UC_XTSX);
						if (datacache.isDelayed())
							datacache.getXtsxQuoddTickerSet()
									.put(ticker + datacache.getXtsxQuoddSuffix() + CPDConstants.DELAYED_SUFFIX,ticker + datacache.getXtsxQuoddSuffix() + CPDConstants.DELAYED_SUFFIX);
						else
							datacache.getXtsxQuoddTickerSet().put(ticker + datacache.getXtsxQuoddSuffix(),ticker + datacache.getXtsxQuoddSuffix());
						tickerSet.add(ticker + CanadianCache.SUFFIX_UC_XTSX);
					} else {
						logger.warning("META Unknown market code " + marketCode + " for symbol " + ticker);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			if (reader != null)
				reader.close();
		}
		return tickerSet;
	}

	@Override
	public void loadDataFromFile(String filename) {
		if (!this.metaTsxFile.equals(filename))
			return;
		logger.info("FILE WATCHER Processing file " + filename);
		Set<String> tickerSet = getMetaFromFile(filename, this.metaDir);
		tickerSet.removeAll(datacache.getMetaTickerSet());
		logger.info("FILE WATCHER newSymbols:" + tickerSet);
		datacache.getMetaTickerSet().addAll(tickerSet);
		channelManager.subscribeTickers(tickerSet);
	}

}