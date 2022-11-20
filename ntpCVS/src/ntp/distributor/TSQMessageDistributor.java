package ntp.distributor;

import ntp.bean.TSQBean;
import ntp.logger.NTPLogger;
import ntp.queue.VectorMessageQueue;
import ntp.util.CPDProperty;

public class TSQMessageDistributor implements Runnable {

	private IDistributionHandlerTSQ handler = null;
	private VectorMessageQueue messageQueue;

	public void setMessageQueue(VectorMessageQueue messageQueue) {
		this.messageQueue = messageQueue;
	}

	public VectorMessageQueue getMessageQueue() {
		return messageQueue;
	}

	public void setDistributionHandler(IDistributionHandlerTSQ handler)
	{
		this.handler = handler;
	}

	public TSQMessageDistributor() {}

	public void run()
	{
		if (messageQueue == null) {
			NTPLogger.warning("QTMessageDistributorTSQ.run - Message Queue is null.  Needs to be set externally!");
			System.exit(0);
		}
		if (handler != null)
		{
			try
			{
				TSQBean tsqBean = null;
				int MAX_RECORDS;
				int surgeThreshold = 40000;
				int conflationTime = 40;
				try
				{
					conflationTime = Integer.parseInt(CPDProperty.getInstance().getProperty("CONFLATION_TIME"));
					NTPLogger.info("QTMessageDistributorTSQ- Conflation time: "+ conflationTime);
				}
				catch (Exception e) 
				{
					NTPLogger.missingProperty("CONFLATION_TIME");
					NTPLogger.defaultSetting("CONFLATION_TIME", "" + conflationTime);
				}
				try
				{
					surgeThreshold = Integer.parseInt(CPDProperty.getInstance().getProperty("SURGE_THRESHOLD"));
					NTPLogger.info("QTMessageDistributorTSQ- Surge threshold: " + surgeThreshold);
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
						NTPLogger.info("QTMessageDistributorTSQ - elements retrieved from message Queue are null");
						continue;
					}
					//handle the surge
					if (elements.length > surgeThreshold) {
						NTPLogger.info("QTMessageDistributorTSQ - elements count : "+elements.length+" resetting max message count to prevent heap enlargement.");
						MAX_RECORDS = surgeThreshold;
					}
					else 
					{
						MAX_RECORDS = elements.length;
					}	
					for (int i=0; i<MAX_RECORDS; i++)
					{
						tsqBean = (TSQBean)elements[i];
						handler.handleMessage(tsqBean);
						elements[i] = null;
					}
					elements = null;
				}
			} catch(Exception error)
			{
				NTPLogger.error("QTMessageDistributorTSQ:run(): " + error.getMessage());
			}
			catch(Error er)
			{
				NTPLogger.error("QTMessageDistributorTSQ:run(): " + er.getMessage());
			}
		}
	}
}
