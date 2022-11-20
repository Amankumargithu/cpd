/******************************************************************************
*
*  BigEndian.java
*     Network-byte order conversions from UltraCache wire protocol
*
*  REVISION HISTORY:
*     13 OCT 2011 jcs  Created.
*     21 OCT 2011 jcs  Build   2: GetInt64
*      2 NOV 2011 jcs  Build   3: GetPrc16 / GetPrc64
*     13 JAN 2012 jcs  Build  10: Negative prices in GetPrc16 / GetPrc64
*     22 FEB 2012 jcs  Build  14: GetInt8()
*     11 NOV 2014 jcs  Build  86: GetPrc32U() - Unsigned
*     20 MAR 2016 jcs  Build 100: GetPrc64() - Signed
*
*  (c) 1994-2016 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.util;

/////////////////////////////////////////////////////////////////
// 
//               c l a s s   B i g E n d i a n
//
/////////////////////////////////////////////////////////////////
public class BigEndian
{
   ////////////////////////////////
   // Constructor
   ////////////////////////////////
   public BigEndian() { ; }


   ////////////////////////////////
   // Class-wide
   ////////////////////////////////
   public static int GetInt8( byte[] buf, int off )
   {
      int i1;

      i1 = (0x000000FF & ((int)buf[off+0]) );
      return i1;
   }

   public static int GetInt16( byte[] buf, int off )
   {
      int i1, i2, i16;

      i1  = (0x000000FF & ((int)buf[off+0]) );
      i2  = (0x000000FF & ((int)buf[off+1]) );
      i16 = ( ( i1 << 8 ) | i2 );
      return( i16 & 0x0000FFFF );
   }

   public static long GetInt32( byte[] buf, int off )
   {
      int  i1, i2, i3, i4;
      long l32;

      i1  = (0x000000FF & ((int)buf[off+0]) );
      i2  = (0x000000FF & ((int)buf[off+1]) );
      i3  = (0x000000FF & ((int)buf[off+2]) );
      i4  = (0x000000FF & ((int)buf[off+3]) );
      l32 = (long)( ( i1 << 24 ) | ( i2 << 16 ) | ( i3 << 8 ) | i4 );
      return( l32 & 0xFFFFFFFFL );
   }

   public static long GetInt64( byte[] buf, int off )
   {
      int  l1, l2, l3, l4;
      int  u1, u2, u3, u4;
      long l32, u32, i64;

      l1  = (0x000000FF & ((int)buf[off+0]) );
      l2  = (0x000000FF & ((int)buf[off+1]) );
      l3  = (0x000000FF & ((int)buf[off+2]) );
      l4  = (0x000000FF & ((int)buf[off+3]) );
      u1  = (0x000000FF & ((int)buf[off+4]) );
      u2  = (0x000000FF & ((int)buf[off+5]) );
      u3  = (0x000000FF & ((int)buf[off+6]) );
      u4  = (0x000000FF & ((int)buf[off+7]) );
      l32 = (long)( ( l1 << 24 ) | ( l2 << 16 ) | ( l3 << 8 ) | l4 );
      u32 = (long)( ( u1 << 24 ) | ( u2 << 16 ) | ( u3 << 8 ) | u4 );
      i64 = ( ( l32 << 32 ) | ( u32 & 0xFFFFFFFFL ) );
      return i64;
   }

   public static double GetPrc16( byte[] buf, int off )
   {
      int     i16;
      boolean bNeg;
      double  d16;

      // Shorts have an implied 2 decimal places

      i16  = GetInt16( buf, off );
      bNeg = ( ( 0x00008000 & i16 ) != 0 );
      i16 |= bNeg ? 0xffff0000 : 0;
      d16  = 0.01 * i16;
      return bNeg ? -d16 : d16;
   }

   public static double GetPrc32U( byte[] buf, int off )
   {
      long    l32;
      double  d32;

      // Integers have an implied 4 decimal places

      l32  = GetInt32( buf, off );
      d32  = 0.0001 * l32;
      return d32;
   }

   public static double GetPrc32( byte[] buf, int off )
   {
      long    l32;
      int     i32;
      boolean bNeg;
      double  d32;

      // Integers have an implied 4 decimal places

      l32  = GetInt32( buf, off );
      bNeg = ( ( 0x80000000 & l32 ) != 0 );
      if ( bNeg ) {
         i32 = (int)l32;
         i32 = -i32;
         l32 = i32;
      }
      d32  = 0.0001 * l32;
      return bNeg ? -d32 : d32;
   }

   public static double GetPrc64( byte[] buf, int off )
   {
      long    l64;
      double  d64;
      boolean neg;

      // Longs have an implied 10 decimal places

      neg = ( ( buf[off] & 0x80 ) == 0x80 );
      l64 = GetInt64( buf, off );
//      l64 = neg ? -l64 : l64;
      d64 = 0.0000000001 * l64;
      return d64;
   }

   public static double GetPrc64U( byte[] buf, int off )
   {
      long   l64;
      double d64;

      // Longs have an implied 10 decimal places

      l64 = GetInt64( buf, off );
      d64 = 0.0000000001 * l64;
      return d64;
   }
}
