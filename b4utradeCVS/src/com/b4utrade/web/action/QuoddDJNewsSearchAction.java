package com.b4utrade.web.action;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

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

import com.b4utrade.bean.NewsCriteriaBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
import com.b4utrade.helper.DJNewsSearchHelper;

/**
 * Retrieves Dow Jones News by search criteria.
 */

public class QuoddDJNewsSearchAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddDJNewsSearchAction.class);
	private static final long ONE_DAY = 1000 * 60 * 60 * 24;

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
		HttpSession hs = request.getSession(false);
		log.info("QuoddDJNewsSearchAction.execute() : Retrieve user info from session.");
		byte[] newsBytes = null;
		String dataInputObj = request.getParameter("NEWS_SEARCH_BEAN");
		log.info("QuoddDJNewsSearchAction.execute() : News search criteria = " + dataInputObj);
		Object resultObject = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(dataInputObj.getBytes());
			try (XMLDecoder decoder = new XMLDecoder(bais);) {
				resultObject = decoder.readObject();
			}
			NewsCriteriaBean criteriaBean = (NewsCriteriaBean) resultObject;
			// get Dow Jones news
			if (criteriaBean.getCategoryV() != null && !criteriaBean.getCategoryV().isEmpty()) {
				NewsCriteriaDetailBean ncdbean = (NewsCriteriaDetailBean) (criteriaBean.getCategoryV()).elementAt(0);
				if (criteriaBean.getEndDate() != null && criteriaBean.getEndDate().trim().length() != 0) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
						Date tempDate = sdf.parse(criteriaBean.getEndDate());
						ncdbean.setEndDate(new Timestamp(tempDate.getTime()));
						Calendar cal = Calendar.getInstance();
						ncdbean.setStartDate(new Timestamp(cal.getTimeInMillis() - (400 * ONE_DAY)));
					} catch (Exception ee) {
						Calendar cal1 = Calendar.getInstance();
						ncdbean.setEndDate(new Timestamp(cal1.getTimeInMillis()));
					}
				} else {
					ncdbean.setStartDate(null);
					ncdbean.setEndDate(null);
				}
				if (ncdbean.getTicker() != null) {
					StringTokenizer st = new StringTokenizer(ncdbean.getTicker(), ",");
					ArrayList<String> tickerlist = new ArrayList<>();
					while (st.hasMoreTokens()) {
						tickerlist.add((st.nextToken()).trim());
					}
					ncdbean.setTickers(tickerlist);
					log.info("QuoddDJNewsSearchAction: ticker search criteria=" + ncdbean.getTicker()
							+ " ticker list size=" + (ncdbean.getTickers()).size());
				}
				if (ncdbean.getCategory() != null) {
					StringTokenizer st = new StringTokenizer(ncdbean.getCategory(), ",");
					ArrayList<String> categorylist = new ArrayList<>();
					while (st.hasMoreTokens()) {
						categorylist.add((st.nextToken()).trim());
					}
					ncdbean.setCategories(categorylist);
					log.info("QuoddDJNewsSearchAction: category search criteria=" + ncdbean.getCategory()
							+ " category list size=" + ncdbean.getCategories().size());
				}
				long beginTime = System.currentTimeMillis();
				newsBytes = DJNewsSearchHelper.searchCompressedNews(ncdbean);
				long endTime = System.currentTimeMillis();
				log.info("Time to pull compressed news data from DJ news cache : " + (endTime - beginTime));
			}
		} catch (Exception e) {
			log.error("QuoddDJNewsSearchAction.execute: encountered an error. ", e);
		}
		try (ServletOutputStream sos = response.getOutputStream();) {
			if (newsBytes != null) {
				sos.write(newsBytes);
				sos.flush();
			}
		} catch (Exception e) {
			log.error("QuoddDJNewsSearchAction.execute() : encountered exception. ", e);
		}
		return null;
	}
}
