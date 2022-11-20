package com.quodd.db;

import static com.quodd.cpd.AlertCpd.dbProperties;
import static com.quodd.cpd.AlertCpd.logger;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

	private final HikariDataSource dataSource;

	public DatabaseManager() {
		final String dburl = dbProperties.getProperty("mysql.url");
		final String user = dbProperties.getProperty("mysql.user");
		final String pwd = dbProperties.getProperty("mysql.password");
		if (dburl == null || user == null || pwd == null) {
			logger.warning("Missing property for mysql db connection. System Exit");
			System.exit(-1);
		}
		final int maxActive = dbProperties.getIntProperty("mysql.maxActive", 3);
		final int maxIdle = dbProperties.getIntProperty("mysql.maxIdle", 1);
		final HikariConfig config = new HikariConfig();
		config.setJdbcUrl(dburl);
		config.setUsername(user);
		config.setPassword(pwd);
		config.setMaximumPoolSize(maxActive);
		config.setMinimumIdle(maxIdle);
		this.dataSource = new HikariDataSource(config);
	}

	public Connection getConnnection() throws SQLException {
		return this.dataSource.getConnection();
	}
}
