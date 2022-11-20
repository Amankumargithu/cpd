package com.quodd.cache;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.alertDataProcessorArray;
import static com.quodd.cpd.AlertCpd.environmentProperties;
import static com.quodd.cpd.AlertCpd.gson;
import static com.quodd.cpd.AlertCpd.logger;
import static com.quodd.cpd.AlertCpd.newsDatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.google.gson.reflect.TypeToken;
import com.quodd.bean.AlertActivityWrapper;
import com.quodd.bean.AlertFundamentalBean;
import com.quodd.bean.SystemAlertBean;
import com.quodd.bean.UserAlert;
import com.quodd.bean.UserAlertDetailBean;
import com.quodd.task.AlertDataProcessor;

public class AlertCache {

	private final ConcurrentMap<Long, String> userEmailMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Long, String> userEntitlemntMap = new ConcurrentHashMap<>();
	private final Set<String> tickerSet = new HashSet<>();
	private final ConcurrentMap<String, List<UserAlert>> userAlertMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, List<UserAlert>> timeAlarmMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, List<String>> firedAlertsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AlertFundamentalBean> fundamentalDataMap = new ConcurrentHashMap<>();
	private final Map<String, String> alertTypeMessageMap = new HashMap<>();
	private final Set<String> fundamentalTypeSet = new HashSet<>();
	private final ConcurrentMap<Long, SystemAlertBean> systemAlertMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Set<Long>> userSystemMap = new ConcurrentHashMap<>();
	private final Set<String> earningTickers = new HashSet<>();
	private final Set<String> dowjonesTickers = new HashSet<>();
	private final Map<String, String> newsedgeTickerMap = new ConcurrentHashMap<>();

	public AlertCache() {
		alertTypeMessageMap.put(UserAlertDetailBean.LAST_OVER_ACTIVITY, "last over");
		alertTypeMessageMap.put(UserAlertDetailBean.LAST_UNDER_ACTIVITY, "last under");
		alertTypeMessageMap.put(UserAlertDetailBean.BID_OVER_ACTIVITY, "bid over");
		alertTypeMessageMap.put(UserAlertDetailBean.BID_UNDER_ACTIVITY, "bid under");
		alertTypeMessageMap.put(UserAlertDetailBean.ASK_OVER_ACTIVITY, "ask over");
		alertTypeMessageMap.put(UserAlertDetailBean.ASK_UNDER_ACTIVITY, "ask under");
		alertTypeMessageMap.put(UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY, "% change up");
		alertTypeMessageMap.put(UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY, "% change down");
		alertTypeMessageMap.put(UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY, "Volume");
		alertTypeMessageMap.put(UserAlertDetailBean.LAST_TRADE_VOLUME_OVER_ACTIVITY, "Last Trade Volume over");
		alertTypeMessageMap.put(UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_ACTIVITY, "Last Trade Volume equal");
		alertTypeMessageMap.put(UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_OVER_ACTIVITY,
				"Last Trade Volume equal over");
		alertTypeMessageMap.put(UserAlertDetailBean.FIFTYTWOWEEK_HIGH_ACTIVITY, "New 52 Week High");
		alertTypeMessageMap.put(UserAlertDetailBean.FIFTYTWOWEEK_LOW_ACTIVITY, "New 52 Week Low");
		alertTypeMessageMap.put(UserAlertDetailBean.LAST_EQUAL_ACTIVITY, "last equal");
		alertTypeMessageMap.put(UserAlertDetailBean.BID_EQUAL_ACTIVITY, "bid equal");
		alertTypeMessageMap.put(UserAlertDetailBean.ASK_EQUAL_ACTIVITY, "ask equal");
		alertTypeMessageMap.put(UserAlertDetailBean.EARNINGS_REPORTED_ACTIVITY, "Earnings are being Reported Today");
		alertTypeMessageMap.put(UserAlertDetailBean.COMPANY_NEWS_ACTIVITY, "in News Today");
		alertTypeMessageMap.put("TIME", "TIME");
		fundamentalTypeSet.add(UserAlertDetailBean.EARNINGS_REPORTED_ACTIVITY);
		fundamentalTypeSet.add(UserAlertDetailBean.COMPANY_NEWS_ACTIVITY);
	}

	public void addEarningTicker(String ticker) {
		earningTickers.add(ticker);
	}

	public void addFundamentalBean(AlertFundamentalBean bean) {
		fundamentalDataMap.put(bean.getTicker(), bean);
	}

	public String getEmailByUserId(long userId) {
		return userEmailMap.getOrDefault(userId, "");
	}

	public String getNewsEntitlementByUserId(long userId) {
		return userEntitlemntMap.getOrDefault(userId, "");
	}

	public void clearObseleteSystemAlert(Set<Long> validIds) {
		systemAlertMap.keySet().retainAll(validIds);
	}

	public void clearSystemAlertById(long id) {
		systemAlertMap.remove(id);
	}

