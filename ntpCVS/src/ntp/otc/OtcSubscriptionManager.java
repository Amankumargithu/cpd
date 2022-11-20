package ntp.otc;

import java.util.ArrayList;
import java.util.Collection;

public class OtcSubscriptionManager {

	private int numberOfChannels = 5;
	private int channelCount = 0;
	private OtcSubscriptionChannel[] channelArray = null;

	public OtcSubscriptionManager(int channels)
	{
		this.numberOfChannels = channels;
		channelArray = new OtcSubscriptionChannel[channels];
		for (int i = 0; i < numberOfChannels; i++) {
			OtcSubscriptionChannel channel = new OtcSubscriptionChannel("OTCChan " + (i + 1), this);	
			channelArray[i] = channel;
		}
	}

	public void subscribeTickers(Collection<String> tickerList) 
	{		
		ArrayList<String> cloneList = new ArrayList<String>();
		cloneList.addAll(tickerList);
		OtcSubscriptionChannel channel = channelArray[channelCount];
		channelCount ++;
		channel.subscribeTickers(cloneList);
	}
}