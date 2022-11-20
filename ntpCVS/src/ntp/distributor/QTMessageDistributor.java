package ntp.distributor;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;

public class QTMessageDistributor implements Runnable {
	private IDistributionHandler handler = null;
	private MappedMessageQueue messageQueue;

	public void setMessageQueue(MappedMessageQueue messageQueue) {
		this.messageQueue = messageQueue;
	}

	public MappedMessageQueue getMessageQueue() {
		return messageQueue;
	}

	public void setDistributionHandler(IDistributionHandler handler)
	{
		this.handler = handler;
	}

	public QTMessageDistributor() {}

	public void run()
	{
		if (messageQueue == null) {
			NTPLogger.warning("QTMessageDistributor.run - Message Queue is null.  Needs to be set externally!");
			System.exit(0);
		}
		if (handler != null)
		{
			try
			{
				QTCPDMessageBean qtmb = null;
				int MAX_RECORDS;
				int surgeThreshold = 40000;
				int conflationTime = 40;
				try
				{
					conflationTime = Integer.parseInt(CPDProperty.getInstance().getProperty("CONFLATION_TIME"));
					NTPLogger.info("QTMessageDistributor- Conflation time: "+ conflationTime);
				}
				catch (Exception e) 
				{
					NTPLogger.missingProperty("CONFLATION_TIME");
					NTPLogger.defaultSetting("CONFLATION_TIME", "" + conflationTime);
				}
				try
				{
					surgeThreshold = Integer.parseInt(CPDProperty.getInstance().getProperty("SURGE_THRESHOLD"));
					NTPLogger.info("QTMessageDistributor- Surge threshold: " + surgeThreshold);
				}
				catch (Exception e) 
				{
					NTPLogger.missingProperty("SURGE_THRESHOLD");
					NTPLogger.defaultSetting("SURGE_THRESHOLD", "" + surgeThreshold);
				}
				while (true)
				{
					try 
					{
						Thread.sleep(conflationTime);
					}
					catch (Exception e) {}
					Object[] elements = messageQueue.removeAll();
					if (elements == null)
					{
						NTPLogger.info("QTMessageDistributor - elements retrieved from message Queue are null");
						continue;
					}
					//handle the surge
					if (elements.length > surgeThreshold) {
						NTPLogger.info("QTMessageDistributor - elements count : "+elements.length+" resetting max message count to prevent heap enlargement.");
						MAX_RECORDS = surgeThreshold;
					}
					else 
					{
						MAX_RECORDS = elements.length;
					}	
					for (int i=0; i<MAX_RECORDS; i++)
					{
						qtmb = (QTCPDMessageBean)elements[i];
						handler.handleMessage(qtmb);
						elements[i] = null;
					}
					elements = null;
				}
			} catch(Exception error)
			{
				NTPLogger.error("QTMessageDistributor:run(): " + error.getMessage());
			}
			catch(Error er)
			{
				NTPLogger.error("QTMessageDistributor:run(): " + er.getMessage());
			}
		}
	}
}
