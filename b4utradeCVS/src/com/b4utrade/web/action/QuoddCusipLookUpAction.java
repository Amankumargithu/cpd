package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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

public class QuoddCusipLookUpAction extends B4UTradeDefaultAction {
	
	static Log log = LogFactory.getLog(QuoddCusipLookUpAction.class);
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
		TickerLookupBean lookUpBean = null;
		try {
			response.setContentType("text/xml");
			HttpSession hs = request.getSession(false);
			String cusipId = request.getParameter("CUSIP");
			log.info("QuoddCusipLookUpAction.execute() : CUSIP from parameter is " + cusipId);
			if (cusipId == null)
				cusipId = (String) hs.getValue("CUSIP");
			String tickerLookupUrl = Environment.get("TICKERLOOKUP_URL");
			try {
				String urlStr = tickerLookupUrl;
				urlStr = urlStr + "/cusip/" + cusipId;
				URL url = new URL(urlStr);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				int responseCode = urlConnection.getResponseCode();
				if (responseCode != 200) {
					log.error("QuoddCusipLookupAction : Unable to fetch list");
				}
				String jsonString = "";
				try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
					jsonString = br.readLine();
				}
				urlConnection.disconnect();
				lookUpBean = new TickerLookupBean();
				jsonToBean(jsonString, lookUpBean);
			} catch (Exception exc) {
				log.error("Exception encountered while trying to get TickerLookUp Data.", exc);
			}
			if (lookUpBean == null)
				lookUpBean = new TickerLookupBean();
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(lookUpBean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddCusipLookUpAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddCusipLookUpAction.execute() : Unable to retrieve data.";
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
	}
}
