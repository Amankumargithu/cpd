/******************************************************************************
*
*  IUpdate.java
*     UltraChan update interface
*
*  REVISION HISTORY:
*     13 OCT 2011 jcs  Created.
*     14 JAN 2012 jcs  Build  11: IUpdate.java
*      1 FEB 2012 jcs  Build  13: v0.12: UCStatus
*     22 FEB 2012 jcs  Build  14: v0.12: IUpdateQ; No mo oldMsg
*     19 MAR 2012 jcs  Build  15: v0.14: 4 spaces in hexMsg()
*      9 APR 2012 jcs  Build  15: v0.14: OPTrade; static public hexMsg()
*     10 APR 2012 jcs  Build  16: v0.xx: _ixSubETPINTRA
*     17 APR 2012 jcs  Build  16: OnUpdate( StreamName, ... )
*      3 MAY 2012 jcs  Build  18: EQMktStats
*     15 MAY 2012 jcs  Build  19: All _fundSubXXXX
*     29 MAY 2012 jcs  Build  21: _eqSubSUMMARY / _eqSubMKTCTRSUMM
*     17 JUL 2012 jcs  Build  29: _eqSubTRDACTION / _eqSubREGSHO
*     17 SEP 2012 jcs  Build  37: _opSubTRDSUMM
*     13 NOV 2012 jcs  Build  44: _ixSubNAV, etc.
*      4 JAN 2013 jcs  Build  50: FUTRSumm / FUTRMisc
*     25 FEB 2013 jcs  Build  54: EQLimitUpDn
*     14 MAY 2013 jcs  Build  59: Tracer
*     14 JUN 2013 jcs  Build  64: OnRaw()
*     26 AUG 2013 jcs  Build  68: OnQueue()
*      9 OCT 2013 jcs  Build  70: OnRaw( ByteBuffer ) 
*     26 FEB 2014 jcs  Build  75: _ixSubSETLVAL / _ixSubSETLSUMM
*     28 MAY 2014 jcs  Build  76: _eqSubTRDCXL; _opSubTRDCXL
*     15 AUG 2014 jcs  Build  81: No mo OnRaw_OBSOLETE()
*      3 FEB 2015 jcs  Build  90: OnRaw( InputStream )
*     14 MAY 2015 jcs  Build  92: _ftSubSETLPRC
*     31 AUG 2015 jcs  Build  96: _opSubTRADE_ID; InRange( int )
*     11 NOV 2015 jcs  Build  98: _opSubTRADE_ID w/ appendage
*      8 DEC 2016 jcs  Build 101: hexDump(); _eqSubTRDASOF / _eqSubTRDASOFCXL
*      9 AUG 2017 jcs  Build 103: _eqSubTRDCORR; _eqSubTRDASOFCORR
*
*  (c) 2011-2017 Quodd Financial
*******************************************************************************/
package QuoddFeed.util;

import java.io.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import QuoddFeed.msg.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    I U p d a t e
//
/////////////////////////////////////////////////////////////////
/**
 * IUpdate is an interface class for receiving updates from UltraCache.
 * <p>
 * Real-time updates are unpacked and delivered to your application via 
 * the polymorphic OnUpdate() methods.  Command response are delivered 
 * via the OnBlobXxxx() callback functions.
 * <p>
 * You may choose to process the QuoddMsg in a different thread than 
 * the UltraChan thread by calling the {@link #StartQ(int)} method.  The
 * default is to dispatch messages to your application in the UltraChan
 * thread.
 */
public class IUpdate extends Cmd
{
   private IUpdateQ _q;

   //////////////////////
   // Constructor
   //////////////////////
   public IUpdate()
   {
      _q = null;
   }

   //////////////////////
   // Queue Operations
   //////////////////////
   /**
    * Returns true if this instance is threaded w/ event queue 
    * <p>
    * @return true if this IUpdate interface has an event queue
    */
   public boolean HasQ()
   {
      return( _q != null );
   }

   /**
    * Returns true if this instance is threaded w/ event queue
    * <p>
    * @param maxQ Maximum queue size
    */
   public void StartQ( int maxQ )
   {
      if ( !HasQ() )
         _q = new IUpdateQ( this, maxQ );
   }

