/**
 * TLZWCompressedSelector.java
 *
 * @author Copyright (c) 2006 by Tacpoint Technologie, Inc.
 *    All rights reserved.
 * @version 1.0
 */
package com.tacpoint.publisher;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import org.apache.commons.logging.LogFactory;

public class TLZWCompressedSelector extends com.tacpoint.publisher.TSelector
{

   public TLZWCompressedSelector() {
      log = LogFactory.getLog(TLZWCompressedSelector.class); 
   }

   protected void feedChannels(int maxMessageIndex) {

      log.debug("TLZWCompressedSelector["+id+"] - begin feed channels.");

      SocketChannel writeChannel = null;
      SelectionKey writeKey = null;

      TAttachKey attachKey = null;

      TMessage message = null;
      ByteBuffer buffer = null;

      String userid = null;

      // Walk through set
      readySet = readyKeys.iterator();

      long beginTime = System.currentTimeMillis();
      int chCount = 0;

      while (readySet.hasNext())
      {
         try
         {
            writeKey = (SelectionKey)readySet.next();
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
                     log.error("TLZWCompressedSelector.run - Interest hash is null for user : "+userid);
                     processCancelKey(writeChannel, writeKey);
                     continue;
                  }
                  int curMsgCount = 0;
                  long now = System.currentTimeMillis();
                  baos.reset();
                  for (int i=0; i<maxMessageIndex; i++) {
                     message = messages[i];

                     if (interestHash.containsKey(message.getKey())) {
                        baos.write(message.getBaos().toByteArray());
                     }
                  }
                  if (baos.size() > 0) {

                     // initialize compressed streams if necessary ...
                     if (attachKey.os == null) {
                        attachKey.baos = new ByteArrayOutputStream(1024);
                        attachKey.os = new net.paxcel.utils.compression.LZWOutputStream(attachKey.baos);
                     }
                     attachKey.baos.reset();
                     byte[] uncompedBytes = baos.toByteArray();
                     attachKey.os.write(uncompedBytes);
                     byte[] compedBytes = attachKey.baos.toByteArray();
                     
                     int blength = uncompedBytes.length;                   
                     int clength = compedBytes.length;

                     if (clength == 0)
                        log.debug("TSelector["+id+"] - Actual byte count : "+blength+" compressed byte count : "+clength);
                     buffer = ByteBuffer.wrap(compedBytes);
                     int curBytesWritten = writeChannel.write(buffer);
                     atByteCount += blength;
                     if (curBytesWritten > 0 || clength == 0) {
                        attachKey.lastWriteTime = now;
                     }
                     acByteCount += curBytesWritten;
                     ++curMsgCount;
                     ++messagesSentCount;

                     if (buffer.hasRemaining()) {
                    	 acByteCount += writeChannel.write(buffer);
                    	 if (buffer.hasRemaining())
                    	 {
                    		 readySet.remove();
                             processCancelKey(writeChannel, writeKey);
                    	 }
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
            log.debug("TLZWCompressedSelector ["+id+"] Concurrent mod ex.",cme);
         }
         catch(Exception e) {
            try {
               log.info("Processing runtime exception for User ID : "+userid+" - "+e.getMessage());
               readySet.remove();
               processCancelKey(writeChannel, writeKey);
            }
            catch (Exception pckEx) {}
         }
      } // while hasNext
      long endTime = System.currentTimeMillis();
      log.debug("TLZWCompressedSelector["+id+"] end feed channels.  Time to write to ["+chCount+"] channels : "+(endTime-beginTime));
   }
}
