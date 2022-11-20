package com.quodd.common.api.util;

import java.security.SecureRandom;

public class ApiStatUtil {
	private static final String ALFA_NUMERIC_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final Integer UNIQUE_IDENTIFIER_LENGTH = Integer.valueOf(16);
	private static final SecureRandom random = new SecureRandom();

	public static String getUniqueIdentifier() {
		StringBuilder sb = new StringBuilder(UNIQUE_IDENTIFIER_LENGTH);
		for (int i = 0; i < UNIQUE_IDENTIFIER_LENGTH; i++)
			sb.append(ALFA_NUMERIC_STRING.charAt(random.nextInt(ALFA_NUMERIC_STRING.length())));
		return sb.toString();
	}

}
