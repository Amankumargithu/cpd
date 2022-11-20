package ntp.tsqdb.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

/*
 * This is testing class right now. Want to replace bestx process using this, but we have not done it yet
 */
public class TsqDbFileCombiner {

	public final static String TSQ_FILE_MASK = ".C";
	private String backupDirectory = null;
	private long fiveMin = 5*60*1000;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	boolean tradeStart = true;
	boolean quoteStart = true;
	long tradeStartTime = 0;
	long quoteStartTime = 0;
	
	public TsqDbFileCombiner()
	{
		CPDProperty prop = CPDProperty.getInstance();
		backupDirectory = prop.getProperty("BACKUP_TSQ_DATA_COMBINER_DIRECTORY");
		if (backupDirectory == null)
		{
			NTPLogger.missingProperty("BACKUP_TSQ_DATA_COMBINER_DIRECTORY");
			backupDirectory = "/home/datafeed/tsqDbCpd/backupDataFiles/";
			NTPLogger.defaultSetting("BACKUP_TSQ_DATA_DIRECTORY", backupDirectory);
		}
	}

	public void run() {
		try {
			while (true) {
				File f = new File(backupDirectory);
				String[] files = f.list();				
				if (files == null || files.length == 0) {
					Thread.sleep(300);
					NTPLogger.info("TsqDbFileCombiner - no files found in directory "+ backupDirectory);
					continue;
				}
				List<String> completedFiles = sort(files);
				if (completedFiles.size() != 0)
					NTPLogger.info("Number of files waiting for processing :"+ completedFiles.size());
				for(String fileName : completedFiles){
					if(fileName.contains("_q"))
						processQuotesFileContents(fileName);
					else
						processTradesFileContents(fileName);
				}
			}
		} catch (Exception e) {
			NTPLogger.info("TsqDbFileCombiner - exception encountered : "+ e.getMessage()+"   "+e);
		}
	}

	private void processTradesFileContents(String filename) {
		NTPLogger.info("Started processing file " + filename);
		String record=null;
		File tradesFile = null;
		RandomAccessFile toFile;
		FileChannel toChannel = null;
		long position = 0;
		long count = 0;
		
		try {	
			if(tradeStart){
				tradeStart = false;
				Date d1 = formatter.parse(filename.substring(3, filename.indexOf("_")));
				tradeStartTime = d1.getTime();
				tradesFile = createTempFile(filename);
				toFile = new RandomAccessFile(tradesFile.getAbsolutePath(), "rw");
				toChannel = toFile.getChannel();
				position = 0;
			}
			Date d2 = formatter.parse(filename.substring(3, filename.indexOf("_")));
			long endTime = d2.getTime();
			if(endTime -tradeStartTime > fiveMin){
				tradesFile = createTempFile(filename);
				tradeStartTime = endTime;
				toFile = new RandomAccessFile(tradesFile.getAbsolutePath(), "rw");
				toChannel = toFile.getChannel();
				position = count;
			}

			File file = new File(backupDirectory + filename);
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			count = fc.size();
			fc.transferTo(position, count, toChannel);
			fis.close();
//			file.delete();
			NTPLogger.info("TsqDbFileCombiner - finished processing file : "	+ backupDirectory + filename + " in "	+ (System.currentTimeMillis() - tradeStartTime) + " millis");
		} catch (Exception e) {
			NTPLogger.info(" For record : "+record+" TsqDbFileCombiner.processFileContents exception encountered : "+ e.getMessage()+"   "+ e);
		}
	}

	private void processQuotesFileContents(String filename) {
		NTPLogger.info("Started processing file " + filename);
		String record=null;
		File quotesFile = null;
		RandomAccessFile toFile = null;
		FileChannel toChannel = null;
		long position = 0;
		long count = 0;
		
		try {	
			if(quoteStart){
				quoteStart = false;
				Date d1 = formatter.parse(filename.substring(3, filename.indexOf("_")));
				quoteStartTime = d1.getTime();
				quotesFile = createTempFile(filename);
				toFile = new RandomAccessFile(quotesFile.getAbsolutePath(), "rw");
				toChannel = toFile.getChannel();
				position = 0;
			}
			Date d2 = formatter.parse(filename.substring(3, filename.indexOf("_")));
			
			long endTime = d2.getTime();
			System.out.println(quoteStartTime +" = quoteStartTime1  :  endTime = "+endTime);
			if(endTime -quoteStartTime > fiveMin){
				System.out.println(quoteStartTime +" = quoteStartTime5  :  endTime5 = "+endTime);
				quotesFile = createTempFile(filename);
				quoteStartTime = endTime;
				toChannel.close();
				toFile.close();
				toFile = new RandomAccessFile(quotesFile.getAbsolutePath(), "rw");
				toChannel = toFile.getChannel();
				position = count;
				
			}

			File file = new File(backupDirectory + filename);
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			count = fc.size();
			fc.transferTo(position, count, toChannel);
			fis.close();
			fc.close();
//			file.delete();
			NTPLogger.info("TsqDbFileCombiner - finished processing file : "	+ backupDirectory + filename + " in "	+ (System.currentTimeMillis() - quoteStartTime) + " millis");
		} catch (Exception e) {
			NTPLogger.info(" For record : "+record+" TsqDbFileCombiner.processFileContents exception encountered : "+ e.getMessage()+"   "+ e);
		}
	}

	private File createTempFile(String fileName) {
		File file = new File(backupDirectory + fileName.substring(0,fileName.indexOf(".")) + "_records.csv");
		return file;
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
		TsqDbFileCombiner t = new TsqDbFileCombiner();
		t.run();
	}
}
