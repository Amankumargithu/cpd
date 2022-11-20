package ntp.eod.futures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;
import ntp.util.FuturesUtility;
import ntp.util.NTPConstants;
import ntp.util.NTPUtility;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.FUTRMisc;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;

public class FuturesOpenInterestChannel extends UltraChan{

	private boolean isRunning = false;
	private String name;
	private int idx = 0;
	private ConcurrentHashMap<String, String> prop = new ConcurrentHashMap<String, String>();
	private Writer writer;
	private HashSet<String> futureSymbols = new HashSet<String>();
	private File outputFile;
//	private int count =0;
	private boolean isSaving = false;

	public FuturesOpenInterestChannel(String name) {
		super(NTPConstants.IP, NTPConstants.PORT, name, "password", false);
		this.name = name;
		connectChannel();
		String filename = CPDProperty.getInstance().getProperty("OUTPUT_DIR");
		if(filename == null)
		{
			NTPLogger.missingProperty("OUTPUT_DIR");
			filename = "/home/futures/eod";
			NTPLogger.defaultSetting("OUTPUT_DIR", filename);
		}
		String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
		String path = filename + "/OpenInterest_" + currentDate + ".csv" ;
		try {
			outputFile = new File(path);
			if(!outputFile.exists())
				outputFile.createNewFile();
		} catch (FileNotFoundException e) {
			NTPLogger.error("Could not found file " + path);
			e.printStackTrace();
		} catch (IOException e) {
			NTPLogger.error("IO exception while opening file " + path); 
			e.printStackTrace();
		}

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(isRunning)
				{
					try {
						if(isSaving)
						{
							isSaving = false;
//							outputFile.renameTo(new File(outputFile.getAbsoluteFile() +  "_" + ++count));
							writer = new FileWriter(outputFile);
							NTPLogger.info("Saving property file " + outputFile.getAbsolutePath());
							Set<String> keys = prop.keySet();
							for(String s : keys)
							{
								writer.write(s + "," + prop.get(s)+ "\n");
							}
							writer.close();
						}
						Thread.sleep(5 * 60*1000);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		},"Futures_Writer");
		t.start();
	}

	public String getChannelName()
	{
		return this.name;
	}
	public void connectChannel() {
		if (!isRunning) {
			Start();
			isRunning = true;
			NTPLogger.connectChannel(name, NTPConstants.IP);
		}
	}

	public void subscribeTickers() 
	{
		new Thread(new Runnable() {
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
				for (String ticker : futureSymbols)
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
							NTPLogger.warning("FuturesOpenInterestChannel: exception while threshold thread sleep");
						}
					}
				}
				NTPLogger.info("Completed Subsscriptions for " + name);
			}
		}, "SUBSCRIPTION_" + name).start();	
	}
	public void subscribe(String ticker) 
	{
		int streamID = Subscribe(ticker, ++idx);
		NTPUtility.updateTickerStreamIDMap(ticker, streamID, this);
		NTPLogger.subscribe(ticker, name, streamID);
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
		if(mt== UltraChan._mtDEAD)
		{
			String ticker = sts.tkr();
			if(ticker != null)
				NTPLogger.dead(ticker, name);
		}
		else
			NTPLogger.unknown(sts.tkr(), name, mt);
	}

	@Override
	public void OnImage( String StreamName, Image image )
	{

		String ticker = image.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "img.tkr() is null");
			return;
		}
		NTPLogger.image(name, image);
		NTPLogger.info("Got Open Interest Image for " + ticker + " values " + image._openVol);
		prop.put(ticker, Long.toString(image._openVol));
		isSaving = true;
	}

	@Override
	public void OnUpdate( String StreamName, FUTRMisc trade )
	{
		String ticker = trade.tkr();
		if(ticker == null)
		{
			NTPLogger.dropSymbol(StreamName, "FUTRMisc.tkr() is null");
			return;
		}
		if (!trade.IsHiLo())
		{
			NTPLogger.info("Got Open Interest Update for " + ticker + " values " + trade._openInt);
			prop.put(ticker, Long.toString(trade._openInt));
			isSaving = true;
		}
	}

	public void subscribeAllFutures()
	{
		HashSet<String> rootSymbols = new HashSet<String>();
		rootSymbols.add("6E");
		rootSymbols.add("E7");
		rootSymbols.add("GE");
		rootSymbols.add("DX");
		rootSymbols.add("ZN");
		rootSymbols.add("ZF");
		rootSymbols.add("ZT");
		rootSymbols.add("6J");
		rootSymbols.add("GC");
		rootSymbols.add("GLB");
		rootSymbols.add("QM");
		rootSymbols.add("NG");
		rootSymbols.add("HG");
		rootSymbols.add("SI");
		rootSymbols.add("CL");
		rootSymbols.add("RB");
		rootSymbols.add("ZC");
		int resubscribeCount = -1;
		BlobTable blobTable = null;
		do
		{
			NTPLogger.requestUC("SyncGetAllFutures", "", ++resubscribeCount);
			blobTable = SyncGetAllFutures( new Object());
			NTPLogger.responseUC("SyncGetAllFutures", "", blobTable == null ? 0 : blobTable.nRow());
		}
		while(blobTable != null && resubscribeCount < 2);	// John Changed API from UC90 onwards
		if(blobTable ==  null || blobTable.nRow() ==0)
		{
			do
			{
				NTPLogger.requestUC("SyncGetFuturesChain", "ULTRACACHE", ++resubscribeCount);
				blobTable = SyncGetFuturesChain("ULTRACACHE", new Object());
				NTPLogger.responseUC("SyncGetFuturesChain", "ULTRACACHE", blobTable == null ? 0 : blobTable.nRow());
			}
			while(blobTable != null && blobTable.len() == 0 && resubscribeCount < 2);
		}
		if(blobTable != null)
		{
			NTPLogger.info("Total root future symbols = " + blobTable.nRow());
			int rowCount = blobTable.nRow();
			for (int count = 0; count < rowCount; count++)
			{
				String ticker = blobTable.GetCell(count, 0);
				rootSymbols.add(ticker);
			}
			for(String ticker : rootSymbols)
			{
				BlobTable bt;
				if(ticker != null)
				{
					int c = -1;
					do
					{
						NTPLogger.requestUC("SyncGetFuturesChain", ticker, ++c);
						bt = SyncGetFuturesChain(ticker, null);
						NTPLogger.responseUC("SyncGetFuturesChain", ticker, bt == null? 0 : bt.nRow());
					}
					while(bt != null && bt.len() == 0 && c < 2);
					if(bt != null && bt.len() == 0 && c == 2)
						NTPLogger.syncAPIOverrun("SyncGetFuturesChain", c);
					if(bt != null)
					{
						int rc = bt.nRow();
						for (int cnt = 0; cnt < rc; cnt++)
						{
							String futTicker = bt.GetCell(cnt, 0);
							if(FuturesUtility.getInstance().validateFuturesSymbol(ticker, futTicker))
								futureSymbols.add(futTicker);
						}
					}
				}
			}
		}
		NTPLogger.info("Total future symbols " + futureSymbols.size());
		subscribeTickers();
		NTPLogger.info("All future symbols requested for subscription");
	}
	public static void main(String[] args) {
		FuturesOpenInterestChannel channel = new FuturesOpenInterestChannel("FUT_OI");
		channel.subscribeAllFutures();
	}
}
