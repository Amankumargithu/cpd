package com.b4utrade.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.cache.SymbolUpdaterCache;

public class ReloadSymbolUpdaterCacheAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(ReloadSymbolUpdaterCacheAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {

		doCheckUser = false;
		doCheckReferalPartner = false;

		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			response.setContentType("text/html");
			int cacheType = Integer.parseInt(request.getParameter("CACHETYPE"));
			System.out.println(new Timestamp(System.currentTimeMillis()) + " Cachetype to reload is "+cacheType);
			SymbolUpdaterCache.getInstance().reloadCache(cacheType);
			try (PrintWriter sos = response.getWriter();) {
				sos.write("Reloaded Successfully");
				sos.flush();
			} catch (Exception e) {
				log.error("RTDTickerValidationAndSnapAction encountered exception. ", e);
			} 
		} catch (Exception e) {
			String msg = "RTDReloadCacheAction.execute() : Unable to reload cache data.";
			log.error(msg, e);
		}
		return (null);
	}
}
