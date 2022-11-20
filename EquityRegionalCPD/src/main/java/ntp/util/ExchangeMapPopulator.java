package ntp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ntp.logger.NTPLogger;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class ExchangeMapPopulator 
{
	private static final int NASDAQ_EXCHANGE_CODE_POS = 0;
	private static final int INSTRUMENT_POS = 2;
	private static final int EQPLUS_EXCHANGE_CODE_POS = 3;
	private HashMap<String, String> exchangeMap = new HashMap<String, String>();
	private HashMap<String, String> uiExchgMap =  new HashMap<String, String>();
	private static volatile ExchangeMapPopulator instance = new ExchangeMapPopulator();

	private ExchangeMapPopulator() {
		populateExchangeMap();
	}

	public static ExchangeMapPopulator getInstance()
	{
		if(instance == null)
			instance = new ExchangeMapPopulator();
		return instance;
	}

	private void populateExchangeMap()
	{
		boolean flag = false;
		try
		{
			String exchangeFilePath = CPDProperty.getInstance().getProperty("EXCHANGE_MAPPING_FILE");
			if(exchangeFilePath != null)
			{
				NTPLogger.info("Loading Exchnage Mapping from " + exchangeFilePath);
				File file = new File(exchangeFilePath);
				if(file.exists())
				{
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String result = null;
					while( (result = reader.readLine()) != null)
					{
						if(result != null && result.length() > 0)
						{
							String[] tmp = result.split(",");
							exchangeMap.put(tmp[0], tmp[1]);
						}
					}
					reader.close();
				}
				else
				{
					NTPLogger.missingProperty("EXCHANGE_MAPPING_FILE");
					flag = true;
				}
			}
			else
			{
				NTPLogger.missingProperty("EXCHANGE_MAPPING_FILE");
				flag = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			flag = true;
		}
		if(exchangeMap.size() == 0)
			flag = true;
		if(flag == true)
		{
			UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "ExchPop", "ExchPop", false);
			NTPLogger.connectChannel("ExchPop", NTPConstants.IP);
			query.Start();
			int resubscribeCount = -1;
			BlobTable table = null;
			do
			{
				NTPLogger.requestUC("SyncGetExchanges", "", ++resubscribeCount);
				table = query.SyncGetExchanges(new Object());
				NTPLogger.responseUC("SyncGetExchanges", "", table == null? 0 : table.nRow());
			}
			while(table != null && table.len() == 0 && resubscribeCount < 2);
			if(table != null && table.len() == 0 && resubscribeCount == 2)
				NTPLogger.syncAPIOverrun("SyncGetExchanges", resubscribeCount);
			query.Stop();
			NTPLogger.disconnectChannel("ExchPop");
			parseBlobTable(table);
		}
		uiExchgMap.putAll(exchangeMap);
	}

	private void parseBlobTable(BlobTable blobTable)
	{
		int rowCount = blobTable.nRow();
		for (int count = 0; count < rowCount; count++)
		{
			String instrument = blobTable.GetCell(count, INSTRUMENT_POS);
			if(! (instrument.equals("CTA_A") || instrument.equals("CTA_B") || instrument.equals("UTP")) )
				continue;
			String nazExchgCode = blobTable.GetCell(count, NASDAQ_EXCHANGE_CODE_POS).toUpperCase();
			String eqPlusExchgCode = blobTable.GetCell(count, EQPLUS_EXCHANGE_CODE_POS).toUpperCase();
			exchangeMap.put(nazExchgCode, eqPlusExchgCode);
			System.out.println(count + ") " + nazExchgCode + "      :      " + eqPlusExchgCode);
		}
	}

	public HashMap<String, String> getExchangeMap()
	{
		return exchangeMap;
	}

	public HashSet<String> getExchanges()
	{
		HashSet<String> exchanges = new HashSet<String>();
		exchanges.addAll(exchangeMap.values());
		return exchanges;
	}
	
	public void updateExchangeMap() {
		Set<String> keys = uiExchgMap.keySet();
		for(String s: keys)
		{
			String val = uiExchgMap.get(s).toUpperCase();
			switch (val) {
			case "D1":
			case "D2":
				val = "d";
				break;
			case "BB":
			case "XOTC":
				val = "v";
				break;
			case "OB":
			case "OOTC":
				val = "u";
				break;
			case "XTSE":
				val = "to";
				break;
			case "XTSX":
				val = "tv";
				break;
			case "PSGM":
				val = "gm";
				break;
			case "PINX":
				val = "pk";
				break;
			case "OTCB":
				val = "qb";
				break;
			case "OTCQ":
				val = "qx";
				break;
			default:
				break;
			}
			uiExchgMap.put(s, val);
		}
	}
	
	public String getEquityPlusExchangeCode(String nasdaqExchangeCode)
	{
		if(nasdaqExchangeCode.contains("??") || nasdaqExchangeCode.length()<1)
			return " ";
		nasdaqExchangeCode = nasdaqExchangeCode.toUpperCase();		
		if(uiExchgMap.containsKey(nasdaqExchangeCode))
			nasdaqExchangeCode =  uiExchgMap.get(nasdaqExchangeCode).toLowerCase();
		return nasdaqExchangeCode;
	}
}
