package ntp.indicesmf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.QTCPDMessageBean;
import ntp.queue.MappedMessageQueue;
import ntp.queue.VectorMessageQueue;

public class IndicesMFQTMessageQueue {

	private Map<String, QTCPDMessageBean> subsData = new ConcurrentHashMap<String, QTCPDMessageBean>();
	private MappedMessageQueue mQueue = new MappedMessageQueue();
	private VectorMessageQueue bondsWriterQueue = new VectorMessageQueue(); 
	private static volatile IndicesMFQTMessageQueue qtMessageQueue = new IndicesMFQTMessageQueue();
	
	private IndicesMFQTMessageQueue() {}

	public static IndicesMFQTMessageQueue getInstance(){
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

	public VectorMessageQueue getBondsWriterQueue() {
		return bondsWriterQueue;
	}

	public void setBondsWriterQueue(VectorMessageQueue bondsWriterQueue) {
		this.bondsWriterQueue = bondsWriterQueue;
	}
}
