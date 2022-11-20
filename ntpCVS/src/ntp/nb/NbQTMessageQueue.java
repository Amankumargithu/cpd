package ntp.nb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.queue.MappedMessageQueue;
import ntp.util.CPDProperty;

public class NbQTMessageQueue {

	private Map<String, QTCPDMessageBean> subsData = new ConcurrentHashMap<String, QTCPDMessageBean>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private static NbQTMessageQueue qtMessageQueue = new NbQTMessageQueue();
	private ConcurrentHashMap<String, String> imageTickerMap = new ConcurrentHashMap<String, String>();
	private String symbolSuffix = null;
	private int tradeProtocol = 0;
	private int quoteProtocol = 0;

	private NbQTMessageQueue() {
		symbolSuffix = CPDProperty.getInstance().getProperty("SYMBOL_SUFFIX");
		if(symbolSuffix == null) {
			NTPLogger.missingProperty("SYMBOL_SUFFIX");
			symbolSuffix = ".NB";
			NTPLogger.defaultSetting("SYMBOL_SUFFIX", symbolSuffix);
		}
		try 
		{
			tradeProtocol = Integer.parseInt(CPDProperty.getInstance().getProperty("TRADE_PROTOCOL"));
		} 
		catch (Exception e) {
			NTPLogger.missingProperty("TRADE_PROTOCOL");
			NTPLogger.defaultSetting("TRADE_PROTOCOL", "" + tradeProtocol);
			tradeProtocol = 0;
		}
		try 
		{
			quoteProtocol = Integer.parseInt(CPDProperty.getInstance().getProperty("QUOTE_PROTOCOL"));
		} 
		catch (Exception e) {
			NTPLogger.missingProperty("QUOTE_PROTOCOL");
			NTPLogger.defaultSetting("QUOTE_PROTOCOL", "" + quoteProtocol);
			quoteProtocol = 0;
		}
	}

	public static NbQTMessageQueue getInstance(){
		return qtMessageQueue;
	}
	/**
	 * @return the subsData
	 */
	public Map<String, QTCPDMessageBean> getSubsData() {
		return subsData;
	}
	/**
	 * @param subsData the subsData to set
	 */
	public void setSubsData(Map<String, QTCPDMessageBean> subsData) {
		this.subsData = subsData;
	}

	/**
	 * @return the mQueue
	 */
	public MappedMessageQueue getmQueue() {
		return mQueue;
	}

	/**
	 * @param mQueue the mQueue to set
	 */
	public void setmQueue(MappedMessageQueue mQueue) {
		this.mQueue = mQueue;
	}
	
	public ConcurrentHashMap<String, String> getImageTickerMap()
	{
		return imageTickerMap;
	}

	public String getSymbolSuffix() {
		return symbolSuffix;
	}

	public int getTradeProtocol() {
		return tradeProtocol;
	}

	public int getQuoteProtocol() {
		return quoteProtocol;
	}
}
