package com.quodd.common.cpd.util;

public interface CPDTickerValidator {

	static final String TORONTO_TSX_IDENTIFIER = ".CA/TO";
	static final String VENTURE_TSX_IDENTIFIER = ".CA/TV";
	static final String TORONTO_TSX_SUFFIX = ".T";
	static final String VENTURE_TSX_SUFFIX = ".V";

	public static boolean isEquityRegionalSymbol(String ticker) {
		return (ticker.lastIndexOf('/') > 0);
	}

	public static String canadianToQuoddSymbology(String ticker) {
		if (ticker.indexOf(TORONTO_TSX_IDENTIFIER) != -1)
			ticker = ticker.replace(TORONTO_TSX_IDENTIFIER, TORONTO_TSX_SUFFIX);
		if (ticker.indexOf(VENTURE_TSX_IDENTIFIER) != -1)
			ticker = ticker.replace(VENTURE_TSX_IDENTIFIER, VENTURE_TSX_SUFFIX);
		return ticker;
	}
}
