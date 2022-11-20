package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.EntitlementPropertyBean;
import com.b4utrade.bean.QuoddUserEntitlementBean;
import com.b4utrade.bean.UserEntitlementBean;
import com.b4utrade.bo.UserBO;
import com.b4utrade.web.rulehandler.B4UTradeWebRuleHandler;

public class QuoddCheckUserEntitlementAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddCheckUserEntitlementAction.class);

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
		QuoddUserEntitlementBean entitlementbean = null;
		String inputUserName = null;
		String userIdStr = null;
		try {
			HttpSession hs = request.getSession(false);
			inputUserName = request.getParameter("USERNAME");
			userIdStr = request.getParameter("USER_ID");
			int userId = 0;
			try {
				userId = Integer.parseInt(userIdStr);
			} catch (Exception e) {
				userId = 0;
			}
			entitlementbean = new QuoddUserEntitlementBean();
			entitlementbean.setMarketMakerEntitlementFlag(false);
			entitlementbean.setOptionEntitlementFlag(false);
			entitlementbean.setDowJonesNewsFlag(false);
			entitlementbean.setBlockTradeEntitlementFlag(false);
			entitlementbean.setVWAPEntitlementFlag(false);
			entitlementbean.setUsername(inputUserName);
			entitlementbean.setUserID(userId);
			String password = (String) hs.getAttribute("PASSWORD");
			if (password == null || password.isEmpty()) {
				password = (String) hs.getValue("PASSWORD");
			}
			getExchangeEntitlements(inputUserName, userId, password, entitlementbean);

			hs.setAttribute("USER_ENTITLEMENT", entitlementbean);
		} catch (Exception e) {
			log.error("Unable to lookup user entitlement information. ", e);
		}
		try {
			response.setContentType("text/html");
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArray);
			encoder.writeObject(entitlementbean);
			encoder.close();
			OutputStream os = response.getOutputStream();
			GZIPOutputStream gz = null;
			String encoding = request.getHeader("Accept-Encoding");
			try {
				// can the client handle gzip compression?
				if (encoding != null && encoding.indexOf("gzip") >= 0) {
					log.info("Compressing entitlements XML for user : " + inputUserName);
					response.setHeader("Content-Encoding", "gzip");
					gz = new GZIPOutputStream(os);
					gz.write(byteArray.toByteArray());
					gz.finish();
				} else {
					log.info("No compression being used for entitlements XML for user : " + inputUserName);
					os.write(byteArray.toByteArray());
				}
			} catch (Exception wex) {
				log.error("Unable to write entitlements XML for user : " + inputUserName, wex);
			} finally {
				// clean up output streams
				try {
					if (os != null)
						os.close();
				} catch (Exception osEx) {
				}
				try {
					if (gz != null) {
						gz.close();
					}
				} catch (Exception gzEx) {
				}
			}
		} catch (Exception e) {
			String msg = "Unable to retrieve and write entitlements data for user " + inputUserName;
			log.error("QuoddCheckUserEntitlementAction.execute(): " + msg, e);
			request.setAttribute(B4UTradeWebRuleHandler.getApplicationErrorMsg(), msg);
			return (mapping.findForward(FAILURE));
		}
		return (null);
	}

	private void getExchangeEntitlements(String username, int userId, String password,
			QuoddUserEntitlementBean entitlementbean) {
		try {
			UserBO user = new UserBO();
			user.setUserName(username);
			user.setUserPassword(password);
			if (password == null || password.isEmpty()) {
				user.setNoAuthNeeded(true);
			}
			user.setUserID(userId);
			Vector entitlementV = null;
			if (username == null || username.isEmpty())
				entitlementV = user.selectUserEntitlementsByID();
			else
				entitlementV = user.selectUserEntitlements();
			if (entitlementV == null || entitlementV.size() == 0)
				return;
			Hashtable ht = entitlementbean.getExchangeHash();
			if (ht == null) {
				ht = new Hashtable();
				entitlementbean.setExchangeHash(ht);
			}
			for (int i = 0; i < entitlementV.size(); i++) {
				Object bean = entitlementV.elementAt(i);
				if (bean instanceof QuoddUserEntitlementBean) { // adding the flags that were being set using a
																// standalone db call from USERBO selectAllAccessLevel()
					QuoddUserEntitlementBean accessLevelBean = (QuoddUserEntitlementBean) bean;
					entitlementbean.setMarketMakerEntitlementFlag(accessLevelBean.getMarketMakerEntitlementFlag());
					entitlementbean.setBlockTradeEntitlementFlag(accessLevelBean.getBlockTradeEntitlementFlag());
					entitlementbean.setDowJonesNewsFlag(accessLevelBean.getDowJonesNewsFlag());
					entitlementbean.setOptionEntitlementFlag(accessLevelBean.getOptionEntitlementFlag());
					entitlementbean.setVWAPEntitlementFlag(accessLevelBean.getVWAPEntitlementFlag());
				} else {
					UserEntitlementBean uebean = (UserEntitlementBean) bean;
					if (uebean == null)
						continue;
					if (uebean.getExchangeID() == null || uebean.getExchangeID().length() == 0)
						continue;
					entitlementbean.setUserID(uebean.getUserID());
					Hashtable exchangeHash = (Hashtable) ht.get(uebean.getExchangeID());
					if (exchangeHash == null) {
						exchangeHash = new Hashtable();
						ht.put(uebean.getExchangeID(), exchangeHash);
					}
					Vector propV = uebean.getEntitlementVector();
					if (propV == null || propV.size() == 0)
						continue;
					for (int j = 0; j < propV.size(); j++) {
						EntitlementPropertyBean propbean = (EntitlementPropertyBean) propV.elementAt(j);
						if (propbean == null)
							continue;
						// skip exchange property code
						if (uebean.getExchangeID().equals(propbean.getPropertyCode()))
							continue;
						exchangeHash.put(propbean.getPropertyCode(), propbean.getPropertyCode());
					}
				}
			}
			if (entitlementbean.getUserID() == 0) {
				entitlementbean.setUserID(user.getUserID());
			}
			if (entitlementbean.getUsername() == null) {
				entitlementbean.setUsername(user.getUserName());
			}
		} catch (Exception dne) {
			String msg = "Exception encountered while attempting to retrieve exchange entitlements for user ["
					+ username + "]. " + dne.getMessage();
			log.error("QuoddCheckUserEntitlementAction - " + msg, dne);
			entitlementbean.setDisableAllExchanges(true);
		}
	}
}
