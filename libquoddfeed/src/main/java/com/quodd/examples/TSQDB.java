/******************************************************************************
*
*  TSQDB.java
*     Query the UC Time/Sales/Quotes Capability
*
*  REVISION HISTORY:
*     15 JAN 2014 jcs  Created.
*     17 JAN 2014 jcs  Build 74: MsgType Filter
*     26 FEB 2014 jcs  Build 75: VWAP
*     23 JUN 2015 jcs  Build 96: Dump()
*
*  (c) 2011-2015 Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//               c l a s s   T S Q D B
//
/////////////////////////////////////////////////////////////////
/**
 * This example shows you how to open a subscription stream from UltraCache
 * via the {@link UltraChan#QueryTSQ(String, String, String, Object obj )}
 * method and proces streaming market data via the 
 * {@link QuoddFeed.util.UltraChan#OnUpdate} callbacks.
 */
public class TSQDB extends UltraChan
{
   private String      _tkr;
   private String      _tBeg;
   private String      _tEnd;
   private String      _msgTy;
   private boolean     _bKid;
   private boolean     _bVwap;
   private EQTrade     _vwap0;
   private EQTrade     _vwap1;
   private String      _arg;
   private Object      _eof;
   private PrintStream cout;

   //////////////////////
   // Constructor
   //////////////////////
   TSQDB( String tkr, String tBeg, String tEnd, String msgTy, boolean bKid )
   {
      this( UCconfig.Hostname(),
            UCconfig.Port(),
            "username",
            "password",
            tkr,
            tBeg,
            tEnd,
            msgTy, bKid );
   }

   TSQDB( String  uHost,
          int     uPort,
          String  uUsr,
          String  uPwd,
          String  tkr,
          String  tBeg,
          String  tEnd,
          String  msgTy,
          boolean bKid )
   {
      super( uHost, uPort, uUsr, uPwd, false );
      _tkr   = tkr;
      _tBeg  = tBeg;
      _tEnd  = tEnd;
      _msgTy = msgTy;
      _bKid  = bKid;
      _bVwap = msgTy.equals( "VWAP" );
      _msgTy = _bVwap ? "TRADE" : _msgTy;
      _vwap0 = null;
      _vwap1 = null;
      _arg   = "User-defined closure for Subscribe.java";
      _eof   = new Object();
      cout   = System.out;
   }


   //////////////////////
   // Operations
   //////////////////////
   /**
    * Query Time/Sales/Quote DB
    */
   public void Query()
   {
      int numRes = 0;

      QueryTSQ( _tkr, _tBeg, _tEnd, _msgTy, _bKid, numRes, _arg, null );

      // Wait for EOF

      try {
         synchronized( _eof ) {
            _eof.wait();
         }
      } catch( InterruptedException e ) {
         cout.printf( "%s\n", e.getMessage() );
      }
      StopQ();
   }


   //////////////////////
   // UltraChan Interface
   //////////////////////
   /**
    * Called when we connect to UltraCache.  We do nothing
    */
   public void OnConnect()
   {
      cout.printf( "CONNECT %s:%d\n", uHost(), uPort() );
   }

   /**
    * Called when we disconnect from UltraCache.  We do nothing here.
    */
   public void OnDisconnect()
   {
      cout.printf( "DISCONNECT %s:%d\n", uHost(), uPort() );
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Called when an unknown QuoddMsg is received.  Should never be called.
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      char mt;

      // Open Complete?

      if  ( (mt=qm.mt()) == _mtSTREAM_OPN ) {
         cout.printf( "OpenComplete()\n" );
         return;
      }

      // Else something else ...

      cout.printf( qm.Dump() );
   }

   /**
    * Called when a data stream is closed.
    */
   public void OnStatus( String StreamName, Status sts )
   {
      double vwap;
      long   num, den;

      cout.printf( sts.Dump() );
      if ( _bVwap ) {
         vwap = 0.0;
         if ( _vwap0 != null ) {
            num = _vwap1._tnOvr - _vwap0._tnOvr;
            den = _vwap1._acVol - _vwap0._acVol;
            vwap = ( den == 0 ) ? _vwap0._vwap : (double)num / (double)den;
         }
         cout.printf( "VWAP {%s,%s} = %.4f\n", _tBeg, _tEnd, vwap );
      }

      // Wake up calling thread

      synchronized( _eof ) {
         _eof.notify();
      }
   }

