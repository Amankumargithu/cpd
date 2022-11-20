package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.StockOptionBean;
import com.b4utrade.ejb.FutureOptionDataHandler;
import com.b4utrade.ejb.OptionData;
import com.b4utrade.ejb.OptionDataHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.objectpool.ObjectPoolManager;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddGetFutureAndCommodityDetailedQuoteAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddGetFutureAndCommodityDetailedQuoteAction.class);

	private static final Type gsonMapType = new TypeToken<Map<String, Object>>() {
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
			String tickerName = request.getParameter("UPCLOSETICKER");
			log.info("QuoddGetFutureAndCommodityDetailedQuoteAction.execute() : ticker is " + tickerName);
			StockOptionBean sobean = null;
			if (tickerName == null) {
				tickerName = "N/A";
			}
			if (tickerName.startsWith("/")) {
				String stockOptionUrl = Environment.get("FUTURE_STOCK_OPTION_URL");
				try {
					String urlStr = stockOptionUrl + tickerName;
					URL url = new URL(urlStr);
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");
					urlConnection.setDoOutput(true);
					int responseCode = urlConnection.getResponseCode();
					if (responseCode != 200) {
						log.error("QuoddGetFutureAndCommodityDetailedQuoteAction : Unable to fetch stock option data");
					}
					String jsonString = "";
					try (BufferedReader br = new BufferedReader(
							new InputStreamReader(urlConnection.getInputStream()));) {
						jsonString = br.readLine();
					}
					urlConnection.disconnect();
					sobean = jsonToBean(jsonString);
				} catch (Exception exc) {
					log.error("Exception encountered while trying to get TickerLookUp Data.", exc);
				}

			} else if (tickerName.startsWith("O:")) {
				ObjectPoolManager opm = null;
				OptionDataHandler odh = null;
				int numTries = 0;
				opm = ObjectPoolManager.getInstance();
				odh = (OptionDataHandler) opm.getObject("OptionDataHandler", 1000);
				while (numTries <= 1) {
					try {
						if (odh != null) {
							OptionData od = odh.getRemoteInterface();
							if (od != null) {
								sobean = od.getStockOption(tickerName.toUpperCase());
							} else {
								log.error(
										"QuoddGetFutureAndCommodityDetailedQuoteAction.execute - OptionData remote interface object is null.");
								throw new Exception("Option Data Remote interface is null.");
							}
						} else
							log.error(
									"QuoddGetFutureAndCommodityDetailedQuoteAction.execute - OptionDataHandler is null.");
						break;
					} catch (Exception exc) {
						Logger.log(
								"QuoddGetFutureAndCommodityDetailedQuoteAction.execute - Exception encountered while trying to get Option Data.",
								exc);
						numTries++;
						if (odh != null)
							odh.init();
					}
				}
				if (odh != null)
					opm.freeObject("OptionDataHandler", odh);
			} else if (tickerName.startsWith("FO:")) {
				ObjectPoolManager opm = null;
				FutureOptionDataHandler odh = null;
				int numTries = 0;
				opm = ObjectPoolManager.getInstance();
				odh = (FutureOptionDataHandler) opm.getObject("FutureOptionDataHandler", 1000);
				while (numTries <= 1) {
					try {
						if (odh != null) {
							OptionData od = odh.getRemoteInterface();
							if (od != null) {
								sobean = od.getStockOption(tickerName.toUpperCase());
							} else {
								log.error(
										"QuoddGetFutureAndCommodityDetailedQuoteAction.execute - OptionData remote interface object is null.");
								throw new Exception("Option Data Remote interface is null.");
							}
						} else
							log.error(
									"QuoddGetFutureAndCommodityDetailedQuoteAction.execute - FutureOptionDataHandler is null.");
						break;
					} catch (Exception exc) {
						Logger.log(
								"QuoddGetFutureAndCommodityDetailedQuoteAction.execute - Exception encountered while trying to get FutureOption Data.",
								exc);
						numTries++;
						if (odh != null)
							odh.init();
					}
				}
				if (odh != null)
					opm.freeObject("FutureOptionDataHandler", odh);
			} else {
				log.error("invalid ticker " + tickerName);
				return null;
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(sobean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddGetFutureAndCommodityDetailedQuoteAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddGetFutureAndCommodityDetailedQuoteAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	private StockOptionBean jsonToBean(String jsonString) {
		StockOptionBean sobean = new StockOptionBean();
		try {
		Map<String, Object> dataMap = gson.fromJson(jsonString, gsonMapType);
		
		sobean.setTicker((String) dataMap.get("ticker"));
		sobean.setTickerInDB((String) dataMap.get("tickerInDB"));
		Map<String, Double> dateMap = (Map<String, Double>) dataMap.get("expirationDate");
		if(dateMap != null) {
			LocalDateTime localDateTime = LocalDateTime.of((dateMap.get("year")).intValue(),
					(dateMap.get("month")).intValue()+1, (dateMap.get("dayOfMonth")).intValue(),
					(dateMap.get("hourOfDay")).intValue(), (dateMap.get("minute")).intValue(),
					(dateMap.get("second")).intValue());

			Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			sobean.setExpirationDate(calendar);
		}
		sobean.setOpenInterest(((Double) dataMap.get("openInterest")).longValue());
		sobean.setExchange((String) dataMap.get("exchange"));
		sobean.setContractSize(((Double) dataMap.get("contractSize")).intValue());
		sobean.setLastClosedPrice((double) dataMap.get("lastClosedPrice"));
		sobean.setHighIn52Week((double) dataMap.get("highIn52Week"));
		sobean.setLowIn52Week((double) dataMap.get("lowIn52Week"));
		sobean.setSecurityDesc((String) dataMap.get("securityDesc"));
		sobean.setOpenPrice((double) dataMap.get("openPrice"));
		sobean.setOpenPriceRange2((double) dataMap.get("openPriceRange2"));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception for "+jsonString);
		}
		return sobean;
	}
}