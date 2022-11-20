package ntp.equityregional.streamer;

import ntp.equityregional.cache.EquityRegionalQTMessageQueue;
import ntp.equityregional.snap.EquityRegionalSnapController;
import ntp.ibus.IbusDispatcher;
import ntp.util.EquityRegionalExchangesPopulator;
import ntp.util.ExchangeMapPopulator;

public class EquityRegionalCPD {

	public EquityRegionalCPD() {
	}

	public static void main(String[] args) {
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("EquityRegionalStreamer", EquityRegionalQTMessageQueue.getInstance().getmQueue());
		EquityRegionalExchangesPopulator.getDefaultInstance();
		EquityRegionalStreamingController streamingController = EquityRegionalStreamingController.getInstance();
		streamingController.initChannels();
		streamingController.addRequestedSymbol("ULTRACACHE_NEW-ISSUES");
		EquityRegionalSnapController.getInstance();
		//Populates the exchanges for mapping NASDAQ exchanges to the one we use at equity +
		ExchangeMapPopulator.getInstance();
	}
}