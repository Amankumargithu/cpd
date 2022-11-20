package ntp.tsqstr;

import ntp.ibus.IbusDispatcherTSQ;
import ntp.util.ExchangeMapPopulator;

public class TSQCPD {

	public static void main(String[] args) {
		IbusDispatcherTSQ dispacther = new IbusDispatcherTSQ();
		dispacther.startDispatcher("TSQStreamer", TsqQTMessageQueue.getInstance().getmQueue());
		ExchangeMapPopulator.getInstance();
		TSQSubscriptionManager tsqSubscriptionManager = TSQSubscriptionManager.getInstance();
		tsqSubscriptionManager.initChannels();
		tsqSubscriptionManager.subscribeTSQSymbol("ULTRACACHE_NEW-ISSUES");
	}
}
