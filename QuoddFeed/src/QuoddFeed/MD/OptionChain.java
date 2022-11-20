/******************************************************************************
*
*  OptionChain.java
*     Higher-order Option Chain object
*
*  REVISION HISTORY:
*     18 JAN 2012 jcs  Created.
*      1 FEB 2012 jcs  Build 13: Cmd.ANSI_CLEAR
*     17 APR 2012 jcs  Build 16: OnUpdate( StreamName, ... )
*     17 JUN 2012 jcs  Build 23: Do not subscribe to null tkrs
*     14 OCT 2012 jcs  Build 42: One-step Subscribe and Register
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.MD;

import java.lang.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    O p t i o n C h a i n
//
/////////////////////////////////////////////////////////////////
/**
 * OptionChain is an higher-order QuoddFeed.MD class for consuming and 
 * processing update messages for all ticker of an Option Chain
 * <p>
 * We store quotes in an unsorted HashMap collection.
 */
public class OptionChain extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan                      _uChan;
   protected String                         _tkr;
   protected int                            _maxRow;
   protected String                         _arg;
   private   int                            _streamID;
   private   HashMap<Integer, OptionRecord> _recs;

   //////////////////////
   // Constructor
   //////////////////////
   public OptionChain( UltraChan uChan, String tkr, int maxRow )
   {
      // Guts

      _uChan  = uChan;
      _tkr    = tkr;
      _maxRow = maxRow;
      _arg    = "User-defined argument";
      _recs   = new HashMap<Integer, OptionRecord>();
      Open();
   }


   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      System.out.printf( ANSI_CLEAR );
      _streamID  = _uChan.GetOptionChain( _tkr, _arg, this );

      // Clear Screen

      System.out.printf( ANSI_HOME );
/*
      System.out.printf( "Ticker       " );
      System.out.printf( "  Bid   MMID BidSz   BidTime    | " );
      System.out.printf( "  Ask   MMID AskSz   AskTime\n" );
      System.out.printf( "------------ " );
      System.out.printf( "------- ---- ----- ------------ + " );
      System.out.printf( "------- ---- ----- ------------\n" );
 */
      System.out.printf( "Ticker       TrdPrc " );
      System.out.printf( "  Bid   MMID BidSz |   Ask   MMID AskSz | " );
      System.out.printf( " AskTime\n" );
      System.out.printf( "------------ ------ " );
      System.out.printf( "------- ---- ----- + ------- ---- ----- + " );
      System.out.printf( " ------------\n" );
   }

   public void Close()
   {
      Iterator     it;
      OptionRecord rec;

      it = _recs.values().iterator();
      while( it.hasNext() ) {
         rec = (OptionRecord)it.next();
         _uChan.Unsubscribe( rec._StreamID );
      }
      _recs.clear();
      System.out.printf( ANSI_CLEAR );
   }


   //////////////////////
   // IUpdate Interface - Blob
   //////////////////////
   /**
    * Callback invoked when BlobList containing list of participants is received.
    *
    * @param qry Query
    * @param b BlobList
    */
   public void OnBlobList( String qry, BlobList b )
   {
      String      tkr;
      OptionRecord rec;
      int         i, nt, sid;

      // Create OptionRecords

      nt = b.lst().length;
      for ( i=0; i<nt; i++ ) {
         tkr = b.lst()[i];
         if ( tkr.length() == 0 )
            continue; // for-i
         sid = _uChan.Subscribe( tkr, null, this );
         rec = new OptionRecord( tkr, sid, i+1 );
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
      OPBbo    q;
      OPTrade  t;
      String   pt;

      // Now populate to look like OPBbo

      pt           = img.tkr();
      q            = new OPBbo();
      q._bid       = img._bid;
      q._bidSize   = img._bidSize;
      q._ask       = img._ask;
      q._askSize   = img._askSize;
      q._bidMktCtr = img._bidMktCtr;
      q._askMktCtr = img._askMktCtr;
      q.SetStreamID( img );
      q.SetTime( img._bidTime );
      OnUpdate( pt, q );
      t            = new OPTrade();
      t._trdPrc    = img._trdPrc;
      t._trdVol    = img._trdVol;
      t.SetStreamID( img );
      OnUpdate( pt, t );
   }

   /**
    * Callback invoked when Equity Quote BBO Message is received 
    *
    * @param StreamName Name of this Data Stream
    * @param qte OPBbo
    */
   public void OnUpdate( String StreamName, OPBbo qte )
   {
      OptionRecord rec;

      if ( (rec=_GetRecord( qte )) != null ) {
         rec.Set( qte );
         _ShowRecord( rec );
      }
   }

   /**
    * Callback invoked when Equity Quote Message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qte OPQuote
    */
   public void OnUpdate( String StreamName, OPQuote qte )
   {
      OptionRecord rec;

      if ( (rec=_GetRecord( qte )) != null ) {
         rec.Set( qte );
         _ShowRecord( rec );
      }
   }

   /**
    * Callback invoked when an Equity Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param trd OPTrade
    */
   public void OnUpdate( String StreamName, OPTrade trd )
   {
      OptionRecord rec;

      if ( (rec=_GetRecord( trd )) != null ) {
         rec.Set( trd );
         _ShowRecord( rec );
      }
   }


   //////////////////////
   // Helpers
   //////////////////////
   private OptionRecord _GetRecord( QuoddMsg qm )
   {
      OptionRecord rec;
      int         id;

      id  = qm.StreamID();
      rec = null;
      if ( _recs.containsKey( id ) )
         rec = (OptionRecord)_recs.get( id );
      return rec;
   }

   private void _ShowRecord( OptionRecord rec )
   {
      int row;

      // Pre-condition

      row  = rec._row;
      row += 2; // Header
      if ( row > _maxRow )
         return;

      // Safe to show ...

      System.out.printf( ANSI_ROWCOL, row, 1 );
/*
      System.out.printf( "%-12s ", rec._dpyTkr );
      System.out.printf( "%7.2f %-4s %5d %12s |", 
         rec._dBid, rec._bidMmid, rec._bidSize, rec._bidTime );
      System.out.printf( "%7.2f %-4s %5d %12s\n",
         rec._dAsk, rec._askMmid, rec._askSize, rec._askTime );
 */
      System.out.printf( "%-12s %7.2f", rec._dpyTkr, rec._trdPrc );
      System.out.printf( "%7.2f %-4s %5d |",
         rec._dBid, rec._bidMmid, rec._bidSize );
      System.out.printf( " %7.2f %-4s %5d | %12s\n",
         rec._dAsk, rec._askMmid, rec._askSize, rec._askTime );
   }
}


