package com.b4utrade.web.action;

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

import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;

public class QuoddRegionalTradesAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddRegionalTradesAction.class);

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has
	 * already been completed.
	 *
	 * @param mapping The ActionMapping used to select this instance
	 * @param actionForm The optional ActionForm bean for this request (if any)
	 * @param request The HTTP request we are processing
	 * @param response The HTTP response we are creating
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 */
	public ActionForward execute(ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try
		{
			response.setContentType("text/html");
			byte[] tsqBytes = null;
			String symbol = request.getParameter("TICKER");
			if (symbol == null) {
				return (null);
			}
			try
			{                
				List tsqs = new ArrayList();                
				tsqBytes = com.b4utrade.helper.TSQCompressor.compress(tsqs);
			}
			catch (Exception e)
			{
				log.error("QuoddRegionalTradesAction.execute: encountered an error for user="+userName+". ", e);
			}
			ServletOutputStream sos = null;
			try
			{
				sos = response.getOutputStream();
				sos.write(tsqBytes);
				sos.flush();
			}
			catch (Exception e)
			{
				log.error("QuoddRegionalTradesAction.execute() : encountered exception. ", e);
				request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), "Unable to retrieve data.");
				return (mapping.findForward(FAILURE));
			}
			finally
			{
				if (sos != null)
				{
					try
					{
						sos.close();
					} catch (Exception e) {}
				}
			}
		}
		catch(Exception e) {
			String msg = "QuoddRegionalTradesAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		return (null);
	}
}
