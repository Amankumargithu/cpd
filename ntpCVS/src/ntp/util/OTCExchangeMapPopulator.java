package ntp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

import ntp.logger.NTPLogger;

public class OTCExchangeMapPopulator 
{
	private HashSet<String> exchangeSet = new HashSet<String>();
	private static volatile OTCExchangeMapPopulator instance = new OTCExchangeMapPopulator();
	private static HashSet<String> otcExchangeIds ;

	private OTCExchangeMapPopulator() {
		populateExchangeMap();
		otcExchangeIds = new HashSet<>();
		otcExchangeIds.add("XOTC");
		otcExchangeIds.add("OOTC");
		otcExchangeIds.add("OTCB");
		otcExchangeIds.add("OTCQ");
		otcExchangeIds.add("PINX");
		otcExchangeIds.add("PSGM");
	}

	public static OTCExchangeMapPopulator getInstance()
	{
		if(instance == null)
			instance = new OTCExchangeMapPopulator();
		return instance;
	}

	private void populateExchangeMap()
	{
		try
		{
			String exchangeFilePath = CPDProperty.getInstance().getProperty("OTC_EXCHANGE_MAPPING_FILE");
			if(exchangeFilePath != null)
			{
				NTPLogger.info("Loading Exchange Mapping from " + exchangeFilePath);
				File file = new File(exchangeFilePath);
				if(file.exists())
				{
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String result = null;
					while( (result = reader.readLine()) != null)
					{
						if(result != null && result.length() > 0)							
							exchangeSet.add(result);
					}
					reader.close();
				}
				else
					NTPLogger.missingProperty("OTC_EXCHANGE_MAPPING_FILE");
			}
			else
			{
				NTPLogger.missingProperty("OTC_EXCHANGE_MAPPING_FILE");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	public HashSet<String> getExchanges()
	{
		return exchangeSet;
	}
	
	public boolean isOtcExchangeId(String tier)
	{
		return otcExchangeIds.contains(tier);
	}
}
