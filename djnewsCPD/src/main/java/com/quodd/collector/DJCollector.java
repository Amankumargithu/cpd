package com.quodd.collector;

import static com.quodd.controller.DJController.logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import com.quodd.common.logger.CommonLogMessage;
import com.quodd.fileWriter.RawFileWriter;
import com.quodd.parser.DJParser;

public class DJCollector extends Thread {
	private static final String WRITER_THREAD_NAME = "DjFileWriter";
	private Socket socket = null;
	private DJParser parser = null;
	private BufferedInputStream bis = null;
	private BufferedOutputStream bos = null;
	private int readCount = 0;
	private byte[] socketInput = new byte[4096];
	private byte[] remainingRawMessage = null;
	private final byte[] acknowledgement = "Dow Jones Newswires...".getBytes();
	private RawFileWriter fileWriter = null;
	private Thread rawWriterThread = null;

	public DJCollector(Socket s, DJParser parser, String filepath) {
		String currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		long fileBuffer = 1024 * 1024 * 1; // 1MB
		this.socket = s;
		this.parser = parser;
		this.fileWriter = new RawFileWriter(filepath + "/" + currentDate + ".dat",
				filepath + "/" + currentDate + ".idx", fileBuffer);
		this.rawWriterThread = new Thread(this.fileWriter, WRITER_THREAD_NAME);
		this.rawWriterThread.start();
		logger.info(() -> CommonLogMessage.startThread(WRITER_THREAD_NAME));
	}

	@Override
	public void run() {
		try {
			logger.info(() -> CommonLogMessage.startThread(this.getName()));
			this.bis = new BufferedInputStream(this.socket.getInputStream());
			this.bos = new BufferedOutputStream(this.socket.getOutputStream());
			this.socket.setKeepAlive(true);
			this.socket.setSoTimeout(1 * 60 * 1000);
			while (!this.socket.isClosed()) {
				try {
					this.readCount = this.bis.read(this.socketInput);
					if (this.readCount > 0) {
						byte[] arr = Arrays.copyOf(this.socketInput, this.readCount);
						this.fileWriter.add(arr);
						processMessage(arr);
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
	}

	public void finish() {
		try {
			this.bis.close();
			this.bos.close();
			this.fileWriter.finish();
			if (this.rawWriterThread != null) {
				try {
					this.rawWriterThread.join();
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
				}
			}
			logger.info(() -> CommonLogMessage.stopThread(WRITER_THREAD_NAME));
		} catch (IOException e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		} finally {
			try {
				this.socket.close();
			} catch (IOException e) {
				logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
			}
		}
		logger.info(this.getName() + " Socket closed");
		logger.info(() -> CommonLogMessage.stopThread(this.getName()));
	}

	private void processMessage(byte[] rawMsg) {
		try {
			if (this.remainingRawMessage != null && this.remainingRawMessage.length > 0) {
				// Scenario where we have some previous bytes pending, but message was
				// incomplete to process
				byte[] mergedArray = new byte[this.remainingRawMessage.length + rawMsg.length];
				System.arraycopy(this.remainingRawMessage, 0, mergedArray, 0, this.remainingRawMessage.length);
				System.arraycopy(rawMsg, 0, mergedArray, this.remainingRawMessage.length, rawMsg.length);
				rawMsg = mergedArray;
				this.remainingRawMessage = null;
			}
			byte currentByte = 0;
			byte nxtByte = 0;
			int index = 0;
			int currentLength = rawMsg.length;
			while (currentLength > (0x0E + index)) // to check if 1st header is complete
			{
				currentByte = rawMsg[index];
				nxtByte = rawMsg[index + 1];
				if (currentByte == 0x01 && nxtByte == 0x11) // starting of message
				{
					byte version = rawMsg[index + 3];
					int msgLength = toInt(rawMsg[index + 5], rawMsg[index + 4]);
					// if array contains complete message, then process
					if (currentLength >= index + msgLength) {
						byte[] msg = Arrays.copyOfRange(rawMsg, index, index + msgLength);
						index += msgLength;
						this.parser.add(msg);
						this.bos.write(this.acknowledgement);
						this.bos.flush();
					} else {
						// "Doesn't have complete message"
						break;
					}
				} else {
					index++; // starting bits did not match. skip current bit and move to next bit.
				}
			}
			if (index < currentLength) {
				byte[] leftOver = Arrays.copyOfRange(rawMsg, index, currentLength);
				this.remainingRawMessage = leftOver;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getName() + " " + e.getMessage(), e);
		}
	}

	private int toInt(byte hb, byte lb) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(lb);
		bb.put(hb);
		return bb.getShort(0);
	}
}