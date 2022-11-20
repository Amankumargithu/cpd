package com.b4utrade.helper;

import com.b4utrade.bean.QTMessageBean;

public class StockActivityBeanConverter {
	
	public static QTMessageBean convertSAHToQTM(StockActivityHelper sah) {
		
		if (sah == null)
			return null;
		QTMessageBean qtmb = new QTMessageBean();
		qtmb.setSystemTicker(sah.getTicker());
		try {
			qtmb.setLastPrice(Double.parseDouble(sah.getLastPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setOpenPrice(Double.parseDouble(sah.getOpenPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setLastClosedPrice(Double.parseDouble(sah.getPreviousPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setBidSize(Long.parseLong(sah.getBidSize()));
		} catch (Exception e) {
		}
		try {
			qtmb.setAskSize(Long.parseLong(sah.getAskSize()));
		} catch (Exception e) {
		}
		try {
			qtmb.setBidPrice(Double.parseDouble(sah.getBidPrice()));
		} catch (Exception e) {
		}
		try {
			qtmb.setAskPrice(Double.parseDouble(sah.getAskPrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setChangePrice(Double.parseDouble(sah.getChangePrice()));
		} catch (Exception e) {
		}

		try {
			qtmb.setPercentChange(Double.parseDouble(sah.getPercentChange()));
		} catch (Exception e) {
		}

		try {
			qtmb.setDayLow(Double.parseDouble(sah.getDayLow()));
		} catch (Exception e) {
		}

		try {
			qtmb.setDayHigh(Double.parseDouble(sah.getDayHigh()));
		} catch (Exception e) {
		}

		try {
			qtmb.setVolume(Long.parseLong(sah.getVolume()));
		} catch (Exception e) {
		}
		try {
			qtmb.setLastTradeVolume(Long.parseLong(sah.getLastTradeVolume()));
		} catch (Exception e) {
		}

		if (sah.getExchange() != null && !sah.getExchange().equals(""))
			qtmb.setExchangeCode(sah.getExchange());		
		
		if (sah.getAskExchangeCode() != null && !sah.getAskExchangeCode().equals(""))
			qtmb.setAskExchangeCode(sah.getAskExchangeCode());
		
		if (sah.getBidExchangeCode() != null && !sah.getBidExchangeCode().equals(""))
			qtmb.setBidExchangeCode(sah.getBidExchangeCode());
		
		if (sah.getMarketCenter() != null && !sah.getMarketCenter().equals(""))
			qtmb.setMarketCenter(sah.getMarketCenter());
		
		if (sah.getVWAP() != null && !sah.getVWAP().equals(""))
			qtmb.setVWAP(sah.getVWAP());
		
		// used to denote the short sale restriction indicator until we can provide a common field for the stock activity, stock activity helper
		// and the QT message bean.
		qtmb.setTickFlag(sah.getRestricted());

		qtmb.setUnabridged(sah.isParseOk());
		qtmb.setTradeDate(sah.getLastTradeDateGMT());
		qtmb.setLastTradeTime(sah.getLastTradeTimeGMT());
		
		qtmb.setLimitUpDown(sah.getLimitUpDown());
		if (sah.getComstockExchangeId() != null && !sah.getComstockExchangeId().equals(""))
			qtmb.setExchangeId(sah.getComstockExchangeId());
		
		try {
			qtmb.setSettlementPrice(Double.parseDouble(sah.getSettlementPrice()));	
		}
		catch (Exception e) {}
		

		return (qtmb);
	}

}
