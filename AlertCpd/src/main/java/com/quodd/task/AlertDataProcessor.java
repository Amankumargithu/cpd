package com.quodd.task;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.firedProcessor;
import static com.quodd.cpd.AlertCpd.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.quodd.bean.AlertActivityWrapper;
import com.quodd.bean.UserAlert;
import com.quodd.bean.UserAlertDetailBean;
import com.quodd.common.collection.MessageQueue;

public class AlertDataProcessor implements Runnable {
	private final MessageQueue queue = new MessageQueue();
	private final String id;
	private boolean doRun = false;
	private long msgCount = 0;

	public AlertDataProcessor(String id) {
		this.id = id;
		this.doRun = true;
	}

	@Override
	public void run() {
		while (doRun) {
			try {
				List<Object> messages = queue.removeAllWithoutWait();
				if (messages != null && !messages.isEmpty()) {
					for (Object message : messages) {
						msgCount++;
						AlertActivityWrapper aaw = (AlertActivityWrapper) message;
						processAlert(aaw);
					}
				} else {
					TimeUnit.MILLISECONDS.sleep(100);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	public void printMessageCount() {
		logger.info("AlertDataProcessor[" + id + "] processed message count : " + msgCount);
		msgCount = 0;
	}

	public void stopThread() {
		doRun = false;
	}

	private void processAlert(AlertActivityWrapper aaw) {
		String ticker = aaw.getTicker().toUpperCase();
		String alertType = aaw.getActivity();
		String value = aaw.getValue();
		List<UserAlert> removedAlertList = new ArrayList<>();
		List<UserAlert> userAlertList = alertCache.getUserAlertListByActivity(ticker + alertType);
		if (userAlertList == null || userAlertList.isEmpty())
			return;
		if (alertType.indexOf(">") > -1) {
			logger.info("userAlertList : " + userAlertList + " key :" + ticker + alertType);
		}
		for (UserAlert userAlert : userAlertList) {
			boolean doFireAlert = false;
			try {
				switch (alertType) {
				case UserAlertDetailBean.COMPANY_NEWS_ACTIVITY: {
					long uid = userAlert.getUserID();
					String entitlement = alertCache.getNewsEntitlementByUserId(uid);
					if (entitlement.contains(value)) {
						userAlert.setTriggeredValue(value);
						doFireAlert = true;
					}
				}
					break;
				case UserAlertDetailBean.EARNINGS_REPORTED_ACTIVITY:
				case UserAlertDetailBean.FIFTYTWOWEEK_LOW_ACTIVITY:
				case UserAlertDetailBean.FIFTYTWOWEEK_HIGH_ACTIVITY: {
					doFireAlert = true;
				}
					break;
				case UserAlertDetailBean.LAST_OVER_ACTIVITY:
				case UserAlertDetailBean.BID_OVER_ACTIVITY:
				case UserAlertDetailBean.ASK_OVER_ACTIVITY:
				case UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY:
				case UserAlertDetailBean.LAST_TRADE_VOLUME_OVER_ACTIVITY: {
					double stockValue = Double.parseDouble(value);
					double userValue = Double.parseDouble(userAlert.getValue());
					if (stockValue > userValue) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
				}
					break;
				case UserAlertDetailBean.LAST_UNDER_ACTIVITY:
				case UserAlertDetailBean.BID_UNDER_ACTIVITY:
				case UserAlertDetailBean.ASK_UNDER_ACTIVITY:
				case UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_ACTIVITY: {
					double stockValue = Double.parseDouble(value);
					double userValue = Double.parseDouble(userAlert.getValue());
					if (stockValue < userValue) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
				}
					break;
				case UserAlertDetailBean.LAST_EQUAL_ACTIVITY:
				case UserAlertDetailBean.BID_EQUAL_ACTIVITY:
				case UserAlertDetailBean.ASK_EQUAL_ACTIVITY: {
					double stockValue = Double.parseDouble(value);
					double userValue = Double.parseDouble(userAlert.getValue());
					if (stockValue == userValue) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
				}
					break;
				case UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY: {
					double stockValue = Double.parseDouble(value);
					double userValue = Double.parseDouble(userAlert.getValue());
					userValue *= -1;
					if (stockValue < userValue) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
				}
					break;
				case UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY: {
					double stockValue = Double.parseDouble(value);
					double userValue = Double.parseDouble(userAlert.getValue());
					if (stockValue > userValue) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
				}
					break;
				case UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_OVER_ACTIVITY: {
					double stockValue = Double.parseDouble(value);
					double userValue = Double.parseDouble(userAlert.getValue());
					if (stockValue >= userValue) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
				}
					break;
				default:
					if (alertType.indexOf(">") != -1) {
						doFireAlert = true;
						userAlert.setTriggeredValue(value);
					}
					break;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "AlertDataProcessor[" + id + "].processAlert(): Ticker: " + aaw.toString()
						+ " - " + userAlert.toString() + " - " + e.getMessage(), e);
			}
			if (doFireAlert) {
				firedProcessor.addMessage(userAlert);
				if (userAlert.getAlertFreq() != UserAlertDetailBean.ALERT_EVERYTIME)
					removedAlertList.add(userAlert);
			}
		}
		userAlertList.removeAll(removedAlertList);
	}

	public void addMessage(AlertActivityWrapper aaw) {
		queue.add(aaw);
	}

}
