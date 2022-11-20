package com.b4utrade.web.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.google.gson.Gson;

/**
 * Handles display of news corresponding to ticker
 *
 * @author Ankit
 */

public class QuoddScrollingNewsHeadlineJsonAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddScrollingNewsHeadlineJsonAction.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
	private static Gson gson = new Gson();

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
		Map<String, Object> newsHeadline = new HashMap<>();
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
		response.addHeader("Access-Control-Allow-Headers", "*");
		try {
			String tickerName = (String) request.getParameter("UPCLOSETICKER");
			String newsSource = (String) request.getParameter("NEWS_SOURCE");
			if (newsSource == null) {
				newsSource = B4UConstants.EDGE_SOURCE;
			}
			log.info("QuoddScrollingNewsHeadlineJsonAction.ActionForward() : newsTicker:" + tickerName + " source = "
					+ newsSource);
			if (tickerName == null) {
				return null;
			}
			LinkedList<Map<String, String>> listOfNews = new LinkedList<>();
			ArrayList<NewsBean> newsResultList = new ArrayList<>();
			if (newsSource.contains(B4UConstants.DJ_NEWS)) {
				List<NewsBean> djNewsList = DJNewsSearchHelper.searchNews(tickerName.trim(), 20);
				if (djNewsList != null) {
					for (NewsBean bean : djNewsList) {
						StockNewsBO newsBO = new StockNewsBO();
//						  newsBO.setNewsType("N"); // why it is required, it was present in QuoddScrollingNewsDetailAction.java 
						newsBO.setDJNewsID(bean.getNewsID());
						newsBO.selectDJNewsByID();
						bean.setCategoryType(B4UConstants.DJ_NEWS);
						// setting value to get database reference
						bean.setNewsLink(newsBO.getNewsLink());
						System.out.println("DJ News: " + bean.getCategories() + "," + bean.getCategoryID() + ","
								+ bean.getCategoryName() + "," + bean.getCategoryType() + ","
								+ bean.getFormattedNewsDate() + "," + bean.getHeadline() + ","
								+ bean.getLastUpdateTime() + "," + bean.getNewsID() + "," + bean.getNewsLink() + ","
								+ bean.getNewsSource() + "," + bean.getNewsStory() + "," + bean.getSeqID() + ","
								+ bean.getTickers());
						newsResultList.add(bean);

					}
				}
				String newsEdgeEntitlement = newsSource;
				newsEdgeEntitlement = newsEdgeEntitlement.replace("B4UConstants.DJ_NEWS", "");
				if (!newsEdgeEntitlement.isEmpty() && !",".equals(newsEdgeEntitlement)) {
					ArrayList<NewsBean> premiumNewsList = getEdgeNewsBeansList(tickerName, newsEdgeEntitlement);
					if (premiumNewsList != null) {
						for (NewsBean bean : premiumNewsList) {
							bean.setCategoryType(B4UConstants.EDGE_SOURCE); // setting value to get database reference
							newsResultList.add(bean);
						}
					}
					Collections.sort(newsResultList, new DJNewsBeanComparator());

				}
			} else {
				ArrayList<NewsBean> newsBeans = getEdgeNewsBeansList(tickerName.trim(), newsSource);
				if (newsBeans != null) {
					newsResultList.addAll(newsBeans);
				}
			}
			int resultSize = newsResultList.size();
			for (int i = 0; i < resultSize; i++) {
				NewsBean bean = newsResultList.get(i);
				Map<String, String> newsMap = new LinkedHashMap<>();
				newsMap.put("news_date", bean.getFormattedNewsDate());
				newsMap.put("news_source", bean.getNewsSource());
				newsMap.put("news_headline", bean.getHeadline());
				newsMap.put("news_link", bean.getNewsLink());
				listOfNews.add(newsMap);
			}
			newsHeadline.put("news_list", listOfNews);
		} catch (Exception e) {
			String msg = "QuoddScrollingNewsHeadlineAction.ActionForward() : no news found.";
			log.error(msg, e);
		}
		response.setContentType("application/json");
		response.getWriter().write(gson.toJson(newsHeadline));
		return null;
	}

	private ArrayList<NewsBean> getEdgeNewsBeansList(String tickerName, String newsSource) {
		EdgeNewsCriteriaBean criteriaBean = new EdgeNewsCriteriaBean();
		criteriaBean.setTickers(tickerName.trim());
		criteriaBean.setSources(newsSource);
		Calendar cal = Calendar.getInstance();
		criteriaBean.setEndDate(sdf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, -400);
		criteriaBean.setStartDate(sdf.format(cal.getTime()));
		return EdgeNewsSearchHelper.searchNews(criteriaBean, 20);
	}
}