/////////////////////////////////////////////////////////////////
//
//               c l a s s    O p t i o n R e c o r d
//
/////////////////////////////////////////////////////////////////
/**
 * OptionRecord is a container class storing market data for a single 
 * record (participant).
 * <p>
 */
class OptionRecord
{
   ///////////////////////////////
   // Instance Members
   ///////////////////////////////
   public String _tkr;
   public int    _StreamID;
   public String _dpyTkr;
   public int    _row;
   public double _trdPrc;
   public long   _trdVol;
   public String _trdTime;
   public double _dBid;
   public long   _bidSize;
   public String _bidTime;
   public String _bidMmid;
   public double _dAsk;
   public long   _askSize;
   public String _askTime;
   public String _askMmid;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public OptionRecord( String tkr, int StreamID, int row )
   {
      _tkr      = tkr;
      _StreamID = StreamID;
      _dpyTkr   = tkr.substring( tkr.indexOf( '\\' )+1 );
      _row      = row;
      _trdPrc   = 0.0;
      _trdVol   = 0;
      _trdTime  = "00:00:00.00";
      _dBid     = 0;
      _bidSize  = 0;
      _bidTime  = "00:00:00.00";
      _bidMmid  = "????";
      _dAsk     = 0.0;
      _askSize  = 0;
      _askTime  = "00:00:00.00";
      _askMmid  = "????";
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   public void Set( OPQuote qte )
   {
      _dBid    = qte._bid;
      _bidSize = qte._bidSize;
      _bidTime = qte.pTimeMs().substring(1,12);
      _dAsk    = qte._ask;
      _askSize = qte._askSize;
      _askTime = _bidTime;
   }

   public void Set( OPBbo bbo )
   {
      _dBid    = bbo._bid;
      _bidSize = bbo._bidSize;
      _bidTime = bbo.pTimeMs().substring(1,12);
      _bidMmid = bbo._bidMktCtr;
      _dAsk    = bbo._ask;
      _askSize = bbo._askSize;
      _askTime = _bidTime;
      _askMmid = bbo._askMktCtr;
   }

   public void Set( OPTrade trd )
   {
      _trdPrc  = trd._trdPrc;
      _trdVol  = trd._trdVol;
      _trdTime = trd.pTimeMs().substring(1,12);
   }
}
