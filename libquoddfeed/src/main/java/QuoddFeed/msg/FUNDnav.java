/******************************************************************************
*
*  FUNDnav.java
*     QuoddMsg._mt    == 'I'  (MsgTypes._mtINDEX)
*     QuoddMsg._mtSub == 0x25 (MsgTypes._fundSubFUNDNAV)
*     QuoddMsg._mtSub == 0x26 (MsgTypes._fundSubMMNAV)
*     QuoddMsg._mtSub == 0x27 (MsgTypes._fundSubCAPDISTRO)
*     QuoddMsg._mtSub == 0x28 (MsgTypes._fundSubDIVID_INT)
*
*  REVISION HISTORY:
*      2 DEC 2011 jcs  Created.
*     15 MAY 2012 jcs  Build 19: v0.18 : FundSpec; 4 FUND msgs
*     23 MAY 2012 jcs  Build 21: v0.19 : Footnotes()
*      4 JUN 2012 jcs  Build 22: v0.20 : _fundType / _fundCode
*      1 AUG 2012 jcs  Build 31: v0.23 : _close / _netChg / _pctChg
*     23 JUN 2015 jcs  Build 94: v0.25: Dump()
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
//                c l a s s   F U N D n a v
//
/////////////////////////////////////////////////////////////////
/**
 * The FUNDnav class is a parsed Fund Net Asset Value (NAV) message received 
 * from the UltraCache.
 * <p>
 * A FUNDnav is generated by the {@link QuoddFeed.util.UltraChan} class
 * which invokes the
 * {@link QuoddFeed.util.UltraChan#OnUpdate(String,FUNDnav)}
 * callback method to deliver the update into your application.
 */
public class FUNDnav extends QuoddMsg
{
   /*
    * _fundSubFUNDNAV
    */
   public int    _flags;
   public char   _reportType;
   public String _footnotes;
   public double _nav;
   public double _price;
   public double _wrapPrice;
   public double _netAssets;
   public double _yield;
   public double _eltr;
   public double _accruedInt;
   public char   _divIndicator;
   public double _dailyDividend;
   public long   _entryDate;
   public double _close;
   public double _netChg;
   public double _pctChg;
   /*
    * _fundSubMMNAV
    */
   public int    _avgMaturity;
   public int    _avgLife;
   public double _yield7DayGross;
   public double _yield7DaySubsidized;
   public double _yield7DayAnnualized;
   public double _yield30DayDay;
   public long   _yield30DayDate;
   public double _totalNetAssets;
   /*
    * _fundSubCAPDISTRO
    */
   public char   _action;
   public double _shortTermGain;
   public double _longTermGain;
   public double _unallocatedDist;
   public double _returnOnCapital;
   public long   _exDate;
   public long   _recordDate;
   public long   _paymentDate;
   public long   _reinvestDate;
   /*
    * _fundSubDIVID_INT
    */ 
   public char   _distroType;
   public double _totalCashDist;
   public double _nonQualCashDist;
   public double _qualCashDist;
   public double _taxFreeCashDist;
   public double _ordForeignTaxCredit;
   public double _qualForeignTaxCredit;
   public double _stockDividend;
   /*
    * All
    */
   public char _fundType;
   public char _fundCode;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public FUNDnav()
   {
      _flags                = 0;
      _reportType           = 0;
      _footnotes            = "";
      _nav                  = 0.0;
      _price                = 0.0;
      _wrapPrice            = 0.0;
      _netAssets            = 0.0;
      _yield                = 0.0;
      _eltr                 = 0.0;
      _accruedInt           = 0.0;
      _divIndicator         = 0;
      _dailyDividend        = 0.0;
      _entryDate            = 0;
      _close                = 0.0;
      _netChg               = 0.0;
      _pctChg               = 0.0;
      _avgMaturity          = 0;
      _avgLife              = 0;
      _yield7DayGross       = 0.0;
      _yield7DaySubsidized  = 0.0;
      _yield7DayAnnualized  = 0.0;
      _yield30DayDay        = 0.0;
      _yield30DayDate       = 0;
      _totalNetAssets       = 0.0;
      _action               = 0;
      _shortTermGain        = 0.0;
      _longTermGain         = 0.0;
      _unallocatedDist      = 0.0;
      _returnOnCapital      = 0.0;
      _exDate               = 0;
      _recordDate           = 0;
      _paymentDate          = 0;
      _reinvestDate         = 0;
      _distroType           = 0;
      _totalCashDist        = 0.0;
      _nonQualCashDist      = 0.0;
      _qualCashDist         = 0.0;
      _taxFreeCashDist      = 0.0;
      _ordForeignTaxCredit  = 0.0;
      _qualForeignTaxCredit = 0.0;
      _stockDividend        = 0.0;
      _fundType             = 0;
      _fundCode             = 0;
   }

