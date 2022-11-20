package com.b4utrade.web.action;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bo.StockMoverBO;
import com.b4utrade.helper.TopTenMoversCache;

public class ViewMoverShakerAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(ViewMoverShakerAction.class);

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
		log.info("ViewMoverShakerAction.execute() : Get Today's upgrades and downgrades data.");
		try {
			TopTenMoversCache.init();
		} catch (Exception e) {
			log.error("ViewMoverShakerAction.execute() - exception : unable to initialize TopTenMoversCache : ", e);
		}
		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		String targetType = request.getParameter("TARGET_TYPE");
		log.debug("ViewMoverShakerAction.execute() : Target Type is " + targetType);
		Vector<StockMoverBO> aUpMoverList = TopTenMoversCache.getUpMovers();
		if (aUpMoverList == null)
			aUpMoverList = new Vector<>();
		Vector<StockMoverBO> aDownMoverList = TopTenMoversCache.getDownMovers();
		if (aDownMoverList == null)
			aDownMoverList = new Vector<>();
		Vector<StockMoverBO> aVolumeMoverList = TopTenMoversCache.getVolumeMovers();
		if (aVolumeMoverList == null)
			aVolumeMoverList = new Vector<>();
		log.info("ViewMoverShakerAction.execute() : Size of UP MOVER data is " + aUpMoverList.size());
		log.info("ViewMoverShakerAction.execute() : Size of DOWN MOVER data is " + aDownMoverList.size());
		log.info("ViewMoverShakerAction.execute() : Size of VOLUME data is " + aVolumeMoverList.size());
		request.setAttribute("UPLIST", aUpMoverList);
		request.setAttribute("DOWNLIST", aDownMoverList);
		request.setAttribute("VOLUMELIST", aVolumeMoverList);
		if ("changedown".equals(targetType))
			return mapping.findForward("changeDown");
		if ("highestvolume".equals(targetType))
			return mapping.findForward("highestVolume");
		return mapping.findForward("changeUp");
	}
}
