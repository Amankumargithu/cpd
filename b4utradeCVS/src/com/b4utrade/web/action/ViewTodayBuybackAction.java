package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
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
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;

public class ViewTodayBuybackAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(ViewTodayBuybackAction.class);
	private static final Type gsonMapType = new TypeToken<List<Map<String, String>>>() {
	}.getType();
	private static final Gson gson = new GsonBuilder().serializeNulls().create();

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			response.setContentType("text/html");
			request.getSession(false);
			String splitLast60DayDataUrl = Environment.get("SPLIT_BUYBACK_60_DAY_URL");
			// fundamental/api/splitBuyback/type
			Vector<StockBuyBackBO> v = new Vector<>();
			try {
				String urlStr = splitLast60DayDataUrl + "/buybacks";
				URL url = new URL(urlStr);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				int responseCode = urlConnection.getResponseCode();
				if (responseCode != 200) {
					log.error("ViewTodayBuybackAction : Unable to fetch buybacks");
				}
				String jsonString = "";
				try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
					jsonString = br.readLine();
				}
				urlConnection.disconnect();
				v = jsonToBean(jsonString);
			} catch (Exception exc) {
				log.error("Exception encountered while trying to get ViewTodayBuybackAction Data.", exc);
			}
			request.setAttribute("BUYBACKVECTOR", v);
		} catch (Exception e) {
			String msg = "Unable to retrieve data from the database.";
			log.error("ViewTodayBuybackAction.execute(): " + msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		return (mapping.findForward("success"));
	}

	private Vector<StockBuyBackBO> jsonToBean(String jsonString) {
		Vector<StockBuyBackBO> v = new Vector<>();
		List<Map<String, String>> dataList = gson.fromJson(jsonString, gsonMapType);
		for (Map<String, String> dataMap : dataList) {
			StockBuyBackBO temp_StockBuyBackBO = new StockBuyBackBO();
			temp_StockBuyBackBO.setTicker(dataMap.get("quoddTicker"));
			temp_StockBuyBackBO.setCompanyName(dataMap.get("name"));
			temp_StockBuyBackBO.setQuantity(dataMap.get("buyBack"));
			temp_StockBuyBackBO.setDetail(dataMap.get("details"));
			temp_StockBuyBackBO.setBuybackAnnounceDate(dataMap.get("announcementDate"));
			v.add(temp_StockBuyBackBO);
		}
		return v;
	}
}
