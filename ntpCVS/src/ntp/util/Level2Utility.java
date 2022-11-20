package ntp.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Level2Utility {

	private static Level2Utility instance = null;
	private int pricingDecimalFormat=2;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

	static private final int QTE_BID_UNPRICED  = 0x0010;
	static private final int QTE_ASK_UNPRICED  = 0x0020;
	static private final int QTE_BID_VALID     = 0x0040;
	static private final int QTE_ASK_VALID     = 0x0080;
	static private final int QTE_OPEN          = 0x0002;

	private Level2Utility()
	{
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public static Level2Utility getInstance()
	{
		if(instance == null)
			instance = new Level2Utility();
		return instance;
	}

	public void setPricingDecimalFormat(int p)
	{
		pricingDecimalFormat = p;
	}

	public String Price2String( double prc)
	{
		BigDecimal dec;
		int rnd = BigDecimal.ROUND_HALF_UP;
		dec = new BigDecimal(prc).setScale( pricingDecimalFormat, rnd );
		return dec.toString();
	}
	public String formatTime(long time)
	{
		Date d = new Date(time);
		return sdf.format(d);
	}

	private boolean isSet( int msk, int bit )
	{
		return( ( msk & bit ) == bit );
	}

	public boolean isAskValid(int flag)
	{
		return isSet(flag, QTE_ASK_VALID);
	}
	public boolean isAskUnpriced(int flag)
	{
		return isSet(flag, QTE_ASK_UNPRICED);
	}

	public boolean isBidValid(int flag)
	{
		return isSet(flag, QTE_BID_VALID);
	}
	
	public boolean isBidUnpriced(int flag)
	{
		return isSet(flag, QTE_BID_UNPRICED);
	}
	public boolean isOpen(int flag)
	{
		return isSet(flag, QTE_OPEN);
	}
}
