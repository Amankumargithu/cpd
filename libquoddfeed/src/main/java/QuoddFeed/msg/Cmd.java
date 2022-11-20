/******************************************************************************
*
*  Cmd.java
*     QuoddFeed Commands
*
*  REVISION HISTORY:
*     24 OCT 2011 jcs  Created.
*     17 NOV 2011 jcs  Build   5: EXCHANGES
*      2 DEC 2011 jcs  Build   7: _IDX_LIST, _INDICES, _FUNDS
*     14 DEC 2011 jcs  Build   8: fmtGetExchBBO()
*      2 JAN 2012 jcs  Build   9: _MMIDS; _FUTURES
*     12 JAN 2012 jcs  Build  10: fmtGetMktMkrs( bMktCtrs )
*     18 JAN 2012 jcs  Build  12: v0.11: fmtUnsubscribe( id )
*      1 FEB 2012 jcs  Build  13: v0.12: ANSI
*     16 MAY 2012 jcs  Build  20: v0.19: _CHANNEL
*      7 JUN 2012 jcs  Build  23: v0.20: _FUNDY
*     19 JUN 2012 jcs  Build  24: v0.20: _FUT_CHAIN
*     10 JUL 2012 jcs  Build  28: v0.21: _FUT_SNAP
*     21 SEP 2012 jcs  Build  38: v0.24: _MKT_CAT
*     25 FEB 2013 jcs  Build  54: v0.24: _BONDS
*     16 JAN 2014 jcs  Build  73: v0.24: TSQDB attributes
*     15 MAY 2014 jcs  Build  76: v0.24: _pAttrKids, _pAttrNum
*     21 JUL 2014 jcs  Build  79: v0.24: _xTranslate()
*     11 SEP 2014 jcs  Build  82: v0.24: _FmtCmdAttr() bug fix
*      2 OCT 2014 jcs  Build  83: v0.24: Heartbeat
*     12 FEB 2015 jcs  Build  90: v0.24: _cmdFUTOPT_CHAIN; _pAll = "ULTRACACHE"
*     30 APR 2015 jcs  Build  92: v0.24: _cmdDUMPDB
*     17 JUN 2015 jcs  Build  94: v0.24: _OPT_UNDLYER
*     22 FEB 2016 jcs  Build 100: v0.24: _SORT
*
*  (c) 1994-2016 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.msg;

import QuoddFeed.msg.*;


/////////////////////////////////////////////////////////////////
//
//                  c l a s s   C m d
//
/////////////////////////////////////////////////////////////////
/**
 * This class contains a list of all currently support commands  
 * from the UltraCache.
 */
public class Cmd extends MsgTypes
{
   ///////////////////////////
   // Class-wide Members
   ///////////////////////////
   /*
    * Commands
    */
   static protected final String _FIELD_LIST   = "FIELD_LIST";
   static protected final String _EXCHANGES    = "EXCHANGES";
   static protected final String _EXCH_LIST    = "EXCHANGE_LIST";
   static protected final String _OPT_UNDLYER  = "OPTIONS_UNDERLYERS";
   static protected final String _OPT_CHAIN    = "OPTIONS_CHAIN";
   static protected final String _OPT_SNAP     = "OPTIONS_SNAPSHOT";
   static protected final String _IDX_LIST     = "INDEX_LIST";
   static protected final String _FUNDS        = "FUNDS";
   static protected final String _BONDS        = "BONDS";
   static protected final String _INDICES      = "INDICES";
   static protected final String _FUT_CHAIN    = "FUTURES_CHAIN";
   static protected final String _FUTOPT_CHAIN = "FUTURES_OPTION_CHAIN";
   static protected final String _FUT_SNAP     = "FUTURES_SNAPSHOT";
   static protected final String _FUTURES      = "FUTURES";
   static protected final String _FUTURES_ALL  = "FUTURES_ALL";
   static protected final String _MMIDS        = "MARKET_MAKERS";
   static protected final String _DUMPDB       = "DUMPDB";
   static protected final String _ALL_LVL2     = "ALL_LEVEL2";
   static protected final String _FUNDY        = "FUNDAMENTAL";
   static protected final String _MKT_CAT      = "MARKET_CATEGORY";
   static protected final String _SORT         = "SORT";
   /*
    * Attributes
    */
   static protected final String _pAll        = "ULTRACACHE";
   static protected final String _pYes        = "YES";
   static protected final String _pNo         = "NO";
   static protected final String _pAttrExch   = "Exchange";
   static protected final String _pAttrLvl2   = "Level2Only";
   static protected final String _pAttrAsset  = "AssetClass";
   static protected final String _notUsed     = "notUsed";
   static protected final String _TSQDB       = "TSQDB";
   static protected final String _pStartTime  = "StartTime";
   static protected final String _pEndTime    = "EndTime";
   static protected final String _pMsgType    = "MsgType";
   static protected final String _pAttrKids   = "Children";
   static protected final String _pAttrNum    = "NumResult";
   static protected final String _pAttrHB     = "Heartbeat";
   static protected final String _pAttrChan   = "Channel";
   /*
    * ANSI
    */
   static public String ANSI_CLEAR  = "\033[H\033[m\033[J";
   static public String ANSI_HOME   = "\033[1;1H\033[K";
   static public String ANSI_ROWCOL = "\033[%d;%dH\033[K";

