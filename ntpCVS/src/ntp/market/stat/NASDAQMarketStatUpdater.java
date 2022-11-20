package ntp.market.stat;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class NASDAQMarketStatUpdater {

	private static NASDAQMarketStatUpdater ndqUpdater ;
	private ConcurrentHashMap<String, String> tickerMap = new ConcurrentHashMap<>();
	private double advancers = 0.0;
	private double decliners = 0.0;
	private double unchanged = 0.0;
	private boolean isUpdated = false;
	private double uptick = 0.0;
	private double downtick = 0.0;
	private ArrayList<String> tickList = new ArrayList<>();
	private ConcurrentHashMap<String, QTCPDMessageBean> beanMap = new ConcurrentHashMap<>();
	
	private NASDAQMarketStatUpdater()
	{
		NASDAQMarketStatStreamer streamer  =  new NASDAQMarketStatStreamer();
		Thread t = new Thread(streamer);
		t.start();
	}

	public static NASDAQMarketStatUpdater getInstance()
	{
		if(ndqUpdater == null)
			ndqUpdater = new NASDAQMarketStatUpdater();
		return ndqUpdater;
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
	public boolean isNASDAQTicker(String ticker)
	{
		if(tickList.contains(ticker))
			return true;
		return false;
	}
	public void onUpdate(QTCPDMessageBean bean, char prcTck)
	{
//		long vol = bean.getVolume();
//		if(vol == 0)
//			return;
		String ticker = bean.getTicker();
		beanMap.put(ticker, bean);
		isUpdated = true;
		double diff =bean.getLastPrice() -  bean.getLastClosedPrice();
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
			if(prvStat.charAt(1) != tickStatus)
			{
				addTick(tickStatus);
				subtractTick(prvStat.charAt(1));
			}
		}
		else
		{
			addVolume(advancerStatus);
			addTick(tickStatus);
		}
		tickerMap.put(ticker,new StringBuilder().append(advancerStatus).append(tickStatus).toString());
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
	private void addTick(char stat)
	{
		switch (stat) {
		case 'P':
			uptick++;
			break;
		case 'O':
			downtick++;
			break;
		}
	}

	private void subtractTick(char stat)
	{
		switch (stat) {
		case 'P':
			uptick--;
			break;
		case 'O':
			downtick--;
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

	private class NASDAQMarketStatStreamer implements Runnable{

		private QTCPDMessageBean upVolBean  = new QTCPDMessageBean();
		private QTCPDMessageBean downVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean totalVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean uncgIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean advIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean declIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean udriBean = new QTCPDMessageBean();		//Up Down Ratio Indicator
		private QTCPDMessageBean sttiBean = new QTCPDMessageBean();		//Short Term Trade Index
		private Map<String, QTCPDMessageBean> qtMap = MarketStatCache.getInstance().getSubsData();
		private int streamInterval = 5000;

		public NASDAQMarketStatStreamer()
		{
			initializeBean(upVolBean, MarketStatTickerManager.SYMBOL_NASDAQ_UP_VOLUME);
			initializeBean(downVolBean, MarketStatTickerManager.SYMBOL_NASDAQ_DOWN_VOLUME);
			initializeBean(totalVolBean, MarketStatTickerManager.SYMBOL_NASDAQ_TOTAL_VOLUME);
			initializeBean(uncgIsueBean, MarketStatTickerManager.SYMBOL_NASDAQ_UNCHANGED_ISSUE);
			initializeBean(advIsueBean, MarketStatTickerManager.SYMBOL_NASDAQ_ADVANCING_ISSUE);
			initializeBean(declIsueBean, MarketStatTickerManager.SYMBOL_NASDAQ_DECLINING_ISSUE);
			initializeBean(udriBean, MarketStatTickerManager.SYMBOL_NASDAQ_ISSUES_UP_DOWN_RATIO);
			initializeBean(sttiBean, MarketStatTickerManager.SYMBOL_NASDAQ_SHORT_TERM_TRADE_INDEX);
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
						double cumTick = uptick - downtick;
						streamBean(udriBean, cumTick);
						double stti = 0.0;
						try{
							if(downVol == 0)
								stti = 0.0;
							else
							{
								double volRatio = upVol / downVol;
								if(volRatio == 0 || decliners == 0 )
									stti = 0.0;
								else
									stti =(advancers/decliners) / volRatio;
							}
						}
						catch (Exception e) {
							stti = 0.0;
						}
						streamBean(sttiBean, stti);
					}
					Thread.sleep(streamInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
