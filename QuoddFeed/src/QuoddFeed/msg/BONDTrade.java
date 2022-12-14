/******************************************************************************
*
*  BONDTrade.java
*     QuoddMsg._mt    == 'b'  (_mtBOND)
*     QuoddMsg._mtSub == 0x28 (_boSubTRADE) - or -
*     QuoddMsg._mtSub == 0x29 (_boSubTRDCXL) - or -
*     QuoddMsg._mtSub == 0x2a (_boSubTRDCORR)
*
*     class BONDTrade : public QuoddMsg
*     {
*     public:
*        u_char _mktCtrLocCode[2];
*        u_char _reference[8];
*        u_char _trdPrc[8];
*        u_char _trdVol[8];
*        u_char _trdCond[4];
*        u_char _trdFlags[2];
*        u_char _netChg[4];
*        u_char _pctChg[4];
*        u_char _high[4];
*        u_char _highTime[4];
*        u_char _low[4];
*        u_char _lowTime[4];
*        u_char _mktCtr[4];
*        u_char _acVol[4];
*        u_char _tnOvr[8];
*     };
*
*  REVISION HISTORY:
*     22 FEB 2012 jcs  Created.
*      1 APR 2013 jcs  Build  56: No mo _oldXxx
*     23 JUN 2015 jcs  Build  94: Dump()
*     24 MAR 2016 jcs  Build 100: PrevClose()
*
*  (c) 1994-2016 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.msg;

import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;

/////////////////////////////////////////////////////////////////
// 
//                 c l a s s   B O N D T r a d e
//
/////////////////////////////////////////////////////////////////
/**
 * The BONDTrade class is a parsed Trade, Cxl or Correction message received
 * from the UltraCache.
 * <p> 
 * A BONDTrade is generated by the {@link QuoddFeed.util.UltraChan} class
 * which invokes the
 * {@link QuoddFeed.util.UltraChan#OnUpdate(String,BONDTrade)}
 * callback method to deliver the update into your application.
 */
public class BONDTrade extends QuoddMsg
{
   static public int MINSZ = QuoddMsg.MINSZ + 72;

   private int    _iMktCtr;
   public  long   _refNum;
   public  double _trdPrc;
   public  long   _trdVol;
   public  String _trdCond;
   public  int    _trdFlags;
   public  double _netChg;
   public  double _pctChg;
   public  double _high;
   public  double _highTime;
   public  double _low;
   public  double _lowTime;
   public  String _mktCtr;
   public  long   _acVol;
   public  long   _tnOvr;
   public  double _vwap;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public BONDTrade()
   {
      _iMktCtr    = 0;
      _refNum     = 0;
      _trdPrc     = 0.0;
      _trdVol     = 0;
      _trdCond    = "";
      _trdFlags   = 0;
      _netChg     = 0.0;
      _pctChg     = 0.0;
      _high       = 0.0;
      _highTime   = 0;
      _low        = 0.0;
      _lowTime    = 0;
      _mktCtr     = "";
      _acVol      = 0;
      _tnOvr      = 0;
      _vwap       = 0.0;
   }

   public BONDTrade( byte[] b, int off, int nLeft )
   {
      this.Set( b, off, nLeft );
   }

   public BONDTrade( BONDTrade c )
   {
      super( c );
      _iMktCtr    = c._iMktCtr;
      _refNum     = c._refNum;
      _trdPrc     = c._trdPrc;
      _trdVol     = c._trdVol;
      _trdCond    = new String( c._trdCond );
      _trdFlags   = c._trdFlags;
      _netChg     = c._netChg;
      _pctChg     = c._pctChg;
      _high       = c._high;
      _highTime   = c._highTime;
      _low        = c._low;
      _lowTime    = c._lowTime;
      _mktCtr     = new String( c._mktCtr );
      _acVol      = c._acVol; 
      _tnOvr      = c._tnOvr;
      _vwap       = c._vwap;
   }

   public BONDTrade clone() { return new BONDTrade( this ); }


   ///////////////////////////////
   // Access
   ///////////////////////////////
   /**
    * \brief Returns previous closing price
    *
    * \return Previous close
    */
   public double PrevClose()
   {
      return _trdPrc - _netChg;
   }

   /**
    * @return true if this is a Trade Cancel msg
    */
   public boolean IsCancel()
   {
      return( mtSub() == _boSubTRDCXL );
   }

   /**
    * @return true if this is a Trade Correction msg
    */
   public boolean IsCorrection()
   {
      return( mtSub() == _boSubTRDCORR );
   }

   public String prcTck()
   {
/*
      switch( _trdCond ) { // TODO
         case '1': // Uptick
            return "^";
         case '2': // Downtick
            return "v";
         case '3': // Zero Uptick
            return "^0";
         case '4': // Zero Downtick
            return "v0";
      }
      return "?";
 */
      return _trdCond;
   }


   ///////////////////////////////
   // QuoddMsg Override - Dump 
   ///////////////////////////////
   @Override
   public String Dump()
   {
      String s;

      s  = super.Dump();
      s += s.format( "(%s) %d @ %8.4f", prcTck(), _trdVol, _trdPrc );
      s += s.format( " {%s}\n", _mktCtr );
      s += s.format( "   _netChg = %.4f; ", _netChg );
      s += s.format( "_pctChg = %.4f\n", _pctChg );
      s += s.format( "   _tnOvr = %d; ", _tnOvr );
      s += s.format( "_acVol = %d; ", _acVol );
      s += s.format( "_vwap = %.4f\n", _vwap );
      return s;
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   @Override
   public QuoddMsg Set( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;
      String err;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _iMktCtr    = BigEndian.GetInt16( b, off+ 0 );
         _refNum     = BigEndian.GetInt64( b, off+ 2 );
         _trdPrc     = BigEndian.GetPrc64( b, off+10 );
         _trdVol     = BigEndian.GetInt64( b, off+18 );
         _trdCond    = new String( b, off+26, 4 );
         _trdFlags   = BigEndian.GetInt16( b, off+30 );
         _netChg     = BigEndian.GetPrc32( b, off+32 );
         _pctChg     = BigEndian.GetPrc32( b, off+36 );
         _high       = BigEndian.GetPrc32( b, off+40 );
         _highTime   = BigEndian.GetInt32( b, off+44 );
         _low        = BigEndian.GetPrc32( b, off+48 );
         _lowTime    = BigEndian.GetInt32( b, off+52 );
         _mktCtr     = new String( b, off+56, 4 ).trim();
         _acVol      = BigEndian.GetInt32( b, off+60 );
         _tnOvr      = BigEndian.GetInt64( b, off+64 );
         _vwap       = ( _acVol != 0.0 ) ? (double)_tnOvr / _acVol : 0.0;
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }
}
