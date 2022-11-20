package com.quodd.common.uc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.CommonLogMessage;
import com.quodd.common.logger.QuoddLogger;

public class OTCExchangeMapPopulator {
	private String exchangeFilePath = null;
	private HashSet<String> exchangeSet = new HashSet<>();
	private HashSet<String> otcExchangeIds;
	private final Logger logger = QuoddLogger.getInstance().getLogger();

	public OTCExchangeMapPopulator(String exchangeFilePath) {
		this.exchangeFilePath = Objects.requireNonNull(exchangeFilePath);
		this.otcExchangeIds = new HashSet<>();
		this.otcExchangeIds.add("XOTC");
		this.otcExchangeIds.add("OOTC");
		this.otcExchangeIds.add("OTCB");
		this.otcExchangeIds.add("OTCQ");
		this.otcExchangeIds.add("PINX");
		this.otcExchangeIds.add("PSGM");
	}

	public Set<String> populateExchangeMap() {
		try {
			this.logger.info("Loading Exchange Mapping from " + this.exchangeFilePath);
			File file = new File(this.exchangeFilePath);
			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
					String result = null;
					while ((result = reader.readLine()) != null) {
						if (result.trim().length() > 0)
							this.exchangeSet.add(result);
					}
				}
			} else {
				this.logger.warning(() -> CommonLogMessage.missingProperty("OTC_EXCHANGE_MAPPING_FILE"));
			}
		} catch (Exception e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
		}
		return this.exchangeSet;
	}

	public boolean isOtcExchangeId(String tier) {
		return this.otcExchangeIds.contains(tier);
	}
}
