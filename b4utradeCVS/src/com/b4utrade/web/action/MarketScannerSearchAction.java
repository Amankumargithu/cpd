package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import com.b4utrade.helper.MarketScannerHelper;

public class MarketScannerSearchAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(MarketScannerSearchAction.class);

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
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		Vector payload = new Vector<>();
		try {
			response.setContentType("text/html");
			Vector tickerVector = new Vector<>();
			Vector companyVector = new Vector<>();
			MarketScannerHelper msh = new MarketScannerHelper();
			msh.setDefaultData();
			msh.setIndustry(request.getParameter("industry"));
			msh.setExchange(request.getParameter("exchange"));
			msh.setPriceRange(request.getParameter("price"));
			msh.setTopTen(request.getParameter("topTen"));
			msh.setMarketCap(request.getParameter("marketCap"));
			msh.selectStocks(tickerVector, companyVector);
			if (!tickerVector.isEmpty()) {
				payload.add(0, tickerVector);
				payload.add(1, companyVector);
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray)) {
				encoder.writeObject(payload);
			}
			try (ServletOutputStream sos = response.getOutputStream()) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("MarketScannerSearchAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "Unable to retrieve market data.";
			log.error("MarketScannerSearchAction.execute(): " + msg, e);
			return null;
		}
		return null;
	}
}
