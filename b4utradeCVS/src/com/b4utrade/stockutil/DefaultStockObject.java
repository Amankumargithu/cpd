/** DefaultStockObject.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author Kim Gentes
* @author kgentes@tacpoint.com
* @version 1.0
* Date created: 5/8/2000
*/

package com.b4utrade.stockutil;

import com.tacpoint.common.DefaultObject;


/** DefaultStockObject class
* The DefaultStockObject class will indicates the type of a stock object.
*
*/

public class DefaultStockObject extends DefaultObject
{
	public int mStockType;
	//public boolean mFoundInHash = false;

	public DefaultStockObject()
	{
	}

   public void clone(DefaultStockObject aStock)
   {
      mStockType = aStock.mStockType;
      //mFoundInHash = aStock.mFoundInHash;
   }
}
