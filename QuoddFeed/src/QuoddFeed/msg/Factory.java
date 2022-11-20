/******************************************************************************
*
*  Factory.java
*     UltraCache wire protocol message factory
*
*  REVISION HISTORY:
*     13 OCT 2011 jcs  Created.
*      9 APR 2012 jcs  Build  15: No mo JNI; Moved out of QuoddMsg
*     10 APR 2012 jcs  Build  16: _ixSubETPINTRA
*      7 MAY 2012 jcs  Build  18: EQMktStats
*     15 MAY 2012 jcs  Build  19: All _fundSubXXXX
*     29 MAY 2012 jcs  Build  21: _eqSubSUMMARY / _eqSubMKTCTRSUMM
*      8 JUN 2012 jcs  Build  23: _blSubFUNDY
*     19 JUN 2012 jcs  Build  23: _blSubFUT_CHAIN
*     10 JUL 2012 jcs  Build  28: _blSubFUT_SNAP
*     17 SEP 2012 jcs  Build  37: _opSubTRDSUMM
*     20 SEP 2012 jcs  Build  38: Image.NEWSZ; _blSubMKT_CAT
*     13 NOV 2012 jcs  Build  44: _ixSubNAV, etc.
*      4 JAN 2013 jcs  Build  50: FUTRSumm / FUTRMisc
*     25 FEB 2013 jcs  Build  54: EQLimitUpDn
*     11 APR 2013 jcs  Build  56: _bBasicBug
*     14 MAY 2013 jcs  Build  59: Tracer
*     25 MAY 2013 jcs  Build  61: EQBbo.SetFromImg()
*     17 JUN 2013 jcs  Build  64: IsLvl2()
*     26 FEB 2014 jcs  Build  75: _ixSubSETLVAL / _ixSubSETLSUMM
*     28 MAY 2014 jcs  Build  76: _eqSubTRDCXL; _opSubTRDCXL
*     12 JAN 2015 jcs  Build  89a:UC_LEVEL2_NAME
*     12 FEB 2015 jcs  Build  90: _blSubFUTOPT_CHAIN
*     14 MAY 2015 jcs  Build  92: _ftSubSETLPRC
*     24 MAY 2015 jcs  Build  93: Image._bExtFlds
*      7 JUL 2015 jcs  Build  94: _blSubUNDERLYERS; _opSubTRADE_ID
*     11 NOV 2015 jcs  Build  98: _opSubTRADE_ID appendage
*     22 FEB 2016 jcs  Build 100: _blSubSORT
*      8 DEC 2016 jcs  Build 101: _eqSubTRDASOF / _eqSubTRDASOFCXL
*      9 AUG 2017 jcs  Build 103: _eqSubTRDCORR; _eqSubTRDASOFCORR
*
*  (c) 2011-2017 Quodd Financial
*******************************************************************************/
package QuoddFeed.msg;

import QuoddFeed.Enum.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//                c l a s s   F a c t o r y
//
/////////////////////////////////////////////////////////////////
/**
 * The Factory class parses and generates the appropriate QuoddMsg object 
 * based on the message type / sub-type.
 * 
 * @author (c) 2011-2013 Quodd Financial.  All rights reserved.
 */