   public FUNDnav( byte[] b, int off, int nLeft )
   {
      this.Set( b, off, nLeft );
   }

   public FUNDnav( FUNDnav c )
   {
      super( c );
      _flags                = c._flags;
      _reportType           = c._reportType;
      _footnotes            = new String( c._footnotes );
      _nav                  = c._nav;
      _price                = c._price;
      _wrapPrice            = c._wrapPrice;
      _netAssets            = c._netAssets;
      _yield                = c._yield;
      _eltr                 = c._eltr;
      _accruedInt           = c._accruedInt;
      _divIndicator         = c._divIndicator;
      _dailyDividend        = c._dailyDividend;
      _entryDate            = c._entryDate;
      _close                = c._close;
      _netChg               = c._netChg;
      _pctChg               = c._pctChg;
      _avgMaturity          = c._avgMaturity;
      _avgLife              = c._avgLife;
      _yield7DayGross       = c._yield7DayGross;
      _yield7DaySubsidized  = c._yield7DaySubsidized;
      _yield7DayAnnualized  = c._yield7DayAnnualized;
      _yield30DayDay        = c._yield30DayDay;
      _yield30DayDate       = c._yield30DayDate;
      _totalNetAssets       = c._totalNetAssets;
      _action               = c._action;
      _shortTermGain        = c._shortTermGain;
      _longTermGain         = c._longTermGain;
      _unallocatedDist      = c._unallocatedDist;
      _returnOnCapital      = c._returnOnCapital;
      _exDate               = c._exDate;
      _recordDate           = c._recordDate;
      _paymentDate          = c._paymentDate;
      _reinvestDate         = c._reinvestDate;
      _distroType           = c._distroType;
      _totalCashDist        = c._totalCashDist;
      _nonQualCashDist      = c._nonQualCashDist;
      _qualCashDist         = c._qualCashDist;
      _taxFreeCashDist      = c._taxFreeCashDist;
      _ordForeignTaxCredit  = c._ordForeignTaxCredit;
      _qualForeignTaxCredit = c._qualForeignTaxCredit;
      _stockDividend        = c._stockDividend;
      _fundType             = c._fundType;
      _fundCode             = c._fundCode;
   }

   public FUNDnav clone() { return new FUNDnav( this ); }


   ///////////////////////////////
   // Access
   ///////////////////////////////
   /**
    * Returns fund type as a string defined in UltraFeed spec
    * @return String-ified fund type
    */
   public String FundType()
   {
      return FundSpec.FundType( _fundType );
   }

   /**
    * Returns fund code as a string defined in UltraFeed spec
    * @return String-ified fund code 
    */
   public String FundCode()
   {
      return FundSpec.FundCode( _fundCode );
   }

   /**
    * Returns footnotes as a string defined in UltraFeed spec
    * @return String-ified footnotes
    */
   public String Footnotes()
   {
      char[] ch  = _footnotes.toCharArray();
      String rtn = "";
      int    i;

      for ( i=0; i<ch.length; i++ ) {
         rtn += FundSpec.Footnote( ch[i] );
         rtn += "\n";
      }
      return rtn; 
   }

   public boolean IsNAV()
   {
      return( ( mtSub() == _fundSubFUNDNAV ) || 
              ( mtSub() == _fundSubMMNAV ) );
   }

   public boolean IsCapDistro()
   {
      return( mtSub() == _fundSubCAPDISTRO );
   }

   public boolean IsDividendInterest()
   {
      return( mtSub() == _fundSubDIVID_INT );
   }

   /**
    * @return String-ified reporting period
    */
   public String ReportPeriod()
   {
      return FundSpec.ReportType( _reportType );
   }

   /**
    * NAV 
    */
   public double NAV()
   {
      switch( mtSub() ) {
         case _fundSubFUNDNAV:
            return _IsSet( FundSpec.NAV.Flags.HAS_NAV ) ? _nav : 0.0;
         case _fundSubMMNAV:
            return _IsSet( FundSpec.NAV.Flags.HAS_NAV ) ? _nav : 0.0;
      }
      return 0.0;
   }

   /**
    * Price
    */
   public double Price()
   {
      boolean bOK;

      bOK  = _IsSet( FundSpec.NAV.Flags.HAS_OFFER );
      bOK |= _IsSet( FundSpec.NAV.Flags.HAS_PRICE );
      return bOK ? _price : 0.0;
   }

   /**
    * Wrap Price 
    */
   public double WrapPrice()
   {
      return _IsSet( FundSpec.NAV.Flags.HAS_WRAP_PRICE ) ? _wrapPrice : 0.0;
   }

