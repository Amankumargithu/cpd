/******************************************************************************
*
*  PumpTSQ.java
*     Pump TSQ via TSQPump
*
*  REVISION HISTORY:
*     25 MAY 2015 jcs  Created.
*     24 JUN 2015 jcs  Build 94: <QuoddMsg>.Dump()
*
*  (c) 2011-2015 Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.MD.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//               c l a s s   T S Q R e c e i v e r
//
/////////////////////////////////////////////////////////////////

/**
 * IUpdate consumer of TSQ messages
 */
class TSQReceiver extends IUpdate
{
   private TSQPump     _pmp;
   private PrintStream pout;
   private PrintStream cout;
   private boolean     _bMsgLog;

   //////////////////////
   // Constructor
   //////////////////////
   TSQReceiver( TSQPump pmp, PrintStream ps, boolean bMsgLog )
   {
      _pmp     = pmp;
      pout     = ps;
      cout     = System.out;
      _bMsgLog = bMsgLog;
   }


   //////////////////////
   // Operations
   //////////////////////
   void PumpAll( String t0, String t1, boolean bSort )
   {
      long tot;

      tot = _pmp.PumpAllTo( t0, t1, bSort, this );
      cout.printf( "[%s] %d msgs in total\n", _pmp.Now(), tot );
   }

   void PumpThreaded( int i, String t0, String t1, String[] tkrs )
   {
      TSQReceiverThread thr;

      thr = new TSQReceiverThread( this, i, t0, t1, tkrs );
   }

   void PumpTicker( String t0, String t1, String tkr )
   {
      long tot;

      tot = _pmp.PumpTickerTo( t0, t1, tkr, this );
      cout.printf( "[%s] %d msgs in total\n", _pmp.Now(), tot );
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Called when an unknown QuoddMsg is received.  Should never be called.
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      if ( _bMsgLog )
         pout.print( qm.Dump() );
      else
         pout.printf( "%s\n", qm.DumpHdr() );
   }

   /**
    * Called when an EQQuote update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuote q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an EQBbo update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBbo q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an EQQuoteMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuoteMM q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an EQBboMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBboMM q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an EQTrade update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      if ( _bMsgLog )
         pout.print( trd.Dump() );
      else
         pout.printf( "%s\n", trd.DumpHdr() );
   }

   /**
    * Called when an EQTradeSts update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTradeSts sts )
   {
      if ( _bMsgLog )
         pout.print( sts.Dump() );
      else
         pout.printf( "%s\n", sts.DumpHdr() );
   }

   /**
    * Called when an EQLimitUpDn update is received.
    */
   public void OnUpdate( String StreamName, EQLimitUpDn lim )
   {
      if ( _bMsgLog )
         pout.print( lim.Dump() );
      else
         pout.printf( "%s\n", lim.DumpHdr() );
   }

   /**
    * Called when an OPQuote update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPQuote q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an OPBbo update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPBbo q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an OPTrade update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPTrade trd )
   {
      if ( _bMsgLog )
         pout.print( trd.Dump() );
      else
         pout.printf( "%s\n", trd.DumpHdr() );
   }

   /**
    * Called when an IDXValue update is received for an index ticker.
    */
   public void OnUpdate( String StreamName, IDXValue ix )
   {
      if ( _bMsgLog )
         pout.print( ix.Dump() );
      else
         pout.printf( "%s\n", ix.DumpHdr() );
   }

