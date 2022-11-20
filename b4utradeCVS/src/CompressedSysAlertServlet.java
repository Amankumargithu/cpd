import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.b4utrade.helper.QuoteServerHelper;
import com.b4utrade.servlet.B4UTServlet;
import com.tacpoint.util.Logger;

public class CompressedSysAlertServlet extends B4UTServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void processRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		int userid = -1;
		boolean checkAlerts = false;
		try {
			userid = Integer.parseInt(req.getParameter("AUID"));
			if (userid > 0)
				checkAlerts = true;
		} catch (NumberFormatException nfe) {
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream os = res.getOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			Hashtable hash = new Hashtable<>();
			if (checkAlerts) {
				String userAlerts = QuoteServerHelper.getUserAlertsJson(Integer.toString(userid));
				if (userAlerts != null && !userAlerts.isEmpty())
					hash.put("ALERTS", userAlerts);
			}
			oos.writeObject(hash);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			String encoding = req.getHeader("Accept-Encoding");
			// can the client handle gzip compression?
			if (encoding != null && encoding.indexOf("gzip") >= 0) {
				try (GZIPOutputStream gz = new GZIPOutputStream(os);) {
					res.setHeader("Content-Encoding", "gzip");
					gz.write(bytes);
					gz.finish();
				}
			} else {
				os.write(bytes);
			}
		} catch (Exception e) {
			Logger.log("Exception in CompressedQuoteSysAlertAndNewsServlet.processRequest " + e.getMessage());
		}
	}

	protected boolean doSaveSession() {
		return false;
	}

	@Override
	public String getServletInfo() {
		return "Copyright(c) 2014, Quodd";
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
