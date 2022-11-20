package ntp.options.chain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import com.csvreader.CsvReader;
import com.quodd.common.logger.*;
import com.quodd.common.util.QuoddProperty;
import com.quodd.db.DatabaseConnectionManager;

public class OptionsChainDistributor {

	public static final Logger logger = QuoddLogger.getInstance("optionChain").getLogger();
	public static final QuoddProperty dbProperties = new QuoddProperty("/db.properties");
	private static final QuoddProperty optionChainProperties = new QuoddProperty("/opchain.properties");
	private static HashMap<String, String> underlyerMap = new HashMap<>();
	private static HashMap<String, String> mappingMonth = new HashMap<>();
	private static SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
	private static SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static Date currentDate = new Date();
	private static Connection aConnection = null;
	private static PreparedStatement prestmt = null;
    private static DatabaseConnectionManager db;
    private static long millisecInDay = 24 * 60 * 60 * 1000;
	private static long maxDateSupported = 2147365800000l; // equivalent to 2038-01-18 ,max year supported by timestamp


	static {
		mappingMonth.put("A", "01"); // call month jan
		mappingMonth.put("B", "02");
		mappingMonth.put("C", "03");
		mappingMonth.put("D", "04");
		mappingMonth.put("E", "05");
		mappingMonth.put("F", "06");
		mappingMonth.put("G", "07");
		mappingMonth.put("H", "08");
		mappingMonth.put("I", "09");
		mappingMonth.put("J", "10");
		mappingMonth.put("K", "11");
		mappingMonth.put("L", "12");
		mappingMonth.put("M", "01"); // put month jan ... so on
		mappingMonth.put("N", "02");
		mappingMonth.put("O", "03");
		mappingMonth.put("P", "04");
		mappingMonth.put("Q", "05");
		mappingMonth.put("R", "06");
		mappingMonth.put("S", "07");
		mappingMonth.put("T", "08");
		mappingMonth.put("U", "09");
		mappingMonth.put("V", "10");
		mappingMonth.put("W", "11");
		mappingMonth.put("X", "12");
	}

