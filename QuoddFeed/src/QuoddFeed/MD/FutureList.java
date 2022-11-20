/******************************************************************************
*
*  FutureList.java
*     Higher-order Future Chain object
*
*  REVISION HISTORY:
*     18 JAN 2012 jcs  Created.
*      1 FEB 2012 jcs  Build 13: Cmd.ANSI_CLEAR
*     17 APR 2012 jcs  Build 16: OnUpdate( StreamName, ... )
*     22 JUN 2012 jcs  Build 25: GetFuturesChain() / FUTRTrade
*     14 OCT 2012 jcs  Build 42: One-step Subscribe and Register
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.MD;

import java.lang.*;
import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    F u t u r e L i s t
//
/////////////////////////////////////////////////////////////////
/**
 * FutrueList is an higher-order QuoddFeed.MD class for consuming and
 * processing update messages for all ticker of an Futures Chain.
 * <p>
 * We store FutureRecords in an unsorted HashMap collection.
 */
public class FutureList extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan                      _uChan;
   protected String                         _tkr;
   protected int                            _maxRow;
   protected String                         _arg;
   private   int                            _streamID;
   private   HashMap<Integer, FutureRecord> _recs;

   //////////////////////
   // Constructor
   //////////////////////
   public FutureList( UltraChan uChan, String tkr, int maxRow )
   {
      // Guts

      _uChan  = uChan;
      _tkr    = tkr;
      _maxRow = maxRow;
      _arg    = "User-defined argument";
      _recs   = new HashMap<Integer, FutureRecord>();
      Open();
   }

   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      System.out.printf( ANSI_CLEAR );
      _streamID  = _uChan.GetFuturesChain( _tkr, _arg, this );

      // Clear Screen

      System.out.printf( ANSI_HOME );
