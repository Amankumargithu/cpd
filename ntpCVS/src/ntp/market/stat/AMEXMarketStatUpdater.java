package ntp.market.stat;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class AMEXMarketStatUpdater {

	private static AMEXMarketStatUpdater amexUpdater ;
	private ConcurrentHashMap<String, String> tickerMap = new ConcurrentHashMap<>();
	private double advancers = 0.0;
	private double decliners = 0.0;
	private double unchanged = 0.0;
	private boolean isUpdated = false;
	private ArrayList<String> tickList = new ArrayList<>(); 
	private ConcurrentHashMap<String, QTCPDMessageBean> beanMap = new ConcurrentHashMap<>();

	private AMEXMarketStatUpdater()
	{
		AMEXMarketStatStreamer streamer  =  new AMEXMarketStatStreamer();
		Thread t = new Thread(streamer);
		t.start();
	}
	public void addTicker(String ticker)
	{
		if(!tickList.contains(ticker))
			tickList.add(ticker);
	}
	public void addAllTickers(ArrayList<String> tickers)
	{
			tickList.addAll(tickers);
	}
	public static AMEXMarketStatUpdater getInstance()
	{
		if(amexUpdater == null)
			amexUpdater = new AMEXMarketStatUpdater();
		return amexUpdater;
	}

	public void onUpdate(QTCPDMessageBean bean, char prcTck)
	{
//		long vol = bean.getVolume();
//		if(vol == 0)
//			return;
		String ticker = bean.getTicker();
		beanMap.put(ticker, bean);
		isUpdated = true;
		double diff = bean.getLastPrice() - bean.getLastClosedPrice();
		char advancerStatus = checkAdvancer(diff);
		char tickStatus = checkTick(prcTck);
		if(tickerMap.containsKey(ticker))
		{
			String prvStat = tickerMap.get(ticker);
			if(prvStat.charAt(0) != advancerStatus)
			{
				addVolume(advancerStatus);
				subtractVolume(prvStat.charAt(0));
			}
		}
		else
		{
			addVolume(advancerStatus);
		}
		tickerMap.put(ticker,new StringBuilder().append(advancerStatus).append(tickStatus).toString());
	}

	public boolean isAMEXTicker(String ticker)
	{
		if(tickList.contains(ticker))
			return true;
		return false;
	}
	private void subtractVolume(char c)
	{
		switch(c){
		case 'A' : 
			advancers--;
			break;
		case 'D':
			decliners--;
			break;
		case 'U':
			unchanged--;
			break;
		}
	}
	private void addVolume(char c)
	{
		switch(c){
		case 'A' : 
			advancers++;
			break;
		case 'D':
			decliners++;
			break;
		case 'U':
			unchanged++;
			break;
		}
	}
	private char checkAdvancer(double diff)
	{
		if(diff > 0) return 'A';
		if(diff < 0) return 'D';
		if(diff == 0) return 'U';
		return 'U';
	}

	private char checkTick(char prcTck)
	{
		switch( prcTck ) {
		case '^': return 'P';
		case 'v': return 'O';
		case '-': return 'N';
		}
		return 'N';
	}

	private class AMEXMarketStatStreamer implements Runnable{

		private QTCPDMessageBean upVolBean  = new QTCPDMessageBean();
		private QTCPDMessageBean downVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean totalVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean uncgIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean advIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean declIsueBean = new QTCPDMessageBean();
		private Map<String, QTCPDMessageBean> qtMap = MarketStatCache.getInstance().getSubsData();
		private int streamInterval = 5000;

		public AMEXMarketStatStreamer()
		{
			initializeBean(upVolBean, MarketStatTickerManager.SYMBOL_AMEX_UP_VOLUME);
			initializeBean(downVolBean, MarketStatTickerManager.SYMBOL_AMEX_DOWN_VOLUME);
			initializeBean(totalVolBean, MarketStatTickerManager.SYMBOL_AMEX_TOTAL_VOLUME);
			initializeBean(uncgIsueBean, MarketStatTickerManager.SYMBOL_AMEX_UNCHANGED_ISSUE);
			initializeBean(advIsueBean, MarketStatTickerManager.SYMBOL_AMEX_ADVANCING_ISSUE);
			initializeBean(declIsueBean, MarketStatTickerManager.SYMBOL_AMEX_DECLINING_ISSUE);
			CPDProperty prop = CPDProperty.getInstance();
			streamInterval = Integer.parseInt(prop.getProperty("STREAMINTERVAL"));
		}

		private void initializeBean(QTCPDMessageBean bean, String ticker)
		{
			bean.setTicker(ticker);
			bean.setSystemTicker(ticker);
			qtMap.put(ticker, bean);
		}

		private void streamBean(QTCPDMessageBean bean, double last)
		{
			bean.setLastPrice(last);
			NTPLogger.info(bean.getTicker() + " : " +  bean.getLastPrice());
			MarketStatCache.getInstance().getmQueue().add(bean.getTicker(), bean);
		}
		@Override
		public void run() {

			while(true)
			{
				try {
					if(isUpdated)
					{
						isUpdated = false;
						double upVol = 0, downVol = 0, uncgVol = 0;
						for(String tick : tickerMap.keySet())
						{
							QTCPDMessageBean bBean = beanMap.get(tick);
							if(bBean != null)
							{
								double vol = bBean.getVolume();
								String val = tickerMap.get(tick);
								switch (val.charAt(0)){
								case 'A': upVol+=vol; break;
								case 'D': downVol+=vol; break;
								case 'U': uncgVol+=vol; break;
								}
							}
						}
						streamBean(upVolBean, upVol);
						streamBean(downVolBean, downVol);
						double totalVol = upVol + downVol + uncgVol;
						streamBean(totalVolBean, totalVol);
						streamBean(uncgIsueBean, unchanged);
						streamBean(advIsueBean, advancers);
						streamBean(declIsueBean, decliners);
					}
					Thread.sleep(streamInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}