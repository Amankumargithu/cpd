package ntp.equityregional.snap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import QuoddFeed.msg.Image;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import ntp.equityregional.cache.EquityRegionalQTMessageQueue;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.NTPConstants;

public class EquityRegionalSnapController {

	private UltraChan[] channelArray;
	private long count = 0;
	private static volatile EquityRegionalSnapController instance = new EquityRegionalSnapController();

	private EquityRegionalSnapController()
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
			String name = "EqSnap_" + i;
			UltraChan chan = new UltraChan(ip, port, name, "password", false);
			channelArray[i] = chan;
			chan.Start();
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public static EquityRegionalSnapController getInstance()
	{
		return instance;
	}

	private synchronized UltraChan getChannel()
	{
		++count;
		int index = (int)(count % channelArray.length);
		return channelArray[index];
	}

	public LinkedList<Image> getSyncSnapData(String tickers){
		LinkedList<Image> list = new LinkedList<Image>();
		int resubscribeCount = -1;
		UltraChan channel =   getChannel();
		try{
			String[] tickerArr = tickers.split(",");
			HashSet<String> resubscribeSet = new HashSet<String>();
			for(String ticker : tickerArr)
				resubscribeSet.add(ticker);
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
								NTPLogger.dead(tkr, "EqRegSyncMulti");
								EquityRegionalQTMessageQueue.getInstance().getIncorrectTickerMap().put(tkr, tkr);
							}
						}
						else
							NTPLogger.unknown(tickerSnaps[j].tkr(), "EqRegSyncMulti", mt);
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
}
