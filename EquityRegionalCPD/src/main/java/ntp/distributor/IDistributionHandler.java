package ntp.distributor;

import ntp.bean.QTCPDMessageBean;

public interface IDistributionHandler {
	public void handleMessage(QTCPDMessageBean qtbean);
}
