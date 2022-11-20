package com.quodd.common.util;

public class Constants {
	public static final String GROUP_ID_DOWNLOAD = "DOWNLOAD";
	public static final String GROUP_ID_PARSING = "PARSING";
	public static final String GROUP_ID_HISTORY = "HISTORY";
	public static final String GROUP_ID_SYNC = "SYNC";
	public static final String GROUP_ID_EOD = "EOD";
	public static final String PROCESS_NAME = "process_name";
	public static final String COMMENT = "comment";
	public static final String PROCESS_ID = "process_id";
	public static final String GROUP_ID = "group_id";
	public static final String SERVER = "server";
	public static final String RECORD_COUNT = "record_count";
	public static final String PROCESS_STATE = "process_state";
	public static final String FAILURE_REASON = "failure_reason";
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAILURE = "FAILURE";
	public static final String STATUS_RUNNING = "RUNNING";
	public static final int PROCESS_STATUS_RUNNING = 0;
	public static final int PROCESS_STATUS_SUCCESS = 1;
	public static final int PROCESS_STATUS_FAILURE = 2;
	public static final String REQUEST_TYPE_JSON = "application/json";
	public static final String RESPONSE_TYPE_HTML = "text/html";
	public static final String RESPONSE_TYPE_CSS = "text/css";
	public static final String UTF8 = "utf-8";
	public static final int HTTP_RESPONSE_BAD_REQUEST = 400; // parameter not good
	public static final int HTTP_RESPONSE_FORBIDDEN = 403; // not authorized
	public static final int HTTP_RESPONSE_PAGE_NOT_FOUND = 404; // page not found
	public static final int HTTP_RESPONSE_INTERNAL_ERROR = 500; // server error, try again

	public static final String REQUEST_BODY_ERROR = "Body is empty";
	public static final String KEY_STATUS = "status";
	public static final String URL_CHARACTER_ENCODING = "UTF-8";

	private static final String KEY_ERROR = "error";

	public static final String ERROR_ADD_DATA_TRUNCATION = "Data truncated.";
	public static final String ERROR_PARSE_JSON = "Parsing of Json failed";
	public static final String ERROR_JSON_TYPE_MISMATCH = "Datatype mismatch for json values";

	public static final String ERROR_PAGE_NOT_FOUND = "{\"" + KEY_STATUS + "\":\"FAILURE\",\"" + KEY_ERROR
			+ "\":\"Page not found.\"}";
	public static final String ERROR_INTERNAL_SERVER_ERROR = "{\"" + KEY_STATUS + "\":\"FAILURE\",\"" + KEY_ERROR
			+ "\":\"Internal Server Error.\"}";

	public static final char COMMA = ',';
	public static final char NEWLINE = '\n';

}
