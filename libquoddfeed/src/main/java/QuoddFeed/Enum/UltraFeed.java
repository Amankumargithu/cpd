/******************************************************************************
*
*  UltraFeed.java
*     Common UltraFeed channel information
*
*  REVISION HISTORY:
*     20 JUL 2012 jcs  Created.
*     22 AUG 2012 jcs  Build 34: _IsSet()
*     25 OCT 2012 jcs  Build 44: [%d] %s for flags / bitmasks
*      4 JAN 2013 jcs  Build 50: ICE
*     31 JAN 2013 jcs  Build 51: Protocol_OUTDATED_20120131
*     30 APR 2013 jcs  Build 58: IsQuodd()
*     30 MAY 2013 jcs  Build 61: _bBasic
*     17 JUN 2013 jcs  Build 64: IsNazzLevel2()
*     28 JUN 2013 jcs  Build 65: IsPink()
*     12 NOV 2014 jcs  Build 86: Proto 34
*     28 JUL 2015 jcs  Build 95: Updated Protocol() from v3.0 spec
*
*  (c) 2011-2015 Quodd Financial
*******************************************************************************/
package QuoddFeed.Enum;


/////////////////////////////////////////////////////////////////
//
//               c l a s s    U l t r a F e e d
//
/////////////////////////////////////////////////////////////////
/**
 * The UltraFeed class encapsulates the UltraFeed channels.
 */
public class UltraFeed
{
   /////////////////////////
   // Globals
   /////////////////////////
   static public boolean _bBasic = false;

   /*
    * Protocol Table - From Richard
    */
   static public boolean IsPink( QuoddFeed.msg.QuoddMsg qm )
   {
      int pro;

      switch( (pro=qm.protocol()) ) {
         case  13: // OTC Quotes
         case  14: // OTC Insides
            return true;
      }
      return false;
   }

   static public boolean IsNazzLevel2( QuoddFeed.msg.QuoddMsg qm )
   {
      int pro;

      switch( (pro=qm.protocol()) ) {
         case  22: // NASDAQ-LEVEL2
            return true;
      }
      return false;
   }

