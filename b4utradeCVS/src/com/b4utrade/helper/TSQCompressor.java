package com.b4utrade.helper;

import com.tacpoint.common.*;

import com.tacpoint.util.*;

import com.b4utrade.bean.TSQBean;
import com.b4utrade.tsq.TSQMessageSerializer;


import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * Responsible for serializing/compressing and de-serializing/de-compressing TSQBeans
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

public class TSQCompressor extends DefaultObject {


   public static final int ATTR_DELIM = 124; // |
   public static final int REC_DELIM  = 63;  // ?

   public static byte[] compress(Collection tsqs) {

      ByteArrayOutputStream baos = null;
      GZIPOutputStream zbaos = null;


      try {

         long beginTime = System.currentTimeMillis();

         if (tsqs == null || tsqs.size() == 0) return null;

         baos = new ByteArrayOutputStream(1024);
         zbaos = new GZIPOutputStream(baos);

         if (tsqs == null) return null;

         Iterator it = tsqs.iterator();

         int totalBytes = 0;
         byte[] bytes = null;

         while (it.hasNext()) {

            TSQBean bean = (TSQBean)it.next();
            bytes = TSQMessageSerializer.deflate(bean);
            totalBytes += bytes.length;
            zbaos.write(bytes);
            totalBytes++;
            zbaos.write(REC_DELIM);
         }

         zbaos.finish();

         bytes = baos.toByteArray();

         long endTime = System.currentTimeMillis();

         Logger.log("TSQCompressor.compress - uncompressed byte count : "+totalBytes+" compressed byte count : "+bytes.length);
         SystemOutLogger.debug("********** TSQCompressor.compress - uncompressed byte count : "+totalBytes+" compressed byte count : "+bytes.length);
         Logger.log("TSQCompressor.compress - time to compress tsqs beans : "+(endTime-beginTime));
         SystemOutLogger.debug("********** TSQCompressor.compress - time to compress TSQ beans : "+(endTime-beginTime));

         return bytes;

      }
      catch (Exception e) {
         Logger.log("TSQCompressor.compress - exception encountered : "+e.getMessage(),e);
         return null;
      }
      finally {
         if (zbaos != null)
            try {
               zbaos.close();
            }
            catch (Exception zex) {}
      }

   }

   private static byte[] unzip(byte[] data) {

      ByteArrayInputStream bais = null;
      GZIPInputStream zbais = null;

      try {
         bais = new ByteArrayInputStream(data);
         zbais = new GZIPInputStream(bais);

         ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length * 2);

         byte[] results = new byte[1024];

         int len = 0;
         while ((len = zbais.read(results, 0, results.length)) >= 0)
            baos.write(results,0,len);

         byte[] bytes = baos.toByteArray();

         Logger.log("TSQCompressor.unzip - compressed byte count : "+data.length+" uncompressed byte count : "+bytes.length);
         SystemOutLogger.debug("********** TSQCompressor.unzip - compressed byte count : "+data.length+" uncompressed byte count : "+bytes.length);

         return baos.toByteArray();
      }
      catch (Exception e) {
         Logger.log("TSQCompressor.unzip - exception encountered : "+e.getMessage(),e);
         return null;
      }
      finally {
         if (zbais != null)
            try {
               zbais.close();
               bais.close();
            }
            catch (Exception zex) {}
      }


   }

   public static Collection decompress(byte[] tsqs) {


      long beginTime = System.currentTimeMillis();

      ArrayList list = new ArrayList();

      if (tsqs == null || tsqs.length == 0) return list;

      tsqs = unzip(tsqs);

      long endTime = System.currentTimeMillis();

      Logger.log("TSQCompressor.decompress - time to decompress tsqs bytes : "+(endTime-beginTime));
      SystemOutLogger.debug("********** TSQCompressor.decompress - time to decompress tsqs bytes : "+(endTime-beginTime));

      beginTime = System.currentTimeMillis();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      int attributeCounter = 0;

      TSQBean bean = null;

      byte[] bytes = null;

      //Logger.log("tsqs DATA : "+new String(tsqs));

      for (int i=0; i<tsqs.length; i++) {

         if (tsqs[i] == REC_DELIM) {
            bytes = baos.toByteArray();
            if (bytes.length > 0)
              bean = TSQMessageSerializer.inflate(bytes);
            baos.reset();
            if (bean != null)
               list.add(bean);
         }
         else {
            baos.write(tsqs[i]);
         }
      }

      endTime = System.currentTimeMillis();

      Logger.log("TSQCompressor.decompress - time to inflate tsqs beans : "+(endTime-beginTime));
      SystemOutLogger.debug("********** TSQCompressor.decompress - time to inflate tsqs beans : "+(endTime-beginTime));

      /*
      Iterator it = list.iterator();
      while (it.hasNext()) {
         TSQBean nb = (TSQBean)it.next();
         SystemOutLogger.debug("********** tsqs Bean ID : "+nb.gettsqsID()+" Formatted tsqs date : "+nb.getFormattedtsqsDate());
      }
      */


      return list;

   }

}
