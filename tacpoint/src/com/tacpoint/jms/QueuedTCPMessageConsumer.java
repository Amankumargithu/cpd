package com.tacpoint.jms;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tacpoint.publisher.TMessageQueue;

public class QueuedTCPMessageConsumer extends Object implements Runnable, TConsumer
{
	private static Log log = LogFactory.getLog(QueuedTCPMessageConsumer.class);

	private MessageHandler messageHandler = null;
	private MessageReducer messageReducer = null;
	private ReducedMessageInflator messageInflator = null;

	private TMessageQueue queue;

	private String consumerName;

	private String clientID;

	private String topicName;

	private String primaryIP;

	private String qos;

	private long aggregationTime = 0;

	public void setAggregationTime(long at) {
		this.aggregationTime = at;
	}

	public void setMessageQueue(TMessageQueue queue) {
		this.queue = queue;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public void setQOS(String qos) {
		this.qos = qos;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public void setMessageReducer(MessageReducer messageReducer) {
		this.messageReducer = messageReducer;
	}

	public void setMessageInflator(ReducedMessageInflator messageInflator) {
		this.messageInflator = messageInflator;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public void setPrimaryIP(String primaryIP) {
		this.primaryIP = primaryIP;
	}

	public String  getConsumerName() {
		return consumerName;
	}

	public String  getQOS() {
		return qos;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public MessageReducer getMessageReducer() {
		return messageReducer;
	}

	public ReducedMessageInflator getMessageInflator() {
		return messageInflator;
	}

	public String getPrimaryIP() {
		return primaryIP;
	}

	public String getTopicName() {
		return topicName;
	}

	public String getClientID() {
		return clientID;
	}

	// constructor
	public QueuedTCPMessageConsumer() throws Exception
	{
		// perform any necessary cleanup prior to shutting down!
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					System.exit(0);
				}
			});
		} catch (Throwable e)
		{
			log.error("QueuedTCPMessageConsumer - Could not add shutdown hook", e);
		} 
	}

	public void run()
	{
		try
		{
			System.out.println("About to execute run method in TCP based consumer.");
			InputStream is;
			Socket socket;
			long msgCount = 0;
			long curTime = 0;
			long beginTime = 0;
			int portNumber = 4444;
			int portIndex = primaryIP.indexOf(":");
			String ipAddress = "";
			if (portIndex > 0) {
				try {
					portNumber = Integer.parseInt(primaryIP.substring(portIndex+1));
				}
				catch (NumberFormatException nfe) {}

				ipAddress = primaryIP.substring(0,portIndex);            
			}
			else {
				ipAddress = primaryIP;
			}
			socket = new Socket(ipAddress,portNumber);
			socket.setReceiveBufferSize(1024 * 1024);
			is = new BufferedInputStream(socket.getInputStream(),50*1024*1024);
			byte[] response = new byte[1024];
			int messageTerminatorByte = 63;
			int bytesRead = 0;
			msgCount = 0;
			curTime=0;
			beginTime=0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(110);
			messageHandler.setQueue(queue);
			while (true) {
				try {
					bytesRead = is.read(response);
					if (bytesRead < 0) {
						System.out.println("Connection broken, exiting run method.");
						break;
					}
					for (int i=0; i<bytesRead; i++) {
						if (response[i] == messageTerminatorByte) {
							msgCount++;
							messageHandler.setMessage(baos.toByteArray());
							baos.reset();
							curTime = System.currentTimeMillis();
							if ( (curTime - beginTime) > 1000) {
								beginTime = curTime;
								System.out.println(new Date()+" Messages received : "+msgCount);
								msgCount = 0;
							}
						}
						else {
							baos.write(response,i,1);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			log.error("QueuedTCPMessageConsumer.run() " + e.getMessage(),e);
		}
	}
}
