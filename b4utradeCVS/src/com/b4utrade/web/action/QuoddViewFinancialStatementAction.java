package com.b4utrade.web.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;

public class QuoddViewFinancialStatementAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddViewFinancialStatementAction.class);

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
		String statementLink = "";
		String includeHeader = "TRUE";
		try {
			response.setContentType("text/html");
			HttpSession hs = request.getSession(false);
			String tickerName = request.getParameter("UPCLOSETICKER");
			log.info("QuoddViewFinancialStatementAction.execute() : ticker from parameter is " + tickerName);
			if (isSpace(tickerName))
				tickerName = "MSFT";
			String statementType = request.getParameter("STATEMENT_TYPE");
			if (isSpace(statementType))
				statementType = "balance";
			String period = request.getParameter("PERIOD");
			if (isSpace(period))
				period = "annual";
			includeHeader = request.getParameter("HEADER");
			if (isSpace(includeHeader))
				includeHeader = "TRUE";
			String topicName = "";
			String nextStatementName = "";
			String nextStatementType = "";
			String nextStatementPeriod = "";
			if (statementType.equals("income")) {
				nextStatementType = "income";
				if (period.equals("quarter")) {
					statementLink = "http://quodd.zacks.com/fundamentals/QuartIncomeSht.php?t=" + tickerName;
					topicName = "Quarterly Income Statement";
					nextStatementName = "Annual Income Statement";
					nextStatementPeriod = "annual";
				} else {
					statementLink = "http://quodd.zacks.com/fundamentals/AnnualIncomeSht.php?t=" + tickerName;
					topicName = "Annual Income Statement";
					nextStatementName = "Quarterly Income Statement";
					nextStatementPeriod = "quarter";
				}
			} else if (statementType.equals("cashflow")) {
				nextStatementType = "cashflow";
				statementLink = "http://quodd.zacks.com/fundamentals/AnnualCashFlow.php?t=" + tickerName;
				topicName = "Annual Cash Flow Statement";
				nextStatementName = "Annual Cash Flow Statement";
				nextStatementPeriod = "annual";
			} else {
				nextStatementType = "balance";
				if (period.equals("quarter")) {
					statementLink = "http://quodd.zacks.com/fundamentals/QuartBalanceSht.php?t=" + tickerName;
					topicName = "Quarterly Balance Sheet";
					nextStatementName = "Annual Balance Sheet";
					nextStatementPeriod = "annual";
				} else {
					statementLink = "http://quodd.zacks.com/fundamentals/AnnualBalanceSht.php?t=" + tickerName;
					topicName = "Annual Balance Sheet";
					nextStatementName = "Quarterly Balance Sheet";
					nextStatementPeriod = "quarter";
				}
			}
			request.setAttribute("COMPANY_NAME", tickerName);
			request.setAttribute("STATEMENT_LINK", statementLink);
			request.setAttribute("STATEMENT_NAME", topicName);
			request.setAttribute("NEXT_STATEMENT_TYPE", nextStatementType);
			request.setAttribute("NEXT_STATEMENT_PERIOD", nextStatementPeriod);
			request.setAttribute("NEXT_STATEMENT_NAME", nextStatementName);
		} catch (Exception e) {
			String msg = "QuoddViewFinancialStatementAction.ActionForward() : no financial statement found.";
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		if (statementLink.equals("N/A"))
			return (mapping.findForward("noFinancialStatement"));
		else if (includeHeader.equals("FALSE"))
			return (mapping.findForward("stockFinancialStatementBody"));
		else
			return (mapping.findForward("stockFinancialStatement"));
	}
}
