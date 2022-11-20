package com.b4utrade.tsq;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import com.b4utrade.bean.TSQBean;
import com.tacpoint.util.Logger;

/**
 * Constants used by the b4utrade TSQSerializer
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2006.  All rights reserved.
 * @version 1.0
 */

public class TSQMessageSerializer  {


	public static byte[] deflate(TSQBean bean) {

		try {

			StringBuffer sb = new StringBuffer();
			sb.append(TSQMessageKeys.TICKER);
			sb.append(TSQMessageKeys.FIELD_SEP);
			sb.append(bean.getTicker());

			if (bean.getMsgSequence() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.MESSAGE_SEQUENCE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getMsgSequence().toString());
			}

			sb.append(TSQMessageKeys.TUPLE_SEP);
			sb.append(TSQMessageKeys.MESSAGE_TYPE);
			sb.append(TSQMessageKeys.FIELD_SEP);
			sb.append(bean.getMessageType().toString());

			if (bean.getTradeCancelIndicator() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_CANCEL_IND);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeCancelIndicator().toString());
			}

			if (bean.getTradeSequence() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_SEQUENCE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeSequence().toString());
			}

			if (bean.getTradeQuoteCondCode1() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_QUOTE_COND_CODE_1);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeQuoteCondCode1());
			}

			if (bean.getTradeQuoteCondCode2() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_QUOTE_COND_CODE_2);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeQuoteCondCode2());
			}

			if (bean.getTradeQuoteCondCode3() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_QUOTE_COND_CODE_3);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeQuoteCondCode3());
			}

			if (bean.getTradeQuoteCondCode4() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_QUOTE_COND_CODE_4);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeQuoteCondCode4());
			}

			if (bean.getTradeQuoteTime() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_QUOTE_TIME);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeQuoteTime().toString());
			}

			if (bean.getTradePrice() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_PRICE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradePrice().toString());
			}

			if (bean.getTradeSize() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_SIZE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeSize().toString());
			}

			if (bean.getBidPrice() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.BID_PRICE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getBidPrice().toString());
			}

			if (bean.getBidSize() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.BID_SIZE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getBidSize().toString());
			}

			if (bean.getAskPrice() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.ASK_PRICE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getAskPrice().toString());
			}

			if (bean.getAskSize() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.ASK_SIZE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getAskSize().toString());
			}

			if (bean.getExchangeId() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.EXCHANGE_ID);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getExchangeId());
			}

			if (bean.getTradeMarketCenter() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TRADE_MARKET_CENTER);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTradeMarketCenter());
			}

			if (bean.getAskMarketCenter() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.ASK_MARKET_CENTER);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getAskMarketCenter());
			}

			if (bean.getBidMarketCenter() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.BID_MARKET_CENTER);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getBidMarketCenter());
			}

			if (bean.getVwap() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.VWAP);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getVwap().toString());
			}

			if (bean.getCreationDateTime() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.CREATION_DATE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getCreationDateTime().toString());
			}

			if (bean.getComputedVwap() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.COMPUTED_VWAP);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getComputedVwap().toString());
			}

			if (bean.getTotalVolume() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.TOTAL_VOLUME);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getTotalVolume().toString());
			}

			if (bean.getFilteredTotalVolume() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.FILTERED_TOTAL_VOLUME);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getFilteredTotalVolume().toString());
			}

			if (bean.getMarketMakerId() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.MARKET_MAKER_ID);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getMarketMakerId().toString());
			}

			if (bean.getUnderlyingBidPrice() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.UND_BID_PRICE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getUnderlyingBidPrice().toString());
			}
			if (bean.getUnderlyingAskPrice() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.UND_ASK_PRICE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getUnderlyingAskPrice().toString());
			}
			if (bean.getUnderlyingBidSize() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.UND_BID_SIZE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getUnderlyingBidSize().toString());
			}
			if (bean.getUnderlyingAskSize() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.UND_ASK_SIZE);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getUnderlyingAskSize().toString());
			}
			if (bean.getUnderlyingBidExchnage() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.UND_BID_EXCH);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getUnderlyingBidExchnage().toString());
			}
			if (bean.getUnderlyingAskExchange() != null) {
				sb.append(TSQMessageKeys.TUPLE_SEP);
				sb.append(TSQMessageKeys.UND_ASK_EXCH);
				sb.append(TSQMessageKeys.FIELD_SEP);
				sb.append(bean.getUnderlyingAskExchange().toString());
			}
			return sb.toString().getBytes();
		}
		catch (Exception e) {
			System.out.println("TSQMessageSerializer.deflate - error encountered - "+e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static TSQBean inflate(byte[] bytes) {

		Map data = parseKeyValues(bytes);

		String value = null;
		TSQBean bean = new TSQBean();

		value = (String)data.get(TSQMessageKeys.TICKER);
		if (value != null) 
			bean.setTicker((String)data.get(TSQMessageKeys.TICKER));

		value = (String)data.get(TSQMessageKeys.MESSAGE_SEQUENCE);
		if (value != null) 
			bean.setMsgSequence(new Long((String)data.get(TSQMessageKeys.MESSAGE_SEQUENCE)));

		value = (String)data.get(TSQMessageKeys.MESSAGE_TYPE);
		if (value != null) 
			bean.setMessageType(new Short((String)data.get(TSQMessageKeys.MESSAGE_TYPE)));

		value = (String)data.get(TSQMessageKeys.TRADE_CANCEL_IND);
		if (value != null) 
			bean.setTradeCancelIndicator(new Short(value));

		value = (String)data.get(TSQMessageKeys.TRADE_SEQUENCE);
		if (value != null) 
			bean.setTradeSequence(new Long(value));

		value = (String)data.get(TSQMessageKeys.TRADE_QUOTE_COND_CODE_1);
		if (value != null) 
			bean.setTradeQuoteCondCode1(value);

		value = (String)data.get(TSQMessageKeys.TRADE_QUOTE_COND_CODE_2);
		if (value != null) 
			bean.setTradeQuoteCondCode2(value);

		value = (String)data.get(TSQMessageKeys.TRADE_QUOTE_COND_CODE_3);
		if (value != null) 
			bean.setTradeQuoteCondCode3(value);

		value = (String)data.get(TSQMessageKeys.TRADE_QUOTE_COND_CODE_4);
		if (value != null) 
			bean.setTradeQuoteCondCode4(value);

		value = (String)data.get(TSQMessageKeys.TRADE_QUOTE_TIME);
		if (value != null) {
			Integer time = new Integer(value);
			bean.setTradeQuoteTime(time);
		}

		value = (String)data.get(TSQMessageKeys.TRADE_PRICE);
		if (value != null) 
			bean.setTradePrice(new Double(value));

		value = (String)data.get(TSQMessageKeys.TRADE_SIZE);
		if (value != null) 
			bean.setTradeSize(new Long(value));

		value = (String)data.get(TSQMessageKeys.BID_PRICE);
		if (value != null) 
			bean.setBidPrice(new Double(value));

		value = (String)data.get(TSQMessageKeys.BID_SIZE);
		if (value != null) 
			bean.setBidSize(new Long(value));

		value = (String)data.get(TSQMessageKeys.ASK_PRICE);
		if (value != null) 
			bean.setAskPrice(new Double(value));

		value = (String)data.get(TSQMessageKeys.ASK_SIZE);
		if (value != null) 
			bean.setAskSize(new Long(value));

		value = (String)data.get(TSQMessageKeys.EXCHANGE_ID);
		if (value != null) 
			bean.setExchangeId(value);

		value = (String)data.get(TSQMessageKeys.TRADE_MARKET_CENTER);
		if (value != null) 
			bean.setTradeMarketCenter(value);

		value = (String)data.get(TSQMessageKeys.BID_MARKET_CENTER);
		if (value != null) 
			bean.setBidMarketCenter(value);

		value = (String)data.get(TSQMessageKeys.ASK_MARKET_CENTER);
		if (value != null) 
			bean.setAskMarketCenter(value);

		value = (String)data.get(TSQMessageKeys.VWAP);
		if (value != null) 
			bean.setVwap(new Double(value));

		value = (String)data.get(TSQMessageKeys.COMPUTED_VWAP);
		if (value != null) 
			bean.setComputedVwap(new Double(value));      

		value = (String)data.get(TSQMessageKeys.TOTAL_VOLUME);
		if (value != null) 
			bean.setTotalVolume(new Long(value));   

		value = (String)data.get(TSQMessageKeys.FILTERED_TOTAL_VOLUME);
		if (value != null) 
			bean.setFilteredTotalVolume(new Long(value));

		value = (String)data.get(TSQMessageKeys.MARKET_MAKER_ID);
		if (value != null) 
			bean.setMarketMakerId(value);

		value = (String)data.get(TSQMessageKeys.UND_BID_PRICE);
		if (value != null) 
			bean.setUnderlyingBidPrice(new Double(value));

		value = (String)data.get(TSQMessageKeys.UND_ASK_PRICE);
		if (value != null) 
			bean.setUnderlyingAskPrice(new Double(value));

		value = (String)data.get(TSQMessageKeys.UND_BID_SIZE);
		if (value != null) 
			bean.setUnderlyingBidSize(new Long(value));

		value = (String)data.get(TSQMessageKeys.UND_ASK_SIZE);
		if (value != null) 
			bean.setUnderlyingAskSize(new Long(value));

		value = (String)data.get(TSQMessageKeys.UND_BID_EXCH);
		if (value != null) 
			bean.setUnderlyingBidExchnage(value);

		value = (String)data.get(TSQMessageKeys.UND_ASK_EXCH);
		if (value != null) 
			bean.setUnderlyingAskExchange(value);

		try {
			value = (String)data.get(TSQMessageKeys.CREATION_DATE);
			if (value != null) 
				bean.setCreationDateTime(Timestamp.valueOf(value));
		}
		catch (Exception e) {e.printStackTrace();}


		Logger.log("bean after infation is : "+bean);
		return bean;
	}

	public static Map parseKeyValues(byte[] bytes) {

		Hashtable map = new Hashtable();

		StringTokenizer st = new StringTokenizer(new String(bytes),TSQMessageKeys.TUPLE_SEP);

		try {
			while (st.hasMoreTokens()) {
				String tuple = st.nextToken();
				int index = tuple.indexOf(TSQMessageKeys.FIELD_SEP);

				//System.out.println("TSQMessageSerializer key : "+tuple.substring(0,index) + " value : "+tuple.substring(index+1));

				map.put(tuple.substring(0,index),tuple.substring(index+1));
			}
			Logger.log("Map in TSQ Options is : "+map);
		}
		catch (Exception ex) {
			System.out.println("TSQMessageSerializer - Error parsing key values - "+ex.getMessage()+ " "+new String(bytes));
		}

		return map;
	}


}
