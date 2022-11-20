/******************************************************************************
*
*  SpryWare.java
*     Common SpryWare channel information
*
*  REVISION HISTORY:
*     29 APR 2014 jcs  Created.
*      7 JUL 2015 jcs  Build 95: TradeCondition() : _trdCond only, not IsCxl()
*
*  (c) 2011-2015 Quodd Financial
*******************************************************************************/
package QuoddFeed.spry;



/////////////////////////////////////////////////////////////////
//
//               c l a s s    S p r y W a r e
//
/////////////////////////////////////////////////////////////////
/**
 * The SpryWare class encapsulates the SpryWare Enumerated stuff.
 */
public class SpryWare
{
   //////////////////////////////////
   //
   //       O P T r a d e
   //
   //////////////////////////////////

   static public String TradeCondition( QuoddFeed.msg.OPTrade trd )
   {
      char cnd;

      /*
       * case   : From OPRA Spec data_recipient_interface.pdf
       * return : From Hanji DataDictionary.xls
       */
/*
 * 15-07-07 jcs  Build 95  _trdCond only
      cnd = trd.IsCxl() ? 'C' : trd._trdCond;
 */
      cnd = trd._trdCond;
      switch( cnd ) {
         case ' ': // RGLR # Regular Sale
            return "RGLR";
         case 'A': // CANC # Cancelled - Not Last or Opening
            return "CANC";
         case 'B': // OSEQ # Out of Sequence
            return "OSEQ";
         case 'C': // CNCL # Cancelled
            return "CNCL";
         case 'D': // LATE # Late, but correct sequence
            return "LATE";
         case 'E': // CNCO # Opening, Now Cancelled
            return "CNCO";
         case 'F': // OPEN # Late report of Opening Trade; Now out of sequence
            return "OPEN";
         case 'G': // CNOL # Only Trade for Day; Now Cancelled
            return "CNOL";
         case 'H': // OPNL # Late report of Opening Trade; Correct sequence
            return "OPNL";
         case 'I': // AUTO # Regular, Electronic Transaction
            return "AUTO";
         case 'J': // REOP # Reopening
            return "REOP";
         case 'K': // AJST # Adjusted - Dividend, Split, etc.
            return "AJST";
         case 'L': // SPRD # Spread : Buy and Sell - Both Call or Both Put
            return "SPRD";
         case 'M': // STDL # Straddle : Buy and Sell - Put / Call
            return "STDL";
         case 'N': // STPD # Stopped
            return "STPD";
         case 'O': // CSTP # Cancel stopped transaction
            return "CSTP";
         case 'P': // BWRT # Option Leg : Buy / Sell of a call or put + Stock
            return "BWRT";
         case 'Q': // CMBO # Buy Call / Sell Put
            return "CMBO";
         case 'R': // SPIM # Stopped; No Trade-Through
            return "SPIM";
         case 'S': // ISOI # Intermarket Sweep Order
            return "ISOI";
         case 'T': // BNMT # Benchmark Trade
            return "BNMT";
         case 'X': // XMPT # Trade Through Exempt
            return "XMPT";
      }
      return "RGLR";
   }


   //////////////////////////////////
   //
   //         O P B b o
   //
   //////////////////////////////////

   static public String QuoteCondition( QuoddFeed.msg.OPBbo bbo )
   {
      return _QuoteCondition( bbo._qteCond );
   }

   static public String QuoteCondition( QuoddFeed.msg.OPQuote qte )
   {
      return _QuoteCondition( qte._qteCond );
   }


   ////////////////////////
   // Helpers
   ////////////////////////
   static private String _QuoteCondition( char cnd )
   {
      /*
       * From Hanji - 14-05-15
       */
      switch( cnd ) {
         case ' ':  // Regular Trading
            return "RGLR";
         case 'F':  // Non-Firm Quote
            return "NFQT";
         case 'R':  // Rotation
            return "ROTA";
         case 'T':  // Trading Halted
            return "HALT";
         case 'A':  // Eligible for Automatic Execution
            return "ELIG";
         case 'B':  // Bid contains Customer trading interest
            return "BINT";
         case 'O':  // Offer contains Customer trading interest
            return "AINT";
         case 'C':  // Both Bid and Offer contain Customer trading interest
            return "BOTH";
         case 'X':  // Offer side of Quote Not Firm; Bid Side Firm
            return "BFRM";
         case 'Y':  // Bid Side of Quote Not Firm; Offer Side Firm
            return "AFRM";
      }
      return "1";   // Regular
   }

/*
 * List of all conditions from Hanji spreadsheet - SpryWare
 *
            return "1";   // Regular
            return "2";   // DepthOnOffer
            return "3";   // DepthOnBid
            return "4";   // Closing
            return "5";   // NewsDissemination
            return "6";   // FastTrading
            return "7";   // TradingRange
            return "8";   // DepthOnBidOffer
            return "9";   // OrderInflux
            return "10";  // DueToRelated
            return "11";  // NewsPending
            return "12";  // MarketMakerClosed
            return "13";  // AdditionalInfo
            return "14";  // NonFirm
            return "15";  // OpeningQuote
            return "16";  // AdditionalInfoDueToRelated
            return "17";  // Resume
            return "18";  // ViewOfCommon
            return "19";  // VolumeAlert
            return "20";  // OrderImbalance
            return "21";  // EquipmentChangeover
            return "22";  // NoOpenNoResume
            return "23";  // Crossed
            return "24";  // Locked
            return "25";  // AutomaticExecution
            return "26";  // AutomaticExecutionETH
            return "27";  // RegularETH
            return "28";  // FastMarket
            return "29";  // FastMarketETH
            return "30";  // Inactive
            return "31";  // InactiveETH
            return "32";  // Rotation
            return "33";  // RotationETH
            return "34";  // Halt
            return "35";  // HaltETH
            return "36";  // DueToNewsDissemination
            return "37";  // DueToNewsPending
            return "38";  // TradingResumption
            return "39";  // OutOfSequence
            return "40";  // BidSpecialist
            return "41";  // OfferSpecialist
            return "42";  // BidOfferSpecialist
            return "43";  // EndOfDaySAM
            return "44";  // ForbiddenSAM
            return "45";  // FrozenSAM
            return "46";  // PreOpeningSAM
            return "47";  // OpeningSAM
            return "48";  // OpenSAM
            return "49";  // SurveillanceSAM
            return "50";  // SuspendedSAM
            return "51";  // ReservedSAM
            return "52";  // NoActiveMarketMaker
            return "53";  // Restricted
            return "54";  // ManualAskAutoBid
            return "55";  // ManualBidAutoAsk
            return "56";  // ManualBidManualAsk
            return "57";  // SlowOnOffer
            return "58";  // SlowOnBid
            return "59";  // SlowOnBidAndOffer
            return "60";  // SlowDueToSetSlowListOnBidAndOffer
            return "61";  // SlowDueToLRPorGapOnOffer
            return "62";  // SlowDueToLRPorGapOnBid
            return "63";  // SlowDueToLRPorGapOnBidAndOffer
            return "64";  // OneSidedAutomated
 */
}
