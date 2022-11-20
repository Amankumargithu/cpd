
package com.b4utrade.helper;

import com.b4utrade.bean.NewsBean;

import java.util.Comparator;

public class DJNewsBeanComparator implements Comparator<NewsBean> {
	public int compare(NewsBean news1, NewsBean news2) {
		long newsID1 = news1.getNewsDate().getTime();
		long newsID2 = news2.getNewsDate().getTime();
		if (newsID1 > newsID2)
			return -1;
		else if (newsID1 == newsID2)
			return 0;
		else
			return 1;
	}
}
