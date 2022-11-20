package com.b4utrade.helper;

import java.security.MessageDigest;
import java.util.Arrays;


public class PasswordEncrypter 
{
	public static final String ENCRYPT_PREFIX = "eNC_p!!";
    private final String ALGORITHM_TYPE = "SHA-256";    
    
    public  String encrypt(char[] pass) throws Exception 
	{
    	if(isPasswordAlreadyEncrypted(pass))
    	{
    		//return the input paramter's original contents in form of String
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < pass.length; i++) 
			{
				buffer.append(pass[i]);
			}
			return buffer.toString();
    	}
    	
    	MessageDigest d = MessageDigest.getInstance(ALGORITHM_TYPE);	   
	   	d.reset();
	   	
	    byte [] bytesPass = convertCharArrayToByteArray(pass);
	    //Empty the char array in order to maintain password sneaking security
	    Arrays.fill(pass, ' ');
	    
	    d.update(bytesPass);
	    byte[] array = d.digest();
	    
	    StringBuffer buffer = new StringBuffer();
	    for (int i = 0; i < array.length; i++) {
			
	    	buffer.append(array[i]);
		}
	    
	    return  ENCRYPT_PREFIX + buffer.toString();
	}
    
    private byte[] convertCharArrayToByteArray(char[] ca) 
    {        	
    	
   	     byte[] ba = new byte[ca.length];
   	     for (int i = 0; i < ca.length; i++) 
   	     {
   	    	 	ba[i] = (byte) ca[i];
   	     }
   	     return ba;
	} 
    
    private boolean isPasswordAlreadyEncrypted(char[] charPass)
    {
    	if(charPass.length <= ENCRYPT_PREFIX.length())
    	{
    		return false;
    	}
    
    	for(int i=0; i<ENCRYPT_PREFIX.length(); i++)
    	{
    		if(charPass[i] != ENCRYPT_PREFIX.charAt(i) )
    		{
    			return false;
    		}
    		
    	}                	        	
    	return true;
    }
    
    
}

