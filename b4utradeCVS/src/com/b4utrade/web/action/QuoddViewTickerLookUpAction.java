package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

public class QuoddViewTickerLookUpAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddViewTickerLookUpAction.class);
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
		TickerLookupBean tluList = null;
		try {
			response.setContentType("text/xml");
			HttpSession hs = request.getSession(false);
			String tickerName = request.getParameter("SEARCH_CRITERIA");
			log.info("QuoddViewTickerLookUpAction.execute() : ticker from parameter is " + tickerName);
			String sCount = request.getParameter("SEARCH_COUNT");
			String sType = request.getParameter("SEARCH_TYPE");
			String searchBy = request.getParameter("SEARCH_BY");
			char searchType;
			if (sType == null)
				searchType = 'A';
			else
				searchType = sType.charAt(0);
			int searchCount;
			if (sCount != null)
				searchCount = Integer.parseInt(sCount);
			else
				searchCount = 1;
			if (tickerName == null) {
				tickerName = (String) hs.getValue("SEARCH_CRITERIA");
				if (tickerName == null)
					tickerName = "MSFT";
			}
			String tickerLookupUrl = Environment.get("TICKERLOOKUP_URL");
			try {
				String urlStr = tickerLookupUrl;
				switch (searchBy) {
				case "Symbol":
					urlStr = urlStr + "/symbol/type/" + searchType + "/ticker/" + URLEncoder.encode(tickerName);
					break;
				case "Company":
					urlStr = urlStr + "/company/type/" + searchType + "/name/" + URLEncoder.encode(tickerName);
					break;
				default:
					return null;
				}
				URL url = new URL(urlStr);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				int responseCode = urlConnection.getResponseCode();
				if (responseCode != 200) {
					log.error("QuoddViewTickerLookupAction : Unable to fetch list");
				}
				String jsonString = "";
				try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
					jsonString = br.readLine();
				}
				urlConnection.disconnect();
				tluList = new TickerLookupBean();
				tluList.setLoadingFailedFlag();
				tluList.setSearchCriteria(tickerName);
				tluList.setSearchCount(searchCount);
				tluList.setSearchType(searchType);
				jsonToBean(jsonString, tluList);
			} catch (Exception exc) {
				log.error("Exception encountered while trying to get TickerLookUp Data.", exc);
			}
			if (tluList == null)
				tluList = new TickerLookupBean();
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray)) {
				encoder.writeObject(tluList);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddViewTickerLookUpAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddViewTickerLookUpAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private void jsonToBean(String jsonString, TickerLookupBean lookupBean) {
		List<Map<String, String>> dataList = gson.fromJson(jsonString, gsonMapType);
		for (Map<String, String> dataMap : dataList) {
			DetailedQuoteBean bean = new DetailedQuoteBean();
			bean.setTickerName(dataMap.get("ticker"));
			bean.setCompanyName(dataMap.get("company"));
			bean.setExchange(dataMap.get("exchange"));
			bean.setType(dataMap.get("issueType"));
			bean.setCurrency(dataMap.get("currency"));
			bean.setCusipId(dataMap.get("cusip"));
			lookupBean.getSearchResult().add(bean);
		}
		if (lookupBean.getSearchCount() == 5 && lookupBean.getSearchResult().size() > 5) {
			lookupBean.setLoadingSuccessfulFlag();
			lookupBean.getSearchResult().setSize(5);
		}
	}

}
