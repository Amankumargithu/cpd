package com.quodd.common.ibus;

import static com.quodd.common.cpd.CPD.cpdProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import com.google.gson.Gson;
import com.quodd.common.collection.MappedMessageQueue;
import com.quodd.common.cpd.util.CPDConstants;
import com.quodd.common.filewriter.PumpFileWriter;
import com.quodd.common.logger.QuoddLogger;

import ch.softwired.jms.IBusDestination;
import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;
import ch.softwired.jms.IBusTopicPublisher;

class IbusMessageDistributor implements Runnable, AutoCloseable {
	private static final long BUFFER_SIZE = 512 * 1024 * 1024l;
	private TopicPublisher qtPublisher;
	private BytesMessage bm;
	private MappedMessageQueue messageQueue;
	private int surgeThreshold = 40000;
	private int conflationTime = 40;
	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private ConcurrentMap<String, Map<String, Object>> cachedTickerDataMap;
	private String dataType;
	private boolean doRun = true;
	private boolean loggerDoRun = true;
	private Thread fileWriterThread = null;
	private PumpFileWriter fileWriter = null;
	private int messageCount;
	private int publishedMessageCount = 0;
	private boolean writeToFile = false;
	private Thread loggerThread = null;
	private final IbusStats stats = new IbusStats(System.currentTimeMillis());
	private final Gson gson = new Gson();

	public IbusMessageDistributor(MappedMessageQueue messageQueue,
			ConcurrentMap<String, Map<String, Object>> cachedTickerDataMap, String dataType, JMSPropertyBean bean) {
		this.dataType = Objects.requireNonNull(dataType);
		this.messageQueue = Objects.requireNonNull(messageQueue);
		this.cachedTickerDataMap = Objects.requireNonNull(cachedTickerDataMap);
		this.conflationTime = bean.getConflationTime();
		this.surgeThreshold = bean.getSurgeThreshold();
		this.loggerThread = new Thread(() -> {
			long lastLogTime = System.currentTimeMillis();
			while (this.loggerDoRun) {
				long timeDiff = System.currentTimeMillis() - lastLogTime;
				if (timeDiff > 1000) {
					this.logger.info(() -> "IBUS published : " + this.publishedMessageCount);
					lastLogTime = System.currentTimeMillis();
					this.publishedMessageCount = 0;
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					this.logger.log(Level.WARNING, "Exception in Producer Stat Thread sleep", e);
				}
			}
		}, "JMS_LOGGER");
		try {
			initMulticast(bean);
		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "Unable to initialize. Exiting system.", e);
			close();
		}
	}

	@Override
	public void run() {
		this.loggerThread.start();
		this.stats.startLoggerThread();
		while (this.doRun) {
			try {
				Thread.sleep(this.conflationTime);
				Map<String, Object> elements = this.messageQueue.removeAsMap();
				if (elements == null) {
					this.logger.info("QTMessageDistributor - elements retrieved from message Queue are null");
					continue;
				}
				// handle the surge
				if (elements.size() > this.surgeThreshold) {
					this.logger.info(() -> "elements count : " + elements.size()
							+ " resetting max message count to prevent heap enlargement.");
				}
				Set<String> keys = elements.keySet();
				for (String topic : keys) {
					Map<String, Object> changedMap = compareData(topic, (Map<String, Object>) elements.get(topic));
					handleMessage(topic, changedMap);
				}
			} catch (Exception e) {
				this.logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	@Override
	public void close() {
		this.doRun = false;
		this.loggerDoRun = false;
		this.writeToFile = false;
		if (this.loggerThread != null) {
			try {
				this.loggerThread.join();
			} catch (InterruptedException e) {
				this.logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		this.stats.close();
		if (this.fileWriter != null)
			this.fileWriter.stopThread();
		if (this.fileWriterThread != null && this.fileWriterThread.isAlive())
			try {
				this.fileWriterThread.join();
			} catch (InterruptedException e) {
				this.logger.log(Level.WARNING, e.getMessage(), e);
			}
		this.logger.info("Closing distributor thread.");
	}

	private void handleMessage(final String topic, final Map<String, Object> cpdMessage) {
		try {
			if (cpdMessage == null) {
				this.logger.warning(() -> "discarded!!!! CPD Message was null " + cpdMessage);
				return;
			}
			if (topic == null) {
				this.logger.warning(() -> "discarded!!!! ticker was null " + topic);
				return;
			}
			this.messageCount++;
			this.bm.clearBody();
			this.bm.writeInt(this.messageCount);
			this.bm.writeUTF(topic);
			Map<String, Object> map = new HashMap<>();
			map.put("event", this.dataType);
			map.put("data", cpdMessage);
			String payload = this.gson.toJson(map);
			this.bm.writeUTF(payload);
			if (this.writeToFile)
				this.fileWriter.add((topic + "," + payload + "\n").getBytes());
			this.qtPublisher.publish(this.bm);
			this.stats.incrementStat(this.dataType);
			++this.publishedMessageCount;
		} catch (Exception jmse) {
			this.logger.log(Level.SEVERE, "JMSException occurred. ", jmse);
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

	private void initFileWriter() {
		try {
			if (this.fileWriterThread != null && this.fileWriterThread.isAlive())
				this.fileWriterThread.join();
			String dataFileDir = cpdProperties.getStringProperty("FILE_WRITER_DIR", "/home/resource");
			File dirFile = new File(dataFileDir);
			if (!dirFile.exists())
				dirFile.mkdirs();
			String fileName = this.dataType + "_" + System.currentTimeMillis() + ".data";
			this.fileWriter = new PumpFileWriter(dirFile, fileName, BUFFER_SIZE);
			this.fileWriterThread = new Thread(this.fileWriter, this.dataType + "WriterThread");
			this.fileWriterThread.start();
		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "log error.", e);
		}
	}

	private Map<String, Object> compareData(final String topic, final Map<String, Object> dataMap) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> cacheMap = this.cachedTickerDataMap.get(topic);
		if (cacheMap == null) {
			cacheMap = new HashMap<>();
			cacheMap.putAll(dataMap);
			this.cachedTickerDataMap.put(topic, cacheMap);
			map.putAll(dataMap);
		} else {
			Set<String> keySet = dataMap.keySet();
			for (String key : keySet) {
				Object oldValue = cacheMap.get(key);
				Object currentValue = dataMap.get(key);
				if (currentValue != null
						&& ((oldValue == null) || (currentValue instanceof String && !(oldValue.equals(currentValue)))
								|| (oldValue != currentValue))) {
					map.put(key, currentValue);
				}
			}
			cacheMap.putAll(map);
			map.put(CPDConstants.ticker, cacheMap.get(CPDConstants.ticker));
			map.put(CPDConstants.rootTicker, cacheMap.get(CPDConstants.rootTicker));
		}
		return map;
	}

	public void setWriteToFile(final boolean b) {
		if (this.writeToFile && !b) {
			this.fileWriter.stopThread();
		} else if (!this.writeToFile && b) {
			initFileWriter();
		}
		this.writeToFile = b;
	}
}