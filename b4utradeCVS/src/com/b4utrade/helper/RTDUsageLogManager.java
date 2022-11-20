package com.b4utrade.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.mysql.MySQLConnectionProvider;

public class RTDUsageLogManager {

	static Log log = LogFactory.getLog(RTDUsageLogManager.class);
	private static RTDUsageLogManager manager = new RTDUsageLogManager();
	private SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");

	private boolean insertFlag = false;
	private String updateQuery = null;
	private String insertQuery = null;

	public static RTDUsageLogManager getInstance() {
		if (manager == null)
			manager = new RTDUsageLogManager();
		return manager;
	}

	public void addQuery(String userid, String type, String vrn) {
		try {
			ConsoleTimestampLogger.println("Going to update the rtd Usage Log of User " + userid);
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();) {
				Date d = new Date();
				fetchSqlUpdateQuery(type);
				try (PreparedStatement preSmt = con.prepareStatement(updateQuery);) {
					preSmt.setString(1, new Timestamp(System.currentTimeMillis()).toString());
					preSmt.setString(2, userid);
					preSmt.setString(3, formatter.format(d));
					preSmt.setString(4, vrn);
					int i = preSmt.executeUpdate();
					if (i > 0) {
						log.info("Succesfully Updated rtd usage logs of User with userId : " + userid);
						insertFlag = false;
					} else {
						log.info("Updation failed of rtd user logs of User with userId : " + userid
								+ " going to insert then into db");
						insertFlag = true;
					}
				} catch (Exception e) {
					log.error("Error while updating the rtd usage logs of user : " + userid, e);
					insertFlag = true;
				}
				if (insertFlag) {
					ConsoleTimestampLogger.println("Going to insert the rtd usage Log of User : " + userid
							+ "  as update failed or there are no record to update.");
					fetchSqlInsertQuery(type);
					try (PreparedStatement preStat = con.prepareStatement(insertQuery);) {
						preStat.setString(1, userid);
						preStat.setString(2, formatter.format(d));
						preStat.setString(3, new Timestamp(System.currentTimeMillis()).toString());
						preStat.setString(4, vrn);
						int i = preStat.executeUpdate();
						if (i > 0) {
							log.info("Succesfully inserted rtd usage logs of User with userId : " + userid);
						} else {
							log.info("insertion failed of rtd usage logs of User with userId : " + userid);
						}
					} catch (Exception e) {
						log.error("Error while inserting the rtd usage logs .", e);
					}
				}
			} catch (Exception e) {
				log.error("RTDUsageLogManager.addQuery(): encountered an error while insertion/updation. ", e);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void fetchSqlUpdateQuery(String type) {
		switch (type) {
		case "1":
			updateQuery = "UPDATE RTD_USAGE_LOG SET LOGIN = LOGIN + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ? AND VERSION = ? ";
			break;
		case "2":
			updateQuery = "UPDATE RTD_USAGE_LOG SET LOGOUT = LOGOUT + 1, LAST_MODIFIED = ? WHERE USER_ID = ? AND ACCESS_DATE = ? AND VERSION = ? ";
			break;
		default:
			break;
		}
	}

	private void fetchSqlInsertQuery(String type) {
		switch (type) {
		case "1":
			insertQuery = "INSERT INTO RTD_USAGE_LOG(USER_ID,ACCESS_DATE,LOGIN,LOGOUT,LAST_MODIFIED,VERSION) VALUES (?, ?, 1, 0, ?, ?)";
			break;
		case "2":
			insertQuery = "INSERT INTO RTD_USAGE_LOG(USER_ID,ACCESS_DATE,LOGIN,LOGOUT,LAST_MODIFIED,VERSION) VALUES (?, ?, 0, 1, ?, ?)";
			break;
		default:
			break;
		}
	}
}
