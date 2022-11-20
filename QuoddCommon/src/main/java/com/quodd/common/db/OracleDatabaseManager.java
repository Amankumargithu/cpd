package com.quodd.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.QuoddProperty;

public class OracleDatabaseManager implements AutoCloseable {

	private final Logger logger = QuoddLogger.getInstance().getLogger();
	private Connection connection = null;

	public OracleDatabaseManager(QuoddProperty dbProperties) throws ClassNotFoundException, SQLException {
		String dburl = dbProperties.getProperty("oracle.url");
		String user = dbProperties.getProperty("oracle.user");
		String pwd = dbProperties.getProperty("oracle.password");
		String driver = "oracle.jdbc.driver.OracleDriver";
		this.logger.info("url: " + dburl + " username: " + user + " password: " + pwd + " driver: " + driver);
		Class.forName(driver);
		this.connection = DriverManager.getConnection(dburl, user, pwd);
		this.logger.info("Connection created successfully");
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
