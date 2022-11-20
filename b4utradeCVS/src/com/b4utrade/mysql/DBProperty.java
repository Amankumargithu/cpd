package com.b4utrade.mysql;

import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author Aman
 *
 */
public class DBProperty 
{
	private static Properties prop = null;
	private static volatile DBProperty instance = new DBProperty();
	
	
	private DBProperty() 
	{
		init();
	}
	
		
	public static String getProperty(String key)
	{
		return prop.getProperty(key);
	}
	
	private void init()
	{
		prop = new Properties();
		InputStream in = getClass().getResourceAsStream ("/mysqldb.properties");
		try 
		{
			prop.load(in);
		}
		catch (Exception e) 
		{
			System.out.println("Unable to load property");
		}
	}
	
}
