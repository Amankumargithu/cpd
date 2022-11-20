package ntp.ejb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.optionsregional.ejb.OptionsRegionalData;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.optionsregional.cache.OptionsRegionalQTMessageQueue;
import ntp.optionsregional.snap.OptionsRegionalSnapController;
import ntp.optionsregional.streamer.OptionsRegionalStreamingController;
import ntp.util.DateTimeUtility;
import ntp.util.OptionsRegionalExchangesPopulator;
import ntp.util.OptionsRegionalUtility;
import QuoddFeed.msg.Image;


@Stateless(mappedName="ejbCache/OptionsRegionalData")
@Remote(OptionsRegionalData.class)
public class OptionsRegionalEJB implements OptionsRegionalData{

	private OptionsRegionalUtility utility = OptionsRegionalUtility.getInstance();

	public LinkedList getOptionsRegionalQuote(String optionRegionalTickers){
		NTPLogger.requestEJB("OpRegSp");
		long s = System.currentTimeMillis();
		LinkedList<byte[]> stocks = new LinkedList<byte[]>();
		OptionsRegionalQTMessageQueue cache = OptionsRegionalQTMessageQueue.getInstance();
		try
		{
			HashSet<String> resubscribeSet = new HashSet<String>();
			String [] tickerArray = optionRegionalTickers.split(",");
			for (int j = 0; j < tickerArray.length; j++) {
				String opRegTicker = tickerArray[j];
				if(!utility.validateOptionRegionalSymbol(opRegTicker))
				{
					NTPLogger.dropSymbol(opRegTicker, "Invalid Option Regional Symbol");
					continue;
				}
				if(!cache.isIncorrectSymbol(opRegTicker))
				{
					QTCPDMessageBean bean = cache.getSubsData().get(utility.getEQPlusFormattedTicker(opRegTicker));
					if(bean != null)
						stocks.add(bean.toByteArray());
					else
						resubscribeSet.add(utility.getUCFormattedTicker(opRegTicker));
				}
			}
			OptionsRegionalStreamingController.getInstance().addRequestedSymbols(resubscribeSet);
			if(resubscribeSet.size() >  0)
			{  
				HashSet<String> dupSet = new HashSet<String>();
				dupSet.addAll(resubscribeSet);
				Map<String,Image> images = OptionsRegionalSnapController.getInstance().getSyncSnapData(resubscribeSet,false);
				Map<String, Image> preImages = OptionsRegionalSnapController.getInstance().getSyncSnapData(dupSet, true);
				Set<String> tickers = images.keySet();
				for(String ticker: tickers)
				{

					QTCPDMessageBean qtMessageBean = new QTCPDMessageBean();
					Image image = images.get(ticker);
					Image preImage = preImages.get(ticker);
					qtMessageBean.setTicker(ticker);
					qtMessageBean.setSystemTicker(ticker);
					if(preImage != null)
					{
						qtMessageBean.setExtendedLastPrice(preImage._trdPrc);
						qtMessageBean.setExtendedLastTradeVolume(preImage._trdVol);
						qtMessageBean.setExtendedChangePrice(preImage._netChg);
						qtMessageBean.setExtendedPercentChange(preImage._pctChg);
						DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, preImage._trdTime);
						qtMessageBean.setExtendedTickUpDown(preImage._prcTck);
					}
					if(image != null)
					{
						int quoteIndex = ticker.indexOf("\"");
						if(quoteIndex !=-1)
							ticker=ticker.substring(0,quoteIndex);
						ticker = utility.getEQPlusFormattedTicker(ticker);
						qtMessageBean.setSystemTicker(ticker);
						qtMessageBean.setLastPrice(image._trdPrc);
						qtMessageBean.setLastTradeVolume(image._trdVol);
//						qtMessageBean.setVolume(image._acVol);
						qtMessageBean.setTicker(ticker);
						qtMessageBean.setDayHigh(image._high);
						qtMessageBean.setDayLow(image._low);
//						qtMessageBean.setAskPrice(image._ask);
//						qtMessageBean.setAskSize(image._askSize);
//						qtMessageBean.setBidPrice(image._bid);
//						qtMessageBean.setBidSize(image._bidSize);
						qtMessageBean.setChangePrice(image._netChg);
						qtMessageBean.setPercentChange(image._pctChg);
						qtMessageBean.setOpenPrice(image._open);
//						qtMessageBean.setBidExchangeCode(image._iBidMktCtr + "");
//						qtMessageBean.setAskExchangeCode(image._iAskMktCtr+ "");
						DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
						if(preImage == null)
						{
							NTPLogger.info("No pre image for " + ticker);
							qtMessageBean.setVolume(image._acVol);
							qtMessageBean.setAskPrice(image._ask);
							qtMessageBean.setAskSize(image._askSize);
							qtMessageBean.setBidPrice(image._bid);
							qtMessageBean.setBidSize(image._bidSize);
							qtMessageBean.setExchangeId("" + image.protocol());
							qtMessageBean.setAskExchangeCode(image._iAskMktCtr+ "");
							qtMessageBean.setBidExchangeCode(image._iBidMktCtr + "");		
						}
						else
						{
							qtMessageBean.setVolume(image._acVol + preImage._acVol);
							if( image._ask == 0 &&  image._askSize == 0) 
							{
								qtMessageBean.setAskPrice(preImage._ask);
								qtMessageBean.setAskSize(preImage._askSize);
								qtMessageBean.setAskExchangeCode(image._iAskMktCtr+ "");
								qtMessageBean.setAskTime(preImage._askTime);								
							}
							else
							{
								qtMessageBean.setAskPrice(image._ask);
								qtMessageBean.setAskSize(image._askSize);
								qtMessageBean.setAskExchangeCode(image._iAskMktCtr+ "");
								qtMessageBean.setAskTime(image._askTime);
							}
							if(image._bid == 0 && image._bidSize == 0 )
							{
								qtMessageBean.setBidPrice(preImage._bid);
								qtMessageBean.setBidSize(preImage._bidSize);
								qtMessageBean.setBidExchangeCode(preImage._iBidMktCtr + "");
								qtMessageBean.setBidTime(preImage._bidTime);
							}
							else
							{
								qtMessageBean.setBidPrice(image._bid);
								qtMessageBean.setBidSize(image._bidSize);
								qtMessageBean.setBidExchangeCode(image._iBidMktCtr + "");
								qtMessageBean.setBidTime(image._bidTime);
							}
						}
					}
					else
					{
							NTPLogger.info("No image for " + ticker);
							qtMessageBean.setVolume(preImage._acVol);
							qtMessageBean.setAskPrice(preImage._ask);
							qtMessageBean.setAskSize(preImage._askSize);
							qtMessageBean.setBidPrice(preImage._bid);
							qtMessageBean.setBidSize(preImage._bidSize);
							qtMessageBean.setExchangeId("" + preImage.protocol());
							qtMessageBean.setAskExchangeCode(preImage._iAskMktCtr+ "");
							qtMessageBean.setBidExchangeCode(preImage._iBidMktCtr + "");	
					}
					stocks.add(qtMessageBean.toByteArray());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("OpSp", t-s);
		return stocks;
	}

	public void subscribeOptionsRegionalSymbol(String id, Object[] keys){
		for (Object object : keys) {
			OptionsRegionalStreamingController.getInstance().subscribeSymbol((String)object);
		}
	}

	public HashMap getOptionsRegionalExchanges(){
		NTPLogger.requestEJB("OpRegExList");
		NTPLogger.responseEJB("OpRegExList", 0);
		return OptionsRegionalExchangesPopulator.getDefaultInstance().getOptionsRegionalExchanges();		
	}
}
