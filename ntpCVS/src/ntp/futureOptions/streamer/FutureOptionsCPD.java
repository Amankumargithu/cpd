package ntp.futureOptions.streamer;

import ntp.futureOptions.cache.FutureOptionsQTMessageQueue;
import ntp.futureOptions.snap.FutureOptionsSnapController;
import ntp.ibus.IbusDispatcher;

public class FutureOptionsCPD {

	public static void main(String[] args) {
		
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("FutureOptionStreamer", FutureOptionsQTMessageQueue.getInstance().getmQueue());
		
		FutureOptionsStreamingController streamingController = FutureOptionsStreamingController.getInstance();
		streamingController.initChannels();
		streamingController.addRequestedSymbol("ULTRACACHE_NEW-ISSUES");
		FutureOptionsSnapController.getInstance();
	}
}
