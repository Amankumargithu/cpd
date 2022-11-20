/** SendMail.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  03/01/2000
* Date modified: 01/15/2001
*
* 01/15/2001 Liam Hennebury - Added importance level capability
*/

package com.tacpoint.mail;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.tacpoint.util.*;


/**
 * Ability to send e-mail using JavaMail API.
 * This class uses only SMTP transport protocol to send mail.
 */
public class SendMail
{
	////////////////////////////////////////////////////////////////////////////
	// D A T A	 M E M B E R S
	////////////////////////////////////////////////////////////////////////////

	/**
	 * X-Mailer header value.
	 */
	private static final String mHeader = "SendMail";

	/**
	 * Transport protocol to send mail.
	 */
	private static final String mTransport = "smtp";
	private static final String mTransportProp = "mail.smtp.host";

	/**
	 * smtp host server name.
	 */
	private String mSMTPServer = null;

	/**
	 * Address(es) of mailbox(es) to send the mail message to in the
	 * form of name@company.com.  If more than one address is specified,
	 * separate the addresses by a comma.
	 */
	private String mTo = null;

	private String[] mMultiTo = null;

	/**
	 * Address or name of the message sender.
	 */
	private String mFrom = null;

	/**
	 * Address(es) of mailbox(es) to send a COPY of the mail message, in the
	 * form of name@company.com.  If more than one address is specified,
	 * separate the addresses by a comma.
	 */
	private String mCC = null;

	/**
	 * Address(es) of mailbox(es) to send a blind COPY of the mail message,
	 * in the form of name@company.com.  If more than one address is
	 * specified, separate the addresses by a comma.
	 */
	private String mBCC = null;

	/**
	 * Message subject
	 */
	private String mSubject = null;

	/**
	 * File name(s) to send with a mail message, as attachment(s).
	 */
	private String[] mFileAttachments = null;

   /**
    * Importance or priority of message.
    */
   private String mImportance = null;

	/**
	 * Body of the message.
	 */
	private String mMessage = null;


	////////////////////////////////////////////////////////////////////////////
	// C O N S T R U C T O R S
	////////////////////////////////////////////////////////////////////////////

	/** Constructor:
	*
	* @param aSMTPServer  smtp host server name
	* @param aTo          address(es) of the recipient(s), if more than 1,
	*                     separate each recipient by a comma
	* @param aFrom        address or name of the sender
	*
	* @exception Exception Passes exception encountered up to the calling
	*						method.
	*/
	public SendMail(String aSMTPServer, String aTo, String aFrom) throws Exception
	{
      // Initialize the Environment
      Environment.init();
      
      // Get the DEBUG flag from the environment
      boolean logDebug = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();

      // Initialize the Logger
      Logger.init();
      Logger.debug("SendMail Constructor", logDebug);

		if (aSMTPServer == null || aSMTPServer.length() == 0)
			throw new Exception("SendMail constructor: " +
										"parameter [aSMTPServer] was blank.");

		if (aTo == null || aTo.length() == 0)
			throw new Exception("SendMail constructor: " +
										"parameter [aTo] was blank.");

		if (aFrom == null || aFrom.length() == 0)
			throw new Exception("SendMail constructor: " +
										"parameter [aFrom] was blank.");

		mSMTPServer = aSMTPServer;
		mTo = aTo;
		mFrom = aFrom;

	}

	/**
	 * Use this constructor if the same message needs to be broadcast to
	 * many different addresses.
	 */
	public SendMail(String aSMTPServer, String[] aToArray, String aFrom)
				throws Exception
	{
		Logger.init();

		// Delete when testing is done!
		Logger.log("SendMail constructor.");

		if (aSMTPServer == null || aSMTPServer.length() == 0)
			throw new Exception("SendMail constructor: " +
										"parameter [aSMTPServer] was blank.");

		if (aToArray == null || aToArray.length == 0)
			throw new Exception("SendMail constructor: " +
										"parameter [aToArray] was empty.");

		if (aFrom == null || aFrom.length() == 0)
			throw new Exception("SendMail constructor: " +
										"parameter [aFrom] was blank.");

		mSMTPServer = aSMTPServer;
		mMultiTo = aToArray;
		mFrom = aFrom;

   }


	////////////////////////////////////////////////////////////////////////////
	//  M E T H O D S
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Set the address(es) of mailbox(es) to send a COPY of the mail message.
	 * If more than one address is specified, separate the addresses by a comma.
	 */
	public void setCC(String aCCName)
	{
		if (aCCName != null && aCCName.length() > 0)
			mCC = aCCName;
   }

	/**
	 * Set the address(es) of mailbox(es) to send a blind COPY of the mail
	 * message.  If more than one address is specified, separate the 
	 * addresses by a comma.
	 */
	public void setBCC(String aBCCName)
	{
		if (aBCCName != null && aBCCName.length() > 0)
			mBCC = aBCCName;
   }

	/**
	 * Set the subject for the mail message.
	 */
	public void setSubject(String aSubject)
	{
		if (aSubject != null && aSubject.length() > 0)
			mSubject = aSubject;
   }

