package ntp.util;

public class NTPConstants {
	public static String IP = CPDProperty.getInstance().getProperty("IP");
	public static int PORT = CPDProperty.getInstance().getProperty("PORT") != null ? Integer.parseInt(CPDProperty.getInstance().getProperty("PORT")) : 4321;
}
