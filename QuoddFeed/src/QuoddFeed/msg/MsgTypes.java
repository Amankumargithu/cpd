/******************************************************************************
*
*  MsgTypes.java
*     UltraCache wire protocol to CPD
*
*  REVISION HISTORY:
*      9 NOV 2011 jcs  Created (from QuoddMsg.java).
*     14 NOV 2011 jcs  Build   4: _opSubXxx
*     17 NOV 2011 jcs  Build   5: _blSubEXCHANGES
*     29 NOV 2011 jcs  Build   7: _mtFUND / mt_INDEX
*      7 DEC 2011 jcs  Build   8: _mtDEAD
*      2 JAN 2012 jcs  Build   9: _blSubMMIDS; _eqSubxxxMM; _mtFUTURE
*     30 JAN 2012 jcs  Build  13: _mtSESSION; _mtUC_STATUS
*     22 FEB 2012 jcs  Build  14: _blSubCONTRIB; _mtBOND
*     10 APR 2012 jcs  Build  16: _ixSubETPINTRA
*      3 MAY 2012 jcs  Build  18: _eqSubMKTSTATS
*     15 MAY 2012 jcs  Build  19: All FUND sub-msgs
*     16 MAY 2012 jcs  Build  20: _mtSTREAM_OPN
*     29 MAY 2012 jcs  Build  21: _eqSubSUMMARY / _eqSubMKTCTRSUMM
*      7 JUN 2012 jcs  Build  23: _eqSubTRDCXL / _eqSubTRDCORR
*     19 JUN 2012 jcs  Build  24: _blSubFUT_CHAIN
*     10 JUL 2012 jcs  Build  28: _blSubFUT_SNAP
*     17 JUL 2012 jcs  Build  29: _eqSubTRDACTION / _eqSubREGSHO
*     21 SEP 2012 jcs  Build  38: _blSubMKT_CAT
*     13 NOV 2012 jcs  Build  44: _ixSubNAV, etc.
*     31 JAN 2013 jcs  Build  51: All public
*     25 FEB 2013 jcs  Build  54: _blSubBONDS / _eqSubLIMITUPDN
*     14 MAY 2013 jcs  Build  59: _mtSubTRACER
*     26 FEB 2014 jcs  Build  75: _ixSubSETLVAL / _ixSubSETLSUMM
*      2 OCT 2014 jcs  Build  83: _mtPING
*     12 FEB 2015 jcs  Build  90: v0.24: _blSubFUTOPT_CHAIN
*     20 APR 2015 jcs  Build  92: v0.24: _blSubDUMPDB
*      7 JUL 2015 jcs  Build  94: v0.24: _blSubUNDERLYERS; _opSubTRADE_ID
*     11 NOV 2015 jcs  Build  98: v0.24: _opSubTRADE_ID appendage
*     22 FEB 2016 jcs  Build 100: v0.24: _blSubSORT
*      1 DEC 2016 jcs  Build 101: v0.25: _eqSubTRDASOF
*      9 AUG 2017 jcs  Build 103: v0.25: _eqSubTRDASOFCORR
*
*  (c) 1994-2017 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.msg;


/////////////////////////////////////////////////////////////////
//
//                c l a s s   M s g T y p e s
//
/////////////////////////////////////////////////////////////////
/**
 * The MsgTypes class defines the QuoddFeed message types and sub-types.
 */
public class MsgTypes
{
   ///////////////////////////
   // Message Types
   ///////////////////////////
   static public final char _mtPING       = 'P';
   static public final char _mtSESSION    = 'S';
   static public final char _mtEQUITY     = 'e';
   static public final char _mtOPTION     = 'o';
   static public final char _mtFUND       = 'F';
   static public final char _mtINDEX      = 'I';
   static public final char _mtFUTURE     = 'T';
   static public final char _mtBOND       = 'b';
   static public final char _mtBLOB       = 'B';
   static public final char _mtDEAD       = 'x';
   static public final char _mtUC_STATUS  = 'U';
   static public final char _mtSTREAM_OPN = '+';

