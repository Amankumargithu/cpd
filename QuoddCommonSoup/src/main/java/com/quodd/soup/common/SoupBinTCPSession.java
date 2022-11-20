package com.quodd.soup.common;

import static com.quodd.soup.common.SoupBinTCP.MAX_PACKET_LENGTH;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.quodd.soup.common.SoupBinTCP.LoginAccepted;
import com.quodd.soup.common.SoupBinTCP.LoginRejected;
import com.quodd.soup.common.SoupBinTCP.LoginRequest;

/**
 * The base for both the client and server side of the protocol.
 */
public class SoupBinTCPSession implements Closeable {

	private static final long RX_HEARTBEAT_TIMEOUT_MILLIS = 15000;
	private static final long TX_HEARTBEAT_INTERVAL_MILLIS = 1000;
	private static final int MIN_MAX_PAYLOAD_LENGTH = 30;
	private final Clock clock;
	private final SocketChannel channel;
	private final MessageListener listener;
	private final SoupBinTCPClientStatusListener statusListener;
	/*
	 * This variable is written on data reception and read on session keep-alive.
	 * These two functions can run on different threads without locking.
	 */
	private volatile long lastRxMillis;
	/*
	 * This variable is written on data transmission and read on session keep-alive.
	 * These two functions can run on different threads but require locking.
	 */
	private long lastTxMillis;
	private final ByteBuffer rxBuffer;
	private final ByteBuffer txHeader;
	private final ByteBuffer[] txBuffers;
	private final byte heartbeatPacketType;
	private int bufferLimit = 0;
	private boolean isClosed = true;
	private final ScheduledExecutorService service;
	private long sequenceNumber = 1;

