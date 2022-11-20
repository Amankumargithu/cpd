package ntp.market.stat;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class NYSERegionalMarketStatUpdater {
	private static NYSERegionalMarketStatUpdater nyseUpdater;
	private ConcurrentHashMap<String, String> tickerMap = new ConcurrentHashMap<>();
	private ArrayList<String> tickList = new ArrayList<>();
	private double advancers = 0.0;
	private double decliners = 0.0;
	private double unchanged = 0.0;
	private boolean isUpdated = false;
	private double uptick = 0.0;
	private double downtick = 0.0;
	private ConcurrentHashMap<String, QTCPDMessageBean> beanMap = new ConcurrentHashMap<>();

	private NYSERegionalMarketStatUpdater() {
		NYSERegionalMarketStatStreamer streamer = new NYSERegionalMarketStatStreamer();
		Thread t = new Thread(streamer);
		t.start();
	}

	public static NYSERegionalMarketStatUpdater getInstance() {
		if (nyseUpdater == null)
			nyseUpdater = new NYSERegionalMarketStatUpdater();
		return nyseUpdater;
	}

	public void addTicker(String ticker) {
		if (!this.tickList.contains(ticker))
			this.tickList.add(ticker);
	}

	public void addAllTickers(ArrayList<String> tickers) {
		this.tickList.addAll(tickers);
	}

	public boolean isNYSERegionalTicker(String ticker) {
		if (this.tickList.contains(ticker))
			return true;
		return false;
	}

	public void onUpdate(QTCPDMessageBean bean, char prcTck) {
		String ticker = bean.getTicker();
		this.beanMap.put(ticker, bean);
		this.isUpdated = true;
		double diff = bean.getLastPrice() - bean.getLastClosedPrice();
		char advancerStatus = checkAdvancer(diff);
		char tickStatus = checkTick(prcTck);
		if (this.tickerMap.containsKey(ticker)) {
			String prvStat = this.tickerMap.get(ticker);
			if (prvStat.charAt(0) != advancerStatus) {
				addVolume(advancerStatus);
				subtractVolume(prvStat.charAt(0));
			}
			if (prvStat.charAt(1) != tickStatus) {
				addTick(tickStatus);
				subtractTick(prvStat.charAt(1));
			}
		} else {
			addVolume(advancerStatus);
			addTick(tickStatus);
		}
		this.tickerMap.put(ticker, new StringBuilder().append(advancerStatus).append(tickStatus).toString());
	}

	private void subtractVolume(char c) {
		switch (c) {
		case 'A':
			this.advancers--;
			break;
		case 'D':
			this.decliners--;
			break;
		case 'U':
			this.unchanged--;
			break;
		}
	}

	private void addVolume(char c) {
		switch (c) {
		case 'A':
			this.advancers++;
			break;
		case 'D':
			this.decliners++;
			break;
		case 'U':
			this.unchanged++;
			break;
		}
	}

	private void addTick(char stat) {
		switch (stat) {
		case 'P':
			this.uptick++;
			break;
		case 'O':
			this.downtick++;
			break;
		}
	}

	private void subtractTick(char stat) {
		switch (stat) {
		case 'P':
			this.uptick--;
			break;
		case 'O':
			this.downtick--;
			break;
		}
	}

	private char checkAdvancer(double diff) {
		if (diff > 0)
			return 'A';
		if (diff < 0)
			return 'D';
		if (diff == 0)
			return 'U';
		return 'U';
	}

	private char checkTick(char prcTck) {
		switch (prcTck) {
		case '^':
			return 'P';
		case 'v':
			return 'O';
		case '-':
			return 'N';
		}
		return 'N';
	}

	private class NYSERegionalMarketStatStreamer implements Runnable {

		private QTCPDMessageBean upVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean downVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean unchVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean totalVolBean = new QTCPDMessageBean();
		private QTCPDMessageBean uncgIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean advIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean declIsueBean = new QTCPDMessageBean();
		private QTCPDMessageBean udriBean = new QTCPDMessageBean();
		private Map<String, QTCPDMessageBean> qtMap = MarketStatCache.getInstance().getSubsData();
		private int streamInterval = 5000;

		public NYSERegionalMarketStatStreamer() {
			initializeBean(this.upVolBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_UP_VOLUME);
			initializeBean(this.downVolBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_DOWN_VOLUME);
			initializeBean(this.unchVolBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_UNCHANGED_VOLUME);
			initializeBean(this.totalVolBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_TOTAL_VOLUME);
			initializeBean(this.uncgIsueBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_UNCHANGED_ISSUE);
			initializeBean(this.advIsueBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_ADVANCING_ISSUE);
			initializeBean(this.declIsueBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_DECLINING_ISSUE);
			initializeBean(this.udriBean, MarketStatTickerManager.SYMBOL_NYSE_REGIONAL_ISSUES_UP_DOWN_RATIO);
			this.streamInterval = Integer.parseInt(CPDProperty.getInstance().getProperty("STREAMINTERVAL"));
		}

		private void initializeBean(QTCPDMessageBean bean, String ticker) {
			bean.setTicker(ticker);
			bean.setSystemTicker(ticker);
			this.qtMap.put(ticker, bean);
		}

		private void streamBean(QTCPDMessageBean bean, double last) {
			bean.setLastPrice(last);
			NTPLogger.info(bean.getTicker() + " : " + bean.getLastPrice());
			MarketStatCache.getInstance().getmQueue().add(bean.getTicker(), bean);
		}

		@Override
		public void run() {

			while (true) {
				try {
					if (NYSERegionalMarketStatUpdater.this.isUpdated) {
						NYSERegionalMarketStatUpdater.this.isUpdated = false;
						double upVol = 0, downVol = 0, uncgVol = 0;
						for (String tick : NYSERegionalMarketStatUpdater.this.tickerMap.keySet()) {
							QTCPDMessageBean bBean = NYSERegionalMarketStatUpdater.this.beanMap.get(tick);
							if (bBean != null) {
								double vol = bBean.getVolume();
								String val = NYSERegionalMarketStatUpdater.this.tickerMap.get(tick);
								switch (val.charAt(0)) {
								case 'A':
									upVol += vol;
									break;
								case 'D':
									downVol += vol;
									break;
								case 'U':
									uncgVol += vol;
									break;
								}
							}
						}
						streamBean(this.upVolBean, upVol);
						streamBean(this.downVolBean, downVol);
						streamBean(this.unchVolBean, uncgVol);
						double totalVol = upVol + downVol + uncgVol;
						streamBean(this.totalVolBean, totalVol);
						streamBean(this.uncgIsueBean, NYSERegionalMarketStatUpdater.this.unchanged);
						streamBean(this.advIsueBean, NYSERegionalMarketStatUpdater.this.advancers);
						streamBean(this.declIsueBean, NYSERegionalMarketStatUpdater.this.decliners);
						double cumTick = NYSERegionalMarketStatUpdater.this.uptick - NYSERegionalMarketStatUpdater.this.downtick;
//						System.out.println("NYSEREG " + uptick + " " + downtick);
						streamBean(this.udriBean, cumTick);
					}
					Thread.sleep(this.streamInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
