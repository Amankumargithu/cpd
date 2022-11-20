package com.b4utrade.web.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.UserStreamerManagementBean;
import com.b4utrade.cache.TickerListManager;
import com.b4utrade.helper.RTDUsageLogManager;

/**
 * The class manages session check for RTD users. Provide URL info along with
 * session Check
 */

public class RTDUnsubscribeTickerAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(RTDUnsubscribeTickerAction.class);
	private static final TickerListManager manager = TickerListManager.getInstance();
	private static final RTDUsageLogManager logManager = RTDUsageLogManager.getInstance();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		String tickers = request.getParameter("TICKERS");
		String userId = request.getHeader("USERID");
		String versionID = request.getHeader("VERSION");
		log.info("version: " + versionID + " userId: " + userId + " tickers: " + tickers);
		if (userId == null || tickers == null)
			return null;
		try {
			UserStreamerManagementBean bean = manager.getUserStreamingBean(Integer.parseInt(userId));
			if (bean == null)
				return null;
			if (tickers.equals("ALL_TICKERS")) {
				bean.clearAllList();
				logManager.addQuery(userId, "2", versionID);
			} else {
				String[] tickerArr = tickers.split(",");
				HashSet<String> tickerSet = new HashSet<>(Arrays.asList(tickerArr));
				bean.unsubscribe(tickerSet);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
}