/******************************************************************************
*
*  FUTRQuote.java
*     QuoddMsg._mt    == 'T'  (_mtFUTURE)
*     QuoddMsg._mtSub == '0'  (_ftSubCOMBOQTE) - or -
*     QuoddMsg._mtSub == '4'  (_ftSubQUOTE) - or -
*     QuoddMsg._mtSub == '5'  (_ftSubEXCQTE)
*
*     class FUTRQuote : public QuoddMsg
*     {
*     public:
*        u_char _bid[8];
*        u_char _ask[8];
*        u_char _bidSize[4];
*        u_char _askSize[4];
*        u_char _mktCtrLocCode[2];
*        u_char _bidCond;
*        u_char _askCond;
*        u_char _flags[2];
*        u_char _sessionId;
*     };
*
*     class FUTRExcQuote : public QuoddMsg
*     {
*     public:
*        u_char _mktCtrLocCode[2];
*        u_char _typeCode;
*        u_char _sessionId;
*        u_char _prc[8];
*        u_char _side;
*        u_char _cond;
*        u_char _vol[8];
*        u_char _volInd;
*        u_char _excInd;;
*        u_char _asofSeqNum[4];
*     };
*
*     class FUTRComboLeg : public QuoddMsg
*     {
*     public:
*        u_char _mktCtrLocCode[2];
*        u_char _comboLocCode[4];
*        u_char _typeCode;
*        u_char _sessionId;
*        u_char _buyOrSell;
*        u_char _price[8];
*        u_char _sizeVol[4];
*     };
*
*  REVISION HISTORY:
*      9 JAN 2012 jcs  Created.
*     22 FEB 2012 jcs  Build 14: v0.12: Copy constructor
*      6 JUN 2012 jcs  Build 23: v0.20: FuturesSpec
*     15 OCT 2012 jcs  Build 43: v0.24: QuoteFlags()
*     23 MAR 2015 jcs  Build 92: v0.24: SetMultiplier()
*     23 JUN 2015 jcs  Build 94: v0.25: Dump()
*     27 JUL 2015 jcs  Build 95: v0.25: SetExcQuote() byte offset bug fix
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
//                 c l a s s   F U T R Q u o t e
//
/////////////////////////////////////////////////////////////////
/**
 * The FUTRQuote class is a parsed Futures Quote message is received from 
 * UltraCache.
 * <p> 
 * A FUTRQuote is generated by the {@link QuoddFeed.util.UltraChan} class
 * which invokes the
 * {@link QuoddFeed.util.UltraChan#OnUpdate(String,FUTRQuote)}
 * callback method to deliver the update into your application.
 */
public class FUTRQuote extends QuoddMsg
{
   static public int QTESZ    = QuoddMsg.MINSZ + 31;
   static public int EXCQTESZ = QuoddMsg.MINSZ + 28;
   static public int COMBOSZ  = QuoddMsg.MINSZ + 21;

   private int     _iMktCtr;
   private boolean _bExcQte;
   private boolean _bComboQte;
   public  char    _typeCode;
   public  char    _sessionID;
   public  char    _side;
   public  double  _bid;
   public  long    _bidSize;
   public  char    _bidCond;
   public  double  _ask;
   public  long    _askSize;
   public  char    _askCond;
   public  int     _qteFlag;
   public  char    _volInd;
   public  char    _excInd;
   public  int     _asofSeq;
   public  long    _comboCode;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public FUTRQuote()
   {
      _iMktCtr   = 0;
      _bExcQte   = false;
      _bComboQte = false;
      _typeCode  = 0;
      _sessionID = 0;
      _side      = '?';
      _bid       = 0.0;
      _bidSize   = 0;
      _bidCond   = 0;
      _ask       = 0.0;
      _askSize   = 0;
      _askCond   = 0;
      _qteFlag   = 0;
      _volInd    = 0;
      _excInd    = 0;
      _asofSeq   = 0;
      _comboCode = 0;
   }

   public FUTRQuote( byte[] b, int off, int nLeft )
   {
      this.Set( b, off, nLeft );
   }     

   public FUTRQuote( FUTRQuote c )
   {
      super( c );
      _iMktCtr   = c._iMktCtr;
      _bExcQte   = c._bExcQte;
      _bComboQte = c._bComboQte;
      _typeCode  = c._typeCode;
      _sessionID = c._sessionID;
      _side      = c._side;
      _bid       = c._bid;
      _bidSize   = c._bidSize;
      _bidCond   = c._bidCond;
      _ask       = c._ask;
      _askSize   = c._askSize;
      _askCond   = c._askCond;
      _qteFlag   = c._qteFlag;
      _volInd    = c._volInd;
      _excInd    = c._excInd;
      _asofSeq   = c._asofSeq;
      _comboCode = c._comboCode;
   }

   public FUTRQuote clone() { return new FUTRQuote( this ); }


   ///////////////////////////////
   // Access
   ///////////////////////////////
   public String QuoteFlags()
   {
      return FuturesSpec.QuoteFlags( this );
   }

   public String ExceptionalCondition()
   {
      String pe;

      pe = FuturesSpec.ExceptionalIndicator( this );
      return _bExcQte ? pe : "";
   }

   public boolean IsExcQuote()
   {
      return _bExcQte;
   }

   public boolean IsComboQuote()
   {
      return _bComboQte;
   }


