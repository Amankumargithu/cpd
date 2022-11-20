/**
 * TArithCompressedSelector.java
 *
 * @author Copyright (c) 2006 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;

public class TArithCompressedSelector extends com.tacpoint.publisher.TSelector

{

   protected HashMap streamMap = new HashMap();
   protected HashMap compMap = new HashMap();

   public TArithCompressedSelector() {
      log = LogFactory.getLog(TArithCompressedSelector.class);
   }

   //
   // need to override unregister method to clean up outputstream map and byte array outputstream map
   //
   public void unregister(SocketChannel sc, String userid, Vector interestList) {

      try {
         synchronized (streamMap) {
            streamMap.remove(userid);
         }

         synchronized (compMap) {
            compMap.remove(userid);
         }
      }
      catch (Exception uex) {
         log.error("Exception encountered while attempting to unregister socket channel and user ID - "+uex.getMessage(),uex);
      }

      super.unregister(sc, userid, interestList);

      log.debug("Current stream map size : "+streamMap.size());
      log.debug("Current comp map size   : "+compMap.size());

   }


   //
   // need to override processCancelKey method to clean up outputstream map and byte array outputstream map
   //
   protected void processCancelKey(SocketChannel writeChannel, SelectionKey writeKey) {

      try {
         String userid = ((TAttachKey)writeKey.attachment()).userId;
         streamMap.remove(userid);
         compMap.remove(userid);
      }
      catch (Exception wkEx) {
         log.error("Exception encountered while attempting to cancel write key - "+wkEx.getMessage(),wkEx);
      }

      super.processCancelKey(writeChannel, writeKey);

      log.debug("Current stream map size : "+streamMap.size());
      log.debug("Current comp map size   : "+compMap.size());

   }

   protected void feedChannels(int maxMessageIndex) {

      SocketChannel writeChannel = null;
      SelectionKey writeKey = null;

      TAttachKey attachKey = null;

      TMessage message = null;
      ByteBuffer buffer = null;
      ByteArrayOutputStream compBaos = null;

      String userid = null;

      // Walk through set
      readySet = readyKeys.iterator();

      long beginTime = System.currentTimeMillis();
      int chCount = 0;

      while (readySet.hasNext())
      {
         try
         {
            // Get key from set
            writeKey = (SelectionKey)readySet.next();

            // Remove current entry

            //**AK**
            //readySet.remove();

            attachKey = (TAttachKey)writeKey.attachment();
            userid = attachKey.userId;

            if (writeKey.isWritable() && writeKey.isValid())
            {

               chCount++;

               // Get channel
               writeChannel = (SocketChannel)writeKey.channel();
               if ( writeChannel != null )
               {

                  HashMap interestHash = attachKey.topics;

                  if (interestHash == null) {
                     log.error("TArithCompressedSelector.run - Interest hash is null for user : "+userid);
                     processCancelKey(writeChannel, writeKey);
                     continue;
                  }

                  int curMsgCount = 0;
                  long now = System.currentTimeMillis();

                  OutputStream os = (OutputStream)compMap.get(userid);
                  if (os == null) {
                     compBaos = new ByteArrayOutputStream(1024);
                     os = new net.paxcel.utils.arith.ArithCodeOutputStream(compBaos,1 /* compression level [1 - 5] */);
                     streamMap.put(userid,compBaos);
                     compMap.put(userid,os);
                  }

                  compBaos = (ByteArrayOutputStream)streamMap.get(userid);

                  // clear out *old* bytes ...
                  compBaos.reset();

                  for (int i=0; i<maxMessageIndex; i++) {
                     message = messages[i];

                     if (interestHash.containsKey(message.getKey())) {
                        os.write(message.getBaos().toByteArray());
                     }
                  }

                  int clength = compBaos.toByteArray().length;

                  //System.out.println("Actual byte count : "+blength+" compressed byte count : "+clength);

                  if (clength > 0) {
                     buffer = ByteBuffer.wrap(compBaos.toByteArray());

                     int curBytesWritten = writeChannel.write(buffer);

                     atByteCount += clength;

                     if (curBytesWritten > 0) {
                        attachKey.lastWriteTime = now;
                     }
                     else {
                        log.error("TArithCompressedSelector.run - no bytes written to channel for user : "+userid);
                     }

                     acByteCount += curBytesWritten;
                     ++curMsgCount;
                     ++messagesSentCount;

                     if (buffer.hasRemaining()) {
                        acByteCount += writeChannel.write(buffer);
                     }
                  }
               }
            }
            else {
               log.info("Write Key is invalid or not writeable for User ID : "+userid);
               readySet.remove();
               processCancelKey(writeChannel, writeKey);
            }
         }
         catch (java.util.ConcurrentModificationException cme) {
            log.debug("TArithCompressedSelector ["+id+"] Concurrent mod ex.",cme);
         }
         catch(Exception e) {
            try {
               log.info("Processing runtime exception for User ID : "+userid+" - "+e.getMessage(),e);
               readySet.remove();
               processCancelKey(writeChannel, writeKey);
            }
            catch (Exception pckEx) {}
         }
      } // while hasNext

      long endTime = System.currentTimeMillis();

      System.out.println("TArithCompressedSelector["+id+"] time to write to ["+chCount+"] channels : "+(endTime-beginTime));

   }

}



