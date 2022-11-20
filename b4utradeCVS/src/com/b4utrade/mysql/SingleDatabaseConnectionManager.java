package com.b4utrade.mysql;

import java.sql.Connection;
import java.sql.DriverManager;

public class SingleDatabaseConnectionManager implements IDBConnectionManager {

	private static Connection conn = null;
	
	private static SingleDatabaseConnectionManager instance = null;
	
	private static Object obj = new Object();
	private SingleDatabaseConnectionManager(){
		conn = createConnection();
	}
	
	private Connection createConnection(){
		try
		{
			String dburl = DBProperty.getProperty("url");
        	String user = DBProperty.getProperty("user");
        	String pwd = DBProperty.getProperty("password");
        	String driver = DBProperty.getProperty("driver");
        	
			Class.forName(driver);
			conn = DriverManager.getConnection(dburl,user, pwd);
			
			System.out.println("Connection for single mysql: " + conn);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return conn;
	}
	
	public static SingleDatabaseConnectionManager getInstance(){
		if(instance == null)
		{
			synchronized (obj) 
			{
				if(instance == null)
				{
					instance= new SingleDatabaseConnectionManager();
				}
			}
		}
		return  instance ;
	}


	public Connection getConnection() {
		
			return conn;		
	}
}
