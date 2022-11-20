/******************************************************************************
*
*  OPTrade.java
*     QuoddMsg._mt    == 'o' (_mtOPTION)
*     QuoddMsg._mtSub == '*' (_opSubTRADE) -or-
*     QuoddMsg._mtSub == '+' (_opSubTRDCXL) -or-
*     QuoddMsg._mtSub == ',' (_opSubTRDSUMM) -or-
*     QuoddMsg._mtSub == '*' (_opSubTRADE_ID) -or- appendage -or-
*     QuoddMsg._mtSub == '+' (_opSubTRDCXL_ID) -or- appendage -or-
*
*     class OPTrade : public QuoddMsg
*     {
*     public:
*        u_char _trdPrc[8];
*        u_char _trdVol[4];
*        u_char _mktCtrLocCode[2];
*        u_char _trdCond;
*        u_char _trdFlags[2];
*        u_char _trdID[8];  // _opSubTRADE_ID / _opSubTRDCXL_ID
*        u_char _netChg[4];
*        u_char _pctChg[4];
*        u_char _high[4];
*        u_char _highTime[4];
*        u_char _low[4];
*        u_char _lowTime[4];
*        u_char _mktCtr[4];
*        u_char _acVol[4];
*        u_char _tnOvr[8];
*        u_char _open[4];
*        u_char _openVol[4];
*        u_char _openTime[4];
*        u_char _prcTck;
*     };
*
*  REVISION HISTORY:
*      9 APR 2012 jcs  Created.
*     17 MAY 2012 jcs  Build 20: v0.19: _openXxx
*     21 MAY 2012 jcs  Build 20: v0.19: _prcTck
*     22 AUG 2012 jcs  Build 34: v0.24: TradeFlags()
*      6 SEP 2012 jcs  Build 35: v0.24: PriceIsValid()
*     17 SEP 2012 jcs  Build 37: v0.24: _opSubTRDSUMM; IsSummary()
*     28 MAY 2014 jcs  Build 76: v0.24: public _iMktCtr; _opSubTRDCXL
*      6 JUL 2015 jcs  Build 94: v0.25: Dump(); _trdID; HasTradeID()
*     16 DEC 2015 jcs  Build 98: v0.25: Quote Appendage(s); _trdIDuniq
*  
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.msg;

import java.util.*;
import QuoddFeed.Enum.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;

/////////////////////////////////////////////////////////////////
// 
//                 c l a s s   O P T r a d e
//
/////////////////////////////////////////////////////////////////
/**
 * The OPTrade class is a parsed Short Form Trade message received from 
 * UltraCache.
 * <p> 
 * A OPTrade is generated by the {@link QuoddFeed.util.UltraChan} class
 * which invokes the
 * {@link QuoddFeed.util.UltraChan#OnUpdate(String,OPTrade)}
 * callback method to deliver the update into your application.
 */
public class OPTrade extends QuoddMsg
{
   static public int TRDSZ       = QuoddMsg.MINSZ + 70;
   static public int SUMSZ       = QuoddMsg.MINSZ + 82;
   static public int TRDIDSZ     = TRDSZ + 8;
   static public int TRDQTESZ    = TRDIDSZ + 32;
   static public int TRDQTESZ_EQ = TRDQTESZ + 32;

