package com.quodd.util;

import static com.quodd.cpd.AlertCpd.logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * EmailManager will create a multipart message and send it to the appropriate
 * recipient. It accepts an optional array of file attachments which will also
 * be appended to the message if supplied.
 * 
 */
public class EmailManager {

	public void send(String to, String cc, String bcc, String from, String fromPersonalName, String smtp,
			String subject, String msgBody, boolean isHtml, boolean debug, String[] files) throws MessagingException {
		// check if we receieved a valid request...
		if (isHtml && files != null && files.length > 0) {
			String error = "File attachments can only be processed " + "using a mime type of \"text/plain\" NOT "
					+ "\"text/html\".";
			throw new MessagingException(error);
		}
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtp);
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);
		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			try {
				msg.setFrom(new InternetAddress(from, fromPersonalName));
			} catch (Exception pne) {
				logger.log(Level.WARNING, "Unable to set personal name in the from email address - " + pne.getMessage(),
						pne);
				msg.setFrom(new InternetAddress(from));
			}
			InternetAddress[] addressTo = extractRecipients(to);
			InternetAddress[] addressCc = extractRecipients(cc);
			InternetAddress[] addressBcc = extractRecipients(bcc);
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			msg.setRecipients(Message.RecipientType.CC, addressCc);
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);
			msg.setSubject(subject);
			if (!isHtml)
				msg.setContent(processMultipart(msgBody, files));
			else
				msg.setDataHandler(new DataHandler(new ByteArrayDataSource(msgBody, "text/html")));
			// set the Date: header
			msg.setSentDate(new Date());
			// send the message
			Transport.send(msg);
		} catch (MessagingException mex) {
			logger.log(Level.WARNING, mex.getMessage(), mex);
			throw mex;
		}
	}

	public void send(String to, String cc, String bcc, String from, String smtp, String subject, String msg_body,
			boolean isHtml, boolean debug, String[] files) throws MessagingException {

		// check if we receieved a valid request...
		if (isHtml && files != null && files.length > 0) {
			String error = "File attachments can only be processed " + "using a mime type of \"text/plain\" NOT "
					+ "\"text/html\".";
			throw new MessagingException(error);
		}

		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtp);

		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);

		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));

			InternetAddress[] address_to = extractRecipients(to);
			InternetAddress[] address_cc = extractRecipients(cc);
			InternetAddress[] address_bcc = extractRecipients(bcc);

			msg.setRecipients(Message.RecipientType.TO, address_to);
			msg.setRecipients(Message.RecipientType.CC, address_cc);
			msg.setRecipients(Message.RecipientType.BCC, address_bcc);

			msg.setSubject(subject);

			if (!isHtml)
				msg.setContent(processMultipart(msg_body, files));
			else
				msg.setDataHandler(new DataHandler(new ByteArrayDataSource(msg_body, "text/html")));

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);

		} catch (MessagingException mex) {
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
			throw mex;
		}
	}

	/**
	 * This method will allow parse out the "to" array and create a new
	 * InternetAddress object for each comma delimited recipient.
	 */
	private InternetAddress[] extractRecipients(String to) throws MessagingException {

		// parse out recipients
		if (to == null)
			to = new String();
		StringTokenizer st = new StringTokenizer(to, ",");
		Vector v = new Vector();
		while (st.hasMoreTokens()) {
			String recip = st.nextToken();
			if (recip != null) {
				InternetAddress ia = new InternetAddress(recip);
				v.addElement(ia);
			}
		}

		InternetAddress[] ia_array = new InternetAddress[v.size()];
		v.copyInto(ia_array);
		return ia_array;
	}

	/**
	 * This method will allow the client to send plain text ("text/plain") and file
	 * attachments.
	 */
	private Multipart processMultipart(String msg_body, String[] files) throws MessagingException {

		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(msg_body);

		// create the Multipart and its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);

		if (files != null && files.length > 0) {
			MimeBodyPart[] mbpArray = new MimeBodyPart[files.length];
			for (int i = 0; i < files.length; i++) {
				mbpArray[i] = new MimeBodyPart();
				FileDataSource fds = new FileDataSource(files[i]);
				mbpArray[i].setDataHandler(new DataHandler(fds));
				mbpArray[i].setFileName(files[i]);
				mp.addBodyPart(mbpArray[i]);
			}
		}

		return mp;
	}
}

/**
 * This class implements a typed DataSource from : an InputStream a byte array a
 * String
 */
class ByteArrayDataSource implements DataSource {
	private byte[] data;
	private String type;

	/* Create a datasource from an input stream */
	ByteArrayDataSource(InputStream is, String type) {
		this.type = type;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int ch;

			while ((ch = is.read()) != -1)
				os.write(ch);
			data = os.toByteArray();

		} catch (IOException ioex) {
		}
	}

	/* Create a datasource from a byte array */
	ByteArrayDataSource(byte[] data, String type) {
		this.data = data;
		this.type = type;
	}

	/* Create a datasource from a String */
	ByteArrayDataSource(String data, String type) {
		try {
			// Assumption that the string contains only ascii
			// characters ! Else just pass in a charset into this
			// constructor and use it in getBytes()
			this.data = data.getBytes("iso-8859-1");
		} catch (UnsupportedEncodingException uex) {
		}
		this.type = type;
	}

	public InputStream getInputStream() throws IOException {
		if (data == null)
			throw new IOException("no data");
		return new ByteArrayInputStream(data);
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("cannot do this");
	}

	public String getContentType() {
		return type;
	}

	public String getName() {
		return "dummy";
	}
}
