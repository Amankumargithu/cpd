package ntp.util;

import java.util.Calendar;
import java.util.TimeZone;



public class BytePumpUtility {
	
	private static long msinday=86400000;//milliseconds in a day
	private static long timdif = TimeZone.getTimeZone("America/New_York").getOffset(Calendar.getInstance().getTimeInMillis());

	/*public static void parseDateLong(ByteOutputStream os, long ms, boolean timeDiff)
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
		System.out.println(hh + " " + mm + " " + ss + " " + ms);
//		os.write(ByteBuffer.allocate(1).put(hh).array());
		os.write(ByteBuffer.allocate(1).putLong(mm).array());
		os.write(ByteBuffer.allocate(1).putLong(ss).array());
		os.write(ByteBuffer.allocate(2).putLong(ms).array());
		byte[] bt = os.getBytes();
		System.out.println(new String(bt));
//		System.out.printf("0x%04X 0x%04X 0x%04X 0x%04X", hh, mm, ss, ms);
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
	}*/
	
	public static void main(String[] args) {
		/*long hh = 9l;
		System.out.println(Long.toBinaryString(hh));
		hh  =hh & ((1 << 4) - 1);
		System.out.println (Long.toBinaryString(hh));*/
		/*long time = System.currentTimeMillis();
		ByteOutputStream bos = new ByteOutputStream();
		parseDateLong(bos, time , false);
		bos.close();*/
	}

}
