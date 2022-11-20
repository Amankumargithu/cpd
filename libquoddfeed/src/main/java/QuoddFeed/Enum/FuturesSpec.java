/******************************************************************************
*
*  FuturesSpec.java
*     UltraFeed Futures Appendices
*
*  REVISION HISTORY:
*      6 JUN 2012 jcs  Created.
*     20 JUL 2012 jcs  Build 29: QuoddFeed.Enum.UltraFeed
*     22 AUG 2012 jcs  Build 34: UltraFeed._IsSet()
*     15 OCT 2012 jcs  Build 43: QuoteFlags / TradeFlags
*     25 OCT 2012 jcs  Build 44: [%d] %s for flags / bitmasks
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.Enum;

import java.util.*;

/////////////////////////////////////////////////////////////////
//
//               c l a s s    F u t u r e s S p e c
//
/////////////////////////////////////////////////////////////////
/**
 * The FuturesSpec class encapsulates the UltraFeed Equity appendices.
 */
public class FuturesSpec extends QuoddFeed.Enum.UltraFeed
{
   /*
    * Appx A - Quote Flags
    */
   static private final int unpricedBid = 0x0001;
   static private final int unpricedAsk = 0x0002;

   static public String QuoteFlags( QuoddFeed.msg.FUTRQuote qte )
   {
      String            s;
      ArrayList<String> v;
      int               i, n, qFlg;

      // Bitmask

      v    = new ArrayList<String>();
      qFlg = qte._qteFlag;
      if ( _IsSet( qFlg, unpricedBid ) )
         v.add( new String( "unpricedBid" ) );
      if ( _IsSet( qFlg, unpricedAsk ) )
         v.add( new String( "unpricedAsk" ) );
      n = v.size();
      s = "Undefined";
      s = s.format( "[0x%02x] ", qFlg );
      for ( i=0; i<n; i++ ) {
         s += ( i>0 ) ? "," : "";
         s += v.get( i );
      }
      return s;
   }


   /*
    * Appx B - Trade Flags
    */
   static private final int tradeOutOfSequence         = 0x0001;
   static private final int tradeNoVolume              = 0x0002;
   static private final int tradeEstimatedVolume       = 0x0004;
   static private final int tradeVolumeNotUpdated      = 0x0008;
   static private final int tradeBeginnigOfGlobexEvent = 0x0010;
   static private final int tradeEndOfGlobexEvent      = 0x0020;
   static private final int midEvent                   = 0x0040;
   static private final int openingTrade               = 0x0080;
   static private final int priceCalculatedByGlobex    = 0x0100;

   static public String TradeFlags( QuoddFeed.msg.FUTRTrade trd )
   {
      String            s;
      ArrayList<String> v;
      int               i, n, tFlg;

      // Bitmask

      v    = new ArrayList<String>();
      tFlg = trd._trdFlags;
      if ( _IsSet( tFlg, tradeOutOfSequence ) )
         v.add( new String( "tradeOutOfSequence" ) );
      if ( _IsSet( tFlg, tradeNoVolume ) )
         v.add( new String( "tradeNoVolume" ) );
      if ( _IsSet( tFlg, tradeEstimatedVolume ) )
         v.add( new String( "tradeEstimatedVolume" ) );
      if ( _IsSet( tFlg, tradeVolumeNotUpdated ) )
         v.add( new String( "tradeVolumeNotUpdated" ) );
      if ( _IsSet( tFlg, tradeBeginnigOfGlobexEvent ) )
         v.add( new String( "tradeBeginnigOfGlobexEvent" ) );
      if ( _IsSet( tFlg, tradeEndOfGlobexEvent ) )
         v.add( new String( "tradeEndOfGlobexEvent" ) );
      if ( _IsSet( tFlg, midEvent ) )
         v.add( new String( "midEvent" ) );
      if ( _IsSet( tFlg, openingTrade ) )
         v.add( new String( "openingTrade" ) );
      if ( _IsSet( tFlg, priceCalculatedByGlobex ) )
         v.add( new String( "priceCalculatedByGlobex" ) );
      n = v.size();
      s = "Undefined";
      s = s.format( "[0x%04x] ", tFlg );
      for ( i=0; i<n; i++ ) {
         s += ( i>0 ) ? "," : "";
         s += v.get( i );
      }
      return s;
   }


   /*
    * Appx C - Indicators
    */
   static public String ExceptionalIndicator( QuoddFeed.msg.FUTRQuote qte )
   {
      switch( qte._excInd ) {
         case 'A': return "Asset Allocation";
         case 'B': return "Wholesale (Block) Trading";
         case 'E': return "Exchange for Physical";
         case 'F': return "Average price for five minute session";
         case 'G': return "Against Actual";
         case 'H': return "Match/Cross Trade";
         case 'O': return "Average price for one minute session";
         case 'P': return "Exchange for Physical";
         case 'R': return "Exchange for Risk";
         case 'S': return "Basis";
         case 'U': return "Exchange for Option";
         case 'W': return "Exchange for Swaps";
      }
      return "";
   }

