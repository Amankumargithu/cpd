package com.quodd.dao;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.dbManager;
import static com.quodd.cpd.AlertCpd.logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.quodd.bean.SystemAlertBean;
import com.quodd.common.logger.CommonLogMessage;

public class SystemAlertDao {

	public long addSystemAlert(String alertText, long effectiveDate, long expiryDate) {
		int result = 0;
		String query = "INSERT INTO system_alerts (alert_text, effective_date, expiry_date) VALUES (?, ?, ?)";
		try (Connection con = dbManager.getConnnection();
				PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, alertText);
			stmt.setDate(2, new Date(effectiveDate));
			stmt.setDate(3, new Date(expiryDate));
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			result = stmt.executeUpdate();
//			try (ResultSet rs = stmt.getGeneratedKeys();) {
//				if (rs.next()) {
//					long systemAlertId = rs.getLong(1);
//					SystemAlertBean systemBean = new SystemAlertBean();
//					systemBean.setAlertText(alertText);
//					systemBean.setEffectiveDate(effectiveDate);
//					systemBean.setExpiryDate(expiryDate);
//					systemBean.setSystemAlertId(systemAlertId);
//					alertCache.addSystemAlert(systemBean);
//				}
//			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

	public long updateSystemAlert(String alertText, long effectiveDate, long expiryDate, long systemAlertId) {
		int result = 0;
		String query = "update system_alerts set alert_text = ?, effective_date = ?, expiry_date = ? where system_alert_id = ?";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setString(1, alertText);
			stmt.setDate(2, new Date(effectiveDate));
			stmt.setDate(3, new Date(expiryDate));
			stmt.setLong(4, systemAlertId);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			result = stmt.executeUpdate();
			SystemAlertBean systemBean = new SystemAlertBean();
			systemBean.setAlertText(alertText);
			systemBean.setEffectiveDate(effectiveDate);
			systemBean.setExpiryDate(expiryDate);
			systemBean.setSystemAlertId(systemAlertId);
			alertCache.addSystemAlert(systemBean, false);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

	public long deleteSystemAlert(long systemAlertId) {
		String query = "delete from system_alerts where system_alert_id = ?";
		int result = 0;
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, systemAlertId);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			result = stmt.executeUpdate();
			alertCache.clearSystemAlertById(systemAlertId);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return result;
	}

	public List<Map<String, Object>> selectActiveSystemAlert() {
		List<Map<String, Object>> resultList = new ArrayList<>();
		String query = "select system_alert_id, alert_text, effective_date, expiry_date from system_alerts where effective_date <= ? and expiry_date > ?";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setDate(1, new Date(System.currentTimeMillis()));
			stmt.setDate(2, new Date(System.currentTimeMillis()));
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					Map<String, Object> bean = new HashMap<String, Object>();
					bean.put("system_alert_id", rs.getLong("system_alert_id"));
					bean.put("alert_text", rs.getString("alert_text"));
					bean.put("effective_date", rs.getString("effective_date"));
					bean.put("expiry_date", rs.getString("expiry_date"));
					resultList.add(bean);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return resultList;
	}

	public void loadActiveSystemAlert() {
		// Load only today's alerts
		String query = "select system_alert_id, alert_text, effective_date, expiry_date from system_alerts where effective_date = ? and expiry_date > ?";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setDate(1, new Date(System.currentTimeMillis()));
			stmt.setDate(2, new Date(System.currentTimeMillis()));
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			Set<Long> systemIdSet = new HashSet<>();
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					SystemAlertBean systemBean = new SystemAlertBean();
					systemBean.setAlertText(rs.getString("alert_text"));
					systemBean.setEffectiveDate(rs.getDate("effective_date").getTime());
					systemBean.setExpiryDate(rs.getDate("expiry_date").getTime());
					systemBean.setSystemAlertId(rs.getLong("system_alert_id"));
					alertCache.addSystemAlert(systemBean, true);
					systemIdSet.add(rs.getLong("system_alert_id"));
				}
			}
			alertCache.clearObseleteSystemAlert(systemIdSet);
			logger.info("Active system alerts loaded " + systemIdSet.size());
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

}
