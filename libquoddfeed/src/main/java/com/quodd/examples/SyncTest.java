/******************************************************************************
*
*  SyncTest.java
*    Sync function exerciser
*
*  REVISION HISTORY:
*     15 MAY 2012 jcs  Created.
*     28 JUN 2012 jcs  Build 27: UCconfig
*
*  (c) 2011-2012, Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.lang.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;

public class SyncTest implements Runnable
{
   private UltraChan _uChan;
   private boolean   _bSnap;
   private Thread    _thr;
   private boolean   _bRun;

   //////////////////////
   // Constructor
   //////////////////////
   public SyncTest( UltraChan uChan, boolean bSnap )
   {
      _uChan = uChan;
      _bSnap = bSnap;
      _thr   = new Thread( this );
      _bRun  = true;
      _thr.start();
   }

   //////////////////////
   // Runnable Interface
   //////////////////////
   public void run()
   {
      Image     img;
      BlobTable tbl;

      while( _bRun ) {
         try {
            if ( _bSnap ) {
               Thread.sleep( 37 );
               img = (Image)_uChan.SyncSnap( "DELL", null );
               System.out.printf( "Image\n" );
            }
            else {
               Thread.sleep( 31 );
               tbl = (BlobTable)_uChan.SyncGetExchanges( null );
               System.out.printf( "BlobTable\n" );
            }
         } catch( InterruptedException ex ) {
            _bRun = false;
         }
      }
   }

   public void Stop()
   {
      _bRun = false;
   }

   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      UltraChan uChan;
      SyncTest  snp, opt;
      Scanner   sc;
      int       argc;

      argc  = args.length;
      uChan = new UltraChan( UCconfig.Hostname(),
                             UCconfig.Port(), 
                             "usr", 
                             "pwd", 
                             false );
      uChan.Start();
      snp = null;
      opt = new SyncTest( uChan, false );
      if ( argc > 0 )
         snp = new SyncTest( uChan, true );
      System.out.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      sc.nextLine();
      System.out.printf( "Shutting down ...\n" );
      if ( snp != null )
         snp.Stop();
      opt.Stop();
      uChan.Stop();
      System.out.printf( "Done!!\n" );
   }
}
