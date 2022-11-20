package com.quodd.task;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.environmentProperties;
import static com.quodd.cpd.AlertCpd.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;

import com.csvreader.CsvReader;
import com.quodd.bean.AlertFundamentalBean;

public class FundamentalProcessor implements Runnable {

	protected String fundamentalDir = null;
	private boolean doRunWatcher = false;

	public FundamentalProcessor() {
		this.doRunWatcher = true;
		this.fundamentalDir = environmentProperties.getStringProperty("FUNDAMENTAL_FILE_DIR",
				"/home/alertcpd/fundamental");
		File fundamentalFile = new File(fundamentalDir, "alert_earning.csv");
		loadDataFromFile(fundamentalFile.toPath());
		fundamentalFile = new File(fundamentalDir, "alert_fundamental.csv");
		loadDataFromFile(fundamentalFile.toPath());
	}

	@Override
	public void run() {
		logger.info(() -> "Starting FILE WATCHER service for " + this.fundamentalDir);
		try (WatchService watcher = FileSystems.getDefault().newWatchService();) {
			Path path = Paths.get(this.fundamentalDir);
			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			while (this.doRunWatcher) {
				try {
					WatchKey key = watcher.take();
					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
							Object ctx = event.context();
							if (ctx instanceof Path) {
								Path filename = (Path) ctx;
								File fundamentalFile = new File(fundamentalDir, filename.getFileName().toString());
								loadDataFromFile(fundamentalFile.toPath());
							}
						}
					}
					if (!key.reset()) {
						break;
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
			logger.info("Exiting the Watcher service ");
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void loadDataFromFile(Path filename) {
		switch (filename.getFileName().toString()) {
		case "alert_earning.csv":
			parseEarnings(filename.toFile().getAbsolutePath());
			break;
		case "alert_fundamental.csv":
			parseFundamental(filename.toFile().getAbsolutePath());
			break;
		default:
			logger.warning("Unknown file " + filename.getFileName().toString());
			break;
		}
	}

	private void parseEarnings(String filePath) {
		try {
			CsvReader reader = new CsvReader(filePath);
			reader.readHeaders();
			int count = 0;
			while (reader.readRecord()) {
				alertCache.addEarningTicker(reader.get(1));
				count++;
			}
			logger.info("Uploaded tickers " + count);
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void parseFundamental(String filePath) {
		try {
			CsvReader reader = new CsvReader(filePath);
			reader.readHeaders();
			int count = 0;
			while (reader.readRecord()) {
				String avgVol = reader.get("avgDaily").replaceAll(",", "");
				if ("N/A".equals(avgVol))
					avgVol = null;
				alertCache.addFundamentalBean(new AlertFundamentalBean(reader.get("ticker"),
						reader.get("fiftytwoWeekHigh"), reader.get("fiftytwoWeekLow"), avgVol));
				count++;
			}
			logger.info("Uploaded fundamental bean " + count);
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}