public class Factory extends MsgTypes
{
   ///////////////////////////
   // Class-wide Private Parts
   ///////////////////////////
   private Image       _img;
   private Tracer      _trc;
   private EQQuote     _qte;
   private EQQuoteMM   _qteMM;
   private OPQuote     _qto;
   private EQBbo       _bbe;
   private EQBboMM     _bbeMM;
   private OPBbo       _bbo;
   private OPTrade     _tro;
   private EQTrade     _trd;
   private EQTradeSts  _trdSts;
   private EQMktStats  _mkt;
   private EQLimitUpDn _lim;
   private FUNDnav     _fuNav;
   private IDXValue    _ixV;
   private IDXSummary  _ixS;
   private FUTRQuote   _ftQte;
   private FUTRTrade   _ftTrd;
   private FUTRSumm    _ftSum;
   private FUTRMisc    _ftMsc;
   private BONDQuote   _boQte;
   private BONDTrade   _boTrd;
   private BlobList    _bL;
   private BlobTable   _bA;
   private Status      _sts;
   private UCStatus    _stsUC;
   private QuoddMsg    _qm;
   private boolean     _bBasicBug;
   private boolean     _bLvl2;
   private boolean     _bLvl2Name;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public Factory()
   {
      _img       = new Image();
      _trc       = new Tracer();
      _qte       = new EQQuote();
      _qteMM     = new EQQuoteMM();
      _qto       = new OPQuote();
      _bbe       = new EQBbo();
      _bbeMM     = new EQBboMM();
      _bbo       = new OPBbo();
      _tro       = new OPTrade();
      _trd       = new EQTrade();
      _trdSts    = new EQTradeSts();
      _mkt       = new EQMktStats();
      _lim       = new EQLimitUpDn();
      _fuNav     = new FUNDnav();
      _ixV       = new IDXValue();
      _ixS       = new IDXSummary();
      _ftQte     = new FUTRQuote();
      _ftTrd     = new FUTRTrade();
      _ftSum     = new FUTRSumm();
      _ftMsc     = new FUTRMisc();
      _boQte     = new BONDQuote();
      _boTrd     = new BONDTrade();
      _bL        = new BlobList();
      _bA        = new BlobTable();
      _sts       = new Status();
      _stsUC     = new UCStatus();
      _qm        = new QuoddMsg();
      _bBasicBug = UCconfig.HasKey( "UC_BASICBUG" );
      _bLvl2     = UCconfig.HasKey( "UC_LEVEL2" );
      _bLvl2Name = UCconfig.HasKey( "UC_LEVEL2_NAME" );
      UltraFeed._bBasic = _bBasicBug;
   }


   ///////////////////////////////
   // Access
   ///////////////////////////////
   public boolean IsBasicBug()
   {
      return _bBasicBug;
   }

   public boolean IsLvl2()
   {
      return _bLvl2;
   }

   public boolean IsFixLvl2Name()
   {
      return _bLvl2Name;
   }


