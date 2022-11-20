package ntp.util;

import java.io.InputStream;
import java.util.Properties;

import ntp.logger.NTPLogger;

public class MDProperty {
	private Properties prop = null;
	private static volatile MDProperty instance = new MDProperty();

	private MDProperty() 
	{
		init();
	}

	public static MDProperty getInstance()
	{
		return instance;
	}

	public String getProperty(String key)
	{
		return prop.getProperty(key);
	}

	public void setProperty(String key, String value)
	{
		prop.setProperty(key, value);
	}
	private void init()
	{
		prop = new Properties();
		InputStream in = getClass().getResourceAsStream ("/metadata.properties");
		try 
		{
			prop.load(in);
		}
		catch (Exception e) 
		{
			NTPLogger.error("Unable to load metadata.properties");
		}
	}
}

