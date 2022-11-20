/**
 * TSelectorRegistration.java
 *
 * @author Copyright (c) 2006 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import java.io.InputStream;
import java.io.OutputStream;


public class TSelectorRegistration {

   SocketChannel socketChannel;
   String userId;
   Vector interestList;

   public TSelectorRegistration() {}

   public void setSocketChannel(SocketChannel socketChannel) {
      this.socketChannel = socketChannel;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public void setInterestList(Vector interestList) {
      this.interestList = interestList;
   }

   public Vector getInterestList() {
      return interestList;
   }

   public String getUserId() {
      return userId;
   }

   public SocketChannel getSocketChannel() {
      return socketChannel;
   }

}


