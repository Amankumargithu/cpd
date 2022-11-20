/**
 * OptionTypeColumnDecorator.java
 *
 * @author Copyright (c) 2004 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */
package com.b4utrade.web.decorator;


import com.b4utrade.util.B4UConstants;
import org.displaytag.decorator.ColumnDecorator;
import org.displaytag.exception.DecoratorException;



public class OptionTypeColumnDecorator implements ColumnDecorator
{

   public String decorate(Object columnValue) throws DecoratorException
   {

      try
      {
         if (((Integer)columnValue).intValue() == B4UConstants.OPTION_TYPE_CALL)
            return "CALL";
         return "PUT";
      }
      catch (Exception e)
      {
         return "";
      }
   }
}
