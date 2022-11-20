package com.b4utrade.helper;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.b4utrade.bean.NewsBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Utility;

public class LatestDJNewsCache implements Runnable {
	private static LatestDJNewsCache instance = null;
	private static Thread thread = null;
	private static Vector newsVector = new Vector<>();
	public static final int MAX_DJ_NEWS_COUNT = 100;
	private static boolean doRun = false;
	private static long tzOffset = 0L;

	private LatestDJNewsCache() {
		doRun = true;
		try {
			tzOffset = Integer.parseInt(Environment.get("DJ_NEWS_TZ_OFFSET"));
		} catch (NumberFormatException nfe) {
		}
		thread = new Thread(this);
		thread.start();
	}

	public static void init() {
		if (instance == null) {
			instance = new LatestDJNewsCache();
		}
	}

	public static Vector getLatestNews() {
		if (instance == null)
			init();
		return newsVector;
	}

	public void run() {
		while (doRun) {
			try {
				refreshNews();
				Thread.sleep(5000L);
			} catch (Exception e) {
				System.out.println("Unable to refresh DJ news - " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private static void refreshNews() {
		NewsCriteriaDetailBean bean = new NewsCriteriaDetailBean();
		bean.setOperationType("OR");
		long now = System.currentTimeMillis() + tzOffset;
		bean.setStartDate(new Timestamp(now));
		List results = DJNewsSearchHelper.searchNews(bean, 100);
		Vector payload = new Vector();
		Iterator it = results.iterator();
		while (it.hasNext()) {
			NewsBean news = (NewsBean) it.next();
			StockNewsUpdateHelper item = new StockNewsUpdateHelper();
			item.setLastNewsID(news.getNewsID());
			item.setTicker(news.getTickers());
			item.setCategoriesAsString(news.getCategories());
			item.setLastNewsDate(Utility.getTimeAsPattern("dd-MMM-yyyy:HH:mm:ss", news.getNewsDate()));
			item.setLastNews(news.getHeadline());
			item.setLastNewsSource(news.getNewsSource());
			payload.addElement(item);
		}
		newsVector = payload;
	}

	public void finalize() {
		doRun = false;
		thread = null;
	}
}
