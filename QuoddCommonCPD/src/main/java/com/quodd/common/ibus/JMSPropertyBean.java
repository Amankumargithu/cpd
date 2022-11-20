package com.quodd.common.ibus;

public class JMSPropertyBean {

	private String qos;
	private String clientId;
	private String topicName;
	private int conflationTime;
	private int surgeThreshold;

	public JMSPropertyBean(String qos, String clientId, String topicName, int conflationTime, int surgeThreshold) {
		this.qos = qos;
		this.clientId = clientId;
		this.topicName = topicName;
		this.conflationTime = conflationTime;
		this.surgeThreshold = surgeThreshold;
	}

	public String getQos() {
		return this.qos;
	}

	public String getClientId() {
		return this.clientId;
	}

	public String getTopicName() {
		return this.topicName;
	}

	public int getConflationTime() {
		return this.conflationTime;
	}

	public int getSurgeThreshold() {
		return this.surgeThreshold;
	}
}
