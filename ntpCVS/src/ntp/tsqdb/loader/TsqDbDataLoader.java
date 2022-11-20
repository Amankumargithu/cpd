package ntp.tsqdb.loader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class TsqDbDataLoader{

	public final static String TSQ_FILE_MASK = ".C";
	private String dataDirectory = null;
	private String backupDirectory = null;
	private TsqDbBatchExecuter tsqbe = new TsqDbBatchExecuter();

	public TsqDbDataLoader()
	{
		CPDProperty prop = CPDProperty.getInstance();
		dataDirectory = prop.getProperty("TSQ_DATA_DIRECTORY");
		if (dataDirectory == null)
		{
			NTPLogger.missingProperty("TSQ_DATA_DIRECTORY");
			dataDirectory = "/home/datafeed/tsqDbCpd/dataFiles/";
			NTPLogger.defaultSetting("TSQ_DATA_DIRECTORY", dataDirectory);
		}
		backupDirectory = prop.getProperty("BACKUP_TSQ_DATA_DIRECTORY");
		if (backupDirectory == null)
		{
			NTPLogger.missingProperty("BACKUP_TSQ_DATA_DIRECTORY");
			backupDirectory = "/home/datafeed/tsqDbCpd/backupDataFiles/";
			NTPLogger.defaultSetting("BACKUP_TSQ_DATA_DIRECTORY", backupDirectory);
		}
	}

	public void run() {
		try {
			while (true) {
				File f = new File(dataDirectory);
				String[] files = f.list();				
				if (files == null || files.length == 0) {
					Thread.sleep(300);
					NTPLogger.info("TSQDBDataLoader - no files found in directory "+ dataDirectory);
					continue;
				}
				List<String> completedFiles = sort(files);
				if (completedFiles.size() != 0)
					NTPLogger.info("Number of files waiting for processing :"+ completedFiles.size());
				for(String fileName : completedFiles)
					processFileContents(fileName);
			}
		} catch (Exception e) {
			NTPLogger.info("TSQMessageLoader - exception encountered : "+ e.getMessage()+"   "+e);
		}
	}

	private void processFileContents(String filename) {
		long startTime = System.currentTimeMillis();
		boolean isQuotes = false;
		boolean isCancelTrade = false;
		if(filename.contains("_q"))
			isQuotes = true;
		if(filename.contains("_cxl"))
			isCancelTrade = true;		
		NTPLogger.info("Started processing file " + filename);
		String record=null;
		try {		
			File file = new File(dataDirectory + filename);
			FileInputStream fis = new FileInputStream(file);
		    FileChannel fc = fis.getChannel();
		    MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    byte[] buffer = new byte[(int)fc.size()];
		    mmb.get(buffer);		    
		    fis.close();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer)));
			Hashtable<String, ArrayList<String>> messages = new Hashtable<String, ArrayList<String>>();			
			while (true) 
			{
				record  = reader.readLine();
				if (record == null) // eof
					break;				
				if(record.indexOf(",")== -1) // empty line
					continue;
				
				String tickerStart = record.substring(0,2);
				String tablename = getTableName(tickerStart);
				if(isCancelTrade){
					updateRecord(tablename+"_T", record);
					tablename = "TSQ_DAY_0_X_T";
				}
				else{
				
				if(isQuotes)
					tablename = tablename+"_Q";
				else
					tablename = tablename +"_T";
				}				
				ArrayList<String> tableInserts = (ArrayList<String>)messages.get(tablename);
				if (tableInserts == null) {
					tableInserts = new ArrayList<String>();
					messages.put(tablename, tableInserts);
				}
				tableInserts.add(record);
			}
			reader.close();
			for(String table: messages.keySet())
			{
				ArrayList<String> inserts = messages.get(table);
				if (inserts.size() > 0)
				{
					File tempFile = createTempFile(inserts,table);
					if(isQuotes)
						tsqbe.insertQuoteRecords(tempFile, table);
					else
						tsqbe.insertTradeRecords(tempFile, table);
					tempFile.delete();
				}
			}
			// move file to backup directory
			file.renameTo(new File(backupDirectory+filename));	
			NTPLogger.info("TSQMessageLoader - finished processing file : "	+ dataDirectory + filename + " in "	+ (System.currentTimeMillis() - startTime) + " millis");
		} catch (Exception e) {
			NTPLogger.info(" For record : "+record+" TSQMessageLoader.processFileContents exception encountered : "+ e.getMessage()+"   "+ e);
		}
	}

	private void updateRecord(String tablename, String record) {
		String[] values = record.split(",");
		Long tradeId = Long.parseLong(values[2]);
		Long ticker = Long.parseLong(values[0]);
		tsqbe.updateTradeRecord(tablename, tradeId, ticker);
	}

	private File createTempFile(ArrayList<String> records, String tableName) {
		File file = new File(backupDirectory + tableName + "_records.csv");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(String record : records)
			{
				writer.append(record);
				writer.append("\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private String getTableName(String ticker){
		int bytes = Integer.parseInt(ticker);
		switch (bytes)
		{
		case 65:
			return "TSQ_DAY_0_1";
		case 66:
			return "TSQ_DAY_0_2";
		case 67:
			return "TSQ_DAY_0_3";
		case 68:
		case 69:
			return "TSQ_DAY_0_4";
		case 70:
		case 71:
			return "TSQ_DAY_0_5";
		case 72:
		case 73:
		case 74:
			return "TSQ_DAY_0_6";
		case 75:
		case 76:
			return "TSQ_DAY_0_7";
		case 77:
		case 78:
		case 79:
			return "TSQ_DAY_0_8";
		case 80:
		case 81:
		case 82:
			return "TSQ_DAY_0_9";
		case 83:
		case 84:
		case 85:
			return "TSQ_DAY_0_10";
		default:
			return "TSQ_DAY_0_11";
		}
	}
	private List<String> sort(String[] files) {
		ArrayList<String> completedFiles = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].indexOf(TSQ_FILE_MASK) > 0)
				completedFiles.add(files[i]);
		}
		Collections.sort(completedFiles, new TSQFilenameComparator());
		return completedFiles;
	}

	public static void main(String[] args)
	{
		TsqDbDataLoader t = new TsqDbDataLoader();
		t.run();
	}
}