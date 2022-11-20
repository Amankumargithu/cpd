package com.b4utrade.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tacpoint.exception.BusinessException;
import com.tacpoint.exception.SessionExpiredException;
import com.tacpoint.exception.SessionExpiredNoForwardException;
import com.tacpoint.servlet.DefaultServlet;
import com.tacpoint.util.Constants;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class B4UTServlet extends DefaultServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
		try {
			processRequest(req, res);
		} catch (SessionExpiredNoForwardException senfe) {
			Logger.debug("B4UTServlet.handleRequest - Session has expired", senfe, doLog);
		} catch (SessionExpiredException see) {
			Logger.debug("B4UTServlet.handleRequest - Session has expired.", see, doLog);
		} catch (BusinessException be) {
			Logger.debug("B4UTServlet.handleRequest - Business exception encountered.", be, doLog);
		} catch (Exception e) {
			Logger.debug("B4UTServlet.handleRequest - Exception encountered.", e, doLog);
		}
	}

	public void processRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		return;
	}

	public void destroy() {
		super.destroy();
	}
}
