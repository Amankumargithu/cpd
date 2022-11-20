package com.quodd.task;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.logger;
import static com.quodd.cpd.AlertCpd.datafeedProcessor;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.quodd.cpd.AlertCpd;

import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;

public class MessageConsumer implements MessageListener {
	private TopicSubscriber subscriber = null;
	private String consumerName = null;
	private long messageCount = 0;
	private long ibusPayload = 0;
	private long payload = 0;

	private long messagesReceived = 0;
	private long messagesConsumed = 0;

	public void connectToIbus(String ip, String topicName, String qos, String name) {
		try {
			this.consumerName = name;
			int index = qos.indexOf("?");
			if (index <= 0) {
				logger.warning(name + " unable to locate replacement character \"?\" for ip address swap.");
				return;
			}
			String qualityOfService = qos.substring(0, index) + ip + qos.substring(index + 1);
			TopicSession session = IBusJMSContext.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			Topic topic = IBusJMSContext.getTopic(topicName);
			((IBusTopic) topic).setQOS(qualityOfService);
			this.subscriber = session.createSubscriber(topic);
			logger.info(() -> this.consumerName + " qos: " + qualityOfService + " Session: " + session + " Subscriber: "
					+ this.subscriber);
			this.subscriber.setMessageListener(this);
			IBusJMSContext.getTopicConnection().start();
			logger.info(() -> this.consumerName + " Connect to ibus complete");
		} catch (Exception e) {
			logger.log(Level.WARNING, name + ": " + e.getMessage(), e);
		}
	}

	public String getStats() {
		return this.consumerName + "," + this.messageCount + "," + this.ibusPayload + "," + this.payload;
	}

	public void stopConsumption() {
		try {
			logger.info(() -> this.consumerName + " stopConsumption Closing consumer");
			this.subscriber.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, this.consumerName + ": " + e.getMessage(), e);
		}
	}

	public void printIbusStat() {
		logger.info("Received " + messagesReceived + " Consumed " + messagesConsumed);
	}

	@Override
	public void onMessage(Message m) {
		int bytesRead = 0;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			messagesReceived++;
			if (!AlertCpd.doProcessStreamingAlerts) {
				return;
			}
			BytesMessage message = (BytesMessage) m;
			message.readInt();
			String ticker = message.readUTF();
			if (ticker.contains("|")) {
				logger.warning("Problem in readUTF() " + ticker);
			}
			if (!alertCache.containsTicker(ticker)) {
				return;
			}
			messagesConsumed++;
			byte[] bytes = new byte[1024];
			while ((bytesRead = message.readBytes(bytes)) != -1) {
				baos.write(bytes, 0, bytesRead);
			}
			datafeedProcessor.addMessage(baos.toByteArray());
		} catch (Exception e) {
			logger.log(Level.WARNING, this.consumerName + ": " + e.getMessage(), e);
		}

	}

}