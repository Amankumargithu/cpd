package ntp.streetsoft.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import ntp.bean.BillDataBean;
import ntp.logger.NTPLogger;
import ntp.streetsoft.util.StreetSoftConstants;

public class StreetSoftwareIntrestRateParser {

	public  HashMap<String, ArrayList<BillDataBean>> parseTreasuryData(String fileName){
		Properties prop = new Properties ();
		HashMap<String, ArrayList<BillDataBean>> dataMap = new HashMap<String, ArrayList<BillDataBean>>();
		FileInputStream stream = null;
		try {
			File file = new File(fileName);
			stream = new FileInputStream(file);
			prop.load(stream);
			String billData = prop.getProperty(StreetSoftConstants.BILL);
			ArrayList<BillDataBean> billDataList = new ArrayList<BillDataBean>();
			StringTokenizer stringTokenizer = new  StringTokenizer(billData,"|");
			while (stringTokenizer.hasMoreTokens()) {
				String billdataTokens = stringTokenizer.nextToken();
				StringTokenizer stringTokenizer2 = new StringTokenizer(billdataTokens, ",");
				while(stringTokenizer2.hasMoreElements()){     
					BillDataBean billDataBean = new BillDataBean();
					billDataBean.setIssue(stringTokenizer2.nextToken().trim());
					stringTokenizer2.nextToken(); // skip Maturity
					billDataBean.setPrice(Double.parseDouble(stringTokenizer2.nextToken().trim()));
					billDataBean.setYield(Double.parseDouble(stringTokenizer2.nextToken().trim()));
					billDataList.add(billDataBean);
					break;
				}
			}
			dataMap.put(StreetSoftConstants.BILL, billDataList);
		}
		catch (FileNotFoundException e) {
			NTPLogger.error("StreetSoftwareIntrestRateParser - file not found - " + fileName );
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if (stream != null)
			{
				try 
				{
					stream.close();
				} 
				catch (Exception e2) 
				{
				}
			}
		}
		return dataMap;
	}
}
