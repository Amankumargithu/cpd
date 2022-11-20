package ntp.ejb;

import java.util.HashMap;
import java.util.LinkedList;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.bean.QTCPDMessageBean;
import ntp.equityregional.snap.EquityRegionalSnapController;
import ntp.equityregional.streamer.EquityRegionalStreamingController;
import ntp.logger.NTPLogger;
import ntp.util.DateTimeUtility;
import ntp.util.EquityRegionalExchangesPopulator;
import ntp.util.EquityRegionalUtility;
import QuoddFeed.msg.Image;

import com.equitymontage.ejb.EquityMontageData;

@Stateless(mappedName="ejbCache/EquityMontageData")
@Remote(EquityMontageData.class)
public class EquityRegionalEJB implements EquityMontageData {

	public LinkedList getEquityMontageQuote(String equityMontageTickers){
		NTPLogger.requestEJB("EqSnap");
		long s = System.currentTimeMillis();
		LinkedList<Image> list =  EquityRegionalSnapController.getInstance().getSyncSnapData(equityMontageTickers);
		LinkedList<byte[]> stocks = new LinkedList<byte[]>();
		EquityRegionalUtility utility = EquityRegionalUtility.getInstance();
		for(Image image : list)
		{
			QTCPDMessageBean qtMessageBean = new QTCPDMessageBean();
			String ticker = image.tkr();
			int quoteIndex = ticker.indexOf("\"");
			if(quoteIndex !=-1)
				ticker=ticker.substring(0,quoteIndex);
//			System.out.println("SNAP - ticker " + ticker);
			qtMessageBean.setSystemTicker(ticker);
			qtMessageBean.setLastPrice(image._trdPrc);
			qtMessageBean.setChangePrice(image._netChg);
			qtMessageBean.setPercentChange(image._pctChg);
			qtMessageBean.setLastTradeVolume(image._trdVol);
			qtMessageBean.setVolume(image._acVol);
			qtMessageBean.setTicker(ticker);
			qtMessageBean.setDayHigh(image._high);
			qtMessageBean.setDayLow(image._low);
			qtMessageBean.setOpenPrice(image._open);
			qtMessageBean.setAskPrice(image._ask);
			qtMessageBean.setAskSize(image._askSize);
			qtMessageBean.setBidPrice(image._bid);
			qtMessageBean.setBidSize(image._bidSize);
			qtMessageBean.setOpenPrice(image._open);
			qtMessageBean.setOpenPrice(image._open);
			String marketCenter = image._priMktCtr;
			qtMessageBean.setLastClosedPrice(image._close);
			qtMessageBean.setExtendedLastPrice(image._trdPrc_ext);
			qtMessageBean.setExtendedChangePrice(image._netChg_ext);
			qtMessageBean.setExtendedPercentChange(image._pctChg_ext);
			qtMessageBean.setExtendedLastTradeVolume(image._trdVol_ext);
			DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, image._trdTime_ext);
			qtMessageBean.setExtendedMarketCenter(utility.getEquityPlusExchangeCode(image._trdMktCtr_ext));
			qtMessageBean.setMarketCenter(utility.getEquityPlusExchangeCode(marketCenter));
			qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(marketCenter));
			qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(marketCenter));
			DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
			stocks.add(qtMessageBean.toByteArray());
		}
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("EqSnap", t-s);
		return stocks;
	}

	public void subscribeEquityMontageSymbol(String id, Object[] keys){
		NTPLogger.requestEJB("EqSub");
		long s = System.currentTimeMillis();
		for (Object object : keys) {
			EquityRegionalStreamingController.getInstance().addRequestedSymbol((String)object);
		}
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("EqSub", t-s);
	}

	public HashMap getEquityMontageExchanges(String rootSymbol){
		NTPLogger.requestEJB("EqRegExList");
		NTPLogger.responseEJB("EqRegExList", 0);
		return EquityRegionalExchangesPopulator.getDefaultInstance().getOptionsRegionalExchanges();
	}	
}