	public SoupBinTCPSession(Clock clock, SocketChannel channel, int payloadSize, MessageListener listener,
			SoupBinTCPClientStatusListener statusListener) {
		this.clock = clock;
		this.channel = channel;
		this.listener = listener;
		this.statusListener = statusListener;
		this.lastRxMillis = clock.currentTimeMillis();
		this.lastTxMillis = clock.currentTimeMillis();
		int maxPayloadLength = Math.max(MIN_MAX_PAYLOAD_LENGTH, payloadSize);
		this.bufferLimit = 3 + Math.min(maxPayloadLength, MAX_PACKET_LENGTH - 1);
		this.rxBuffer = ByteBuffer.allocateDirect(this.bufferLimit);
		this.txHeader = ByteBuffer.allocateDirect(3);
		this.txBuffers = new ByteBuffer[2];
		this.txHeader.order(ByteOrder.BIG_ENDIAN);
		this.txBuffers[0] = this.txHeader;
		this.heartbeatPacketType = SoupBinTCP.PACKET_TYPE_CLIENT_HEARTBEAT;
		this.isClosed = false;
		this.service = Executors.newSingleThreadScheduledExecutor();
		this.service.scheduleAtFixedRate(this::keepAlive, 0, 500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get the underlying socket channel.
	 *
	 * @return the underlying socket channel
	 */
	public SocketChannel getChannel() {
		return this.channel;
	}

	/**
	 * Receive data from the underlying socket channel. For each packet received,
	 * invoke the corresponding listener if applicable.
	 *
	 * @return the number of bytes read, possibly zero, or {@code -1} if the channel
	 *         has reached end-of-stream
	 * @throws IOException if an I/O error occurs
	 */
	public int receive() throws IOException {
		if (this.isClosed)
			return 0;
		int bytes = this.channel.read(this.rxBuffer);
		if (bytes <= 0)
			return bytes;
		this.rxBuffer.flip();
		while (parse())
			;
		this.rxBuffer.compact();
		receivedData();
//		this.keepAlive();
		return bytes;
	}

	private boolean parse() throws IOException {
		if (this.rxBuffer.remaining() < 2)
			return false;
		this.rxBuffer.mark();
		this.rxBuffer.order(ByteOrder.BIG_ENDIAN);
		int packetLength = this.rxBuffer.getShort() & 0xffff;
		if (packetLength > this.rxBuffer.capacity() - 2)
			throw new SoupBinTCPException(
					"Packet length exceeds buffer capacity " + packetLength + " " + this.rxBuffer.capacity());
		if (this.rxBuffer.remaining() < packetLength) {
			this.rxBuffer.reset();
			return false;
		}
		byte packetType = this.rxBuffer.get();
		int limit = this.rxBuffer.limit();
		this.rxBuffer.limit(this.rxBuffer.position() + packetLength - 1);
		packet(packetType, this.rxBuffer);
		this.rxBuffer.position(this.rxBuffer.limit());
		this.rxBuffer.limit(limit);
		return true;
	}

	/**
	 * Keep the session alive.
	 *
	 * <p>
	 * If the heartbeat interval duration has passed since the last packet was sent,
	 * send a Heartbeat packet. If the heartbeat timeout duration has passed since
	 * the last packet was received, invoke the corresponding method on the status
	 * listener.
	 * </p>
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void keepAlive() {
		if (this.isClosed)
			return;
		try {
			long currentTimeMillis = this.clock.currentTimeMillis();
			if (currentTimeMillis - this.lastTxMillis > TX_HEARTBEAT_INTERVAL_MILLIS)
				send(this.heartbeatPacketType);
			if (currentTimeMillis - this.lastRxMillis > RX_HEARTBEAT_TIMEOUT_MILLIS)
				handleHeartbeatTimeout();
		} catch (Exception e) {

		}
	}

	/**
	 * Close the underlying socket channel.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		this.isClosed = true;
		int limit = (int) (this.bufferLimit * 0.9);
		int bytes = 0;
		do {
			this.rxBuffer.clear();
			bytes = this.channel.read(this.rxBuffer);
		} while (bytes >= limit);
		this.rxBuffer.clear();
		this.channel.close();
		this.service.shutdownNow();
	}

	/**
	 * Send a Login Request packet.
	 *
	 * @param payload the packet payload
	 * @throws IOException if an I/O error occurs
	 */
	public void login(LoginRequest payload) throws IOException {
		ByteBuffer txPayload = ByteBuffer.allocateDirect(46);
		txPayload.clear();
		payload.put(txPayload);
		txPayload.flip();
		send(SoupBinTCP.PACKET_TYPE_LOGIN_REQUEST, txPayload);
	}

	/**
	 * Send a Logout Request packet.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void logout() throws IOException {
		send(SoupBinTCP.PACKET_TYPE_LOGOUT_REQUEST);
	}

	/**
	 * Send an Unsequenced Data packet.
	 *
	 * @param buffer a buffer containing the packet payload
	 * @throws IOException if an I/O error occurs
	 */
	public void send(ByteBuffer buffer) throws IOException {
		send(SoupBinTCP.PACKET_TYPE_UNSEQUENCED_DATA, buffer);
	}

	protected void heartbeatTimeout() throws IOException {
		this.statusListener.heartbeatTimeout(this);
	}

	protected void packet(byte packetType, ByteBuffer payload) throws IOException {
		switch (packetType) {
		case SoupBinTCP.PACKET_TYPE_DEBUG:
			break;
		case SoupBinTCP.PACKET_TYPE_LOGIN_ACCEPTED:
			LoginAccepted loginAccepted = new LoginAccepted();
			loginAccepted.get(payload);
			this.statusListener.loginAccepted(this, loginAccepted);
			break;
		case SoupBinTCP.PACKET_TYPE_LOGIN_REJECTED:
			LoginRejected loginRejected = new LoginRejected();
			loginRejected.get(payload);
			this.statusListener.loginRejected(this, loginRejected);
			break;
		case SoupBinTCP.PACKET_TYPE_SEQUENCED_DATA:
			this.listener.message(payload);
			this.setSequenceNumber(this.getSequenceNumber() + 1);
			break;
		case SoupBinTCP.PACKET_TYPE_SERVER_HEARTBEAT:
			break;
		case SoupBinTCP.PACKET_TYPE_END_OF_SESSION:
			this.statusListener.endOfSession(this);
			break;
		default:
			this.statusListener.unexceptedPacket(packetType, payload);
			break;
		}
	}

	protected void send(byte packetType) throws IOException {
		this.txHeader.clear();
		this.txHeader.putShort((short) 1);
		this.txHeader.put(packetType);
		this.txHeader.flip();
		do {
			this.channel.write(this.txHeader);
		} while (this.txHeader.hasRemaining());
		sentData();
	}

	protected void send(byte packetType, ByteBuffer payload) throws IOException {
		int packetLength = payload.remaining() + 1;
		if (packetLength > SoupBinTCP.MAX_PACKET_LENGTH)
			throw new SoupBinTCPException("Packet length exceeds maximum packet length");
		this.txHeader.clear();
		this.txHeader.putShort((short) packetLength);
		this.txHeader.put(packetType);
		this.txHeader.flip();
		this.txBuffers[1] = payload;
		int remaining = this.txHeader.remaining() + payload.remaining();
		do {
			remaining -= this.channel.write(this.txBuffers);
		} while (remaining > 0);
		sentData();
	}

	private void handleHeartbeatTimeout() throws IOException {
		heartbeatTimeout();
		receivedData();
	}

	private void receivedData() {
		this.lastRxMillis = this.clock.currentTimeMillis();
	}

	private void sentData() {
		this.lastTxMillis = this.clock.currentTimeMillis();
	}

	public long getSequenceNumber() {
		return this.sequenceNumber;
	}
	
	public long getlastReceivedTime() {
		return this.lastRxMillis;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

}
