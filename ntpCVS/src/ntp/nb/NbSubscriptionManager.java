package ntp.nb;

import java.util.Collection;

import ntp.util.CPDProperty;

public class NbSubscriptionManager {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private NbSubscriptionChannel[] channelArray = null;
	private NBDelayedEquityChannel[] delayedChannelArray = null;

	public NbSubscriptionManager(int channels)
	{
		this.numberOfChannels = channels;
		channelArray = new NbSubscriptionChannel[channels];
		for (int i = 0; i < numberOfChannels; i++) {
			NbSubscriptionChannel channel = new NbSubscriptionChannel("EqtyChan " + (i + 1));	
			channelArray[i] = channel;
		}
		try
		{
			String delayedIp = CPDProperty.getInstance().getProperty("DELAYED_UC_IP");
			int delayedPort= Integer.parseInt(CPDProperty.getInstance().getProperty("DELAYED_UC_PORT"));
			delayedChannelArray = new NBDelayedEquityChannel[channels];
			for (int i = 0; i < numberOfChannels; i++) {
				NBDelayedEquityChannel channel = new NBDelayedEquityChannel(delayedIp, delayedPort, "DelayedNB_" + i);	
				delayedChannelArray[i] = channel;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void subscribeTickers(Collection<String> tickerList) 
	{		
		NbSubscriptionChannel channel = channelArray[channelCount];
		channelCount ++;
		channel.subscribeTickers(tickerList);
	}

	public void subscribeVolumePlusTickers(Collection<String> tickerList, int channelCount)
	{
		NBDelayedEquityChannel nbChannel = delayedChannelArray[channelCount];
		nbChannel.subscribeTickers(tickerList);
	}
}
