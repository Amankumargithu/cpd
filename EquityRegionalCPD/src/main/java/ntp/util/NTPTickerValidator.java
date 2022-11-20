package ntp.util;

public class NTPTickerValidator 
{
	public final static String TORONTO_TSX_IDENTIFIER = ".CA/TO";
	public final static String VENTURE_TSX_IDENTIFIER = ".CA/TV";
	public static  final String TORONTO_TSX_SUFFIX = ".T";
	public static  final String VENTURE_TSX_SUFFIX = ".V";

	public static String getRootSymbol(String ticker) {
		if(ticker.indexOf(TORONTO_TSX_IDENTIFIER) != -1)
			ticker = ticker.replace(TORONTO_TSX_IDENTIFIER, TORONTO_TSX_SUFFIX);
		else if(ticker.indexOf(VENTURE_TSX_IDENTIFIER) != -1)
			ticker = ticker.replace(VENTURE_TSX_IDENTIFIER, VENTURE_TSX_SUFFIX );
		else if (ticker.contains("/"))
			ticker = ticker.substring(0, ticker.indexOf("/"));
		return ticker;
	}
	
	public static String canadianToQuoddSymbology(String ticker)
	{
		if(ticker.indexOf(TORONTO_TSX_IDENTIFIER) != -1)
			ticker = ticker.replace(TORONTO_TSX_IDENTIFIER, TORONTO_TSX_SUFFIX);
		if(ticker.indexOf(VENTURE_TSX_IDENTIFIER) != -1)
			ticker = ticker.replace(VENTURE_TSX_IDENTIFIER, VENTURE_TSX_SUFFIX );
		return ticker;
	}
	
	public static boolean isEquityRegionalSymbol(String ticker)
	{
		if(ticker.lastIndexOf('/') > 0)
			return true;
		return false;
	}

	public static boolean isCanadianStock(String ticker)
	{
		if(ticker.endsWith(TORONTO_TSX_IDENTIFIER) || ticker.endsWith(VENTURE_TSX_IDENTIFIER))
			return true;
		return false;
	}

	public static boolean isCanadianTicker(String ticker)
	{
		return (ticker.endsWith(TORONTO_TSX_SUFFIX)  || ticker.endsWith(VENTURE_TSX_SUFFIX));
	}
	
	public static boolean validationSucceeded(String ticker) 
	{
		if(ticker.indexOf('@') == -1 && 
				ticker.indexOf('~') == -1 &&
				ticker.indexOf('`') == -1 &&
				ticker.indexOf('!') == -1 &&
				ticker.indexOf('#') == -1 &&
				ticker.indexOf('$') == -1 &&
				ticker.indexOf('%') == -1 &&
				ticker.indexOf('^') == -1 && 
				ticker.indexOf('&') == -1 &&
				ticker.indexOf('*') == -1 &&
				ticker.indexOf('(') == -1 &&
				ticker.indexOf(')') == -1 &&
				ticker.indexOf('_') == -1 &&
				ticker.indexOf('=') == -1 && 
				ticker.indexOf('+') == -1 &&
				ticker.indexOf('{') == -1 &&
				ticker.indexOf('}') == -1 &&
				ticker.indexOf('[') == -1 &&
				ticker.indexOf(']') == -1 &&
				ticker.indexOf('|') == -1 &&
				ticker.indexOf('?') == -1 && 
				ticker.indexOf('>') == -1 &&
				ticker.indexOf('<') == -1 &&
				ticker.indexOf(',') == -1 &&
				ticker.indexOf('"') == -1 &&
				ticker.indexOf('\'') == -1 )
		{
			return true;
		}
		return false;
	}

	public static boolean isPinkSheet(String ticker)
	{
		if(ticker.endsWith("/BB") || ticker.endsWith("/OB") ||
				ticker.endsWith("/QX") || ticker.endsWith("/QB") ||
				ticker.endsWith("/GM") || ticker.endsWith("/PK") || ticker.endsWith(".PK"))
		{
			return true;
		}
		return false;
	}
	
	/*public static boolean isTSQDBPinkSheet(String ticker)
	{
		if(ticker.endsWith("/QX") || ticker.endsWith("/QB") ||
				ticker.endsWith("/GM") || ticker.endsWith("/PK") || ticker.endsWith(".PK"))
			return true;
		return false;
	}*/
	/*public static boolean isOTCTicker(String ticker)
	{
		if(ticker.endsWith("/OB") || ticker.endsWith("/BB") )
			return true;
		return false;
	}*/
	
	public static boolean isFutureSymbol(String ticker)
	{
		if(ticker.startsWith("/"))
			return true;
		return false;
	}
	
	/*public static String getNormalizedTicker(String ticker) 
	{
		Map<String, String> tierMap = TierTickerRetriever.getInstance().getTickerMap();
		if(tierMap.containsKey(ticker))
		{		   
			ticker = ticker + "/" + tierMap.get(ticker);
		}
		return ticker;
	}*/
}
