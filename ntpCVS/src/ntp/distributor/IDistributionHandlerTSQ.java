package ntp.distributor;

import ntp.bean.TSQBean;

public interface IDistributionHandlerTSQ {
	
	public void handleMessage(TSQBean tsqBean);
}
