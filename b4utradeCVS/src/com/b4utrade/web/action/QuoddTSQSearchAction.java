package com.b4utrade.web.action;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import com.b4utrade.bean.TSQCriteriaBean;
import com.b4utrade.helper.TSQCompressor;
import com.b4utrade.tsq.TSQMessageSelector;

public class QuoddTSQSearchAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddTSQSearchAction.class);

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

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;

		response.setContentType("text/xml");
		byte[] tsqBytes = null;
		String dataInputObj = request.getParameter("TSANDQ_SEARCH_BEAN");
		String dayS = request.getParameter("SEARCH_DAY");
		int day = 0;
		boolean vwap = false;
		String vwapS = request.getParameter("VWAP");
		if ((vwapS != null) && (vwapS.equals("true")))
			vwap = true;
		try {
			day = Integer.parseInt(dayS);
		} catch (Exception e) {
			day = 0;
		}
		log.info("QuoddTSQSearchAction.execute() : time and sales Quote search criteria = " + dataInputObj);
		Object resultObject = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(dataInputObj.getBytes());
			try (XMLDecoder decoder = new XMLDecoder(bais);) {
				resultObject = decoder.readObject();
			}
			TSQCriteriaBean criteria = (TSQCriteriaBean) resultObject;
			criteria.setDay(new Integer(day));
			if (vwap)
				criteria.setDisplay(TSQCriteriaBean.TSV);
			List tsqs = null;
			if (criteria.getMarketMakerId() == null)
				tsqs = TSQMessageSelector.execute(criteria);
			else
				tsqs = TSQMessageSelector.selectMarketMakers(criteria);
			if (tsqs != null)
				log.info("QuoddTSQSearchAction - result size : " + tsqs.size());
			tsqBytes = TSQCompressor.compress(tsqs);
		} catch (Exception e) {
			log.error("QuoddTSQSearchAction.execute: encountered an error for user= " + userName + ". ", e);
		}
		try (ServletOutputStream sos = response.getOutputStream();) {
			if (tsqBytes != null) {
				sos.write(tsqBytes);
				sos.flush();
			}
		} catch (Exception e) {
			log.error("QuoddTSQSearchAction.execute() : encountered exception. ", e);
		}
		return null;
	}
}
