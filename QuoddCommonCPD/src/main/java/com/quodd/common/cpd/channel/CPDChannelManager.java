package com.quodd.common.cpd.channel;

import static com.quodd.common.cpd.CPD.logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class CPDChannelManager {

	private int numberOfChannels = 6;
	private int channelCount = 0;
	private List<CPDChannel> channelList = null;
	private Map<String, CPDChannel> channelNameMap;

	public CPDChannelManager() {
		this.channelNameMap = new HashMap<>();
	}

	public void init(List<CPDChannel> channelListArg) {
		this.channelList = Objects.requireNonNull(channelListArg);
		this.numberOfChannels = channelListArg.size();
		for (CPDChannel c : this.channelList)
			this.channelNameMap.put(c.getName(), c);
	}

	public void subscribeTickers(Set<String> tickerList) {
		Objects.requireNonNull(tickerList);
		logger.info(() -> "Tickers subscribed in CPDChannelManager " + this.channelCount + " = " + tickerList.size());
		CPDChannel channel = this.channelList.get(this.channelCount);
		this.channelCount = (++this.channelCount) % this.numberOfChannels;
		if (channel != null)
			channel.subscribeTickers(tickerList);
	}

	public CPDChannel getChannelByName(String channelName) {
		return this.channelNameMap.get(channelName);
	}

	public void stopChannels() {
		for (CPDChannel c : this.channelList) {
			try {
				c.stopChannel();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
}
