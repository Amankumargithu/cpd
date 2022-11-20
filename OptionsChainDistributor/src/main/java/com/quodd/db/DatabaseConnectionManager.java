package com.quodd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.QuoddProperty;

public class DatabaseConnectionManager implements AutoCloseable {

	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private Connection connection = null;

	public DatabaseConnectionManager(QuoddProperty dbProperties) throws ClassNotFoundException, SQLException {
		String dburl = dbProperties.getProperty("mysql.url");
		String user = dbProperties.getProperty("mysql.user");
		String pwd = dbProperties.getProperty("mysql.password");
		String driver = dbProperties.getProperty("mysql.driver");
		this.logger.finest("url: " + dburl + " username: " + user + " password: " + pwd + " driver: " + driver);
		Class.forName(driver);
		this.connection = DriverManager.getConnection(dburl, user, pwd);
		this.logger.finer("Connection created successfully");
	}

	public Connection getConnection() {
		return this.connection;
	}

	@Override
	public void close() throws Exception {
		try {
			if (this.connection != null)
				this.connection.close();
		} catch (SQLException e) {
			this.logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.connection = null;
	}
}
