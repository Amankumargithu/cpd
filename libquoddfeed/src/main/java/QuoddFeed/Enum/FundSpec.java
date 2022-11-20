/******************************************************************************
*
*  FundSpec.java
*     UltraFeed Fund Appendices
*
*  REVISION HISTORY:
*      3 MAY 2012 jcs  Created.
*     15 MAY 2012 jcs  Build 19: 4.2.x sub-classes
*     20 JUL 2012 jcs  Build 29: QuoddFeed.Enum.UltraFeed
*
*  (c) 1994-2012 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.Enum;


/////////////////////////////////////////////////////////////////
//
//                c l a s s    F u n d S p e c
//
/////////////////////////////////////////////////////////////////
/**
 * The FundSpec class encapsulates the UltraFeed Fund appendices.
 */
public class FundSpec extends QuoddFeed.Enum.UltraFeed
{
   /*
    * 4.2.3 Valuation Msg - NAV
    */
   public class NAV
   {
      public class Flags
      {
         static public final int HAS_NAV         = 0x0001;
         static public final int HAS_OFFER       = 0x0002;
         static public final int HAS_PRICE       = 0x0004;
         static public final int HAS_REDEMPT_NAV = 0x0008;
         static public final int HAS_WRAP_PRICE  = 0x0010;
         static public final int HAS_NET_ASSETS  = 0x0020;
         static public final int HAS_YIELD       = 0x0040;
         static public final int HAS_ELTR        = 0x0080;
         static public final int HAS_ACCRUED_INT = 0x0100;
         static public final int HAS_DIV_FACTOR  = 0x0200;
      }
      public class ReportType
      {
         static public final char PriorDayCorrection = 'C';
         static public final char DailyUpdate        = 'R';
         static public final char IntraDayUpdate     = 'I';
         static public final char SummarySpin        = 'E';
      }
   }

   static public String ReportType( char ty )
   {
      switch( ty ) {
         case NAV.ReportType.PriorDayCorrection: return "Prior Day Correction";
         case NAV.ReportType.DailyUpdate:        return "Daily Update";
         case NAV.ReportType.IntraDayUpdate:     return "Intraday Update";
         case NAV.ReportType.SummarySpin:        return "Summary Spin";
      }
      return "Undefined";
   }

   static public String Dividend( char ty )
   {
      switch( ty ) {
         case 'Y': return "Fat Adjustment";
         case 'N': return "No Fat Adjustment";
         case ' ': return "Not applicable";
      }
      return "Undefined";
   }

