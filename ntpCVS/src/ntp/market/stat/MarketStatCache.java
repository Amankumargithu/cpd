package ntp.market.stat;

import java.util.HashMap;
import java.util.Map;

import ntp.bean.QTCPDMessageBean;
import ntp.queue.MappedMessageQueue;

public class MarketStatCache {

	private Map<String, QTCPDMessageBean> subsData = new HashMap<>();
	private Map<String, String> exchangeCodeMap = new HashMap<>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private static MarketStatCache qtMessageQueue = new MarketStatCache();

	private MarketStatCache() {
	}

	public static MarketStatCache getInstance() {
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
	 * @return the pinkSheetData
	 */
	public Map<String, String> getExchangeCodeMap() {
		return exchangeCodeMap;
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
}
