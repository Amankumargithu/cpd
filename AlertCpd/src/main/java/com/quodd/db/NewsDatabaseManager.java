package com.quodd.db;

import static com.quodd.cpd.AlertCpd.dbProperties;
import static com.quodd.cpd.AlertCpd.logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;

public class NewsDatabaseManager {

	public Connection getDowjonesConnection() {
		Connection conn = null;
		try {
			String dburl = dbProperties.getProperty("djnews.url");
			String user = dbProperties.getProperty("djnews.user");
			String pwd = dbProperties.getProperty("djnews.password");
			String driver = dbProperties.getProperty("djnews.driver");
			Class.forName(driver);
			conn = DriverManager.getConnection(dburl, user, pwd);
			logger.info("Connection for  mysql: " + dburl);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return conn;
	}

	public Connection getNewsEdgeConnection() {
		Connection conn = null;
		try {
			String dburl = dbProperties.getProperty("newsedge.url");
			String user = dbProperties.getProperty("newsedge.user");
			String pwd = dbProperties.getProperty("newsedge.password");
			String driver = dbProperties.getProperty("newsedge.driver");
			Class.forName(driver);
			conn = DriverManager.getConnection(dburl, user, pwd);
			logger.info("Connection for  mysql: " + dburl);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return conn;
	}
}
