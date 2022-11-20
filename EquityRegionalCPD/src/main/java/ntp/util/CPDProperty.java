package ntp.util;

import java.io.InputStream;
import java.util.Properties;

import ntp.logger.NTPLogger;

public class CPDProperty 
{
	private Properties prop = null;
	private static volatile CPDProperty instance = new CPDProperty();

	private CPDProperty() 
	{
		init();
	}

	public static CPDProperty getInstance()
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
		InputStream in = getClass().getResourceAsStream ("/cpd.properties");
		try 
		{
			prop.load(in);
		}
		catch (Exception e) 
		{
			NTPLogger.error("Unable to load cpd.properties");
		}
	}
}
