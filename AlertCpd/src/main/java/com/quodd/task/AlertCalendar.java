package com.quodd.task;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.doProcessStreamingAlerts;
import static com.quodd.cpd.AlertCpd.environmentProperties;
import static com.quodd.cpd.AlertCpd.firedProcessor;
import static com.quodd.cpd.AlertCpd.logger;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import com.quodd.bean.UserAlert;

public class AlertCalendar {
	private final int startHour = environmentProperties.getIntProperty("BEGIN_HOUR", 9);
	private final int startMinute = environmentProperties.getIntProperty("BEGIN_MINUTE", 30);
	private int endHour = environmentProperties.getIntProperty("END_HOUR", 16);
	private int endMinute = environmentProperties.getIntProperty("END_MINUTE", 30);
	private final boolean shouldCheckDay = environmentProperties.getBooleanProperty("SHOULD_CHECK_DAY", true);

	public AlertCalendar() {
		logger.info("AlertCalendar: StartHour: " + startHour + " StartMinute: " + startMinute + " EndHour: " + endHour
				+ " EndMinute: " + endMinute + " shouldCheckDay: " + shouldCheckDay);
	}

	public void computeProcess() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int dow = cal.get(Calendar.DAY_OF_WEEK);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		logger.info("AlertCalendar.computeProcess: DayOfWeek: " + dow + " Hour: " + hour + " minute: " + minute);
		if ((shouldCheckDay) && (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY)) {
			doProcessStreamingAlerts = false;
			return;
		}
		if (hour < this.startHour || hour > this.endHour) {
			doProcessStreamingAlerts = false;
			return;
		}
		if (hour == this.startHour) {
			if (minute < this.startMinute) {
				doProcessStreamingAlerts = false;
				return;
			}
		}
		if (hour == this.endHour) {
			if (minute > this.endMinute) {
				doProcessStreamingAlerts = false;
				return;
			}
		}
		doProcessStreamingAlerts = true;
	}

	public void processTimeAlert() {
		try {
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			String time = hour + "" + minute;
			List<UserAlert> alarmDetailVector = alertCache.getTimeAlerts(time);
			if (alarmDetailVector != null) {
				logger.info("Vector size: " + alarmDetailVector.size() + " for time " + time);
				for (UserAlert userAlert : alarmDetailVector) {
					if (userAlert != null) {
						userAlert.setTriggeredValue(time);
						firedProcessor.addMessage(userAlert);
					}
				}
			} else
				logger.info("UserAlarmAlertsManager No Alert for Time: " + time);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}
