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
import java.util.HashMap;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;

public class QuoddLoadFutureAndCommoditySpotSymbolMapAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadFutureAndCommoditySpotSymbolMapAction.class);
	private static final Type gsonMapType = new TypeToken<Map<String, ArrayList<String>>>() {
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
			HashMap temphash = null;
			String spotSymbolUrl = Environment.get("FUTURE_SPOT_SYMBOL_URL");
			try {
				URL url = new URL(spotSymbolUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				int responseCode = urlConnection.getResponseCode();
				if (responseCode != 200) {
					log.error("QuoddLoadFutureAndCommoditySpotSymbolMapAction : Unable to fetch spot symbol data");
				}
				String jsonString = "";
				try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
					jsonString = br.readLine();
				}
				urlConnection.disconnect();
				temphash = gson.fromJson(jsonString, gsonMapType);
			} catch (Exception exc) {
				log.error("Exception encountered while trying to get TickerLookUp Data.", exc);
			}
			if (temphash == null)
				temphash = new HashMap<>();
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(temphash);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLoadFutureAndCommoditySpotSymbolMapAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddLoadFutureAndCommoditySpotSymbolMapAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}
}
