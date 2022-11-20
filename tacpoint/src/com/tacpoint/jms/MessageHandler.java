package com.tacpoint.jms;

import com.tacpoint.publisher.TMessageQueue;

public class MessageHandler implements Runnable, Cloneable {

    public void setMessage(byte[] message){}

    public void setQueue(TMessageQueue queue){}

    public void setMessage(javax.jms.Message message){}

    public javax.jms.Message getMessage() {
       return null;
    }

    public Object clone() throws CloneNotSupportedException {
       return super.clone();
    }

    public Object process(javax.jms.Message message) {
       return null;
    }

    public Object process(com.tacpoint.publisher.TMessage message) {
        return null;
    }
    
    public void run() {
       return;
    }
    
}
