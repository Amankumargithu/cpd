package ntp.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.logger.NTPLogger;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class TierTickerRetriever 
{
	private static final int TICKER_POS = 0;
	private Map<String,String> tickersMap = new ConcurrentHashMap<String,String>();
	private static volatile TierTickerRetriever instance = null;

	private TierTickerRetriever() {
		populateTickerCache();
	}

	public static TierTickerRetriever getInstance()
	{
		if(instance == null)
			instance = new TierTickerRetriever();
		return instance;
	}

	private void populateTickerCache()
	{
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "TierTick", "TierTick", false);
		NTPLogger.connectChannel("TierTick", NTPConstants.IP);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do
		{
			NTPLogger.requestUC("SyncGetExchTickers", "TIER", ++resubscribeCount);
			table = query.SyncGetExchTickers( "TIER", new Object());
			NTPLogger.responseUC("SyncGetExchTickers",  "TIER", table == null? 0 : table.nRow());
		}
		while(table != null && table.len() == 0 && resubscribeCount <3);
		if(table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetExchTickers", resubscribeCount);
		query.Stop(); 
		NTPLogger.disconnectChannel("ExchTick");
		parseBlobTable(table);
	}

	private void parseBlobTable(BlobTable blobTable)
	{
		int rowCount = blobTable.nRow();
		for (int count = 0; count < rowCount; count++)
		{
			String ticker = blobTable.GetCell(count, TICKER_POS);
			try 
			{
				String array[] = ticker.split("/");
				if (NTPTickerValidator.isPinkSheet(ticker))
				{
					tickersMap.put(array[0], array[1]);
					System.out.println(array[0] + "," + array[1]);
				}
			} 
			catch (Exception e) 
			{
				NTPLogger.error(" Ticker " + ticker + " not valid TIER");
			}
		}
		NTPLogger.info("TIER count: " + tickersMap.size());
	}

	public Map<String,String> getTickerMap()
	{
		return tickersMap;
	}
}