   ////////////////////
   // Instance Members
   ////////////////////
   public  double _trdPrc;
   public  long   _trdVol;
   public  int    _iMktCtr;
   public  char   _trdCond;
   public  int    _trdFlags;
   public  long   _trdID;
   public  long   _trdIDuniq;
   public  double _netChg;
   public  double _pctChg;
   public  double _high;
   public  double _highTime;
   public  double _low;
   public  double _lowTime;
   public  String _mktCtr;
   public  long   _acVol;
   public  long   _tnOvr;
   public  double _openPrc;
   public  long   _openVol;
   public  long   _openTime;
   public  char   _prcTck;
   public  double _vwap;
   /*
    * 15-11-12 Build 98: Quote Appendage (Options Contract)
    */
   private boolean _bHasQte;
   public  double  _bid;
   public  long    _bidSize;
   public  long    _bidTime;
   public  String  _bidMktCtr;
   public  double  _ask;
   public  long    _askSize;
   public  long    _askTime;
   public  String  _askMktCtr;    
   /*
    * 15-12-16 Build 98: Quote Appendage (Equity Underlyer)
    */
   private boolean EQ_bHasQte;
   public  double  EQ_bid;
   public  long    EQ_bidSize;
   public  long    EQ_bidTime;
   public  String  EQ_bidMktCtr;
   public  double  EQ_ask;
   public  long    EQ_askSize;
   public  long    EQ_askTime;
   public  String  EQ_askMktCtr;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public OPTrade()
   {
      _trdPrc    = 0.0;
      _trdVol    = 0;
      _iMktCtr   = 0;
      _trdCond   = '?';
      _trdFlags  = 0;
      _trdID     = 0;
      _trdIDuniq = 0;
      _netChg    = 0.0;
      _pctChg    = 0.0;
      _high      = 0.0;
      _highTime  = 0;
      _low       = 0.0;
      _lowTime   = 0;
      _mktCtr    = "";
      _acVol     = 0;
      _tnOvr     = 0;
      _openPrc   = 0.0;
      _openVol   = 0;
      _openTime  = 0;
      _prcTck    = '-';
      _vwap      = 0.0;
      /*
       * 15-11-12 Build 98: Quote Appendage (Options Contract)
       */
      _bHasQte   = false;
      _bid       = 0.0;
      _bidSize   = 0;
      _bidTime   = 0;
      _bidMktCtr = "";
      _ask       = 0.0;
      _askSize   = 0;     
      _askTime   = 0; 
      _askMktCtr = "";
      /*
       * 15-12-16 Build 98: Quote Appendage (Equity Underlyer)
       */
      EQ_bHasQte   = false;
      EQ_bid       = 0.0;
      EQ_bidSize   = 0;
      EQ_bidTime   = 0;
      EQ_bidMktCtr = "";
      EQ_ask       = 0.0;
      EQ_askSize   = 0;
      EQ_askTime   = 0;
      EQ_askMktCtr = "";
   }

   public OPTrade( byte[] b, int off, int nLeft )
   {
      this.Set( b, off, nLeft );
   }

   public OPTrade( OPTrade c )
   {
      super( c );
      _trdPrc    = c._trdPrc;
      _trdVol    = c._trdVol;
      _iMktCtr   = c._iMktCtr;
      _trdCond   = c._trdCond;
      _trdFlags  = c._trdFlags;
      _trdID     = c._trdID;
      _trdIDuniq = c._trdIDuniq;
      _netChg    = c._netChg;
      _pctChg    = c._pctChg;
      _high      = c._high;
      _highTime  = c._highTime;
      _low       = c._low;
      _lowTime   = c._lowTime;
      _mktCtr    = new String( c._mktCtr );
      _acVol     = c._acVol; 
      _tnOvr     = c._tnOvr;
      _openPrc   = c._openPrc;
      _openVol   = c._openVol;
      _openTime  = c._openTime;
      _prcTck    = c._prcTck;
      _vwap      = c._vwap;
      /*
       * 15-11-12 Build 98: Quote Appendage (Options Contract)
       */
      _bHasQte   = c._bHasQte;
      _bid       = _bHasQte ? c._bid       : 0.0;
      _bidSize   = _bHasQte ? c._bidSize   : 0;
      _bidTime   = _bHasQte ? c._bidTime   : 0;
      _bidMktCtr = _bHasQte ? c._bidMktCtr : "";
      _ask       = _bHasQte ? c._ask       : 0.0;
      _askSize   = _bHasQte ? c._askSize   : 0;
      _askTime   = _bHasQte ? c._askTime   : 0;
      _askMktCtr = _bHasQte ? c._askMktCtr : "";
      /*
       * 15-12-16 Build 98: Quote Appendage (Equity Underlyer)
       */
      EQ_bHasQte   = c.EQ_bHasQte;
      EQ_bid       = EQ_bHasQte ? c.EQ_bid       : 0.0;
      EQ_bidSize   = EQ_bHasQte ? c.EQ_bidSize   : 0;
      EQ_bidTime   = EQ_bHasQte ? c.EQ_bidTime   : 0;
      EQ_bidMktCtr = EQ_bHasQte ? c.EQ_bidMktCtr : "";
      EQ_ask       = EQ_bHasQte ? c.EQ_ask       : 0.0;
      EQ_askSize   = EQ_bHasQte ? c.EQ_askSize   : 0;
      EQ_askTime   = EQ_bHasQte ? c.EQ_askTime   : 0;
      EQ_askMktCtr = EQ_bHasQte ? c.EQ_askMktCtr : "";
   }

