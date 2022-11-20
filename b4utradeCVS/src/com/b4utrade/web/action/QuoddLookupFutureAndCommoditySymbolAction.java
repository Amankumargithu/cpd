package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.DetailedQuoteBean;
import com.b4utrade.bean.TickerLookupBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddLookupFutureAndCommoditySymbolAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLookupFutureAndCommoditySymbolAction.class);

	private static final Type gsonMapType = new TypeToken<ArrayList<Map<String, Object>>>() {
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
			response.setContentType("text/xml");
			final String byDesc = "DESC";
			final String bySymbol = "SYMB";
			Vector<DetailedQuoteBean> criteriaList = null;
			String tickerName = request.getParameter("SYMBOL_NAME");
			String searchType = request.getParameter("SEARCH_TYPE");
			String isFilterOn = request.getParameter("FILTER");
			int pagingIndex = Integer.parseInt(request.getParameter("INDEX"));
			if (tickerName == null)
				tickerName = "NG";
			ArrayList<Map<String, Object>> results = null;
			try {
				if (searchType.equals(bySymbol)) {
					criteriaList = getFutureChainByBaseSymbol(tickerName.toUpperCase(), isFilterOn.equals("true"));
				}
				if (searchType.equals(byDesc)) {
					criteriaList = getFutureChainByDescription(tickerName, pagingIndex, isFilterOn.equals("true"));
				}
			} catch (Exception exc) {
				Logger.log(
						"QuoddLookupFutureAndCommoditySymbolAction.execute - Exception encountered while trying to get Option Data.",
						exc);
			}

			TickerLookupBean lookupBean = new TickerLookupBean();
			lookupBean.setSearchResult(criteriaList);
			lookupBean.setLoadingSuccessfulFlag();
			if (criteriaList == null) {
				criteriaList = new Vector<>();
				lookupBean.setSearchResult(criteriaList);
				lookupBean.setLoadingFailedFlag();
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(lookupBean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLookupFutureAndCommoditySymbolAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddLookupFutureAndCommoditySymbolAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private Vector<DetailedQuoteBean> getFutureChainByDescription(String tickerName, int pagingIndex, boolean equals) {
		String url = Environment.get("FUTURE_CHAIN_BY_DESC_URL");
		return getResultFromUrl(url + tickerName + "/" + pagingIndex + "/" + equals, equals);
	}

	private Vector<DetailedQuoteBean> getFutureChainByBaseSymbol(String ticker, boolean equals) {
		String url = Environment.get("FUTURE_CHAIN_BY_BASE_URL");
		return getResultFromUrl(url + ticker, equals);
	}

	private Vector<DetailedQuoteBean> getResultFromUrl(String urlString, boolean isFilterOn) {
		Vector<DetailedQuoteBean> criteriaList = new Vector<>();
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				log.error("QuoddLookupFutureAndCommoditySymbolAction : Unable to fetch data");
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			ArrayList<Map<String, Object>> dataMap = gson.fromJson(jsonString, gsonMapType);
			if (dataMap != null) {
				DetailedQuoteBean dqbean = null;
				for (Map<String, Object> map : dataMap) {
					if ((((String) map.get("ticker")).indexOf(".")) > 4)
						continue;
					if (isFilterOn && ((String) map.get("ticker")).indexOf("F2:") < 0) {
						continue;
					}

					dqbean = new DetailedQuoteBean();
					dqbean.setTickerName((String) map.get("ticker"));
					dqbean.setCompanyName((String) map.get("securityDesc"));
					dqbean.setExchange((String) map.get("exchange"));
					criteriaList.addElement(dqbean);
				}
			}

			return criteriaList;
		} catch (Exception exc) {
			log.error("Exception encountered while trying to get TickerLookUp Data.", exc);
		}
		return null;
	}

}