   ///////////////////////////
   // Message Sub-Types
   ///////////////////////////
   /*
    * Any
    */
   static public final char _mtSubIMG         = 'X';
   static public final char _mtSubTRACER      = 0x7e;
   static public final char _sessSubCHALLENGE = 'C';
   static public final char _sessSubSUCCESS   = '^';
   static public final char _sessSubFAILURE   = 'v';
   /*
    * _mtEQUITY
    */
   static public final char _eqSubQTESHORT    = 0x27;
   static public final char _eqSubQTELONG     = 0x28;
   static public final char _eqSubBBOSHORT    = 0x29;
   static public final char _eqSubBBOLONG     = 0x2a;
   static public final char _eqSubQTESHORTMM  = 0x2b;
   static public final char _eqSubQTELONGMM   = 0x2c;
   static public final char _eqSubBBOSHORTMM  = 0x2d;
   static public final char _eqSubBBOLONGMM   = 0x2e;
   static public final char _eqSubTRDSHORT    = 0x2f;
   static public final char _eqSubTRDLONG     = 0x30;
   static public final char _eqSubTRDCXL      = 0x31;
   static public final char _eqSubTRDCORR     = 0x32;
   static public final char _eqSubTRDASOF     = 0x33;
   static public final char _eqSubTRDASOFCXL  = 0x34;
   static public final char _eqSubTRDASOFCORR = 0x35;
   static public final char _eqSubTRDACTION   = 0x36;
   static public final char _eqSubREGSHO      = 0x38;
   static public final char _eqSubSUMMARY     = 0x39;
   static public final char _eqSubMKTCTRSUMM  = 0x3b;
   static public final char _eqSubLIMITUPDN   = 0x3d;
   static public final char _eqSubMKTSTATS    = 0x7f;
   /*
    * _mtOPTION
    */
   static public final char _opSubOPENINT     = 0x25;
   static public final char _opSubQTESHORT    = 0x26;
   static public final char _opSubQTELONG     = 0x27;
   static public final char _opSubBBOSHORT    = 0x28;
   static public final char _opSubBBOLONG     = 0x29;
   static public final char _opSubTRADE       = 0x2a;
   static public final char _opSubTRDCXL      = 0x2b;
   static public final char _opSubTRDSUMM     = 0x2c;
   static public final char _opSubTRADE_ID    = 0x3a;
   static public final char _opSubTRDCXL_ID   = 0x3b;
   /*
    * _mtFUND
    */
   static public final char _fundSubFUNDNAV   = 0x25;
   static public final char _fundSubMMNAV     = 0x26;
   static public final char _fundSubCAPDISTRO = 0x27;
   static public final char _fundSubDIVID_INT = 0x28;
   /*
    * _mtINDEX
    */
   static public final char _ixSubVALUE       = 0x31;
   static public final char _ixSubSUMM        = 0x33;
   static public final char _ixSubSETLVAL     = 0x40;
   static public final char _ixSubSETLSUMM    = 0x41;
   static public final char _ixSubETPINTRA    = 0x50;
   static public final char _ixSubEST_CASH    = 0x52;
   static public final char _ixSubTOT_CASH    = 0x53;
   static public final char _ixSubNAV         = 0x54;
   static public final char _ixSubSHARES      = 0x55;
   static public final char _ixSubETPDIV      = 0x56;
   /*
    * _mtBOND
    */
   static public final char _boSubQUOTE       = 0x26;
   static public final char _boSubQTEYLD      = 0x27;
   static public final char _boSubTRADE       = 0x28;
   static public final char _boSubTRDCXL      = 0x29;
   static public final char _boSubTRDCORR     = 0x2a;
   /*
    * _mtFUTURE
    */
   static public final char _ftSubCOMBOQTE    = 0x30; // 0
   static public final char _ftSubTRADE       = 0x31; // 1
   static public final char _ftSubTRDCXL      = 0x32; // 2
   static public final char _ftSubTRDCORR     = 0x33; // 3
   static public final char _ftSubQUOTE       = 0x34; // 4
   static public final char _ftSubEXCQTE      = 0x35; // 5
   static public final char _ftSubMKTCOND     = 0x36; // 6
   static public final char _ftSubRFQ         = 0x37; // 7
   static public final char _ftSubHILOLAST    = 0x38; // 8
   static public final char _ftSubOPENINT     = 0x39; // 9
   static public final char _ftSubSUMMARY     = 0x3a; // :
   static public final char _ftSubSETLPRC     = 0x3b; // ;
   static public final char _ftSubRANGEQTE    = 0x3c; // <
   static public final char _ftSubCUMVOL      = 0x3d; // =
   static public final char _ftSubCASHPRC     = 0x4d; // M
   /*
    * _mtBLOB
    */
   static public final char _blSubFIELDLIST    = 'F';
   static public final char _blSubEXCHLIST     = 'X';
   static public final char _blSubEXCHANGES    = 'E';
   static public final char _blSubUNDERLYERS   = 'U';
   static public final char _blSubOPTIONS      = 'O';
   static public final char _blSubSNAPSHOT     = 'S';
   static public final char _blSubIDXLIST      = 'I';
   static public final char _blSubFUNDS        = 'f';
   static public final char _blSubINDICES      = 'i';
   static public final char _blSubFUT_CHAIN    = 'c';
   static public final char _blSubFUTOPT_CHAIN = 'o';
   static public final char _blSubFUT_SNAP     = 's';
   static public final char _blSubFUTURES      = 't';
   static public final char _blSubBONDS        = 'b';
   static public final char _blSubSORT         = 'T';
   static public final char _blSubMMIDS        = 'm';
   static public final char _blSubCONTRIB      = 'C';
   static public final char _blSubFUNDY        = 'Y';
   static public final char _blSubMKT_CAT      = 'M';
   static public final char _blSubDUMPDB       = 'D';
}
