
import java.io.OutputStream;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.b4utrade.datainterfaceserver.QuoteAsXMLDataFormatter;
import com.b4utrade.helper.QuoteServerHelper;
import com.b4utrade.servlet.B4UTServlet;
import com.tacpoint.util.Constants;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoteAsXMLServlet extends B4UTServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void processRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
		try (OutputStream os = res.getOutputStream();) {
			String stocks = req.getParameter("COMPANYID");
			Hashtable hash = null;
			if (stocks != null)
				hash = QuoteServerHelper.getQuote(stocks);
			if (hash == null)
				hash = new Hashtable<>();
			QuoteAsXMLDataFormatter formatter = new QuoteAsXMLDataFormatter();
			byte[] bytes = formatter.formatStockQuote(hash);
			os.write(bytes);
		} catch (Exception e) {
			Logger.log("Exception in QuoteAsXMLServlet.processRequest " + e.getMessage());
		}
	}

	protected boolean doSaveSession() {
		return false;
	}

	@Override
	public String getServletInfo() {
		return "Copyright(c) 2000, Tacpoint Technologies Inc.";
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
