package ntp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MDDBManager {
	private static Connection connection = null;

	private MDDBManager(){}

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
		if(connection == null||connection.isClosed()){
			try{
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Connection is null, so creating a new one ");
				DBProperty prop = DBProperty.getInstance();
				String dburl = prop.getProperty("md.mysql.url");
				String user = prop.getProperty("md.mysql.user");
				String pwd = prop.getProperty("md.mysql.password");
				String driver = prop.getProperty("md.mysql.driver"); 
				System.out.println(new Timestamp(System.currentTimeMillis()) + " URL: " + dburl + " User: " + user + " pwd: " +pwd + " driver: " + driver);	
				Class.forName(driver);
				connection = DriverManager.getConnection(dburl, user, pwd);
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Connection returned: " + connection);
			}
			catch(Exception exception){
				exception.printStackTrace();
				throw exception;
			}
		}
		return connection;
	}

	
	
	

}
