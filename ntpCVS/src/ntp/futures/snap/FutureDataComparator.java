package ntp.futures.snap;

import java.util.Comparator;

import com.b4utrade.bean.StockOptionBean;

public class FutureDataComparator implements Comparator<StockOptionBean> {
	
	public int compare(StockOptionBean dq1, StockOptionBean dq2) {
		String ticker1 = dq1.getTicker();
		String ticker2 = dq2.getTicker();
		String type1 = "";
		if (ticker1.indexOf('.') != -1)
		{
			type1 = ticker1.substring(ticker1.lastIndexOf('.'));
			ticker1 = ticker1.substring(0,ticker1.lastIndexOf('.'));
		}
		if (ticker2.indexOf('.') != -1)
		{
			ticker2 = ticker2.substring(0,ticker2.lastIndexOf('.'));
		}
		String contract1 = ticker1.substring(ticker1.length() - 3);
		String contract2 = ticker2.substring(ticker1.length() - 3);
		char month1 = contract1.charAt(0);
		char month2 = contract2.charAt(0);
		String year1 = contract1.substring(1);
		String year2 = contract2.substring(1);
		int value = year1.compareTo(year2);
		if(value!=0) {
			return value;
		}else {
			if(month1>month2) {
				return 1;
			}else if(month1<month2){
				return -1;
			}else {
				if(type1.indexOf('.') == -1)  //F2:
				{
					return 1;
				}
				else if(type1.indexOf(".C") != -1) { //F:
					return -1;
				}else  {
					return 0;
				}
			}
		}

	}

}
