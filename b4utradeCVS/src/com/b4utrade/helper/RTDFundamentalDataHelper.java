package com.b4utrade.helper;

import java.text.DecimalFormat;

public class RTDFundamentalDataHelper {

	public static final String TICKER ="TICKER";
	public static final String YRAGOPRC1 = "YRAGOPRC1";
	public static final String QTOTD2EQ = "QTOTD2EQ";
	public static final String QLTD2EQ = "QLTD2EQ" ;
	public static final String FLOAT = "FLOAT" ;
	public static final String DIVNQ = "DIVNQ";
	public static final String DRECDATE = "DRECDATE" ;
	public static final String QDPS = "QDPS";
	public static final String ADIVSHR = "ADIVSHR";
	public static final String TTMDIVSHR = "TTMDIVSHR" ;
	public static final String ADIV5YAVG = "ADIV5YAVG";
	public static final String EPS_A0 = "EPS_A0";
	public static final String EPS_A1 = "EPS_A1";
	public static final String EPS_A2 = "EPS_A2" ;
	public static final String EPS_A3 = "EPS_A3";
	public static final String EPS_A4 = "EPS_A4";
	public static final String EPS_Q0 = "EPS_Q0";
	public static final String EPS_Q1 = "EPS_Q1";
	public static final String EPS_Q2 = "EPS_Q2" ;
	public static final String EPS_Q3 = "EPS_Q3";
	public static final String EPS_Q4 = "EPS_Q4";
	public static final String FY_NUMQ_1 = "FY_NUMQ_1" ;
	public static final String FY_NUMQ_2 = "FY_NUMQ_2";
	public static final String FY_NUMA_1 = "FY_NUMA_1";
	public static final String FY_NUMA_2 = "FY_NUMA_2";
	public static final String EPS_EST_MEAN_CURQ_1 = "EPS_EST_MEAN_CURQ_1";
	public static final String EPS_EST_MEAN_CURQ_2 = "EPS_EST_MEAN_CURQ_2";
	public static final String EPS_EST_MEAN_CURA_1 = "EPS_EST_MEAN_CURA_1";
	public static final String EPS_EST_MEAN_CURA_2 = "EPS_EST_MEAN_CURA_2";
	public static final String FY_NUMQ0 = "FY_NUMQ0";
	public static final String FY_NUMQ1 = "FY_NUMQ1";
	public static final String FY_NUMQ2 = "FY_NUMQ2";
	public static final String FY_NUMQ3 = "FY_NUMQ3";
	public static final String FY_NUMQ4 = "FY_NUMQ4";
	public static final String RATE_MeanLabel_CURR = "RATE_MeanLabel_CURR";
	public static final String RATE_BUY_CURR = "RATE_BUY_CURR";
	public static final String RATE_BUY_1MAGO = "RATE_BUY_1MAGO";
	public static final String RATE_BUY_2MAGO = "RATE_BUY_2MAGO";
	public static final String RATE_BUY_12MAGO = "RATE_BUY_12MAGO";
	public static final String RATE_OUTPERFORM_CURR = "RATE_OUTPERFORM_CURR";
	public static final String RATE_OUTPERFORM_1MAGO = "RATE_OUTPERFORM_1MAGO";
	public static final String RATE_OUTPERFORM_2MAGO = "RATE_OUTPERFORM_2MAGO";
	public static final String RATE_OUTPERFORM_12MAGO = "RATE_OUTPERFORM_12MAGO";
	public static final String RATE_HOLD_CURR = "RATE_HOLD_CURR";
	public static final String RATE_HOLD_1MAGO = "RATE_HOLD_1MAGO";
	public static final String RATE_HOLD_2MAGO = "RATE_HOLD_2MAGO";
	public static final String RATE_HOLD_12MAGO = "RATE_HOLD_12MAGO";
	public static final String RATE_UNDERPERFORM_CURR = "RATE_UNDERPERFORM_CURR";
	public static final String RATE_UNDERPERFORM_1MAGO = "RATE_UNDERPERFORM_1MAGO";
	public static final String RATE_UNDERPERFORM_2MAGO = "RATE_UNDERPERFORM_2MAGO";
	public static final String RATE_UNDERPERFORM_12MAGO = "RATE_UNDERPERFORM_12MAGO";
	public static final String RATE_SELL_CURR = "RATE_SELL_CURR";
	public static final String RATE_SELL_1MAGO = "RATE_SELL_1MAGO";
	public static final String RATE_SELL_2MAGO = "RATE_SELL_2MAGO";
	public static final String RATE_SELL_12MAGO = "RATE_SELL_12MAGO";
	public static final String MeanRating_CURR = "MeanRating_CURR";
	public static final String MeanRating_1MAGO = "MeanRating_1MAGO";
	public static final String MeanRating_2MAGO = "MeanRating_2MAGO";
	public static final String MeanRating_12MAGO = "MeanRating_12MAGO";
	public static final String PR04WKPCTR = "PR04WKPCTR";
	public static final String PR13WKPCTR = "PR13WKPCTR";
	public static final String PR26WKPCTR = "PR26WKPCTR";
	public static final String BETA = "BETA";
	public static final String MKTCAP = "MKTCAP";
	public static final String TTMPRCFPS = "TTMPRCFPS";
	public static final String QQUICKRATI = "QQUICKRATI";
	public static final String TTMCFSHR = "TTMCFSHR";
	public static final String QCSHPS = "QCSHPS";
	public static final String TTMROEPCT = "TTMROEPCT";
	public static final String TTMROAPCT = "TTMROAPCT";
	public static final String TTMROIPCT = "TTMROIPCT";
	public static final String TTMGROSMGN = "TTMGROSMGN";
	public static final String TTMOPMGN = "TTMOPMGN";
	public static final String TTMEPSXCLX = "TTMEPSXCLX";
	public static final String PEEXCLXOR = "PEEXCLXOR";
	public static final String TTMNPMGN = "TTMNPMGN";
	public static final String COMPANYNAME = "COMPANYNAME";
	public static final String PRICE2BK = "PRICE2BK";
	public static final String TTMREVPS = "TTMREVPS";
	public static final String QBVPS = "QBVPS";
	public static final String TTMPR2REV = "TTMPR2REV";
	public static final String QCURRATIO = "QCURRATIO";
	public static final String CUR_FY_NUM ="CUR_FY_NUM" ;
	public static final String FY_NUMA0 ="FY_NUMA0" ;
	public static final String FY_NUMA1 = "FY_NUMA1";
	public static final String FY_NUMA2 = "FY_NUMA2";
	public static final String FY_NUMA3 = "FY_NUMA3";
	public static final String FY_NUMA4 = "FY_NUMA4";
	public static final String IAD = "IAD";
	public static final String DIVNQXDT = "DIVNQXDT";
	public static final String DIVNQPDT = "DIVNQPDT";
	public static final String ISIN = "ISIN";
	public static final String VOL1DAVG = "VOL1DAVG";
	public static final String VOL10DAVG = "VOL10DAVG";
	public static final String VOL3MAVG = "VOL3MAVG";
	public static final String NHIG = "NHIG";
	public static final String NLOW = "NLOW";
	public static final String SHSOUT = "SHSOUT";
	public static final String YIELD = "YIELD";
	public static final String TTMPAYRAT = "TTMPAYRAT";
	public static final String EXCHANGE = "EXCHANGE";
	public static final String CUSIP = "CUSIP";
	
	public static String formatFourDecimals(String val) {
		try {
			if(val == null || val.equals("N/A")){
				return val;
			}
			DecimalFormat df = new DecimalFormat("0.0000");
			val = (String) df.format(Double.parseDouble(val));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return (val);
		} catch (Exception e) {
			e.printStackTrace();
			return (val);
		}
		return (val);
	}
}
