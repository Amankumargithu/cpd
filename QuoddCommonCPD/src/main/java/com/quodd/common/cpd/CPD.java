package com.quodd.common.cpd;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.quodd.common.cpd.channel.CPDChannelManager;
import com.quodd.common.cpd.util.Stats;
import com.quodd.common.ibus.IbusDispatcher;
import com.quodd.common.ibus.IbusDispatcherLegacy;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.QuoddProperty;

public abstract class CPD {
	public static final Logger logger = QuoddLogger.getInstance("cpdLog").getLogger();
	public static final Gson gson = new Gson();
	public static final QuoddProperty cpdProperties = new QuoddProperty("/cpd.properties");
	public static final CPDChannelManager channelManager = new CPDChannelManager();
	public static final Stats stats = new Stats(System.currentTimeMillis());
	public static final IbusDispatcher tradeDispacther = new IbusDispatcher();
	public static final IbusDispatcher quoteDispacther = new IbusDispatcher();
	public static final IbusDispatcherLegacy legacyDispacther = new IbusDispatcherLegacy();
	
}