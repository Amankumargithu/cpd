/******************************************************************************
*
*  IndexChain.java
*     Higher-order Index Chain object
*
*  REVISION HISTORY:
*     18 JAN 2012 jcs  Created.
*      1 FEB 2012 jcs  Build 13: Cmd.ANSI_CLEAR
*     17 APR 2012 jcs  Build 16: OnUpdate( StreamName, ... )
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
//                c l a s s    I n d e x C h a i n
//
/////////////////////////////////////////////////////////////////
/**
 * IndexChain is an higher-order QuoddFeed.MD class for consuming and 
 * processing update messages for all participants of an Index
 * <p>
 * We store quotes in an unsorted HashMap collection.
 */
public class IndexChain extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan                     _uChan;
   protected String                        _tkr;
   protected int                           _maxRow;
   protected String                        _arg;
   private   int                           _streamID;
   private   HashMap<Integer, IndexRecord> _recs;

   //////////////////////
   // Constructor
   //////////////////////
   public IndexChain( UltraChan uChan, String tkr, int maxRow )
   {
      // Guts

      _uChan  = uChan;
      _tkr    = tkr;
      _maxRow = maxRow;
      _arg    = "User-defined argument";
      _recs   = new HashMap<Integer, IndexRecord>();
      Open();
   }

   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      System.out.printf( ANSI_CLEAR );
      _streamID  = _uChan.GetIndexParticipants( _tkr, _arg, this );

      // Clear Screen

      System.out.printf( ANSI_HOME );
      System.out.printf( "Ticker " );
      System.out.printf( "  Bid   MMID BidSize   BidTime    | " );
      System.out.printf( "  Ask   MMID AskSize   AskTime\n" );
      System.out.printf( "------ " );
      System.out.printf( "------- ---- ------- ------------ + " );
      System.out.printf( "------- ---- ------- ------------\n" );
   }

   public void Close()
   {
      Iterator    it;
      IndexRecord rec;

      it = _recs.values().iterator();
      while( it.hasNext() ) {
         rec = (IndexRecord)it.next();
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
    * @param b BlobTable
    */
   public void OnBlobList( String qry, BlobList b )
   {
      String      tkr;
      IndexRecord rec;
      int         i, nt, sid;

      // Create IndexRecords

      nt = b.lst().length;
      for ( i=0; i<nt; i++ ) {
         tkr = b.lst()[i];
         sid = _uChan.Subscribe( tkr, null, this );
         rec = new IndexRecord( tkr, sid, i+1 );
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
      EQBbo    q;
      String[] kv;
      String   pt, mmid;
      int      iBidCtr, iAskCtr;
      boolean  bMktMkr;

      /*
       * Convoluted way to figure out MMID; For Market Makers, the 
       * Image._iBidMktCtr / _iAskMktCtr values are non-zezo; If so, then 
       * tokenize the ticker name such that for ticker DELL/GSCO, the 
       * MMID = GSCO.
       *
       * For Market Centers, the MMID is in the Image._priMktCtr.
       *
       * Going forward, we should fix this piece of UltraCache stupidity.
       */
      iBidCtr = img._iBidMktCtr;
      iAskCtr = img._iAskMktCtr;
      bMktMkr = ( iBidCtr != 0 ) && ( iBidCtr == iAskCtr );
      pt      = img.tkr();
      kv      = pt.split("/");
      if ( bMktMkr && ( kv.length > 1 ) )
         mmid = kv[1];
      else
         mmid = img._priMktCtr;

      // Now populate to look like EQBbo

      q            = new EQBbo();
      q._bid       = img._bid;
      q._bidSize   = img._bidSize;
      q._ask       = img._ask;
      q._askSize   = img._askSize;
      q._bidMktCtr = mmid;
      q._askMktCtr = mmid;
      q.SetStreamID( img );
      q.SetTime( img._bidTime );
      OnUpdate( pt, q );
   }

   /**
    * Callback invoked when Equity Quote BBO Message is received 
    *
    * @param StreamName Name of this Data Stream
    * @param qte EQBbo
    */
   public void OnUpdate( String StreamName, EQBbo qte )
   {
      IndexRecord rec;

      if ( (rec=_GetRecord( qte )) != null ) {
         rec.Set( qte );
         _ShowRecord( rec );
      }
   }

   /**
    * Callback invoked when Equity Quote Message is received
    *
    * @param StreamName Name of this Data Stream
    * @param qte EQQuote
    */
   public void OnUpdate( String StreamName, EQQuote qte )
   {
      IndexRecord rec;

      if ( (rec=_GetRecord( qte )) != null ) {
         rec.Set( qte );
         _ShowRecord( rec );
      }
   }

   /**
    * Callback invoked when an Equity Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param trd EQTrade
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      IndexRecord rec;

      if ( (rec=_GetRecord( trd )) != null ) {
         rec.Set( trd );
         _ShowRecord( rec );
      }
   }


   //////////////////////
   // Helpers
   //////////////////////
   private IndexRecord _GetRecord( QuoddMsg qm )
   {
      IndexRecord rec;
      int         id;

      id  = qm.StreamID();
      rec = null;
      if ( _recs.containsKey( id ) )
         rec = (IndexRecord)_recs.get( id );
      return rec;
   }

   private void _ShowRecord( IndexRecord rec )
   {
      int row;

      // Pre-condition

      row  = rec._row;
      row += 2; // Header
      if ( row > _maxRow )
         return;

      // Safe to show ...


      System.out.printf( ANSI_ROWCOL, row, 1 );
      System.out.printf( "%-6s ", rec._tkr );
      System.out.printf( "%7.2f %-4s %6d  %12s |", 
         rec._dBid, rec._bidMmid, rec._bidSize, rec._bidTime );
      System.out.printf( "%7.2f %-4s %6d  %12s\n",
         rec._dAsk, rec._askMmid, rec._askSize, rec._askTime );
   }
}


/////////////////////////////////////////////////////////////////
//
//                c l a s s    I n d e x R e c o r d
//
/////////////////////////////////////////////////////////////////
/**
 * IndexRecord is a container class storing market data for a single 
 * record (participant).
 * <p>
 */
class IndexRecord
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
   public String _bidMmid;
   public double _dAsk;
   public long   _askSize;
   public String _askTime;
   public String _askMmid;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public IndexRecord( String tkr, int StreamID, int row )
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
      _bidMmid  = "????";
      _dAsk     = 0.0;
      _askSize  = 0;
      _askTime  = "00:00:00.00";
      _askMmid  = "????";
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   public void Set( EQQuote qte )
   {
      _dBid    = qte._bid;
      _bidSize = qte._bidSize;
      _bidTime = qte.pTimeMs().substring(1,12);
      _dAsk    = qte._ask;
      _askSize = qte._askSize;
      _askTime = _bidTime;
   }

   public void Set( EQBbo bbo )
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

   public void Set( EQTrade trd )
   {
      _trdPrc  = trd._trdPrc;
      _trdVol  = trd._trdVol;
      _trdTime = trd.pTimeMs().substring(1,12);
   }
}
