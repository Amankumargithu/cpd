package com.quodd.common.api.util;

public final class ApiCommonVariable {

	public static final int HTTP_RESPONSE_BAD_REQUEST = 400; // parameter not good
	public static final int HTTP_RESPONSE_UNAUTHORIZED = 401; // not authenticated
	public static final int HTTP_RESPONSE_FORBIDDEN = 403; // not authorized
	public static final String STATUS_FAILURE = "FAILURE";
	public static final String KEY_STATUS = "status";
	public static final String KEY_ERROR = "error";
	public static final String ERROR_USER_NOT_AUTHENTICATED_JSON = "{\"" + KEY_STATUS + "\":\"" + STATUS_FAILURE
			+ "\",\"" + KEY_ERROR + "\":\"Not authenticated.\"}";
	public static final String ERROR_USER_EXCEED_LIMIT_JSON = "{\"" + KEY_STATUS + "\":\"" + STATUS_FAILURE + "\",\""
			+ KEY_ERROR + "\":\"Exceeds limit\"}";
	public static final String ERROR_PAGE_NOT_FOUND = "{\"" + KEY_STATUS + "\":\"" + STATUS_FAILURE + "\",\""
			+ KEY_ERROR + "\":\"Page not found.\"}";
	public static final String ERROR_INTERNAL_SERVER_ERROR = "{\"" + KEY_STATUS + "\":\"" + STATUS_FAILURE + "\",\""
			+ KEY_ERROR + "\":\"Internal Server Error.\"}";
	public static final String ERROR_NOT_SUPPORTED = "{\"" + KEY_STATUS + "\":\"" + STATUS_FAILURE + "\",\"" + KEY_ERROR
			+ "\":\"Not supported.\"}";

	private ApiCommonVariable() {
		throw new IllegalStateException("Utility class");
	}
}
