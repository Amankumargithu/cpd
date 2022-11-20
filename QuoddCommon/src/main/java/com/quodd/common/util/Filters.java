package com.quodd.common.util;

public interface Filters {

	public static boolean checkNullableStrings(String param1, String... params) {
		if (param1 == null)
			return false;
		for (String p : params)
			if (p == null)
				return false;
		return true;
	}

	public static boolean checkEmptyStrings(String param1, String... params) {
		if (param1.trim().length() == 0)
			return false;
		for (String p : params)
			if (p.trim().length() == 0)
				return false;
		return true;
	}
}