   static public String Protocol( QuoddFeed.msg.QuoddMsg qm )
   {
      String s;
      int    pro;

      s = "Undefined";
      switch( (pro=qm.protocol()) ) {
         case   1: s = "CQS  Network E"; break;
         case   2: s = "CQS  Network F"; break;
         case   3: s = "CTS  Network A"; break;
         case   4: s = "CTS  Network B"; break;
         case   5: s = "CTS  Indices"; break;
         case   6: s = "UQDF"; break;
         case   7: s = "UTDF"; break;
         case   8: s = "OMDF"; break;
         case   9: s = "BBDS"; break;
         case  10: s = "TDDS"; break;
         case  13: s = "OTC Markets  Quotes"; break;
         case  14: s = "OTC Markets  Insides"; break;
         case  16: s = "NASDAQ OMX Last Sale (NLS)"; break;
         case  17: s = "NASDAQ OMX BX Last Sale (BLS)"; break;
         case  18: s = "NASDAQ OMX PSX Last Sale (PLS)"; break;
         case  19: s = "NASDAQ OMX BBO (QBBO)"; break;
         case  20: s = "NASDAQ OMX BX BBO (BBBO)"; break;
         case  21: s = "NASDAQ OMX PSX BBO (PBBO)"; break;
         case  22: s = "NASDAQ OMX Level 2"; break;
         case  23: s = "TSX CDF (Consolidated Data Feed)"; break;
         case  24: s = "TSX Level 1 Quantum Feed (TL1Q)"; break;
         case  25: s = "TSX Venture Level 1 Quantum Feed (CL1Q)"; break;
         case  26: s = "TSX Index Quantum Feed (TX1)"; break;
         case  27: s = "TSX Reference Data Quantum Feed (TRD)"; break;
         case  28: s = "TSX Venture Reference Data Quantum Feed (VRD)"; break;
         case  29: s = "Alpha Level 1 Quantum Feed (AL1)"; break;
         case  30: s = "TSX Level 2 Quantum Feed (TL2Q)"; break;
         case  31: s = "TSX Venture Level 2 Quantum Feed (CL2Q)"; break;
         case  32: s = "TMX Select Level 2 Quantum Feed (SL2Q)"; break;
         case  33: s = "Alpha Level 2 Quantum Feed (AL2Q)"; break;
         case  34: s = "TDDS 2.0"; break;
         case  35: s = "Omega ATS Itch 3.0 Feed"; break;
         case  36: s = "Chi-X Canada CHIXMMD Feed"; break;
         case  37: s = "CX2 Canada CHIXMMD Feed"; break;
         case  38: s = "NYSE RussellTick"; break;
         case  39: s = "NASDAQ OMX GIDS 2.0"; break;
         case  41: s = "NYSE Global Index Feed (GIF)"; break;
         case  42: s = "CBOE Market Data Indices (MDI)"; break;
         case  46: s = "S&P Indices (CME Streamlined FAST-FIX)"; break;
         case  48: s = "Dow Jones Indices (CME Streamlined FAST-FIX)"; break;
         case  51: s = "OPRA v3 (Binary)"; break;
         case  60: s = "CANDEAL (CDL)"; break;
         case  61: s = "ArcaTrade for Bonds"; break;
         case  62: s = "ArcaBond Quotes"; break;
         case  64: s = "E-Speed Book"; break;
         case  70: s = "Mutual Fund Dissemination Service (MFDS)"; break;
         case  71: s = "NASDAQ OMX Last Sale (NLS) 2.0"; break;
         case  72: s = "NASDAQ OMX BX Last Sale (BLS) 2.0"; break;
         case  73: s = "NASDAQ OMX PSX Last Sale (PLS) 2.0"; break;
         case  74: s = "NASDAQ OMX BBO (QBBO) 2.0"; break;
         case  75: s = "NASDAQ OMX BX BBO (BBBO) 2.0"; break;
         case  76: s = "NASDAQ OMX PSX BBO (PBBO) 2.0"; break;
         case  77: s = "NASDAQ OMX Level 2 2.0"; break;
         case  80: s = "CME  ITC (Floor)"; break;
         case  81: s = "CME  FAST-FIX (Electronic)  Pending Removal"; break;
         case  82: s = "CME  FAST-FIX (Electronic)  Pending Removal"; break;
         case  83: s = "CME  FAST-FIX (Electronic)  Pending Removal"; break;
         case  84: s = "NYMEX  ITC (Floor)"; break;
         case  85: s = "NYMEX  FAST-FIX (Electronic)  Pending Removal"; break;
         case  86: s = "NYMEX  FAST-FIX (Electronic)  Pending Removal"; break;
         case  87: s = "NYMEX  FAST-FIX (Electronic)  Pending Removal"; break;
         case  88: s = "COMEX  ITC (Floor)"; break;
         case  89: s = "COMEX  FAST-FIX (Electronic)  Pending Removal"; break;
         case  90: s = "COMEX  FAST-FIX (Electronic)  Pending Removal"; break;
         case  91: s = "COMEX  FAST-FIX (Electronic)  Pending Removal"; break;
         case  92: s = "CBOT  ITC (Floor)"; break;
         case  93: s = "CBOT  FAST-FIX (Electronic)  Pending Removal"; break;
         case  94: s = "CBOT  FAST-FIX (Electronic)  Pending Removal"; break;
         case  95: s = "CBOT  FAST-FIX (Electronic)  Pending Removal"; break;
         case 130: s = "CME-MDP3 (Electronic)"; break;
         case 131: s = "CBOT-MDP3 (Electronic)"; break;
         case 132: s = "NYMEX-MDP3 (Electronic)"; break;
         case 133: s = "COMEX-MDP3 (Electronic)"; break;
         case 150: s = "ArcaBook  Listed"; break;
         case 151: s = "ArcaBook  OTC"; break;
         case 152: s = "ArcaBook  ETF"; break;
         case 153: s = "NYSE OpenBook Ultra"; break;
         case 154: s = "NYSE MKT OpenBook Ultra"; break;
         case 155: s = "EdgeBook Depth  EDGX"; break;
         case 156: s = "EdgeBook Depth  EDGA"; break;
         case 157: s = "BATS PITCH"; break;
         case 158: s = "BATS-Y PITCH"; break;
         case 159: s = "NASDAQ OMX TotalView ITCH"; break;
         case 160: s = "NASDAQ OMX BX TotalView ITCH"; break;
         case 161: s = "NASDAQ OMX PSX TotalView ITCH"; break;
         case 162: s = "NYSE Imbalance"; break;
         case 163: s = "NYSE MKT Imbalance"; break;
         case 165: s = "NYSE Alerts"; break;
         case 166: s = "NASDAQ TotalView ITCH 5.0"; break;
         case 167: s = "NASDAQ BX TotalView ITCH 5.0"; break;
         case 168: s = "NASDAQ PSX TotalView ITCH 5.0"; break;
         case 175: s = "CSE L2"; break;
         case 176: s = "Canadian Best Bid and Offer"; break;
         case 177: s = "Canadian Best Bid and Offer"; break;
         case 178: s = "TMX Consolidated Last Sale"; break;
         case 179: s = "TMX Consolidated Last Sale"; break;
         case 255: s = "QUODD"; break;
      }
      s = s.format( "[%02d] %s", pro, s );
      return s;
   }

