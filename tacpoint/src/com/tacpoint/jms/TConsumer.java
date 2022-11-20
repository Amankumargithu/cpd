package com.tacpoint.jms;


import com.tacpoint.publisher.TMessageQueue;

public interface TConsumer {


   public void setAggregationTime(long at);

   public void setMessageQueue(TMessageQueue queue);

   public void setConsumerName(String consumerName);

   public void setQOS(String qos);

   public void setMessageHandler(MessageHandler messageHandler);

   public void setMessageReducer(MessageReducer messageReducer);

   public void setMessageInflator(ReducedMessageInflator messageInflator);

   public void setClientID(String clientID);

   public void setTopicName(String topicName);

   public void setPrimaryIP(String primaryIP);

   public String getConsumerName();
   

}
