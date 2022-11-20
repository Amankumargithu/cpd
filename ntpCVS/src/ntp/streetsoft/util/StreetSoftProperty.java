package ntp.streetsoft.util;

import java.io.InputStream;
import java.util.Properties;

import ntp.logger.NTPLogger;

public class StreetSoftProperty 
{
	private Properties prop = null;
	private static volatile StreetSoftProperty instance = new StreetSoftProperty();

	private StreetSoftProperty() 
	{
		init();
	}

	public static StreetSoftProperty getInstance()
	{
		return instance;
	}

	public String getProperty(String key)
	{
		return prop.getProperty(key);
	}

	private void init()
	{
		prop = new Properties();
		InputStream in = getClass().getResourceAsStream ("/streetsoftware_ftp.properties");
		try 
		{
			prop.load(in);
		}
		catch (Exception e) 
		{
			NTPLogger.error("Unable to load streetsoftware_ftp.properties");
		}
	}
}