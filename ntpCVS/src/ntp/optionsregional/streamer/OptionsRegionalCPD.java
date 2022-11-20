package ntp.optionsregional.streamer;

import ntp.ibus.IbusDispatcher;
import ntp.optionsregional.cache.OptionsRegionalQTMessageQueue;
import ntp.optionsregional.snap.OptionsRegionalSnapController;
import ntp.util.OptionsRegionalExchangesPopulator;

public class OptionsRegionalCPD {

	public static void main(String[] args) {
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("OptionRegionalStreamer", OptionsRegionalQTMessageQueue.getInstance().getmQueue());
		OptionsRegionalExchangesPopulator.getDefaultInstance();
		OptionsRegionalStreamingController streamingController = OptionsRegionalStreamingController.getInstance();
		streamingController.initChannels();
		streamingController.addRequestedSymbol("ULTRACACHE_NEW-ISSUES");
		OptionsRegionalSnapController.getInstance();

	}
}
