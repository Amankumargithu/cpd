package ntp.equity;

import java.util.Collection;

import ntp.util.CPDProperty;

public class EquitySubscriptionManager {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private EquitySubscriptionChannel[] channelArray = null;
	private NBDelayedEquityChannel[] delayedChannelArray = null;
	private boolean isNasdaqbasic = false;

	public EquitySubscriptionManager(int channels, boolean isNasdaqBasic)
	{
		this.numberOfChannels = channels;
		this.isNasdaqbasic = isNasdaqBasic;
		channelArray = new EquitySubscriptionChannel[channels];
		for (int i = 0; i < numberOfChannels; i++) {
			EquitySubscriptionChannel channel = new EquitySubscriptionChannel("EqtyChan " + (i + 1));	
			channelArray[i] = channel;
		}
		if(isNasdaqbasic)
		{
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
	}

	public void subscribeTickers(Collection<String> tickerList) 
	{		
		EquitySubscriptionChannel channel = channelArray[channelCount];
		channelCount ++;
		channel.subscribeTickers(tickerList);
	}
	
	public void subscribeVolumePlusTickers(Collection<String> tickerList, int channelCount)
	{
		NBDelayedEquityChannel nbChannel = delayedChannelArray[channelCount];
		nbChannel.subscribeTickers(tickerList);
	}
}
