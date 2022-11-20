package ntp.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import ntp.logger.NTPLogger;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class OptionsExchangeMapPopulator 
{
	private static final int NASDAQ_EXCHANGE_CODE_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private static final int EQPLUS_EXCHANGE_CODE_POS = 3;
	private HashMap<String, String> exchangeMap = new HashMap<String, String>();
	private static volatile OptionsExchangeMapPopulator instance = new OptionsExchangeMapPopulator();

	private OptionsExchangeMapPopulator() {
		populateExchangeMap();
	}

	public static OptionsExchangeMapPopulator getInstance()
	{
		return instance;
	}

	private void populateExchangeMap()
	{
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "ExchPop", "ExchPop", false);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do
		{
			NTPLogger.requestUC("SyncGetExchanges", "", ++resubscribeCount);
			table = query.SyncGetExchanges(new Object());
			NTPLogger.responseUC("SyncGetExchanges", "", table != null ? table.len():0);
		}
		while(table != null && table.len() == 0 && resubscribeCount < 2);
		if(table != null && table.len() == 0 && resubscribeCount == 2)
			NTPLogger.syncAPIOverrun("SyncGetExchanges", resubscribeCount);
		query.Stop(); 
		parseBlobTable(table);
	}

	private void parseBlobTable(BlobTable blobTable)
	{
		int rowCount = blobTable.nRow();
		for (int count = 0; count < rowCount; count++)
		{
			String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
			if(! instrument.startsWith("OPR"))
				continue;
			String nazExchgCode = blobTable.GetCell(count, NASDAQ_EXCHANGE_CODE_POS).toUpperCase();
			String eqPlusExchgCode = blobTable.GetCell(count, EQPLUS_EXCHANGE_CODE_POS).toUpperCase();
			exchangeMap.put(nazExchgCode, eqPlusExchgCode);
			NTPLogger.info("Exchange " + count + ") " + nazExchgCode + " : " + eqPlusExchgCode);
		}
	}

	public HashMap<String, String> getExchangeMap()
	{
		return exchangeMap;
	}

	public Collection<String> getExchanges()
	{
		HashSet<String> exchanges = new HashSet<String>();
		exchanges.addAll(exchangeMap.values());
		return exchanges;
	}
}