   /**
    * Called when an EQQuote update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuote q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an EQBbo update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBbo q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an EQQuoteMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuoteMM q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an EQBboMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBboMM q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an EQTrade update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      String pt, pm;

      // VWAP

      if ( _bVwap ) {
         _vwap0 = ( _vwap0 == null ) ? trd.clone() : _vwap0;
         _vwap1 = trd.clone();
         return;
      }
      cout.printf( trd.Dump() );
   }

   /**
    * Called when an EQTradeSts update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTradeSts sts )
   {
      cout.printf( sts.Dump() );
   }

   /**
    * Called when an EQMktStats update is received.
    */
   public void OnUpdate( String StreamName, EQMktStats st )
   {
      cout.printf( st.Dump() );
   }

   /**
    * Called when an EQLimitUpDn update is received.
    */
   public void OnUpdate( String StreamName, EQLimitUpDn lim )
   {
      cout.printf( lim.Dump() );
   }

   /**
    * Called when an OPQuote update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPQuote q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an OPBbo update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPBbo q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an OPTrade update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPTrade trd )
   {
      cout.printf( trd.Dump() );
   }

   /**
    * Called when an IDXValue update is received for an index ticker.
    */
   public void OnUpdate( String StreamName, IDXValue ix )
   {
      cout.printf( ix.Dump() );
   }

   /**
    * Called when an IDXSummary update is received for an index ticker.
    */
   public void OnUpdate( String StreamName, IDXSummary q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when FUNDnav message is received
    */
   public void OnUpdate( String StreamName, FUNDnav q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when FUTRTrade message is received
    */
   public void OnUpdate( String StreamName, FUTRTrade trd )
   {
      cout.printf( trd.Dump() );
   }

   /**
    * Called when an Futures Summary is received.
    */
   public void OnUpdate( String StreamName, FUTRSumm sum )
   {
      cout.printf( sum.Dump() );
   }

   /**
    * Called when an Futures HiLoLast or OpenInt is received.
    */
   public void OnUpdate( String StreamName, FUTRMisc msc )
   {
      cout.printf( msc.Dump() );
   }

   /**
    * Called when FUTRQuote message is received
    */
   public void OnUpdate( String StreamName, FUTRQuote q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an BONDQuote update is received for an Bond ticker.
    */
   public void OnUpdate( String StreamName, BONDQuote q )
   {
      cout.printf( q.Dump() );
   }

   /**
    * Called when an BONDTrade update is received for an Bond ticker
    */
   public void OnUpdate( String StreamName, BONDTrade trd )
   {
      cout.printf( trd.Dump() );
   }


   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      TSQDB   tsq;
      Scanner sc;
      String  tkr, t0, t1, mt;
      boolean kid;
      int     argc;

      // Quick check

      argc = args.length;
      if ( ( argc > 0 ) && args[0].equals( "--version" ) ) {
         System.out.printf( "%s\n", QuoddMsg.Version() );
         return;
      }
      tkr = ( argc > 0 ) ? args[0] : "SPY";
      t0  = ( argc > 1 ) ? args[1] : "09:30";
      t1  = ( argc > 2 ) ? args[2] : "16:00";
      mt  = ( argc > 3 ) ? args[3] : "";
      kid = ( argc > 4 ) ? args[4].equals( "KID" ) : false;
System.out.printf( "kid = %s; argc=%d\n", kid ? "YES" : "NO", argc );
      tsq = new TSQDB( tkr, t0, t1, mt, kid );
      tsq.StartQ( 10485760 ); // 10 million msgs
      tsq.Start();
      tsq.Query();
      tsq.Stop();
      System.out.printf( "Done!!\n" );
   }
}
