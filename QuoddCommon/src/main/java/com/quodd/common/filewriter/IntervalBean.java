package com.quodd.common.filewriter;

public class IntervalBean {
	private long messageTimestamp;
	private byte[] message;
	private String messageDate;

	public IntervalBean(long timestamp, byte[] message) {
		this.messageTimestamp = timestamp;
		this.message = message;
		this.messageDate = null;
	}

	public IntervalBean(long timestamp, byte[] message, String date) {
		this.messageTimestamp = timestamp;
		this.message = message;
		this.messageDate = date;
	}

	public long getMessageTimestamp() {
		return this.messageTimestamp;
	}

	public byte[] getMessage() {
		return this.message;
	}

	public String getMessageDate() {
		return messageDate;
	}

}
