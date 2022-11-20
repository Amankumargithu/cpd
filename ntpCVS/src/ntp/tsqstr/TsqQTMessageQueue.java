package ntp.tsqstr;

import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.EquityQuotesBean;
import ntp.queue.VectorMessageQueue;


public class TsqQTMessageQueue {
	
	private static TsqQTMessageQueue qtMessageQueue = new TsqQTMessageQueue();
	private VectorMessageQueue mQueue = new VectorMessageQueue();
	private ConcurrentHashMap<String, EquityQuotesBean> tsqBBOcache = new ConcurrentHashMap<String, EquityQuotesBean>();
	private ConcurrentHashMap<String, Long> symbolTradeIdMap = new ConcurrentHashMap<String, Long>();

	private TsqQTMessageQueue() {}

	public static TsqQTMessageQueue getInstance(){
		return qtMessageQueue;
	}
	
	public VectorMessageQueue getmQueue() {
		return mQueue;
	}
	
	public ConcurrentHashMap<String, EquityQuotesBean> getTsqBBOCacheMap() {
		return tsqBBOcache;
	}

	public ConcurrentHashMap<String, Long> getSymbolTradeIdMap() {
		return symbolTradeIdMap;
	}

	public void setSymbolTradeIdMap(ConcurrentHashMap<String, Long> symbolTradeIdMap) {
		this.symbolTradeIdMap = symbolTradeIdMap;
	}
}