   /**
    * Net Assets 
    */
   public double NetAssets()
   {
      return _IsSet( FundSpec.NAV.Flags.HAS_NET_ASSETS ) ? _netAssets : 0.0;
   }

   /**
    * Yield 
    */
   public double Yield()
   {
      return _IsSet( FundSpec.NAV.Flags.HAS_YIELD ) ? _yield : 0.0;
   }

   /**
    * ELTR 
    */
   public double ELTR()
   {
      return _IsSet( FundSpec.NAV.Flags.HAS_ELTR ) ? _eltr : 0.0;
   }

   /**
    * Accrued Interest 
    */
   public double AccruedInterest()
   {
      return _IsSet( FundSpec.NAV.Flags.HAS_ACCRUED_INT ) ? _accruedInt : 0.0;
   }

   /**
    * Dividend Indicator 
    */
   public String DividendType()
   {
      return FundSpec.Dividend( _divIndicator );
   }

   /**
    * Entry Date 
    */
   public GregorianCalendar EntryDate()
   {
      return Long2Date( _entryDate );
   }

   /**
    * Entry Date as String
    */
   public String pEntryDate()
   {
      return pLong2Date( _entryDate );
   }


   ///////////////////////////////
   // Helpers
   ///////////////////////////////
   public boolean _IsSet( int bitmask )
   {
      return( ( _flags & bitmask ) == bitmask );
   }


