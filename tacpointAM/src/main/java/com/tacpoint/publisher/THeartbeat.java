/**
 * THeartbeat.java
 *
 * @author Copyright (c) 2003 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 * Oct 29, 2003
 */

package com.tacpoint.publisher;


import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class THeartbeat implements Runnable {
	private static Log log = LogFactory.getLog(THeartbeat.class);

	private TPublisher publisher;

	private int sleepTime;

	private String topic;

	private String message;

	private int estOffset;

	private java.io.ByteArrayOutputStream baos = null;

	public THeartbeat(TPublisher p) {
		publisher = p;
		TPublisherConfigBean bean = publisher.getConfiguration();
		sleepTime = bean.getHeartbeatIntervalInSeconds() * 1000;
		estOffset = bean.getEstOffset();
		topic = bean.getHeartbeatTopic();
		message = bean.getHeartbeatMessage();

	}

	public void run() {

		GregorianCalendar gc = new GregorianCalendar();

		try {

			while (true) {
				Thread.sleep(sleepTime);
				if (publisher != null) {
					baos = new java.io.ByteArrayOutputStream();

					long now = System.currentTimeMillis();
					gc.setTimeInMillis(now + estOffset);
					
					int hour = gc.get(java.util.Calendar.HOUR_OF_DAY);
					int minute = gc.get(java.util.Calendar.MINUTE);
					int second = gc.get(java.util.Calendar.SECOND);
					
					StringBuffer sb = new StringBuffer();
					sb.append("L:");	
					
					if (hour < 10)
						sb.append("0");
					sb.append(String.valueOf(hour));
					
					if (minute < 10)
						sb.append("0");					
				    sb.append(String.valueOf(minute));
					
					if (second < 10)
						sb.append("0");					
					sb.append(String.valueOf(second));
					
					baos.write(message.getBytes());
					baos.write(sb.toString().getBytes());
					baos.write(com.tacpoint.publisher.TConstants.TERMINATOR_BYTE);

					log.debug("Publishing heartbeat msg: "+new String(baos.toByteArray()));
					publisher.publish(topic, baos);
				}
			}
		} catch (Exception e) {
			log.error("THeartbeat.run() exception " + e.getMessage());
		}

	}
}
