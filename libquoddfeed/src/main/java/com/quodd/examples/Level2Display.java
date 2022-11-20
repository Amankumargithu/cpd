/******************************************************************************
*
*  Level2Display.java
*     QuoddFeed.MD.Level2 example
*
*  REVISION HISTORY:
*     18 JAN 2012 jcs  Created.
*     30 JAN 2012 jcs  Build 13: UltraChan._usr / _pwd
*     20 MAR 2012 jcs  Build 15: bLvl2Only = false
*     22 APR 2012 jcs  Build 17: Level2Dump = false
*     28 JUN 2012 jcs  Build 27: UCconfig
*
*  (c) 2011-2012, Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.util.*;
import java.lang.*;
import QuoddFeed.util.*;

public class Level2Display
{
   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      UltraChan               uChan;
      QuoddFeed.MD.Level2     l2;
      QuoddFeed.MD.Level2Dump dmp;
      Scanner                 sc;
      String                  tkr;
      double                  tSnap;
      boolean                 bLvl2Only, bDmp;
      int                     i, argc;

      argc      = args.length;
      tkr       = ( argc > 0 ) ? args[0] : "DELL";
      bLvl2Only = ( argc > 1 ) ? args[1].equals( "LEVEL2_ONLY" ) : false;
      bDmp      = ( argc > 1 ) ? args[1].equals( "DUMP" ) : false;
      tSnap     = ( argc > 2 ) ? Float.valueOf( args[2] ).doubleValue() : 0.0;
      uChan = new UltraChan( UCconfig.Hostname(), 
                             UCconfig.Port(),
                             "username",
                             "password",
                             false );
      uChan.Start();
      if ( bDmp )
         dmp = new QuoddFeed.MD.Level2Dump( uChan, tkr, bLvl2Only );
      else
         l2  = new QuoddFeed.MD.Level2( uChan, tkr, 45, tSnap, bLvl2Only );
      System.out.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      sc.nextLine();
      System.out.printf( "Shutting down ...\n" );
      uChan.Stop();
      System.out.printf( "Done!!\n" );
   }
}