   //////////////////////
   // Formatting
   //////////////////////
   protected String fmtSubscribe( String tkr, int StreamID )
   {
      String opn;

      /*
       * <OPN Name="DELL" TAG="12345"/>
       * ASSUME : Caller has _xTranslate()'ed tkr, which
       *          might be multiple attrs, like Type="xxx";
       *          where the double-quote would choke in  
       *          _xTranslate()
       */
      opn  = "<OPN Name=\"" + tkr + "\"";
      opn += " TAG=\"" + StreamID + "\"";
      opn += "/>\n";
      return opn;
   }

   protected String fmtUnsubscribe( String tkr, int StreamID )
   {
      // <CLS Name="DELL"/>

      return "<CLS Name=\"" + _xTranslate( tkr ) + "\"/>\n";
   }

   protected String fmtMount( String usr, String auth )
   {
      String cmd;

      // <MNT Name="DELL" TAG="12345"/>

      cmd  = "<MNT User=\"" + _xTranslate( usr ) + "\"";
      cmd += " Authenticate=\"" + _xTranslate( auth ) + "\"";
      cmd += "".format( " %s=\"%s\"", _pAttrHB, _pYes );
      cmd += "/>\n";
      return cmd;
   }

   protected String fmtGetFieldList( int StreamID )
   {
      return _FmtCmd( _FIELD_LIST, _notUsed, StreamID );
   }

   protected String fmtGetExchanges( int StreamID )
   {
      // <CMD Type="EXCHANGES"/>

      return _FmtCmd( _EXCHANGES, _notUsed, StreamID );
   }

   protected String fmtGetExch( String exch, int StreamID )
   {
      // <CMD Type="EXCHANGE_LIST" Name="A"/>

      if ( exch.startsWith( "BBO-" ) )
         return fmtGetExchBBO( exch.split("-"), StreamID );
      return _FmtCmd( _EXCH_LIST, exch, StreamID );
   }

   private String fmtGetExchBBO( String[] ex, int StreamID )
   {
      // <CMD Type="EXCHANGE_LIST" Name="BBO" Exchange="ARCX"/>

      return _FmtCmdAttr( _EXCH_LIST, ex[0], StreamID, _pAttrExch, ex[1] );
   }

   protected String fmtGetOptChain( String pUnd, int StreamID )
   {
      // <CMD Type="OPTIONS_CHAIN" Name="DELL"/>

      return _FmtCmd( _OPT_CHAIN, pUnd, StreamID );
   }

   protected String fmtGetOptionChainExch( String pUnd, 
                                           String exch, 
                                           int    StreamID )
   {
      // <CMD Type="OPTIONS_CHAIN" Name="DELL" Exchange="ARCX"/>

      return _FmtCmdAttr( _OPT_CHAIN, pUnd, StreamID, _pAttrExch, exch );
   }

   protected String fmtGetOptionUnderlyer( int StreamID )
   {
      // <CMD Type="OPTIONS_UNDERLYERS" />

      return _FmtCmd( _OPT_UNDLYER, _notUsed, StreamID );
   }

   protected String fmtGetOptionSnap( String   pUnd, 
                                      int      StreamID, 
                                      String[] flds )
   {
      String rtn, attr;
      int    i, nf;

      /*
       * <CMD Type="OPTIONS_SNAPSHOT" Name="DELL"/>
       * <CMD Type="OPTIONS_SNAPSHOT" FieldName="BID,ASK"/>
       */
      attr = "";
      nf   = flds.length;
      for ( i=0; i<nf; i++ ) {
         if ( i>0 )
            attr += ",";
         attr += flds[i];
      }
      if ( nf>0 )
         rtn = _FmtCmdAttr( _OPT_SNAP, pUnd, StreamID, "FieldName", attr );
      else
         rtn = _FmtCmd( _OPT_SNAP, pUnd, StreamID );
      return rtn;
   }

