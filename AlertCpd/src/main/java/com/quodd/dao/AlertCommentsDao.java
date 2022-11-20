package com.quodd.dao;

import static com.quodd.cpd.AlertCpd.dbManager;
import static com.quodd.cpd.AlertCpd.logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

import com.quodd.common.logger.CommonLogMessage;

public class AlertCommentsDao {

	public long deleteAlertComment(long userId, String alertName) {
		String query = "delete from alert_comments where USER_ID = ? and ALERT_NAME = ? ";
		int result = 0;
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			stmt.setString(2, alertName);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			result = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

	public long deleteSingleAlertComment(long userId, String alertName) {
		String query = "delete from alert_comments where USER_ID = ? and ALERT_NAME = ?  and (Select count(*) from alerts WHERE USER_ID = ? AND ALERT_NAME = ?) = 0";
		int result = 0;
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			stmt.setString(2, alertName);
			stmt.setLong(3, userId);
			stmt.setString(4, alertName);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			result = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

	public long addAlertComment(long userId, String alertName, String comment) {
		int result = 0;
		String query = "INSERT INTO alert_comments (USER_ID, ALERT_NAME, ALERT_COMMENT) VALUES (?, ?, ?)";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			stmt.setString(2, alertName);
			stmt.setString(3, comment);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			result = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

}
