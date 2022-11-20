package ntp.util;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import ntp.bean.TSQBean;
import ntp.bean.TickerChannelBean;
import ntp.logger.NTPLogger;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.util.UltraChan;

public class NTPUtility {

	private static ConcurrentHashMap<String, TickerChannelBean> tickerStreamIDMap = new ConcurrentHashMap<>();
	private static DecimalFormat df = new DecimalFormat("#.####");
	private static long msinday=86400000;//milliseconds in a day
	private static long timdif = TimeZone.getTimeZone("America/New_York").getOffset(Calendar.getInstance().getTimeInMillis());

	public static void updateTickerStreamIDMap(String ticker, int streamID, UltraChan channel)
	{
		TickerChannelBean bean = new TickerChannelBean();
		bean.setChannel(channel);
		bean.setStreamID(streamID);
		tickerStreamIDMap.put(ticker, bean);
	}

	public static int getStreamID(String ticker)
	{
		if (tickerStreamIDMap.containsKey(ticker))
			return tickerStreamIDMap.get(ticker).getStreamID();
		return -1;
	}

	public static UltraChan getSubscribedChannel(String ticker)
	{
		if (tickerStreamIDMap.containsKey(ticker))
			return tickerStreamIDMap.get(ticker).getChannel();
		return null;
	}

	public static String getMappedExchangeCode(String exchange)
	{
		if(exchange.equals("ARCX"))
			return "NYSE ARCA";
		else if(exchange.equals("XASE"))
			return "NYSE MKT";
		else if(exchange.equals("XNYS"))
			return "NYSE";
		else if(exchange.equals("XOTC"))
			return "OTCBB";	
		else if(exchange.equals("XTSE"))
			return "Toronto";	
		else if(exchange.equals("XTSX"))
			return "TSX Venture";	
		else if(exchange.equals("XNAS"))
			return "NASDAQ";
		else if (exchange.equals(""))
			return " ";
		return exchange;
	}

