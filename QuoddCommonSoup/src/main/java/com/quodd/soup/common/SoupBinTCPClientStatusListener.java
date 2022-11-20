package com.quodd.soup.common;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.quodd.soup.common.SoupBinTCP.LoginAccepted;
import com.quodd.soup.common.SoupBinTCP.LoginRejected;

/**
 * The interface for inbound status events on the client side.
 */
public interface SoupBinTCPClientStatusListener {

	/**
	 * Receive an indication of a heartbeat timeout.
	 *
	 * @param session the session
	 * @throws IOException if an I/O error occurs
	 */
	void heartbeatTimeout(SoupBinTCPSession session);

	/**
	 * Receive a Login Accepted packet.
	 *
	 * @param session the session
	 * @param payload the packet payload
	 * @throws IOException if an I/O error occurs
	 */
	void loginAccepted(SoupBinTCPSession session, LoginAccepted payload);

	/**
	 * Receive a Login Rejected packet.
	 *
	 * @param session the session
	 * @param payload the packet payload
	 * @throws IOException if an I/O error occurs
	 */
	void loginRejected(SoupBinTCPSession session, LoginRejected payload);

	/**
	 * Receive an End Of Session packet.
	 *
	 * @param session the session
	 * @throws IOException if an I/O error occurs
	 */
	void endOfSession(SoupBinTCPSession session);

	void unexceptedPacket(byte packetType, ByteBuffer payload);

}
