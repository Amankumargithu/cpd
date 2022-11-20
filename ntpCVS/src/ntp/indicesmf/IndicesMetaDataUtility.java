package ntp.indicesmf;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.csvreader.CsvReader;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class IndicesMetaDataUtility {
	private static final CPDProperty prop = CPDProperty.getInstance();
	public static final List<String> indexSuffixList = Collections
			.unmodifiableList(Arrays.asList("IV", "NV", "EU", "TC", "DV", "SO"));
	private String metaDir = null;
	private String metaCtaidxFile = null;
	private String metaDjidxFile = null;
	private String metaGifFile = null;
	private String metaMdiFile = null;
	private String metaRsltckFile = null;
	private String metaSpidxFile = null;
	private String metaTsxidxFile = null;
	private String metaGidsFile = null;

	private Set<String> metaTickerSet = new HashSet<>();

	public IndicesMetaDataUtility() {
		this.metaDir = prop.getProperty("META_FILE_DIR");
		this.metaCtaidxFile = prop.getProperty("META_CTA_FILE_NAME");
		this.metaDjidxFile = prop.getProperty("META_DJ_FILE_NAME");
		this.metaGifFile = prop.getProperty("META_GIF_FILE_NAME");
		this.metaMdiFile = prop.getProperty("META_MDI_FILE_NAME");
		this.metaRsltckFile = prop.getProperty("META_RSLTCK_FILE_NAME");
		this.metaSpidxFile = prop.getProperty("META_SP_FILE_NAME");
		this.metaTsxidxFile = prop.getProperty("META_TSX_FILE_NAME");
		this.metaGidsFile = prop.getProperty("META_GIDS_FILE_NAME");
		init();
	}

	private void init() {
		processIndexTickers(this.metaCtaidxFile);
		processIndexTickers(this.metaDjidxFile);
		processIndexTickers(this.metaGifFile);
		processIndexTickers(this.metaMdiFile);
		processIndexTickers(this.metaRsltckFile);
		processIndexTickers(this.metaSpidxFile);
		processIndexTickers(this.metaTsxidxFile);
		processIndexTickers(this.metaGidsFile);
	}

	private void processIndexTickers(String filename) {
		Set<String> tickerSet = getMetaFromFile(filename);
		ConcurrentMap<String, String> indexTickerMap = generateUcTickerMap(tickerSet);
		this.metaTickerSet.addAll(indexTickerMap.keySet());
	}

	private Set<String> getMetaFromFile(String filename) {
		CsvReader reader = null;
		Set<String> tickerSet = new HashSet<>();
		NTPLogger.info("Reading metaData file = " + this.metaDir + "/" + filename);
		try {
			File file = new File(this.metaDir + "/" + filename);
			if (!file.exists()) {
				NTPLogger.warning("File not exist " + this.metaDir + "/" + filename);
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
			NTPLogger.warning(e.getMessage());
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}
		NTPLogger.info("MetaDataUtility getTickersFromMetaFile " + filename + " size " + tickerSet.size());
		return tickerSet;
	}

	private ConcurrentMap<String, String> generateUcTickerMap(Set<String> rootSymbolSet) {
		ConcurrentMap<String, String> symbolMap = new ConcurrentHashMap<>();
		for (String ticker : rootSymbolSet) {
			String indexTicker = "I:" + ticker;
			boolean isSuffixPresent = false;
			symbolMap.put(indexTicker, indexTicker);
			if (indexTicker.contains(".")) {
				String suffix = indexTicker.substring(indexTicker.lastIndexOf(".") + 1);
				if (indexSuffixList.contains(suffix))
					isSuffixPresent = true;
			}
			if (!isSuffixPresent) {
				symbolMap.put(indexTicker + ".IV", indexTicker + ".IV");
				symbolMap.put(indexTicker + ".NV", indexTicker + ".NV");
				symbolMap.put(indexTicker + ".EU", indexTicker + ".EU");
				symbolMap.put(indexTicker + ".TC", indexTicker + ".TC");
				symbolMap.put(indexTicker + ".DV", indexTicker + ".DV");
				symbolMap.put(indexTicker + ".SO", indexTicker + ".SO");
			}
		}
		return symbolMap;
	}
	
	public Set<String> getMetaTickerList(){
		return this.metaTickerSet;
	}

}