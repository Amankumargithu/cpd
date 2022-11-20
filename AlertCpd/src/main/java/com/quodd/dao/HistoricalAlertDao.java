package com.quodd.dao;

import static com.quodd.cpd.AlertCpd.dbManager;
import static com.quodd.cpd.AlertCpd.logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.quodd.bean.UserAlert;
import com.quodd.common.logger.CommonLogMessage;

public class HistoricalAlertDao {

	public static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

	public long getHistoricalAlertCountById(long userId) {
		long count = 0;
		String query = "Select count(*) from historical_alerts where USER_ID = ? ";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			try (ResultSet rs = stmt.executeQuery();) {
				if (rs.next()) {
					count = rs.getLong(1);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return count;
	}

	public long deleteHistoricalAlert(long alertId) {
		String query = "delete from historical_alerts where HISTORICAL_ALERT_ID = ?";
		int count = 0;
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, alertId);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			count = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return count;
	}

	public List<Map<String, Object>> gethistoricalAlertbyId(long userId, int startIndex, int endIndex) {
		List<Map<String, Object>> list = new LinkedList<>();
		String query = "Select USER_ID, ALERT_NAME, TICKER, ALERT_VALUE, NEWS_TODAY, ALERT_COMMENT, OCCURED_ON, ALERT_TYPE, ALERT_FREQ, HISTORICAL_ALERT_ID from "
				+ " historical_alerts where USER_ID = ? ORDER BY Historical_ALERT_ID DESC  Limit ?, ?";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			startIndex -= 1;
			if (startIndex < 0)
				startIndex = 0;
			stmt.setInt(2, startIndex);
			stmt.setInt(3, endIndex);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					Map<String, Object> bean = new HashMap<>();
					bean.put("user_id", rs.getLong("USER_ID"));
					bean.put("alert_name", rs.getString("ALERT_NAME"));
					bean.put("ticker", rs.getString("TICKER"));
					bean.put("alert_value", rs.getString("ALERT_VALUE"));
					bean.put("news_id", rs.getString("NEWS_TODAY"));
					bean.put("alert_comment", rs.getString("ALERT_COMMENT"));
					bean.put("occured_on", rs.getString("OCCURED_ON"));
					bean.put("alert_type", rs.getString("ALERT_TYPE"));
					bean.put("alert_id", rs.getLong("HISTORICAL_ALERT_ID"));
					bean.put("alert_frequency", rs.getInt("ALERT_FREQ"));
					list.add(bean);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return list;
	}

//	public List<HistoricalAlertBean> gethistoricalAlertbyIdandName(int userId, String alertName) {
//		ArrayList<HistoricalAlertBean> list = new ArrayList<>();
//		String query = "Select * from historical_alerts where USER_ID = ? and ALERT_NAME = ?";
//		try (Connection con = dbManager.getConnnection(); PreparedStatement pre = con.prepareStatement(query);) {
//			pre.setInt(1, userId);
//			pre.setString(2, alertName);
//			try (ResultSet rs = pre.executeQuery();) {
//				while (rs.next()) {
//
//					HistoricalAlertBean bean = new HistoricalAlertBean();
//					bean.setUserId(rs.getLong("USER_ID"));
//					bean.setAlertName(rs.getString("ALERT_NAME"));
//					bean.setTicker(rs.getString("TICKER"));
//					bean.setAlertValue(rs.getString("ALERT_VALUE"));
//					bean.setNewsToday(rs.getString("NEWS_TODAY"));
//					bean.setAlertComment(rs.getString("ALERT_COMMENT"));
//					bean.setOccuredOn(rs.getString("OCCURED_ON"));
//					bean.setAlertType(rs.getString("ALERT_TYPE"));
//					bean.setHistoricalAlertId(rs.getInt("HISTORICAL_ALERT_ID"));
//					bean.setAlertFrequency(rs.getDouble("ALERT_FREQ"));
//					bean.setWebFlag(rs.getString("WEB_FLAG"));
//					bean.setTrigValue(rs.getString("TRIG_VALUE"));
//					bean.setEmail(rs.getString("EMAIL"));
//					list.add(bean);
//				}
//			}
//		} catch (Exception e) {
//			logger.log(Level.WARNING, e.getMessage(), e);
//		}
//		return list;
//	}
//
//	public List<HistoricalAlertBean> getHistoricalAlertNameandNewsSource(int userId) {
//		ArrayList<HistoricalAlertBean> list = new ArrayList<>();
//		String query = "Select * from historical_alerts where USER_ID = ? and ALERT_NAME = ?";
//		try (Connection con = dbManager.getConnnection(); PreparedStatement pre = con.prepareStatement(query);) {
//			pre.setInt(1, userId);
//			try (ResultSet rs = pre.executeQuery();) {
//				while (rs.next()) {
//					HistoricalAlertBean bean = new HistoricalAlertBean();
//					bean.setUserId(rs.getLong("USER_ID"));
//					bean.setAlertName(rs.getString("ALERT_NAME"));
//					bean.setTicker(rs.getString("TICKER"));
//					bean.setAlertValue(rs.getString("ALERT_VALUE"));
//					bean.setNewsToday(rs.getString("NEWS_TODAY"));
//					bean.setAlertComment(rs.getString("ALERT_COMMENT"));
//					bean.setOccuredOn(rs.getString("OCCURED_ON"));
//					bean.setAlertType(rs.getString("ALERT_TYPE"));
//					bean.setHistoricalAlertId(rs.getInt("HISTORICAL_ALERT_ID"));
//					bean.setAlertFrequency(rs.getDouble("ALERT_FREQ"));
//					bean.setWebFlag(rs.getString("WEB_FLAG"));
//					bean.setTrigValue(rs.getString("TRIG_VALUE"));
//					bean.setEmail(rs.getString("EMAIL"));
//					list.add(bean);
//				}
//			}
//		} catch (Exception e) {
//			logger.log(Level.WARNING, e.getMessage(), e);
//		}
//		return list;
//	}
//
	public long addHistoricalAlert(UserAlert userAlert) {
		int count = 0;
		String query = "INSERT INTO historical_alerts (USER_ID, ALERT_NAME, TICKER, ALERT_VALUE, ALERT_COMMENT, OCCURED_ON, ALERT_TYPE, "
				+ " ALERT_FREQ, WEB_FLAG, TRIG_VALUE, EMAIL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userAlert.getUserID());
			stmt.setString(2, userAlert.getAlertName());
			stmt.setString(3, userAlert.getTicker());
			stmt.setString(4, userAlert.getValue());
			stmt.setString(5, userAlert.getComments());
			stmt.setString(6, sdf.format(new Date()));
			stmt.setString(7, userAlert.getAlertType());
			stmt.setInt(8, userAlert.getAlertFreq());
			stmt.setString(9, userAlert.getWebFlag());
			stmt.setString(10, userAlert.getTriggeredValue());
			stmt.setString(11, userAlert.getEmail());
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			count = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return count;
	}
}
