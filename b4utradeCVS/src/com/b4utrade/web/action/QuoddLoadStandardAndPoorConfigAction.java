package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.StandardAndPoorConfigBean;

public class QuoddLoadStandardAndPoorConfigAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadStandardAndPoorConfigAction.class);

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

		StandardAndPoorConfigBean snpBean = new StandardAndPoorConfigBean();
		Properties props = new Properties();
		try (InputStream is = getClass().getResourceAsStream("/standard_poor_url.properties");) {
			log.info("Going to fetch properties from file standard_poor_url.properties");
			props.load(is);
			snpBean.setConfigurationLoadedFlag(true);
		} catch (Exception e) {
			log.error("Unable to read the standard_poor_url.properties.  Make sure it exists in your CLASSPATH", e);
			props = new Properties();
			snpBean.setConfigurationLoadedFlag(false);
		}
		snpBean.setUserConfigurationProperties(props);
		try {
			response.setContentType("text/xml");
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(snpBean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLoadStandardAndPoorConfigAction encountered exception. ", e);
			}
		} catch (Exception e) {
			String msg = "Unable to retrieve data.";
			log.error("QuoddLoadStandardAndPoorConfigAction.execute(): " + msg, e);
		}
		return null;
	}
}
