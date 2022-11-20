package com.b4utrade.stock;

import java.io.ByteArrayOutputStream;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;

import com.tacpoint.jms.MessageHandler;
import com.tacpoint.util.Logger;

public class QuoteMessageHandler extends MessageHandler {

	private byte[] bytes = new byte[1024];
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private BytesMessage message = null;

	public void setMessage(Message message) {
		this.message = (BytesMessage) message;
	}

	public Message getMessage() {
		return message;
	}

	public void run() {
		int bytesRead = 0;
		try {
			baos.reset();
			String ticker = message.readUTF();
			while ((bytesRead = message.readBytes(bytes)) != -1) {
				baos.write(bytes, 0, bytesRead);
			}
			Quotes.getInstance().setStock(ticker, baos.toByteArray());
		} catch (MessageEOFException e) {
			Logger.log("QuoteMessageHandler.run - MessageEOFException");
		} catch (Exception e) {
			Logger.log("QuoteMessageHandler.run - Unable to retrieve message.", e);
		}
	}

	public Object clone() throws CloneNotSupportedException {
		return new QuoteMessageHandler();
	}
}
