package com.quodd.common.ibus;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import com.quodd.common.collection.MappedMessageQueue;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.logger.QuoddLogger;

import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;
import ch.softwired.jms.IBusTopicPublisher;

class LegacyMessageDistributor implements Runnable, AutoCloseable {
	private MappedMessageQueue messageQueue;
	private int surgeThreshold = 40000;
	private int conflationTime = 40;
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private boolean doRun = false;
	private BytesMessage bm;
	private int messageCount;
	private TopicPublisher qtPublisher;
	private int publishedMessageCount = 0;
	private boolean doRunLogging = false;
	private Thread loggingThread = null;

	public LegacyMessageDistributor(final MappedMessageQueue messageQueue, final JMSPropertyBean bean) {
		this.messageQueue = Objects.requireNonNull(messageQueue);
		this.conflationTime = bean.getConflationTime();
		this.surgeThreshold = bean.getSurgeThreshold();
		this.doRun = true;
		this.doRunLogging = true;
		startLoggingThread();
		try {
			initMulticast(bean);
		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "Unable to initialize. Exiting system.", e);
			close();
		}
	}

	private void initMulticast(final JMSPropertyBean bean) throws JMSException {
		IBusJMSContext.getTopicConnection().setClientID(bean.getClientId());
		TopicSession session = IBusJMSContext.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
		IBusTopic topic = (IBusTopic) IBusJMSContext.getTopic(bean.getTopicName());
		int index = bean.getQos().indexOf('?');
		if (index <= 0) {
			this.logger.severe("unable to locate replacement character \"?\" for ip address swap.");
			return;
		}
		this.qtPublisher = session.createPublisher(topic);
		((IBusTopicPublisher) this.qtPublisher).setDisableMessageClone(true);
		this.bm = session.createBytesMessage();
	}

	@Override
	public void run() {
		while (this.doRun) {
			try {
				Thread.sleep(this.conflationTime);
				Map<String, Object> elements = this.messageQueue.removeAsMap();
				if (elements == null) {
					this.logger.info("elements retrieved from message Queue are null");
					continue;
				}
				// handle the surge
				if (elements.size() > this.surgeThreshold) {
					this.logger.info("elements count : " + elements.size()
							+ " resetting max message count to prevent heap enlargement.");
					this.surgeThreshold = elements.size();
				}
				Set<String> keys = elements.keySet();
				for (String topic : keys) {
					String map = generateStringData((Map<String, Object>) elements.get(topic));
					handleMessage(topic, map);
				}
			} catch (Exception e) {
				this.logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	private String generateStringData(final Map<String, Object> dataMap) {
		StringBuilder sb = new StringBuilder();
		sb.append(dataMap.get(CPDConstants.ticker));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.rootTicker));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.lastPrice));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.openPrice));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.percentChange));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.changePrice));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.dayHigh));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.dayLow));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.bidSize));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.askSize));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.totalVolume));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.tradeVolume));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.bidPrice));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.askPrice));
		sb.append("||");
		sb.append("0");
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.previousClose));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extLastPrice));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extPerChg));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extChangePrc));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extTradeVol));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extTradeExh) != null ? dataMap.get(CPDConstants.extTradeExh) : " ");
		sb.append("||");
		sb.append("F"); // false
		sb.append("||");
		if (dataMap.get(CPDConstants.isSho) != null && (boolean) dataMap.get(CPDConstants.isSho)) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		sb.append("||");
		sb.append("0.0000");
		sb.append("||");
		sb.append("0.0000");
		sb.append("||");
		sb.append("0.0000");
		sb.append("||");
		sb.append("0.0000");
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.tradeDate));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.tradeTime));
		// used _priMktcenter
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.exchangeCode));
		// ask market
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.askExchange) != null ? dataMap.get(CPDConstants.askExchange) : " ");
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.bidExchange) != null ? dataMap.get(CPDConstants.bidExchange) : " ");
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.tradeExchange) != null ? dataMap.get(CPDConstants.tradeExchange) : " ");
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.vwap));
		// exchangeId - it is used as 16 or img.protocol
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.ucProtocol) != null ? dataMap.get(CPDConstants.ucProtocol) : " ");
		sb.append("||");
		sb.append(0.0);// settlement price
		// for Halted status
		sb.append("||");
		if (dataMap.get(CPDConstants.isHalted) != null && (boolean) dataMap.get(CPDConstants.isHalted)) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		// For Limit up down we donot process it in new cpd
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.limitUpDown) != null ? dataMap.get(CPDConstants.limitUpDown) : " ");
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extTradeDate));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extTradeTime));
		sb.append("||");
		sb.append(0l);
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.tickUpDown));
		sb.append("||");
		sb.append(dataMap.get(CPDConstants.extTickUpDown));
		return sb.toString();
	}

	@Override
	public void close() {
		this.doRun = false;
		this.doRunLogging = false;
		try {
			if (this.loggingThread != null)
				this.loggingThread.join();
		} catch (InterruptedException e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.logger.info("Closing distributor thread.");
	}

	private void handleMessage(final String topic, final String qtCPDMessageBean) {
		try {
			if (topic == null) {
				this.logger.warning("Message discarded!!!! topic is null " + topic);
				return;
			}
			if (qtCPDMessageBean == null) {
				this.logger.warning("Message discarded!!!! bean is null " + qtCPDMessageBean);
				return;
			}
			this.messageCount++;
			this.bm.clearBody();
			this.bm.writeInt(this.messageCount);
			this.bm.writeUTF(topic);
			this.bm.writeBytes(qtCPDMessageBean.getBytes());
			this.qtPublisher.publish(this.bm);
			++this.publishedMessageCount;
		} catch (Exception jmse) {
			this.logger.log(Level.SEVERE, jmse.getMessage(), jmse);
		}
	}

	private void startLoggingThread() {
		this.loggingThread = new Thread(() -> {
			long lastLogTime = System.currentTimeMillis();
			while (this.doRunLogging) {
				long timeDiff = System.currentTimeMillis() - lastLogTime;
				if (timeDiff > 1000) {
					this.logger.info("LEGACY IBUS published : " + this.publishedMessageCount);
					lastLogTime = System.currentTimeMillis();
					this.publishedMessageCount = 0;
				}
				try {
					TimeUnit.MILLISECONDS.sleep(900);
				} catch (Exception e) {
					this.logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}, "JMS_LOGGER_LEGACY");
		this.loggingThread.start();
	}
}