   /**
    * Called when an IDXSummary update is received for an index ticker.
    */
   public void OnUpdate( String StreamName, IDXSummary q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when FUNDnav message is received
    */
   public void OnUpdate( String StreamName, FUNDnav q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when FUTRTrade message is received
    */
   public void OnUpdate( String StreamName, FUTRTrade trd )
   {
      if ( _bMsgLog )
         pout.print( trd.Dump() );
      else
         pout.printf( "%s\n", trd.DumpHdr() );
   }

   /**
    * Called when an Futures Summary is received.
    */
   public void OnUpdate( String StreamName, FUTRSumm q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an Futures HiLoLast or OpenInt is received.
    */
   public void OnUpdate( String StreamName, FUTRMisc q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when FUTRQuote message is received
    */
   public void OnUpdate( String StreamName, FUTRQuote q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an BONDQuote update is received for an Bond ticker.
    */
   public void OnUpdate( String StreamName, BONDQuote q )
   {
      if ( _bMsgLog )
         pout.print( q.Dump() );
      else
         pout.printf( "%s\n", q.DumpHdr() );
   }

   /**
    * Called when an BONDTrade update is received for an Bond ticker
    */
   public void OnUpdate( String StreamName, BONDTrade trd )
   {
      if ( _bMsgLog )
         pout.print( trd.Dump() );
      else
         pout.printf( "%s\n", trd.DumpHdr() );
   }


   //////////////////////
   // Helpers
   //////////////////////
   private String[] _TickersFromFile( String[] tkrs )
   {
      String[]        rtn;
      String          line;
      BufferedReader  buf;
      HashSet<String> hdb;
      int             i, n;

      try {
         buf = new BufferedReader( new FileReader( tkrs[0] ) );
         hdb = new HashSet<String>();
         for ( ; (line=buf.readLine()) != null; hdb.add( line ) );
         buf.close();
         n   = hdb.size();
         rtn = new String[n];
         i   = 0;
         for ( String tkr : hdb ) 
            rtn[i++] = tkr;
      } catch( Exception e ) {
         rtn = tkrs;
      }
      return rtn;
   }


   //////////////////////////////////////////////////
   //
   // c l a s s   T S Q R e c e i v e r T h r e a d
   //
   //////////////////////////////////////////////////
   class TSQReceiverThread implements Runnable
   {
      //////////////////////
      // Instance Members
      //////////////////////
      TSQReceiver _rcv;
      TSQPump     _pmp;
      int         _nThr;
      String      _t0;
      String      _t1;
      String[]    _tkrs;
      Thread      _thr;

      ///////////////////////////////
      // Constructor
      ///////////////////////////////
      TSQReceiverThread( TSQReceiver rcv, 
                         int         nThr,
                         String      t0, 
                         String      t1, 
                         String[]    tkrs )
      {
         _rcv  = rcv;
         _pmp  = rcv._pmp;
         _nThr = nThr;
         _t0   = t0;
         _t1   = t1;
         _tkrs = tkrs;
         _thr  = new Thread( this, "TSQReceiverThread" );
         _thr.start();
      }


      ///////////////////////////////
      // Runnable Interface
      ///////////////////////////////
      public void run()
      {
         String tkr, fmt;
         double dd;
         long   tm0, tot;
         int    i, tid;

         for ( i=0; i<_tkrs.length; i++ ) {
            tkr = _tkrs[i];
            tm0 = System.currentTimeMillis();
            tid = _pmp.PumpTickerThreaded( _t0, _t1, tkr, _rcv );
            fmt = "[%s] [%d.%d] PumpThreaded( %s )\n";
            cout.printf( fmt, _pmp.Now(), _nThr, i, tkr );
            tot = _pmp.WaitPumpThread( tid );
            dd  = 0.001 * ( System.currentTimeMillis() - tm0 );
            fmt = "[%s] [%d.%d] %d msgs for %s in %.3fs\n";
            cout.printf( fmt, _pmp.Now(), _nThr, i, tot, tkr, dd );
         }
      }
   }
   // class TSQReceiverThread

} // class TSQReceiver


/////////////////////////////////////////////////////////////////
//
//               c l a s s   P u m p T S Q
//
/////////////////////////////////////////////////////////////////

/**
 * This example shows you how to use TSQPump to pump messages from the TSQ 
 * data file.
 */
public class PumpTSQ extends TSQPump
{
   private PrintStream cout;

   //////////////////////
   // Constructor
   //////////////////////
   public PumpTSQ( String idxFile, String dataFile )
   {
      super( idxFile, dataFile );
      cout = System.out;
   }


   //////////////////////
   // TSQPump Interface
   //////////////////////
   /**
    * \brief Callback invoked when Index File is loading
    *
    * @param idxFile Index filename
    */
   public void OnIndexFileStart( String idxFile )
   {
      cout.printf( "[%s] INDEX file %s loading ...\n", Now(), idxFile );
   }

   /**
    * \brief Callback invoked when Index File has been loaded
    *
    * @param idxFile Index filename
    */
   public void OnIndexFileLoaded( String idxFile )
   {
      cout.printf( "[%s] INDEX file %s loaded\n", Now(), idxFile );
   }

   /**
    * \brief Callback invoked when Data File has been loaded
    *
    * @param dataFile Index filename
    */
   public void OnDataFileLoaded( String dataFile )
   {
      cout.printf( "[%s] DATA  file %s loaded\n", Now(), dataFile );
   }

   /**
    * \brief Callback invoked when query begins
    *
    * @param bSorted true if sorted query
    */
   public void OnQueryStart( boolean bSorted )
   {
      String ty = bSorted ? "SORTED" : "UNSORTED";

//      cout.printf( "[%s] OnQueryStart( %s )\n", Now(), ty );
   }

   /**
    * \brief Callback invoked when data file is mapped
    *
    * @param dFile Name of file just mapped in
    * @param tm Time slice mapped in HH:MM:SS
    * @param off Mapped file window offset
    * @param len Mapped file window len
    */
   public void OnFileMapped( String dFile, String tm, long off, long len )
   {
      String fmt; 
      long   oMB, lMB;

      oMB = off / ( _K*_K );
      lMB = len / ( _K*_K );
      fmt = "[%s] %s mapped at %dMB for %dMB\n";
//      cout.printf( fmt, Now(), tm, oMB, lMB );
   }

   /**
    * \brief Callback invoked when TSQ query is complete
    *
    * @param tot Total msgs queried 
    */
   public void OnQueryComplete( long total )
   {
//      cout.printf( "[%s] OnQueryComplete( %d )\n", Now(), total );
   }

   /**
    * \brief Callback invoked when TSQ query sort is complete
    *
    * @param total Total msgs queried and sorted
    */
   public void OnSortComplete( int total )
   {
      cout.printf( "[%s] OnSortComplete( %d )\n", Now(), total );
   }

   /**
    * \brief Callback invoked when TSQ query for specified ticker starts
    *
    * @param tkr - Ticker name
    */
   public void OnTickerStart( String tkr )
   {
//      cout.printf( "[%s] OnTickerStart( %s )\n", Now(), tkr );
   }

   /**
    * \brief Callback invoked when TSQ query for specified ticker ends
    *
    * @param tkr - Ticker name
    * @param total - Total msgs pumped for ticker
    */
   public void OnTickerEnd( String tkr, int total )
   {
//      cout.printf( "[%s] OnTickerEnd( %s ) : %d msgs\n", Now(), tkr, total );
   }

   /**
    * \brief Callback invoked when TSQ error occurs
    *
    * @param txt Textual description of error
    */
   public void OnError( String txt )
   {
      cout.printf( "[%s] OnError( %s )\n", Now(), txt );
   }


   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      PumpTSQ       t;
      UCfileConfig  cfg;
      TSQReceiver[] udb;
      PrintStream   cout = System.out;
      PrintStream   ps;
      int           tot, argc, i, nr, nt, ix0, ix1, len;
      boolean       bDmp, bSort;
      String[]      kv, tdb, tkrs;
      String        iFile, dFile, t0, t1, tkr, opt;

      // Load Config

      cfg = new UCfileConfig();
      cfg.AddString( "IndexFile", "/dev/null" );
      cfg.AddString( "DataFile", "/dev/null" );
      cfg.AddString( "StartTime", "09:30:00" );
      cfg.AddString( "EndTime", "09:45:00" );
      cfg.AddString( "Ticker", "ALL" );
      cfg.AddBool( "MsgDump", false );

      // Quick Check

      argc = args.length;
      if ( ( argc > 0 ) && args[0].equals( "--version" ) ) {
         cout.printf( "%s\n", QuoddMsg.Version() );
         return;
      }
      if ( ( argc > 0 ) && args[0].equals( "--help" ) ) {
         cout.print( cfg.Dump() );
         return;
      }

      // <idxFile> <dataFile> <tStart> <tEnd>

      if ( argc == 0 ) {
         cout.printf( "Usage : PumpTSQ <config_file>; Exitting ...\n" );
         return;
      }
      cfg.Load( args[0] );
      iFile = cfg.GetString( "IndexFile" );
      dFile = cfg.GetString( "DataFile" );
      t0    = cfg.GetString( "StartTime" );
      t1    = cfg.GetString( "EndTime" );
      bDmp  = cfg.GetBool( "MsgDump" );
      tkr   = cfg.GetString( "Ticker" );
      kv    = tkr.split(":");
      t = new PumpTSQ( iFile, dFile );
      cout.printf( "[%s] Started ...\n", t.Now() );
      t.Start();
      if ( tkr.equals( "ALL" ) || tkr.equals( "ALL_SORT" ) ) {
         bSort = tkr.equals( "ALL_SORT" );
         try {
            ps = new PrintStream( new File( "./msgs.out" ) );
            new TSQReceiver( t, ps, bDmp ).PumpAll( t0, t1, bSort );
         } catch( FileNotFoundException f ) {
            cout.printf( "ERROR : %s\n", f.getMessage() );
            return;
         }
      }
      else if ( (nr=kv.length) > 1 ) {
         udb = new TSQReceiver[nr];
         for ( i=0; i<nr; i++ ) {
            try {
               ps     = new PrintStream( new File( kv[i] ) );
               udb[i] = new TSQReceiver( t, ps, bDmp );
            } catch( FileNotFoundException f ) {
               cout.printf( "ERROR %s : %s\n", kv[i], f.getMessage() );
               return;
            }
         }
         tdb = t.GetTickers();
         nt  = tdb.length;
         len = nt / nr;
         ix0 = 0;
         cout.printf( "[%s] SetMapRange( %s,%s )\n", Now(), t0, t1 );
         t.SetMapRange( t0, t1 );
         cout.printf( "[%s] %d tickers to pump in %d threads\n", Now(), nt, nr );
         for ( i=0; i<nr; i++ ) {
            ix1  = Math.min( ix0+len, nt-1 );
            tkrs = Arrays.copyOfRange( tdb, ix0, ix1 );
            ix0 += len;
            udb[i].PumpThreaded( i+1, t0, t1, tkrs );
         }
      }
      else
         new TSQReceiver( t, cout, bDmp ).PumpTicker( t0, t1, tkr );
   }
} // class PumpTSQ