   protected String fmtGetIndexParticipants( String pIdx, int StreamID )
   {
       // <CMD Type="INDEX_LIST" Name="COMP"/>

      return _FmtCmd( _IDX_LIST, pIdx, StreamID );
   }

   protected String fmtGetIndices( int StreamID )
   {
      return _FmtCmd( _INDICES, _notUsed, StreamID );
   }

   protected String fmtGetFunds( int StreamID )
   {
      return _FmtCmd( _FUNDS, _notUsed, StreamID );
   }

   protected String fmtGetBonds( int StreamID )
   {
      return _FmtCmd( _BONDS, _notUsed, StreamID );
   }

   protected String fmtGetFuturesChain( String pUnd, int StreamID )
   {
      // <CMD Type="FUTURES_CHAIN" Name="CL"/>

      return _FmtCmd( _FUT_CHAIN, pUnd, StreamID );
   }

   protected String fmtGetFuturesOptionChain( String pUnd, int StreamID )
   {
      // <CMD Type="FUTURES_OPTION_CHAIN" Name="CL"/>

      return _FmtCmd( _FUTOPT_CHAIN, pUnd, StreamID );
   }

   protected String fmtGetFuturesSnap( String pUnd, int StreamID )
   {
      // <CMD Type="FUTURES_SNAPSHOT" Name="CL"/>

      return _FmtCmd( _FUT_SNAP, pUnd, StreamID );
   }

   protected String fmtGetFutures( int StreamID, boolean bAll )
   {
      String cmd;

      cmd = bAll ? _FUTURES_ALL : _FUTURES;
      return _FmtCmd( cmd, _notUsed, StreamID );
   }

   protected String fmtGetMktMkrs( String pTkr, int StreamID, boolean bMktCtrs )
   {
       // <CMD Type="MARKET_MAKERS" Name="DELL"/>

      if ( bMktCtrs )
         return _FmtCmd( _MMIDS, pTkr, StreamID );
      return _FmtCmdAttr( _MMIDS, pTkr, StreamID, _pAttrLvl2, _pYes );
   }

   protected String fmtGetFundamental( int StreamID, char asset )
   {
      char[] ch = { asset };
      String a  = new String( ch );

       // <CMD Type="FUNDAMENTAL" Asset="EQUITY"/>

      return _FmtCmdAttr( _FUNDY, _notUsed, StreamID, _pAttrAsset, a );
   }

   protected String fmtGetMktCategory( int StreamID, String cat )
   {
      // <CMD Type="MARKET_CATEGORY" Name="N"/>

      return _FmtCmd( _MKT_CAT, cat, StreamID );
   }

   protected String fmtDumpDb( int StreamID, String chan )
   {
      /*
       * Specific : <CMD Type="DUMPDB" Name="OPRA16" />
       * All      : <CMD Type="DUMPDB" Name="" />
       */
      return _FmtCmd( _DUMPDB, chan, StreamID );
   }


   //////////////////////
   // Formatting
   //////////////////////
   private String _FmtCmd( String cmd, String pn, int StreamID )
   {
      String opn, err;

      try {
         opn  = "<CMD Type=\"" + cmd + "\" ";
         opn += "Name=\"" + _xTranslate( pn ) + "\" ";
         opn += "TAG=\"" + StreamID + "\"/>\n";
      } catch( Exception e ) {
         opn = e.getMessage();
      }
      return opn;
   }

   private String _FmtCmdAttr( String cmd,
                               String pn,
                               int    StreamID,
                               String aKey,
                               String aVal )
   {
      String opn, err;

      try {
         opn  = "<CMD Type=\"" + cmd + "\" ";
         opn += "Name=\"" + _xTranslate( pn ) + "\" ";
         opn += "TAG=\"" + StreamID + "\" ";
         opn += _xTranslate( aKey ) + "=\"" + _xTranslate( aVal ) + "\" ";
         opn += "/>\n";
      } catch( Exception e ) {
         opn = e.getMessage();
      }
      return opn;
   }

   protected String _xTranslate( String cmd )
   {
      String xt;
      char[] ch = cmd.toCharArray();
      int    i, sz;

      xt = "";
      sz = ch.length;
      for ( i=0; i<sz; i++ ) {
         switch( ch[i] ) {
            case '>':  xt += "&gt;";   break;
            case '<':  xt += "&lt;";   break;
            case '\"': xt += "&quot;"; break;
            case '&':  xt += "&amp;";  break;
            case '\'': xt += "&apos;"; break;
            default:   xt += xt.format( "%c", ch[i] );
         }
      }
      return xt;
   }
}
