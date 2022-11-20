package com.quodd.cpd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.CommonLogMessage;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.db.DatabaseManager;

public class AlertsOncePerDay {

	public static final Logger logger = QuoddLogger.getInstance("alertOnceDay").getLogger();
	public static final DatabaseManager dbManager = new DatabaseManager();
	private final Map<String, Integer> keyHistoryMap = new HashMap<>();
	private final Map<String, Map<String, Object>> alertDetailMap = new HashMap<>();

	public AlertsOncePerDay() {

	}

	public static void main(String[] args) {
		logger.info("Process started");
		AlertsOncePerDay oncePerDay = new AlertsOncePerDay();
		oncePerDay.startProcessing();
		oncePerDay.updateDb();
		logger.info("Process completed");
	}

	private void updateDb() {
		Set<String> keySet = alertDetailMap.keySet();
		try (Connection con = dbManager.getConnnection();) {
			for (String key : keySet) {
				Map<String, Object> valueMap = alertDetailMap.get(key);
				String[] values = key.split("_");
				String query = "SELECT * from alerts where ALERT_NAME = '" + values[0] + "' " + "and USER_ID = "
						+ values[1] + " and ALERT_TYPE ='" + values[2] + "'";
				try (PreparedStatement stmt = con.prepareStatement(query); ResultSet rs = stmt.executeQuery();) {
					if (rs.next()) {
						logger.info(
								"Alert exists in active alerts: " + values[0] + ", " + values[1] + ", " + values[2]);
					} else {
						query = "INSERT INTO alerts (USER_ID, TICKER, WEB_FLAG, ALERT_TYPE, "
								+ " ALERT_VALUE, DATE_CREATED, ALERT_NAME, ALERT_FREQ ) VALUES (?, ?, ?, ?, ?, now(), ?, ?)";
						try (PreparedStatement stmt1 = con.prepareStatement(query);) {
							stmt1.setLong(1, (int) valueMap.get("userId"));
							stmt1.setString(2, (String) valueMap.get("ticker"));
							stmt1.setString(3, (String) valueMap.get("webFlag"));
							stmt1.setString(4, (String) valueMap.get("alertType"));
							stmt1.setString(5, (String) valueMap.get("alertValue"));
							stmt1.setString(6, (String) valueMap.get("alertName"));
							stmt1.setInt(7, (int) valueMap.get("alertFreq"));
							String s = stmt1.toString();
							s = s.substring(s.indexOf(':') + 1, s.length());
							logger.info(CommonLogMessage.requestQuery(s));
							stmt1.executeUpdate();
						}
					}
				}
				query = "SELECT * from alert_comments where ALERT_NAME = '" + values[0] + "' " + "and USER_ID = "
						+ values[1];
				try (PreparedStatement stmt = con.prepareStatement(query); ResultSet rs = stmt.executeQuery();) {
					if (rs.next()) {
						logger.info("Alert comment exists in active alerts: " + values[0] + ", " + values[1]);
					} else {
						query = "Insert into alert_comments values (" + valueMap.get("userId") + ",'"
								+ valueMap.get("alertName") + "','" + valueMap.get("comment") + "')";
						try (PreparedStatement stmt1 = con.prepareStatement(query);) {
							logger.info(query);
							stmt1.executeUpdate();
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void startProcessing() {
		// select records from db where alert_freq =1 (it corresponds to
		// alert_once_per_day
		String query = "select USER_ID, TICKER, ALERT_TYPE, ALERT_VALUE, ALERT_NAME, "
				+ " ALERT_FREQ, WEB_FLAG, ALERT_COMMENT, HISTORICAL_ALERT_ID from historical_alerts where alert_freq = 1";
		try (Connection con = dbManager.getConnnection();
				PreparedStatement stmt = con.prepareStatement(query);
				ResultSet rs = stmt.executeQuery();) {
			logger.info(query);
			while (rs.next()) {
				int userId = rs.getInt(1);
				String ticker = rs.getString(2);
				String alertType = rs.getString(3);
				String alertValue = rs.getString(4);
				String alertName = rs.getString(5);
				int alertFreq = rs.getInt(6);
				String webFlag = rs.getString(7);
				String alertComment = rs.getString(8);
				int historicalAlertId = rs.getInt(9);
				String key = alertName + "_" + userId + "_" + alertType;
				Integer oldHistoryKey = keyHistoryMap.get(key);
				if (oldHistoryKey != null) {
					if (oldHistoryKey < historicalAlertId) {
						keyHistoryMap.put(key, historicalAlertId);
						Map<String, Object> valuesMap = new HashMap<>();
						valuesMap.put("ticker", ticker);
						valuesMap.put("alertType", alertType);
						valuesMap.put("alertValue", alertValue);
						valuesMap.put("alertName", alertName);
						valuesMap.put("userId", userId);
						valuesMap.put("alertFreq", alertFreq);
						valuesMap.put("webFlag", webFlag);
						valuesMap.put("comment", alertComment);
						alertDetailMap.put(key, valuesMap);
					}
				} else {
					keyHistoryMap.put(key, historicalAlertId);
					Map<String, Object> valuesMap = new HashMap<>();
					valuesMap.put("ticker", ticker);
					valuesMap.put("alertType", alertType);
					valuesMap.put("alertValue", alertValue);
					valuesMap.put("alertName", alertName);
					valuesMap.put("userId", userId);
					valuesMap.put("alertFreq", alertFreq);
					valuesMap.put("webFlag", webFlag);
					valuesMap.put("comment", alertComment);
					alertDetailMap.put(key, valuesMap);
					// before inserting chec is alert exists with same name then it will remain
					// there
				}
			}
			logger.info("records to be inserted are : " + alertDetailMap.size());
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}