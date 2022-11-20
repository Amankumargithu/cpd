package ntp.eod.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.csvreader.CsvReader;

public class CSVFileComparator {

	public ConcurrentHashMap<String, Object> readAndCompare(String fileName1, String fileName2, int primaryColumn , Integer[] columnToCompare,boolean isNewIssue,int exchangeColumn){
		ConcurrentHashMap<String, String[]> map = new ConcurrentHashMap<String, String[]>();
		ConcurrentHashMap<String, String[]> map2 = new ConcurrentHashMap<String, String[]>();
		CsvReader reader;
		CsvReader reader2;
		String primaryRecord1=null;
		String primaryRecord2=null;
		String primaryColumnHeading="";
		ConcurrentHashMap<String, Object> resultMap=new ConcurrentHashMap<String, Object>();
		ConcurrentHashMap<String,String> symbolMap= new ConcurrentHashMap<String,String>();
		try {
			reader = new CsvReader(fileName1);
			
			reader.readRecord(); 
			String[] columnHeading=  new String[reader.getColumnCount()];	
			for(int columnCount=0;columnCount<columnHeading.length;columnCount++)
				  columnHeading[columnCount]=reader.get(columnCount);
			resultMap.put("COLUMN_HEADINGS", columnHeading);
			
			System.out.println("Files are : "+fileName1 + fileName2);
			if(isNewIssue||!new File(fileName2).exists())
			{   
				ConcurrentHashMap<String, String[]>addedSymbolMap= new ConcurrentHashMap<String, String[]>();
				while(reader.readRecord())
				{ 
					if(!(reader.get(primaryColumn)).trim().equals(""))
						addedSymbolMap.put(reader.get(primaryColumn), reader.getValues());
				  
				}
				
				addedSymbolMap.remove("");
				resultMap.put("NEW_ISSUES", addedSymbolMap);
				return resultMap;
			}
			
			reader2 = new CsvReader(fileName2);
			if(reader2.readRecord()==false)
				return null;
			if(reader.getColumnCount()!=reader2.getColumnCount())
			{
				System.out.println("Total column count for two files are differnt");
				resultMap.put("ERROR", "Total column count for two files are differnt");
				return resultMap;
			}
			else
			{
				for(int columnCount=0;columnCount<reader.getColumnCount();columnCount++)
				{
					if(!(reader.get(columnCount).equalsIgnoreCase(reader2.get(columnCount))))
					{
						System.out.println("Column "+columnCount+" heading is not same for comparing files , in File1 column heading is "+reader.get(columnCount)+" and in File2 column heading is "+reader2.get(columnCount));
						resultMap.put("ERROR", "Column "+columnCount+" heading is not same for comparing files , in File1 column heading is "+reader.get(columnCount)+" and in File2 column heading is "+reader2.get(columnCount));
						return resultMap;
					}
				}
			}
			
			primaryColumnHeading=reader.get(primaryColumn);

				
//			for(Integer column : columnToCompare)
//				compareColumnHeading[column]=reader.get(column);
			String exchange="";
			while(true)
			{  
				if (!reader.readRecord()&!reader2.readRecord())
					  break;
					exchange="";
					primaryRecord1=reader.get(primaryColumn).trim();
					primaryRecord2=reader2.get(primaryColumn).trim();
					if(exchangeColumn!=0)
						exchange=reader.get(exchangeColumn).trim();
					symbolMap.put(primaryRecord1, exchange);
					ConcurrentHashMap<String, String[]> cMap= null;
					if(primaryRecord1.equals(primaryRecord2))
					{   
							for(Integer column : columnToCompare)
							{
								if(column!=999&&!reader.get(column).equals(reader2.get(column)))
								{ 
									if(cMap==null)
										cMap=new ConcurrentHashMap<String, String[]>();
									String[] comparedValue=new String[2];
									comparedValue[0]=reader.get(column);
									comparedValue[1]=reader2.get(column);
								   cMap.put(columnHeading[column],comparedValue);
									System.out.println(columnHeading[column]+" column value is not same for "+primaryColumnHeading+" '"+primaryRecord1+"' , Compared values are : "+reader.get(column) +" and "+reader2.get(column));
								}
							}
	    					if(cMap!=null)
	    						resultMap.put(primaryRecord2, cMap);
					}
					else
					{
						map.put(primaryRecord1, reader.getValues());
						String[]record2;
						record2=map.get(primaryRecord2);
	                    if(record2==null) 	
	                    {
	                    	map2.put(primaryRecord2, reader2.getValues());
	                    	
	                    }
	                    else
	                    {   
	                    	for(Integer column : columnToCompare)
	                    	{
								if(column!=999&&!record2[column].equals(reader2.get(column)))
								{ 
									if(cMap==null)
										cMap=new ConcurrentHashMap<String, String[]>();
									String[] comparedValue=new String[2];
									comparedValue[0]=record2[column];
									comparedValue[1]=reader2.get(column);
									cMap.put(columnHeading[column],comparedValue);
									System.out.println(columnHeading[column]+" column value is not same for "+primaryColumnHeading +" '"+primaryRecord2+"' , Compared values are : "+record2[column] +" and "+reader2.get(column) );
								}
	                    	}
	                    	map.remove(primaryRecord2);
	    					if(cMap!=null)
	    						resultMap.put(primaryRecord2, cMap);
	                    }
					}
			}
			reader.close();
			reader2.close();
			
			ConcurrentHashMap<String, String[]>addedSymbolMap= new ConcurrentHashMap<String, String[]>();
			for (String key :map.keySet())
			{
				if(map2.get(key)==null)
				{   
					addedSymbolMap.put(key,map.get(key));
					continue;
				}
				
				String[] record1=map.get(key);
				String[] record2= map2.get(key);
            	ConcurrentHashMap<String, String[]> cMap= null;
            	for(Integer column : columnToCompare)
            	{
					if(column!=999&&!record1[column].equals(record2[column]))
					{  
						if(cMap==null)
							cMap=new ConcurrentHashMap<String, String[]>();
						String[] comparedValue=new String[2];
						comparedValue[0]=record1[column];
						comparedValue[1]=record2[column];
						cMap.put(columnHeading[column],comparedValue);
						System.out.println(columnHeading[column]+" column value is not same for "+primaryColumnHeading+" '"+key+"' , Compared values are : "+record1[column] +" and "+record2[column]);
					}
            	}
				if(cMap!=null)
					resultMap.put(key, cMap);
            	map2.remove(key);
				
			}
			addedSymbolMap.remove("");
			map2.remove("");
			resultMap.put("SYMBOL_ADDED",addedSymbolMap);
			resultMap.put("SYMBOL_REMOVED", map2.keySet());
			resultMap.put("SYMBOL_SET", symbolMap);
			System.out.println(primaryColumnHeading +"present in new  file are :"+addedSymbolMap.keySet());
			System.out.println(primaryColumnHeading +" not present in new  file are :"+map2.keySet());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
//		ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<ConcurrentHashMap<String, String>>();
//		list.add(map);
//		list.add(map2);
		return resultMap;
	}

	
	public ArrayList<ConcurrentHashMap<String, String>> readCsvFile(String fileName1, String fileName2, int primaryColumn , int columnToCompare){
		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
		ConcurrentHashMap<String, String> map2 = new ConcurrentHashMap<String, String>();
		CsvReader reader;
		CsvReader reader2;
		try {
			reader = new CsvReader(fileName1);
			reader2 = new CsvReader(fileName2);
			while(reader.readRecord())
			{
				if(reader2.readRecord() != false){
					if(reader.get(primaryColumn).equals(reader2.get(primaryColumn))){
						System.out.println(1);
						if(!reader.get(columnToCompare).equals(reader2.get(columnToCompare))){
							System.out.println("File Records are not same for ticker "+reader.get(primaryColumn)+" : "+reader.getRawRecord() +" and "+reader2.getRawRecord());
						}
					}
					else{
						map.put(reader.get(primaryColumn), reader.getRawRecord());
						map2.put(reader2.get(primaryColumn), reader2.getRawRecord());
					}
				}
				else{
					map.put(reader.get(primaryColumn), reader.getRawRecord());
				}
			}
			System.out.println(map.keySet().size());
			while(reader2.readRecord()){
				map2.put(reader2.get(primaryColumn), reader2.getRawRecord());
			}
			reader.close();
			reader2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<ConcurrentHashMap<String, String>>();
		list.add(map);
		list.add(map2);
		return list;
	}
	public void compareFiles(ArrayList<ConcurrentHashMap<String, String>> list, int columnToCompare){

		ConcurrentHashMap<String, String> file1 = list.get(0);
		ConcurrentHashMap<String, String> file2 = list.get(1);
		Set<String> keys = file1.keySet();

		for (String key : keys) {
			String record1 = file1.get(key);
			String record2 = file2.get(key);
			if(record2 != null)
			{
				String[] recordArray1 = record1.split(",");
				String[] recordArray2 = record2.split(",");
				if(!recordArray1[columnToCompare].equals(recordArray2[columnToCompare])){
					System.out.println("Records are not same for ticker "+key+" : "+record1 +" and "+record2);
				}
			}
			else
			{
				System.out.println("No record exists 1 for "+key+" in second file");
			}
		}

		Set<String> keys2 = file2.keySet();
		for (String key : keys2) {
			if(!file1.containsKey(key)){
				System.out.println("No record exists 2 for "+key+" in first file");
			}
		}
	}
	
	public ConcurrentHashMap<String, Object>readAndCompareSubset(String fileName1, String fileName2, int primaryColumn , Integer[] columnToCompare){
		ConcurrentHashMap<String, String[]> map = new ConcurrentHashMap<String, String[]>();
		ConcurrentHashMap<String, String[]> map2 = new ConcurrentHashMap<String, String[]>();
		CsvReader reader;
		CsvReader reader2;
		String primaryRecord1=null;
		String primaryRecord2=null;
		String primaryColumnHeading="";
		int columnCount=2;
		ConcurrentHashMap<String, Object> resultMap=new ConcurrentHashMap<String, Object>();
		try {
			reader = new CsvReader(fileName1);
			
			reader.readRecord(); 
			String[] columnHeading=  new String[reader.getColumnCount()];	
			for(int column:columnToCompare)
				  columnHeading[column]=reader.get(column);
			columnHeading[primaryColumn]=reader.get(primaryColumn);
			resultMap.put("COLUMN_HEADINGS", columnHeading);
			
			System.out.println("Files are : "+fileName1 + fileName2);
			reader2 = new CsvReader(fileName2);
			if(reader2.readRecord()==false)
				return null;
			if(columnToCompare.length!=(reader2.getColumnCount()-2))
			{
				System.out.println("Total column count for two files are differnt");
				resultMap.put("ERROR", "Total column count for two files are differnt");
				return resultMap;
			}
			else
			{
				 columnCount=2;
				for(int column:columnToCompare)
				{
					
					if(!(reader.get(column).equalsIgnoreCase(reader2.get(columnCount))))
					{
						System.out.println("Column "+columnCount+" heading is not same for comparing files , in File1 column heading is "+reader.get(columnCount)+" and in File2 column heading is "+reader2.get(columnCount));
						resultMap.put("ERROR", "Column "+columnCount+" heading is not same for comparing files , in File1 column heading is "+reader.get(columnCount)+" and in File2 column heading is "+reader2.get(columnCount));
						return resultMap;
					}
					columnCount++;
				}
			}
			
			primaryColumnHeading=reader.get(primaryColumn);
			
			while(true)
			{  
				if (!reader.readRecord()&!reader2.readRecord())
					  break;
					primaryRecord1=reader.get(primaryColumn).trim();
					primaryRecord2=reader2.get(primaryColumn).trim();
					ConcurrentHashMap<String, String[]> cMap= null;
					columnCount=2;
					if(primaryRecord1.equals(primaryRecord2))
					{   
						for(Integer column : columnToCompare)
						{
							if(!reader.get(column).equals(reader2.get(columnCount)))
							{ 
								if(cMap==null)
									cMap=new ConcurrentHashMap<String, String[]>();
								String[] comparedValue=new String[2];
								comparedValue[0]=reader.get(column);
								comparedValue[1]=reader2.get(columnCount);
								cMap.put(columnHeading[column],comparedValue);
								System.out.println(columnHeading[column]+" column value is not same for "+primaryColumnHeading+" '"+primaryRecord1+"' , Compared values are : "+reader.get(column) +" and "+reader2.get(columnCount));
							}
							columnCount++;
						}
    					if(cMap!=null)
    						resultMap.put(primaryRecord2, cMap);
					}
					else
					{
						map.put(primaryRecord1, reader.getValues());
						String[]record2;
						record2=map.get(primaryRecord2);
	                    if(record2==null) 	
	                    {
	                    	map2.put(primaryRecord2, reader2.getValues());
	                    	
	                    }
	                    else
	                    {   
	                    	for(Integer column : columnToCompare)
	                    	{
								if(!record2[column].equals(reader2.get(columnCount)))
								{ 
									if(cMap==null)
										cMap=new ConcurrentHashMap<String, String[]>();
									String[] comparedValue=new String[2];
									comparedValue[0]=record2[column];
									comparedValue[1]=reader2.get(columnCount);
									cMap.put(columnHeading[column],comparedValue);
									System.out.println(columnHeading[column]+" column value is not same for "+primaryColumnHeading +" '"+primaryRecord2+"' , Compared values are : "+record2[column] +" and "+reader2.get(columnCount) );
								}
								columnCount++;
	                    	}
	                    	map.remove(primaryRecord2);
	    					if(cMap!=null)
	    						resultMap.put(primaryRecord2, cMap);
	                    }
					}
			}
			reader.close();
			reader2.close();
			
			ConcurrentHashMap<String, String[]>addedSymbolMap= new ConcurrentHashMap<String, String[]>();
			for (String key :map.keySet())
			{
				if(map2.get(key)==null)
				{   
					addedSymbolMap.put(key,map.get(key));
					continue;
				}
				
				String[] record1=map.get(key);
				String[] record2= map2.get(key);
            	ConcurrentHashMap<String, String[]> cMap= null;
            	columnCount=2;
            	for(Integer column : columnToCompare)
            	{
					if(!record1[column].equals(record2[columnCount]))
					{  
						if(cMap==null)
							cMap=new ConcurrentHashMap<String, String[]>();
						String[] comparedValue=new String[2];
						comparedValue[0]=record1[column];
						comparedValue[1]=record2[columnCount];
						cMap.put(columnHeading[column],comparedValue);
						System.out.println(columnHeading[column]+" column value is not same for "+primaryColumnHeading+" '"+key+"' , Compared values are : "+record1[column] +" and "+record2[columnCount]);
					}
					columnCount++;
            	}
				if(cMap!=null)
					resultMap.put(key, cMap);
            	map2.remove(key);
				
			}
			addedSymbolMap.remove("");
			map2.remove("");
			resultMap.put("SYMBOL_ADDED",addedSymbolMap);
			resultMap.put("SYMBOL_REMOVED", map2.keySet());
			System.out.println(primaryColumnHeading +"present in new  file are :"+addedSymbolMap.keySet());
			System.out.println(primaryColumnHeading +" not present in new  file are :"+map2.keySet());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
//		ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<ConcurrentHashMap<String, String>>();
//		list.add(map);
//		list.add(map2);
		return resultMap;
	}
}