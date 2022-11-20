package com.quodd.common.cpd.util;

import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;

/**
 * It is a utility class for Meta files which include data from different
 * channels like CTA-A, CTA-B and UTP.
 * 
 * @author
 * @version 1.0
 */
public abstract class CPDMetaDataUtility implements Runnable {

	/** Meta file directory. */
	protected String metaDir = null;
	private boolean doRunWatcher = false;

	public CPDMetaDataUtility() {
		this.doRunWatcher = true;
		this.metaDir = cpdProperties.getStringProperty("META_FILE_DIR", "");
	}

	public abstract void loadDataFromFile(String string);

	@Override
	public void run() {
		logger.info(() -> "Starting FILE WATCHER service for " + this.metaDir);
		try (WatchService watcher = FileSystems.getDefault().newWatchService();) {
			Path path = Paths.get(this.metaDir);
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
								loadDataFromFile(filename.getFileName().toString());
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

	public void close() {
		this.doRunWatcher = false;
	}
}