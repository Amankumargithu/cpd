package ntp.ejb;

import java.util.HashSet;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.futures.streamer.FuturesStreamingController;
import ntp.logger.NTPLogger;

import com.b4utrade.ejb.OptionSubscriber;

@Remote(OptionSubscriber.class)
@Stateless(mappedName="ejbCache/OptionSubscriber")
public class FuturesSubscribeEJB implements OptionSubscriber{

	@Override
	public void subscribe(String id, Object[] keys) {
		NTPLogger.requestEJB("FutSub");
		long s = System.currentTimeMillis();
		HashSet<String> set = new HashSet<>();
		for(Object o : keys)
			set.add((String)o);
		FuturesStreamingController.getInstance().addRequestedSymbols(set);
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("FutSub", t-s);
	}
}
