package com.b4utrade.helper;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.b4utrade.mysql.MySQLConnectionProvider;
import com.tacpoint.dataaccess.DefaultBusinessObject;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class SingleLoginHelper extends DefaultBusinessObject implements HttpSessionBindingListener, Serializable {
	private final String serverName;
	private String mSessionID;
	private int mUserID;
	private String source;

	// constructors
	public SingleLoginHelper() {
		serverName = Environment.get("LOCAL_SERVER_NAME");
	}

	public SingleLoginHelper(int userID) {
		this();
		mUserID = userID;
	}

	// getters & setters
	public void setUserID(int inLogin) {
		mUserID = inLogin;
	}

	public int getUserID() {
		return mUserID;
	}

	public void setSessionID(String inSessionID) {
		mSessionID = inSessionID;
	}

	public String getSessionID() {
		return mSessionID;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	// methods
	public void addLoggedInUser() {
		if (mUserID == 0) {
			Logger.log("Unable to obtain userID: " + mUserID);
			return;
		}
		try {
			ConsoleTimestampLogger
					.println("Going to save the user " + getUserID() + " with session: " + getSessionID());
			String saveQuery = "INSERT INTO USER_SESSIONS (USER_ID, SESSION_ID, SERVER_URL, SOURCE) VALUES (?, ?, ?, ?) "
					+ " ON DUPLICATE KEY UPDATE user_id = ?, session_id = ?, server_url = ?, source = ?";
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = con.prepareStatement(saveQuery);) {
				preSmt.setInt(1, mUserID);
				preSmt.setString(2, mSessionID);
				preSmt.setString(3, serverName);
				preSmt.setString(4, source);
				preSmt.setInt(5, mUserID);
				preSmt.setString(6, mSessionID);
				preSmt.setString(7, serverName);
				preSmt.setString(8, source);
				int i = preSmt.executeUpdate();
				if (i > 0)
					Logger.log("Session Id with User Id : " + mUserID + " Saved Successfully in db");
				else
					Logger.log("Session Id with User Id : " + mUserID + " not saved in db");
			} catch (Exception e) {
				Logger.log("Exception While fetching session id of User :" + mUserID + " " + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent hsbe) {
//		addLoggedInUser();
		if (mUserID == 0) {
			Logger.log("Unable to obtain userID: " + mUserID);
			return;
		}
		try {
			ConsoleTimestampLogger
					.println("valueBound Going to save the user " + getUserID() + " with session: " + getSessionID());
			String saveQuery = "INSERT INTO USER_SESSIONS (USER_ID, SESSION_ID, SERVER_URL, SOURCE) VALUES (?, ?, ?, ?) ";
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = con.prepareStatement(saveQuery);) {
				preSmt.setInt(1, mUserID);
				preSmt.setString(2, mSessionID);
				preSmt.setString(3, serverName);
				preSmt.setString(4, source);
				int i = preSmt.executeUpdate();
				if (i > 0)
					Logger.log("valueBound Session Id with User Id : " + mUserID + " Saved Successfully in db");
				else
					Logger.log("valueBound Session Id with User Id : " + mUserID + " not saved in db");
			} catch (Exception e) {
				Logger.log("valueBound Exception While fetching session id of User :" + mUserID + " " + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void deleteUser() {
		if (mUserID == 0) {
			Logger.log("Unable to obtain userID: " + mUserID);
			return;
		}
		try {
			ConsoleTimestampLogger.println("Going to delete  user with ID: " + this.getUserID());
			String deleteQuery = "DELETE FROM USER_SESSIONS where USER_ID = ?";
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = con.prepareStatement(deleteQuery);) {
				preSmt.setInt(1, mUserID);
				int i = preSmt.executeUpdate();
				if (i > 0)
					Logger.log("Session Id with User Id : " + mUserID + " Deleted Successfully");
				else
					Logger.log("Session Id with User Id : " + mUserID + " not deleted");
			} catch (Exception e) {
				Logger.log("Exception While deleting session id of User :" + mUserID + " " + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			Logger.log("SingleLoginHelper: deleteUser() - unable to delete user " + mUserID
					+ " from the USER_SESSIONS table.");
		}
	}

	public void deleteUserBySession() {
		if (mUserID == 0) {
			Logger.log("Unable to obtain userID: " + mUserID);
			return;
		}
		try {
			ConsoleTimestampLogger.println(
					"Going to delete user with ID: " + this.getUserID() + "having session ID: " + getSessionID());
			String deleteQuery = "DELETE FROM USER_SESSIONS where USER_ID = ? AND SESSION_ID = ?";
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = con.prepareStatement(deleteQuery);) {
				preSmt.setInt(1, mUserID);
				preSmt.setString(2, mSessionID);
				int i = preSmt.executeUpdate();
				if (i > 0)
					Logger.log("SessionId " + mSessionID + " UserId: " + mUserID + " Deleted Successfully");
				else
					Logger.log("SessionId " + mSessionID + " UserId: " + mUserID + " not deleted");
			} catch (Exception e) {
				Logger.log("Exception While deleting session id of User :" + mUserID + " session " + mSessionID
						+ e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			Logger.log("SingleLoginHelper: deleteUserBySession() - unable to delete user " + mUserID
					+ " from the current users table.");
		}
	}

	public boolean isCurrentUserLoggedOn() {
		if (mUserID == 0) {
			Logger.log(" .isCurrentUserLoggedOn() ,Unable to obtain userID :" + mUserID);
			return false;
		}
		try {
			String getQuery = "SELECT USER_ID, SESSION_ID, SERVER_URL, SOURCE FROM USER_SESSIONS WHERE USER_ID = ?";
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = con.prepareStatement(getQuery);) {
				preSmt.setInt(1, mUserID);
				ResultSet resultSet = preSmt.executeQuery();
				if (resultSet.next()) {
					this.mUserID = resultSet.getInt("USER_ID");
					this.mSessionID = resultSet.getString("SESSION_ID");
					String mServerURL = resultSet.getString("SERVER_URL");
					this.source = resultSet.getString("SOURCE");
					System.out.println(".isCurrentUserLoggedOn(), VALUE FETCHED : " + this.mUserID + " , "
							+ this.mSessionID + " , " + mServerURL + " , " + source);
				} else {
					return false;
				}
			} catch (Exception e) {
				Logger.log("Exception While fetching session id of User :" + mUserID + " " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			Logger.log("Exception while check user : " + mUserID + " is logged on ,Error :" + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean doesEQPlusSessionExist() {
		if (mSessionID == null) {
			Logger.log("Unable to obtain session: " + mSessionID);
			return false;
		}
		try {
			String query = "SELECT USER_ID, SESSION_ID, SERVER_URL FROM USER_SESSIONS where SOURCE in ('EQ+','RTD') AND SESSION_ID = ?";
			try (Connection con = MySQLConnectionProvider.createConnection("mysql1").getConnection();
					PreparedStatement preSmt = con.prepareStatement(query);) {
				preSmt.setString(1, mSessionID);
				ResultSet resultSet = preSmt.executeQuery();
				if (resultSet.next()) {
					this.mUserID = resultSet.getInt("USER_ID");
					this.mSessionID = resultSet.getString("SESSION_ID");
					String mServerURL = resultSet.getString("SERVER_URL");
					System.out.println(".doesEQPlusSessionExist(), VALUE FETCHED : " + this.mUserID + " , "
							+ this.mSessionID + " , " + mServerURL);
				} else {
					return false;
				}
			} catch (Exception e) {
				Logger.log("Exception While Eq+ session id , " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			Logger.log("SingleLoginHelper: doesEQPlusSessionExist() - NoDataFoundException caught ... Session "
					+ mSessionID + " is not currently logged on.");
			return false;
		}
		return true;
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent arg0) {

	}
}
