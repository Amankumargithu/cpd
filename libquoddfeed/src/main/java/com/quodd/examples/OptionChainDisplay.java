/******************************************************************************
*
*  OptionChainDisplay.java
*     QuoddFeed.MD.OptionChain example
*
*  REVISION HISTORY:
*     18 JAN 2012 jcs  Created.
*     30 JAN 2012 jcs  Build 13: UltraChan._usr / _pwd
*      9 APR 2012 jcs  Build 15: OptionTrade
*     28 JUN 2012 jcs  Build 27: UCconfig
*
*  (c) 2011-2012, Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.util.*;
import QuoddFeed.util.*;

public class OptionChainDisplay
{
   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      UltraChan                uChan;
      QuoddFeed.MD.OptionChain chn;
      QuoddFeed.MD.OptionTrade trd;
      QuoddFeed.MD.OptionDump  dmp;
      Scanner                  sc;
      String                   idx;
      boolean                  bTrd, bDmp;
      int                      i, nRow, argc;

      argc  = args.length;
      idx   = ( argc > 0 ) ? args[0] : "AAPL";
      bTrd  = ( argc > 1 ) ? args[1].equals( "TRADE" ) : false;
      bDmp  = ( argc > 1 ) ? args[1].equals( "DUMP" ) : false;
      nRow  = ( argc > 2 ) ? Integer.parseInt( args[2] ) : 45;
      uChan = new UltraChan( UCconfig.Hostname(), 
                             UCconfig.Port(),
                             "username",
                             "password",
                             false );
      uChan.Start();
      sc  = new Scanner( System.in );
      System.out.printf( "Hit <ENTER> to close...\n" );
      chn = null;
      if ( bTrd )
         trd = new QuoddFeed.MD.OptionTrade( uChan, idx );
      else if ( bDmp )
         dmp = new QuoddFeed.MD.OptionDump( uChan, idx );
      else
         chn = new QuoddFeed.MD.OptionChain( uChan, idx, nRow );
      sc.nextLine();
      if ( chn != null )
         chn.Close();
      System.out.printf( "Shutting down ...\n" );
      uChan.Stop();
      System.out.printf( "Done!!\n" );
   }
}
