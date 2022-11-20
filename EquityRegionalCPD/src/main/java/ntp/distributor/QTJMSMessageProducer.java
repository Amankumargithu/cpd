package ntp.distributor;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import ch.softwired.jms.IBusJMSContext;
import ch.softwired.jms.IBusTopic;
import ch.softwired.jms.IBusTopicPublisher;
import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.EnvironmentProperty;

public class QTJMSMessageProducer implements IDistributionHandler {

	private TopicSession session;
	private TopicPublisher qtPublisher;
	private BytesMessage bm;
	private int messageCount;
	private int publishedMessageCount = 0;
	private long lastLogTime= System.currentTimeMillis();
	private boolean useQOS = false;

	public void setUseQOSFlag(boolean useQOS)
	{
		this.useQOS = useQOS;
	}

	public QTJMSMessageProducer()
	{
		try
		{
			startLoggingThread();
			initMulticast();
		}
		catch(Exception e)
		{
			NTPLogger.error("QTJMSMessageProducer: Unable to initialize QTJMSMessageProducer. Exiting system.");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void initMulticast()
	{
		try
		{
			EnvironmentProperty prop = EnvironmentProperty.getInstance();;
			String ip = prop.getProperty("JMS_QT_PRIMARY_IP");
			String qos = prop.getProperty("JMS_QT_QOS");
			NTPLogger.info("Multicast stream IP = " + ip);
			IBusJMSContext.getTopicConnection().setClientID(prop.getProperty("JMS_CLIENT_ID"));
			session = IBusJMSContext.createTopicSession(false,TopicSession.DUPS_OK_ACKNOWLEDGE);
			IBusTopic topic = (IBusTopic)IBusJMSContext.getTopic(prop.getProperty("JMS_QT_TOPIC_NAME"));
			int index = qos.indexOf("?");
			if (index <= 0) {
				NTPLogger.error("QTJMSMessageProducer - unable to locate replacement character \"?\" for ip address swap.");
				return;
			}
			String qualityOfService = qos.substring(0,index) + ip + qos.substring(index+1);
			if (useQOS)
			{
				((IBusTopic)topic).setQOS(qualityOfService);
			}
			qtPublisher = session.createPublisher(topic);
			((IBusTopicPublisher)qtPublisher).setDisableMessageClone(true);
			bm = session.createBytesMessage();
		} catch (Exception e)
		{
			e.printStackTrace();
			NTPLogger.error("QTJMSMessageProducer:initMulticast() has error." +  e.getMessage());
			e.printStackTrace();
		}
	}

	public void handleMessage(QTCPDMessageBean qtCPDMessageBean)
	{
		try
		{
			if(qtCPDMessageBean == null )
			{
				NTPLogger.warning("QTJMSMessageProducer-Message discarded!!!! bean was null");
				return;
			}
			if( qtCPDMessageBean.getSystemTicker() == null)
			{
				NTPLogger.warning("QTJMSMessageProducer-Message discarded!!!! ticker was null");
				return;
			}
			messageCount++;
			byte[] bytes;
			if(qtCPDMessageBean.getSystemTicker().startsWith("O:")){
				bytes = qtCPDMessageBean.toOptionByteArray();
			}
			else{
				bytes = qtCPDMessageBean.toByteArray();
			}

			if (bytes == null)
			{
				NTPLogger.warning("QTJMSMessageProducer- byte data is null.");
				return;
			}         
			bm.clearBody();
			bm.writeInt(messageCount);
			bm.writeUTF(qtCPDMessageBean.getSystemTicker());
			bm.writeBytes(bytes);
			qtPublisher.publish(bm);
			++publishedMessageCount;
		}
		catch(JMSException jmse) {
			NTPLogger.error("QTJMSMessageProducer-.handleMessage - JMSException occurred. " + jmse.getMessage());
			jmse.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startLoggingThread()
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true)
				{
					long timeDiff = System.currentTimeMillis() - lastLogTime;
					if (timeDiff > 1000)
					{
						NTPLogger.info("IBUS published : " + publishedMessageCount);
						lastLogTime = System.currentTimeMillis();
						publishedMessageCount = 0;
					}
					try {
						Thread.sleep(900);
					} catch (Exception e) {}
				}
			}
		}, "JMS_LOGGER").start();
	}
}
