package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsBean;
import com.b4utrade.helper.DJNewsSearchHelper;
import com.b4utrade.helper.EdgeNewsSearchHelper;
import com.b4utrade.helper.NewsDateComparator;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.util.B4UConstants;
import com.tacpoint.util.Utility;

/**
 * Shows Top Twenty News of Given Source
 *
 * @author Ankit
 */

public class QuoddTopTwentyNewsHeadlineAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddTopTwentyNewsHeadlineAction.class);

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
		try {
			response.setContentType("text/xml");
			HttpSession hs = request.getSession(false);
			String tickerName = request.getParameter("NEWSTICKER");
			String newsSource = request.getParameter("NEWS_SOURCE");
			if (tickerName == null) {
				tickerName = "N/A";
			}
			if (newsSource == null) {
				newsSource = B4UConstants.EDGE_SOURCE;
			}
			Vector<StockNewsUpdateHelper> newsResultList = new Vector<>();
			List<NewsBean> premiumNewsList = null;
			StringBuilder sb = new StringBuilder();
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
					int size = premiumNewsList.size();
					for (int i = 0; i < size; i++) {
						NewsBean bean = premiumNewsList.get(i);
						StockNewsUpdateHelper newsHelper = new StockNewsUpdateHelper();
						newsHelper.setTicker(tickerName);
						newsHelper.setLastNews(bean.getHeadline());
						newsHelper.setCategoriesAsString(bean.getCategories());
						newsHelper.setLastNewsSource(bean.getNewsSource());
						newsHelper.setLastNewsID(bean.getNewsID());
						newsHelper.setDowJones(B4UConstants.NEWS_EDGE_CODE);
						newsHelper.setLastNewsDate(
								Utility.getTimeAsPattern(Utility.DATE_PATTERN_DDMMMYYYYHHMISS, bean.getNewsDate()));
						newsResultList.addElement(newsHelper);
					}
				}
				List<NewsBean> aList = DJNewsSearchHelper.searchNews(tickerName.trim(), 20);
				if (aList == null)
					aList = new ArrayList<>();
				int size = aList.size();
				for (int i = 0; i < size; i++) {
					NewsBean bean = aList.get(i);
					StockNewsUpdateHelper newsHelper = new StockNewsUpdateHelper();
					newsHelper.setTicker(tickerName);
					newsHelper.setLastNews(bean.getHeadline());
					newsHelper.setCategoriesAsString(bean.getCategories());
					newsHelper.setLastNewsSource(bean.getNewsSource());
					newsHelper.setLastNewsID(bean.getNewsID());
					newsHelper.setLastNewsDate(
							Utility.getTimeAsPattern(Utility.DATE_PATTERN_DDMMMYYYYHHMISS, bean.getNewsDate()));
					newsResultList.addElement(newsHelper);
				}
				if (!newsResultList.isEmpty()) {
					Collections.sort(newsResultList, new NewsDateComparator());
				}
				if (newsResultList.size() > 20) {
					newsResultList.setSize(20);
				}
			} else {
				List<NewsBean> newsBeans = getEdgeNewsBeansList(tickerName, newsSource);
				if (newsBeans != null) {
					int size = newsBeans.size();
					log.info("QuoddTopTwentyNewsHeadlineAction result size " + size);
					for (int i = 0; i < size; i++) {
						NewsBean bean = newsBeans.get(i);
						StockNewsUpdateHelper newsHelper = new StockNewsUpdateHelper();
						newsHelper.setTicker(tickerName);
						newsHelper.setLastNews(bean.getHeadline());
						newsHelper.setCategoriesAsString(bean.getCategories());
						newsHelper.setLastNewsSource(bean.getNewsSource());
						newsHelper.setLastNewsID(bean.getNewsID());
						newsHelper.setDowJones(B4UConstants.NEWS_EDGE_CODE);
						newsHelper.setLastNewsDate(
								Utility.getTimeAsPattern(Utility.DATE_PATTERN_DDMMMYYYYHHMISS, bean.getNewsDate()));
						newsResultList.addElement(newsHelper);
					}
				} else
					log.info("QuoddTopTwentyNewsHeadlineAction result is null");
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(newsResultList);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddTopTwentyNewsHeadlineAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddTopTwentyNewsHeadlineAction.ActionForward() : no news found.";
			log.error(msg, e);
		}
		return null;
	}

	private List<NewsBean> getEdgeNewsBeansList(String tickerName, String newsSource) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
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
