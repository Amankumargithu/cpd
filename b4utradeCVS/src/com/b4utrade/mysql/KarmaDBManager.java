package com.b4utrade.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

public class KarmaDBManager {
	private static Connection connection = null;

	private KarmaDBManager(){

	}

	public static void closeConnection()
	{
		if(connection != null)
		{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public static Connection getConnection() throws Exception
	{
		if(connection == null){
			try{
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Connection is null, so creating a new one ");
				String dburl = DBProperty.getProperty("karma.url");
				String user = DBProperty.getProperty("karma.user");
				String pwd = DBProperty.getProperty("karma.password");
				String driver =  DBProperty.getProperty("karma.driver");
				System.out.println(new Timestamp(System.currentTimeMillis()) + " URL: " + dburl + " User: " + user + " pwd: " +pwd + " driver: " + driver);	
				Class.forName(driver);
				connection = DriverManager.getConnection(dburl, user, pwd);
				System.out.println(new Timestamp(System.currentTimeMillis()) + " karma db connection returned: " + connection);
			}
			catch(Exception exception){
				exception.printStackTrace();
				throw exception;
			}
		}
		return connection;
	}
}