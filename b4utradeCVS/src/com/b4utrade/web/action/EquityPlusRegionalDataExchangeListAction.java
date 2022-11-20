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

public class EquityPlusRegionalDataExchangeListAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(EquityPlusRegionalDataExchangeListAction.class);
	private static Map<String, String> exchangeMap = new HashMap<>();
	static {
		exchangeMap.put("CBOE", "C");
		exchangeMap.put("AMEX", "A");
		exchangeMap.put("BATS", "Z");
		exchangeMap.put("International", "I");
		exchangeMap.put("Pacific", "N");
		exchangeMap.put("Philadelphia", "X");
		exchangeMap.put("Boston", "B");
		exchangeMap.put("NASDAQ", "Q");
		exchangeMap.put("CBE C2", "W");
		exchangeMap.put("NASDAQ OMX BX", "T");
		exchangeMap.put("Miami", "M");
		exchangeMap.put("ISE Gemini", "H");
		exchangeMap.put("EDGX", "E");
		exchangeMap.put("ISE Mercury", "J");
		exchangeMap.put("MPRL", "P");
		exchangeMap.put("EMLD", "D");
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
		Logger.log("EquityPlusRegionalDataExchangeListAction Regional Data Exchange List called");
		try {
			response.setContentType("text/xml");
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
				try (XMLEncoder xmlenc = new XMLEncoder(baos);) {
					xmlenc.writeObject(exchangeMap);
				}
				response.getOutputStream().write(baos.toByteArray());
			}
		} catch (Exception e) {
			String msg = "EquityPlusRegionalDataExchangeListAction Unable to retrieve data from the database.";
			log.error(msg, e);
		}
		return null;
	}
}