/*
      System.out.printf( "Ticker       " );
      System.out.printf( "  Bid   BidSz   BidTime    | " );
      System.out.printf( "  Ask   AskSz   AskTime\n" );
      System.out.printf( "------------ " );
      System.out.printf( "------- ----- ------------ + " );
      System.out.printf( "------- ----- ------------\n" );
 */
      System.out.printf( "Ticker       TrdPrc " );
      System.out.printf( "  Bid   BidSz |   Ask   AskSz | " );
      System.out.printf( " AskTime\n" );
      System.out.printf( "------------ ------ " );
      System.out.printf( "------- ----- + ------- ----- + " );
      System.out.printf( " ------------\n" );

   }

   public void Close()
   {
      Iterator    it;
      FutureRecord rec;

      it = _recs.values().iterator();
      while( it.hasNext() ) {
         rec = (FutureRecord)it.next();
         _uChan.Unsubscribe( rec._StreamID );
      }
      _recs.clear();
      System.out.printf( ANSI_CLEAR );
   }

   //////////////////////
   // IUpdate Interface - Blob
   //////////////////////
   /**
    * Callback invoked when BlobTable containing list of futures is received.
    *
    * @param qry Query
    * @param b BlobTable
    */
   public void OnBlobTable( String qry, BlobTable b )
   {
      String       tkr;
      FutureRecord rec;
      int          i, r, nt, sid;

      // Create FutureRecords that pass _regex

      nt = b.nRow();
      for ( i=0,r=1; i<nt; i++ ) {
         tkr = b.GetCell( i,0 );
         sid = _uChan.Subscribe( tkr, null, this );
         rec = new FutureRecord( tkr, sid, r++ );
         _recs.put( sid, rec );
      }
   }


   //////////////////////
   // IUpdate Interface - Stream
   //////////////////////
   /**
    * Callback invoked when an unknown QuoddMsg is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm QuoddMsg
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      // Don't care, for now
   }

   /**
    * Callback invoked when status received on a data stream
    *
    * @param StreamName Name of this Data Stream 
    * @param sts Status
    */
   public void OnStatus( String StreamName, Status sts )
   {
      // Don't care, for now
   }

   /**
    * Callback invoked when an Initial Image is received
    *
    * @param StreamName Name of this Data Stream
    * @param img Image
    */
   public void OnImage( String StreamName, Image img )
   {
      FUTRQuote q;
      FUTRTrade t;

      // Now populate to look like FUTRQuote

      q            = new FUTRQuote();
      q._bid       = img._bid;
      q._bidSize   = img._bidSize;
      q._ask       = img._ask;
      q._askSize   = img._askSize;
      q.SetStreamID( img );
      q.SetTime( img._bidTime );
      OnUpdate( StreamName, q );

      // FUTRTrade too

      t            = new FUTRTrade();
      t._trdPrc    = img._trdPrc;
      t._trdVol    = img._trdVol;
      t.SetStreamID( img );
      t.SetTime( img._bidTime );
      OnUpdate( StreamName, t );
   }

   /**
    * Callback invoked when Futures Quote Message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qte FUTRQuote
    */
   public void OnUpdate( String StreamName, FUTRQuote qte )
   {
      FutureRecord rec;

      if ( (rec=_GetRecord( qte )) != null ) {
         rec.Set( qte );
         _ShowRecord( rec );
      }
   }

   /**
    * Callback invoked when Futures Trade Message is received
    *
    * @param StreamName Name of this Data Stream
    * @param trd FUTRTrade
    */
   public void OnUpdate( String StreamName, FUTRTrade trd )
   {
      FutureRecord rec;

      if ( (rec=_GetRecord( trd )) != null ) {
         rec.Set( trd );
         _ShowRecord( rec );
      }
   }


   //////////////////////
   // Helpers
   //////////////////////
   private FutureRecord _GetRecord( QuoddMsg qm )
   {
      FutureRecord rec;
      int         id;

      id  = qm.StreamID();
      rec = null;
      if ( _recs.containsKey( id ) )
         rec = (FutureRecord)_recs.get( id );
      return rec;
   }

   private void _ShowRecord( FutureRecord rec )
   {
      PrintStream cout = System.out;
      int         row;

      // Pre-condition

      row  = rec._row;
      row += 2; // Header
      if ( row > _maxRow )
         return;

      // Safe to show ...

      cout.printf( ANSI_ROWCOL, row, 1 );
/*
      cout.printf( "%-7s ", rec._tkr );
      cout.printf( "%7.2f %6d %12s | ", rec._dBid, rec._bidSize, rec._bidTime );
      cout.printf( "%7.2f %6d %12s\n", rec._dAsk, rec._askSize, rec._askTime );
 */
      System.out.printf( "%-12s %7.2f", rec._tkr, rec._trdPrc );
      System.out.printf( "%7.2f %5d |",
         rec._dBid, rec._bidSize );
      System.out.printf( " %7.2f %5d | %12s\n",
         rec._dAsk, rec._askSize, rec._askTime );

   }
}


/////////////////////////////////////////////////////////////////
//
//                c l a s s    F u t u r e R e c o r d
//
/////////////////////////////////////////////////////////////////
/**
 * FutureRecord is a container class storing market data for a single 
 * record (participant).
 * <p>
 */
class FutureRecord
{
   ///////////////////////////////
   // Instance Members
   ///////////////////////////////
   public String _tkr;
   public int    _StreamID;
   public int    _row;
   public double _trdPrc;
   public long   _trdVol;
   public String _trdTime;
   public double _dBid;
   public long   _bidSize;
   public String _bidTime;
   public double _dAsk;
   public long   _askSize;
   public String _askTime;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public FutureRecord( String tkr, int StreamID, int row )
   {
      _tkr      = tkr;
      _StreamID = StreamID;
      _row      = row;
      _trdPrc   = 0.0;
      _trdVol   = 0;
      _trdTime  = "00:00:00.00";
      _dBid     = 0;
      _bidSize  = 0;
      _bidTime  = "00:00:00.00";
      _dAsk     = 0.0;
      _askSize  = 0;
      _askTime  = "00:00:00.00";
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   public void Set( FUTRQuote qte )
   {
      _dBid    = qte._bid;
      _bidSize = qte._bidSize;
      _bidTime = qte.pTimeMs().substring(1,12);
      _dAsk    = qte._ask;
      _askSize = qte._askSize;
      _askTime = _bidTime;
   }

   public void Set( FUTRTrade trd )
   {
      _trdPrc  = trd._trdPrc;
      _trdVol  = trd._trdVol;
      _trdTime = trd.pTimeMs().substring(1,12);
   }
}
