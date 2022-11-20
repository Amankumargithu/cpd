package com.b4utrade.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.ejb.Remote;

import com.b4utrade.bean.InterestRateBean;
import com.b4utrade.bean.StockOptionBean;
@Remote
public interface OptionData
{
	public HashMap getOptionChain(String ticker) ;
	public ArrayList getFutureChainByDescription(String ticker,int pagingIndex,boolean fitlerOn);
	public ArrayList getFutureChainByBaseSymbol(String ticker);
	public StockOptionBean getStockOption(String optionTicker) ;
	public HashMap getExpirationChain(String ticker) ;
	public InterestRateBean getInterestRate() ;
	public LinkedList getOptionQuote(String optionTickers) ;
	public HashMap getSpotSymbolMap() ; 
	public HashMap getTSQOptionChain(String rootTicker);
}