   ///////////////////////////////
   // QuoddMsg Override - Dump 
   ///////////////////////////////
   @Override
   public String Dump()
   {
      String s;

      s  = super.Dump();
      s += s.format( "DT=%s NAV=%.2f; PRC=%.2f; AST=%.1f; YLD=%.3f\n",
            pEntryDate(), NAV(), Price(), NetAssets(), Yield() );
      s += s.format( "   Type = %s\n", FundType() );
      s += s.format( "   Code = %s\n", FundCode() );
      return s;
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   @Override
   public QuoddMsg Set( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;

      super.Set( b, off, nLeft );
      switch( mtSub() ) {
         case _fundSubFUNDNAV:
            return _SetFundNAV( b, off, nLeft );
         case _fundSubMMNAV:
            return _SetMoneyMktNAV( b, off, nLeft );
         case _fundSubCAPDISTRO:
            return _SetCapDistro( b, off, nLeft );
         case _fundSubDIVID_INT:
            return _SetDivInt( b, off, nLeft );
      }
      return null;
   }

   ///////////////////////////////
   // Helpers
   ///////////////////////////////
   private QuoddMsg _SetFundNAV( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;
      String   err;

      off += QuoddMsg.MINSZ;
      try {
         _flags         = BigEndian.GetInt16( b, off+ 0 );
         _reportType    = (char)b[off+2];
         _footnotes     = new String( b, off+3, 10 ).trim();
         _nav           = BigEndian.GetPrc64( b, off+13 );
         _price         = BigEndian.GetPrc64( b, off+21 );
         _wrapPrice     = BigEndian.GetPrc64( b, off+29 );
         _netAssets     = BigEndian.GetPrc64( b, off+37 );
         _yield         = BigEndian.GetPrc64( b, off+45 );
         _eltr          = BigEndian.GetPrc64( b, off+53 );
         _accruedInt    = BigEndian.GetPrc64( b, off+61 );
         _divIndicator  = (char)b[off+69];
         _dailyDividend = BigEndian.GetPrc64( b, off+70 );
         _entryDate     = BigEndian.GetInt32( b, off+78 );
         _fundType      = (char)b[off+82];
         _fundCode      = (char)b[off+83];
         _close         = BigEndian.GetPrc32( b, off+84 );
         _netChg        = BigEndian.GetPrc32( b, off+88 );
         _pctChg        = BigEndian.GetPrc32( b, off+92 );
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }

   private QuoddMsg _SetMoneyMktNAV( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;
      String   err;

      off += QuoddMsg.MINSZ;
      try {
         _flags               = BigEndian.GetInt16( b, off+ 0 );
         _reportType          = (char)b[off+2];
         _footnotes           = new String( b, off+3, 10 ).trim();
         _avgMaturity         = BigEndian.GetInt16( b, off+13 );
         _avgLife             = BigEndian.GetInt16( b, off+15 );
         _nav                 = BigEndian.GetPrc64( b, off+17 );
         _yield7DayGross      = BigEndian.GetPrc64( b, off+25 );
         _yield7DaySubsidized = BigEndian.GetPrc64( b, off+33 );
         _yield7DayAnnualized = BigEndian.GetPrc64( b, off+41 );
         _yield30DayDay       = BigEndian.GetPrc64( b, off+49 );
         _yield30DayDate      = BigEndian.GetInt32( b, off+57 );
         _divIndicator        = (char)b[off+61];
         _dailyDividend       = BigEndian.GetPrc64( b, off+62 );
         _totalNetAssets      = BigEndian.GetPrc64( b, off+70 );
         _entryDate           = BigEndian.GetInt32( b, off+78 );
         _fundType            = (char)b[off+82];
         _fundCode            = (char)b[off+83];
         _close               = BigEndian.GetPrc32( b, off+84 );
         _netChg              = BigEndian.GetPrc32( b, off+88 );
         _pctChg              = BigEndian.GetPrc32( b, off+92 );
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }
   private QuoddMsg _SetCapDistro( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;
      String   err;

      off += QuoddMsg.MINSZ;
      try {
         _flags           = BigEndian.GetInt16( b, off+ 0 );
         _action          = (char)b[off+2];
         _longTermGain    = BigEndian.GetPrc64( b, off+ 3 );
         _unallocatedDist = BigEndian.GetPrc64( b, off+11 );
         _returnOnCapital = BigEndian.GetPrc64( b, off+19 );
         _shortTermGain   = BigEndian.GetPrc64( b, off+27 );
         _exDate          = BigEndian.GetInt32( b, off+35 );
         _recordDate      = BigEndian.GetInt32( b, off+39 );
         _paymentDate     = BigEndian.GetInt32( b, off+43 );
         _reinvestDate    = BigEndian.GetInt32( b, off+47 );
         _fundType        = (char)b[off+51];
         _fundCode        = (char)b[off+52];
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }
   private QuoddMsg _SetDivInt( byte[] b, int off, int nLeft )
   {
      QuoddMsg rtn;
      String   err;

      off += QuoddMsg.MINSZ;
      try {
         _flags                = BigEndian.GetInt16( b, off+ 0 );
         _action               = (char)b[off+2];
         _distroType           = (char)b[off+3];
         _totalCashDist        = BigEndian.GetPrc64( b, off+ 4 );
         _nonQualCashDist      = BigEndian.GetPrc64( b, off+12 );
         _qualCashDist         = BigEndian.GetPrc64( b, off+20 );
         _taxFreeCashDist      = BigEndian.GetPrc64( b, off+28 );
         _ordForeignTaxCredit  = BigEndian.GetPrc64( b, off+36 );
         _qualForeignTaxCredit = BigEndian.GetPrc64( b, off+44 );
         _stockDividend        = BigEndian.GetPrc64( b, off+52 );
         _exDate               = BigEndian.GetInt32( b, off+60 );
         _recordDate           = BigEndian.GetInt32( b, off+64 );
         _paymentDate          = BigEndian.GetInt32( b, off+68 );
         _reinvestDate         = BigEndian.GetInt32( b, off+72 );
         _fundType             = (char)b[off+76];
         _fundCode             = (char)b[off+77];
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
      }
      return this;
   }
   private GregorianCalendar Long2Date( long dt )
   {
      int m, d, y;

      // MMDDYYYY : TODO : Put to native Date Time

      m = (int)dt / 1000000;
      d = ( (int)dt / 10000 ) % 100;
      y = (int)dt % 10000;
      return new GregorianCalendar( y, m, d );
   }

   /**
    * Entry Date as String
    */
   private String pLong2Date( long dt )
   {
      GregorianCalendar g;
      int               m, d, y;

      g = Long2Date( dt );
      m = g.get( Calendar.MONTH );
      d = g.get( Calendar.DAY_OF_MONTH );
      y = g.get( Calendar.YEAR );
      return String.format( "[%08d] %04d-%02d-%02d", dt, y, m, d );
   }


   ///////////////////////////////
   // Class-wide
   ///////////////////////////////
   static private int FUNDNAV_MINSZ   = QuoddMsg.MINSZ + 96; // 94;
   static private int MMNAV_MINSZ     = QuoddMsg.MINSZ + 96; // 94;
   static private int CAPDISTRO_MINSZ = QuoddMsg.MINSZ + 53; // 51;
   static private int DIVID_INT_MINSZ = QuoddMsg.MINSZ + 78; // 76;

   static public int MinSize( char mt2 )
   {
      switch( mt2 ) {
         case _fundSubFUNDNAV:
            return FUNDnav.FUNDNAV_MINSZ;
         case _fundSubMMNAV:
            return FUNDnav.MMNAV_MINSZ;
         case _fundSubCAPDISTRO:
            return FUNDnav.CAPDISTRO_MINSZ;
         case _fundSubDIVID_INT:
            return FUNDnav.DIVID_INT_MINSZ;
      }
      return -1;
   }
}
