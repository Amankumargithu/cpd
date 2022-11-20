package com.b4utrade.tsq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;



public class TSQTradeMessageLoader implements Runnable {

	
	public final static String CONNECTION_NAME="MySQL";
    public final static String TRADE_DATA_DIRECTORY = "/home/datafeed/tsqtradedata/";	 
	public final static String TSQ_FILE_MASK = ".C";
	 
	private String directory = null;
	
	private Statement stmt;
	private Connection con;
	 
	public static void main(String[] args) {
		
		TSQTradeMessageLoader tml = new TSQTradeMessageLoader();
		tml.run();		
	}
	
	public void run() {			
		
		try {

			try {
			    con = com.tacpoint.dataconnection.DBConnectionManager.getInstance().getConnection(TSQTradeMessageLoader.CONNECTION_NAME,1000);	
			    stmt = con.createStatement();
			}
			catch (Exception e) {
				Logger.log("TSQTradeMessageLoader - exception encountered : "+e.getMessage(),e);
				return;
			}
			
			directory = Environment.get("TRADE_DATA_DIRECTORY");
			if (directory == null)
				directory = TRADE_DATA_DIRECTORY;
			
		
			while (true) {
		
				// search for files ready for insertion ...
				File f = new File(directory);
				String[] files =  f.list();
				if (files == null) {
					Thread.sleep(1000);
					System.out.println("TSQTradeMessageLoader - no files found in directory "+directory);
					continue;
				}
				
				List completedFiles = sort(files);
				
				Iterator it = completedFiles.iterator();
				while (it.hasNext()) {
					processFileContents((String)it.next());					
				}
				
			}		
		}
		catch (Exception e) {
			Logger.log("TSQTradeMessageLoader - exception encountered : "+e.getMessage(),e);
			
		}
		

	}
	
	private List sort(String[] files) {		
		// we only want to sort files which are complete ...
		ArrayList completedFiles = new ArrayList();
		
		for (int i=0; i<files.length; i++) {
			if (files[i].indexOf(TSQ_FILE_MASK) > 0)
				completedFiles.add(files[i]);
		}
		
		Collections.sort(completedFiles, new TSQFilenameComparator());
		return completedFiles;		
	}
	

	
	private void processFileContents(String filename) {
		
		try {			
			
			File file = new File(directory+filename);
			BufferedReader reader = new BufferedReader(new FileReader(directory+filename));		
			
			//long startTime = System.currentTimeMillis();
			while (true) {
				String record = reader.readLine();
				if (record == null) break;	
				
				updateTradeData(record);
			}
			
			reader.close();
			file.delete();
			
			//System.out.println(new Date()+"TSQTradeMessageLoader - finished processing file : "+directory+filename+" in "+(System.currentTimeMillis()-startTime)+" millis");
			
		}
		catch (Exception e) {
			Logger.log("TSQTradeMessageLoader.processFileContents exception encountered : "+e.getMessage(),e);
		}
	}
	
	private void updateTradeData(String record) {

		try {

			stmt.addBatch(record);
			
			long beginTime = System.currentTimeMillis();
            stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            
            System.out.println(new Date()+"TSQTradeMessageLoader - time to insert batch : "+(endTime-beginTime));
            
            stmt.clearBatch();
		    
		}
		catch (Exception e) {
			Logger.log("TSQTradeMessageLoader.updateTradeData - exception encountered : "+e.getMessage(),e);	
			return;
		}	
	}

}

