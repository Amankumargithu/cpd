/**
  * TPushHttpd.java
  *
  * @author Copyright (c) 2003 by Tacpoint Technologie, Inc.
  *    All rights reserved.
  * @version 1.0
  * Created on Sep 18, 2003
  */
package com.tacpoint.publisher;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;


public final class TPushHttpd implements Runnable
{
   private static Log log = LogFactory.getLog(TPushHttpd.class);
   
   // default values
   private static final int DEFAULT_PUSH_PORT = 8080;
   private static final int DEFAULT_ACCEPT_THREAD_POOL_COUNT = 2;

   private TPublisher publisher;
   private int pushPort = DEFAULT_PUSH_PORT;
   private PooledExecutor acceptPool;
   
   public TPushHttpd(TPublisher p)
   {
      log.debug("TPushHttpd.constructor()");
      
      publisher = p;
      TPublisherConfigBean bean = publisher.getConfiguration();
      
      pushPort = bean.getPushPort();
      if (pushPort <= 0)
         pushPort = DEFAULT_PUSH_PORT;
         
      int acceptThreadPoolCount = bean.getAcceptThreadPoolCount();
      if (acceptThreadPoolCount <= 0)
         acceptThreadPoolCount = DEFAULT_ACCEPT_THREAD_POOL_COUNT;
      
      acceptPool = new PooledExecutor(acceptThreadPoolCount);
   }

   public void run()
   {
      log.debug("TPushHttpd.run() port= " + pushPort);  
      try
      {
         InetSocketAddress localhost = new InetSocketAddress(pushPort);
              
         // get the channel and multiplexor for non-blocking read and write
         Selector acceptSelector = Selector.open();
         ServerSocketChannel mainChannel = ServerSocketChannel.open();
         mainChannel.configureBlocking(false);
         mainChannel.socket().bind(localhost);
              
         // register interest in accepting connections
         mainChannel.register(acceptSelector, SelectionKey.OP_ACCEPT, null);
              
         // when there are avaliable incoming connections then start accepting
         while (true)
         {
            try {

               if (acceptSelector.select() <= 0) {
                  log.debug("select finished ... continuing!");
                  continue;  // do nothing
               } 

               // get the set of ready connections
               Set readyKeys = acceptSelector.selectedKeys();
               Iterator readySet = readyKeys.iterator();
               // walk through set
               while (readySet.hasNext())
               {
                  // get acceptance key from set
                  SelectionKey acceptKey = (SelectionKey)readySet.next();

                  if (acceptKey.isAcceptable())
                  {
                     // get the client channel
                     ServerSocketChannel keyChannel = (ServerSocketChannel)acceptKey.channel();

                     try
                     {
                        // accept connection and configure socket non blocking
                        SocketChannel acceptedChannel = keyChannel.accept();
                     
                        // pass to thread pool to register user
                        TRegisterCommand cmd = new TRegisterCommand(publisher, acceptKey, acceptedChannel);
                        acceptPool.execute(cmd);
                     }
                     catch (Exception acceptException)
                     {
                        log.error(acceptException.getMessage(),acceptException);
                     }
                  } 
                  else
                  {
                     log.error("Incoming connection not acceptable.");
                  }
               
                  // remove current entry from iterator
                  readySet.remove();
               } // end while readyset
            }
            catch (Exception outerEx) {
               log.error("Exception encountered - "+outerEx.getMessage(),outerEx);
            }
         } // end while select
      }
      catch(Exception e)
      {
         log.error("Exception occured during initialization - terminating process : "+e.getMessage(),e);
         System.exit(0);
      }
   }
}
