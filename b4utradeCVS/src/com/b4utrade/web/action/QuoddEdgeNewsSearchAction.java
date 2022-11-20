package com.b4utrade.web.action;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import com.b4utrade.helper.EdgeNewsSearchHelper;

/**
 * Retrieves Edge News by search criteria.
 *
 * @author Ankit
 */

public class QuoddEdgeNewsSearchAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddEdgeNewsSearchAction.class);

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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		try {
			response.setContentType("text/xml");
			HttpSession hs = request.getSession(false);
			log.info("QuoddEdgeNewsSearchAction.execute() : Retrieve user info from session.");
			byte[] newsBytes = null;
			String dataInputObj = request.getParameter("EDGE_NEWS_SEARCH_BEAN");
			log.info("QuoddEdgeNewsSearchAction.execute() : News search criteria = " + dataInputObj);
			Object resultObject = null;
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(dataInputObj.getBytes());
				try (XMLDecoder decoder = new XMLDecoder(bais);) {
					resultObject = decoder.readObject();
				}
				EdgeNewsCriteriaBean criteriaBean = (EdgeNewsCriteriaBean) resultObject;
				if (criteriaBean.getEndDate() != null) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, -400);
					criteriaBean.setStartDate(sdf.format(cal.getTime()));
				} else {
					criteriaBean.setStartDate(null);
					criteriaBean.setEndDate(null);
				}
				long beginTime = System.currentTimeMillis();
				newsBytes = EdgeNewsSearchHelper.searchCompressedNews(criteriaBean);
				long endTime = System.currentTimeMillis();
				log.info("Time to pull compressed news data from Edge news cache : " + (endTime - beginTime));
			} catch (Exception e) {
				log.error("QuoddEdgeNewsSearchAction.execute: encountered an error. ", e);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				if (newsBytes != null) {
					sos.write(newsBytes);
					sos.flush();
				}
			} catch (Exception e) {
				log.error("QuoddEdgeNewsSearchAction.execute() : encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "QuoddEdgeNewsSearchAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}
}