   ///////////////////////////////
   // Operations
   ///////////////////////////////
   public QuoddMsg Parse( byte[] b, int off, int nLeft )
   {
      char     mt, mt2;
      int      bSz, expSz;
      boolean  bQM, bLong, bSHO, bSum, bCxl, bExt;
      QuoddMsg rtn;

      // Each time ...

      mt   = (char)b[off+2];
      mt2  = (char)b[off+3];

      // Image Sub-Type is consistent across all MsgTypes except Blob

      if ( mt2 == _mtSubIMG ) {
         switch( mt ) {
            case _mtEQUITY:
            case _mtOPTION:
            case _mtFUND:
            case _mtINDEX:
            case _mtFUTURE:
            case _mtBOND:
               expSz = Image.MINSZ;
               if ( nLeft >= expSz ) {
                  _img.Set( b, off, nLeft );
                  expSz = ( _img.len() > expSz ) ? Image.NEWSZ : expSz;
                  if ( _img._bExtFlds )
                     expSz = Image.NEWSZ_EXT;
                  rtn  = _img.SetName( expSz );
                  if ( _bBasicBug )
                     rtn = _bbe.SetFromImg( _img, b, off, nLeft );
                  return rtn;
               }
               break;
         }
      }

      // Tracer

      if ( mt2 == _mtSubTRACER ) {
         expSz = Tracer.MINSZ;
         if ( nLeft >= expSz ) {
            rtn = _trc.Set( b, off, nLeft ).SetName( expSz );
            return rtn;
         }
         return null;
      }

      // Non-image

      bQM = false;
      switch( mt ) {
         case _mtEQUITY:
            switch( mt2 ) {
               case _eqSubQTESHORT:
               case _eqSubQTELONG:
                  bLong = ( mt2 == _eqSubQTELONG );
                  expSz = bLong ? EQQuote.LONGSZ : EQQuote.SHORTSZ;
                  if ( nLeft >= expSz ) {
                     rtn = _qte.Set( b, off, nLeft ).SetName( expSz );
                     if ( _bBasicBug )
                        rtn = _bbe.SetFromQte( _qte, b, off, nLeft );
                     if ( IsFixLvl2Name() )
                        FixLevel2Name( rtn, _qte._mktCtr );
                     return rtn;
                  }
                  break;
               case _eqSubBBOSHORT:
               case _eqSubBBOLONG:
                  bLong = ( mt2 == _eqSubBBOLONG );
                  expSz = bLong ?  EQBbo.LONGSZ : EQBbo.SHORTSZ;
                  if ( nLeft >= expSz )
                     return _bbe.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _eqSubQTESHORTMM:
               case _eqSubQTELONGMM:
                  bLong = ( mt2 == _eqSubQTELONGMM );
                  expSz = bLong ?  EQQuoteMM.LONGSZ : EQQuoteMM.SHORTSZ;
                  if ( nLeft >= expSz ) {
                     _qteMM.Set( b, off, nLeft ).SetName( expSz );
                     if ( _bLvl2 && !_qteMM.IsNazzLevel2() ) {
                        if ( !_qteMM.CanDisplayBid() )
                           _qteMM._bid = 0.0;
                        if ( !_qteMM.CanDisplayAsk() )
                           _qteMM._ask = 0.0;
                     }
                     if ( IsFixLvl2Name() )
                        FixLevel2Name( _qteMM, _qteMM._mktCtr );
                     return _qteMM;
                  }
                  break;
               case _eqSubBBOSHORTMM:
               case _eqSubBBOLONGMM:
                  bLong = ( mt2 == _eqSubBBOLONGMM );
                  expSz = bLong ?  EQBboMM.LONGSZ : EQBboMM.SHORTSZ;
                  if ( nLeft >= expSz ) {
                     _bbeMM.Set( b, off, nLeft );
                     return _bbeMM.SetName( expSz );
                  }
                  break;
               case _eqSubTRDSHORT:
               case _eqSubSUMMARY:
               case _eqSubMKTCTRSUMM:
                  expSz = EQTrade.SHORTSZ;
                  if ( nLeft >= expSz )
                     return _trd.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _eqSubTRDLONG:
               case _eqSubTRDCXL:
                  expSz = EQTrade.LONGSZ;
                  if ( nLeft >= expSz )
                     return _trd.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _eqSubTRDCORR:
                  expSz = EQTrade.CORRSZ;
                  if ( nLeft >= expSz )
                     return _trd.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _eqSubTRDASOF:
               case _eqSubTRDASOFCXL:
                  expSz = EQTrade.ASOFSZ;
                  if ( nLeft >= expSz )
                     return _trd.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _eqSubTRDASOFCORR:
                  expSz = EQTrade.ASOFSZ_CORR;
                  if ( nLeft >= expSz )
                     return _trd.Set( b, off, nLeft ).SetName( expSz );
               case _eqSubTRDACTION:
               case _eqSubREGSHO:
                  bSHO  = ( mt2 == _eqSubREGSHO );
                  expSz = bSHO ?  EQTradeSts.SHOSZ : EQTradeSts.ACTSZ;
                  if ( nLeft >= expSz )
                     return _trdSts.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _eqSubMKTSTATS:
                  expSz = EQMktStats.MINSZ;
                  if ( nLeft >= expSz )
                     return _mkt.Set( b, off, nLeft );
                  break;
               case _eqSubLIMITUPDN:
                  expSz = EQLimitUpDn.MINSZ;
                  if ( nLeft >= expSz )
                     return _lim.Set( b, off, nLeft );
                  break;
               default:
                  bQM = true;
                  break;
            }
            break;
         case _mtOPTION:
            switch( mt2 ) {
               case _opSubQTESHORT:
               case _opSubQTELONG:
                  bLong = ( mt2 == _opSubQTELONG );
                  expSz = bLong ? OPQuote.LONGSZ : OPQuote.SHORTSZ;
                  if ( nLeft >= expSz )
                     return _qto.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _opSubBBOSHORT:
               case _opSubBBOLONG:
                  bLong = ( mt2 == _opSubBBOLONG );
                  expSz = bLong ? OPBbo.LONGSZ : OPBbo.SHORTSZ;
                  if ( nLeft >= expSz )
                     return _bbo.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _opSubTRADE:
               case _opSubTRDCXL:
               case _opSubTRDSUMM:
               case _opSubTRADE_ID:
               case _opSubTRDCXL_ID:
                  expSz = OPTrade.TRDSZ;
                  switch( mt2 ) {
                     case _opSubTRDSUMM:    expSz = OPTrade.SUMSZ;    break;
                     case _opSubTRADE_ID:   expSz = OPTrade.TRDIDSZ;  break;
                     case _opSubTRDCXL_ID:  expSz = OPTrade.TRDIDSZ;  break;
                  }
                  if ( nLeft >= expSz )
                     return _tro.Set( b, off, nLeft ).SetName( expSz );
                  break;
               default:
                  bQM = true;
                  break;
            }
            break;
         case _mtFUND:
            switch( mt2 ) {
               case _fundSubFUNDNAV:
               case _fundSubMMNAV:
               case _fundSubCAPDISTRO:
               case _fundSubDIVID_INT:
                  expSz = FUNDnav.MinSize( mt2 );
                  if ( nLeft >= expSz )
                     return _fuNav.Set( b, off, nLeft ).SetName( expSz );
                  break;
               default:
                  bQM = true;
                  break;
            }
            break;
         case _mtINDEX:
            switch( mt2 ) {
               case _ixSubVALUE:
               case _ixSubETPINTRA:
                  expSz = IDXValue.VALSZ;
                  if ( nLeft >= expSz )
                     return _ixV.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ixSubSETLVAL:
                  expSz = IDXValue.SETLSZ;
                  if ( nLeft >= expSz )
                     return _ixV.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ixSubSETLSUMM:
                  expSz = IDXValue.SETLSUMSZ;
                  if ( nLeft >= expSz )
                     return _ixV.Set( b, off, nLeft ).SetName( expSz );
               case _ixSubEST_CASH:
               case _ixSubTOT_CASH:
               case _ixSubNAV:
               case _ixSubSHARES:
               case _ixSubETPDIV:
                  expSz = IDXValue.MINSZ;
                  if ( nLeft >= expSz )
                     return _ixV.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ixSubSUMM:
                  expSz = IDXSummary.MINSZ;
                  if ( nLeft >= expSz )
                     return _ixS.Set( b, off, nLeft ).SetName( expSz );
                  break;
               default:
                  bQM = true;
                  break;
            }
            break;
         case _mtFUTURE:
            switch( mt2 ) {
               case _ftSubCOMBOQTE:
                  expSz = FUTRQuote.COMBOSZ;
                  if ( nLeft >= expSz )
                     return _ftQte.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ftSubTRADE:
               case _ftSubTRDCXL:
               case _ftSubTRDCORR:
                  bCxl  = ( mt2 != _ftSubTRADE );
                  expSz = bCxl ? FUTRTrade.CXLSZ : FUTRTrade.TRDSZ;
                  if ( nLeft >= expSz )
                     return _ftTrd.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ftSubSUMMARY:
                  expSz = FUTRSumm.MINSZ;
                  if ( nLeft >= expSz )
                     return _ftSum.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ftSubHILOLAST:
               case _ftSubOPENINT:
               case _ftSubSETLPRC:
                  switch( mt2 ) {
                     case _ftSubHILOLAST: expSz = FUTRMisc.HILOSZ;   break;
                     case _ftSubOPENINT:  expSz = FUTRMisc.OPNINTSZ; break;
                     case _ftSubSETLPRC:
                     default:             expSz = FUTRMisc.SETLSZ;   break;
                  }
                  if ( nLeft >= expSz )
                     return _ftMsc.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ftSubQUOTE:
                  expSz = FUTRQuote.QTESZ;
                  if ( nLeft >= expSz )
                     return _ftQte.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _ftSubEXCQTE:
                  expSz = FUTRQuote.EXCQTESZ;
                  if ( nLeft >= expSz )
                     return _ftQte.Set( b, off, nLeft ).SetName( expSz );
                  break;
               default:
                  bQM = true;
                  break;
            }
            break;
         case _mtBOND:
            switch( mt2 ) {
               case _boSubQUOTE:
               case _boSubQTEYLD:
                  expSz = BONDQuote.MINSZ;
                  if ( nLeft >= expSz )
                     return _boQte.Set( b, off, nLeft ).SetName( expSz );
                  break;
               case _boSubTRADE:
               case _boSubTRDCXL:
               case _boSubTRDCORR:
                  expSz = BONDTrade.MINSZ;
                  if ( nLeft >= expSz )
                     return _boTrd.Set( b, off, nLeft ).SetName( expSz );
                  break;
            }
            break;
         case _mtBLOB:
            if ( nLeft >= QuoddMsg.MINSZ ) {
               bSz = BigEndian.GetInt16( b, off );
               if ( nLeft < bSz )
                  break; // _mtBLOB
               switch( mt2 ) {
                  case _blSubFIELDLIST:
                  case _blSubEXCHANGES:
                  case _blSubOPTIONS:
                  case _blSubIDXLIST:
                  case _blSubMMIDS:
                     _bL.Set( b, off, nLeft );
                     return _bL.Sort();
                  case _blSubSORT:
                     return _bL.Set( b, off, nLeft );
                  case _blSubEXCHLIST:
                  case _blSubSNAPSHOT:
                  case _blSubFUNDS:
                  case _blSubINDICES:
                  case _blSubFUT_CHAIN:
                  case _blSubFUTOPT_CHAIN:
                  case _blSubFUT_SNAP:
                  case _blSubFUTURES:
                  case _blSubBONDS:
                  case _blSubCONTRIB:
                  case _blSubFUNDY:
                  case _blSubMKT_CAT:
                  case _blSubDUMPDB:
                  case _blSubUNDERLYERS:
                     return _bA.Set( b, off, nLeft );
                  default:
                     return _qm.Set( b, off, nLeft );
               }
            }
            break;
         case _mtDEAD:
            expSz = Status.MINSZ;
            if ( nLeft >= expSz )
               return _sts.Set( b, off, nLeft );
            break;
         case _mtUC_STATUS:
            expSz = UCStatus.MINSZ;
            if ( nLeft >= expSz )
               return _stsUC.Set( b, off, nLeft );
            break;
         case _mtSESSION:
            expSz = Status.MINSZ;
            if ( nLeft >= expSz )
               return _sts.Set( b, off, nLeft );
            break;
         default:
            return _qm.Set( b, off, nLeft );
      }

      // QuoddMsg??

      expSz = QuoddMsg.MINSZ;
      if ( bQM && ( nLeft >= expSz ) )
         return _qm.Set( b, off, nLeft ).SetName( expSz );
      return null;
   }

   public void FixLevel2Name( QuoddMsg qm, String newMmid )
   {
      String[] kv;
      String   mmid, tkr;
      int      i, nk;

      // Pre-condition

      if ( !IsFixLvl2Name() )
         return;

      // Are we Level2?

      kv = qm.tkr().split( "/" );
      nk = kv.length;
      switch( nk ) {
         case 2:
         case 3:  mmid = kv[nk-1]; break;
         default: mmid = "";       break;
      }
      if ( mmid.length() != 4 )
         return;
      if ( mmid.equals( newMmid ) )
         return;

      // New name 

      for ( i=0,tkr=""; i<nk-1; tkr += kv[i++]  + "/" );
      tkr += newMmid;
      qm.SetTkr( tkr );
   }
}
