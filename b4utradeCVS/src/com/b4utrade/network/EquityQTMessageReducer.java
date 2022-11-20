package com.b4utrade.network;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.b4utrade.bean.QTMessageBean;
import com.tacpoint.common.DefaultObject;
import com.tacpoint.jms.MessageReducer;
import com.tacpoint.util.SystemOutLogger;

/**
 * 
 * @author Ankit
 *
 */
public class EquityQTMessageReducer extends DefaultObject implements MessageReducer {

	private HashMap<String, QTMessageBean> compactionCache = new HashMap<String, QTMessageBean>();

	@Override
	public HashMap parseKeyValues(ByteArrayOutputStream baos) {
		HashMap map = new HashMap();

		StringTokenizer st = new StringTokenizer(new String(baos.toByteArray()),QTMessageKeys.TUPLE_SEP);
		while (st.hasMoreTokens()) {
			String tuple = st.nextToken();
			int index = tuple.indexOf(QTMessageKeys.FIELD_SEP);
			map.put(tuple.substring(0,index),tuple.substring(index+1));
		}
		return map;
	}

	@Override
	public void conflateReducedMessages(ByteArrayOutputStream baos,
			Map existing, Map additions) {

		Iterator it = additions.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			existing.put(key,additions.get(key));
		}

		try {
			baos.reset();

			baos.write(QTMessageKeys.TICKER.getBytes());
			baos.write(QTMessageKeys.FIELD_SEP.getBytes());
			baos.write(((String)existing.get(QTMessageKeys.TICKER)).getBytes());

			existing.remove(QTMessageKeys.TICKER);

			it = existing.keySet().iterator();

			while (it.hasNext()) {
				String key   = (String)it.next();
				String value = (String)existing.get(key);
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(key.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(value.getBytes());
			}

			baos.write(com.tacpoint.publisher.TConstants.TERMINATOR_BYTE);
		}
		catch (Exception e) {
			SystemOutLogger.error("Unable to conflate messages - "+e.getMessage());
			e.printStackTrace();
			baos = null;
		}
	}

	@Override
	public ByteArrayOutputStream reduceMessage(String ticker, byte[] message) {
		if (compactionCache == null) {
			SystemOutLogger.debug("Compaction cache map is null.");
			return null;
		}
		QTMessageBean qtBean = compactionCache.get(ticker);
		return buildCompactedMessage(ticker, message, qtBean);  
	}

