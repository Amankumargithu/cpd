package com.quodd.common.logger;

public interface CommonLogMessage {

	static String missingProperty(String propertyName) {
		return "MISSING Property - " + propertyName;
	}

	static String defaultSetting(String prop, String value) {
		return "DEFAULT value set for " + prop + " to " + value;
	}

	static String subscribe(String ticker, String streamName, int streamId) {
		return "SUBSCRIBE " + ticker + " on " + streamName + " streamId " + streamId;
	}

	static String dropSymbol(String ticker, String reason) {
		return "DROP " + ticker + " - " + reason;
	}

	static String connectChannel(String channelName, String ip) {
		return "CONNECT channel " + channelName + " to UC " + ip;
	}

	static String disconnectChannel(String channelName) {
		return "DISCONNECT channel to UC " + channelName;
	}

	static String image(String channelName, String ticker, double tradePrice, double ask, double bid, int protocol) {
		return "IMAGE " + ticker + " on " + channelName + " - " + tradePrice + "," + ask + "," + bid + "," + protocol;
	}

	static String dead(String ticker, String channelName) {
		return "DEAD " + ticker + " on " + channelName;
	}

	static String unknown(String ticker, String channelName, char sts) {
		return "UNKNOWN " + ticker + " on " + channelName + " status " + sts;
	}

	static String newIssue(String ticker, String instrument) {
		return "NEWISSUE " + ticker + " " + instrument;
	}

	static String requestUC(String api, String ticker, int resubscribeCount) {
		return "REQUEST UC " + api + " for " + ticker + " count " + resubscribeCount;
	}

	static String responseUC(String api, String ticker, int resultCount) {
		return "RESPONSE UC " + api + " for " + ticker + " rowCount " + resultCount;
	}

	static String requestEJB(String api) {
		return "EJB ENTER (" + api + ")";
	}

	static String responseEJB(String api, long time) {
		return "EJB EXIT (" + api + ") - " + time;
	}

	static String onConnectUC(String channelName) {
		return "OnCONNECT " + channelName;
	}

	static String onDisconnectUC(String channelName) {
		return "OnDISCONNECT " + channelName;
	}

	static String onSessionUC(String channelName, String txt, boolean flag) {
		return "OnSESSION " + channelName + " " + txt + " " + flag;
	}

	static String onResubscribeUC(String tkr, int oldStreamID, int newStreamID) {
		return "OnRESUBSCRIBE " + tkr + " old id: " + oldStreamID + " new id: " + newStreamID;
	}

	static String syncAPIOverrun(String api, int count) {
		return "SYNC OVERFLOW - " + api + " " + count;
	}

	static String unsubscribeSymbol(String ticker, String streamId) {
		return "UNSUBSCRIBE " + ticker + " on streamID " + streamId;
	}

	static String missingMetaValue(String ticker, String metaField, String metaValue) {
		return "META-MISSING " + ticker + " " + metaField + " = " + metaValue;
	}

	static String startThread(String threadName) {
		return "THREAD START : " + threadName;
	}

	static String stopThread(String threadName) {
		return "THREAD STOP : " + threadName;
	}

	static String requestQuery(String query) {
		return "QUERY " + query;
	}

}
