package com.b4utrade.web.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsBean;
import com.b4utrade.bo.StockNewsBO;
import com.b4utrade.helper.DJNewsBeanComparator;
import com.b4utrade.helper.DJNewsSearchHelper;
import com.b4utrade.helper.EdgeNewsSearchHelper;
import com.b4utrade.util.B4UConstants;
import com.b4utrade.web.form.ScrollingNewsForm;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;

/**
 * Handles display of news corresponding to ticker
 *
 * @author Ankit
 */

public class QuoddScrollingNewsHeadlineAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddScrollingNewsHeadlineAction.class);

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
		try {
			response.setContentType("text/html");
			String tickerName = request.getParameter("UPCLOSETICKER");
			String newsSource = request.getParameter("NEWS_SOURCE");
			if (newsSource == null) {
				newsSource = B4UConstants.EDGE_SOURCE;
			}
			log.info("QuoddScrollingNewsHeadlineAction.ActionForward() : newsTicker:" + tickerName + " source = "
					+ newsSource);
			if (tickerName == null) {
				tickerName = "N/A";
			}
			newsForm.setNewsTicker(tickerName);
			Vector<StockNewsBO> listOfNews = new Vector<>();
			Vector<NewsBean> newsResultList = new Vector<>();
			ArrayList<NewsBean> premiumNewsList = null;
			StringBuffer sb = new StringBuffer();
			String premiumSource = "";
			if (newsSource.contains(B4UConstants.DJ_NEWS)) {
				String[] sources = newsSource.split(",");
				for (int i = 0; i < sources.length; i++) {
					if (!sources[i].equals(B4UConstants.DJ_NEWS)) {
						sb.append(",");
						sb.append(sources[i]);
					}
				}
				if (sb.length() > 1) {
					premiumSource = sb.substring(1);
					premiumNewsList = getEdgeNewsBeansList(tickerName, premiumSource);
					if (premiumNewsList != null) {
						int size = premiumNewsList.size();
						for (int i = 0; i < size; i++) {
							NewsBean bean = premiumNewsList.get(i);
							bean.setCategoryType(B4UConstants.EDGE_SOURCE); // setting value to get database reference
							newsResultList.add(bean);
						}
					}
				}
				ArrayList<NewsBean> aList = DJNewsSearchHelper.searchNews(tickerName.trim());
				if (aList != null) {
					int size = aList.size();
					for (int i = 0; i < size; i++) {
						NewsBean bean = aList.get(i);
						bean.setCategoryType(B4UConstants.DJ_NEWS); // setting value to get database reference
						newsResultList.add(bean);
					}
				}
				Collections.sort(newsResultList, new DJNewsBeanComparator());
				if (newsResultList.size() > 500) {
					newsResultList.setSize(500);
				}
				int resultSize = newsResultList.size();
				for (int i = 0; i < resultSize; i++) {
					NewsBean bean = newsResultList.get(i);
					StockNewsBO newsBo = new StockNewsBO();
					newsBo.setTicker(tickerName);
					newsBo.setNewsType("");
					newsBo.setNewsDescription(bean.getHeadline());
					newsBo.setNewsSource(bean.getNewsSource());
					newsBo.setDJNewsID(bean.getNewsID());
					newsBo.setNewsDate(bean.getFormattedNewsDate());
					newsBo.setNewsLink(bean.getCategoryType()); // using it in jsp for Source
					listOfNews.add(newsBo);
				}
			} else {
				ArrayList<NewsBean> newsBeans = getEdgeNewsBeansList(tickerName.trim(), newsSource);
				if (newsBeans != null) {
					int size = newsBeans.size();
					for (int i = 0; i < size; i++) {
						NewsBean bean = newsBeans.get(i);
						StockNewsBO newsBo = new StockNewsBO();
						newsBo.setTicker(tickerName);
						newsBo.setNewsType("");
						newsBo.setNewsDescription(bean.getHeadline());
						newsBo.setNewsSource(bean.getNewsSource());
						newsBo.setDJNewsID(bean.getNewsID());
						newsBo.setNewsDate(bean.getFormattedNewsDate());
						newsBo.setNewsLink(newsSource); // using it in jsp for Source
						listOfNews.add(newsBo);
					}
				}
			}
			request.setAttribute("NEWSLIST", listOfNews);
			request.setAttribute("NEWS_SOURCE", newsSource);
		} catch (Exception e) {
			String msg = "QuoddScrollingNewsHeadlineAction.ActionForward() : no news found.";
			log.error(msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		return (mapping.findForward("success"));
	}

	private ArrayList<NewsBean> getEdgeNewsBeansList(String tickerName, String newsSource) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		EdgeNewsCriteriaBean criteriaBean = new EdgeNewsCriteriaBean();
		criteriaBean.setTickers(tickerName.trim());
		criteriaBean.setSources(newsSource);
		Calendar cal = Calendar.getInstance();
		criteriaBean.setEndDate(sdf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, -400);
		criteriaBean.setStartDate(sdf.format(cal.getTime()));
		return EdgeNewsSearchHelper.searchNews(criteriaBean);
	}
}
