package ntp.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.bean.QTCPDMessageBean;
import ntp.futures.cache.FuturesMappingSymbolsCache;
import ntp.futures.cache.FuturesQTMessageQueue;
import ntp.futures.snap.FuturesSnapController;
import ntp.futures.streamer.FuturesStreamingController;
import ntp.logger.NTPLogger;
import ntp.util.DateTimeUtility;
import QuoddFeed.msg.Image;

import com.b4utrade.bean.InterestRateBean;
import com.b4utrade.bean.StockOptionBean;
import com.b4utrade.ejb.OptionData;
@Remote(OptionData.class)
@Stateless(mappedName="ejbCache/OptionData")
public class FuturesSnapEJB implements OptionData{

	@Override
	public HashMap getOptionChain(String ticker) {
		//Not applicable - should be deleted
		return null;
	}

	@Override
	public String updateFutureMapping(String UITicker, String oldMappedTicker, String mappedTicker) {
		return null;
//		return FuturesMappingSymbolsCache.getInstance().updateFutureMapping(UITicker, oldMappedTicker, mappedTicker);
	}
	
	@Override
	public ArrayList getFutureChainByDescription(String ticker, int pagingIndex, boolean filterOn) {
		return (FuturesMappingSymbolsCache.getInstance().getFutureChainByDescription(ticker,pagingIndex,filterOn));
	}

	@Override
	public ArrayList getFutureChainByBaseSymbol(String ticker) {
		return (FuturesMappingSymbolsCache.getInstance().getFutureChainByBaseSymbol(ticker));
	}

	@Override
	public StockOptionBean getStockOption(String optionTicker) {
		NTPLogger.requestEJB("FutFun");
		long s = System.currentTimeMillis();
		StockOptionBean bean = FuturesMappingSymbolsCache.getInstance().getFundamentalData(optionTicker);
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("FutFun", t-s);
		return bean;
	}

	@Override
	public HashMap getExpirationChain(String ticker) {
		//Not applicable - should be deleted
		return null;
	}

	@Override
	public InterestRateBean getInterestRate() {
		//Not applicable - should be deleted
		return null;
	}

	@Override
	public LinkedList getOptionQuote(String optionTickers) {

		NTPLogger.requestEJB("FutSp");
		long s = System.currentTimeMillis();
		LinkedList<byte[]> stocks = new LinkedList<byte[]>();
		FuturesQTMessageQueue cache = FuturesQTMessageQueue.getInstance();
		try
		{
			HashSet<String> resubscribeSet = new HashSet<String>();
			String [] tickerArray = optionTickers.split(",");
			for (int j = 0; j < tickerArray.length; j++) {
				String ticker = tickerArray[j];
				if(!cache.isIncorrectSymbol(ticker))
				{
					QTCPDMessageBean bean = cache.getSubsData().get(ticker);
					if(bean != null)
						stocks.add(bean.toByteArray());
					else
						resubscribeSet.add(ticker);
				}
			}
			FuturesStreamingController.getInstance().addRequestedSymbols(resubscribeSet);
			if(resubscribeSet.size() >  0)
			{
				LinkedList<Image> images = FuturesSnapController.getInstance().getSyncSnapData(resubscribeSet);
				for(Image image: images)
				{
					QTCPDMessageBean qtMessageBean = new QTCPDMessageBean();
					String ticker = image.tkr();
					int quoteIndex = ticker.indexOf("\"");
					if(quoteIndex !=-1)
						ticker=ticker.substring(0,quoteIndex);
					qtMessageBean.setTicker(ticker);
					qtMessageBean.setSystemTicker(ticker);
					if(image._trdPrc > 0.0)
						qtMessageBean.setLastPrice(image._trdPrc);
					else
					{
						image._trdPrc = image._close;
						qtMessageBean.setLastPrice(image._close);
					}
					qtMessageBean.setLastClosedPrice(image._close);
					qtMessageBean.setLastTradeVolume(image._trdVol);
					qtMessageBean.setVolume(image._acVol);
					qtMessageBean.setDayHigh(image._high);
					qtMessageBean.setDayLow(image._low);
					qtMessageBean.setAskPrice(image._ask);
					qtMessageBean.setAskSize(image._askSize);
					qtMessageBean.setBidPrice(image._bid);
					qtMessageBean.setBidSize(image._bidSize);
					qtMessageBean.setChangePrice(image._netChg);
					qtMessageBean.setPercentChange(image._pctChg);
					qtMessageBean.setOpenPrice(image._open);
					qtMessageBean.setOpenPriceRange1(image._open);
					qtMessageBean.setBidExchangeCode(image._iBidMktCtr + "");
					qtMessageBean.setAskExchangeCode(image._iAskMktCtr+ "");
					qtMessageBean.setSettlementPrice(image._settlePrc);
					qtMessageBean.setExchangeId("" + image.protocol());
					System.out.println(ticker + " " + image.protocol());
					DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
					qtMessageBean.setTickUpDown(image._prcTck);
					stocks.add(qtMessageBean.toByteArray());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("FutSp", t-s);
		return stocks;
	}

	@Override
	public HashMap getSpotSymbolMap() {
		return FuturesMappingSymbolsCache.getInstance().getUiFutureSymbolMap();
	}

	@Override
	public HashMap getTSQOptionChain(String rootTicker) {
		// Not applicable - should be deleted
		return null;
	}
	
	@Override
	public void subscribe(String id, Object[] keys) {
		NTPLogger.requestEJB("FutSub");
		long s = System.currentTimeMillis();
		HashSet<String> set = new HashSet<>();
		for(Object o : keys)
			set.add((String)o);
		FuturesStreamingController.getInstance().addRequestedSymbols(set);
		long t =System.currentTimeMillis();
		NTPLogger.responseEJB("FutSub", t-s);
	}
}
