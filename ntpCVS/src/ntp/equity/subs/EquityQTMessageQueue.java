package ntp.equity.subs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.queue.MappedMessageQueue;

public class EquityQTMessageQueue {

	private Map<String, QTCPDMessageBean> subsData = new ConcurrentHashMap<>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private static EquityQTMessageQueue qtMessageQueue = new EquityQTMessageQueue();
	private ConcurrentHashMap<String, String> imageTickerMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> duallyQuotedTickerMap = new ConcurrentHashMap<>();

	private EquityQTMessageQueue() {
	}

	public static EquityQTMessageQueue getInstance() {
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

	public ConcurrentHashMap<String, String> getImageTickerMap() {
		return imageTickerMap;
	}

	public ConcurrentHashMap<String, String> getDuallyQuotedMap() {
		return duallyQuotedTickerMap;
	}
}
