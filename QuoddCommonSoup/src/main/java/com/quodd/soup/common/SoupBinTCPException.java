package com.quodd.soup.common;

import java.io.IOException;

/**
 * Indicates a protocol error while handling the SoupBinTCP protocol.
 */
public class SoupBinTCPException extends IOException {

	/**
	 * Create an instance with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public SoupBinTCPException(String message) {
		super(message);
	}

	/**
	 * Construct an instance with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause   the cause
	 */
	public SoupBinTCPException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct an instance with the specified cause.
	 *
	 * @param cause the cause
	 */
	public SoupBinTCPException(Throwable cause) {
		super(cause);
	}

}
