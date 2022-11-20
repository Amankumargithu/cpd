package ntp.ejb;

import java.util.HashSet;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.futureOptions.streamer.FutureOptionsStreamingController;
import ntp.logger.NTPLogger;

import com.b4utrade.ejb.OptionSubscriber;

@Remote(OptionSubscriber.class)
@Stateless(mappedName="ejbCache/OptionSubscriber")
public class FutureOptionsSubscribeEJB implements OptionSubscriber{

	@Override
	public void subscribe(String id, Object[] keys) {
		NTPLogger.requestEJB("FutOpSub");
		long s = System.currentTimeMillis();
		HashSet<String> set = new HashSet<>();
		for(Object o : keys)
			set.add((String)o);
		NTPLogger.info("Subscribed Tickers - " + set.size()); 
		FutureOptionsStreamingController.getInstance().addRequestedSymbols(set);
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("FutOpSub", t-s);
	}
}