	/**
	 * Set the body of the mail message.
	 * @exception Exception  'aMessage' parameter cannot be blank.
	 */
	public void setMessage(String aMessage) throws Exception
	{
		if (aMessage == null || aMessage.length() == 0)
		{
			String vMsg = "SendMail.setMessage(): ";
			vMsg += "parameter [aMessage] was blank.";
			throw new Exception(vMsg);
		}

		mMessage = aMessage;
   }

	/**
	 * Set the body of the mail message from an input buffer (i.e. a file).
	 * 
	 * @exception Exception    'aMessage' parameter cannot be blank.
	 *                         Input buffer cannot be blank.
	 * @exception IOException  'readLine' IOException passed on.
	 */
	public void setMessage(BufferedReader aInputBuffer)
									throws IOException, Exception
	{
		if (aInputBuffer == null)
		{
			String vMsg = "SendMail.setMessage(): ";
			vMsg += "parameter [aInputBuffer] was blank.";
			throw new Exception(vMsg);
		}

		String vLine;
		StringBuffer vMessageBuffer = new StringBuffer();
		while ((vLine = aInputBuffer.readLine()) != null)
		{
			vMessageBuffer.append(vLine);
			vMessageBuffer.append("\n");
		}

		if (vMessageBuffer.length() == 0)
		{
			String vMsg = "SendMail.setMessage(): ";
			vMsg += "Input Buffer was blank.";
			throw new Exception(vMsg);
		}

		mMessage = vMessageBuffer.toString();
   }

	/**
	 * File name(s) to send with a mail message, as an attachment.
	 *
	 * @param aFilename[] = an array of filenames to send as attachments.
	 * @exception Exception   'aFilename[]' parameter cannot be empty.
	 */
	public void setFileAttachments(String[] aFilenames) throws Exception
	{
		if (aFilenames == null || aFilenames.length == 0)
		{
			String vMsg = "SendMail.setFileAttachments(): ";
			vMsg += "parameter [aFilenames] was empty.";
			throw new Exception(vMsg);
		}

		mFileAttachments = aFilenames;
   }

   /**
    * Sets the importance level of the message in the mail header.  The default
    * importance level is Normal, which is automatically done if no importance
    * is specified.
    *
    * @param aImportance    the importance level of the message typically
    *                       one of (Low, Normal, High).
    */
   public void setImportance(String aImportance)
   {
      if (aImportance != null && aImportance.length() > 0)
      {
         mImportance = aImportance;
      }
   }

	/**
	 * Send mail message to SMTP server.
	 */
	public void send() throws NoSuchProviderException, AddressException,
										MessagingException, Exception
	{
		if (mMessage == null || mMessage.length() == 0)
		{
			String vMsg = "SendMail.send(): ";
			vMsg += "member variable [mMessage] was blank.";
			throw new Exception(vMsg);
		}

		// Set mail user properties
		Properties vMailProperties = System.getProperties();
		vMailProperties.setProperty("mail.transport.protocol", mTransport);
		vMailProperties.setProperty(mTransportProp, mSMTPServer);

		// Get the default Session
		Session vSession = Session.getDefaultInstance(vMailProperties, null);

		// Create a new message
		MimeMessage vMimeMessage = new MimeMessage(vSession);

		// Initialize the message
		vMimeMessage.setFrom(new InternetAddress(mFrom));

		if (mCC != null && mCC.length() > 0)
			vMimeMessage.setRecipients(Message.RecipientType.CC,
										InternetAddress.parse(mCC, false));
		if (mBCC != null && mBCC.length() > 0)
			vMimeMessage.setRecipients(Message.RecipientType.BCC,
										InternetAddress.parse(mBCC, false));
		if (mSubject != null && mSubject.length() > 0)
			vMimeMessage.setSubject(mSubject);
      if (mImportance != null && mImportance.length() > 0)
    		vMimeMessage.setHeader("Importance", mImportance);

		vMimeMessage.setHeader("X-Mailer", mHeader);
		vMimeMessage.setSentDate(new Date());

		// Set message body.
		if (mFileAttachments != null && mFileAttachments.length > 0)
			setMultiPartMessage(vMimeMessage);
		else
			vMimeMessage.setText(mMessage);

		// Send the message)
		if (mTo != null && mTo.length() > 0)
		{
			vMimeMessage.setRecipients(Message.RecipientType.TO,
										InternetAddress.parse(mTo, false));
			Transport.send(vMimeMessage);
		}
		else if (mMultiTo != null && mMultiTo.length > 0)
		{
			vMimeMessage.saveChanges();
			sendMultipleMessages(vSession, vMimeMessage);
      }
		else
		{
			// Should never get to this section, as both of the constructors
			// require a "TO".
			String vMsg = "SendMail.send(): ";
			vMsg += "member variables [mTo] and [mMultiTo] were blank.";
			throw new Exception(vMsg);
      }

   }