	public static void main(String[] args) {
		try {
			HashMap<String, Long> preTradeMap = new HashMap<>();
			HashMap<String, Long> preVolMap = new HashMap<>();
			String currentDateStr = dateformatter.format(new Date());
			db =  new DatabaseConnectionManager( dbProperties);
			String fileDirectory = optionChainProperties.getProperty("FILE_DIRECTORY");
			if (fileDirectory == null) {
				CommonLogMessage.missingProperty("FILE_DIRECTORY");
				fileDirectory = "/home/opChain/files";
				CommonLogMessage.defaultSetting("FILE_DIRECTORY", fileDirectory);
			}
			String optionsFile = fileDirectory + "/OptionChain_" + currentDateStr + ".csv";
			String propOptionsFile =optionChainProperties .getProperty("OPTIONS_DATA_FILE");
			if (propOptionsFile != null) {
				optionsFile = propOptionsFile;
				logger.warning("Proeperty file Override Option Chain file to " + propOptionsFile);
			}

			String preFileDirectory = optionChainProperties.getProperty("PRE_FILE_DIRECTORY");
			if (preFileDirectory == null) {
				CommonLogMessage.missingProperty("PRE_FILE_DIRECTORY");
				preFileDirectory = "/home/opChain/files";
				CommonLogMessage.defaultSetting("PRE_FILE_DIRECTORY", preFileDirectory);
			}
			String preOptionsFile = preFileDirectory + "/OptionChain_" + currentDateStr + ".csv";
			String propPreOptionsFile = dbProperties.getProperty("PRE_OPTIONS_DATA_FILE");
			if (propPreOptionsFile != null) {
				preOptionsFile = propPreOptionsFile;
				logger.warning("Property file Override Pre Option Chain file to " + propPreOptionsFile);
			}
			String underLyerFile = fileDirectory + "/underlyers.opt_" + currentDateStr;
			String propUnderLyerFile = dbProperties.getProperty("UNDERLYER_FILE");
			if (propUnderLyerFile != null) {
				underLyerFile = propUnderLyerFile;
				logger.warning("Property file Override Underlyer file to " + propUnderLyerFile);
			}
			String recordDate = dbProperties.getProperty("RECORD_DATE");
			if (recordDate == null) {
				CommonLogMessage.missingProperty("RECORD_DATE");
				recordDate = dateformatter.format(currentDate);
				currentDate = dateformatter.parse(recordDate);
				CommonLogMessage.defaultSetting("RECORD_DATE", recordDate);
			} else {
				currentDate = dateformatter.parse(recordDate);
			}
			CsvReader underlyerReader = new CsvReader(underLyerFile);
			while (underlyerReader.readRecord())
				underlyerMap.put(underlyerReader.get(0), underlyerReader.get(1));
			underlyerReader.close();
			logger.finer("Total underlyers: " + underlyerMap.size());
			
			aConnection = db.getConnection();
			Statement stmt = aConnection.createStatement();
			try {
				logger.fine("Dropping Index OPTIONS_CHAIN.UNDERLYER_INDEX start");
				stmt.executeUpdate("ALTER TABLE OPTIONS_CHAIN DROP INDEX UNDERLYER_INDEX");
				logger.finer("Dropping Index OPTIONS_CHAIN.UNDERLYER_INDEX end");
			} catch (Exception e) {
				logger.warning("Cannot drop UNDERLYER_INDEX" + e.getMessage());
			}
			try {
				logger.fine("Dropping Index OPTIONS_CHAIN.RECORD_DATE_INDEX start");
				stmt.executeUpdate("ALTER TABLE OPTIONS_CHAIN DROP INDEX RECORD_DATE_INDEX");
				logger.finer("Dropping Index OPTIONS_CHAIN.RECORD_DATE_INDEX end");
			} catch (Exception e) {
				logger.warning("Cannot drop RECORD_DATE_INDEX"+e.getMessage());
			}
			
			CsvReader preOptionsReader = new CsvReader(preOptionsFile);
			preOptionsReader.readHeaders();
			while (preOptionsReader.readRecord()) {
				try {
					String opTick = preOptionsReader.get(0);
					if (!opTick.startsWith("P:"))
						continue;
					if (opTick.contains("/"))
						continue;
					opTick = opTick.replace("P:", "O:");
					Long nTrd = Long.parseLong(preOptionsReader.get(1));
					Long vol = Long.parseLong(preOptionsReader.get(2));
					if (nTrd != null && nTrd != 0 && vol != null && vol != 0) {
						System.out.println("ADD " + opTick);
						preTradeMap.put(opTick, nTrd);
						preVolMap.put(opTick, vol);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			preOptionsReader.close();
			prestmt = aConnection.prepareStatement(
					"INSERT INTO OPTIONS_CHAIN (TICKER, UNDERLYER, NUMBER_TRADES, VOLUME, TRADE_TIME, RECORD_DATE ) VALUES (?,?,?,?,?,?)");
			CsvReader optionsReader = new CsvReader(optionsFile);
			optionsReader.readHeaders();
			while (optionsReader.readRecord()) {
				String opTick = optionsReader.get(0);
				if (!opTick.startsWith("O:"))
					continue;
				if (opTick.contains("/"))
					continue;
				String nTrd = optionsReader.get(1);
			
				String vol = optionsReader.get(2);
				String time = optionsReader.get(3);
				String[] arr = opTick.split("\\\\");
				Date dt = null;
				try {
					dt = timeFormatter.parse(time);
				} catch (Exception e) {
					dt = null;
				}
				long t = dt == null ? 0 : dt.getTime();
				if (validateOptionTicker(arr[1])) {
					String root = arr[0].substring(2);
					String underlyer = underlyerMap.get(root);
					if (underlyer == null) {
						logger.warning("No underlyer for " + root);
						underlyer = root;
					}
					long preVol = 0;
					long preTrd = 0;
					if (preTradeMap.containsKey(opTick)) {
						preTrd = preTradeMap.get(opTick);
						preVol = preVolMap.get(opTick);
						System.out.println("PRE " + opTick + " " + preTrd + " " + preVol);
					}
					prestmt.setString(1, opTick);
					prestmt.setString(2, underlyer);
					prestmt.setLong(3, Long.parseLong(nTrd) + preTrd);
					prestmt.setLong(4, Long.parseLong(vol) + preVol);
                         if (dt == null || t > maxDateSupported) {
						t = 0;
						prestmt.setTimestamp(5, new java.sql.Timestamp(t + millisecInDay));
					} else {
						prestmt.setTimestamp(5, new java.sql.Timestamp(t));
					}
                     prestmt.setString(6, recordDate);
					prestmt.execute();
				} else
					CommonLogMessage.dropSymbol(opTick, "Already Expired");
			}
			try {
				logger.fine("Create Index OPTIONS_CHAIN.RECORD_DATE_INDEX start");
				stmt.executeUpdate("CREATE INDEX RECORD_DATE_INDEX ON OPTIONS_CHAIN(RECORD_DATE)");
				logger.finer("Create Index OPTIONS_CHAIN.RECORD_DATE_INDEX end");
			} catch (Exception e) {
				logger.warning("Cannot create RECORD_DATE_INDEX");
				e.printStackTrace();
			}
			try {
				logger.fine("Create Index OPTIONS_CHAIN.UNDERLYER_INDEX start");
				stmt.executeUpdate("CREATE INDEX UNDERLYER_INDEX ON OPTIONS_CHAIN(UNDERLYER)");
				logger.finer("Create Index OPTIONS_CHAIN.UNDERLYER_INDEX end");
			} catch (Exception e) {
				logger.warning("Cannot create UNDERLYER_INDEX");
				e.printStackTrace();
			}
			optionsReader.close();
			prestmt.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.finest("Process Completed");
	}

	private static boolean validateOptionTicker(String dateTime) {
		try {
			String year = dateTime.substring(0, 2);
			char month = dateTime.charAt(2);
			String mon = mappingMonth.get("" + month);
			String date = dateTime.substring(3);
			String expireDate = date + mon + year; 
			Date expired = formatter.parse(expireDate);
			if (expired.after(currentDate))
				return true;
			else if (expired.before(currentDate))
				return false;
			else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
