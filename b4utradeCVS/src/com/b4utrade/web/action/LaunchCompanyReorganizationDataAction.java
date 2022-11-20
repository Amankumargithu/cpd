package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

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

import com.b4utrade.helper.CompanyReorgnizationHelper;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;

/**
 * Handles Company reorgnization info
 *
 */

public class LaunchCompanyReorganizationDataAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(LaunchCompanyReorganizationDataAction.class);

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
			CompanyReorgnizationHelper reorgnisationHelper = CompanyReorgnizationHelper.getInstance();
			LinkedHashMap<String, String> tickerMap = reorgnisationHelper.getCompanyReorganiztionDataMap();
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(tickerMap);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error(
						"LaunchCompanyReorganizationDataAction.execute() : encountered exception from servlet output stream.",
						e);
			}
		} catch (Exception e) {
			String msg = "LaunchCompanyReorganizationDataAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

}
