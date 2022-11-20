package com.quodd.dao;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.dbManager;
import static com.quodd.cpd.AlertCpd.logger;
import static com.quodd.cpd.AlertCpd.alertCommentDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.quodd.bean.UserAlert;
import com.quodd.bean.UserAlertDetailBean;
import com.quodd.common.logger.CommonLogMessage;

/*
 * Manage Mysql database along with Oracle database 
 * 1. From Tomcat, api call will be make to insert/delete
 * 2. From alert CPD - to manage summary and historical
 * 3. From Daily Alert - to manage reocurring alerts
 * 
 * Once Mysql Database will be 100% in sync with Oracle,
 *  
 * 1. EQ+ will use apis from this get load alerts, alert summary and historical summary
 * 2. Implement CPD to fire alerts
 * 3. EQ+ will use this directly to poll fired alert
 * 4. EQ+ will use this to manage alerts
 */
public class AlertDao {

	public Collection<Map<String, Object>> getUserAlertList(long userId) {
		Map<String, Map<String, Object>> alertMap = new LinkedHashMap<>();
		String query = "Select TICKER, WEB_FLAG, ALERT_TYPE, ALERT_VALUE, alerts.ALERT_NAME, ALERT_FREQ, alert_comments.ALERT_COMMENT from alerts "
				+ " Left Join alert_comments on alerts.USER_ID = alert_comments.USER_ID and alerts.ALERT_NAME = alert_comments.ALERT_NAME where alerts.USER_ID = ? ORDER BY alerts.ALERT_NAME";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					String alertName = rs.getString("ALERT_NAME");
					Map<String, Object> detailBean = alertMap.computeIfAbsent(alertName,
							n -> new HashMap<String, Object>());
					detailBean.put("alert_name", alertName);
					detailBean.put("web_flag", rs.getString("WEB_FLAG"));
					detailBean.put("alert_frequency", rs.getInt("ALERT_FREQ"));
					detailBean.put("ticker", rs.getString("TICKER"));
					detailBean.put("comments", rs.getString("ALERT_COMMENT"));
					setAlertType(rs.getString("ALERT_TYPE"), rs.getString("ALERT_VALUE"), detailBean);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return alertMap.values();
	}

	public long deleteUserAlertByName(long userId, String alertName) {
		String query = "Delete from alerts where USER_ID = ? and ALERT_NAME = ? ";
		int count = 0;
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			stmt.setString(2, alertName);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			count = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		alertCache.deleteUserAlert(userId, alertName);
		return count;
	}

