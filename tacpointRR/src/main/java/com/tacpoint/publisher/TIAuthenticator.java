/**
  * TIAuthenticator.java
  *
  * @author Copyright (c) 2003 by Tacpoint Technologie, Inc.
  *    All rights reserved.
  * @version 1.0
  * Oct 29, 2003
  */

package com.tacpoint.publisher;

public interface TIAuthenticator
{
   public boolean authenticate(String sessionID);
}
