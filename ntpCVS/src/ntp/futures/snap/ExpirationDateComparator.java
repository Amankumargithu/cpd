package ntp.futures.snap;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Map.Entry;

public class ExpirationDateComparator implements Comparator<Entry<String, Calendar>> {
	// Sorting the list based on values
	public int compare(Entry<String, Calendar> o1, Entry<String, Calendar> o2)
	{
		Calendar xcal = o1.getValue();
		Calendar ycal = o2.getValue();
		if ( xcal.before(ycal) ) return -1;
		if ( xcal.after(ycal) ) return 1;
		return 0;
	}
}
