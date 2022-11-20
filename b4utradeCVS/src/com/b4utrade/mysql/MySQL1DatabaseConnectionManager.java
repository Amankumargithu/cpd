package com.b4utrade.mysql;

import java.sql.Connection;
import java.sql.Timestamp;

import org.apache.commons.dbcp.BasicDataSource;

public class MySQL1DatabaseConnectionManager implements IDBConnectionManager {

	private static BasicDataSource dataSource = null;
	private static MySQL1DatabaseConnectionManager instance = null;
	private static Object obj = new Object();

	private MySQL1DatabaseConnectionManager() {
		createConnection();
	}

	private void createConnection() {
		try {
			dataSource = new BasicDataSource();
			String dburl = DBProperty.getProperty("mysql1.url");
			String user = DBProperty.getProperty("mysql1.user");
			String pwd = DBProperty.getProperty("mysql1.password");
			String driver = DBProperty.getProperty("mysql1.driver");
			String maxActive = DBProperty.getProperty("mysql1.maxActive");
			String maxIdle = DBProperty.getProperty("mysql1.maxIdle");
			dataSource.setDriverClassName(driver);
			dataSource.setUsername(user);
			dataSource.setPassword(pwd);
			dataSource.setUrl(dburl);
			if (maxActive != null) {
				int active = 10;
				try {
					active = Integer.parseInt(maxActive);
				} catch (Exception e) {
					active = 10;
				}
				dataSource.setMaxActive(active);
			} else
				dataSource.setMaxActive(10);
			if (maxIdle != null) {
				int active = 5;
				try {
					active = Integer.parseInt(maxIdle);
				} catch (Exception e) {
					active = 5;
				}
				dataSource.setMaxIdle(active);
			} else
				dataSource.setMaxIdle(5);
			System.out.println(
					new Timestamp(System.currentTimeMillis()) + " MYSQL1 connection - MYSQL1 Database - connected");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MySQL1DatabaseConnectionManager getInstance() {
		try {
			synchronized (obj) {
				if (instance == null) {
					instance = new MySQL1DatabaseConnectionManager();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	public Connection getConnection() {

		try {
			return dataSource.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			createConnection();
			try {
				return dataSource.getConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}
