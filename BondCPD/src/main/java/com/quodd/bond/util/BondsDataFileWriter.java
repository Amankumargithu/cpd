package com.quodd.bond.util;

import static com.quodd.bond.BondsCPD.datacache;
import static com.quodd.common.cpd.CPD.cpdProperties;
import static com.quodd.common.cpd.CPD.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import com.quodd.common.cpd.collection.MappedMessageQueue;
import com.quodd.common.cpd.util.CPDConstants;

public class BondsDataFileWriter extends Thread {
	private boolean doRun = true;

	public boolean isDoRun() {
		return this.doRun;
	}

	public void setDoRun(boolean doRun) {
		this.doRun = doRun;
	}

	@Override
	public void run() {
		String bondsDataFile = cpdProperties.getStringProperty("BONDS_DATA_FILE", "/home/bondcpd/data");
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		bondsDataFile = bondsDataFile + "_" + formatter.format(new Date()) + ".csv";
		logger.info("Going to write bonds data to file: " + bondsDataFile);
		try (FileOutputStream fos = new FileOutputStream(new File(bondsDataFile));) {
			StringBuilder header = new StringBuilder();
			header.append("Ticker,Last price,Ask price,Bid price,Size,Volume\n");
			fos.write(header.toString().getBytes());
			fos.flush();
			MappedMessageQueue queue = datacache.getBondWriterQueue();
			while (this.doRun) {
				try {
					Map<String, Object> objs = queue.removeAsMap();
					for (String ticker : objs.keySet()) {
						StringBuilder line = new StringBuilder();
						Map<String, Object> map = (Map<String, Object>) objs.get(ticker);
						line.append(ticker).append(",");
						line.append(map.get(CPDConstants.lastPrice)).append(",");
						line.append(map.get(CPDConstants.askPrice)).append(",");
						line.append(map.get(CPDConstants.bidPrice)).append(",");
						line.append(map.get(CPDConstants.bidSize)).append("x");
						line.append(map.get(CPDConstants.askSize)).append(",");
						line.append(map.get(CPDConstants.tradeVolume));
						line.append("\n");
						logger.info("Going to write to file: " + line.toString());
						fos.write(line.toString().getBytes());
						fos.flush();
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}
