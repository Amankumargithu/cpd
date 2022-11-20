package ntp.options.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.b4utrade.bean.InterestRateBean;

import ntp.bean.QTCPDMessageBean;
import ntp.options.parser.InterestRateParser;
import ntp.options.parser.OptionsUnderlyerParser;
import ntp.options.parser.OptionsVolatilityDataParser;
import ntp.options.streamer.OptionsPreStreamingChannel;
import ntp.options.streamer.OptionsStreamingChannel;
import ntp.queue.MappedMessageQueue;
import ntp.util.OptionsUtility;

public class OptionsQTMessageQueue {

	public static final String BLANK = "";
	private static OptionsQTMessageQueue qtMessageQueue = new OptionsQTMessageQueue();

	public static OptionsQTMessageQueue getInstance() {
		return qtMessageQueue;
	}

	private ConcurrentMap<String, Double> equityPriceMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> imageTickerMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> incorrectTickerMap = new ConcurrentHashMap<>();
	private InterestRateParser interestRateParser = InterestRateParser.getInstance();
	private ConcurrentMap<String, String> markedTickersMap = new ConcurrentHashMap<>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private ConcurrentMap<String, OptionsPreStreamingChannel> preSubscribedSymbolMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, String> requestedSymbolMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, OptionsStreamingChannel> subscribedSymbolMap = new ConcurrentHashMap<>();
	private Map<String, QTCPDMessageBean> subsData = new ConcurrentHashMap<>();
	private OptionsUnderlyerParser underlyerParser = OptionsUnderlyerParser.getInstance();
	private OptionsVolatilityDataParser volatilityParser = OptionsVolatilityDataParser.getInstance();

	private OptionsQTMessageQueue() {
	}

	public void addMarkedSymbol(String symbol) {
		if (symbol != null)
			markedTickersMap.put(symbol, symbol);
	}

	public void addSubscribedSymbol(String symbol, OptionsStreamingChannel channel,
			OptionsPreStreamingChannel preChannel) {
		subscribedSymbolMap.put(symbol, channel);
		preSubscribedSymbolMap.put(symbol, preChannel);
	}

	public synchronized QTCPDMessageBean getCachedBean(String ticker) {
		QTCPDMessageBean cachedBean = subsData.get(ticker);
		if (cachedBean == null) {
			cachedBean = new QTCPDMessageBean();
			subsData.put(ticker, cachedBean);
		}
		return cachedBean;
	}

	public String getChannelName(String symbol) {
		return subscribedSymbolMap.get(symbol).getChannelName();
	}

	public double getEquityLast(String ticker) {
		return equityPriceMap.get(ticker) == null ? 0 : equityPriceMap.get(ticker);
	}

	public ConcurrentMap<String, Double> getEquityPriceMap() {
		return equityPriceMap;
	}

	public ConcurrentMap<String, String> getImageTickerMap() {
		return imageTickerMap;
	}

	public ConcurrentMap<String, String> getIncorrectTickerMap() {
		return incorrectTickerMap;
	}

	public InterestRateBean getInterestRatebean() {
		return interestRateParser.getInterestRateBean();
	}

	/**
	 * @return the mQueue
	 */
	public MappedMessageQueue getmQueue() {
		return mQueue;
	}

	public ConcurrentMap<String, String> getRequestedSymbolMap() {
		return requestedSymbolMap;
	}

	/**
	 * @return the subsData
	 */
	public Map<String, QTCPDMessageBean> getSubsData() {
		return subsData;
	}

	public String getUnderlyer(String rootSymbol) {
		return underlyerParser.getUnderlyer(rootSymbol);
	}

	public double getVolatility(String symbol) {
		return volatilityParser.getVolatility(symbol);
	}

	public boolean isIncorrectSymbol(String ticker) {
		ticker = OptionsUtility.getInstance().getUCFormattedTicker(ticker);
		return incorrectTickerMap.containsKey(ticker);
	}

	public boolean isMarkedSymbol(String symbol) {
		return markedTickersMap.containsKey(symbol);
	}

	public boolean isSubscribedSymbol(String symbol) {
		if (subscribedSymbolMap.containsKey(symbol))
			return true;
		return false;
	}

	public void removeMarkedSymbol(String symbol) {
		if (symbol != null)
			markedTickersMap.remove(symbol);
	}

	/**
	 * @param mQueue the mQueue to set
	 */
	public void setmQueue(MappedMessageQueue mQueue) {
		this.mQueue = mQueue;
	}
}
