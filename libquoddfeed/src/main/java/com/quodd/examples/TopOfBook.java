/******************************************************************************
*
*  TopOfBook.java
*     Validate top of QuoddFeed.MD.Level2 book(s)
*
*  REVISION HISTORY:
*     21 FEB 2012 jcs  Created.
*     28 JUN 2012 jcs  Build 27: UCconfig
*
*  (c) 2011-2012, Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.lang.*;
import java.util.*;
import QuoddFeed.util.*;

public class TopOfBook extends QuoddFeed.MD.Level2
{
   static private long _nTop = 0;
   static private long _nX   = 0;

   //////////////////////
   // Constructor
   //////////////////////
   public TopOfBook( UltraChan uChan, 
                     String    tkr, 
                     boolean   bLvl2Only )
   {
      super( uChan, tkr, 45, 0.0, bLvl2Only );
   }

   //////////////////////
   // Level2 Interface
   //////////////////////
   public void OnLevel2()
   {
      double  b, a;
      boolean bX;

      b    = BestBid();
      a    = BestAsk();
      bX   = ( b > a );
      _nX += bX ? 1 : 0;
      if ( (_nTop%1000) == 0 )
         System.out.printf( "[%05d of %05d] crossed\n", _nX, _nTop );
      if ( bX )
         System.out.printf( "%s %7.2f x %7.2f\n", _tkr, b, a );
      _nTop += 1;
   }

   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      UltraChan uChan;
      Scanner   sc;
      String    tkr;
      String[]  tkrs;
      boolean   bLvl2Only;
      int       i, n, argc;

      argc      = args.length;
      tkr       = ( argc > 0 ) ? args[0] : "DELL";
      tkrs      = tkr.split(",");
      bLvl2Only = ( argc > 1 ) ? args[1].equals( "LEVEL2_ONLY" ) : true;
      uChan = new UltraChan( UCconfig.Hostname(),
                             UCconfig.Port(),
                             "username",
                             "password",
                             false );
      uChan.Start();
      n   = tkrs.length;
      for ( i=0; i<n; new TopOfBook( uChan, tkrs[i++], bLvl2Only ) );
      System.out.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      sc.nextLine();
      System.out.printf( "Shutting down ...\n" );
      uChan.Stop();
      System.out.printf( "Done!!\n" );
   }
}
