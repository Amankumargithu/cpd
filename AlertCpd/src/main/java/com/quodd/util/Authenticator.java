package com.quodd.util;

import static spark.Spark.halt;

import com.quodd.common.util.Constants;

import spark.Filter;
import spark.Request;
import spark.Response;

public class Authenticator {
	public static final String REQUEST_TYPE_OPTIONS = "OPTIONS";
	public static final int HTTP_RESPONSE_UNAUTHORIZED = 401; // not authenticated
	public static final String KEY_ERROR = "error";
	public static final String ERROR_MISSING_HEADER_JSON = "{\"" + Constants.KEY_STATUS + "\":\"FAILURE\",\""
			+ KEY_ERROR + "\":\"Header Missing.\"}";
	public static final String ERROR_FIRM_NOT_AUTHENTICATED_JSON = "{\"" + Constants.KEY_STATUS + "\":\"FAILURE\",\""
			+ KEY_ERROR + "\":\"Not authenticated.\"}";

	// quoddsystemalert:No5jg3!85nf#
	public static final String basicAuth = "Basic cXVvZGRzeXN0ZW1hbGVydDpObzVqZzMhODVuZiM=";

	public static final Filter authenticateSystemAlertApi = (Request request, Response response) -> {
		if (REQUEST_TYPE_OPTIONS.equalsIgnoreCase(request.requestMethod()))
			return;
		String auth = request.headers("Authorization");
		if (auth == null)
			halt(HTTP_RESPONSE_UNAUTHORIZED, ERROR_MISSING_HEADER_JSON);
		if (!basicAuth.equals(auth)) {
			response.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
			halt(HTTP_RESPONSE_UNAUTHORIZED, ERROR_FIRM_NOT_AUTHENTICATED_JSON);
		}

	};
}
