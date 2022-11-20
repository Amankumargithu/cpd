package ntp.futures.streamer;

import java.util.HashSet;

import ntp.futures.cache.FuturesMappingSymbolsCache;
import ntp.futures.cache.FuturesQTMessageQueue;
import ntp.futures.snap.FuturesSnapController;
import ntp.futures.snap.FuturesSymbolMappingInitializer;
import ntp.ibus.IbusDispatcher;

public class FuturesCPD {

	public static void main(String[] args) {
		IbusDispatcher dispacther = new IbusDispatcher();
		dispacther.startDispatcher("FuturesStreamer", FuturesQTMessageQueue.getInstance().getmQueue());
		FuturesStreamingController streamingController = FuturesStreamingController.getInstance();
		streamingController.initChannels();
		
		FuturesSnapController.getInstance();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				FuturesSnapController snapController = FuturesSnapController.getInstance();
				HashSet<String> rootSet = snapController.getFuturesRootSymbols();
				for(String s : rootSet)
					FuturesMappingSymbolsCache.getInstance().getFuturesChain(s);
			}
		}, "FuturesTickerLookUp");
		t.start();
		
		FuturesSymbolMappingInitializer mappingInitializer = new FuturesSymbolMappingInitializer();
		mappingInitializer.mapSymbols();
	}
}
