/**
  * TPublisherConfigBean.java
  *
  * @author Copyright (c) 2003 by Tacpoint Technologie, Inc.
  *    All rights reserved.
  * @version 1.0
  * Created on Oct 16, 2003
  */
package com.tacpoint.publisher;

import java.util.Vector;

public class TPublisherConfigBean
{
   private int pushPort;
   private int acceptThreadPoolCount;
   private int selectorThreadCount;
   private long aggregationTime;
   private String  subscriptionAnalyzer;

   private String  optionsJndiAddress;
   private String  futuresJndiAddress;
   private String  futuresOptionsJndiAddress;
   private String  optionsRegionalJndiAddress;
   private String  equityMontageJndiAddress;
   private String  level2JndiAddress;

   private String  udpHandler;
   private String  udpPort;
   private String  udpTtl;
   private String  udpBindAddress;
   private String  streamerId;

   private boolean  runJmsConsumer;
   private String  equityUrl;
   private String  optionUrl;
   private String  messageInflator;
   private String  userId;

   private Vector clientID;
   private Vector primaryIP;
   private Vector consumerName;

   private Vector topicName;
   private Vector qos;
   private Vector messageHandler;
   private Vector messageReducer;
   private Vector consumer;

   private String authenticationClass;
   private String heartbeatTopic;
   private int    heartbeatIntervalInSeconds;
   private String heartbeatMessage;
   private int    estOffset;

   private String heartbeat1Topic;
   private String heartbeat1Message;
   
   private boolean requiresAuthentication;
   private String authUserName;
   private String authPassword;
   private String streamerClassName;
   private String  selectorClassName;

   public TPublisherConfigBean()
   {
   }

   public int getPushPort()               { return pushPort; }
   public int getAcceptThreadPoolCount()  { return acceptThreadPoolCount; }
   public int getSelectorThreadCount()  { return selectorThreadCount; }
   public long getAggregationTime()  { return aggregationTime; }
   public boolean getRunJmsConsumer() { return runJmsConsumer; }
   public String getMessageInflator() { return messageInflator; }
   public String getSubscriptionAnalyzer() { return subscriptionAnalyzer; }
   public String getUdpHandler() { return udpHandler; }

   public String getUdpPort() { return udpPort; }
   public String getUdpTtl() { return udpTtl; }
   public String getUdpBindAddress() { return udpBindAddress; }
   public String getStreamerId() { return streamerId; }

   public String getOptionsJndiAddress() { return optionsJndiAddress; }
   public String getLevel2JndiAddress() { return level2JndiAddress; }
   public String getOptionsRegionalJndiAddress() { return optionsRegionalJndiAddress; }
   public String getEquityUrl() { return equityUrl; }
   public String getOptionUrl() { return optionUrl; }
   public String getUserId() { return userId; }

   public Vector  getClientID()         { return clientID; }
   public Vector  getConsumerName()     { return consumerName; }
   public Vector  getPrimaryIP()        { return primaryIP; }
   public Vector getQos()             { return qos; }
   public Vector getMessageHandler()  { return messageHandler; }
   public Vector getMessageReducer()  { return messageReducer; }
   public Vector getConsumer()  { return consumer; }
   public Vector getTopicName()       { return topicName; }

   public String getAuthenticationClass()          { return authenticationClass; }
   public String getHeartbeatTopic()               { return heartbeatTopic; }



public String getHeartbeat1Topic() {
	return heartbeat1Topic;
}

public void setHeartbeat1Topic(String heartbeat1Topic) {
	this.heartbeat1Topic = heartbeat1Topic;
}

public String getHeartbeat1Message() {
	return heartbeat1Message;
}

public void setHeartbeat1Message(String heartbeat1Message) {
	this.heartbeat1Message = heartbeat1Message;
}

public int    getHeartbeatIntervalInSeconds()   { return heartbeatIntervalInSeconds; }
   public int    getEstOffset()   { return estOffset; }
   public String getHeartbeatMessage()             { return heartbeatMessage; }