	public void addSystemAlert(SystemAlertBean bean, boolean isExpiryChecked) {
		if (isExpiryChecked) {
			systemAlertMap.put(bean.getSystemAlertId(), bean);
		} else {
			if (systemAlertMap.containsKey(bean.getSystemAlertId()))
				systemAlertMap.put(bean.getSystemAlertId(), bean);
		}
	}

	public List<String> getUserSystemAlerts(String userId) {
		List<String> userAlerts = new ArrayList<>();
		Set<Long> activeIds = new HashSet<>();
		activeIds.addAll(systemAlertMap.keySet());
		Set<Long> userProcessedAlerts = userSystemMap.computeIfAbsent(userId, uid -> new HashSet<Long>());
		activeIds.removeAll(userProcessedAlerts);
		activeIds.forEach(id -> userAlerts.add(systemAlertMap.get(id).getAlertText()));
		userProcessedAlerts.addAll(activeIds);
		return userAlerts;
	}

	public void loadUserEmailMap() {
		String userEmailUrl = environmentProperties.getStringProperty("USER_EMAIL_MAP_URL",
				"www6.quodd.com/b4utrade/app/QuoddActiveUsers.do");
		URL url;
		try {
			url = new URL(userEmailUrl);
			URLConnection urlConnection = url.openConnection();
			InputStream inputStream = urlConnection.getInputStream();
			int bytesRead = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			List<Map<String, Object>> bean = gson.fromJson(baos.toString(), new TypeToken<List<Map<String, Object>>>() {
			}.getType());
			if (bean.isEmpty())
				logger.log(Level.INFO, "User Email Map returned from DB is empty.");
			bean.forEach(temp -> {
				if (temp.containsKey("mUserID") && temp.containsKey("mEmailAddress"))
					userEmailMap.put(Long.parseLong((String) temp.get("mUserID")), (String) temp.get("mEmailAddress"));
				userEntitlemntMap.put(Long.parseLong((String) temp.get("mUserID")),
						(String) temp.get("news_entitlements"));
			});
			logger.log(Level.INFO, "Number of record fetched from DB :" + bean.size()
					+ "Total Number of User Email Set  :" + userEmailMap.size());
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void addAlert(UserAlert userAlert) {
		String alertType = userAlert.getAlertType();
		if ("TIME".equalsIgnoreCase(alertType)) {
			String formattedTime = formatTime(userAlert.getValue());
			timeAlarmMap.computeIfAbsent(formattedTime, t -> new ArrayList<>()).add(userAlert);
		} else {
			String ticker = userAlert.getTicker().toUpperCase();
			String value = userAlert.getValue();
			String key = ticker + alertType;
			// VOL >1 Million
			if (UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY.equals(alertType) && value.indexOf('>') > -1) {
				key = ticker + alertType + value;
			}
			tickerSet.add(ticker);
			userAlertMap.computeIfAbsent(key, k -> new ArrayList<>()).add(userAlert);
		}
	}

	public void deleteUserAlert(long userId, String alertName) {
		userAlertMap.keySet().forEach(k -> checkAndRemoveAlertEntry(userAlertMap.get(k), alertName, userId));
		timeAlarmMap.keySet().forEach(k -> checkAndRemoveAlertEntry(timeAlarmMap.get(k), alertName, userId));
	}

	public void deleteSingleUserAlert(UserAlert userAlert) {
		long userId = userAlert.getUserID();
		String ticker = userAlert.getTicker();
		String alertType = userAlert.getAlertType();
		String alertName = userAlert.getAlertName();
		String value = userAlert.getValue();
		if ("TIME".equalsIgnoreCase(alertType)) {
			String key = formatTime(value);
			checkAndRemoveAlertEntry(timeAlarmMap.get(key), alertName, userId);
		} else {
			String key = ticker + alertType;
			// VOL >1 Million
			if (UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY.equals(alertType) && value.indexOf('>') > -1) {
				key = ticker + alertType + value;
			}
			checkAndRemoveAlertEntry(userAlertMap.get(key), alertName, userId);
		}
	}

	public List<UserAlert> getTimeAlerts(String key) {
		return timeAlarmMap.remove(key);
	}

	public String getAlertMessage(String alertType) {
		return alertTypeMessageMap.get(alertType);
	}

	public List<UserAlert> getUserAlertListByActivity(String key) {
		return userAlertMap.get(key);
	}

	public void addFiredAlert(String userId, String alertMessage) {
		logger.info("fired alert : " + userId + ": " + alertMessage);
		firedAlertsMap.computeIfAbsent(userId, uid -> new ArrayList<>()).add(alertMessage);
	}

	public List<String> getFiredUserAlerts(String userId) {
		return firedAlertsMap.remove(userId);
	}

	public boolean containsTicker(String ticker) {
		return tickerSet.contains(ticker);
	}

	public AlertFundamentalBean getFundamentalBeanByTicker(String ticker) {
		return fundamentalDataMap.get(ticker);
	}

	private void checkAndRemoveAlertEntry(List<UserAlert> userAlertVector, String alertName, long userId) {
		if (userAlertVector == null || userAlertVector.isEmpty())
			return;
		for (int i = 0; i < userAlertVector.size(); i++) {
			UserAlert userAlert = userAlertVector.get(i);
			String userAlertName = userAlert.getAlertName();
			long alertUserId = userAlert.getUserID();
			if (userAlertName.equalsIgnoreCase(alertName) && (userId == alertUserId)) {
				userAlertVector.remove(userAlert);
				break;
			}
		}
	}

	public int findHelperIndex(String ticker) {
		if (ticker != null) {
			int c1 = ticker.charAt(0);
			if (c1 <= 66)
				return 0;
			else if (c1 <= 68)
				return 1;
			else if (c1 <= 70)
				return 2;
			else if (c1 <= 72)
				return 3;
			else if (c1 <= 74)
				return 4;
			else if (c1 <= 76)
				return 5;
			else if (c1 <= 78)
				return 6;
			else if (c1 <= 80)
				return 7;
			else if (c1 <= 82)
				return 8;
			else if (c1 <= 84)
				return 9;
			else if (c1 <= 86)
				return 10;
			return 11;
		}
		return -1;
	}

	private String formatTime(String aValue) {
		String[] array = aValue.split(",");
		String time = array[0];
		try {
			String ampm = array[1];
			int idx = time.indexOf(':');
			String hrStr = time.substring(0, idx);
			if (hrStr != null && !hrStr.isEmpty()) {
				int hr = 0;
				try {
					hr = Integer.parseInt(hrStr);
				} catch (Exception e) {
				}
				if (ampm.equals("PM")) {
					if (hr != 12 && hr != 0) {
						hr += 12;
						time = String.valueOf(hr) + time.substring(idx);
					}
				} else if (ampm.equals("AM")) {
					if (hr == 12) {
						time = "00" + time.substring(idx);
					}
				}
				int hr1 = Integer.parseInt(time.substring(0, 2));
				int min = Integer.parseInt(time.substring(3, 5));
				time = hr1 + "" + min;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Problem in formatting time " + aValue, e);
		}
		return time;
	}

	public void loadDowJonesCache() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		String dateStr = sdf.format(new Date());
		String query = "SELECT distinct(tickers) FROM djnews.DJ_NEWS_INFO where NEWS_DATE like '" + dateStr
				+ "%' and tickers is not null";
		try (Connection conn = newsDatabaseManager.getDowjonesConnection();
				PreparedStatement stmt = conn.prepareStatement(query);) {
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					String tickers = rs.getString(1);
					String[] tickerArray = tickers.split(",");
					for (String tick : tickerArray) {
						if (!tick.isEmpty()) {
							dowjonesTickers.add(tick);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info("Total ticker " + dowjonesTickers.size());
		dowjonesTickers.forEach(ticker -> alert(ticker, UserAlertDetailBean.COMPANY_NEWS_ACTIVITY, "DOWJONES"));
	}

	public void loadNewsEdgeCache() {
		String query = "SELECT DISTINCT TICKERS, CASE WHEN source = 'Benzinga' THEN 'BEN' WHEN source = 'Midnight Trader' THEN 'MID' WHEN source = 'StreetInsider' THEN 'STR' "
				+ " WHEN source = 'TheFlyOnTheWall' THEN 'FLY' ELSE 'EDGE' END as SOURCE FROM NEWSEDGE_INFO WHERE STR_TO_DATE(NEWS_DATE, '%d-%b-%Y') = CURDATE() AND "
				+ " TICKERS IS NOT NULL AND TICKERS != '' GROUP BY TICKERS, SOURCE ";
		try (Connection conn = newsDatabaseManager.getNewsEdgeConnection();
				PreparedStatement stmt = conn.prepareStatement(query);) {
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					String tickers = rs.getString(1);
					String[] tickerArray = tickers.split(",");
					for (String tick : tickerArray) {
						if (!tick.isEmpty()) {
							newsedgeTickerMap.put(tick, rs.getString(2));
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info("Total ticker " + newsedgeTickerMap.size());
		newsedgeTickerMap.keySet().forEach(
				ticker -> alert(ticker, UserAlertDetailBean.COMPANY_NEWS_ACTIVITY, newsedgeTickerMap.get(ticker)));
	}

	public void processEarningsAlert() {
		earningTickers.forEach(ticker -> alert(ticker, UserAlertDetailBean.EARNINGS_REPORTED_ACTIVITY, null));
	}

	private void alert(String ticker, String activity, String value) {
		int index = alertCache.findHelperIndex(ticker);
		if (index >= 0 && alertDataProcessorArray.length > index) {
			AlertDataProcessor alertProcessor = alertDataProcessorArray[index];
			if (alertProcessor != null)
				alertProcessor.addMessage(new AlertActivityWrapper(ticker, activity, value));
			else
				logger.log(Level.WARNING, "AlertDataProcessor is null " + index + "," + ticker);
		} else {
			logger.log(Level.WARNING, "Cannot find AlertDataProcessor " + index + "," + ticker);
		}
	}

}
