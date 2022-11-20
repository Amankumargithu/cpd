/**
 * QuoddUserEntitlementBO.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Tacpoint Technologies, Inc.
 * @version 1.0
 * Date created:  8/7/2002
 */
package com.b4utrade.bean;

import com.b4utrade.network.*;

import com.tacpoint.exception.*;
import com.tacpoint.common.DefaultObject;


public class StreamerInitializationBean extends DefaultObject
{

    private String streamerClassName;
    private String udpBindAddress;
    private String streamerId;
    private int udpPort;
    public static final String ACTION_KEY = "ACTION";
    public static final String BASIC_AUTH_KEY="Authorization";
    public StreamerInitializationBean()
    {
    }


    public void setStreamerClassName(String streamerClassName)
    {
         this.streamerClassName = streamerClassName;
    }

    public String getStreamerClassName()
    {
         return(this.streamerClassName);
    }

    public void setStreamerId(String streamerId)
    {
         this.streamerId = streamerId;
    }

    public String getStreamerId()
    {
         return(this.streamerId);
    }

    public void setUdpBindAddress(String udpBindAddress)
    {
         this.udpBindAddress = udpBindAddress;
    }

    public String getUdpBindAddress()
    {
         return(this.udpBindAddress);
    }

    public void setUdpPort(int udpPort)
    {
         this.udpPort = udpPort;
    }

    public int getUdpPort()
    {
         return(this.udpPort);
    }


}
