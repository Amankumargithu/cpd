package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.QuoddUserOperationStatusBean;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;

public class QuoddSaveUserPropertySheetAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddSaveUserPropertySheetAction.class);

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has
	 * already been completed.
	 *
	 * @param mapping The ActionMapping used to select this instance
	 * @param actionForm The optional ActionForm bean for this request (if any)
	 * @param request The HTTP request we are processing
	 * @param response The HTTP response we are creating
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 */
	public ActionForward execute(ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {

		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			response.setContentType("text/html");
			QuoddUserOperationStatusBean statusbean = new QuoddUserOperationStatusBean();
			try
			{
				statusbean.setOperationSuccessful();
			} catch (Exception e)
			{
				log.error("QuoddSaveUserPropertySheetAction.execute - has error. ", e);
				statusbean.setOperationFailed();
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArray);
			encoder.writeObject(statusbean);
			encoder.close();
			ServletOutputStream sos = null;
			try
			{
				sos = response.getOutputStream();
				sos.write(byteArray.toByteArray());
				sos.flush();
			} 
			catch (Exception e)
			{
				log.error("QuoddSaveUserPropertySheetAction.execute() : servlet output stream encountered exception. ", e);
			}
			finally
			{
				if (sos != null)
				{
					try
					{
						sos.close();
					} 
					catch (Exception e)
					{
					}
				}
			}         
		}
		catch(Exception e) {
			String msg = "Unable to retrieve data.";
			log.error("QuoddSaveUserPropertySheetAction.execute(): "+msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		return (null);
	}
}
