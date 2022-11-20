package ntp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ntp.logger.NTPLogger;

public class MySQLDBManager {
	private static Connection connection = null;

	private MySQLDBManager(){}

	public static void closeConnection()
	{
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		connection = null;
	}

	public static Connection getConnection() throws Exception
	{
		if(connection == null){
			try{
				NTPLogger.info("Connection is null, so creating a new one ");
				DBProperty prop = DBProperty.getInstance();
				String dburl = prop.getProperty("url");
				String user = prop.getProperty("user");
				String pwd = prop.getProperty("password");
				String driver = prop.getProperty("driver"); 
				NTPLogger.info("DB Connectio - URL: " + dburl + " User: " + user + " pwd: " +pwd + " driver: " + driver);	
				Class.forName(driver);
				connection = DriverManager.getConnection(dburl, user, pwd);
				NTPLogger.info("connection returned: " + connection);
			}
			catch(Exception exception){
				exception.printStackTrace();
				throw exception;
			}
		}
		return connection;
	}
}
