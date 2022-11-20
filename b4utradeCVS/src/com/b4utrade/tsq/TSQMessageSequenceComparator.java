package com.b4utrade.tsq;

import com.b4utrade.bean.TSQBean;

import java.util.Comparator;

public class TSQMessageSequenceComparator implements Comparator {

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
	public int compare(Object o1, Object o2) {

		TSQBean tsq1 = (TSQBean) o1;
		TSQBean tsq2 = (TSQBean) o2;

		if (tsq1.getMsgSequence().longValue() == tsq2.getMsgSequence()
				.longValue())
			return 0;

		if (tsq1.getMsgSequence().longValue() < tsq2.getMsgSequence()
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
