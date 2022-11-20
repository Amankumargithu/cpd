package ntp.ibus;

import ntp.distributor.IDistributionHandlerTSQ;
import ntp.distributor.TSQMessageDistributor;
import ntp.queue.VectorMessageQueue;

public class IbusDispatcherTSQ {

	private TSQMessageDistributor distributor = null;
	private Thread distributorThread;
	
	public void startDispatcher(String cpdType, VectorMessageQueue queue)
	{
		try 
		{
			distributor = new TSQMessageDistributor();
			IDistributionHandlerTSQ messagehandler = null;
			String messagehandlerClassname = null;
			if (messagehandlerClassname == null)
			{
				messagehandlerClassname = "ntp.distributor.TSQJMSMessageProducer";
			}
			Class messagehandlerClass = Class.forName(messagehandlerClassname);
			messagehandler = (IDistributionHandlerTSQ)messagehandlerClass.newInstance();
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