	private ByteArrayOutputStream buildCompactedMessage(String ticker, byte[] message, QTMessageBean prevMessage) {
		try{
			boolean doSend = false;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			QTMessageBean qtBean = new QTMessageBean();
			changeQTStreamToQTMessageBean(qtBean, new String(message));
			if (prevMessage == null) {
				doSend = true;
			}
			baos.write(QTMessageKeys.TICKER.getBytes());
			baos.write(QTMessageKeys.FIELD_SEP.getBytes());
			baos.write(ticker.getBytes());
			if ( prevMessage == null || (prevMessage.getBidSize() != qtBean.getBidSize())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.BID_SIZE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getBidSize()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getAskSize() != qtBean.getAskSize())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.ASK_SIZE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getAskSize()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getAskPrice() != qtBean.getAskPrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.ASK_PRICE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getAskPrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getBidPrice() != qtBean.getBidPrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.BID_PRICE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getBidPrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || !prevMessage.getAskExchangeCode().equals(qtBean.getAskExchangeCode())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.ASK_EXCHANGE_CODE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(qtBean.getAskExchangeCode().getBytes());
				doSend = true;
			}
			if ( prevMessage == null || !prevMessage.getBidExchangeCode().equals(qtBean.getBidExchangeCode())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.BID_EXCHANGE_CODE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(qtBean.getBidExchangeCode().getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getDayLow() != qtBean.getDayLow())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.DAY_LOW.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getDayLow()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getDayHigh() != qtBean.getDayHigh())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.DAY_HIGH.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getDayHigh()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getChangePrice() != qtBean.getChangePrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.CHANGE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getChangePrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getPercentChange() != qtBean.getPercentChange())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.PERCENT_CHANGE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getPercentChange()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getLastPrice() != qtBean.getLastPrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.LAST.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getLastPrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getVolume() != qtBean.getVolume())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.VOLUME.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getVolume()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getLastTradeVolume() != qtBean.getLastTradeVolume())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.TRADE_VOLUME.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getLastTradeVolume()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getOpenPrice() != qtBean.getOpenPrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.OPEN_PRICE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getOpenPrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || !prevMessage.getMarketCenter().equals(qtBean.getMarketCenter())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.MARKET_CENTER_CODE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(qtBean.getMarketCenter().getBytes());
				doSend = true;
			}
			if ( prevMessage == null || !prevMessage.getVWAP().equals(qtBean.getVWAP())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.VWAP_CODE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(qtBean.getVWAP().getBytes());
				doSend = true;
			}
			if ( prevMessage == null || !prevMessage.getTradeDate().equals(qtBean.getTradeDate())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.TRADE_DATE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(qtBean.getTradeDate().getBytes());
				doSend = true;
			}
			if ( prevMessage == null || !prevMessage.getLastTradeTime().equals(qtBean.getLastTradeTime())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.TRADE_TIME.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(qtBean.getLastTradeTime().getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getLastClosedPrice() != qtBean.getLastClosedPrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.LAST_CLOSED_PRICE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getLastClosedPrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getSettlementPrice() != qtBean.getSettlementPrice())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.SETTLEMENT_PRICE.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(String.valueOf(qtBean.getSettlementPrice()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.getTickFlag() != qtBean.getTickFlag())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.SHORT_SALE_RESTRICTION_IND.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(Boolean.toString(qtBean.getTickFlag()).getBytes());
				doSend = true;
			}
			if ( prevMessage == null || (prevMessage.isUnabridged() != qtBean.isUnabridged())) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.HALT_IND.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				baos.write(Boolean.toString(qtBean.isUnabridged()).getBytes());
				doSend = true;
			}
			//Limit up down
			if ( prevMessage == null || (qtBean.getLimitUpDown() != null && prevMessage.getLimitUpDown() != null && !prevMessage.getLimitUpDown().equals(qtBean.getLimitUpDown()))) {
				baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
				baos.write(QTMessageKeys.LIMIT_UP_DOWN.getBytes());
				baos.write(QTMessageKeys.FIELD_SEP.getBytes());
				if (qtBean.getLimitUpDown() != null)
				{
					baos.write(qtBean.getLimitUpDown().getBytes());
				}
				else
				{
					baos.write(" , ".getBytes());
				}
				doSend = true;
			}

			try
			{
				if ( prevMessage == null || (prevMessage.getExtendedLastPrice() != qtBean.getExtendedLastPrice())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.LAST_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(String.valueOf(qtBean.getExtendedLastPrice()).getBytes());
					doSend = true;
				}
				if ( prevMessage == null || (prevMessage.getExtendedChangePrice() != qtBean.getExtendedChangePrice())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.CHANGE_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(String.valueOf(qtBean.getExtendedChangePrice()).getBytes());
					doSend = true;
				}
				if ( prevMessage == null || (prevMessage.getExtendedPercentChange() != qtBean.getExtendedPercentChange())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.PERCENT_CHANGE_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(String.valueOf(qtBean.getExtendedPercentChange()).getBytes());
					doSend = true;
				}
				if ( prevMessage == null || !prevMessage.getExtendedTradeDate().equals(qtBean.getExtendedTradeDate())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.TRADE_DATE_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(qtBean.getExtendedTradeDate().getBytes());
					doSend = true;
				}
				if ( prevMessage == null || !prevMessage.getExtendedLastTradeTime().equals(qtBean.getExtendedLastTradeTime())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.TRADE_TIME_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(qtBean.getExtendedLastTradeTime().getBytes());
					doSend = true;
				}
				if ( prevMessage == null || !prevMessage.getExtendedMarketCenter().equals(qtBean.getExtendedMarketCenter())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.MARKET_CENTER_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(qtBean.getExtendedMarketCenter().getBytes());
					doSend = true;
				}
				if ( prevMessage == null || (prevMessage.getExtendedLastTradeVolume() != qtBean.getExtendedLastTradeVolume())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.TRADE_VOLUME_EXTENDED.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(String.valueOf(qtBean.getExtendedLastTradeVolume()).getBytes());
					doSend = true;
				}
			}
			catch(Exception ex){}
			try
			{
				if ( prevMessage == null || (prevMessage.getVolumePlus() != qtBean.getVolumePlus())) {
					baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
					baos.write(QTMessageKeys.VOLUME_PLUS.getBytes());
					baos.write(QTMessageKeys.FIELD_SEP.getBytes());
					baos.write(String.valueOf(qtBean.getVolumePlus()).getBytes());
					doSend = true;
				}
			}
			catch(Exception ex){}
			compactionCache.put(ticker,qtBean);

			if (doSend)  
				return baos;
			else
				return null;

		}
		catch(Exception e)
		{
			SystemOutLogger.error("Unable to build compaction string.");
			e.printStackTrace();
			return null;
		}
	}

	private void changeQTStreamToQTMessageBean(QTMessageBean qtbean, String beanstr)
	{
		try
		{
			//	    	System.out.println("Input Message is - " + beanstr);
			StringTokenizer st = new StringTokenizer(beanstr, "||");
			st.nextToken();
			qtbean.setTicker((st.nextToken()).trim());
			//	        qtbean.setSystemTicker((st.nextToken()).trim());
			qtbean.setLastPrice(Double.parseDouble(st.nextToken()));
			qtbean.setOpenPrice(Double.parseDouble(st.nextToken()));
			qtbean.setPercentChange(Double.parseDouble(st.nextToken().trim()));
			qtbean.setChangePrice(Double.parseDouble(st.nextToken()));
			qtbean.setDayHigh(Double.parseDouble(st.nextToken()));
			qtbean.setDayLow(Double.parseDouble(st.nextToken()));
			qtbean.setBidSize(Long.parseLong(st.nextToken().trim()));
			qtbean.setAskSize(Long.parseLong(st.nextToken().trim()));
			qtbean.setVolume(Long.parseLong(st.nextToken().trim()));
			qtbean.setLastTradeVolume(Long.parseLong(st.nextToken().trim()));
			qtbean.setBidPrice(Double.parseDouble(st.nextToken()));
			qtbean.setAskPrice(Double.parseDouble(st.nextToken()));
			// skip messageType
			st.nextToken();	        
			qtbean.setLastClosedPrice(Double.parseDouble(st.nextToken()));
			//skip lastTradeYear
			qtbean.setExtendedLastPrice(Double.parseDouble(st.nextToken()));
			//skip lastTradeMonth
			qtbean.setExtendedPercentChange(Double.parseDouble(st.nextToken().trim()));
			//skip lastTradeDay
			qtbean.setExtendedChangePrice(Double.parseDouble(st.nextToken()));
			//skip lastTradeHour
			qtbean.setExtendedLastTradeVolume(Long.parseLong(st.nextToken().trim()));
			//skip lastTradeMinute
			qtbean.setExtendedMarketCenter(st.nextToken());
			//skip lastTradeSecond
			String flag = st.nextToken().trim();
			if (flag.equals("T"))
				qtbean.setExtendedUpDownTick(true);
			else
				qtbean.setExtendedUpDownTick(false);
			flag = st.nextToken().trim();
			if (flag.equals("T"))
				qtbean.setTickFlag(true);
			else
				qtbean.setTickFlag(false);
			qtbean.setOpenPriceRange1(Double.parseDouble(st.nextToken()));
			qtbean.setOpenPriceRange2(Double.parseDouble(st.nextToken()));
			qtbean.setLastClosedPriceRange1(Double.parseDouble(st.nextToken()));
			qtbean.setLastClosedPriceRange2(Double.parseDouble(st.nextToken()));	        
			qtbean.setTradeDate(st.nextToken());
			qtbean.setLastTradeTime(st.nextToken());
			qtbean.setExchangeCode(st.nextToken());
			qtbean.setAskExchangeCode(st.nextToken());
			qtbean.setBidExchangeCode(st.nextToken());
			qtbean.setMarketCenter(st.nextToken());
			qtbean.setVWAP(st.nextToken());	        
			qtbean.setExchangeId(st.nextToken());
			qtbean.setSettlementPrice(Double.parseDouble(st.nextToken()));
			if(st.hasMoreTokens())
			{
				flag = st.nextToken().trim();
				if (flag.equals("T"))
					qtbean.setUnabridged(true);
				else
					qtbean.setUnabridged(false);
			}
			if(st.hasMoreTokens())
			{
				qtbean.setLimitUpDown(st.nextToken());
			}
			if(st.hasMoreTokens())
			{
				qtbean.setExtendedTradeDate(st.nextToken());
				qtbean.setExtendedLastTradeTime(st.nextToken());
			}
			if(st.hasMoreTokens())
			{
				qtbean.setVolumePlus(Long.parseLong(st.nextToken().trim()));
			}
		} catch (Exception e)
		{
			SystemOutLogger.error("Unable to change QTStream To QTMessageBean.");
			System.out.println("Exception for ticker " + qtbean.getTicker() + " " + beanstr);
			e.printStackTrace();
			//Exception happend may due to not enough tokens or invalid data.
			//when one of the above situation happend, return a null object.
		}
		return;
	}

	@Override
	public void resetAttribute(String attribute, String value) {

		if (attribute == null) return;

		Iterator it = compactionCache.values().iterator();
		while (it.hasNext()) {
			QTMessageBean qtBean = (QTMessageBean)it.next();

			// currently, only the open price would need to be reset
			// but if other resets are needed, this is where the 
			// reset should be placed.
			if(value.equalsIgnoreCase("N/A"))
				continue;

			if (attribute.equals(QTMessageKeys.OPEN_PRICE))
				qtBean.setOpenPrice(Double.parseDouble(value));
			if (attribute.equals(QTMessageKeys.LAST_CLOSED_PRICE))
				qtBean.setLastClosedPrice(Double.parseDouble(value));
			if (attribute.equals(QTMessageKeys.SETTLEMENT_PRICE))
				qtBean.setSettlementPrice(Double.parseDouble(value));

		}
	}

	@Override
	public MessageReducer cloneMe() {
		return new EquityQTMessageReducer();
	}
}