	public long deleteUserAlertByNameAndType(UserAlert userAlert) {
		String query = "Delete from alerts where USER_ID = ? and ALERT_NAME = ? and ALERT_TYPE = ?";
		int count = 0;
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userAlert.getUserID());
			stmt.setString(2, userAlert.getAlertName());
			stmt.setString(3, userAlert.getAlertType());
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			count = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		alertCommentDao.deleteSingleAlertComment(userAlert.getUserID(), userAlert.getAlertName());
		alertCache.deleteSingleUserAlert(userAlert);
		return count;
	}

	public long addUserAlert(long userId, String ticker, String webFlag, String alertType, String alertValue,
			String alertName, int alertFreq, String comment) {
		int count = 0;
		String query = "INSERT INTO alerts (USER_ID, TICKER, WEB_FLAG, ALERT_TYPE, "
				+ " ALERT_VALUE, DATE_CREATED, ALERT_NAME, ALERT_FREQ ) VALUES (?, ?, ?, ?, ?, now(), ?, ?)";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			stmt.setString(2, ticker);
			stmt.setString(3, webFlag);
			stmt.setString(4, alertType);
			stmt.setString(5, alertValue);
			stmt.setString(6, alertName);
			stmt.setInt(7, alertFreq);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			count = stmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		UserAlert userAlert = new UserAlert();
		userAlert.setEmail(alertCache.getEmailByUserId(userId));
		userAlert.setComments(comment);
		userAlert.setUserID(userId);
		userAlert.setTicker(ticker);
		userAlert.setWebFlag(webFlag);
		userAlert.setAlertType(alertType);
		userAlert.setValue(alertValue);
		userAlert.setAlertName(alertName);
		userAlert.setAlertFreq(alertFreq);
		alertCache.addAlert(userAlert);
		return count;
	}

	private void setAlertType(String alertType, String alertValue, Map<String, Object> detailBean) {
		switch (alertType) {
		case UserAlertDetailBean.LAST_OVER_ACTIVITY:
			detailBean.put("last_over_value", alertValue);
			detailBean.put("last_over_activity", true);
			break;

		case UserAlertDetailBean.LAST_UNDER_ACTIVITY:
			detailBean.put("last_under_value", alertValue);
			detailBean.put("last_under_activity", true);
			break;
		case UserAlertDetailBean.LAST_EQUAL_ACTIVITY:
			detailBean.put("last_equal_value", alertValue);
			detailBean.put("last_equal_activity", true);
			break;
		case UserAlertDetailBean.ASK_OVER_ACTIVITY:
			detailBean.put("ask_over_value", alertValue);
			detailBean.put("ask_over_activity", true);
			break;
		case UserAlertDetailBean.ASK_UNDER_ACTIVITY:
			detailBean.put("ask_under_value", alertValue);
			detailBean.put("ask_under_activity", true);
			break;
		case UserAlertDetailBean.ASK_EQUAL_ACTIVITY:
			detailBean.put("ask_equal_value", alertValue);
			detailBean.put("ask_equal_activity", true);
			break;
		case UserAlertDetailBean.BID_OVER_ACTIVITY:
			detailBean.put("bid_over_value", alertValue);
			detailBean.put("bid_over_activity", true);
			break;
		case UserAlertDetailBean.BID_UNDER_ACTIVITY:
			detailBean.put("bid_under_value", alertValue);
			detailBean.put("bid_under_activity", true);
			break;
		case UserAlertDetailBean.BID_EQUAL_ACTIVITY:
			detailBean.put("bid_equal_value", alertValue);
			detailBean.put("bid_equal_activity", true);
			break;
		case UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY:
			detailBean.put("change_up_value", alertValue);
			detailBean.put("change_up_activity", true);
			break;
		case UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY:
			detailBean.put("change_down_value", alertValue);
			detailBean.put("change_down_activity", true);
			break;
		case UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY:
			detailBean.put("volume_over_equal_value", alertValue);
			detailBean.put("volume_over_equal_activity", true);
			break;
		case UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_ACTIVITY:
			detailBean.put("trade_vol_equal_value", alertValue);
			detailBean.put("trade_vol_equal_activity", true);
			break;
		case UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_OVER_ACTIVITY:
			detailBean.put("trade_vol_equal_over_value", alertValue);
			detailBean.put("trade_vol_equal_over_activity", true);
			break;
		case UserAlertDetailBean.LAST_TRADE_VOLUME_OVER_ACTIVITY:
			detailBean.put("trade_vol_over_value", alertValue);
			detailBean.put("trade_vol_over_activity", true);
			break;
		case "TIME":
			detailBean.put("alarm_time", alertValue);
			break;
		case UserAlertDetailBean.EARNINGS_REPORTED_ACTIVITY:
			detailBean.put("earnings_reported_activity", true);
			break;
		case UserAlertDetailBean.COMPANY_NEWS_ACTIVITY:
			detailBean.put("company_news_activity", true);
			break;
		case UserAlertDetailBean.FIFTYTWOWEEK_HIGH_ACTIVITY:
			detailBean.put("fiftytwo_week_high_activity", true);
			break;
		case UserAlertDetailBean.FIFTYTWOWEEK_LOW_ACTIVITY:
			detailBean.put("fiftytwo_week_low_activity", true);
			break;
		default:
			break;
		}
	}

	public List<Map<String, Object>> getActiveAlertsByUserId(long userId) {
		List<Map<String, Object>> list = new LinkedList<>();
		String query = "Select TICKER, WEB_FLAG, ALERT_TYPE, ALERT_VALUE, alerts.ALERT_NAME, DATE_CREATED, ALERT_FREQ, alert_comments.ALERT_COMMENT from alerts "
				+ " Left Join alert_comments on alerts.USER_ID = alert_comments.USER_ID and alerts.ALERT_NAME = alert_comments.ALERT_NAME where alerts.USER_ID = ? ORDER BY alerts.ALERT_NAME";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			stmt.setLong(1, userId);
			String s = stmt.toString();
			s = s.substring(s.indexOf(':') + 1, s.length());
			logger.info(CommonLogMessage.requestQuery(s));
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					Map<String, Object> bean = new HashMap<>();
					bean.put("userId", userId);
					bean.put("ticker", rs.getString("TICKER"));
					bean.put("web_flag", rs.getString("WEB_FLAG"));
					bean.put("alert_type", rs.getString("ALERT_TYPE"));
					bean.put("alert_value", rs.getString("ALERT_VALUE"));
					bean.put("date_created", rs.getString("DATE_CREATED"));
					bean.put("alert_name", rs.getString("ALERT_NAME"));
					bean.put("alert_frequency", rs.getInt("ALERT_FREQ"));
					bean.put("comments", rs.getString("ALERT_COMMENT"));
					list.add(bean);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return list;
	}

	public void loadAllActiveUserAlerts() {
		String query = "Select TICKER, WEB_FLAG, ALERT_TYPE, ALERT_VALUE, alerts.ALERT_NAME, DATE_CREATED, ALERT_FREQ, alert_comments.ALERT_COMMENT,  alerts.USER_ID from alerts "
				+ " Left Join alert_comments on alerts.USER_ID = alert_comments.USER_ID and alerts.ALERT_NAME = alert_comments.ALERT_NAME  ORDER BY alerts.ALERT_NAME";
		try (Connection con = dbManager.getConnnection(); PreparedStatement stmt = con.prepareStatement(query);) {
			logger.info(CommonLogMessage.requestQuery(query));
			int count = 0;
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					UserAlert alert = new UserAlert();
					long userId = rs.getLong("USER_ID");
					alert.setTicker(rs.getString("TICKER"));
					alert.setAlertName(rs.getString("ALERT_NAME"));
					alert.setUserID(userId);
					alert.setEmail(alertCache.getEmailByUserId(userId));
					alert.setAlertType(rs.getString("ALERT_TYPE"));
					alert.setValue(rs.getString("ALERT_VALUE"));
					alert.setWebFlag(rs.getString("WEB_FLAG"));
					alert.setComments(rs.getString("ALERT_COMMENT"));
					alert.setAlertFreq(rs.getInt("ALERT_FREQ"));
					alertCache.addAlert(alert);
					count++;
				}
			}
			logger.info("Loaded records " + count);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
}
