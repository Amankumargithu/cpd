package com.b4utrade.web.action;

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

import com.b4utrade.tsq.TSQMessageSelector;

public class QuoddSelectTSQDatesAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddSelectTSQDatesAction.class);

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
		StringBuilder sb = new StringBuilder();
		response.setContentType("text/text");
		try {
			List<String> results = TSQMessageSelector.obtainTSQDates();
			if (results != null) {
				results.forEach(date -> sb.append("," + date));
			}
		} catch (Exception e) {
			log.error("QuoddSelectTSQDatesAction.execute - error = " + e.getMessage(), e);
		}
		try (ServletOutputStream sos = response.getOutputStream();) {
			if (sb.length() > 1)
				sos.write(sb.toString().substring(1).getBytes());
			else
				sos.write(sb.toString().getBytes());
			sos.flush();
		} catch (Exception e) {
			log.error("QuoddSelectTSQDatesAction.execute() : encountered exception. ", e);
		}
		return null;
	}

}
