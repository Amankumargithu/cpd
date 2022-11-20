package ntp.futures.snap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import ntp.futures.cache.FuturesQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class FuturesSnapController {

	private UltraChan[] channelArray;
	private static volatile FuturesSnapController instance = new FuturesSnapController();
	private long count = 0;
	private Object obj = new Object();
	
	public static FuturesSnapController getInstance()
	{
		return instance;
	}
	
	public FuturesSnapController() 
	{

		int snapChannelCount = 5;
		try {snapChannelCount = Integer.parseInt(CPDProperty.getInstance().getProperty("NUMBER_OF_SNAP_CHANNELS"));}
		catch (Exception e) {
			NTPLogger.missingProperty("NUMBER_OF_SNAP_CHANNELS");
			snapChannelCount = 5;
			NTPLogger.defaultSetting("NUMBER_OF_SNAP_CHANNELS", "" + snapChannelCount);
		}
		channelArray = new UltraChan[snapChannelCount];
		for(int i = 0; i < snapChannelCount; i++)
		{
			String name = "FutSnap_" + i;
			UltraChan chan = new UltraChan(NTPConstants.IP,NTPConstants.PORT, name, "password", false);
			channelArray[i] = chan;
			chan.Start();
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	private synchronized UltraChan getChannel()
	{
		++count;
		int index = (int)(count % channelArray.length);
		return channelArray[index];
	}
	
	public LinkedList<Image> getSyncSnapData(HashSet<String> resubscribeSet)
	{
		LinkedList<Image> list = new LinkedList<Image>();
		int resubscribeCount = -1;
		UltraChan channel =  getChannel();
		try{
			do
			{
				QuoddMsg[] tickerSnaps = null;
				String[] tkrArr = resubscribeSet.toArray(new String[0]);
				resubscribeSet.clear();
				int tickCount = tkrArr.length;
				for (int index = 0; index < tickCount; index+=1000)
				{
					int destRange = Math.min(index + 1000, tickCount);
					Object obj = new  Object();
					NTPLogger.requestUC("SyncMultiSnap",(destRange- index) + " ticker" , ++resubscribeCount);
					tickerSnaps = channel.SyncMultiSnap(Arrays.copyOfRange(tkrArr,index,destRange), obj);
					NTPLogger.responseUC("SyncMultiSnap", (destRange- index) + " ticker", tickerSnaps == null? 0 : tickerSnaps.length);
					for(int j =0;j<tickerSnaps.length;j++)
					{
						char mt = tickerSnaps[j].mt();
						if(tickerSnaps[j] instanceof Image)
						{
							Image rtn = (Image) tickerSnaps[j];
							list.add(rtn);	
						}
						else if(mt== UltraChan._mtDEAD)
						{
							String tkr = null;
							Status sts = (Status)tickerSnaps[j];
							try {
								tkr = sts.tkr().substring(0,sts.tkr().indexOf("\""));   
							} catch (Exception e) {
								tkr = sts.tkr();
							}
							if(sts.IsIOException())
								resubscribeSet.add(tkr);
							else
							{
								NTPLogger.dead(tkr, "FutSyncMulti");
								FuturesQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
							}
						}
						else
							NTPLogger.unknown(tickerSnaps[j].tkr(), "FutSyncMulti", mt);
					}
					tickerSnaps = null;
				}
			}while(resubscribeSet.size() > 0 && resubscribeCount < 2);
			if(resubscribeSet.size() > 0 && resubscribeCount == 2)
				NTPLogger.syncAPIOverrun("SyncMultiSnap", resubscribeCount);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		catch (Error ee)
		{
			ee.printStackTrace();
		}
		return list;
	}
	
	public Image getFuturesFundamentalData(String ticker)
	{
		UltraChan channel = getChannel();
		try 
		{
			boolean rerunFlag = false;
			int resubscribeCount = -1;
			do
			{
				NTPLogger.requestUC("SyncSnap", ticker, ++resubscribeCount);
				QuoddMsg tickerSnap = channel.SyncSnap(ticker, obj);
				NTPLogger.responseUC("SyncSnap", ticker, tickerSnap == null? 0 : 1);
				char mt = tickerSnap.mt();
				if(tickerSnap instanceof Image)
				{
					rerunFlag= false;
					Image img = (Image) tickerSnap;
					img.SetTkr(ticker);
					return img;
				}
				else if(mt== UltraChan._mtDEAD)
				{
					String tkr = null;
					Status sts = (Status)tickerSnap;
					try {
						tkr = sts.tkr().substring(0,sts.tkr().indexOf("\""));   
					} 
					catch (Exception e) {
						tkr = sts.tkr();
					}
					if(sts.IsIOException())
						rerunFlag = true;
					else
					{
						NTPLogger.dead(tkr, "FutSnap");
						if(tkr != null)
							FuturesQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
						rerunFlag = false;
					}
				}
				else
					NTPLogger.unknown(ticker, "FutSnap", mt);
			}while(rerunFlag && resubscribeCount < 2);
			if(rerunFlag && resubscribeCount == 2)
				NTPLogger.syncAPIOverrun("SyncSnap", resubscribeCount);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public BlobTable getFutureChain(String ticker)
	{
		UltraChan channel = getChannel();
		BlobTable bt = null;
		int resubscribeCount = -1;
		do
		{
			NTPLogger.requestUC("SyncGetFuturesChain", ticker, ++resubscribeCount);
			bt = channel.SyncGetFuturesChain(ticker, obj);
			NTPLogger.responseUC("SyncGetFuturesChain", ticker, bt == null? 0 : bt.nRow());
		}
		while(bt != null && bt.len() == 0 && resubscribeCount < 2);
		if(bt != null && bt.len() == 0 && resubscribeCount == 2)
			NTPLogger.syncAPIOverrun("SyncGetFuturesChain", resubscribeCount);
		return bt;
	}
	
	public HashSet<String> getFuturesRootSymbols()
	{
		UltraChan channel = getChannel();
		int resubscribeCount = -1;
		BlobTable blobTable = null;
		do
		{
			NTPLogger.requestUC("SyncGetAllFutures", "", ++resubscribeCount);
			blobTable = channel.SyncGetAllFutures( new Object());
			NTPLogger.responseUC("SyncGetAllFutures", "", blobTable == null ? 0 : blobTable.len());
		}
		while(blobTable != null && resubscribeCount < 2);	// John Changed API from UC90 onwards
		if(blobTable ==  null || blobTable.len() ==0)
		{
			do
			{
				NTPLogger.requestUC("SyncGetFuturesChain", "ULTRACACHE", ++resubscribeCount);
				blobTable = channel.SyncGetFuturesChain("ULTRACACHE", new Object());
				NTPLogger.responseUC("SyncGetFuturesChain", "ULTRACACHE", blobTable == null ? 0 : blobTable.len());
			}
			while(blobTable != null && blobTable.len() == 0 && resubscribeCount < 2);
		}
		HashSet<String> rootSymbols = new HashSet<>();
		if(blobTable != null)
		{
			int rowCount = blobTable.nRow();
			for (int count = 0; count < rowCount; count++)
			{
				String ticker = blobTable.GetCell(count, 0);
				if(ticker != null)
					rootSymbols.add(ticker);
			}
		}
		return rootSymbols;
	}
}
