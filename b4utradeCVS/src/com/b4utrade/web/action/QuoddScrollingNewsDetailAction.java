package com.b4utrade.web.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bo.StockNewsBO;
import com.b4utrade.util.B4UConstants;
import com.b4utrade.web.form.ScrollingNewsForm;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;
import com.tacpoint.exception.NoDataFoundException;

public class QuoddScrollingNewsDetailAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddScrollingNewsDetailAction.class);
	private static final String TYPE_NEWS = "N";

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
		ScrollingNewsForm newsForm = (ScrollingNewsForm) form;
		String includeHeader = "TRUE";
		String newsId = null;
		try {
			response.setContentType("text/html");
			// get news link, news date, and news description
			newsId = request.getParameter("NEWS_ID");
			String newsSource = request.getParameter("NEWS_SOURCE");
			String userNewsSource = request.getParameter("USER_NEWS_SOURCE");
			if (newsSource == null)
				newsSource = B4UConstants.EDGE_SOURCE;
			long newsIdLong = 0;
			try {
				newsIdLong = Long.parseLong(newsId);
			} catch (NumberFormatException ne) {
				newsIdLong = 0;
			}
			log.info("QuoddScrollingNewsDetailAction: NewsID= " + newsId + " source= " + newsSource + " userSource= "
					+ userNewsSource);
			StockNewsBO newsBO = new StockNewsBO();
			newsBO.setNewsType(TYPE_NEWS);
			if (newsSource.equals(B4UConstants.DJ_NEWS)) {
				newsBO.setDJNewsID(newsIdLong);
				newsBO.selectDJNewsByID();
			} else {
				newsBO.setID((int) newsIdLong);
				newsBO.selectCompanyNewsByID();
			}
			// get news page forward information
			String newsTicker = request.getParameter("NEWSTICKER");
			if (newsTicker == null)
				newsTicker = "";
			String backToFlag = request.getParameter("BACK_TO_ID");
			if (backToFlag == null)
				backToFlag = "";
			includeHeader = request.getParameter("HEADER");
			if (isSpace(includeHeader))
				includeHeader = "TRUE";
			newsForm.setNewsURL(newsBO.getNewsLink());
			newsForm.setNewsDate(newsBO.getNewsDate());
			newsForm.setNewsDesc(newsBO.getNewsDescription());
			newsForm.setNewsTicker(newsTicker);
			newsForm.setNewsBackToID(backToFlag);
			request.setAttribute("NEWS_SOURCE", newsSource);
			if (userNewsSource == null)
				userNewsSource = newsSource;
			request.setAttribute("USER_NEWS_SOURCE", userNewsSource);
			log.info("QuoddScrollingNewsDetailAction: NEWS_URL = " + newsBO.getNewsLink() + " NEWS_DESC = "
					+ newsBO.getNewsDescription());
		} catch (NoDataFoundException e) {
			String msg = "DJ News not found in database for news id :" + newsId;
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		} catch (Exception e) {
			String msg = "Unable to retrieve data.";
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		if (includeHeader.equals("FALSE"))
			return (mapping.findForward("stockNewsBody"));
		else
			return (mapping.findForward("success"));
	}
}
