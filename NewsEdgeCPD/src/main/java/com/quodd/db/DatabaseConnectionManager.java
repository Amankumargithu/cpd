package com.quodd.db;

import static com.quodd.controller.NewsEdgeDataController.dbProperties;
import static com.quodd.controller.NewsEdgeDataController.logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;

public class DatabaseConnectionManager {
	private static Connection conn = null;

	public static Connection getConnection() {
		String dburl = dbProperties.getProperty("url");
		String user = dbProperties.getProperty("user");
		String pwd = dbProperties.getProperty("password");
		String driver = dbProperties.getProperty("driver");
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dburl, user, pwd);
			if (conn == null)
				logger.warning("Could not connect to MySQL database " + dburl);
			else
				logger.info("DATABASE connected " + dburl + " " + user + " " + pwd);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not connect to MySQL database " + dburl, e);
		}
		return conn;
	}
}