   /*
    * 4.2.4 Valuation Msg - Money Mkt NAV
    */
   public class NAVmm
   {
      public class Flags
      {
         static public final int HAS_AVGMATURITY     = 0x0001;
         static public final int HAS_AVGLIFE         = 0x0002;
         static public final int HAS_NAV             = 0x0004;
         static public final int HAS_GROSS_7DAY      = 0x0008;
         static public final int HAS_SUBSIDIZED_7DAY = 0x0010;
         static public final int HAS_ANNULIZED_7DAY  = 0x0020;
         static public final int HAS_30DAY_YIELD     = 0x0040;
         static public final int HAS_DIVIDEND_FACTOR = 0x0080;
         static public final int HAS_NET_ASSETS      = 0x0100;
      }
      public class ReportType
      {
         static public final char PriorDayCorrection = 'C'; 
         static public final char DailyUpdate        = 'R';
         static public final char IntraDayUpdate     = 'I';
         static public final char SummarySpin        = 'E';
      }
   }
   /*
    * 4.2.5 Capital Distribution
    */
   public class CapDist
   {
      public class Flags
      {
         static public final int HAS_SHORTTERM_GAIN    = 0x0001; 
         static public final int HAS_LONGTERM_GAIN     = 0x0002; 
         static public final int HAS_UNALLOCATED_DIST  = 0x0004;
         static public final int HAS_RETURN_OF_CAPITAL = 0x0008;
      }
      public class Action
      {
         static public final char New          = 'N';
         static public final char Correction   = 'C';
         static public final char Cancellation = 'X';
      }
   }
   /*
    * 4.2.6 Dividend / Interest 
    */
   public class DivInt
   {
      public class Flags
      {
         static public final int HAS_TOTAL_CASH        = 0x0001; 
         static public final int HAS_NONQUAL_CASH      = 0x0002; 
         static public final int HAS_QUALIFIED_CASH    = 0x0004; 
         static public final int HAS_TAX_FREE_CASH     = 0x0008; 
         static public final int HAS_ORD_FOREIGN_CASH  = 0x0010; 
         static public final int HAS_QUAL_FOREIGN_CASH = 0x0020; 
         static public final int HAS_STOCK_DIVIDEND    = 0x0040; 
      }
      public class Action
      {
         static public final char New          = 'N';
         static public final char Correction   = 'C'; 
         static public final char Cancellation = 'X';     
      }
      public class DistType
      {
         static public final char Dividend           = 'D';
         static public final char InterestPayment    = 'I';
         static public final char NoCashDistribution = 'N';
      }
   }
   /*
    * Appendix A - Fund Type
    */
   static public String FundType( char ty )
   {
      switch( ty ) {
         case '1': return "Mutual Fund  News Media List";
         case '2': return "Money Market Fund  News Media List";
         case '3': return "Mutual Fund Supplemental List";
         case '4': return "Money Market Fund Supplemental List";
         case '5': return "(Not currently supported)";
         case '6': return "Unit Investment Trust (UIT)";
         case '7': return "Structured Product";
         case '8': return "Annuity";
      }
      return "Undefined";
   }

   /*
    * Appendix B - Fund Code
    */
   static public String FundCode( char code )
   {
      switch( code ) {
         case 'A': return "Money Market Fund General Purpose";
         case 'G': return "Money Market Fund Government Securities";
         case 'X': return "Money Market Fund Tax Exempt Securities";
         case 'Z': return "Mutual Fund Open End";
         case 'Y': return "Mutual Fund Closed End";
         case 'D': return "Unit Investment Trust Debt Securities";
         case 'E': return "Unit Investment Trust Equity Securities";
         case 'M': return "Annuity Variable";
         case 'N': return "Annuity Equity Indexed";
         case 'R': return "Structured Product Access Transactions";
         case 'S': return "Structured Product Tax Driven Structure";
         case 'T': return "Structured Product Buffered Note";
         case 'U': return "Structured Product Principal Protected Note";
         case 'V': return "Structured Product Levered Note";
         case 'W': return "Structured Product Enhanced Income Note";
      }
      return "Undefined";
   }

   /*
    * Appendix C - Fund Footnotes
    */
   static public String Footnote( char foot )
   {
      String rtn;

      switch( foot ) {
         case 'A':
            rtn  = "Actual return to shareholders may differ due to ";
            rtn += "capital gains or losses.";
            return rtn;
         case 'B':
            return "Account size varies, yield due to fixed charges.";
         case 'D':
            return "Ex-distribution.";
         case 'F':
            return "Fund always prices using a previous day' quotation.";
         case 'H':
            return "Deferred sales charge.";
         case 'N':
            return "No front-end load or contingent deferred sales load.";
         case 'O':
            rtn  = "Primary offering.  Portfolio that is currently available ";
            rtn += "to investors.";
            return rtn;
         case 'P':
            rtn  = "12(b)(1) plan fund assets are used to pay for ";
            rtn += "distribution costs.";
            return rtn;
         case 'R':
            return "Redemption fee or contingent deferred sales load may apply.";
         case 'S':
            return "Stock split/dividend.";
         case 'W':
            return "NRO or Brown-out indicator.  Trust will not re-offer.";
         case 'X':
            return "Ex-dividend.";
         case 'Y':
            rtn  = "Secondary offering.  Past portfolio that is in the ";
            rtn += "secondary offering period.";
            return rtn;
      }
      return "Undefined";
   }
}
