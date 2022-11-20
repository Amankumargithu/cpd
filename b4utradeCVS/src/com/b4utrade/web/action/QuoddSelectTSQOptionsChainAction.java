package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.ejb.OptionData;
import com.b4utrade.ejb.OptionDataHandler;
import com.b4utrade.mysql.MySQLConnectionProvider;
import com.tacpoint.objectpool.ObjectPoolManager;
import com.tacpoint.util.Logger;

public class QuoddSelectTSQOptionsChainAction extends B4UTradeDefaultAction {
	static Log log = LogFactory.getLog(QuoddSelectTSQOptionsChainAction.class);

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it). Return an
	 * <code>ActionForward</code> instance describing where and how control should
	 * be forwarded, or <code>null</code> if the response has already been
	 * completed.
	 *
	 * @param mapping    The ActionMapping used to select this instance
	 * @param actionForm The optional ActionForm bean for this request (if any)
	 * @param request    The HTTP request we are processing
	 * @param response   The HTTP response we are creating
	 *
	 * @exception IOException      if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		try {
			response.setContentType("text/html");
			String date = request.getParameter("DATE");
			String currentDate = new SimpleDateFormat("MM/dd/yy").format(new Date());
			String rootTicker = request.getParameter("ROOT_TICKER");
			if (rootTicker == null)
				rootTicker = "MSFT";
			if (rootTicker.startsWith("I:"))
				rootTicker = rootTicker.substring(2);
			ObjectPoolManager opm = null;
			OptionDataHandler odh = null;
			HashMap<String, String> temphash = null;
			if (date == null || date.equals(currentDate)) {
				int numTries = 0;
				while (numTries <= 1) {
					try {
						opm = ObjectPoolManager.getInstance();
						odh = (OptionDataHandler) opm.getObject("OptionDataHandler", 1000);
						if (odh != null) {
							OptionData od = odh.getRemoteInterface();
							if (od != null) {
								Properties props = new Properties();
								try (InputStream istream = getClass()
										.getResourceAsStream("/indexticker_conversion.properties");) {
									props.load(istream);
									String optIndexTicker = props.getProperty(rootTicker.toUpperCase());
									if (optIndexTicker != null)
										temphash = od.getTSQOptionChain(optIndexTicker);
									else
										temphash = od.getTSQOptionChain(rootTicker.toUpperCase());
								} catch (Exception e) {
									log.warn(
											"QuoddSelectTSQOptionsChainAction.execute - exception encountered in looking up indexticker_conversion properties file");
									temphash = od.getTSQOptionChain(rootTicker.toUpperCase());
								}
							} else {
								log.info(
										"QuoddSelectTSQOptionsChainAction.execute - OptionData remote interface object is null.");
								throw new Exception("Option Data Remote interface is null.");
							}
						} else
							Logger.log("QuoddSelectTSQOptionsChainAction.execute - OptionDataHandler is null.");
						break;
					} catch (Exception exc) {
						Logger.log(
								"QuoddSelectTSQOptionsChainAction.execute - Exception encountered while trying to get Option Data.",
								exc);
						numTries++;
						if (odh != null)
							odh.init();
					} finally {
						if (odh != null)
							opm.freeObject("OptionDataHandler", odh);
					}
				}
			} else {
				temphash = new HashMap<>();
				String recordDate = new SimpleDateFormat("yyyyMMdd")
						.format(new SimpleDateFormat("MM/dd/yy").parse(date));
				String query = "SELECT TICKER, VOLUME FROM OPTIONS_CHAIN WHERE UNDERLYER = ? AND RECORD_DATE = ?";
				try (Connection mysqlConn = MySQLConnectionProvider.createConnection("mysql1").getConnection();
						PreparedStatement preSmt = mysqlConn.prepareStatement(query);) {
					preSmt.setString(1, rootTicker);
					preSmt.setString(2, recordDate);
					try (ResultSet rs = preSmt.executeQuery();) {
						while (rs.next()) {
							String ticker = rs.getString("TICKER");
							String nTrd = rs.getString("VOLUME");
							temphash.put(ticker, nTrd);
						}
					}
				}
			}
			ArrayList<String> tempList = new ArrayList<>();
			tempList.addAll(temphash.keySet());
			Collections.sort(tempList, TSQOptionSymbolComparator);
			ArrayList<String> resultList = new ArrayList<>();
			for (String s : tempList) {
				String nTrd = temphash.get(s);
				s = convertOptionSymbol(s, nTrd);
				resultList.add(s);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (XMLEncoder xmlenc = new XMLEncoder(baos);) {
				xmlenc.writeObject(resultList);
			}
			response.getOutputStream().write(baos.toByteArray());
		} catch (Exception e) {
			String msg = "QuoddSelectTSQOptionsChainAction Unable to retrieve data from the database.";
			log.error(msg, e);
		}
		return null;
	}

	private String convertOptionSymbol(String ticker, String nTrd) {
		try {
			String[] arr = ticker.split("\\\\");
			if (arr.length < 3)
				return ticker;
			String root = arr[0].substring(2); // Removing O:
			String strike = arr[2];
			String date = arr[1];
			char month = date.charAt(2);
			String isCall = month <= 'L' ? "C" : "P";
			String mon = mappingMonth.get("" + month);
			Date d = formatter.parse(date.substring(0, 2) + mon + date.substring(3));
			ticker = root + " " + tsqFormatter.format(d) + " " + strike + " " + isCall + " (" + nTrd + ")";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ticker;
	}

	private static HashMap<String, String> mappingMonth = new HashMap<>();
	private SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
	private SimpleDateFormat tsqFormatter = new SimpleDateFormat("MMMdd yyyy");
	static {
		mappingMonth.put("A", "01"); // call month jan
		mappingMonth.put("B", "02");
		mappingMonth.put("C", "03");
		mappingMonth.put("D", "04");
		mappingMonth.put("E", "05");
		mappingMonth.put("F", "06");
		mappingMonth.put("G", "07");
		mappingMonth.put("H", "08");
		mappingMonth.put("I", "09");
		mappingMonth.put("J", "10");
		mappingMonth.put("K", "11");
		mappingMonth.put("L", "12");
		mappingMonth.put("M", "01"); // put month jan ... so on
		mappingMonth.put("N", "02");
		mappingMonth.put("O", "03");
		mappingMonth.put("P", "04");
		mappingMonth.put("Q", "05");
		mappingMonth.put("R", "06");
		mappingMonth.put("S", "07");
		mappingMonth.put("T", "08");
		mappingMonth.put("U", "09");
		mappingMonth.put("V", "10");
		mappingMonth.put("W", "11");
		mappingMonth.put("X", "12");
	}

	public static Comparator<String> TSQOptionSymbolComparator = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			String[] arr1 = s1.split("\\\\");
			String[] arr2 = s2.split("\\\\");
			char c1 = arr1[1].charAt(2);
			if (c1 > 'L') {
				int i = c1 - 12;
				char nc1 = (char) i;
				arr1[1] = arr1[1].replace(c1, nc1);
			}
			char c2 = arr2[1].charAt(2);
			if (c2 > 'L') {
				int i = c2 - 12;
				char nc2 = (char) i;
				arr2[1] = arr2[1].replace(c2, nc2);
			}
			int comp = arr1[1].compareTo(arr2[1]);
			if (comp == 0) {
				try {
					Double d1 = Double.parseDouble(arr1[2]);
					Double d2 = Double.parseDouble(arr2[2]);
					comp = d1.compareTo(d2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return comp;
		}
	};
}
