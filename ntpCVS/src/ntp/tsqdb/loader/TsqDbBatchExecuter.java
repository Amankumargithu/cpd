package ntp.tsqdb.loader;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import ntp.logger.NTPLogger;
import ntp.util.MySQLDBManager;

public class TsqDbBatchExecuter {

	private Connection con;

	public TsqDbBatchExecuter(){
		try {
			con = MySQLDBManager.getConnection();
		} catch (Exception e) {
			NTPLogger.info("TSQBatchExecutor- exception encountered : " + e.getMessage()+"   "+ e);
			return;
		}
	}

	public void insertTradeRecords(File file, String tablename)
	{
		try
		{
			String loadQuery = "LOAD DATA LOCAL INFILE '" + file + "' INTO TABLE "+tablename+" FIELDS TERMINATED BY ','" 
					+ " LINES TERMINATED BY '\n' (TICKER,MSG_SEQUENCE," +
					"TRADE_SEQUENCE,TRADE_CANCEL_IND,TRADE_QUOTE_TYPE," +
					"INCL_IN_VWAP,TRADE_QUOTE_COND_CODE1,TRADE_QUOTE_TIME," +
					"DATE_CREATED,TRADE_PRICE,VWAP,TRADE_SIZE," +
					"VOLUME,BID_PRICE,ASK_PRICE,BID_SIZE,ASK_SIZE," +
					"TRADE_ID,TRADE_MARKET_CENTER,BID_MARKET_CENTER,ASK_MARKET_CENTER,RTL) ";
			long beginTime = System.currentTimeMillis();
			Statement stmt = con.createStatement();
			stmt.execute(loadQuery);
			long endTime = System.currentTimeMillis();
			NTPLogger.info("TSQBE[" + tablename + "] - "+"  insert time : " + (endTime - beginTime));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertQuoteRecords(File file, String tablename)
	{
		try
		{
			String loadQuery = "LOAD DATA LOCAL INFILE '" + file + "' INTO TABLE "+tablename+" FIELDS TERMINATED BY ','" 
					+ " LINES TERMINATED BY '\n' (TICKER,MSG_SEQUENCE," +
					"TRADE_QUOTE_TYPE,INCL_IN_VWAP,TRADE_QUOTE_COND_CODE1," +
					"TRADE_QUOTE_TIME,DATE_CREATED,BID_PRICE," +
					"ASK_PRICE,BID_SIZE,ASK_SIZE," +
					"BID_MARKET_CENTER,ASK_MARKET_CENTER,RTL) ";
			long beginTime = System.currentTimeMillis();
			Statement stmt = con.createStatement();
			stmt.execute(loadQuery);
			long endTime = System.currentTimeMillis();
			NTPLogger.info("TSQBE[" + tablename + "] - "+"  insert time : " + (endTime - beginTime));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateTradeRecord(String tablename , Long trade_id , Long ticker){
		String updateQuery = "Update "+tablename +" set TRADE_CANCEL_IND = 0 where ticker = '" + ticker + "' AND trade_id = '"+trade_id + "'";
		NTPLogger.info("Update query is : "+updateQuery);
		Statement stmt;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(updateQuery);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}