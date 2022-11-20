package ntp.logger;

import java.sql.Timestamp;
import java.util.Date;

import QuoddFeed.msg.Image;

public class NTPLogger {
	private static final int LOG_LEVEL_DEBUG = 0;
	private static final int LOG_LEVEL_INFO = 1;
	private static final int LOG_LEVEL_WARNING = 2;
	private static final int LOG_LEVEL_ERROR = 3;
	private static final String INFO = "INFO";
	private static final String WARNING = "WARNING";
	private static final String ERROR = "ERROR";
	private static final String DEBUG = "DEBUG";
	private static int logLevel = 0;

	public static int getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(int logLevel) {
		NTPLogger.logLevel = logLevel;
	}

	public static void info(String text) {
		if(LOG_LEVEL_INFO >= logLevel)
			System.out.println("[" +new Timestamp(new Date().getTime()) + ",NTP.jar," +  INFO + "] "+ text);
	}

	public static void warning(String text) {
		if(LOG_LEVEL_WARNING >= logLevel)
			System.out.println("[" +new Timestamp(new Date().getTime()) + ",NTP.jar," +  WARNING + "] "+ text);
	}

	public static void error(String text) {
		if(LOG_LEVEL_ERROR >= logLevel)
			System.out.println("[" +new Timestamp(new Date().getTime()) + ",NTP.jar," +  ERROR + "] "+ text);
	}

	public static void debug(String text) {
		if(LOG_LEVEL_DEBUG >= logLevel)
			System.out.println("[" +new Timestamp(new Date().getTime()) + ",NTP.jar," +  DEBUG + "] "+ text);
	}

	public static void missingProperty(String propertyName) {
		warning("MISSING Property - " + propertyName);
	}

	public static void defaultSetting(String prop, String value)
	{
		info("DEFAULT value set for " + prop + " to " + value);
	}

	public static void subscribe(String ticker, String streamName, int streamId)
	{
		debug("SUBSCRIBE " + ticker + " on "+ streamName + " streamId " + streamId);
	}
	public static void dropSymbol(String ticker, String reason)
	{
		info("DROP " + ticker + " - " + reason);
	}
	public static void connectChannel(String channelName, String ip)
	{
		info("CONNECT channel " +  channelName +" to UC " + ip);
	}
	public static void disconnectChannel(String channelName)
	{
		info("DISCONNECT channel to UC " + channelName);
	}
	public static void image(String channelName, Image image)
	{
		debug("IMAGE " + image.tkr() + " on " + channelName + " - " + image._trdPrc + "," + image._ask + "," + image._bid + "," + image.protocol());
	}
	public static void dead(String ticker, String channelName)
	{
		debug("DEAD " + ticker + " on " + channelName);
	}
	public static void unknown(String ticker, String channelName, char sts)
	{
		info("UNKNOWN " + ticker + " on " + channelName + " status " + sts);
	}
	public static void newIssue(String ticker, String instrument)
	{
		debug("NEWISSUE " + ticker + " " + instrument);
	}
	public static void requestUC(String api, String ticker, int resubscribeCount)
	{
		info("REQUEST UC " + api + " for " + ticker + " count " + resubscribeCount);
	}
	public static void responseUC(String api, String ticker, int resultCount)
	{
		info("RESPONSE UC " + api + " for " + ticker + " rowCount " + resultCount);
	}
	public static void requestEJB(String api)
	{
		info("EJB ENTER (" + api + ")");
	}
	public static void responseEJB(String api, long time)
	{
		info("EJB EXIT (" + api + ") - " + time);
	}
	
	public static void onConnectUC(String channelName)
	{
		info("OnCONNECT " + channelName);
	}
	
	public static void onDisconnectUC(String channelName)
	{
		info("OnDISCONNECT " + channelName);
	}
	
	public static void onSessionUC(String channelName, String txt, boolean flag)
	{
		info("OnSESSION " + channelName + " " + txt + " " + flag);
	}
	
	public static void onResubscribeUC(String tkr, int oldStreamID, int newStreamID)
	{
		info("OnRESUBSCRIBE " + tkr + " old id: " + oldStreamID + " new id: " + newStreamID);
	}
	
	public static void syncAPIOverrun(String api, int count)
	{
		warning("SYNC OVERFLOW - " + api + " " + count);
	}
	
	public static void unsubscribeSymbol(String ticker, String streamId )
	{
		debug("UNSUBSCRIBE " + ticker + " on streamID " + streamId);
	}
	
	public static void missingMetaValue(String ticker, String metaField, String metaValue)
	{
		warning("META-MISSING " + ticker + " " + metaField + " = " + metaValue);
	}
}
