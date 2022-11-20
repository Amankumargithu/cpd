package com.quodd.bean;

public class BaseBean {
	private int protocolId;
	private int channelIndex;
	private int messageFlag;
	private long sequenceNumber;
	private long seconds;

	public int getProtocolId() {
		return this.protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}

	public int getChannelIndex() {
		return this.channelIndex;
	}

	public void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
	}

	public int getMessageFlag() {
		return this.messageFlag;
	}

	public void setMessageFlag(int messageFlag) {
		this.messageFlag = messageFlag;
	}

	public long getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public long getSeconds() {
		return this.seconds;
	}

	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}
}
