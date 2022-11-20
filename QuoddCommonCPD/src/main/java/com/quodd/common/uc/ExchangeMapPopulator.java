package com.quodd.common.uc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.CommonLogMessage;
import com.quodd.common.logger.QuoddLogger;

import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class ExchangeMapPopulator {
	private static final int NASDAQ_EXCHANGE_CODE_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private static final int EQPLUS_EXCHANGE_CODE_POS = 3;
	private HashMap<String, String> exchangeMap = new HashMap<>();
	private HashMap<String, String> uiExchgMap = new HashMap<>();
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private String ip;
	private int port;

	public ExchangeMapPopulator(String ip, int port, String exchangeFilePath) {
		this.ip = Objects.requireNonNull(ip);
		this.port = port;
		Objects.requireNonNull(exchangeFilePath);
		populateExchangeMap(exchangeFilePath);
	}

	private void populateExchangeMap(String exchangeFilePath) {
		boolean flag = false;
		try {
			this.logger.info("Loading Exchnage Mapping from " + exchangeFilePath);
			File file = new File(exchangeFilePath);
			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
					String result = null;
					while ((result = reader.readLine()) != null) {
						if (result.trim().length() > 0) {
							String[] tmp = result.split(",");
							this.exchangeMap.put(tmp[0], tmp[1]);
						}
					}
				}
			} else {
				this.logger.warning(CommonLogMessage.missingProperty("EXCHANGE_MAPPING_FILE"));
				flag = true;
			}
		} catch (Exception e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
			flag = true;
		}
		if (this.exchangeMap.size() == 0)
			flag = true;
		if (flag) {
			UltraChan query = new UltraChan(this.ip, this.port, "ExchPop", "ExchPop", false);
			this.logger.info(() -> CommonLogMessage.connectChannel("ExchPop", this.ip));
			query.Start();
			int resubscribeCount = -1;
			BlobTable table = null;
			do {
				this.logger.info(CommonLogMessage.requestUC("SyncGetExchanges", "", ++resubscribeCount));
				table = query.SyncGetExchanges(new Object());
				this.logger.info(CommonLogMessage.responseUC("SyncGetExchanges", "", table == null ? 0 : table.nRow()));
			} while (table != null && table.len() == 0 && resubscribeCount < 2);
			if (table != null && table.len() == 0 && resubscribeCount == 2)
				this.logger.info(CommonLogMessage.syncAPIOverrun("SyncGetExchanges", resubscribeCount));
			query.Stop();
			this.logger.info(() -> CommonLogMessage.disconnectChannel("ExchPop"));
			if (table != null)
				parseBlobTable(table);
		}
		this.uiExchgMap.putAll(this.exchangeMap);
	}

	private void parseBlobTable(BlobTable blobTable) {
		int rowCount = blobTable.nRow();
		for (int count = 0; count < rowCount; count++) {
			String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
			if (!(instrument.equals("EQUITY") || instrument.equals("TSX-OTC")))
				continue;
			String nazExchgCode = blobTable.GetCell(count, NASDAQ_EXCHANGE_CODE_POS).toUpperCase();
			String eqPlusExchgCode = blobTable.GetCell(count, EQPLUS_EXCHANGE_CODE_POS).toUpperCase();
			this.exchangeMap.put(nazExchgCode, eqPlusExchgCode);
			System.out.println(count + ") " + nazExchgCode + "      :      " + eqPlusExchgCode);
		}
	}

	public Map<String, String> getExchangeMap() {
		return this.exchangeMap;
	}

	public Set<String> getExchanges() {
		HashSet<String> exchanges = new HashSet<>();
		exchanges.addAll(this.exchangeMap.values());
		return exchanges;
	}

	public void updateExchangeMap() {
		Set<String> keys = this.uiExchgMap.keySet();
		for (String s : keys) {
			String val = this.uiExchgMap.get(s).toUpperCase();
			switch (val) {
			case "D1":
			case "D2":
				val = "d";
				break;
			case "BB":
			case "XOTC":
				val = "v";
				break;
			case "OB":
			case "OOTC":
				val = "u";
				break;
			case "XTSE":
				val = "to";
				break;
			case "XTSX":
				val = "tv";
				break;
			case "PSGM":
				val = "gm";
				break;
			case "PINX":
				val = "pk";
				break;
			case "OTCB":
				val = "qb";
				break;
			case "OTCQ":
				val = "qx";
				break;
			default:
				break;
			}
			this.uiExchgMap.put(s, val);
		}
	}

	public String getEquityPlusExchangeCode(String nasdaqExchangeCode) {
		if (nasdaqExchangeCode == null || nasdaqExchangeCode.contains("??") || nasdaqExchangeCode.length() < 1)
			return " ";
		String exchangeCode = nasdaqExchangeCode.toUpperCase();
		if (this.uiExchgMap.containsKey(nasdaqExchangeCode))
			exchangeCode = this.uiExchgMap.get(nasdaqExchangeCode).toLowerCase();
		return exchangeCode;
	}
}