   public boolean getRequiresAuthentication()      { return requiresAuthentication; }
   public String getAuthUserName()      { return authUserName; }
   public String getAuthPassword()      { return authPassword; }
   public String getStreamerClassName()      { return streamerClassName; }
   public String getSelectorClassName()      { return selectorClassName; }

   public void setPushPort(int i)               { pushPort = i; }
   public void setAcceptThreadPoolCount(int i)  { acceptThreadPoolCount = i; }
   public void setSelectorThreadCount(int i)  { selectorThreadCount = i; }
   public void setAggregationTime(long l)  { aggregationTime = l; }
   public void setRunJmsConsumer(boolean doRun) { runJmsConsumer = doRun; }
   public void setMessageInflator(String mi) { this.messageInflator = mi; }
   public void setSubscriptionAnalyzer(String sa) { this.subscriptionAnalyzer = sa; }

   public void setUdpHandler(String udp) { this.udpHandler = udp; }
   public void setUdpPort(String port) { udpPort = port; }
   public void setUdpTtl(String ttl) { udpTtl = ttl; }
   public void setUdpBindAddress(String bindAddress) { udpBindAddress = bindAddress; }
   public void setStreamerId(String streamerId) { this.streamerId = streamerId; }


   public void setOptionsJndiAddress(String jndiAddress) { optionsJndiAddress = jndiAddress; }
   
   public void setLevel2JndiAddress(String jndiAddress) { level2JndiAddress = jndiAddress; }
   
   public void setOptionsRegionalJndiAddress(String jndiAddress) { optionsRegionalJndiAddress = jndiAddress; }
   public void setEquityUrl(String url) { this.equityUrl = url; }
   public void setOptionUrl(String url) { this.optionUrl = url; }
   public void setUserId(String id) { this.userId = id; }

   public void setClientID(Vector v)         { clientID = v; }
   public void setConsumerName(Vector v)     { consumerName = v; }
   public void setPrimaryIP(Vector v)        { primaryIP = v; }
   public void setQos(Vector v)             { qos = v; }
   public void setMessageHandler(Vector v)             { messageHandler = v; }
   public void setMessageReducer(Vector v)             { messageReducer = v; }
   public void setConsumer(Vector v)             { consumer = v; }
   public void setTopicName(Vector v)       { topicName = v; }

   public void setAuthenticationClass(String s)       { authenticationClass = s; }
   public void setHeartbeatTopic(String s)            { heartbeatTopic = s; }
   public void setHeartbeatIntervalInSeconds(int i)   { heartbeatIntervalInSeconds = i; }
   public void setEstOffset(int i)   { estOffset = i; }
   public void setHeartbeatMessage(String s)          { heartbeatMessage = s; }

   public void setRequiresAuthentication(boolean ra)  { requiresAuthentication = ra; }
   public void setAuthUserName(String aun)  { authUserName = aun; }
   public void setAuthPassword(String ap)   { authPassword = ap; }
   public void setStreamerClassName(String scn)   { streamerClassName = scn; }
   public void setSelectorClassName(String scn)   { selectorClassName = scn; }

public String getEquityMontageJndiAddress() {
	return equityMontageJndiAddress;
}

public void setEquityMontageJndiAddress(String equityMontageJndiAddress) {
	this.equityMontageJndiAddress = equityMontageJndiAddress;
}

public String getFuturesJndiAddress() {
	return futuresJndiAddress;
}

public void setFuturesJndiAddress(String futureJndiAddress) {
	this.futuresJndiAddress = futureJndiAddress;
}

public String getFuturesOptionsJndiAddress() {
	return futuresOptionsJndiAddress;
}

public void setFuturesOptionsJndiAddress(String futuresOptionsJndiAddress) {
	this.futuresOptionsJndiAddress = futuresOptionsJndiAddress;
}
}
