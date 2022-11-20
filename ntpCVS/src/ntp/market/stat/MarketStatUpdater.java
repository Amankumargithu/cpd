package ntp.market.stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class MarketStatUpdater {	

	private static MarketStatUpdater updater;
	private ConcurrentHashMap<String, Character> tickerMap = new ConcurrentHashMap<>();
	private ArrayList<String> djiaList = MarketStatTickerManager.getDJIATickers();
	private boolean isUpdated = false;
	private HashSet<String> eligibleTradedTickers = new HashSet<>();

	private MarketStatUpdater(){
		DJIAStreamer streamer = new DJIAStreamer();
		Thread t = new Thread(streamer, "DJIAThread");
		t.start();
	}

	public static MarketStatUpdater getInstance()
	{
		if(updater == null)
			updater = new MarketStatUpdater();
		return updater;
	}

	public void addTickertoTradedList(String ticker)
	{
		eligibleTradedTickers.add(ticker);
	}

	public boolean isTradedTicker(String ticker)
	{
		if(eligibleTradedTickers.contains(ticker))
			return true;
		return false;
	}

	public void writeTradedTicker(String ticker)
	{
		try
		{
			String tradedListFilePath = "/home/mkt_stat/traded_list.txt";
			File tradedFile = new File(tradedListFilePath);
			if(!tradedFile.exists())
				tradedFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tradedFile, true));
			writer.write(ticker);
			writer.newLine();
			writer.flush();
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onUpdate(QTCPDMessageBean bean, char prcTck)
	{
		String ticker = bean.getTicker();
		if(djiaList.contains(ticker))
		{
			isUpdated = true;
			tickerMap.put(ticker, prcTck);
		}
		if(NYSEMarketStatUpdater.getInstance().isNYSETicker(ticker))
			NYSEMarketStatUpdater.getInstance().onUpdate(bean, prcTck);
		else if(NASDAQMarketStatUpdater.getInstance().isNASDAQTicker(ticker))
			NASDAQMarketStatUpdater.getInstance().onUpdate(bean, prcTck);
		else if(AMEXMarketStatUpdater.getInstance().isAMEXTicker(ticker))
			AMEXMarketStatUpdater.getInstance().onUpdate(bean, prcTck);
		else if(NYSERegionalMarketStatUpdater.getInstance().isNYSERegionalTicker(ticker))
			NYSERegionalMarketStatUpdater.getInstance().onUpdate(bean, prcTck);
	}

	private class DJIAStreamer implements Runnable
	{
		private QTCPDMessageBean djiaBean = new QTCPDMessageBean();		//Dow Jones IOndustrial Average
		private Map<String, QTCPDMessageBean> qtMap = MarketStatCache.getInstance().getSubsData();
		private int streamInterval = 5000;

		public DJIAStreamer()
		{
			initializeBean(djiaBean, MarketStatTickerManager.SYMBOL_NYSE_DOW_JONES_INDUSTRIAL_AVERAGE);
			CPDProperty prop = CPDProperty.getInstance();
			streamInterval = Integer.parseInt(prop.getProperty("STREAMINTERVAL"));
		}

		private void initializeBean(QTCPDMessageBean bean, String ticker)
		{
			bean.setTicker(ticker);
			bean.setSystemTicker(ticker);
			qtMap.put(ticker, bean);
			streamBean(djiaBean, 0);
		}

		private void streamBean(QTCPDMessageBean bean, double last)
		{
			bean.setLastPrice(last);
			NTPLogger.info(bean.getTicker() + " : " +  bean.getLastPrice());
			MarketStatCache.getInstance().getmQueue().add(bean.getTicker(), bean);
		}

		@Override
		public void run() {
			NTPLogger.info("Started DJIA thread");
			while(true)
			{
				try {
					if(isUpdated)
					{
						isUpdated = false;
						int upTick = 0;
						int downTick = 0;
						Set<String> ticks = tickerMap.keySet();
						for(String tick : ticks)
						{
							char val = tickerMap.get(tick);
							switch (val)
							{
							case '^': upTick++; break;
							case 'v': downTick++; break;
							}
						}
						double djia = upTick-downTick;
						streamBean(djiaBean, djia);
					}	
					Thread.sleep(streamInterval);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
