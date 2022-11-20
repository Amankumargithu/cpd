package com.b4utrade.web.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.ejb.OptionData;
import com.b4utrade.ejb.OptionDataHandler;
import com.google.gson.Gson;
import com.tacpoint.objectpool.ObjectPoolManager;

public class QuoddSherbrookChainAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddSherbrookChainAction.class);
	private static long requestCount = 0;
	private static long requestMinTime = 0;
	private static Gson gson = new Gson();
	private static HashMap<String, Integer> monthMap = new HashMap<>();
	static {
		monthMap.put("A", 1);
		monthMap.put("B", 2);
		monthMap.put("C", 3);
		monthMap.put("D", 4);
		monthMap.put("E", 5);
		monthMap.put("F", 6);
		monthMap.put("G", 7);
		monthMap.put("H", 8);
		monthMap.put("I", 9);
		monthMap.put("J", 10);
		monthMap.put("K", 11);
		monthMap.put("L", 12);
		monthMap.put("M", 1);
		monthMap.put("N", 2);
		monthMap.put("O", 3);
		monthMap.put("P", 4);
		monthMap.put("Q", 5);
		monthMap.put("R", 6);
		monthMap.put("S", 7);
		monthMap.put("T", 8);
		monthMap.put("U", 9);
		monthMap.put("V", 10);
		monthMap.put("W", 11);
		monthMap.put("X", 12);
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		try {
			long currentTimeMin = System.currentTimeMillis() / 1000 / 60;
			if (requestMinTime != currentTimeMin) {
				requestMinTime = currentTimeMin;
				requestCount = 1;
			} else {
				requestCount++;
				if (requestCount > 1000) {
					response.setStatus(403);
					return null;
				}
			}
			response.setContentType("application/json");
			String tickerName = request.getParameter("root_ticker");
			String username = request.getHeader("username");
			String password = request.getHeader("password");
			if (!("sherbrookoptions".equals(username) && "ondemandsherbrookoptions".equals(password))) {
				response.setStatus(401);
				return null;
			}
			ObjectPoolManager opm = null;
			OptionDataHandler odh = null;
			HashMap temphash = new HashMap<>();
			Set<String> optionTickerSet = null;
			int numTries = 0;
			while (numTries <= 1) {
				try {
					opm = ObjectPoolManager.getInstance();
					odh = (OptionDataHandler) opm.getObject("OptionDataHandler", 1000);
					if (odh != null) {
						OptionData od = odh.getRemoteInterface();
						if (od != null) {
							try {
								temphash = od.getOptionChain(tickerName.toUpperCase());
								if (temphash != null) {
									optionTickerSet = temphash.keySet();
								} else {
									log.warn(
											"QuoddSherbrookChainAction.getOptionDataList - unable to obtain OptionDataHandler from object pool");
								}
								break;
							} catch (Exception e) {
								log.warn(
										"QuoddSherbrookChainAction.execute - exception while connecting OptionDataHandler");
								temphash = od.getOptionChain(tickerName.toUpperCase());
							}
						} else {
							log.error(
									"QuoddSherbrookChainAction.execute - OptionData remote interface object is null.");
							throw new Exception("Option Data Remote interface is null.");
						}
					} else
						log.error("QuoddSherbrookChainAction.execute - OptionDataHandler is null.");
					break;
				} catch (Exception exc) {
					log.warn(
							"QuoddSherbrookChainAction.execute - Exception encountered while trying to get Option Data.",
							exc);
					if (odh != null)
						odh.init();
				} finally {
					numTries++;
					if (odh != null)
						opm.freeObject("OptionDataHandler", odh);
				}
			}
			Set<String> resultSet = new HashSet<>();
			if (optionTickerSet != null) {
				for (String s : optionTickerSet) {
					resultSet.add(s + "0");
				}
			}
			String map = gson.toJson(resultSet);
			try (ServletOutputStream sos = response.getOutputStream();) {
				response.setContentType("application/json");
				sos.write(map.getBytes());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddSherbrookChainAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddSherbrookChainAction Unable to retrieve data from the ejbCall.";
			log.error(msg, e);
		}
		return null;
	}

}