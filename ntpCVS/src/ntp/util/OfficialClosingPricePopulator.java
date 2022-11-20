package ntp.util;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class OfficialClosingPricePopulator {
	
	public void getClosingPrice(HashMap<String, Double> tickerClosePrice, 
			HashMap<String, String> tickerPriMarketCenterMap, HashMap<String, String> exchangeMap){
		
		Iterator<Entry<String, String>>  iterator = tickerPriMarketCenterMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, String> entry = iterator.next();
			String ticker = entry.getKey();
			String tickerInt = convertStringToAscii(ticker);
			BigInteger tickerBigInt = new BigInteger(tickerInt);
			String tableName = findTableName(tickerInt, 1) ;
			String exchange = exchangeMap.get(tickerPriMarketCenterMap.get(ticker));
			String query = "Select trade_price from "+tableName 
					+"_T where TRADE_QUOTE_COND_CODE1 LIKE '%13%' AND TRADE_MARKET_CENTER = '"+exchange+"' AND TICKER = "+tickerBigInt;
			//		System.out.println(query);
//			int db = findDBName(tickerInt);
			Connection conn = null;
			Statement stmt;
			try {
				stmt = conn.createStatement();

				ResultSet rs = stmt.executeQuery(query);
				if(rs != null && rs.next()){
					System.out.println(rs.getDouble(1) +" , "+ tickerInt+" , "+ticker);
					tickerClosePrice.put(ticker, rs.getDouble(1));
				}
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}	
	}
	
	private String convertStringToAscii(String ticker){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< ticker.length(); i++){
			char temp = ticker.charAt(i);
			sb.append((int)temp);
		}
		return sb.toString();
	}
	
	/*private int findDBName(String ticker){
		int i = Integer.parseInt(ticker.substring(0, 2));
		if (i < 68)
			return Constants.DB_1;
		if (i < 75)
			return Constants.DB_2;
		if (i < 83)
			return Constants.DB_3;
		return Constants.DB_4;
	}
	*/
	private String findTableName(String ticker, Integer absDay) 
	{
		int i = Integer.parseInt(ticker.substring(0, 2));
		if (i < 66)
			return "TSQ_DAY_" + absDay.toString() + "_1";
		if (i < 67)
			return "TSQ_DAY_" + absDay.toString() + "_2";
		if (i < 68)
			return "TSQ_DAY_" + absDay.toString() + "_3";
		if (i < 70)
			return "TSQ_DAY_" + absDay.toString() + "_4";
		if (i < 72)
			return "TSQ_DAY_" + absDay.toString() + "_5";
		if (i < 75)
			return "TSQ_DAY_" + absDay.toString() + "_6";
		if (i < 77)
			return "TSQ_DAY_" + absDay.toString() + "_7";
		if (i < 80)
			return "TSQ_DAY_" + absDay.toString() + "_8";
		if (i < 83)
			return "TSQ_DAY_" + absDay.toString() + "_9";
		if (i < 86)
			return "TSQ_DAY_" + absDay.toString() + "_10";
		return "TSQ_DAY_" + absDay.toString() + "_11";
	}
}
