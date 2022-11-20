package com.quodd.task;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.alertDao;
import static com.quodd.cpd.AlertCpd.environmentProperties;
import static com.quodd.cpd.AlertCpd.historicalAlertDao;
import static com.quodd.cpd.AlertCpd.logger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.mail.MessagingException;

import com.quodd.bean.UserAlert;
import com.quodd.bean.UserAlertDetailBean;
import com.quodd.common.collection.MessageQueue;
import com.quodd.util.AlertConstant;
import com.quodd.util.EmailManager;

public class FiredAlertsProcessor implements Runnable {

	private static final String ALERT_MESSAGE_SEPERATOR = "##";
	private static final String AUDIBLE_ALERT_IDENTIFIER = "AUDIBLE";
	private static final String FAKE_ALERT_IDENTIFIER = "$$FAKE";
	private static final String smtpServer = environmentProperties.getStringProperty("SMTP_SERVER", "localhost");
	private static final String fromEmailAddress = environmentProperties.getStringProperty("ALERT_EMAIL_FROM",
			"QuoddAlert@quodd.com");
	private static final String fromPersonalName = environmentProperties
			.getStringProperty("ALERT_EMAIL_FROM_PERSONAL_NAME", "QUODD Stock Alert");
	private final MessageQueue queue = new MessageQueue();
	private boolean doRun = false;

	public FiredAlertsProcessor() {
		this.doRun = true;
	}

	public void addMessage(UserAlert userAlert) {
		queue.add(userAlert);
	}

	public void stopThread() {
		this.doRun = false;
	}

	@Override
	public void run() {
		while (doRun) {
			try {
				List<Object> alertList = queue.removeAllWithoutWait();
				if (alertList != null && !alertList.isEmpty()) {
					alertList.forEach(ua -> {
						UserAlert userAlert = (UserAlert) ua;
						if (userAlert.isByEmail() && userAlert.getEmail() != null) {
							sendEmail(userAlert);
						}
						fireAlert(userAlert);
						updateDatabaseOnAlert(userAlert);
					});
				} else {
					TimeUnit.MILLISECONDS.sleep(50);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	private void updateDatabaseOnAlert(UserAlert userAlert) {
		try {
			int freq = userAlert.getAlertFreq();
			if (freq != UserAlertDetailBean.ALERT_EVERYTIME) {
				alertDao.deleteUserAlertByNameAndType(userAlert);
			}
			historicalAlertDao.addHistoricalAlert(userAlert);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void fireAlert(UserAlert userAlert) {
		long userID = userAlert.getUserID();
		String ticker = userAlert.getTicker();
		String activity = userAlert.getAlertType();
		String triggeredValue = userAlert.getTriggeredValue();
		String message = alertCache.getAlertMessage(activity);
		String alertName = userAlert.getAlertName();
		String alertMessage = "";
		try {
			if (!userAlert.isByWindow()) {
				alertName = alertName + FAKE_ALERT_IDENTIFIER;
			}
			if (triggeredValue == null)
				alertMessage = "Alert! " + ticker + " " + message;
			else {
				// >1million
				if (activity.startsWith(UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY)) {
					int index = activity.indexOf(">");
					if (index != -1) {
						alertMessage = "Alert! " + ticker + " Volume " + activity.substring(index) + " (at "
								+ AlertConstant.formatVolume(triggeredValue) + ")";
					} else {
						alertMessage = "Alert! " + ticker + " Volume over " + userAlert.getValue() + " (at "
								+ AlertConstant.formatVolume(triggeredValue) + ")";
					}
				} else if (activity.equalsIgnoreCase(UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY)
						|| activity.equalsIgnoreCase(UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY)) {
					alertMessage = "Alert! " + ticker + " " + message + " " + userAlert.getValue() + "% (at "
							+ triggeredValue + "%)";
				} else if ("TIME".equalsIgnoreCase(activity)) {
					String alertTime = userAlert.getValue().replaceAll(",", " ");
					alertMessage = "Alert! Time is now: " + alertTime + " EASTERN";
				} else {
					alertMessage = "Alert! " + ticker + " " + message + " " + userAlert.getValue() + " (at "
							+ triggeredValue + ")";
				}
			}
			alertMessage += ALERT_MESSAGE_SEPERATOR + userAlert.getComments() + ALERT_MESSAGE_SEPERATOR + alertName
					+ ALERT_MESSAGE_SEPERATOR + userAlert.getWebFlag();
			alertCache.addFiredAlert(String.valueOf(userID), alertMessage);
			if (userAlert.isByAudio()) {
				alertCache.addFiredAlert(String.valueOf(userID), AUDIBLE_ALERT_IDENTIFIER);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"fireAlert: " + userAlert.toString() + " msg: " + alertMessage + " " + e.getMessage(), e);
		}
	}

	private void sendEmail(UserAlert userAlert) {
		String ticker = userAlert.getTicker();
		String activity = userAlert.getAlertType();
		String value = userAlert.getTriggeredValue();
		String message = alertCache.getAlertMessage(activity);
		String emailSubject = null;
		String emailBody = "";
		try {
			EmailManager em = new EmailManager();
			if (value != null) {
				if (activity.startsWith(UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY)) {
					if (activity.indexOf(">") != -1) {
						emailSubject = "Alert! " + ticker + " Volume " + message + " (at "
								+ AlertConstant.formatVolume(value) + ")";
					} else {
						emailSubject = "Alert! " + ticker + " Volume over " + userAlert.getValue() + " (at "
								+ AlertConstant.formatVolume(value) + ")";
					}
				} else if (activity.equalsIgnoreCase(UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY)
						|| activity.equalsIgnoreCase(UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY)) {
					emailSubject = "Alert! " + ticker + " " + message + " " + userAlert.getValue() + "% (at " + value
							+ "%)";
				} else if ("TIME".equalsIgnoreCase(activity)) {
					String tempValue = userAlert.getValue().replaceAll(",", " ");
					emailSubject = "Alert! Time is now: " + tempValue + " EASTERN";
				} else {
					emailSubject = "Alert! " + ticker + " " + message + " " + userAlert.getValue() + " (at " + value
							+ ")";
				}
			}
			if (userAlert.getComments() == null || userAlert.getComments().equalsIgnoreCase("null")) {
				emailBody = "Alert Comments: " + "\n" + emailBody;
			} else {
				emailBody = "Alert Comments: " + userAlert.getComments() + "\n" + emailBody;
			}
			em.send(userAlert.getEmail(), null, null, fromEmailAddress, fromPersonalName, smtpServer, emailSubject,
					emailBody, true, false, null);
		} catch (MessagingException me) {
			logger.log(Level.WARNING,
					userAlert.toString() + " sub: " + emailSubject + " body: " + emailBody + " " + me.getMessage(), me);
		}
	}
}
