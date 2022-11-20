package com.b4utrade.web.action;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.QuoddUserOperationStatusBean;
import com.b4utrade.dataaccess.NewsSelector;

public class QuoddDeleteUserEdgeNewsCriteriaAction extends B4UTradeDefaultAction {
	static Log log = LogFactory.getLog(QuoddDeleteUserEdgeNewsCriteriaAction.class);

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
		try {
			Cookie[] cookies = request.getCookies();
			String userIdStr = "0";
			for (Cookie cook : cookies) {
				if ("USER_ID".equals(cook.getName()))
					userIdStr = cook.getValue();
			}
			long userId = 0;
			try {
				userId = Long.parseLong(userIdStr);
			} catch (Exception e) {
				userId = 0;
			}
			QuoddUserOperationStatusBean statusbean = new QuoddUserOperationStatusBean();
			if (userId <= 0) {
				log.error("QuoddDeleteUserEdgeNewsCriteriaAction.execute: unable to find USER_ID from request cookie.");
				statusbean.setOperationFailed();
			} else {
				String dataInputObj = request.getParameter("NEWS_SEARCH_BEAN");
				log.info("QuoddDeleteUserEdgeNewsCriteriaAction.execute() : News search criteria = " + dataInputObj
						+ " id: " + userId);
				Object resultObject = null;
				try (ByteArrayInputStream bais = new ByteArrayInputStream(dataInputObj.getBytes());
						XMLDecoder decoder = new XMLDecoder(bais);) {
					resultObject = decoder.readObject();
					EdgeNewsCriteriaBean criteriaBean = (EdgeNewsCriteriaBean) resultObject;
					boolean ok = NewsSelector.deleteEdgeNewsCriteria(userId, criteriaBean);
					if (ok)
						statusbean.setOperationSuccessful();
					else
						statusbean.setOperationFailed();
				} catch (Exception e) {
					log.error("QuoddDeleteUserEdgeNewsCriteriaAction.execute: encountered an error for user=" + userId,
							e);
					statusbean.setOperationFailed();
				}
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(statusbean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddDeleteUserEdgeNewsCriteriaAction.execute() : servlet output stream exception. ", e);
			}
		} catch (Exception e) {
			String msg = "Unable to delete data.";
			log.error("QuoddDeleteUserEdgeNewsCriteriaAction.execute(): " + msg, e);
		}
		return null;
	}
}
