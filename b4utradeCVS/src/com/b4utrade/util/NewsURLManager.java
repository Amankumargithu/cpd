package com.b4utrade.util;

import com.tacpoint.util.Environment;

public class NewsURLManager {
	
	private static final String NEWS_URL_KEY = "NEWS_URL_DOMAIN";
	private static final String EDGE_NEWS_URL_KEY = "EDGE_NEWS_URL_DOMAIN";
	
	private static final String DEFAULT_NEWS_DOMAIN = "http://209.10.213.209";
	
	private static final String EDGE_DEFAULT_NEWS_DOMAIN = "http://209.10.213.209";
	
	private static final String KEY = "/";
		
	
	public static String configure(String url) {
		
		if (url == null || url.length() == 0)
			return url;
		
		String replacementURL = Environment.get(NEWS_URL_KEY);
		
		if (replacementURL == null)
			replacementURL = DEFAULT_NEWS_DOMAIN;
		
		//
		// chop protocol and domain name from current URL string ...
        // there should be three slashes (2 after the protocol, and 1 after the domain & port number)
		//
		int begIndex = url.indexOf(KEY);
		begIndex = url.indexOf(KEY,++begIndex);
		begIndex = url.indexOf(KEY,++begIndex);
		
		if (begIndex < 0)
			return url;
		
		String newURL = replacementURL+url.substring(begIndex);
		
		//System.out.println("Old URL : "+url);
		//System.out.println("New URL : "+newURL);
		
		return newURL;
	}
	
	public static String formatNewsEdgeURL(String url) {
		
		if (url == null || url.length() == 0)
			return url;	
		
		String replacementURL = Environment.get(EDGE_NEWS_URL_KEY);
		
		if (replacementURL == null)
			replacementURL = EDGE_DEFAULT_NEWS_DOMAIN;
		
		//
		// chop protocol and domain name from current URL string ...
        // there should be three slashes (2 after the protocol, and 1 after the domain & port number)
		//
		int begIndex = url.indexOf(KEY);
		begIndex = url.indexOf(KEY,++begIndex);
		begIndex = url.indexOf(KEY,++begIndex);
		
		if (begIndex < 0)
			return url;
		
		String newURL = replacementURL+url.substring(begIndex);
		
		//System.out.println("Old URL : "+url);
		//System.out.println("New URL : "+newURL);
		
		return newURL;
	}
	
	public static void main(String[] args) {
		
		NewsURLManager.formatNewsEdgeURL("http://www.quodd.com/newsedge/201310140001TEDS____NEWS_____E352148-2013_2025.1.html");
	}

}
