package ntp.ibus;

import ntp.distributor.IDistributionHandler;
import ntp.distributor.QTMessageDistributor;
import ntp.queue.MappedMessageQueue;

public class IbusDispatcher {
	private QTMessageDistributor distributor = null;
	private Thread distributorThread;
	
	public void startDispatcher(String cpdType, MappedMessageQueue queue)
	{
		try 
		{
			distributor = new QTMessageDistributor();
			IDistributionHandler messagehandler = null;
			String messagehandlerClassname = null;
			if (messagehandlerClassname == null)
			{
				messagehandlerClassname = "ntp.distributor.QTJMSMessageProducer";
			}
			Class messagehandlerClass = Class.forName(messagehandlerClassname);
			messagehandler = (IDistributionHandler)messagehandlerClass.newInstance();
			distributor.setDistributionHandler(messagehandler);
			distributor.setMessageQueue(queue);
			distributorThread = new Thread(distributor, cpdType + "_Distributer");
			distributorThread.start();
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
