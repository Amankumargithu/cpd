package ntp.futureOptions.snap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import ntp.futureOptions.cache.FutureOptionsQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class FutureOptionsSnapController {

	private UltraChan[] channelArray;
	private static volatile FutureOptionsSnapController instance = new FutureOptionsSnapController();
	private long count = 0;
	private Object obj = new Object();

	public static FutureOptionsSnapController getInstance()
	{
		return instance;
	}

	private FutureOptionsSnapController()
	{
		int snapChannelCount = 5;
		try {snapChannelCount = Integer.parseInt(CPDProperty.getInstance().getProperty("NUMBER_OF_SNAP_CHANNELS"));}
		catch (Exception e) {
			NTPLogger.missingProperty("NUMBER_OF_SNAP_CHANNELS");
			snapChannelCount = 5;
			NTPLogger.defaultSetting("NUMBER_OF_SNAP_CHANNELS", "" + snapChannelCount);
		}
		String ip = CPDProperty.getInstance().getProperty("SNAP_UC_IP");
		if(ip == null)
		{
			NTPLogger.missingProperty("SNAP_UC_IP");
			ip = NTPConstants.IP;
			NTPLogger.defaultSetting("SNAP_UC_IP", ip);
		}
		int port = NTPConstants.PORT;
		try {port = Integer.parseInt(CPDProperty.getInstance().getProperty("SNAP_UC_PORT"));}
		catch (Exception e) {
			NTPLogger.missingProperty("SNAP_UC_PORT");
			port = NTPConstants.PORT;
			NTPLogger.defaultSetting("SNAP_UC_PORT", "" + port);
		}
		channelArray = new UltraChan[snapChannelCount];
		for(int i = 0; i < snapChannelCount; i++)
		{
			String name = "FutOpSnap" + i;
			UltraChan chan = new UltraChan(ip, port, name, "password", false);
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

	public BlobTable getOptionChain(String tkr){
		if(tkr.startsWith("/"))
			tkr = tkr.substring(1);
		UltraChan channel = getChannel();
		BlobTable bt = null;
		int resubscribeCount = -1;
		do
		{
			NTPLogger.requestUC("SyncGetFuturesOptionChain", tkr, ++resubscribeCount);
			bt = channel.SyncGetFuturesOptionChain(tkr, obj);
			NTPLogger.responseUC("SyncGetFuturesOptionChain", tkr, bt == null? 0 : bt.nRow());
		}
		while(bt != null && bt.len() == 0 && resubscribeCount < 2);
		if(bt != null && bt.len() == 0 && resubscribeCount == 2)
			NTPLogger.syncAPIOverrun("SyncGetFuturesOptionChain", resubscribeCount);
		return bt;
	}

	public Image getOptionsFundamentalData(String ticker)
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
						NTPLogger.dead(tkr, "FutOpSnap");
						if(tkr != null)
							FutureOptionsQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
						rerunFlag = false;
					}
				}
				else
					NTPLogger.unknown(ticker, "FutOpSnap", mt);
			}while(rerunFlag && resubscribeCount < 2);
			if(rerunFlag && resubscribeCount == 2)
				NTPLogger.syncAPIOverrun("SyncSnap", resubscribeCount);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public HashMap<String, Image> getSyncSnapData(HashSet<String> resubscribeSet){
		HashMap<String, Image> map = new HashMap<String, Image>();
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
							String ticker = rtn.tkr();
							int quoteIndex = ticker.indexOf("\"");
							if(quoteIndex !=-1)
								ticker=ticker.substring(0,quoteIndex);
							map.put(ticker, rtn);	
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
								NTPLogger.dead(tkr, "FutOpSyncMulti");
								FutureOptionsQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
							}
						}
						else
							NTPLogger.unknown(tickerSnaps[j].tkr(), "FutOpSyncMulti", mt);
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
		return map;
	}

}