	public static String formatLimitUpDown(String limitUpDown) 
	{
		if(limitUpDown == null || limitUpDown.length()<1 )
			return " , ";
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

	public static void unsubscribeTicker(String ticker)
	{
		if(ticker == null)
			return;
		int streamID = getStreamID(ticker); 
		UltraChan channel = getSubscribedChannel(ticker);
		if (channel != null && streamID != -1)
		{
			channel.Unsubscribe(streamID);
			NTPLogger.unsubscribeSymbol(ticker, "" + streamID);
//			StockRetriever.getInstance().getTickerList().remove(ticker);
		}
		else
			NTPLogger.info("Ticker: " + ticker +" can not be unsubscribed with streamID: " + streamID + " no channel found");
	}

	public static void formatTSQLimitUpDown(String limitUpDown, TSQBean bean) 
	{
		if (limitUpDown == null || limitUpDown.length()<1)
		{
			bean.setTradeQuoteCondCode3(" ");
			bean.setTradeQuoteCondCode4(" ");

			return;
		}
		char limitChar = limitUpDown.charAt(0);
		switch (limitChar)
		{
		case 'A':
			bean.setTradeQuoteCondCode3(" ");
			bean.setTradeQuoteCondCode4(" ");

			break;

		case 'B':
			bean.setTradeQuoteCondCode3("N");
			bean.setTradeQuoteCondCode4(" ");

			break;

		case 'C':
			bean.setTradeQuoteCondCode3(" ");
			bean.setTradeQuoteCondCode4("N");

			break;

		case 'D':
			bean.setTradeQuoteCondCode3("N");
			bean.setTradeQuoteCondCode4("N");

			break;

		case 'E':
			bean.setTradeQuoteCondCode3("L");
			bean.setTradeQuoteCondCode4(" ");

			break;

		case 'F':
			bean.setTradeQuoteCondCode3(" ");
			bean.setTradeQuoteCondCode4("L");

			break;

		case 'G':
			bean.setTradeQuoteCondCode3("L");
			bean.setTradeQuoteCondCode4("N");

			break;

		case 'H':
			bean.setTradeQuoteCondCode3("N");
			bean.setTradeQuoteCondCode4("L");

			break;

		case 'I':
			bean.setTradeQuoteCondCode3("L");
			bean.setTradeQuoteCondCode4("L");

			break;

		default:
			bean.setTradeQuoteCondCode3(" ");
			bean.setTradeQuoteCondCode4(" ");

		}
	}

	public static Integer getFormattedTime(long millis)
	{
		int[] time = QuoddMsg.timeHMSms(millis);
		String strTime = String.format( "%02d%02d%02d%03d", time[0], time[1], time[2], time[3] );
		Integer intTime =  new Integer(99999999);
		try 
		{
			intTime = Integer.parseInt(strTime);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return intTime;
	}

	public static Double parseFourDecimal(Double value)
	{
		if (value == null)
			return value;
		try
		{
			return Double.parseDouble(df.format(value));
		} catch (NumberFormatException ne)
		{
			ne.printStackTrace();
		}
		return value;
	}

	public static void parseDateLong(StringBuilder sb, long ms, boolean timeDiff)
	{
		long hh=0,mm=0,ss=0;
		if(timeDiff)
			ms=ms+timdif;//changing GMT to Local Time
		ms=ms%msinday;//Finding total milliseconds of current day //passed till now
		hh=ms/(1000*60*60);
		ms=ms%(1000*60*60);
		mm=ms/(1000*60);
		ms=ms%(1000*60);
		ss=ms/1000;
		ms=ms%1000;
		if(hh<10)
			sb.append("0");
		sb.append(hh);
		if(mm<10)
			sb.append("0");
		sb.append(mm);
		if(ss<10)
			sb.append("0");
		sb.append(ss);
		if(ms<10)
			sb.append("00");
		else if(ms<100)
			sb.append("0");
		sb.append(ms);
	}

	public static void lPadZero(StringBuilder sb, long in, int fill){
		boolean negative = false;
		long value, len = 0;
		if(in >= 0){
			value = in;
		} else {
			negative = true;
			value = - in;
			in = - in;
			len ++;
		}
		if(value == 0){
			len = 1;
		} else{         
			for(; value != 0; len ++){
				value /= 10;
			}
		}
		if(negative){
			sb.append('-');
		}
		if(fill < len)
			NTPLogger.error("Length Exceed " + in + " " + fill);
		for(int i = fill; i > len; i--){
			sb.append('0');
		}
		sb.append(in);   
	}

	public static void doublePadZero(StringBuilder builder, double d, int padding) {
		if(d<0)
		{
			builder.append("-");
			d=d*(-1);
			padding--;
		}
		long scaled = (long) (d * 1e4 + 0.5);
		long factor = 10000;
		int scale = 5;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if(scale < padding)
			{
				builder.append('0');
				padding--;
			}
			else
			{
				long c = scaled / factor % 10;
				factor /= 10;
				builder.append((char) ('0' + c));
				scale--;
				padding--;
			}
		}
	}
	
	public static void stringPadLeftSpace (StringBuilder builder, String string, int length)
	{
		int size = string.length();
		if(size > length)
			NTPLogger.error("Length Exceed " + string + " " + length);
		while(size < length)
		{
			builder.append(" ");
			length--;
		}
		builder.append(string);
	}
	public static void stringPadRightSpace(StringBuilder builder, String string, int length)
	{
		builder.append(string);
		int size = string.length();
		while(size < length)
		{
			builder.append(" ");
			length--;
		}		
	}
	
	public static void stringPadZero (StringBuilder builder, String string, int length)
	{
		int size = string.length();
		while(size < length)
		{
			builder.append("0");
			length--;
		}
		builder.append(string);
	}
	public static void appendTo4(StringBuilder builder, double d) {
		if(d<0)
		{
			builder.append("-");
			d=d*(-1);
		}
		long scaled = (long) (d * 1e4 + 0.5);
		long factor = 10000;
		int scale = 5;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if (scale == 4)
				builder.append('.');
			long c = scaled / factor % 10;
			factor /= 10;
			builder.append((char) ('0' + c));
			scale--;
		}
	}

	public static String appendTo6(double d){
		StringBuilder builder = new StringBuilder();
		if(d<0)
		{
			builder.append("-");
			d=d*(-1);
		}
		long scaled = (long) (d * 1e6 + 0.5);
		long factor = 1000000;
		int scale = 7;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if (scale == 6)
				builder.append('.');
			long c = scaled / factor % 10;
			factor /= 10;
			builder.append((char) ('0' + c));
			scale--;
		}
		return builder.toString();
	}
	
}
