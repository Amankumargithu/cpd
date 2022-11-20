package ntp.indicesmf;

import java.util.ArrayList;
import java.util.Collection;

import ntp.logger.NTPLogger;

public class IndicesMFSubscriptionManager {
	private int numberOfChannels = 6;
	private int channelCount = 0;
	private IndicesMFSubsChannel [] channelArray = null;

	public IndicesMFSubscriptionManager(int numberofChannels) 
	{
		this.numberOfChannels = numberofChannels;
		channelArray = new IndicesMFSubsChannel[numberofChannels];
		for (int i = 0; i < numberOfChannels; i++) {
			IndicesMFSubsChannel channel = new IndicesMFSubsChannel("Inxchan " + (i + 1));	
			channelArray[i] = channel;
		}
	}

	public void subscribeTickers(Collection<String> tickerList) 
	{		
		ArrayList<String> cloneList = new ArrayList<String>();
		cloneList.addAll(tickerList);
		IndicesMFSubsChannel channel = channelArray[channelCount];
		channelCount ++;
		channel.subscribeTickers(cloneList);
		NTPLogger.info("Subscription request sent to " + channel.getName() +"  for  "+ tickerList.size()+ " tickers.");
	}	
}
