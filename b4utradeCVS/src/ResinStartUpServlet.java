import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.b4utrade.helper.TopTenEdgeNewsCache;
import com.b4utrade.servlet.B4UTServlet;
import com.b4utrade.stock.QuoteMessageHandler;
import com.tacpoint.jms.JMSMessageConsumer;
import com.tacpoint.util.Constants;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class ResinStartUpServlet extends B4UTServlet {

	static Thread t = null;

	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
			Environment.init();
			Logger.init();
			boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
			boolean local = Boolean.valueOf(Environment.get("LOCAL_QUOTE_SERVER")).booleanValue();
			if (local) {
				JMSMessageConsumer mc = new JMSMessageConsumer();
				mc.setMessageHandler(new QuoteMessageHandler());
				mc.setClientID(Environment.get("JMS_QUOTES_CLIENT_ID"));
				mc.setPrimaryIP(Environment.get("JMS_QUOTES_PRIMARY_IP"));
				mc.setSecondaryIP(Environment.get("JMS_QUOTES_SECONDARY_IP"));
				mc.setTopicName(Environment.get("JMS_QUOTES_TOPIC_NAME"));
				mc.setQOS(Environment.get("JMS_QUOTES_QOS"));
				mc.setConsumerName("Quote Data");
				t = new Thread(mc);
				t.setDaemon(true);
				t.start();
				Logger.log("ResinStartUpServerlet.init : JMS Consumer thread for equities started");
			}
			TopTenEdgeNewsCache.init();
		} catch (Exception e) {
			Logger.log("ResinStartUpServlet " + e.getMessage(), e);
		}
	}
}
