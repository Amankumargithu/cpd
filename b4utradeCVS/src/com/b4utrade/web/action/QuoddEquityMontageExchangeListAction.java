package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.tacpoint.util.Logger;

public class QuoddEquityMontageExchangeListAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddEquityMontageExchangeListAction.class);
	private static Map<String, String> exchangeMap = new HashMap<>();
	static {
		exchangeMap.put("NYSE MKT", "A");
		exchangeMap.put("BATS", "Z");
		exchangeMap.put("BATS Y", "Y");
		exchangeMap.put("EDGA", "J");
		exchangeMap.put("EDGX", "K");
		exchangeMap.put("New York", "N");
		exchangeMap.put("ISE", "I");
		exchangeMap.put("Boston", "B");
		exchangeMap.put("National", "C");
		exchangeMap.put("FINRA ADF", "D");
		exchangeMap.put("CBSX", "W");
		exchangeMap.put("Chicago", "M");
		exchangeMap.put("NASDAQ", "T");
		exchangeMap.put("NYSE Arca", "P");
		exchangeMap.put("Philadelphia", "X");
		exchangeMap.put("IEX", "V");
		exchangeMap.put("EPRL", "H");
		exchangeMap.put("LTSE", "L");
		exchangeMap.put("MEMX", "U");
	}

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
		Logger.log("QuoddEquityMontageExchangeListAction Exchange List called");
		try {
			response.setContentType("text/html");
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
				try (XMLEncoder xmlenc = new XMLEncoder(baos);) {
					xmlenc.writeObject(exchangeMap);
				}
				response.getOutputStream().write(baos.toByteArray());
			}
		} catch (Exception e) {
			String msg = "QuoddEquityMontageExchangeListAction Unable to retrieve data from the database.";
			log.error(msg, e);
		}
		return null;
	}
}
