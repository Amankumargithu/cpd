package com.b4utrade.tsq;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.b4utrade.bean.TSQBean;
import com.b4utrade.bean.TSQCriteriaBean;
import com.tacpoint.dataconnection.DBConnectionManager;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class TSQMessageSelector {

	private static final String CONNECTION_NAME = "MySQL";
	private static final String ALT_CONNECTION_NAME = "MySQL2";
	private static final String ALT3_CONNECTION_NAME = "MySQL3";
	private static final String ALT4_CONNECTION_NAME = "MySQL4";

	private static final String CONNECTION_NAME_DAY_0 = "MySQL_Day0";
	private static final String ALT_CONNECTION_NAME_DAY_0 = "MySQL2_Day0";
	private static final String ALT3_CONNECTION_NAME_DAY_0 = "MySQL3_Day0";
	private static final String ALT4_CONNECTION_NAME_DAY_0 = "MySQL4_Day0";

	private static final String MARKET_MAKER_CONNECTION_POOL_NAME = "MarketMaker";
	private static final String ALL_MARKET_MAKERS = "--ALL--";
	private static final String NON_REG_MARKET_MAKERS = "--ALL (no regionals)--";
	public static final String TORONTO_TSX_SUFFIX = ".T";
	public static final String VENTURE_TSX_SUFFIX = ".V";

	private static final String REGIONAL_MARKET_MAKER_EXCLUSIONS = 	
			" and MMID != 'ARCA' " +
					" and MMID != 'AMEX' " +
					" and MMID != 'BATS' " +
					" and MMID != 'BOSX' " +
					" and MMID != 'CBSX' " +
					" and MMID != 'CINN' " +
					" and MMID != 'ISEG' " +
					" and MMID != 'MWSE' " +
					" and MMID != 'NDQR' " +
					" and MMID != 'NYSE' " +
					" and MMID != 'OMDF' " +
					" and MMID != 'PHLX' ";

	private static final String NASD_BASIC_EXCHANGES = " and    (TRADE_MARKET_CENTER = 'd' or TRADE_MARKET_CENTER = 't' or TRADE_MARKET_CENTER = 'v' or TRADE_MARKET_CENTER = 'u') ";
	private static final String ORDER_BY_MSG_SEQUENCE_DESC = " ORDER BY TICKER DESC, TRADE_QUOTE_TIME DESC, MSG_SEQUENCE DESC ";
	private static final String ORDER_BY_MSG_SEQUENCE_ASC  = " ORDER BY TICKER ASC, TRADE_QUOTE_TIME ASC, MSG_SEQUENCE ASC  ";
	private static final String MARKET_OPEN_TIME  = "30000000";
	private static final String MARKET_CLOSE_TIME = "210000000";
	private static final String MKT_MAKER_MARKET_OPEN_TIME  = "30000";
	private static final String MKT_MAKER_MARKET_CLOSE_TIME = "210000";
	private static int MAX_ROWS = 251;
	private static final String TRADE_TABLE_SUFFIX = "_T";
	private static final String QUOTE_TABLE_SUFFIX = "_Q";
	private static final int PRECISION = 4;
	private static final String RANGE_INDEX_REPLACEMENT_TOKEN = "#";
	private static final String FORCE_RANGE_INDEX = " force INDEX(TICKER) ";
	private static final String NO_FORCE_RANGE_INDEX ="";
	private static long totalTradeSize = 0;

	private static String getConnectionPoolName(String symbol, int day) {
		if (symbol == null)
			return CONNECTION_NAME;
		byte[] bytes = symbol.getBytes();
		if (bytes[0] < 68) {
			if (day == 0)
				return CONNECTION_NAME_DAY_0;
			else
				return CONNECTION_NAME;
		}
		else if (bytes[0] < 75) {
			if (day == 0) 
				return ALT_CONNECTION_NAME_DAY_0;
			else
				return ALT_CONNECTION_NAME;
		}
		else if (bytes[0] < 83) {
			if (day == 0) 
				return ALT3_CONNECTION_NAME_DAY_0;
			else
				return ALT3_CONNECTION_NAME;
		}		
		else {
			if (day == 0) 
				return ALT4_CONNECTION_NAME_DAY_0;
			else
				return ALT4_CONNECTION_NAME;
		}
	}

	public static List<String> obtainTSQDates() {
		ArrayList<String> results = new ArrayList<String>();	
		DBConnectionManager dbConnectionMgr = DBConnectionManager.getInstance();
		Connection con = dbConnectionMgr.getConnection(CONNECTION_NAME_DAY_0, 1000);
		Statement dateStatement = null;
		ResultSet dateResults = null;
		if (con == null) {
			Logger.log("TSQMessageSelector.obtainTSQDates - unable to obtain connection from pool.");
			return results;
		}
		try {	     
			dateStatement = con.createStatement();
			long beginTime = System.currentTimeMillis();
			for (int i=0; i < 41; i++) {
				String query = "select DATE_CREATED from TSQ_DAY_"+i+"_1_Q limit 1";
				try {
					dateResults = dateStatement.executeQuery(query.toString());	 	    
					if (dateResults.next()) {				                 
						String day = dateResults.getString(1);
						results.add(day);
					}
					else {
						results.add("Unavailable");
					}
				}
				catch (Exception sqlex) {
					Logger.log("TSQMessageSelector.obtainDates - exception encountered - "+sqlex.getMessage(),sqlex);
				}	
				finally {
					if (dateResults != null) {
						try {
							dateResults.close();
						} catch (Exception dbex) {
						}
					}
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println("TSQMessageSelector.obtainTSQDates - result set size : "+results.size()+" total time to retrieve dates : "+(endTime - beginTime));
			return results;    
		}
		catch (Exception e) {
			Logger.log("TSQMessageSelector.obtainTSQDates - exception encountered - "+e.getMessage(),e);
		}
		finally {
			if (con != null) {
				try {
					dbConnectionMgr.freeConnection(CONNECTION_NAME_DAY_0, con);
				} catch (Exception dbex) {
				}
			}
		}		
		return results;
	}

	public static List<TSQBean> selectMarketMakers(TSQCriteriaBean criteria) {
		ArrayList<TSQBean> results = new ArrayList<TSQBean>();
		DBConnectionManager dbConnectionMgr = DBConnectionManager.getInstance();
		Connection con = dbConnectionMgr.getConnection(MARKET_MAKER_CONNECTION_POOL_NAME, 1000);
		Statement marketMakerStatement = null;
		ResultSet marketMakerResults = null;
		if (con == null) {
			Logger.log("TSQMessageSelector.selectMarketMakers - unable to obtain connection from pool.");
			return results;
		}
		try {
			marketMakerStatement = con.createStatement();
			String query = buildMarketMakerQueryString(criteria);	  
			long beginTime = System.currentTimeMillis();
			marketMakerResults = marketMakerStatement.executeQuery(query);	  
			long endTime = System.currentTimeMillis();
			System.out.println("TSQMessageSelector.selectMarketMakers query time : "+(endTime - beginTime));		    
			Timestamp creationDate = null;
			while (marketMakerResults.next()) {
				int i=1;	
				TSQBean bean = new TSQBean();
				// first column is the ticker, but we'll want the ASCII version,
				// so we'll skip it ...
				marketMakerResults.getLong(i++);
				bean.setTicker(criteria.getSymbol());
				bean.setMessageType(TSQBean.TYPE_MARKET_MAKER);
				long mseq = marketMakerResults.getLong(i++);
				if (mseq > 0)
					bean.setMsgSequence(new Long(mseq));
				bean.setMarketMakerId(marketMakerResults.getString(i++));
				int tqtime = marketMakerResults.getInt(i++);
				bean.setTradeQuoteTime(new Integer(tqtime));
				String curDay = marketMakerResults.getString(i++);
				if (creationDate == null) {
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
					java.util.Date now = sdf.parse(curDay);
					creationDate = new Timestamp(now.getTime());
				}
				bean.setCreationDateTime(creationDate);
				double bprice = marketMakerResults.getDouble(i++);
				if (bprice > 0.00001)
					bean.setBidPrice(new Double(bprice));
				double aprice = marketMakerResults.getDouble(i++);
				if (aprice > 0.0001)
					bean.setAskPrice(new Double(aprice));
				long bsize = marketMakerResults.getLong(i++);
				if (bsize > 0)
					bean.setBidSize(new Long(bsize));
				long asize = marketMakerResults.getLong(i++);
				if (asize > 0)
					bean.setAskSize(new Long(asize));
				results.add(bean);	    	
			}
			System.out.println("TSQMessageSelector.selectMarketMakers - result set size : "+results.size());
			List<TSQBean> finalResults = TSQMessageMerger.merge(results, new ArrayList<TSQBean>());
			return finalResults;    
		}
		catch (Exception e) {
			Logger.log("TSQMessageSelector.selectMarketMakers - exception encountered - "+e.getMessage(),e);
		}
		finally {
			if (marketMakerResults != null) {
				try {
					marketMakerResults.close();
				} catch (Exception dbex) {}
			}
			if (marketMakerStatement != null) {
				try {
					marketMakerStatement.close();
				} catch (Exception dbex) {}
			}
			if (con != null) {
				try {
					dbConnectionMgr.freeConnection(MARKET_MAKER_CONNECTION_POOL_NAME, con);
				} catch (Exception dbex) {}
			}
		}		
		return results;
	}

	public static HashMap<String, String> executeVWAP(String symbol, String startTime, String endTime) throws Exception
	{
		int day =0;
		DBConnectionManager dbConnectionMgr = DBConnectionManager.getInstance();
		String connectionPoolName = CONNECTION_NAME;
		Statement xvwapStatement = null;
		ResultSet xvwapRS = null;
		String dailyTableName = TSQTableMapper.findTableName(symbol, day);
		StringBuffer sb = createVWAPSelectStatement(TSQCriteriaBean.TS, symbol, dailyTableName, day );
		if (startTime != null && endTime != null) {
			sb.append(" and TRADE_QUOTE_TIME between "+startTime+" and "+endTime);		    	
		}		    
		else {
			// assume this is an initial query with no time params specified ...
			sb.append(" and TRADE_QUOTE_TIME <= "+MARKET_CLOSE_TIME);
		}
		sb.append(" and    INCL_IN_VWAP = " + TSQBean.INCLUDE_IN_VWAP);
		// To remove dulpicate count of Opening price for Nasdaq symbols
		sb.append(" AND NOT (TRADE_QUOTE_COND_CODE1 LIKE '%17%') ");
		System.out.println("VWAP query string : " + sb.toString());
		connectionPoolName = getConnectionPoolName(symbol, day);
		Connection con = dbConnectionMgr.getConnection(connectionPoolName, 1000);
		if (con == null) {
			Logger.log("TSQMessageSelector - unable to obtain connection from pool : " + connectionPoolName);
			return null;
		}
		Statement vwapStatement = con.createStatement();
		ResultSet vwapRS = vwapStatement.executeQuery(sb.toString());
		HashMap<String, String> result = new HashMap<>();
		if (vwapRS.next()) {
			double totalTradeSizeByTradePrice = vwapRS.getDouble(1);
			totalTradeSize = vwapRS.getLong(2);
			String cancelTableName = TSQTableMapper.findCancelledTableName(day);
			sb = new StringBuffer();
			sb = createXVWAPSelectStatement(TSQCriteriaBean.TS, symbol, dailyTableName, cancelTableName, day);
			sb.append(" and x.TICKER = t.TICKER ");
			sb.append(" and x.TRADE_SEQUENCE = t.TRADE_SEQUENCE ");
			sb.append(" and x.TRADE_MARKET_CENTER = t.TRADE_MARKET_CENTER ");	
			if (startTime != null && endTime != null) {
				sb.append(" and t.TRADE_QUOTE_TIME between "+startTime+" and "+endTime);		    	
			}		    
			else {
				// assume this is an initial query with no time params specified ...
				sb.append(" and t.TRADE_QUOTE_TIME <= "+MARKET_CLOSE_TIME);
			}
			xvwapStatement = con.createStatement();
			xvwapRS = xvwapStatement.executeQuery(sb.toString());
			double vwap = 0;
			vwap = calculateVWAP(totalTradeSizeByTradePrice, totalTradeSize, xvwapRS);
			result.put("vwap", Double.toString(vwap));
			result.put("symbol", symbol);
			result.put("volume", Long.toString(totalTradeSize));
		}
		if (con != null) {
			try {
				dbConnectionMgr.freeConnection(connectionPoolName, con);
			} catch (Exception dbex) {
			}
		}
		return result;
	}
	
	public static List<TSQBean> execute(TSQCriteriaBean criteria) {
		try {
			MAX_ROWS = Integer.parseInt(Environment.get("MAX_TSQ_RESULTS"));
		}
		catch (Exception e) {}
		DBConnectionManager dbConnectionMgr = DBConnectionManager.getInstance();
		System.out.println("Table history index being requested : "+criteria.getDay()+" for ticker : "+criteria.getSymbol());
		ArrayList<TSQBean> dailyResults = new ArrayList<TSQBean>();
		ArrayList<TSQBean> cancelledResults = new ArrayList<TSQBean>();
		Connection con = null;
		Statement dailyStatement = null;
		ResultSet dailyRS = null;
		Statement cancelledStatement = null;
		ResultSet cancelledRS = null;
		Statement vwapStatement = null;
		ResultSet vwapRS = null;
		Statement xvwapStatement = null;
		ResultSet xvwapRS = null;
		String connectionPoolName = CONNECTION_NAME;
		try {
			// for an initial query, the from time and to time are both coming in as zero.  reset to null which will use the market open and market close
			if (criteria.getFromTime() != null) {
				try {
					if (Integer.parseInt(criteria.getFromTime()) == 0)
						criteria.setFromTime(null);
				}
				catch (NumberFormatException nfe) {
					System.out.println("TSQMessageSelector - invalid from time being passed : "+criteria.getFromTime());
					criteria.setFromTime(null);
				}
			}
			if (criteria.getToTime() != null) {
				try {
					if (Integer.parseInt(criteria.getToTime()) == 0)
						criteria.setToTime(null);
				}
				catch (NumberFormatException nfe) {
					System.out.println("TSQMessageSelector - invalid to time being passed : "+criteria.getToTime());
					criteria.setToTime(null);
				}
			}			
			String query = null;
			// extract the daily table data first ...
			if (criteria != null
					&& (criteria.getDisplay().equals(TSQCriteriaBean.TSQ) || criteria.getDisplay().equals(TSQCriteriaBean.TSQ_NASD_BASIC)))
				query = buildTradeAndQuoteQueryString(criteria);
			if (criteria != null
					&& (criteria.getDisplay().equals(TSQCriteriaBean.TS) || criteria
							.getDisplay().equals(TSQCriteriaBean.TSV) || criteria.getDisplay().equals(TSQCriteriaBean.TS_NASD_BASIC)))
				query = buildTradeQueryString(criteria);
			if (criteria != null
					&& (criteria.getDisplay().equals(TSQCriteriaBean.TQ) || criteria.getDisplay().equals(TSQCriteriaBean.TQ_NASD_BASIC)))
				query = buildQuoteQueryString(criteria);
			connectionPoolName = getConnectionPoolName(criteria.getSymbol(), criteria.getDay().intValue());
			con = dbConnectionMgr.getConnection(connectionPoolName, 1000);
			if (con == null) {
				Logger.log("TSQMessageSelector - unable to obtain connection from pool : " + connectionPoolName);
				return null;
			}
			long beginTime = System.currentTimeMillis();
			Logger.log("TSQMessageSelector - about to run TSQ query!");
			dailyStatement = con.createStatement();
			long curTime = System.currentTimeMillis();			
			dailyRS = dailyStatement.executeQuery(query);
			long endTime = System.currentTimeMillis();
			System.out.println("TSQMessageSelector - time to execute TSQ query : "+(endTime - curTime));
			curTime = System.currentTimeMillis();
			inflateResults(dailyRS, dailyResults, criteria);
			endTime = System.currentTimeMillis();
			System.out.println("TSQMessageSelector - time to inflate TSQ query results : "+(endTime - curTime));
			// now extract the cancelled data ...
			query = buildCancelledQueryString(criteria);
			cancelledStatement = con.createStatement();
			curTime = System.currentTimeMillis();
			cancelledRS = cancelledStatement.executeQuery(query);
			endTime = System.currentTimeMillis();
			System.out.println("TSQMessageSelector - time to execute X query : "+(endTime - curTime));
			curTime = System.currentTimeMillis();
			inflateResults(cancelledRS, cancelledResults, criteria);
			endTime = System.currentTimeMillis();
			System.out.println("TSQMessageSelector - time to inflate X query results : "+(endTime - curTime));
			double vwap = 0;
			long totalTradeSize = 0;
			// perform VWAP calculation if necessary ...
			if (criteria != null
					&& criteria.getDisplay().equals(TSQCriteriaBean.TSV)) {
				query = buildVWAPQueryString(criteria);
				vwapStatement = con.createStatement();
				curTime = System.currentTimeMillis();
				vwapRS = vwapStatement.executeQuery(query);
				endTime = System.currentTimeMillis();
				System.out.println("TSQMessageSelector - time to execute VWAP query : "+(endTime - curTime));				
				if (vwapRS.next()) {
					double totalTradeSizeByTradePrice = vwapRS.getDouble(1);
					totalTradeSize = vwapRS.getLong(2);
					query = buildXVWAPQueryString(criteria);
					xvwapStatement = con.createStatement();
					curTime = System.currentTimeMillis();
					xvwapRS = xvwapStatement.executeQuery(query);
					endTime = System.currentTimeMillis();
					System.out.println("TSQMessageSelector - time to execute X VWAP query : "+(endTime - curTime));
					vwap = calculateVWAP(totalTradeSizeByTradePrice, totalTradeSize, xvwapRS);
					totalTradeSize = TSQMessageSelector.totalTradeSize;
					System.out.println("VWAP for ticker : " + criteria.getSymbol() + " " + vwap);
				} else {
					System.out.println("TSQMessageSelector - no VWAP qualified trade messages available for ticker : " + criteria.getSymbol());
				}
			}
			endTime = System.currentTimeMillis();
			Logger.log("TSQMessageSelector - time to execute queries : " + (endTime - beginTime));
			System.out.println("TSQMessageSelector - total time to execute all queries : "+(endTime - beginTime));		
			Logger.log("TSQMessageSelector - results size : " + dailyResults.size());
			Logger.log("TSQMessageSelector - cancelled results size : " + cancelledResults.size());
			List<TSQBean> finalResults = TSQMessageMerger.merge(dailyResults, cancelledResults);
			Logger.log("TSQMessageSelector - final merged results size : " + finalResults.size());
			TSQTradeQuoteTimeComparator msc = new TSQTradeQuoteTimeComparator();
			msc.setDirection(TSQTradeQuoteTimeComparator.DESCENDING);
			if (finalResults != null) {
				Collections.sort(finalResults, msc);  
			}
			if (criteria.getDisplay().equals(TSQCriteriaBean.TSV)) {
				// kluge - but we set the same VWAP calc and filtered total volume on each TSQBean ...
				Double compVWAP = new Double(vwap);
				Long ftv = new Long(totalTradeSize);
				for(TSQBean b : finalResults) {
					b.setComputedVwap(compVWAP);
					b.setFilteredTotalVolume(ftv);
				}
			}
			return finalResults;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.log("TSQMessageSelector.execute - error encountered - " + e.getMessage(), e);
			return null;
		}
		finally {
			if (dailyRS != null) {
				try {
					dailyRS.close();
				} catch (Exception rsex) {
				}
			}
			if (dailyStatement != null) {
				try {
					dailyStatement.close();
				} catch (Exception stex) {
				}
			}
			if (cancelledRS != null) {
				try {
					cancelledRS.close();
				} catch (Exception rsex) {
				}
			}
			if (cancelledStatement != null) {
				try {
					cancelledStatement.close();
				} catch (Exception stex) {
				}
			}
			if (vwapRS != null) {
				try {
					vwapRS.close();
				} catch (Exception rsex) {
				}
			}
			if (vwapStatement != null) {
				try {
					vwapStatement.close();
				} catch (Exception stex) {
				}
			}
			if (xvwapRS != null) {
				try {
					xvwapRS.close();
				} catch (Exception rsex) {
				}
			}
			if (xvwapStatement != null) {
				try {
					xvwapStatement.close();
				} catch (Exception stex) {
				}
			}
			if (con != null) {
				try {
					dbConnectionMgr.freeConnection(connectionPoolName, con);
				} catch (Exception dbex) {
				}
			}
		}
	}

	private static double calculateVWAP(double totalTradePriceByTradeSize, long totalTradeSize, ResultSet rs) {
		try {
			BigDecimal totalTradePriceByTradeSizeBD = new BigDecimal(totalTradePriceByTradeSize);
			// comment out the cancelled trades to see if that makes a difference
			// in the VWAP calculation ...
			while (rs.next()) {
				int i = 1;
				double tradePrice = rs.getDouble(i++);
				long tradeSize = rs.getLong(i++);
				BigDecimal ctp = new BigDecimal(tradePrice);
				BigDecimal cts = new BigDecimal(tradeSize);
				BigDecimal result = ctp.multiply(cts);
				// remove cancelled trade from numerator ...
				totalTradePriceByTradeSizeBD = totalTradePriceByTradeSizeBD.subtract(result);
				// remove cancelled size from denominator ...
				totalTradeSize = totalTradeSize - tradeSize;
			}			
			double vwap = 0;
			if(totalTradeSize != 0)
				vwap = totalTradePriceByTradeSizeBD.divide(
						new BigDecimal(totalTradeSize), PRECISION,
						BigDecimal.ROUND_HALF_UP).doubleValue();
			TSQMessageSelector.totalTradeSize = totalTradeSize;
			return vwap;
		} catch (Exception e) {
			Logger.log("TSQMessageSelector.caclulateVWAP - exception encountered : "+ e.getMessage(), e);
			System.out.println("TSQMessageSelector - exception ocurred - "+e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	private static void inflateResults(ResultSet rs, List<TSQBean> results, TSQCriteriaBean criteria) {
		try {
			Timestamp creationDate = null;
			while (rs.next()) {
				int i = 1;
				TSQBean bean = new TSQBean();
				// first column is the ticker, but we'll want the ASCII version,
				// so we'll skip it ...
				rs.getLong(i++);
				bean.setTicker(criteria.getSymbol());
				long mseq = rs.getLong(i++);
				if (mseq > 0)
					bean.setMsgSequence(new Long(mseq));
				long tseq = rs.getLong(i++);
				if (!rs.wasNull())
					bean.setTradeSequence(new Long(tseq));
				short msgType = rs.getShort(i++);
				bean.setMessageType(new Short(msgType));
				short cancelInd = rs.getShort(i++);
				if (bean.getMessageType().equals(TSQBean.TYPE_TRADE)||bean.getMessageType().equals(TSQBean.CANCELLED_TRADE))
					bean.setTradeCancelIndicator(new Short(cancelInd));
				
				String temp =rs.getString(i++);
				if(temp != null)
					temp = temp.replaceAll("\\|", ",");
				bean.setTradeQuoteCondCode1(temp);				
				bean.setTradeQuoteCondCode2(rs.getString(i++));
				bean.setTradeQuoteCondCode3(rs.getString(i++));
				bean.setTradeQuoteCondCode4(rs.getString(i++));
				try
				{
					if (bean.getMessageType().equals(TSQBean.TYPE_COMPOSITE_QUOTE))
					{
						String upDown = formatLimitUpDown(bean.getTradeQuoteCondCode1());
						String[] vals = upDown.split(",");
						if(vals.length == 2)
						{
							bean.setTradeQuoteCondCode3(vals[0]);
							bean.setTradeQuoteCondCode4(vals[1]);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				int tqtime = rs.getInt(i++);
				bean.setTradeQuoteTime(new Integer(tqtime));
				String curDay = rs.getString(i++);
				if (creationDate == null) {
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
					java.util.Date now = sdf.parse(curDay);
					creationDate = new Timestamp(now.getTime());
				}
				bean.setCreationDateTime(creationDate);
				double tprice = rs.getDouble(i++);
				if (tprice > 0.00001)
					bean.setTradePrice(new Double(tprice));
				double vwap = rs.getDouble(i++);
				if (vwap > 0.00001)
					bean.setVwap(new Double(vwap));
				long tsize = rs.getLong(i++);
				if (tsize > 0)
					bean.setTradeSize(new Long(tsize));
				long totalVolume = rs.getLong(i++);
				if (totalVolume > 0)
					bean.setTotalVolume(new Long(totalVolume));
				double bprice = rs.getDouble(i++);
				if (bprice > 0.00001)
					bean.setBidPrice(new Double(bprice));
				double aprice = rs.getDouble(i++);
				if (aprice > 0.0001)
					bean.setAskPrice(new Double(aprice));
				long bsize = rs.getLong(i++);
				if (bsize > 0)
					bean.setBidSize(new Long(bsize));
				long asize = rs.getLong(i++);
				if (asize > 0)
					bean.setAskSize(new Long(asize));
				bean.setTradeMarketCenter(rs.getString(i++));
				bean.setBidMarketCenter(rs.getString(i++));
				bean.setAskMarketCenter(rs.getString(i++));
				results.add(bean);
			}
		} catch (Exception e) {
			Logger.log("TSQMessageSelector.inflateResults - exception encountered : " + e.getMessage(), e);
		}
	}

	public static String formatLimitUpDown(String limitUpDown) 
	{
		if(limitUpDown == null || limitUpDown.length()<1 )
		{
			return " , ";
		}
		char limitChar = limitUpDown.charAt(0);
		switch (limitChar)
		{
		case 'A':
			return " , ";
		case 'B':
			return "N, ";
		case 'C':
			return " ,N";
		case 'D':
			return "N,N";
		case 'E':
			return "L, ";
		case 'F':
			return " ,L";
		case 'G':
			return "L,N";
		case 'H':
			return "N,L";
		case 'I':
			return "L,L";
		}
		return " , ";
	}

	private static StringBuffer createVWAPSelectStatement(String display, String symbol, String tableName, int day) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select sum(TRADE_PRICE * TRADE_SIZE), ");
		sb.append("        sum(TRADE_SIZE) ");
		sb.append(" from   " + tableName + TRADE_TABLE_SUFFIX + " ");
		sb.append(" where  TICKER = " + getHashCode(symbol, day));
		return sb;
	}

	private static StringBuffer createXVWAPSelectStatement(String display, String symbol, String tableName, String xtableName, int day) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select x.TRADE_PRICE, ");
		sb.append("        x.TRADE_SIZE ");
		sb.append(" from   " + tableName + TRADE_TABLE_SUFFIX + "  t, ");
		sb.append(" " + xtableName + TRADE_TABLE_SUFFIX + " x  ");
		sb.append(" where  x.TICKER = " + getHashCode(symbol, day));
		return sb;
	}

	private static StringBuffer createSelectStatement(String display, String symbol, String tableName, int day) {
		StringBuffer sb = new StringBuffer();
		if (display != null && (display.equals(TSQCriteriaBean.TS) || display
						.equals(TSQCriteriaBean.TSV) || display.equals(TSQCriteriaBean.TS_NASD_BASIC))) {
			sb.append(" select TICKER, ");
			sb.append("        MSG_SEQUENCE, ");
			sb.append("        TRADE_SEQUENCE, ");
			sb.append("        TRADE_QUOTE_TYPE, ");
			sb.append("        TRADE_CANCEL_IND, ");
			sb.append("        TRADE_QUOTE_COND_CODE1, ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        TRADE_QUOTE_TIME, ");
			sb.append("        DATE_CREATED, ");
			sb.append("        TRADE_PRICE, ");
			sb.append("        VWAP, ");
			sb.append("        TRADE_SIZE, ");
			sb.append("        VOLUME, ");
			sb.append("        BID_PRICE, ");
			sb.append("        ASK_PRICE, ");
			sb.append("        BID_SIZE, ");
			sb.append("        ASK_SIZE, ");
			sb.append("        TRADE_MARKET_CENTER, ");
			sb.append("        BID_MARKET_CENTER, ");
			sb.append("        ASK_MARKET_CENTER ");
		}
		else if (display != null && (display.equals(TSQCriteriaBean.TQ) || display.equals(TSQCriteriaBean.TQ_NASD_BASIC))) {
			sb.append(" select TICKER, ");
			sb.append("        MSG_SEQUENCE, ");
			sb.append("        '', ");
			sb.append("        TRADE_QUOTE_TYPE, ");
			sb.append("        '', ");
			sb.append("        TRADE_QUOTE_COND_CODE1, ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        TRADE_QUOTE_TIME, ");
			sb.append("        DATE_CREATED, ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        '', ");
			sb.append("        BID_PRICE, ");
			sb.append("        ASK_PRICE, ");
			sb.append("        BID_SIZE, ");
			sb.append("        ASK_SIZE, ");
			sb.append("        '', ");
			sb.append("        BID_MARKET_CENTER, ");
			sb.append("        ASK_MARKET_CENTER ");
		}
		if (display != null && (display.equals(TSQCriteriaBean.TS) || display
						.equals(TSQCriteriaBean.TSV) || display.equals(TSQCriteriaBean.TS_NASD_BASIC)))
			sb.append(" from   " + tableName + TRADE_TABLE_SUFFIX + " ");
		else if (display != null && (display.equals(TSQCriteriaBean.TQ) || display.equals(TSQCriteriaBean.TQ_NASD_BASIC)))
			sb.append(" from   " + tableName + QUOTE_TABLE_SUFFIX + " ");
		else
			sb.append(" from   " + tableName + " ");
		sb.append(" "+RANGE_INDEX_REPLACEMENT_TOKEN+ " ");
		sb.append(" where  TICKER = " + getHashCode(symbol, day));
		return sb;
	}

	private static String buildTradePredicate(TSQCriteriaBean criteria, TSQIndex forceRangeIndex) {
		boolean orderByDesc = true;
		boolean useTradeMarketCenter = false;
		forceRangeIndex.noForceIndex();
		StringBuffer sb = new StringBuffer(256);
		boolean isNasdaq = false;
		// need to limit exchanges for NASD basic users ...
		if ((criteria.getDisplay().equals(TSQCriteriaBean.TS_NASD_BASIC) || criteria.getDisplay().equals(TSQCriteriaBean.TSQ_NASD_BASIC)) && criteria.getExchange() == null) {
			sb.append(NASD_BASIC_EXCHANGES);
			useTradeMarketCenter = true;
			isNasdaq = true;
		}
		else if (criteria.getExchange() != null) {
			sb.append(" and    TRADE_MARKET_CENTER = '" + criteria.getExchange() + "'");
			useTradeMarketCenter = true;
		}	
		if(isNasdaq)
		{
			sb.append(" and    TRADE_QUOTE_TYPE !=  " + TSQBean.TYPE_COMPOSITE_QUOTE);
			forceRangeIndex.noForceIndex();	
		}
		if (criteria.getQuoteFromPrice() != null && criteria.getQuotePriceFilterType() != null) {
			if (criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_BID) || 
					criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_BID_AND_ASK)) {
				sb.append(" and    BID_PRICE >= " + criteria.getQuoteFromPrice());
				forceRangeIndex.noForceIndex();	
				if (criteria.getQuoteToPrice() != null) {
					sb.append(" and    BID_PRICE <= " + criteria.getQuoteToPrice());
				}
			}
			if (criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_ASK) || 
					criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_BID_AND_ASK)) {
				sb.append(" and    ASK_PRICE >= " + criteria.getQuoteFromPrice());
				forceRangeIndex.noForceIndex();	
				if (criteria.getQuoteToPrice() != null) {
					sb.append(" and    ASK_PRICE <= " + criteria.getQuoteToPrice());
				}
			}	
		}
		try {
			String isScottradeBuild = Environment.get("IS_SCOTTRADE_BUILD");
			if(isScottradeBuild.equalsIgnoreCase("false")){
				System.out.println("it is not a scottrade build");
				if (criteria.isBboOnly() && criteria.getExchange() == null && !isNasdaq) {
					sb.append(" and    TRADE_QUOTE_TYPE = " + TSQBean.TYPE_COMPOSITE_QUOTE);			
					forceRangeIndex.noForceIndex();		
				}	
			}
		} catch (Exception e) {}
		boolean useMessageTimes = true;
		// for client side paging!
		if (criteria.getFromMessageSequence() != null
				&& criteria.getPageDirection() != null) {
			if (criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_BACKWARD)) {
				String startTime = criteria.getFromTime() != null ? criteria.getFromTime() : MARKET_OPEN_TIME;
				sb.append(" and  TRADE_QUOTE_TIME between "+startTime+" and "+criteria.getFromMessageSequenceTime());
				sb.append(" and  MSG_SEQUENCE < " + criteria.getFromMessageSequence());
				useMessageTimes = false;
			} else if (criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_FORWARD)) {
				String endTime = criteria.getToTime() != null ? criteria.getToTime() : MARKET_CLOSE_TIME;
				sb.append(" and  TRADE_QUOTE_TIME between "+criteria.getFromMessageSequenceTime()+" and "+endTime);
				sb.append(" and  MSG_SEQUENCE > " + criteria.getFromMessageSequence());
				orderByDesc = false;
				useMessageTimes = false;
			}
			else {
				Logger.log("TSQMessageSelector.buildTradePredicate() - invalid page direction : " + criteria.getPageDirection());
			}
			if (!useTradeMarketCenter)
				forceRangeIndex.forceIndex();
		}		
		if (useMessageTimes) {
			if (criteria.getFromTime() != null && criteria.getToTime() != null) {
				sb.append(" and TRADE_QUOTE_TIME between "+criteria.getFromTime()+" and "+criteria.getToTime());		    	
			}		    
			else {
				// assume this is an initial query with no time params specified ...
				sb.append(" and TRADE_QUOTE_TIME <= "+MARKET_CLOSE_TIME);
			}
			if (!useTradeMarketCenter)
				forceRangeIndex.forceIndex();
		}
		if (criteria.getFromPrice() != null) {
			sb.append(" and    TRADE_PRICE >= " + criteria.getFromPrice());
			forceRangeIndex.noForceIndex();
		}
		if (criteria.getFromPrice() != null && criteria.getToPrice() != null) {
			sb.append(" and    TRADE_PRICE <= " + criteria.getToPrice());
			forceRangeIndex.noForceIndex();
		}
		if (criteria.getFromSize() != null) {
			sb.append(" and    TRADE_SIZE >= " + criteria.getFromSize());
			forceRangeIndex.noForceIndex();
		}
		if (criteria.getFromSize() != null && criteria.getToSize() != null) {
			sb.append(" and    TRADE_SIZE <= " + criteria.getToSize());
			forceRangeIndex.noForceIndex();
		}
		if (orderByDesc)
			sb.append(ORDER_BY_MSG_SEQUENCE_DESC);
		else
			sb.append(ORDER_BY_MSG_SEQUENCE_ASC);
		sb.append(" LIMIT " + MAX_ROWS + " ");
		return sb.toString();
	}

	private static String buildQuotePredicate(TSQCriteriaBean criteria, TSQIndex forceRangeIndex) {
		forceRangeIndex.noForceIndex();
		boolean orderByDesc = true;
		StringBuffer sb = new StringBuffer(256);
		forceRangeIndex.forceIndex();
		// need to limit exchanges for NASD basic users ...
		boolean isNasdaq = false;
		if ((criteria.getDisplay().equals(TSQCriteriaBean.TSQ_NASD_BASIC) || criteria.getDisplay().equals(TSQCriteriaBean.TQ_NASD_BASIC)) && criteria.getExchange() == null) {
			isNasdaq = true;
			sb.append(" and    (BID_MARKET_CENTER = 'd' or BID_MARKET_CENTER = 't') ");
			sb.append(" and    (ASK_MARKET_CENTER = 'd' or ASK_MARKET_CENTER = 't') ");
			// No BBO for NAsdaq Basic
			sb.append(" and    TRADE_QUOTE_TYPE !=  " + TSQBean.TYPE_COMPOSITE_QUOTE);	
			forceRangeIndex.noForceIndex();	
		}
		else if (criteria.getExchange() != null) {
			sb.append(" and    (BID_MARKET_CENTER = '" + criteria.getExchange() + "'");
			sb.append(" or     ASK_MARKET_CENTER = '" + criteria.getExchange() + "') ");			
			forceRangeIndex.noForceIndex();		
		}
		if (criteria.isBboOnly() && criteria.getExchange() == null && !isNasdaq) {
			sb.append(" and    TRADE_QUOTE_TYPE = " + TSQBean.TYPE_COMPOSITE_QUOTE);			
			forceRangeIndex.noForceIndex();		
		}	
		boolean useMessageTimes = true;
		// for client side paging!
		if (criteria.getFromMessageSequence() != null
				&& criteria.getPageDirection() != null) {
			if (criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_BACKWARD)) {
				String startTime = criteria.getFromTime() != null ? criteria.getFromTime() : MARKET_OPEN_TIME;
				sb.append(" and  TRADE_QUOTE_TIME between "+startTime+" and "+criteria.getFromMessageSequenceTime());
				sb.append(" and  MSG_SEQUENCE < " + criteria.getFromMessageSequence());
				useMessageTimes = false;
			} else if (criteria.getPageDirection().equals(TSQCriteriaBean.PAGE_FORWARD)) {
				String endTime = criteria.getToTime() != null ? criteria.getToTime() : MARKET_CLOSE_TIME;
				sb.append(" and  TRADE_QUOTE_TIME between "+criteria.getFromMessageSequenceTime()+" and "+endTime);
				sb.append(" and  MSG_SEQUENCE > " + criteria.getFromMessageSequence());
				orderByDesc = false;
				useMessageTimes = false;
			}
			else {
				Logger.log("TSQMessageSelector.buildQuotePredicate() - invalid page direction : " + criteria.getPageDirection());
			}
		}		
		if (useMessageTimes) {
			if (criteria.getFromTime() != null && criteria.getToTime() != null) {
				sb.append(" and TRADE_QUOTE_TIME between "+criteria.getFromTime()+" and "+criteria.getToTime());		    	
			}		    
			else {
				// assume this is an initial query with no time params specified ...
				sb.append(" and TRADE_QUOTE_TIME <= "+MARKET_CLOSE_TIME);
			}		    
		}
		// if no bid / ask market center are present AND bbo only is false AND bid or ask price filters are present, 
		// we need include it in the query to ensure the proper index is picked up ...
		if (criteria.getExchange() == null && 
				!criteria.getDisplay().equals(TSQCriteriaBean.TSQ_NASD_BASIC) && 
				!criteria.getDisplay().equals(TSQCriteriaBean.TQ_NASD_BASIC) &&
				criteria.isBboOnly() == false  &&
				criteria.getQuoteFromPrice() != null) {
			sb.append(" and    BID_MARKET_CENTER <> ''");
			sb.append(" and    ASK_MARKET_CENTER <> ''");			
			forceRangeIndex.noForceIndex();						
		}
		// build bid / ask quote pricing predicate ...
		if (criteria.getQuoteFromPrice() != null && criteria.getQuotePriceFilterType() != null) {
			if (criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_BID) || 
					criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_BID_AND_ASK)) {
				sb.append(" and    BID_PRICE >= " + criteria.getQuoteFromPrice());
				forceRangeIndex.noForceIndex();	
				if (criteria.getQuoteToPrice() != null) {
					sb.append(" and    BID_PRICE <= " + criteria.getQuoteToPrice());
				}
			}
			if (criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_ASK) || 
					criteria.getQuotePriceFilterType().equals(TSQCriteriaBean.QUOTE_PRICE_FILTER_TYPE_BID_AND_ASK)) {
				sb.append(" and    ASK_PRICE >= " + criteria.getQuoteFromPrice());
				forceRangeIndex.noForceIndex();	
				if (criteria.getQuoteToPrice() != null) {
					sb.append(" and    ASK_PRICE <= " + criteria.getQuoteToPrice());
				}
			}		
		}		
		if (orderByDesc)
			sb.append(ORDER_BY_MSG_SEQUENCE_DESC);
		else
			sb.append(ORDER_BY_MSG_SEQUENCE_ASC);
		sb.append(" LIMIT " + MAX_ROWS + " ");
		return sb.toString();
	}

	private static String buildVWAPPredicate(TSQCriteriaBean criteria) {
		StringBuffer sb = new StringBuffer(256);
		if (criteria.getExchange() != null) {
			sb.append(" and    TRADE_MARKET_CENTER = '" + criteria.getExchange() + "'");
		}
		if (criteria.getFromTime() != null && criteria.getToTime() != null) {
			sb.append(" and TRADE_QUOTE_TIME between "+criteria.getFromTime()+" and "+criteria.getToTime());		    	
		}		    
		else {
			// assume this is an initial query with no time params specified ...
			sb.append(" and TRADE_QUOTE_TIME <= "+MARKET_CLOSE_TIME);
		}
		if (criteria.getFromPrice() != null)
			sb.append(" and    TRADE_PRICE >= " + criteria.getFromPrice());
		if (criteria.getFromPrice() != null && criteria.getToPrice() != null)
			sb.append(" and    TRADE_PRICE <= " + criteria.getToPrice());
		if (criteria.getFromSize() != null)
			sb.append(" and    TRADE_SIZE >= " + criteria.getFromSize());
		if (criteria.getFromSize() != null && criteria.getToSize() != null)
			sb.append(" and    TRADE_SIZE <= " + criteria.getToSize());
		sb.append(" and    INCL_IN_VWAP = " + TSQBean.INCLUDE_IN_VWAP);
		// To remove dulpicate count of Opening price for Nasdaq symbols
		sb.append(" AND NOT (TRADE_QUOTE_COND_CODE1 LIKE '%17%') ");
		return sb.toString();
	}

	private static String buildXVWAPPredicate(TSQCriteriaBean criteria) {
		StringBuffer sb = new StringBuffer(256);
		sb.append(" and x.TICKER = t.TICKER ");
		sb.append(" and x.TRADE_SEQUENCE = t.TRADE_SEQUENCE ");
		sb.append(" and x.TRADE_MARKET_CENTER = t.TRADE_MARKET_CENTER ");	
		if (criteria.getFromTime() != null && criteria.getToTime() != null) {
			sb.append(" and t.TRADE_QUOTE_TIME between "+criteria.getFromTime()+" and "+criteria.getToTime());		    	
		}		    
		else {
			// assume this is an initial query with no time params specified ...
			sb.append(" and t.TRADE_QUOTE_TIME <= "+MARKET_CLOSE_TIME);
		}
		if (criteria.getFromPrice() != null)
			sb.append(" and    t.TRADE_PRICE >= " + criteria.getFromPrice());
		if (criteria.getFromPrice() != null && criteria.getToPrice() != null)
			sb.append(" and    t.TRADE_PRICE <= " + criteria.getToPrice());
		if (criteria.getFromSize() != null)
			sb.append(" and    t.TRADE_SIZE >= " + criteria.getFromSize());
		if (criteria.getFromSize() != null && criteria.getToSize() != null)
			sb.append(" and    t.TRADE_SIZE <= " + criteria.getToSize());
		if (criteria.getExchange() != null)
			sb.append(" and    t.TRADE_MARKET_CENTER = '" + criteria.getExchange() + "'");
		return sb.toString();
	}

	private static String buildTradeAndQuoteQueryString(TSQCriteriaBean criteria) {
		String dailyTableName = TSQTableMapper.findTableName(criteria.getSymbol(), criteria.getDay());
		StringBuffer sb = new StringBuffer(512);
		sb.append("(");
		String tradeSelectStatement = createSelectStatement(TSQCriteriaBean.TS,
				criteria.getSymbol(), dailyTableName, criteria.getDay()).toString();
		TSQIndex forceRangeIndex = new TSQIndex();
		String tradePredicate = buildTradePredicate(criteria, forceRangeIndex);
		if (forceRangeIndex.useForceIndex()) {
			tradeSelectStatement = tradeSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,FORCE_RANGE_INDEX);			
		}
		else {
			tradeSelectStatement = tradeSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,NO_FORCE_RANGE_INDEX);
		}
		sb.append(tradeSelectStatement);
		sb.append(tradePredicate);
		sb.append(") UNION (");
		String quoteSelectStatement = createSelectStatement(TSQCriteriaBean.TQ,
				criteria.getSymbol(), dailyTableName, criteria.getDay()).toString();
		String quotePredicate = buildQuotePredicate(criteria, forceRangeIndex);
		if (forceRangeIndex.useForceIndex()) {
			quoteSelectStatement = quoteSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,FORCE_RANGE_INDEX);			
		}
		else {
			quoteSelectStatement = quoteSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,NO_FORCE_RANGE_INDEX);
		}		
		sb.append(quoteSelectStatement);
		sb.append(quotePredicate);
		sb.append(") ");
		boolean orderByDesc = true;
		if (criteria.getFromMessageSequence() != null
				&& criteria.getPageDirection() != null) {
			if (criteria.getPageDirection()
					.equals(TSQCriteriaBean.PAGE_FORWARD)) {
				orderByDesc = false;
			}
		}
		if (orderByDesc)
			sb.append(ORDER_BY_MSG_SEQUENCE_DESC);
		else
			sb.append(ORDER_BY_MSG_SEQUENCE_ASC);
		sb.append(" LIMIT " + MAX_ROWS + " ");
		System.out.println("Trade and Quote query string : " + sb.toString());
		return sb.toString();
	}

	private static String buildMarketMakerQueryString(TSQCriteriaBean criteria) {
		StringBuffer sb = new StringBuffer();
		String mmDay = "0";
		try {
			mmDay = String.valueOf(Math.abs(criteria.getDay().intValue()));
		}
		catch (Exception e) {
			Logger.log("TSQMessageSelector.buildMarketMakerQuery - unable to parse absolute value of selection day : "+criteria.getDay());
		}
		sb.append(" select TICKER, ");
		sb.append("        MSG_SEQUENCE, ");
		sb.append("        MMID, ");
		sb.append("        QUOTE_TIME, ");
		sb.append("        DATE_CREATED, ");
		sb.append("        BID_PRICE, ");
		sb.append("        ASK_PRICE, ");
		sb.append("        BID_SIZE, ");
		sb.append("        ASK_SIZE ");
		sb.append(" from   TSQ_DAY_"+mmDay+"_MARKET_MAKERS ");	
		sb.append(" where  TICKER = " + getHashCode(criteria.getSymbol(),criteria.getDay()));
		if (criteria.getMarketMakerId() != null) {
			if (criteria.getMarketMakerId().equals(NON_REG_MARKET_MAKERS)) {
				sb.append(REGIONAL_MARKET_MAKER_EXCLUSIONS);
			}
			if (!criteria.getMarketMakerId().equals(ALL_MARKET_MAKERS) && !criteria.getMarketMakerId().equals(NON_REG_MARKET_MAKERS)) {
				sb.append(" and    MMID = '" + criteria.getMarketMakerId()+ "'");
			}
		}
		boolean orderByDesc = true;
		boolean useMessageTimes = true;
		// for client side paging!
		if (criteria.getFromMessageSequence() != null
				&& criteria.getPageDirection() != null) {
			if (criteria.getPageDirection().equals(
					TSQCriteriaBean.PAGE_BACKWARD)) {
				String startTime = criteria.getFromTime() != null ? criteria.getFromTime() : MKT_MAKER_MARKET_OPEN_TIME;
				sb.append(" and  QUOTE_TIME between "+startTime+" and "+criteria.getFromMessageSequenceTime());
				sb.append(" and  MSG_SEQUENCE < "
						+ criteria.getFromMessageSequence());
				useMessageTimes = false;
			} else if (criteria.getPageDirection().equals(
					TSQCriteriaBean.PAGE_FORWARD)) {
				String endTime = criteria.getToTime() != null ? criteria.getToTime() : MKT_MAKER_MARKET_CLOSE_TIME;
				sb.append(" and  QUOTE_TIME between "+criteria.getFromMessageSequenceTime()+" and "+endTime);
				sb.append(" and  MSG_SEQUENCE > "
						+ criteria.getFromMessageSequence());
				orderByDesc = false;
				useMessageTimes = false;
			}
			else {
				Logger.log("TSQMessageSelector.buildMarketMakerQueryString() - invalid page direction : " + criteria.getPageDirection());
			}
		}		
		if (useMessageTimes) {
			if (criteria.getFromTime() != null && criteria.getToTime() != null) {
				sb.append(" and QUOTE_TIME between "+criteria.getFromTime()+" and "+criteria.getToTime());		    	
			}		    
			else {
				// assume this is an initial query with no time params specified ...
				sb.append(" and QUOTE_TIME <= "+MKT_MAKER_MARKET_CLOSE_TIME);
			}		    
		}
		if (orderByDesc)
			sb.append(" ORDER BY TICKER DESC, QUOTE_TIME DESC, MSG_SEQUENCE DESC ");
		else
			sb.append(" ORDER BY TICKER ASC, QUOTE_TIME ASC, MSG_SEQUENCE ASC ");
		sb.append(" LIMIT " + MAX_ROWS + " ");
		String queryString = sb.toString();
		System.out.println("Market Maker Query String : "+queryString);
		return queryString;
	}

	private static String buildQuoteQueryString(TSQCriteriaBean criteria) {
		String dailyTableName = TSQTableMapper.findTableName(criteria.getSymbol(), criteria.getDay());
		StringBuffer sb = new StringBuffer(512);
		String quoteSelectStatement = createSelectStatement(TSQCriteriaBean.TQ,
				criteria.getSymbol(), dailyTableName, criteria.getDay()).toString();
		TSQIndex forceRangeIndex = new TSQIndex();
		String quotePredicate = buildQuotePredicate(criteria, forceRangeIndex);
		if (forceRangeIndex.useForceIndex()) {
			quoteSelectStatement = quoteSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,FORCE_RANGE_INDEX);			
		}
		else {
			quoteSelectStatement = quoteSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,NO_FORCE_RANGE_INDEX);
		}
		sb.append(quoteSelectStatement);
		sb.append(quotePredicate);
		System.out.println("Quote query string : " + sb.toString());
		return sb.toString();
	}

	private static String buildTradeQueryString(TSQCriteriaBean criteria) {
		String dailyTableName = TSQTableMapper.findTableName(criteria.getSymbol(), criteria.getDay());
		StringBuffer sb = new StringBuffer(512);
		String tradeSelectStatement = createSelectStatement(TSQCriteriaBean.TS,
				criteria.getSymbol(), dailyTableName, criteria.getDay()).toString();
		TSQIndex forceRangeIndex = new TSQIndex();
		String tradePredicate = buildTradePredicate(criteria,forceRangeIndex);
		if (forceRangeIndex.useForceIndex()) {
			tradeSelectStatement = tradeSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,FORCE_RANGE_INDEX);			
		}
		else {
			tradeSelectStatement = tradeSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,NO_FORCE_RANGE_INDEX);
		}
		sb.append(tradeSelectStatement);
		sb.append(tradePredicate);
		System.out.println("Trade query string : " + sb.toString());
		return sb.toString();
	}

	private static String buildCancelledQueryString(TSQCriteriaBean criteria) {
		String cancelTableName = TSQTableMapper.findCancelledTableName(criteria.getDay());
		StringBuffer sb = new StringBuffer(256);
		// kluge - but all cancelled messages should relate to trades!
		String cancelSelectStatement = createSelectStatement(TSQCriteriaBean.TS, criteria
				.getSymbol(), cancelTableName, criteria.getDay()).toString();
		cancelSelectStatement = cancelSelectStatement.replaceFirst(RANGE_INDEX_REPLACEMENT_TOKEN,NO_FORCE_RANGE_INDEX);
		sb.append(cancelSelectStatement);
		sb.append(ORDER_BY_MSG_SEQUENCE_DESC);
		return sb.toString();
	}

	private static String buildVWAPQueryString(TSQCriteriaBean criteria) {
		String dailyTableName = TSQTableMapper.findTableName(criteria.getSymbol(), criteria.getDay());
		StringBuffer sb = createVWAPSelectStatement(TSQCriteriaBean.TS, criteria.getSymbol(), dailyTableName, criteria.getDay() );
		sb.append(buildVWAPPredicate(criteria));
		System.out.println("VWAP query string : " + sb.toString());
		return sb.toString();
	}

	private static String buildXVWAPQueryString(TSQCriteriaBean criteria) {
		String dailyTableName = TSQTableMapper.findTableName(criteria.getSymbol(), criteria.getDay());
		String cancelTableName = TSQTableMapper.findCancelledTableName(criteria.getDay());
		StringBuffer sb = createXVWAPSelectStatement(TSQCriteriaBean.TS,
				criteria.getSymbol(), dailyTableName, cancelTableName, criteria.getDay());
		sb.append(buildXVWAPPredicate(criteria));
		System.out.println("Excluded VWAP query string : " + sb.toString());
		return sb.toString();
	}

	private static Long getHashCode(String ticker,  int day)
	{
		char[] arr = ticker.toCharArray();
		StringBuffer buff = new StringBuffer();
		if(ticker.endsWith(TORONTO_TSX_SUFFIX))
		{
			int size = arr.length -2;
			for(int i=0;i<size;i++)
			{
				buff.append( ((int)arr[i]) %100 );
			}
			buff.append(32);	//using space ascii value to control overflow
		}
		else if(ticker.endsWith(VENTURE_TSX_SUFFIX))
		{
			int size = arr.length -2;
			for(int i=0;i<size;i++)
			{
				buff.append( ((int)arr[i]) %100 );
			}
			buff.append(33);	//using ! ascii value to control overflow
		}
		else
		{
			for(int i=0;i<arr.length;i++)
			{
				buff.append( ((int)arr[i]) %100 );
			}
		}
		return Long.parseLong(buff.toString());
	}
}
