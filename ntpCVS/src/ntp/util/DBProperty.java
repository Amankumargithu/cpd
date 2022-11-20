package ntp.util;

import java.io.InputStream;
import java.util.Properties;

import ntp.logger.NTPLogger;

public class DBProperty 
{
	private static Properties prop = null;
	private static volatile DBProperty instance = null;

	private DBProperty() throws Exception 
	{
		init();
	}

	public static DBProperty getInstance() throws Exception
	{
		if(instance == null)
			instance = new DBProperty();
		return instance;
	}

	public String getProperty(String key)
	{
		return (instance == null)? null :  prop.getProperty(key);
	}

	private void init() throws Exception
	{
		prop = new Properties();	
		try (InputStream in = getClass().getResourceAsStream ("/db.properties");)
		{
			prop.load(in);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			NTPLogger.error("Unable to load db.properties");
			throw e;
		}
	}
}
