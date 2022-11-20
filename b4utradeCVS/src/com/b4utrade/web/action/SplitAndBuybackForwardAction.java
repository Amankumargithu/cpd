package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bo.StockBuyBackBO;
import com.b4utrade.bo.StockSplitBO;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;

public class SplitAndBuybackForwardAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(SplitAndBuybackForwardAction.class);
	private static final Type gsonMapType = new TypeToken<List<Map<String, String>>>() {
	}.getType();
	private static final Gson gson = new GsonBuilder().serializeNulls().create();

	private static final String FROM_SPLIT = "SPLIT";
	private static final String FROM_STATUS = "STATUS";
	private static final String TO_SPLIT = "viewsplits";
	private static final String TO_STATUS = "viewstatus";

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		String forwardURL = FAILURE;
		try {
			response.setContentType("text/html");
			request.getSession(false);
			String fromPage = (String) request.getParameter("FROMPAGE");
			if (fromPage == null || fromPage.trim().length() == 0) {
				log.error(
						"SplitAndBuybackForwardAction.execute(): FROMPAGE Parameter is not found for split and buyback.");
				return (mapping.findForward(FAILURE));
			}
			if (fromPage.trim().equals(FROM_SPLIT)) {
				getSplitStatus(request);
				forwardURL = TO_SPLIT;
			} else if (fromPage.trim().equals(FROM_STATUS)) {
				getStatuses(request);
				forwardURL = TO_STATUS;
			} else {
				log.error("SplitAndBuybackForwardAction.execute(): invalid FROMPAGE Parameter=" + fromPage);
			}
		} catch (Exception e) {
			String msg = "Unable to retrieve splits or buyback data.";
			log.error("SplitAndBuybackForwardAction.execute(): " + msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		return (mapping.findForward(forwardURL));
	}

	/**
	 * Retrieve splits by date for a ticker from the database.
	 *
	 * @param request    The HTTP request we are processing
	 * @param searchDate Formatted date to use as search criteria.
	 * @exception Exception
	 */
	private void getSplitStatus(HttpServletRequest request) throws Exception {
		String displayDate = (String) request.getParameter("DISPLAYDATE");
		if (displayDate == null)
			displayDate = "";
		request.setAttribute("DISPLAYDATE", displayDate);
		String searchDate = (String) request.getParameter("SEARCHDATE");
		if (searchDate == null || searchDate.trim().length() == 0) {
			log.warn(
					"SplitAndBuybackForwardAction.getSplitStatus(): SEARCHDATE Parameter is missing for split and buyback.");
			return;
		}
		Object sDate = getFormattedDate(LocalDate.parse(searchDate, new DateTimeFormatterBuilder()
				.parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH)), "%tY%<tm%<td");
		String splitDataByDate = Environment.get("SPLIT_BY_DATE_URL");
		// fundamental/api/splitBuyback/date
		Vector<StockSplitBO> v = new Vector<>();
		try {
			String urlStr = splitDataByDate + "/" + sDate;
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				log.error("SplitAndBuybackForwardAction : Unable to fetch list");
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			List<Map<String, String>> dataList = gson.fromJson(jsonString, gsonMapType);
			for (Map<String, String> dataMap : dataList) {
				StockSplitBO temp_StockSplitBO = new StockSplitBO();
				temp_StockSplitBO.setTicker(dataMap.get("quoddTicker"));
				temp_StockSplitBO.setCompanyName(dataMap.get("name"));
				temp_StockSplitBO.setSplitRatio(dataMap.get("split"));
				temp_StockSplitBO.setSplitEffectiveDate(dataMap.get("payDate"));
				v.add(temp_StockSplitBO);
			}
		} catch (Exception exc) {
			log.error("Exception encountered while trying to get TickerLookUp Data.", exc);
		}
		request.setAttribute("SEARCHDATE", searchDate);
		request.setAttribute("SPLITVECTOR", v);
	}

	/**
	 * Retrieve split and buyback status reports for a ticker from the database.
	 *
	 * @param request The HTTP request we are processing
	 */
	private void getStatuses(HttpServletRequest request) {
		String searchTicker = request.getParameter("TICKERNAME");
		if (searchTicker == null || searchTicker.trim().length() == 0) {
			log.warn(
					"SplitAndBuybackForwardAction.getStatuses(): TICKERNAME Parameter is missing for split and buyback.");
			return;
		}
		String splitDataByDate = Environment.get("SPLIT_BUYBACK_BY_TICKER_URL");
		// fundamental/api/splitBuyback/ticker
		try {
			String urlStr = splitDataByDate + "/" + (searchTicker.trim()).toUpperCase();
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				log.error("SplitAndBuybackForwardAction : Unable to fetch list");
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			List<Map<String, String>> dataList = gson.fromJson(jsonString, gsonMapType);
			Map<String, String> splitDataMap = dataList.get(0);
			Map<String, String> buyBackDataMap = dataList.get(1);
			StockSplitBO sbo = new StockSplitBO();
			sbo.setTicker(splitDataMap.get("quoddTicker"));
			sbo.setCompanyName(splitDataMap.get("name"));
			sbo.setSplitAnnounceDate(splitDataMap.get("announcementDate"));
			sbo.setPriceAtSplit(splitDataMap.get("priceAtSplit"));
			sbo.setSplitEffectiveDate(splitDataMap.get("payDate"));
			sbo.setSplitRatio(splitDataMap.get("split"));
			sbo.setCurrentDate(splitDataMap.get("recordDate"));
			request.setAttribute("STOCKSPLIT", sbo);
			StockBuyBackBO bbo = new StockBuyBackBO();
			bbo.setTicker(buyBackDataMap.get("quoddTicker"));
			bbo.setCompanyName(buyBackDataMap.get("name"));
			bbo.setBuybackAnnounceDate(buyBackDataMap.get("announcementDate"));
			bbo.setPriceAtBuyBack(buyBackDataMap.get("priceAtBuyback"));
			bbo.setQuantity(buyBackDataMap.get("buyBack"));
			bbo.setDetail(buyBackDataMap.get("details"));
			request.setAttribute("STOCKBUYBACK", bbo);
		} catch (Exception exc) {
			log.error("Exception encountered while trying to get SplitAndBuybackForwardAction Data.", exc);
		}
		request.setAttribute("TICKERNAME", searchTicker.trim().toUpperCase());
	}

	private Object getFormattedDate(Object value, String dateFormat) {
		if (value == null)
			return value;
		if (value instanceof LocalDate || value instanceof Date)
			return String.format(dateFormat, value);
		return value;
	}
}