	private void setMultiPartMessage(MimeMessage aMimeMessage) throws Exception
	{
		if (aMimeMessage == null)
		{
			String vMsg = "SendMail.setMultiPartMessage(): ";
			vMsg += "parameter [aMimeMessage] was null.";
			throw new Exception(vMsg);
		}
		if (mFileAttachments == null || mFileAttachments.length == 0)
		{
			String vMsg = "SendMail.setMultiPartMessage(): ";
			vMsg += "member variable [mFileAttachments] was empty.";
			throw new Exception(vMsg);
		}
		if (mMessage == null || mMessage.length() == 0)
		{
			String vMsg = "SendMail.setMultiPartMessage(): ";
			vMsg += "member variable [mMessage] was blank.";
			throw new Exception(vMsg);
		}

		// Create and fill the first message part
		MimeBodyPart vMessageBody = new MimeBodyPart();
		vMessageBody.setText(mMessage);

		// Create the Multipart mime message.
		Multipart vMultiPartMessage = new MimeMultipart();
		// Add the main message to the Multipart mime message.
		vMultiPartMessage.addBodyPart(vMessageBody);

		// Create attachment parts
		for (int i=0; i < mFileAttachments.length; i++)
		{
			// Create the next message part
			MimeBodyPart vAttachment = new MimeBodyPart();

			// Attach the file to the message
			FileDataSource vDataSource = new FileDataSource(mFileAttachments[i]);
			vAttachment.setDataHandler(new DataHandler(vDataSource));
			vAttachment.setFileName(vDataSource.getName());

			// Add the attachment to the Multipart mime message
			vMultiPartMessage.addBodyPart(vAttachment);
		}

		// Add the Multipart to the message
		aMimeMessage.setContent(vMultiPartMessage);

	}

	private void sendMultipleMessages(Session aSession, MimeMessage aMessage)
					throws NoSuchProviderException, MessagingException, Exception
	{
		if (aSession == null)
		{
			String vMsg = "SendMail.sendMultipleMessages(): ";
			vMsg += "parameter [aSession] was null.";
			throw new Exception(vMsg);
		}
		if (aMessage == null)
		{
			String vMsg = "SendMail.sendMultipleMessages(): ";
			vMsg += "parameter [aMessage] was null.";
			throw new Exception(vMsg);
		}
		if (mMultiTo == null || mMultiTo.length == 0)
		{
			String vMsg = "SendMail.setMultiPartMessage(): ";
			vMsg += "member variable [mMultiTo] was blank.";
			throw new Exception(vMsg);
		}

		Transport vTransport = null;
		try
		{
			vTransport = aSession.getTransport(mTransport);
			if (vTransport == null)
				throw new Exception("SendMail.sendMultipleMessages(): "
										+ "Unable to obtain a transport object.");

			vTransport.connect();
			for (int i=0; i < mMultiTo.length; i++)
			{
				aMessage.setRecipients(Message.RecipientType.TO,
										InternetAddress.parse(mMultiTo[i], false));
				vTransport.sendMessage(aMessage,
									InternetAddress.parse(mMultiTo[i], false));
         }
      }
      finally
      {
      	if (vTransport != null)
      		vTransport.close();
      }

   }

	public static void main(String[] args)
	{
		try
		{
/*************************************************************************
			1. Send mail one time to the mail server.
			2. Use a text file for the body of the mail.
			3. Send 2 files as attachments.
*************************************************************************/
			SendMail vMail = new SendMail("smtp.netwiz.net",
									"kgentes@tacpoint.com,kim_tacpoint@yahoo.com",
									"kimg@netwiz.net");
			vMail.setCC("kimg@netwizards.net");
			vMail.setSubject("Test SendMail.java for text");

			// Open file for reading.
			BufferedReader vFile = new BufferedReader(
											new FileReader("c:\\temp\\message.txt"));
			vMail.setMessage(vFile);

			String[] vFilenames = new String[2];
			vFilenames[0] = new String("c:\\temp\\coms1.txt");
			vFilenames[1] = new String("c:\\temp\\gal2.jpg");
			vMail.setFileAttachments(vFilenames);

			vMail.send();
			vFile.close();

/*************************************************************************
			1. Send mail 2 times to the mail server.
			2. Use a string for the body of the mail.
			3. No attachments.
*************************************************************************/
			String[] vMultiTo = new String[2];
			vMultiTo[0] = "kgentes@tacpoint.com,kim_tacpoint@yahoo.com";
			vMultiTo[1] = "kimg@netwizards.net";

			vMail = new SendMail("smtp.netwiz.net", vMultiTo, "kimg@netwiz.net");

			vMail.setSubject("Test SendMail.java for text");
			vMail.setMessage("1. Testing\n2. New message\n");
			vMail.send();

      }
		catch(FileNotFoundException e)
		{
			System.out.println("FileNotFoundException: " + e.getMessage());
		}
		catch(IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
		}
      catch(NoSuchProviderException e)
      {
      	System.out.println("NoSuchProviderException: " + e.getMessage());
      }
      catch(AddressException e)
      {
      	System.out.println("AddressException: " + e.getMessage());
      }
      catch(MessagingException e)
      {
      	System.out.println("Messaging Exception: " + e.getMessage());
      }
      catch(Exception e)
      {
      	System.out.println("Exception: " + e.getMessage());
      }

   }
}
