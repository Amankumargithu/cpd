package ntp.util;

import java.util.HashSet;
import java.util.Set;

import ntp.logger.NTPLogger;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.util.UltraChan;

public class IndicesMFTickersPopulator 
{
	private static final int TICKER_POS = 0;

	private static volatile IndicesMFTickersPopulator instance = new IndicesMFTickersPopulator();
	private HashSet<String> indicesList = new HashSet<String>();
	private HashSet<String> mutualFundsList = new HashSet<String>();
	private HashSet<String> bondsList = new HashSet<String>();
	private HashSet<String> listOfAllTickers = new HashSet<String>();
	private boolean isIndices = false;
	private boolean isMutual = false;
	private boolean isBonds = false;

	private IndicesMFTickersPopulator() {}

	public void initialize()
	{
		populateMFTickers();
		populateIndicesTickers();
		populateBondsTickers();
	}

	public static IndicesMFTickersPopulator getInstance()
	{
		return instance;
	}

	public void populateBondsTickers()
	{
		isBonds = true;
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "GetBonds", "GetBonds", false);
		NTPLogger.connectChannel("BondsTick", NTPConstants.IP);
		query.Start();	
		int resubscribeCount = -1;
		BlobTable table = null;
		do
		{
			NTPLogger.requestUC("SyncGetBonds", "B", ++resubscribeCount);
			table = query.SyncGetBonds(new Object());
			NTPLogger.responseUC("SyncGetBonds", "B", table == null? 0 : table.nRow());
		}
		while(table != null && table.len() == 0 && resubscribeCount <3);
		if(table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetBonds", resubscribeCount);
		parseBlobTable(table, bondsList);
		query.Stop();
		NTPLogger.disconnectChannel("BondsTick");
	}

	public void populateMFTickers()
	{
		isMutual = true;
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "GetFunds", "GetFunds", false);
		NTPLogger.connectChannel("MFTick", NTPConstants.IP);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do
		{
			NTPLogger.requestUC("SyncGetFunds", "F", ++resubscribeCount);
			table = query.SyncGetFunds(new Object());
			NTPLogger.responseUC("SyncGetFunds", "F", table == null? 0 : table.nRow());
		}
		while(table != null && table.len() == 0 && resubscribeCount <3);
		if(table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetFunds", resubscribeCount);
		parseBlobTable(table, mutualFundsList);
		query.Stop();
		NTPLogger.disconnectChannel("MFTick");
	}

	public void populateIndicesTickers()
	{
		isIndices = true;
		UltraChan query = new UltraChan(NTPConstants.IP, NTPConstants.PORT, "GetIndices", "GetIndices", false);
		NTPLogger.connectChannel("IdxTick", NTPConstants.IP);
		query.Start();
		int resubscribeCount = -1;
		BlobTable table = null;
		do
		{
			NTPLogger.requestUC("SyncGetIndices", "I", ++resubscribeCount);
			table = query.SyncGetIndices(new Object());
			NTPLogger.responseUC("SyncGetIndices", "I", table == null? 0 : table.nRow());
		}
		while(table != null && table.len() == 0 && resubscribeCount <3);
		if(table != null && table.len() == 0 && resubscribeCount == 3)
			NTPLogger.syncAPIOverrun("SyncGetIndices", resubscribeCount);
		parseBlobTable(table, indicesList);
		query.Stop();
		NTPLogger.disconnectChannel("IdxTick");
	}

	private void parseBlobTable(BlobTable blobTable, Set<String> instrumentList)
	{
		int rowCount = blobTable.nRow();
		for (int count = 0; count < rowCount; count++)
		{
			String ticker = blobTable.GetCell(count, TICKER_POS);
			instrumentList.add(ticker);
		}
		listOfAllTickers.addAll(instrumentList);
	}

	public HashSet<String> getIndicesList() {
		return indicesList;
	}

	public HashSet<String> getMutualFundsList() {
		return mutualFundsList;
	}

	public HashSet<String> getAllTickers()
	{
		return listOfAllTickers;
	}
	public HashSet<String> getBondsList() {
		return bondsList;
	}

	public boolean isIndices() {
		return isIndices;
	}
	public boolean isMutual() {
		return isMutual;
	}
	public boolean isBonds() {
		return isBonds;
	}
}