package com.b4utrade.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.b4utrade.cache.TickerListManager;
import com.b4utrade.cache.UserIdCache;
import com.b4utrade.helper.MPidListCache;
import com.b4utrade.helper.TopTenMoversCache;
import com.tacpoint.cache.CacheManager;
import com.tacpoint.servlet.DefaultServlet;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class StartUpServlet extends DefaultServlet {

	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		try {
			Environment.init();
			Logger.init();
			Logger.log("StartUpServlet.init - start initialization.");
			MPidListCache.init();
			CacheManager.init();
			TopTenMoversCache.init();
			UserIdCache.init();
			TickerListManager.getInstance();
			Logger.trace(Logger.INFO, "StartUpServlet.init - initialization is successful.");
		} catch (Exception e) {
			Logger.trace(Logger.CRITICAL, "StartUpServlet.init - exception occurred.", e);
			throw new ServletException();
		}
	}
}
