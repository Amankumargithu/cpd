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

public class QuoddSaveUserEdgeNewsCriteriaAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddSaveUserEdgeNewsCriteriaAction.class);

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
				log.error("QuoddSaveUserEdgeNewsCriteriaAction.execute: unable to find USER_ID from request cookie.");
				statusbean.setOperationFailed();
			} else {
				String dataInputObj = request.getParameter("NEWS_SEARCH_BEAN");
				log.info("QuoddSaveUserEdgeNewsCriteriaAction.execute() : News search criteria = " + dataInputObj
						+ " id: " + userId);
				Object resultObject = null;
				try (ByteArrayInputStream bais = new ByteArrayInputStream(dataInputObj.getBytes());
						XMLDecoder decoder = new XMLDecoder(bais);) {
					resultObject = decoder.readObject();
					EdgeNewsCriteriaBean criteriaBean = (EdgeNewsCriteriaBean) resultObject;
					NewsSelector.deleteEdgeNewsCriteria(userId, criteriaBean);
					boolean flag = NewsSelector.saveEdgeNewsCriteria(userId, criteriaBean);
					if (flag)
						statusbean.setOperationSuccessful();
					else
						statusbean.setOperationFailed();
				} catch (Exception e) {
					log.error("QuoddSaveUserEdgeNewsCriteriaAction.execute: encountered an error for user=" + userId,
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
				log.error("QuoddSaveUserEdgeNewsCriteriaAction.execute() : servlet output stream exception. ", e);
			}
		} catch (Exception e) {
			String msg = "Unable to save data.";
			log.error("QuoddSaveUserEdgeNewsCriteriaAction.execute(): " + msg, e);
		}
		return null;
	}
}
