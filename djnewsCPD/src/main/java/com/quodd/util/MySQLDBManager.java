package com.quodd.util;

import static com.quodd.controller.DJController.dbProperties;
import static com.quodd.controller.DJController.logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySQLDBManager {
	private static Connection connection = null;

	private MySQLDBManager() {
	}

	public static void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		connection = null;
	}

	public static Connection getConnection() throws Exception {
		if (connection == null) {
			try {
				logger.info("Connection is null, so creating a new one ");
				String dburl = dbProperties.getProperty("url");
				String user = dbProperties.getProperty("user");
				String pwd = dbProperties.getProperty("password");
				String driver = dbProperties.getProperty("driver");
				logger.info("DB Connectio - URL: " + dburl + " User: " + user + " pwd: " + pwd + " driver: " + driver);
				Class.forName(driver);
				connection = DriverManager.getConnection(dburl, user, pwd);
				logger.info("connection returned: " + connection);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				throw e;
			}
		}
		return connection;
	}
}