   /*
    * Channel Index Table - From Richard
    */
   static public String ChanIdx( QuoddFeed.msg.QuoddMsg qm )
   {
      String  s;
      int     idx;
      boolean bQ;

      s  = "Undefined";
      bQ = _IsQuodd( qm );
      switch( (idx=qm.chanIdx()) ) {
         case   1: s = bQ ? "ICE (QUODD)" : ""; break;
         case   3: s = "CME Open Outcry (ITC 2.1)"; break;
         case   6: s = "Volatility Quoted Options"; break;
         case   7: s = "CME Globex Equity Futures"; break;
         case   8: s = "CME Globex Equity Options"; break;
         case   9: s = "CME Globex Interest Rate Futures"; break;
         case  10: s = "CME Globex Interest Rate Options"; break;
         case  11: s = "CME Globex FX Futures"; break;
         case  12: s = "CME Globex FX Options"; break;
         case  13: s = "CME Globex Commodity, Industrial Commodity and TRAKRS Futures"; break;
         case  14: s = "CME Globex Commodity, Industrial Commodity and TRAKRS Options"; break;
         case  15: s = "CME Equity Futures - excludes E-mini S&P 500"; break;
         case  16: s = "CME Equity Options - excludes E-mini S&P 500"; break;
         case  18: s = "Green Exchange Futures"; break;
         case  19: s = "Green Exchange Options"; break;
         case  21: s = "KRX Futures"; break;
         case  22: s = "BMD Futures"; break;
         case  23: s = "BMD Options"; break;
         case  24: s = "BM&F Futures"; break;
         case  25: s = "BM&F Options"; break;
         case  26: s = "MexDer Futures"; break;
         case  27: s = "MexDer Options"; break;
         case  28: s = "DME Futures"; break;
         case  29: s = "DME Options"; break;
         case  30: s = "NYMEX Crude Futures"; break;
         case  31: s = "NYMEX Non-Crude Energy Futures"; break;
         case  32: s = "NYMEX Metals, Softs, and Alternative Markets & COMEX Futures"; break;
         case  33: s = "COMEX Futures"; break;
         case  35: s = "NYMEX Crude Options"; break;
         case  36: s = "NYMEX Non-Crude Energy Options"; break;
         case  37: s = "NYMEX Metals, Softs, and Alternative Markets & COMEX Options"; break;
         case  38: s = "COMEX Options"; break;
         case  60: s = "CME FX Futures II"; break;
         case  61: s = "CME FX Options II"; break;
         case 100: s = "CBOT Open Auction (ITC 2.1)"; break;
         case 104: s = "MGEX Open Auction (ITC 2.1)"; break;
         case 107: s = "KCBT Open Auction (ITC 2.1)"; break;
         case 110: s = "Dow Jones Indexes (ITC 2.1)"; break;
         case 111: s = "CBOT Globex Commodity Futures"; break;
         case 112: s = "CBOT Globex Commodity Options"; break;
         case 113: s = "CBOT Globex Equity Index Futures"; break;
         case 114: s = "CBOT Globex Equity Index Options"; break;
         case 115: s = "CBOT Globex Interest Rate Futures"; break;
         case 116: s = "CBOT Globex Interest Rate Options"; break;
         case 118: s = "MGEX Globex Futures"; break;
         case 119: s = "MGEX Globex Options"; break;
         case 120: s = "KCBT Globex Futures"; break;
         case 121: s = "KCBT Globex Options"; break;
         case 150: s = "S&P Indexes"; break;
         case 190: s = "Eris Exchange"; break;
         case 220: s = "Indxis"; break;
         case 229: s = "DME Non-Globex floor cPRS"; break;
         case 244: s = "NYMEX floor cPRS"; break;
         case 245: s = "COMEX floor cPRS"; break;
         case 801: s = "Inter Exchange Spreads"; break;
      }
      s = s.format( "[%02d] %s", idx, s );
      return s;
   }


   ////////////////////////
   // Helpers
   ////////////////////////
   static public boolean _IsSet( int msk, int bit )
   {
      return( ( msk & bit ) == bit );
   }

   static public boolean _IsQuodd( QuoddFeed.msg.QuoddMsg qm )
   {
      int pro;

      return( (pro=qm.protocol()) == 255 );
   }
}
