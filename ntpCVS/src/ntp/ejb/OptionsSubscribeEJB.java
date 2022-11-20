package ntp.ejb;

import java.util.HashSet;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.logger.NTPLogger;
import ntp.options.streamer.OptionsStreamingController;

import com.b4utrade.ejb.OptionSubscriber;

@Remote(OptionSubscriber.class)
@Stateless(mappedName = "ejbCache/OptionSubscriber")
public class OptionsSubscribeEJB implements OptionSubscriber {

	@Override
	public void subscribe(String id, Object[] keys) {
		NTPLogger.requestEJB("OpSub");
		long s = System.currentTimeMillis();
		HashSet<String> set = new HashSet<>();
		for (Object o : keys)
			set.add((String) o);
		OptionsStreamingController.getInstance().addRequestedSymbols(set);
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpSub", t - s);
	}
}
