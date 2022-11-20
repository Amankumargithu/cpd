package ntp.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ntp.logger.NTPLogger;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class UCSnap  extends UltraChan{

	private int idx = 0;
	private String name = "";
	private Thread subscriptionThread = null;
	private HashMap<String, Image> imageMap = new HashMap<String,Image>();
	private HashSet<String> deadSet = new HashSet<String>();
	private HashSet<String> unknownSet = new HashSet<String>();
	private HashSet<String> missingSet = new HashSet<String>();

	public UCSnap(String ip, int port, String name, Set<String> tickerSet) {
		super(ip, port, name, name, false);
		this.name = name;
		Start();
		NTPLogger.connectChannel(name, ip);
		
		missingSet.addAll(tickerSet);
		subscribeTickers(tickerSet);
		try {
			if(subscriptionThread == null)
				Thread.sleep(1000);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		try {
			subscriptionThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try{
			int sleepTime = 10*1000;
			NTPLogger.info("Starting thread to sleep for " + sleepTime);
			Thread.sleep(sleepTime);
			NTPLogger.info("Stopped sleeping thread");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		for(String ticker : tickerSet)
			NTPUtility.unsubscribeTicker(ticker);
		Stop();
		NTPLogger.info("STOP  channel " +  name +" to UC ");
	}

	private void subscribe(String ticker)
	{
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
	}
	
	private void subscribeTickers(final Collection<String> tickerList) 
	{
		subscriptionThread = new Thread(new Runnable() {
			public void run() {
				int count = 0;
				int threshold = 200;
				int sleepTime = 500;
				try 
				{
					threshold = Integer.parseInt(CPDProperty.getInstance().getProperty("THRESHOLD"));
					sleepTime = Integer.parseInt(CPDProperty.getInstance().getProperty("SLEEP_TIME"));
				} 	
				catch (Exception e) 
				{
					NTPLogger.missingProperty("THRESHOLD");
					NTPLogger.defaultSetting("THRESHOLD", "200");
					NTPLogger.missingProperty("SLEEP_TIME");
					NTPLogger.defaultSetting("SLEEP_TIME", "500");
					threshold = 200;
					sleepTime = 500;
				}
				for (String ticker : tickerList)
				{
					count ++;
					subscribe(ticker);
					if(count % threshold == 0)
					{
						try 
						{
							Thread.sleep(sleepTime);
						} 
						catch (Exception e) {
							NTPLogger.warning("UCSnap: exception while threshold thread sleep");
						}
					}
				}
				NTPLogger.info("Completed Subscriptions for " + name);
			}
		}, "SUBSCRIPTION_" + name);	
		subscriptionThread.start();
	}

	@Override
	public void OnConnect() {
		NTPLogger.onConnectUC(name);
	}

	@Override
	public void OnDisconnect() {
		NTPLogger.onDisconnectUC(name);
	}

	@Override
	public void OnSession(String txt, boolean bUP) {
		NTPLogger.onSessionUC(name, txt, bUP);
	}

	@Override
	public void OnStatus( String StreamName, Status sts )
	{
		char mt = sts.mt();
		String ticker = sts.tkr();
		missingSet.remove(ticker);
		if(mt== UltraChan._mtDEAD)
		{
			if(ticker != null)
			{
				NTPLogger.dead(ticker, name);
				deadSet.add(ticker);
			}
		}
		else
		{
			NTPLogger.unknown(sts.tkr(), name, mt);
			unknownSet.add(ticker);
		}
	}

	/**
	 * Called when an Initial Image is received for a ticker.
	 */
	public void OnImage( String StreamName, Image img )
	{
		if(img.tkr() == null||img.tkr().length()==0 )
		{
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		NTPLogger.image(name, img);
		missingSet.remove(img.tkr());
		imageMap.put(img.tkr(), (Image)img.clone());
	}

	public HashMap<String, Image> getImageMap() {
		return imageMap;
	}

	public HashSet<String> getDeadSet() {
		return deadSet;
	}

	public HashSet<String> getUnknownSet() {
		return unknownSet;
	}

	public HashSet<String> getMissingSet() {
		return missingSet;
	}
}
