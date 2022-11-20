package ntp.bean;

import QuoddFeed.util.UltraChan;

/**
 * 
 * @author Aman
 *	Stores the streamID and the channel where ticker was subscribed
 */
public class TickerChannelBean {

	private int streamID;
	private UltraChan channel;

	public int getStreamID() {
		return streamID;
	}

	public void setStreamID(int streamID) {
		this.streamID = streamID;
	}

	public UltraChan getChannel() {
		return channel;
	}

	public void setChannel(UltraChan channel) {
		this.channel = channel;
	}
}
