import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.b4utrade.bean.NewsBean;
import com.b4utrade.helper.QuoteServerHelper;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.servlet.B4UTServlet;
import com.b4utrade.util.B4UConstants;
import com.tacpoint.util.Logger;
import com.tacpoint.util.Utility;

public class QuoteSysAlertAndEdgeNewsServlet extends B4UTServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void processRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		boolean edgeGeneralNews = false;
		boolean showSnapAndCompanyNews = false;
		String stocks = req.getParameter("COMPANYID");
		String action = req.getParameter("ACTION");
		String source = req.getParameter("SOURCE");
		if (action != null) {
			if (action.equals("GENERAL")) {
				edgeGeneralNews = true;
			} else if (action.equals("SNAP_COMPANY")) {
				showSnapAndCompanyNews = true;
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream os = res.getOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			Hashtable hash = new Hashtable<>();
			if (showSnapAndCompanyNews && stocks != null) {
				Hashtable snaps = QuoteServerHelper.getQuote(stocks);
				ArrayList<NewsBean> company = QuoteServerHelper.getEdgeLatestNewsByTickers(stocks, source);
				hash = mergeSnapAndNewsResults(snaps, company);
			} else if (stocks != null) {
				hash = QuoteServerHelper.getQuote(stocks);
			} else if (edgeGeneralNews) {
				hash = QuoteServerHelper.getGeneralNews(source);
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

	private Hashtable mergeSnapAndNewsResults(Hashtable snapHash, ArrayList<NewsBean> company) {
		Vector snapVector = new Vector();
		Vector companyNewsVector = new Vector();
		Iterator it = snapHash.values().iterator();
		while (it.hasNext()) {
			snapVector.addElement(it.next());
		}
		it = company.iterator();
		while (it.hasNext()) {
			NewsBean news = (NewsBean) it.next();
			StockNewsUpdateHelper item = new StockNewsUpdateHelper();
			item.setLastNewsID(news.getNewsID());
			item.setTicker(news.getTickers());
			item.setCategoriesAsString(news.getCategories());
			item.setLastNewsDate(com.tacpoint.util.Utility.getTimeAsPattern(Utility.DATE_PATTERN_DDMMMYYYYHHMISS,
					news.getNewsDate()));
			item.setLastNews(news.getHeadline());
			item.setLastNewsSource(news.getNewsSource());
			item.setDowJones(B4UConstants.NEWS_EDGE_CODE);
			companyNewsVector.addElement(item);
		}
		Logger.log("Company news vector size : " + companyNewsVector.size());
		Logger.log("Snap vector size : " + snapVector.size());
		Hashtable vhash = new Hashtable();
		vhash.put("SNAP_QUOTE", snapVector);
		vhash.put("COMPANY_NEWS", companyNewsVector);
		return vhash;
	}

	protected boolean doSaveSession() {
		return false;
	}

	@Override
	public String getServletInfo() {
		return "Copyright(c) Quodd";
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
