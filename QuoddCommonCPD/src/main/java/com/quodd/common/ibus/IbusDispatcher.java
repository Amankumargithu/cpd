package com.quodd.common.ibus;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.collection.MappedMessageQueue;
import com.quodd.common.logger.QuoddLogger;

public class IbusDispatcher implements AutoCloseable {

	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private IbusMessageDistributor distributor = null;
	private String dispatcherName = null;
	private Thread distributorThread = null;

	public void startDispatcher(String cpdType, MappedMessageQueue queue, String dataType, boolean writeToFile,
			ConcurrentMap<String, Map<String, Object>> cachedTickerDataMap, JMSPropertyBean bean) {
		Objects.requireNonNull(bean);
		Objects.requireNonNull(queue);
		Objects.requireNonNull(cachedTickerDataMap);
		this.dispatcherName = cpdType + "_Dispatcher";
		if (bean.getClientId() == null || bean.getTopicName() == null || bean.getQos() == null) {
			this.logger.severe("bean properties are null. Closing Dispatcher " + this.dispatcherName);
			return;
		}
		try {
			this.distributor = new IbusMessageDistributor(queue, cachedTickerDataMap, dataType, bean);
			if (writeToFile)
				startFileThread();
			this.distributorThread = new Thread(this.distributor, cpdType + "_Distributor");
			this.distributorThread.start();
		} catch (Exception e) {
			this.logger.log(Level.WARNING, "Exception in startDispatcher " + e.getMessage(), e);
		}
	}

	public void startFileThread() {
		if (this.distributor != null)
			this.distributor.setWriteToFile(true);
	}

	public void stopFileThread() {
		if (this.distributor != null)
			this.distributor.setWriteToFile(false);
	}

	@Override
	public void close() {
		if (this.distributor != null)
			this.distributor.close();
		try {
			if (this.distributorThread != null)
				this.distributorThread.join();
		} catch (InterruptedException e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.logger.info("Closed successsfully " + this.dispatcherName);
	}

}