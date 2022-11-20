/**HttpAuthenticator.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
All rights reserved.
* @author Paul Kim
* @author pkim@tacpoint.com
* @version 1.0
* Date created:  1/18/2000
*
*
* Description
*----------------------------------------------------
*Class for basic HTTP authentication
**/
package com.tacpoint.network;
import java.net.*;
public class HttpAuthenticator extends Authenticator
{
	private String mUsername;
	private String mPassword;

	public HttpAuthenticator(String aUsername, String aPassword)
	{
		mUsername = aUsername;
		mPassword = aPassword;
	}
	protected PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication(
								mUsername,
								mPassword.toCharArray());

	}
}