   static public String MarketCondition( char ind )
   {
      switch( ind ) {
         case ' ':  return "Unspecified";
         case 'A':  return "Halt Trading";
         case 'B':  return "Resume Trading";
         case 'C':  return "No Cancel";
         case 'E':  return "End Fast Market";
         case 'F':  return "Start Fast Market";
         case 'I':  return "Price Indication";
         case 'L':  return "Start Late Market (Time may not be exact)";
         case 'M':  return "End Late Market";
         case 'N':  return "Not Available";
         case 'O':  return "Pre-Open";
         case 'P':  return "Start Post Suspension/Close/Settle Session";
         case 'Q':  return "End Post Suspension/Close/Settle Session";
         case 'R':  return "Pre-cross";
         case 'U':  return "Unknown";
         case 'X':  return "Cross";
      }
      return "";
   }

   static public String MarketDirection( char ind )
   {
      switch( ind ) {
         case '+': return "Up";
         case '-': return "Down";
         case 'S': return "Stable";
         case ' ': return "Market direction not indicated by Exchange";
      }
      return "";
   }

   static public String OpenInterest( char ind )
   {
      switch( ind ) {
         case ' ': return "Open Interest Field Not Updated";
         case 'A': return "Actual";
         case 'E': return "Estimated";
      }
      return "";
   }

   static public String SaleCondition( char ind )
   {
      switch( ind ) {
         case ' ': return "Normal";
         case '?': return "Indeterminate";
         case 'B': return "Blank out the associated price";
         case 'C': return "Cabinet";
         case 'D': return "Differential";
         case 'E': return "Exchange for Physical";
         case 'F': return "Fast";
         case 'G': return "Exchange for Physical / Cross Trade";
         case 'H': return "Hit";
         case 'I': return "Implied";
         case 'J': return "Large Order";
         case 'K': return "Small Order";
         case 'L': return "Late (Time may not be exact)";
         case 'M': return "Match/Cross Trade";
         case 'N': return "Nominal / Notional";
         case 'O': return "Option Exercise";
         case 'P': return "Percentage";
         case 'Q': return "Auto Quotes";
         case 'R': return "Indicative";
         case 'S': return "Exchange for Swaps";
         case 'T': return "Take";
         case 'U': return "Exchange for Options";
         case 'V': return "Nominal Cabinet";
         case 'X': return "Changing Transaction";
      }
      return "";
   }

   static public String ProductClassification( char ind )
   {   
      switch( ind ) { 
         case ' ': return "Unspecified";
         case 'D': return "Delta Options";
         case 'F': return "Flexible Options";
         case 'I': return "Index Values";
         case 'S': return "Short-dated Options";
         case 'V': return "Volatility Options";
      }
      return "";
   }

   static public String RangeIndicator( char ind )
   {
      switch( ind ) {
         case 'B': return "Indicative Bid/Ask with Delta";
         case 'C': return "Close";
         case 'D': return "Day Open";
         case 'I': return "Indicative Open";
         case 'O': return "Open";
         case 'P': return "Post Close / Suspension";
         case 'R': return "Resumption of Trading";
         case 'S': return "Suspension of Trading";
      }
      return "";
   }

   static public String RequestIndicator( char ind )
   {
      switch( ind ) {
         case 'D': return "End Request for Quote";
         case 'R': return "Start Request for Quote";
      }
      return "";
   }

   static public String TypeCode( char ind )
   {
      switch( ind ) {
         case ' ': return "General Text";
         case 'A': return "Trading Authorized in New Delivery Months";
         case 'D': return "Deliveries and Intentions";
         case 'E': return "Option Exercises and Assignments";
         case 'F': return "Fix High Low";
         case 'G': return "Good Morning ";
         case 'I': return "Volume & Open Interest (Text format)";
         case 'K': return "Lead Month Identification";
         case 'L': return "Last Day of Trading in Delivery Months";
         case 'M': return "Margin Changes";
         case 'N': return "New Commodity Listings";
         case 'P': return "Cash Prices";
         case 'R': return "Receipts and Shipments";
         case 'S': return "System Changes";
         case 'T': return "Opening/Closing Time Changes, Permanent and Emergency";
         case 'U': return "Crop Reports";
         case 'V': return "Variable Limits Effective";
         case 'W': return "Warehousing Information";
         case 'X': return "Globex";
         case 'Z': return "Flexible Options Text";
      }
      return "";
   }

   static public String VolumeIndicator( char ind )
   {
      switch( ind ) {
         case ' ': return "Volume field not updated";
         case 'A': return "Actual";
         case 'E': return "Estimated";
      }
      return "";
   }
}
