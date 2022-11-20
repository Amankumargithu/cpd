package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.helper.TickerExclusionHelper;

public class TickerExclusionListDataUpdateAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(TickerExclusionListDataUpdateAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		List<String> tickerExclusionList = new ArrayList<>();
		try {
			response.setContentType("text/xml");
			String method = request.getParameter("function");
			String ticker = request.getParameter("ticker");
			log.info("TickerExclusionListDataUpdateAction: function is " + method + " ticker is " + ticker);
			TickerExclusionHelper tickerExclusionHelper = new TickerExclusionHelper();
			try {
				if (method.equals("fetch")) {
					tickerExclusionList = tickerExclusionHelper.getTickerExclusionList();
				}
			} catch (Exception exc) {
				log.error(" Exception encountered while trying to get TickerExclusionListData Data.", exc);
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(tickerExclusionList);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error(" TickerExclusionListDataUpdateAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "TickerExclusionListDataUpdateAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}
}