   ///////////////////////////////
   // QuoddMsg Override - Dump 
   ///////////////////////////////
   @Override
   public String pMsgName()
   {
      String s, pt;

      s  = super.pMsgName();
      pt = IsExcQuote()   ? "-EXCQTE " : "-QTE ";
      pt = IsComboQuote() ? "-COMBOQTE  " : pt;
      s += pt;
      return s;
   }

   @Override
   public String Dump()
   {
      String s;

      s  = super.Dump();
      s += s.format( "%8.6f x %8.6f; %dx%d\n",
         _bid, _ask, _bidSize, _askSize );
      return s;
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   /**
    * Called AFTER the message has been parsed via Set() to modify the    
    * message prices.
    *
    * @param dMult Multiplier
    */
   public void SetMultiplier( double dMult )
   {
      _bid *= dMult;
      _ask *= dMult;
   }

   @Override
   public QuoddMsg Set( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;
      char     mt2;

      super.Set( b, off, nLeft );
      mt2        = mtSub();
      _bExcQte   = ( mt2 == _ftSubEXCQTE );
      _bComboQte = ( mt2 == _ftSubCOMBOQTE );
      switch( mt2 ) {
         case _ftSubEXCQTE:
            return SetExcQuote( b, off, nLeft );
         case _ftSubCOMBOQTE:
            return SetComboQuote( b, off, nLeft );
         case _ftSubQUOTE:
            return SetQuote( b, off, nLeft );
      }
      return null;
   }


   ///////////////////////////////
   // Helpers
   ///////////////////////////////
   /**
    * Parse the Futures Quote message from the incoming byte buffer.
    * <p>
    * @param b Input buffer
    * @param off Offset in b
    * @param nLeft NumBytes remaining in Input byte[] array after offset.
    * @return this
    */
   private QuoddMsg SetQuote( byte[] b, int off, int nLeft )
   {
      String err;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _bid       = BigEndian.GetPrc64( b, off+ 0 );
         _ask       = BigEndian.GetPrc64( b, off+ 8 );
         _bidSize   = BigEndian.GetInt32( b, off+16 );
         _askSize   = BigEndian.GetInt32( b, off+20 );
         _iMktCtr   = BigEndian.GetInt16( b, off+24 );
         _bidCond   = (char)b[off+26];
         _askCond   = (char)b[off+27];
         _qteFlag   = BigEndian.GetInt16( b, off+28 );
         _sessionID = (char)b[off+30];

         // Not set

         _typeCode  = 0;
         _volInd    = 0;
         _excInd    = 0;
         _asofSeq   = 0;
         _comboCode = 0;
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }

   /**
    * Parse the Exceptional Quotation message from the incoming byte buffer.
    * <p>
    * @param b Input buffer
    * @param off Offset in b
    * @param nLeft NumBytes remaining in Input byte[] array after offset.
    * @return this
    */
   private QuoddMsg SetExcQuote( byte[] b, int off, int nLeft )
   {
      String  err;
      double  dPrc;
      long    vol;
      char    cond;
      boolean bBuy;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _iMktCtr   = BigEndian.GetInt16( b, off+ 0 );
         _typeCode  = (char)b[off+2];
         _sessionID = (char)b[off+3];
         dPrc       = BigEndian.GetPrc64( b, off+ 4 );
         _side      = (char)b[off+12];
         cond       = (char)b[off+13];
         vol        = (long)BigEndian.GetInt64( b, off+ 14 );
         _volInd    = (char)b[off+22];
         _excInd    = (char)b[off+23];
         _asofSeq   = BigEndian.GetInt16( b, off+ 24 );

         // Buy or Sell?

         bBuy       = ( _side == 'B' );
         _bid       = bBuy ? dPrc : 0.0; 
         _bidSize   = bBuy ? vol  : 0;
         _bidCond   = bBuy ? cond : 0;
         _ask       = bBuy ? 0.0 : dPrc;
         _askSize   = bBuy ? 0   : vol;
         _askCond   = bBuy ? 0   : cond;

         // Not set

         _qteFlag   = 0;
         _comboCode = 0;
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }

   /**
    * Parse the ComboLeg Quotation message from the incoming byte buffer.
    * <p>
    * @param b Input buffer
    * @param off Offset in b
    * @param nLeft NumBytes remaining in Input byte[] array after offset.
    * @return this
    */
   private QuoddMsg SetComboQuote( byte[] b, int off, int nLeft )
   {
      String  err;
      double  dPrc;
      long    vol;
      char    cond;
      boolean bBuy;

      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _iMktCtr   = BigEndian.GetInt16( b, off+ 0 );
         _comboCode = BigEndian.GetInt32( b, off+ 2 );
         _typeCode  = (char)b[off+6];
         _sessionID = (char)b[off+7];
         _side      = (char)b[off+8];
         dPrc       = BigEndian.GetPrc64( b, off+ 9 );
         vol        = BigEndian.GetInt32( b, off+17 );

         // Buy or Sell?

         bBuy       = ( _side == 'B' );
         _bid       = bBuy ? dPrc : 0.0;
         _bidSize   = bBuy ? vol  : 0;
         _ask       = bBuy ? 0.0 : dPrc;
         _askSize   = bBuy ? 0   : vol;

         // Not set

         _bidCond   = 0;
         _askCond   = 0;
         _qteFlag   = 0;
         _volInd    = 0;
         _excInd    = 0;
         _asofSeq   = 0;
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }
}