   public OPTrade clone() { return new OPTrade( this ); }


   ///////////////////////////////
   // Access
   ///////////////////////////////
   public boolean HasTradeID()
   {
      char mt2;

      mt2 = mtSub();
      return( ( mt2 == _opSubTRADE_ID ) || ( mt2 == _opSubTRDCXL_ID ) );
   }

   public boolean HasQuote()
   {
      return _bHasQte;
   }

   public boolean EQ_HasQuote()
   {
      return EQ_bHasQte;
   }

   public boolean IsCxl()
   {
      char mt2 = mtSub();

      return( ( mt2 == _opSubTRDCXL ) || ( mt2 == _opSubTRDCXL ) );
   }

   public boolean IsSummary()
   {
      return( mtSub() == _opSubTRDSUMM );
   }

   public String prcTck()
   {
      switch( _prcTck ) {
         case '^': return "UP";
         case 'v': return "DOWN";
         case '-': return "UNCHANGED";
      }
      return "?";
   }

   public String TradeFlags()
   {
      return OptionSpec.TradeFlags( this );
   }

   public boolean PriceIsValid()
   {
      return OptionSpec.PriceIsValid( this );
   }


   ///////////////////////////////
   // QuoddMsg Override - Dump 
   ///////////////////////////////
   @Override
   public String pMsgName()
   {
      String s;

      s = super.pMsgName();
      s += IsCxl()      ? "-Cxl" : "";
      s += IsSummary()  ? "-Summ" : "";
      return s;
   }

