package ntp.options.streamer;

import ntp.ibus.IbusDispatcher;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.options.snap.OptionsSnapController;

public class OptionsCPD {

	public static void main(String[] args) {
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("OptionStreamer", OptionsQTMessageQueue.getInstance().getmQueue());
		OptionsStreamingController streamingController = OptionsStreamingController.getInstance();
		streamingController.initChannels();
		streamingController.addRequestedSymbol("ULTRACACHE_NEW-ISSUES");
		OptionsSnapController.getInstance();
	}
}