   /**
    * Stops the event queue and kills the dispatch thread.  All future
    * update events will be dispatched in the UltraChan thread.
    */
   public void StopQ()
   {
      if ( HasQ() )
         _q.Stop();
      _q = null;
   }

   /**
    * @return Maximum message queue size
    */
   public int qMax()
   {
      return HasQ() ? _q.qMax() : 0;
   }

   /**
    * @return Remaining message queue length
    */
   public int qLeft()
   {
      return HasQ() ? _q.qLeft() : 0;
   }

   /**
    * @return Message queue length
    */
   public int qSize()
   {
      return HasQ() ? _q.qSize() : 0;
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Callback invoked when raw data read from DataInputStream
    *
    * @param in InputStream
    * @param qm  QuoddMsg
    * @param buf ByteBuffer containing raw QuoddMsg data
    */
   public void OnRaw( InputStream in, QuoddMsg qm, ByteBuffer buf )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an unknown QuoddMsg is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm QuoddMsg
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when Tracer (performance) msg received on 
    * a data stream
    *
    * @param StreamName Name of this Data Stream
    * @param trc Tracer
    */
   public void OnTrace( String StreamName, Tracer trc )
   {
      // Derived classes implement
   }


   /**
    * Callback invoked when status received on a data stream
    *
    * @param StreamName Name of this Data Stream 
    * @param sts Status
    */
   public void OnStatus( String StreamName, Status sts )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when UltraCache status is received on data stream
    *
    * @param sts UCStatus
    */
   public void OnStatus( UCStatus sts )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when contributed data update is received from UltraCache
    *
    * @param b BlobTable
    */
   public void OnContrib( String StreamName, BlobTable b )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when a BlobList such as Options Chain is received.
    * 
    * @param qry Query
    * @param b BlobList 
    */
   public void OnBlobList( String qry, BlobList b )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when a BlobTable such as Options Snapshot is received.  
    * 
    * @param qry Query
    * @param b BlobTable
    */
   public void OnBlobTable( String qry, BlobTable b )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Initial Image is received
    *
    * @param StreamName Name of this Data Stream
    * @param img Image
    */
   public void OnImage( String StreamName, Image img )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Short- or Long-Form Equity Quote message is 
    * received
    *
    * @param StreamName Name of this Data Stream
    * @param qm EQQuote
    */
   public void OnUpdate( String StreamName, EQQuote qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Short-or Long-Form Equity BBO Quote message
    * is received
    *
    * @param StreamName Name of this Data Stream
    * @param qm EQBbo
    */
   public void OnUpdate( String StreamName, EQBbo qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Short- or Long-Form Equity Market Maker Quote 
    * message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qm EQQuoteMM
    */
   public void OnUpdate( String StreamName, EQQuoteMM qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Short-or Long-Form Equity Market Maker BBO 
    * Quote message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qm EQBboMM
    */
   public void OnUpdate( String StreamName, EQBboMM qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Equity Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qm EQTrade
    */
   public void OnUpdate( String StreamName, EQTrade qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Equity Trade Status message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qm EQTradeSts
    */
   public void OnUpdate( String StreamName, EQTradeSts qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Equity MktStats message is received
    *
    * @param StreamName Name of this Data Stream
    * @param st EQMktStats
    */
   public void OnUpdate( String StreamName, EQMktStats st )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Equity Limit Up/Down message is received
    *
    * @param StreamName Name of this Data Stream
    * @param st EQLimitUpDn
    */
   public void OnUpdate( String StreamName, EQLimitUpDn st )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Short- or Long-Form Option Quote message is
    * received
    *
    * @param StreamName Name of this Data Stream
    * @param qm OPQuote
    */
   public void OnUpdate( String StreamName, OPQuote qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Short- or Long-Form Option BBO message is
    * received
    *
    * @param StreamName Name of this Data Stream
    * @param qm OPBbo
    */
   public void OnUpdate( String StreamName, OPBbo qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Option Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qm OPTrade
    */
   public void OnUpdate( String StreamName, OPTrade qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Index Value message is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm IDXValue
    */
   public void OnUpdate( String StreamName, IDXValue qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Index Summary message is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm IDXSummary
    */
   public void OnUpdate( String StreamName, IDXSummary qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Fund NAV message is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm FUNDnav
    */
   public void OnUpdate( String StreamName, FUNDnav qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Futures quote is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm FUTRQuote
    */
   public void OnUpdate( String StreamName, FUTRQuote qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Futures trade is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm FUTRTrade
    */
   public void OnUpdate( String StreamName, FUTRTrade qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Futures Summary is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm FUTRSumm
    */
   public void OnUpdate( String StreamName, FUTRSumm qm )
   {
      // Derived classes implement
   }

   /**
    * Callback invoked when an Futures Misc - Hi/Lo/Last or OpenInt - 
    * is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm FUTRMisc
    */
   public void OnUpdate( String StreamName, FUTRMisc qm )
   {
      // Derived classes implement
   }

   /** 
    * Callback invoked when an Bond quote is received.
    *   
    * @param StreamName Name of this Data Stream
    * @param qm BONDQuote
    */  
   public void OnUpdate( String StreamName, BONDQuote qm )
   {   
      // Derived classes implement
   }   

   /** 
    * Callback invoked when an Bond trade is received.
    *   
    * @param StreamName Name of this Data Stream
    * @param qm BONDTrade
    */  
   public void OnUpdate( String StreamName, BONDTrade qm )
   {   
      // Derived classes implement
   }   


   //////////////////////////////////////////////////////
   //                                                  //
   //  Message Processing                              //
   //                                                  //
   //////////////////////////////////////////////////////
   /**
    * Callback invoked when queue grows or shrinks
    */
   protected void OnQueue()
   {
      // Derived classes implement
   }

   public void OnMsg( String StreamName, QuoddMsg qm, boolean bQ )
   {
      char mt, mt2;

      // Queue'ed?

      if ( ( _q != null ) && !bQ ) {
         _q.qAdd( StreamName, qm.clone() );
         OnQueue();
         return;
      }

      // Safe to process

      OnQueue();
      mt  = qm.mt();
      mt2 = qm.mtSub();
      if ( mt2 == _mtSubTRACER ) {
         OnTrace( StreamName, (Tracer)qm );
         return;
      }
      switch( mt ) {
         case _mtEQUITY:
            switch( mt2 ) {
               case _eqSubQTESHORT:
               case _eqSubQTELONG:
                  OnUpdate( StreamName, (EQQuote)qm );
                  break;
               case _eqSubBBOSHORT:
               case _eqSubBBOLONG:
                  OnUpdate( StreamName, (EQBbo)qm );
                  break;
               case _eqSubQTESHORTMM:
               case _eqSubQTELONGMM:
                  OnUpdate( StreamName, (EQQuoteMM)qm );
                  break;
               case _eqSubBBOSHORTMM:
               case _eqSubBBOLONGMM:
                  OnUpdate( StreamName, (EQBboMM)qm );
                  break;
               case _eqSubTRDSHORT:
               case _eqSubTRDLONG:
               case _eqSubTRDCXL:
               case _eqSubTRDCORR:
               case _eqSubSUMMARY:
               case _eqSubMKTCTRSUMM:
               case _eqSubTRDASOF:
               case _eqSubTRDASOFCXL:
               case _eqSubTRDASOFCORR:
                  OnUpdate( StreamName, (EQTrade)qm );
                  break;
               case _eqSubTRDACTION:
               case _eqSubREGSHO:
                  OnUpdate( StreamName, (EQTradeSts)qm );
                  break;
               case _eqSubMKTSTATS:
                  OnUpdate( StreamName, (EQMktStats)qm );
                  break;
               case _eqSubLIMITUPDN:
                  OnUpdate( StreamName, (EQLimitUpDn)qm );
                  break;
               default:
                  OnQuoddMsg( StreamName, qm );
                  break;
            }
            break;
         case _mtOPTION:
            switch( mt2 ) {
               case _opSubQTESHORT:
               case _opSubQTELONG:
                  OnUpdate( StreamName, (OPQuote)qm );
                  break;
               case _opSubBBOSHORT:
               case _opSubBBOLONG:
                  OnUpdate( StreamName, (OPBbo)qm );
                  break;
               case _opSubTRADE:
               case _opSubTRDCXL:
               case _opSubTRADE_ID:
               case _opSubTRDCXL_ID:
               case _opSubTRDSUMM:
                  OnUpdate( StreamName, (OPTrade)qm );
                  break;
               default:
                  OnQuoddMsg( StreamName, qm );
                  break;
            }
            break;
         case _mtFUND:
            switch( mt2 ) {
               case _fundSubFUNDNAV:
               case _fundSubMMNAV:
               case _fundSubCAPDISTRO:
               case _fundSubDIVID_INT:
                  OnUpdate( StreamName, (FUNDnav)qm );
                  break;
               default:
                  OnQuoddMsg( StreamName, qm );
                  break;
            }
            break;
         case _mtINDEX:
            switch( mt2 ) {
               case _ixSubVALUE:
               case _ixSubSETLVAL:
               case _ixSubSETLSUMM:
               case _ixSubETPINTRA:
               case _ixSubEST_CASH:
               case _ixSubTOT_CASH:
               case _ixSubNAV:
               case _ixSubSHARES:
               case _ixSubETPDIV:
                  OnUpdate( StreamName, (IDXValue)qm );
                  break;
               case _ixSubSUMM:
                  OnUpdate( StreamName, (IDXSummary)qm );
                  break;
               default:
                  OnQuoddMsg( StreamName, qm );
                  break;
            }
            break;
         case _mtFUTURE:
            switch( mt2 ) {
               case _ftSubTRADE:
               case _ftSubTRDCXL:
               case _ftSubTRDCORR:
                  OnUpdate( StreamName, (FUTRTrade)qm );
                  break;
               case _ftSubSUMMARY:
                  OnUpdate( StreamName, (FUTRSumm)qm );
                  break;
               case _ftSubHILOLAST:
               case _ftSubOPENINT:
               case _ftSubSETLPRC:
                  OnUpdate( StreamName, (FUTRMisc)qm );
                  break;
               case _ftSubCOMBOQTE:
               case _ftSubQUOTE:
               case _ftSubEXCQTE:
                  OnUpdate( StreamName, (FUTRQuote)qm );
                  break;
/* 
   static protected final char _ftSubMKTCOND  = 0x36; // 6
   static protected final char _ftSubRFQ      = 0x37; // 7
   static protected final char _ftSubRANGEQTE = 0x3c; // <
   static protected final char _ftSubCUMVOL   = 0x3d; // =
   static protected final char _ftSubCASHPRC  = 0x4d; // M
 */
               default:
                  OnQuoddMsg( StreamName, qm );
                  break;
            }
            break;
         case _mtBOND:
            switch( mt2 ) {
               case _boSubQUOTE:
               case _boSubQTEYLD:
                  OnUpdate( StreamName, (BONDQuote)qm );
                  break;
               case _boSubTRADE:
               case _boSubTRDCXL:
               case _boSubTRDCORR:
                  OnUpdate( StreamName, (BONDTrade)qm );
                  break;
            }
            break;
         case _mtBLOB:
            switch( mt2 ) {
               case _blSubCONTRIB:
                  OnContrib( StreamName, (BlobTable)qm );
                  break;
            }
            break;
         case _mtDEAD:
            OnStatus( StreamName, (Status)qm );
            break;
         case _mtUC_STATUS:
            OnStatus( (UCStatus)qm );
            break;
         default:
            OnQuoddMsg( StreamName, qm );
            break;
      }
   } 


   /////////////////////////
   // Class-wide
   /////////////////////////
   static public String hexMsg( byte[] b, int off, int sz )
   {
      String s, rtn;
      char   ch;
      int    i, mSz;

      rtn = "";
      mSz = 72;
      for ( i=0,s=""; i<sz; i++ ) {
         ch = (char)b[off+i];
         if ( InRange( ' ', ch, '~' ) )
            s += String.format( "%c", ch );
         else {
            switch( ch ) {
               case 0x1b: s += "<ESC>"; break;
               case 0x9b: s += "<CSI>"; break;
               case 0x1c: s += "<FS>";  break;
               case 0x1d: s += "<GS>";  break;
               case 0x1e: s += "<RS>";  break;
               case 0x1f: s += "<US>";  break;
               default:
                  s += String.format( "<%02x>", (byte)ch );
                  break;
            }
         }
         if ( s.length() > mSz ) {
            rtn += s;
            rtn += "\n";
            s    = "    ";
            mSz  = 76;
         }
      }
      if ( s.length() == 4 )
         s = "";
      rtn += String.format( "%s\n", s );
      return rtn;
   }

   static public String hexDump( byte[] b, int off, int sz )
   {
      String s;
      char   ch;
      int    i;

      s = "";
      for ( i=0,s=""; i<sz; i++ ) {
         if ( i > 0 ) {
            if ( ( i%16 ) == 0 )
               s += s.format( "\n" );
            else if ( ( i%8 ) == 0 )
               s += s.format( "- " );
         }
         ch = (char)b[off+i];
         s += s.format( "%02x ", (byte)ch );
      }
      s += s.format( "\n" );
      return s;
   }

   static public boolean InRange( long a, long b, long c )
   {
      return( ( a <= b ) && ( b <= c ) );
   }

   static public boolean InRange( int a, int b, int c )
   {
      return( ( a <= b ) && ( b <= c ) );
   }

   static public boolean InRange( char a, char b, char c )
   {
      return( ( a <= b ) && ( b <= c ) );
   }

   static public long WithinRange( long a, long b, long c )
   {
      return Math.min( Math.max( a,b ), c );
   }

   static public int WithinRange( int a, int b, int c )
   {
      return Math.min( Math.max( a,b ), c );
   }

   static public void raw_input( String msg )
   {
      Scanner sc;

      System.out.printf( msg );
      sc = new Scanner( System.in );
      sc.nextLine();
   }

   static public void breakpoint() { ; }
}



/////////////////////////////////////////////////////////////////
//
//             c l a s s    I U p d a t e Q
//
/////////////////////////////////////////////////////////////////
/**
 * IUpdateQ is an interface class for receiving updates from UltraCache.
 * <p>
 * Real-time updates are unpacked and delivered to your application via
 * the polymorphic OnUpdate() methods.  Command response are delivered
 * via the OnBlobXxxx() callback functions.
 */
class IUpdateQ implements Runnable
{
   private IUpdate                 _iUpd;
   private Thread                  _thr;
   private int                     _qMax;
   private boolean                 _bRun;
   private BlockingQueue<QuoddMsg> _q;

   //////////////////////
   // Constructor
   //////////////////////
   public IUpdateQ( IUpdate iUpd, int qMax )
   {
      _iUpd = iUpd;
      _thr  = new Thread( this );
      _bRun = true;
      _qMax = qMax;
      _q    = new ArrayBlockingQueue<QuoddMsg>(_qMax);
      _thr.start();
   }

   //////////////////////
   // Queue Stuff
   //////////////////////
   public boolean qAdd( String reqKey, QuoddMsg qm )
   {
      // Non-blocking

      try {
         _q.add( qm.SetReqKey( reqKey ) );
      } catch( IllegalStateException e ) {
         return false;
      }
      return true;
   }

   public int qMax()
   {
      return _qMax;
   }

   public int qLeft()
   {
      return _q.remainingCapacity();
   }

   public int qSize()
   {
      return _q.size();
   }


   //////////////////////
   // Runnable Interface
   //////////////////////
   public void run()
   {
      QuoddMsg qm;

      while( _bRun ) {
         try {
            if ( (qm=_q.take()).len() > 0 )
               _iUpd.OnMsg( qm.ReqKey(), qm, true );
         } catch( InterruptedException ex ) {
            _bRun = false;
         }
      }
   }

   public void Stop()
   {
      _bRun = false;
      qAdd( "Done!!", new QuoddMsg() );
   }
}

