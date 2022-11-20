package com.b4utrade.ejb;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.tsqstr.TSQSubscriptionManager;

@Stateless(mappedName="ejbCache/tsqSubscriber")
@Remote(TSQSubscriber.class)
public class TSQSubscriberBean implements TSQSubscriber
{
	public void subscribe(String id, Object[] keys) {
		// System.out.println("In subscribe of TSQSubscriberBean");
		TSQSubscriptionManager manager = TSQSubscriptionManager.getInstance();
		for (Object object : keys) {
			manager.subscribeTSQSymbol((String)object);
		}
	}
}