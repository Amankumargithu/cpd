/******************************************************************************
*
*  Blob.java
*     QuoddMsg._mt == 'B' (MsgTypes._mtBLOB)
*
*     class QBlob : public QuoddMsg
*     {
*     public:
*        u_short _nPkt;    // BLOB packet number
*        u_short _totPkt;  // Total packets in BLOB
*        u_int   _nLst;    // Total elements in packet list 
*        u_char  _name[32];
*     };
*
*  REVISION HISTORY:
*     24 OCT 2011 jcs  Created.
*     18 NOV 2011 jcs  Build  5: Copy Constructor
*     22 FEB 2012 jcs  Build 14: v0.12: Copy constructor (QuoddMsg)
*      9 APR 2012 jcs  Build 16: v0.14: Multi-msg parsing error
*
*  (c) 2011-2012 Quodd Financial
*******************************************************************************/
package QuoddFeed.msg;

import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;

/////////////////////////////////////////////////////////////////
//
//                  c l a s s   B l o b 
//
/////////////////////////////////////////////////////////////////
/**
 * A Blob is an unstructured byte[] array that is sent from UltraCache 
 * in one <b>OR MORE</b> QuoddMsg messages.  This allows us to encapsulate 
 * effectively any data object, chunk, etc. from UltraCache.
 *
 *  Currently, we define 2 structured Blob object:
 *    1) BlobList
 *    2) BlobTable
 */
public class Blob extends QuoddMsg
{
   ///////////////////////////
   // Class-wide Members
   ///////////////////////////
   static public final int   MINSZ    = 40;
static private char[] _cFS = { 0x03 };
static private String _FS  = new String( _cFS );

   ///////////////////////////
   // Instance Members
   ///////////////////////////
   private long   _nPkt;
   private long   _totPkt;
   private long   _nLst;
   private String _name;
   /**
    * Raw Blob data, either from single or multiple QuoddMsg messages.
    */
   protected String _blob;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public Blob()
   {
      _nPkt   = 0;
      _totPkt = 0;
      _nLst   = 0;
      _name   = null;
      _blob   = new String();
   }

   public Blob( byte[] b, int off, int nLeft )
   {
      this.Set( b, off, nLeft );
   }

   public Blob( Blob c )
   {
      super( c );
      _nPkt   = c._nPkt;
      _totPkt = c._totPkt;
      _nLst   = c._nLst;
      _name   = new String( c._name );
      _blob   = new String( c._blob );
   }

   public Blob clone() { return new Blob( this ); }


   ///////////////////////////////
   // Access
   ///////////////////////////////
   /**
    * Returns true, if entire Blob has been received and parsed.  Blobs   
    * may span multiple QuoddMsg packets, since QuoddMsg's are limited to 
    * a maximum length of 64 KB message length field in the QuoddMsg header. 
    *
    * We have received the entire Blob of _nPkt == _totPkt.
    */
   public boolean IsDone()
   {
      return( _nPkt == _totPkt );
   }

   /**
    * Packet number of this Blob message.  Blob has been completely received
    * when nPkt() == totPkt().
    */
   public long nPkt()
   {
      return _nPkt;
   }

   /**
    * Total packets in the Blob. Blob has been completely received when
    * nPkt() == totPkt().
    */
   public long totPkt()
   {
      return _totPkt;
   } 

   /**
    * Number of structured records (e.g., Ticker Name, or Ticker Snapshot)
    * in the BlobList or BlobTable.
    */
   public long nLst()
   {
      return _nLst;
   }

   /**
    * Blob name (e.g., Ticker or Query Name)
    */
   public String name()
   {
      return _name;
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   /**
    * Parse the Blob message from the incoming byte buffer.
    * <p>
    * @param b Input buffer
    * @param off Offset in b
    * @param nLeft NumBytes remaining in Input byte[] array after offset.
    * @return this
    */
   public QuoddMsg Set( byte[] b, int off, int nLeft )
   {
      String err, blob;
      int    bSz, cSz;
int o0;

      // Process

      if ( IsDone() )
         _blob = "";
      cSz = _blob.length();
      o0  = off;
      super.Set( b, off, nLeft );
      off += QuoddMsg.MINSZ;
      try {
         _nPkt   = BigEndian.GetInt16( b, off+0 );
         _totPkt = BigEndian.GetInt16( b, off+2 );
         _nLst   = BigEndian.GetInt32( b, off+4 );
         _name   = new String( b, off+8, 32 ).trim();
         off    += Blob.MINSZ;
/*
 * 12-04-09 jcs  Build 15 : BUG
         bSz     = len() - off;
         bSz    += ( bSz < 0 ) ? cSz : 0;
 */
         bSz     = len() - ( QuoddMsg.MINSZ + Blob.MINSZ );
         blob    = new String( b, off, bSz );
         _blob  += blob;
      } catch( StringIndexOutOfBoundsException e ) {
         err = e.getMessage();
         return this;
      }
      return this;
   }
}
