package com.quodd.common.ibus;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.collection.MappedMessageQueue;
import com.quodd.common.logger.QuoddLogger;

public class IbusDispatcherLegacy implements AutoCloseable {
	private LegacyMessageDistributor distributor = null;
	private Thread distributorThread = null;
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private String dispatcherName = null;

	public void startDispatcher(String cpdType, MappedMessageQueue queue, JMSPropertyBean bean) {
		Objects.requireNonNull(bean);
		Objects.requireNonNull(queue);
		this.dispatcherName = cpdType + "_LegacyDispatcher";
		if (bean.getClientId() == null || bean.getTopicName() == null || bean.getQos() == null) {
			this.logger.severe("bean properties are null. Closing Dispatcher " + this.dispatcherName);
			return;
		}
		this.distributor = new LegacyMessageDistributor(queue, bean);
		this.distributorThread = new Thread(this.distributor, cpdType + "_LegacyDistributor");
		this.distributorThread.start();
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