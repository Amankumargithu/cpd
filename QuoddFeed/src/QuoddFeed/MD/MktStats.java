/******************************************************************************
*
*  MktStats.java
*     Higher-order Market Stats object (from MKT-STATS ticker)
*
*  REVISION HISTORY:
*     11 JUL 2012 jcs  Created.
*      3 OCT 2012 jcs  Build 40: _nTickUp / Dn
*     14 OCT 2012 jcs  Build 42: One-step Subscribe and Register
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.MD;

import java.lang.*;
import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    M k t S t a t s
//
/////////////////////////////////////////////////////////////////
/**
 * MktStats is an higher-order QuoddFeed.MD class for consuming and
 * showing EQMktStats msgs.
 * <p>
 * We store row numbers in an unsorted HashMap collection keyed by exch.
 */
public class MktStats extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan                _uChan;
   protected int                      _maxRow;
   protected String                   _arg;
   protected PrintStream               cout;
   private   int                      _StreamID;
   private   HashMap<String, Integer> _recs;

   //////////////////////
   // Constructor
   //////////////////////
   public MktStats( UltraChan uChan, int maxRow )
   {
      // Guts

      _uChan    = uChan;
      _maxRow   = maxRow;
      _arg      = "User-defined argument";
       cout      = System.out;
      _StreamID = 0;
      _recs     = new HashMap<String, Integer>();
      Open();
   }

   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      _StreamID  = _uChan.Subscribe( "MKT_STATS", _arg, this );

      // Clear Screen

      cout.printf( ANSI_CLEAR );
      cout.printf( ANSI_HOME );
      cout.printf( "Exch nTkrUp nTkrDn | TickUp TickDn " );
      cout.printf( "|   VolumeUp    VolumeDown  | TRIN\n" );
      cout.printf( "---- ------ ------ + ------ ------ " );
      cout.printf( "+ ------------ ------------ + ----\n" );
   }

   public void Close()
   {
      _uChan.Unsubscribe( _StreamID );
      _recs.clear();
      System.out.printf( ANSI_CLEAR );
   }


   //////////////////////
   // IUpdate Interface - Stream
   //////////////////////
   /**
    * Called when an EQMktStats update is received.
    */
   public void OnUpdate( String StreamName, EQMktStats st )
   {
      int row;

      // Pre-condition

      row  = _GetRow( st );
      row += 3; // Header
      if ( row > _maxRow )
         return;

      // Safe to show ...

      cout.printf( ANSI_ROWCOL, row, 1 );
      cout.printf( "%-4s %6d %6d + %6d %6d + %12d %12d + %.3f\n",
         st._mktCtr,
         st._nTkrUp, st._nTkrDn,
         st._nTickUp, st._nTickDn,
         st._volUp, st._volDn,
         st.TRIN() );
   }


   //////////////////////
   // Helpers
   //////////////////////
   private int _GetRow( EQMktStats st )
   {
      String  tkr;
      Integer row;

      tkr = st._mktCtr;
      if ( _recs.containsKey( tkr ) )
         row = (Integer)_recs.get( tkr );
      else {
         row = new Integer( _recs.size() );
         _recs.put( tkr, row );
      }
      return row.intValue();
   }
}
