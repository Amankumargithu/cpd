/**
 * PriceColumnDecorator.java
 *
 * @author Copyright (c) 2004 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */
package com.b4utrade.web.decorator;

import java.text.DecimalFormat;

import org.displaytag.decorator.ColumnDecorator;
import org.displaytag.exception.DecoratorException;



public class PriceColumnDecorator implements ColumnDecorator
{
   private static final String doubleValueFormat = "###,###,###,##0.0000";


   public String decorate(Object columnValue) throws DecoratorException
   {
      DecimalFormat df = new DecimalFormat(doubleValueFormat);
      try
      {
         return(df.format(((Double)columnValue).doubleValue()));
      }
      catch (Exception e)
      {
         return "";
      }
   }
}
