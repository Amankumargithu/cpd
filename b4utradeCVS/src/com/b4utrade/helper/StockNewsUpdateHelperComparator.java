package com.b4utrade.helper;

import java.util.Comparator;

public class StockNewsUpdateHelperComparator implements Comparator {
	@Override
	public int compare(Object news1, Object news2) {
		long newsID1 = 0L, newsID2 = 0L;
		newsID1 = ((StockNewsUpdateHelper) news1).getLastNewsID();
		newsID2 = ((StockNewsUpdateHelper) news2).getLastNewsID();
		if (newsID1 > newsID2) {
			return 1;
		}
		if (newsID1 == newsID2) {
			return 0;
		}
		return -1;
	}
}
