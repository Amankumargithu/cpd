package com.b4utrade.tsq;

import com.b4utrade.bean.TSQBean;

import java.util.Comparator;

public class TSQTradeQuoteTimeComparator implements Comparator<TSQBean> {

	public static final int ASCENDING = 1;

	public static final int DESCENDING = -1;

	private int direction = ASCENDING;

	/**
	 * Called by sort function to determine ordering of two objects.
	 *
	 * @param o1 Object
	 * @param o2 Object
	 * @returns int comparison result
	 */
	public int compare(TSQBean tsq1, TSQBean tsq2) {

		if (tsq1.getTradeQuoteTime() == tsq2.getTradeQuoteTime()
				.longValue())
			return 0;

		if (tsq1.getTradeQuoteTime() < tsq2.getTradeQuoteTime()
				.longValue())
			if (direction == ASCENDING)
			   return -1;
			else
			   return 1;
		
		if (direction == ASCENDING)
		   return 1;
		else
		   return -1;

	}

	/**
	 * @return Returns the direction.
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * @param direction The direction to set.
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

}
