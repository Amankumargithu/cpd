package ntp.tsqdb.writer;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.ExchangeMapPopulator;
import ntp.util.StockRetriever;

public class TsqDbCPD {

	public static void main(String[] args) {
		ExchangeMapPopulator.getInstance();
		String exchg = CPDProperty.getInstance().getProperty("EXCHG");
		NTPLogger.info("Exchanges are: " + exchg);
		String[] insts = exchg.split(",");
		for (int i = 0; i < insts.length; i++) 
		{
			String exchange = insts[i];
			/*if(exchange.equals("TIER"))
				TierTickerRetriever.getInstance();
			else*/
				StockRetriever.getInstance().populateTickerCache(exchange);
		}
		TsqDbSubsManager tsqSubscriptionManager = TsqDbSubsManager.getInstance();
		NTPLogger.info("Starting Thread to subscribe all tickers");
		tsqSubscriptionManager.subscribeAll();
	}
}