   @Override
   public String Dump()
   {
      String s, pb, pa, fmt;

      s  = super.Dump();
      s += s.format( "(%s) %d @ %8.4f", prcTck(), _trdVol, _trdPrc );
      s += s.format( " {%s}; ID=%d (uniq=%d)\n", _mktCtr, _trdID, _trdIDuniq );
      s += s.format( "   _netChg = %.4f; ", _netChg );
      s += s.format( "_pctChg = %.4f\n", _pctChg );
      s += s.format( "   _tnOvr = %d; ", _tnOvr );
      s += s.format( "_acVol = %d; ", _acVol );
      s += s.format( "_vwap = %.4f\n", _vwap );
      s += s.format( "   Cond = %c; Flags = %s\n", _trdCond, TradeFlags() );
      if ( HasQuote() ) {
         pb  = pTimeMs( JavaTime( _bidTime ) );
         pa  = pTimeMs( JavaTime( _askTime ) );
         fmt = "   %s %s : %4d @ %7.2f from {%s}\n";
         s  += s.format( fmt, "BID", pb, _bidSize, _bid, _bidMktCtr );
         s  += s.format( fmt, "ASK", pa, _askSize, _ask, _askMktCtr );
      }
      if ( EQ_HasQuote() ) {
         pb  = pTimeMs( JavaTime( EQ_bidTime ) );
         pa  = pTimeMs( JavaTime( EQ_askTime ) );
         fmt = "   EQ-%s %s : %4d @ %7.2f from {%s}\n";
         s  += s.format( fmt, "BID", pb, EQ_bidSize, EQ_bid, EQ_bidMktCtr );
         s  += s.format( fmt, "ASK", pa, EQ_askSize, EQ_ask, EQ_askMktCtr );
      }

      return s;
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   @Override
   public QuoddMsg Set( byte[] b, int off, int nLeft )
   {
      _bHasQte = false;
      super.Set( b, off, nLeft );
      switch( mtSub() ) {
         case _opSubTRADE:
         case _opSubTRDCXL:
            return SetTrade( b, off, nLeft );
         case _opSubTRADE_ID:
         case _opSubTRDCXL_ID:
            return SetTradeWithID( b, off, nLeft );
         case _opSubTRDSUMM:
            return SetSummary( b, off, nLeft );
      }
      return null;
   }


   ///////////////////////////////
   // Helpers
   ///////////////////////////////
   private QuoddMsg SetTrade( byte[] b, int off, int nLeft )
   {
      String   err;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _trdPrc    = BigEndian.GetPrc64( b, off+ 0 );
         _trdVol    = (long)BigEndian.GetInt32( b, off+8 );
         _iMktCtr   = BigEndian.GetInt16( b, off+12 );
         _trdCond   = (char)b[off+14];
         _trdFlags  = BigEndian.GetInt16( b, off+15 );
         _trdID     = 0;
         _trdIDuniq = 0;
         _netChg    = BigEndian.GetPrc32( b, off+17 );
         _pctChg    = BigEndian.GetPrc32( b, off+21 );
         _high      = BigEndian.GetPrc32( b, off+25 );
         _highTime  = BigEndian.GetInt32( b, off+29 );
         _low       = BigEndian.GetPrc32( b, off+33 );
         _lowTime   = BigEndian.GetInt32( b, off+37 );
         _mktCtr    = new String( b, off+41, 4 ).trim();
         _acVol     = BigEndian.GetInt32( b, off+45 );
         _tnOvr     = BigEndian.GetInt64( b, off+49 );
         _openPrc   = BigEndian.GetPrc32( b, off+57 );
         _openVol   = BigEndian.GetInt32( b, off+61 );
         _openTime  = BigEndian.GetInt32( b, off+65 );
         _prcTck    = (char)b[off+69];
         _vwap      = ( _acVol != 0.0 ) ? (double)_tnOvr / _acVol : 0.0;
         /*
          * Not populated : Quote Appendage (Options Contract)
          */
         _bHasQte   = false;
         _bid       = 0.0;
         _bidSize   = 0;
         _bidTime   = 0;
         _bidMktCtr = "";
         _ask       = 0.0;
         _askSize   = 0;
         _askTime   = 0;
         _askMktCtr = "";
         /*
          * Not populated : Quote Appendage (Equity Underlyer) 
          */
         EQ_bHasQte   = false;
         EQ_bid       = 0.0;
         EQ_bidSize   = 0;
         EQ_bidTime   = 0;
         EQ_bidMktCtr = "";
         EQ_ask       = 0.0;
         EQ_askSize   = 0;
         EQ_askTime   = 0;
         EQ_askMktCtr = "";
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }

   private QuoddMsg SetTradeWithID( byte[] b, int off, int nLeft )
   {
      String err;
      int    offQ;
      long   srcSeq, trdInPkt;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _trdPrc    = BigEndian.GetPrc64( b, off+ 0 );
         _trdVol    = (long)BigEndian.GetInt32( b, off+8 );
         _iMktCtr   = BigEndian.GetInt16( b, off+12 );
         _trdCond   = (char)b[off+14];
         _trdFlags  = BigEndian.GetInt16( b, off+15 );
         srcSeq     = BigEndian.GetInt32( b, off+17 );
         trdInPkt   = BigEndian.GetInt32( b, off+21 );
         _trdID     = srcSeq;
         _trdIDuniq = ( ( _trdID * 100 ) % 0x7fffffff ) + trdInPkt;
         _netChg    = BigEndian.GetPrc32( b, off+25 );
         _pctChg    = BigEndian.GetPrc32( b, off+29 );
         _high      = BigEndian.GetPrc32( b, off+33 );
         _highTime  = BigEndian.GetInt32( b, off+37 );
         _low       = BigEndian.GetPrc32( b, off+41 );
         _lowTime   = BigEndian.GetInt32( b, off+45 );
         _mktCtr    = new String( b, off+49, 4 ).trim();
         _acVol     = BigEndian.GetInt32( b, off+53 );
         _tnOvr     = BigEndian.GetInt64( b, off+57 );
         _openPrc   = BigEndian.GetPrc32( b, off+65 );
         _openVol   = BigEndian.GetInt32( b, off+69 );
         _openTime  = BigEndian.GetInt32( b, off+73 );
         _prcTck    = (char)b[off+77];
         _vwap      = ( _acVol != 0.0 ) ? (double)_tnOvr / _acVol : 0.0;

         /*
          * Quote Appendage (Options Contract)
          */
         _bHasQte   = false;
         _bid       = 0.0;
         _bidSize   = 0;
         _bidTime   = 0;
         _bidMktCtr = "";
         _ask       = 0.0;
         _askSize   = 0;
         _askTime   = 0;
         _askMktCtr = "";
         /*
          * Quote Appendage (Equity Underlyer)
          */
         EQ_bHasQte   = false;
         EQ_bid       = 0.0;
         EQ_bidSize   = 0;
         EQ_bidTime   = 0;
         EQ_bidMktCtr = "";
         EQ_ask       = 0.0;
         EQ_askSize   = 0;
         EQ_askTime   = 0;
         EQ_askMktCtr = "";

         // Are we populated??

         _bHasQte   = ( len() >= TRDQTESZ );
         EQ_bHasQte = ( len() >= TRDQTESZ_EQ );
         if ( _bHasQte ) {
            offQ       = off + ( TRDIDSZ - QuoddMsg.MINSZ );
            _bid       = BigEndian.GetPrc32( b, offQ+ 0 );
            _ask       = BigEndian.GetPrc32( b, offQ+ 4 );
            _bidSize   = BigEndian.GetInt32( b, offQ+ 8 );
            _askSize   = BigEndian.GetInt32( b, offQ+12 );
            _bidTime   = BigEndian.GetInt32( b, offQ+16 );
            _askTime   = BigEndian.GetInt32( b, offQ+20 );
            _bidMktCtr = new String( b, offQ+24, 4 ).trim();
            _askMktCtr = new String( b, offQ+28, 4 ).trim();
         }
         if ( EQ_bHasQte ) {
            offQ         = off + ( TRDQTESZ - QuoddMsg.MINSZ );
            EQ_bid       = BigEndian.GetPrc32( b, offQ+ 0 );
            EQ_ask       = BigEndian.GetPrc32( b, offQ+ 4 );
            EQ_bidSize   = BigEndian.GetInt32( b, offQ+ 8 );
            EQ_askSize   = BigEndian.GetInt32( b, offQ+12 );
            EQ_bidTime   = BigEndian.GetInt32( b, offQ+16 );
            EQ_askTime   = BigEndian.GetInt32( b, offQ+20 );
            EQ_bidMktCtr = new String( b, offQ+24, 4 ).trim();
            EQ_askMktCtr = new String( b, offQ+28, 4 ).trim();
         }
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }

   private QuoddMsg SetSummary( byte[] b, int off, int nLeft )
   {
      String err;
      double dUnd, bid, ask;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _iMktCtr  = BigEndian.GetInt16( b, off+ 0 );
         _trdPrc   = BigEndian.GetPrc64( b, off+ 2 );
         _openPrc  = BigEndian.GetPrc64( b, off+10 );
         _high     = BigEndian.GetPrc64( b, off+18 );
         _low      = BigEndian.GetPrc64( b, off+26 );
         bid       = BigEndian.GetPrc64( b, off+34 );
         ask       = BigEndian.GetPrc64( b, off+42 );
         _netChg   = BigEndian.GetPrc64( b, off+50 );
         dUnd      = BigEndian.GetPrc64( b, off+58 );
         _acVol    = BigEndian.GetInt64( b, off+66 );
         _openVol  = BigEndian.GetInt64( b, off+74 );

         // Not populated

         _trdVol    = 0;
         _trdCond   = 0;
         _trdFlags  = 0;
         _trdID     = 0;
         _trdIDuniq = 0;
         _pctChg    = 0;
         _highTime  = 0;
         _lowTime   = 0;
         _mktCtr    = new String( b, off+41, 4 ).trim();
         _tnOvr     = 0;
         _openTime  = 0;
         _prcTck    = 0;
         _vwap      = 0.0;
         /*
          * Not populated : Quote Appendage (Options Contract)
          */
         _bHasQte   = false;
         _bid       = 0.0;
         _bidSize   = 0;
         _bidTime   = 0;
         _bidMktCtr = "";
         _ask       = 0.0;
         _askSize   = 0;
         _askTime   = 0;
         _askMktCtr = "";
         /*
          * Not populated : Quote Appendage (Equity Underlyer)
          */
         EQ_bHasQte   = false;
         EQ_bid       = 0.0;
         EQ_bidSize   = 0;
         EQ_bidTime   = 0;
         EQ_bidMktCtr = "";
         EQ_ask       = 0.0;
         EQ_askSize   = 0;
         EQ_askTime   = 0;
         EQ_askMktCtr = "";
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }
}
