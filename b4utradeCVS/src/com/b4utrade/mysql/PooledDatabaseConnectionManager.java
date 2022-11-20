package com.b4utrade.mysql;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class PooledDatabaseConnectionManager implements IDBConnectionManager{

	private static InitialContext ctx = null;
	private static DataSource ds = null; 
	private static PooledDatabaseConnectionManager instance = null;
	private static Object obj = new Object();
	
	private PooledDatabaseConnectionManager()
	{
		init();
	}
	
	public static PooledDatabaseConnectionManager getInstance()
	{
		if(instance == null)
		{
			synchronized (obj) 
			{
				if(instance == null)
				{
					instance = new PooledDatabaseConnectionManager();
				}
			}
		}
		
		return instance;		
	}
	
    public  Connection getConnection()
    {
    	/*if (ctx == null)
    	{
    		init();
    	}*/
		Connection conn = null;
		try 
		{
			conn = ds.getConnection();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    	
		System.out.println("Returned MYSQL connection " + conn);
        return conn;
    }


	private static void init() {
		
		 try {
			 
			
			 ctx = new InitialContext();
			 ds = (DataSource)ctx.lookup("java:comp/env/jdbc/MySQLDB");
			 
			 
			 
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
	}

}

