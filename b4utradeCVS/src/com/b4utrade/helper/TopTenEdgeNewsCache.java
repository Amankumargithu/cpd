package com.b4utrade.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.b4utrade.bean.NewsBean;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class TopTenEdgeNewsCache implements Runnable {

	/**
	 * The Vector containing all the news urls.
	 */
	private static HashMap<String, ArrayList<NewsBean>> newsHash = null;

	/**
	 * The instance of TopTenNewsCache.
	 */
	private static TopTenEdgeNewsCache instance = null;

	/**
	 * The allows for dynamic database queries.
	 */
	private static Thread thread = null;

	/**
	 * Determines whether or not to execute the run method.
	 */
	private static boolean doRun = false;

	/**
	 * The delay in milliseconds between database selects
	 */
	private static long delay = 0L;

	private TopTenEdgeNewsCache() {
		try {
			delay = Integer.parseInt(Environment.get("TOP_TEN_NEWS_CACHE_DELAY"));
		} catch (NumberFormatException nfe) {
			// 10 seconds...
			delay = 10000L;
		}
		newsHash = new HashMap<>();
		doRun = true;
		thread = new Thread(this);
		thread.start();
	}

	public static synchronized void init() {
		if (instance == null)
			instance = new TopTenEdgeNewsCache();
	}

	@Override
	public void run() {
		while (doRun) {
			HashMap<String, ArrayList<NewsBean>> news = null;
			try {
				news = EdgeNewsSearchHelper.selectGeneralNews(10);
			} catch (Exception e) {
				Logger.log("TopTenEdgeNewsCache.run() - Error encountered " + "while selecting top ten news.", e);
			}
			if (news != null) {
				newsHash = news;
			}
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
			}
		}
	}

	public static Map<String, ArrayList<NewsBean>> getLatestNews() {
		return newsHash;
	}

	public void finalize() {
		doRun = false;
		thread = null;
	}

}
